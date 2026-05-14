/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.server;

/**
 *
 * @authors mk, natr, olga, jimem
 */

import com.umg.callcenter.system.model.Atencion;
import com.umg.callcenter.system.model.Cliente;
import com.umg.callcenter.system.controller.CentralServerController;
import com.umg.callcenter.system.persistence.HashTableAtenciones;
import com.umg.callcenter.system.protocol.Comando;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.umg.callcenter.system.persistence.PersistenciaClientes;
import com.umg.callcenter.system.queue.GestorColas;
import com.umg.callcenter.system.ui.CentralServerUI;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.Timer;

public class ClienteHandler implements Runnable {
    private final Socket socket;
    private final String identificador;
    private final GestorColas gestorColas;
    private final PersistenciaWorker persistenciaWorker;
    private final HashTableAtenciones tablaHash;
    private final ServidorCentral servidor;
    private final CentralServerUI ui;
    private final Gson gson;
    private String tipoCliente;
    private DataOutputStream salida;
    private DataInputStream entrada;
    private String nombreChat;
    private String tipoChat;
    private Timer heartbeatTimeout;
    private volatile boolean ultimoHeartbeatRecibido = true;
    private static final int HEARTBEAT_TIMEOUT_MS = 60000;
    private static final int MAX_COMANDOS_POR_SEGUNDO = 10;
    private static final int MAX_MENSAJE_SIZE = 1024 * 1024;
    private static final Map<String, Queue<Long>> requestTimestamps = new ConcurrentHashMap<>();
    
    public ClienteHandler(Socket socket, String identificador, 
                         GestorColas gestorColas, PersistenciaWorker persistenciaWorker,
                         HashTableAtenciones tablaHash, ServidorCentral servidor,
                         CentralServerUI ui) {
        this.socket = socket;
        this.identificador = identificador;
        this.gestorColas = gestorColas;
        this.persistenciaWorker = persistenciaWorker;
        this.tablaHash = tablaHash;
        this.servidor = servidor;
        this.ui = ui;
        this.gson = new Gson();
        
        try {
            socket.setSoTimeout(300000);
            this.salida = new DataOutputStream(socket.getOutputStream());
            this.entrada = new DataInputStream(socket.getInputStream());
            iniciarHeartbeatMonitor();
        } catch (IOException e) {
            ui.logs("Error creando flujos para " + identificador);
        }
    }
    
    private boolean tieneCampos(JsonObject json, String... campos) {
        for (String campo : campos) {
            if (!json.has(campo) || json.get(campo).isJsonNull()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                try {
                    String mensajeJson = entrada.readUTF();
                    ui.logs("[" + identificador + "] Recibido: " + mensajeJson);
                    procesarMensaje(mensajeJson);
                } catch (SocketTimeoutException e) {
                    ui.logs("Timeout - " + identificador + " inactivo");
                    break;
                }
            }
        } catch (IOException e) {
            ui.logs("Cliente desconectado: " + identificador);
        } finally {
            cerrarConexion();
        }
    }
    
    private void procesarMensaje(String mensajeJson) throws IOException {
        if (mensajeJson.length() > MAX_MENSAJE_SIZE) {
            enviarRespuesta("ERROR", "Mensaje demasiado grande");
            return;
        }
        
        String ip = socket.getInetAddress().getHostAddress();
        
        if (!checkRateLimit(ip)) {
            enviarRespuesta("ERROR", "Demasiadas solicitudes. Espere un momento.");
            return;
        }
        
        JsonObject json;
        try {
            json = gson.fromJson(mensajeJson, JsonObject.class);
        } catch (Exception e) {
            enviarRespuesta("ERROR", "JSON inválido");
            return;
        }
        
        if (!json.has("comando")) {
            enviarRespuesta("ERROR", "Comando no especificado");
            return;
        }
        
        String comandoStr = json.get("comando").getAsString();
        Comando comando;
        
        try {
            comando = Comando.valueOf(comandoStr);
        } catch (IllegalArgumentException e) {
            enviarRespuesta("ERROR", "Comando no reconocido: " + comandoStr);
            return;
        }
        
        switch (comando) {
            case REGISTRAR:
                registrarCliente(json);
                break;
            case SOLICITAR_CLIENTE:
                solicitarCliente(json);
                break;
            case FINALIZAR_ATENCION:
                finalizarAtencion(json);
                break;
            case BUSCAR_POR_DPI:
                buscarPorDPI(json);
                break;
            case ESTADO_COLAS:
                enviarEstadoColas();
                break;
            case HEARTBEAT:
                ultimoHeartbeatRecibido = true;
                break;
            case IDENTIFICAR:
                identificarCliente(json);
                break;
            case SOLICITAR_LISTA_CLIENTES:
                enviarListaClientes(json);
                break;
            case TOMAR_CLIENTE_ESPECIFICO:
                tomarClienteEspecifico(json);
                break;
            case ENVIAR_MENSAJE_CHAT:
                procesarMensajeChat(json);
                break;
            case SOLICITAR_USUARIOS_CHAT:
                enviarListaUsuariosChat();
                break;
            case CAMBIAR_MODO:
                cambiarModo(json);
                break;
            default:
                enviarRespuesta("ERROR", "Comando no implementado: " + comando);
        }
    }
    
    private void procesarMensajeChat(JsonObject json) throws IOException {
        if (!tieneCampos(json, "mensaje")) {
            enviarRespuesta("ERROR", "Mensaje vacío");
            return;
        }

        String mensaje = json.get("mensaje").getAsString();
        servidor.broadcastMensajeChat(this, mensaje);
    }

    private void enviarListaUsuariosChat() throws IOException {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("estado", "LISTA_USUARIOS_CHAT");
        respuesta.add("usuarios", servidor.getUsuariosChat());
        salida.writeUTF(gson.toJson(respuesta));
        salida.flush();
    }

    private void iniciarHeartbeatMonitor() {
        heartbeatTimeout = new Timer(HEARTBEAT_TIMEOUT_MS, e -> {
            if (!ultimoHeartbeatRecibido) {
                ui.logs("Cliente " + identificador + " sin heartbeat - cerrando conexión");
                cerrarConexion();
            }
            ultimoHeartbeatRecibido = false;
        });
        heartbeatTimeout.setRepeats(true);
        heartbeatTimeout.start();
    }
    
    public void recibirMensajeChat(String emisor, String mensaje, String tipoEmisor) {
        try {
            JsonObject chatMsg = new JsonObject();
            chatMsg.addProperty("estado", "NUEVO_MENSAJE_CHAT");
            chatMsg.addProperty("emisor", emisor);
            chatMsg.addProperty("mensaje", mensaje);
            chatMsg.addProperty("tipo", tipoEmisor);
            chatMsg.addProperty("hora", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            salida.writeUTF(gson.toJson(chatMsg));
            salida.flush();
        } catch (IOException e) {
            // Cliente desconectado, ignorar
        }
    }
    
    private void registrarCliente(JsonObject json) throws IOException {
        if (!tieneCampos(json, "dpi", "nombre", "apellido", "tipo")) {
            enviarRespuesta("ERROR", "JSON incompleto - faltan campos requeridos");
            return;
        }
        
        String dpi = json.get("dpi").getAsString().trim();
        String nombre = json.get("nombre").getAsString().trim();
        String apellido = json.get("apellido").getAsString().trim();
        String tipo = json.get("tipo").getAsString().toLowerCase();
        
        if (dpi.isEmpty() || nombre.isEmpty() || apellido.isEmpty()) {
            enviarRespuesta("ERROR", "Campos vacíos");
            return;
        }
        
        if (!dpi.matches("\\d+")) {
            enviarRespuesta("ERROR", "DPI debe contener solo números");
            return;
        }
        
        if (nombre.length() > 50 || apellido.length() > 50) {
            enviarRespuesta("ERROR", "Nombre o apellido demasiado largo");
            return;
        }
        
        Cliente nuevoCliente = new Cliente(dpi, nombre, apellido, tipo);
        PersistenciaClientes.guardar(nuevoCliente);
        servidor.encolarCliente(nuevoCliente);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            ui.agregarTicketTabla(nuevoCliente);
            ui.actualizarContadores(servidor.tamanioNormal(), servidor.tamanioPremium());
        });
        
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("estado", "TICKET_GENERADO");
        respuesta.addProperty("ticket", nuevoCliente.getNumeroTicket());
        respuesta.addProperty("horaIngreso", nuevoCliente.getHoraIngresoFormateada());
        salida.writeUTF(gson.toJson(respuesta));
        salida.flush();
        
        ui.logs("Nuevo cliente registrado: " + nuevoCliente.getNumeroTicket());
    }
    
    private void solicitarCliente(JsonObject json) throws IOException {
        // Obtener el tipo que está solicitando (puede venir del JSON o usar el almacenado)
        String tipoSolicitado = json.has("tipo") ? json.get("tipo").getAsString() : this.tipoCliente;

        Cliente cliente = null;

        // Buscar cliente del tipo solicitado (con prioridad)
        List<Cliente> pendientes = gestorColas.getPendientes();
        for (Cliente c : pendientes) {
            if (c.getTipo().equals(tipoSolicitado)) {
                cliente = c;
                gestorColas.removerCliente(c.getNumeroTicket());
                break;
            }
        }

        JsonObject respuesta = new JsonObject();
        if (cliente != null) {
            // Marcar como atendido localmente
            PersistenciaClientes.marcarComoAtendido(cliente.getNumeroTicket());

            respuesta.addProperty("estado", "CLIENTE_ASIGNADO");
            respuesta.addProperty("ticket", cliente.getNumeroTicket());
            respuesta.addProperty("dpi", cliente.getDpi());
            respuesta.addProperty("nombre", cliente.getNombre());
            respuesta.addProperty("apellido", cliente.getApellido());
            respuesta.addProperty("tipo", cliente.getTipo());
            ui.logs("Cliente " + tipoSolicitado + " asignado a " + identificador + ": " + cliente.getNumeroTicket());

            // Actualizar UI del servidor
            javax.swing.SwingUtilities.invokeLater(() -> {
                servidor.cargarPendientesEnUI();
                ui.actualizarContadores(servidor.tamanioNormal(), servidor.tamanioPremium());
            });
        } else {
            respuesta.addProperty("estado", "SIN_CLIENTES");
            ui.logs("No hay clientes " + tipoSolicitado + " en espera para " + identificador);
        }

        salida.writeUTF(gson.toJson(respuesta));
        salida.flush();
    }
    
    private void finalizarAtencion(JsonObject json) throws IOException {
        if (!tieneCampos(json, "ticket", "dpi", "motivo", "duracion", "agente", "tipoAtencion")) {
            enviarRespuesta("ERROR", "JSON incompleto para finalizar atención");
            return;
        }
        
        String ticket = json.get("ticket").getAsString();
        String dpi = json.get("dpi").getAsString();
        String motivo = json.get("motivo").getAsString();
        int duracion = json.get("duracion").getAsInt();
        String agente = json.get("agente").getAsString();
        String tipoAtencion = json.get("tipoAtencion").getAsString();
        
        if (duracion < 0 || duracion > 480) {
            enviarRespuesta("ERROR", "Duración inválida (0-480 minutos)");
            return;
        }
        
        PersistenciaClientes.marcarComoAtendido(ticket);
        servidor.moverClienteAAntendidos(ticket);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            servidor.cargarPendientesEnUI();
            servidor.cargarAtendidosEnUI();
        });
        
        Atencion atencion = new Atencion(ticket, dpi, motivo, duracion, agente, tipoAtencion);
        persistenciaWorker.guardarAtencion(atencion);
        
        enviarRespuesta("ATENCION_REGISTRADA", "Atención guardada exitosamente");
        ui.logs("Atención finalizada: " + ticket + " por " + agente);
    }
    
    private void buscarPorDPI(JsonObject json) throws IOException {
        if (!tieneCampos(json, "dpi")) {
            enviarRespuesta("ERROR", "Se requiere DPI para búsqueda");
            return;
        }
        
        String dpi = json.get("dpi").getAsString().trim();
        
        if (dpi.isEmpty()) {
            JsonObject respuesta = new JsonObject();
            respuesta.addProperty("estado", "RESULTADOS_BUSQUEDA");
            respuesta.addProperty("total", 0);
            respuesta.addProperty("datos", "[]");
            salida.writeUTF(gson.toJson(respuesta));
            salida.flush();
            return;
        }
        
        var resultados = tablaHash.buscar(dpi);
        
        com.google.gson.JsonArray resultadosArray = new com.google.gson.JsonArray();
        
        for (Atencion a : resultados) {
            JsonObject item = new JsonObject();
            item.addProperty("ticket", a.getNumeroTicket());
            item.addProperty("fecha", a.getFechaHora());
            item.addProperty("motivo", a.getMotivo());
            item.addProperty("duracion", a.getDuracionMinutos());
            item.addProperty("agente", a.getUsuarioAgente());
            item.addProperty("tipo", a.getTipoAtencion());
            resultadosArray.add(item);
        }
        
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("estado", "RESULTADOS_BUSQUEDA");
        respuesta.addProperty("total", resultados.size());
        respuesta.add("datos", resultadosArray);
        
        salida.writeUTF(gson.toJson(respuesta));
        salida.flush();
        
        ui.logs("Búsqueda para DPI: " + dpi + " - " + resultados.size() + " resultados");
    }
    
    private void enviarEstadoColas() throws IOException {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("estado", "ESTADO_COLAS");
        respuesta.addProperty("normal", servidor.tamanioNormal());
        respuesta.addProperty("premium", servidor.tamanioPremium());
        salida.writeUTF(gson.toJson(respuesta));
        salida.flush();
    }
    
    private void identificarCliente(JsonObject json) throws IOException {
        String tipo = json.get("tipo").getAsString();
        String nombre = json.has("nombre") ? json.get("nombre").getAsString() : "";

        this.tipoChat = tipo;
        this.nombreChat = nombre.isEmpty() ? tipo + " " + identificador : nombre;

        servidor.actualizarTipoEquipo(identificador, tipo, nombre);
        servidor.registrarUsuarioChat(identificador, this.nombreChat, tipo, this);

        enviarRespuesta("IDENTIFICADO", "Bienvenido " + tipo);
    }
    
    private void enviarListaClientes(JsonObject json) throws IOException {
        // Puede venir el tipo en el JSON o usar el almacenado
        String tipo = json.has("tipo") ? json.get("tipo").getAsString() : this.tipoCliente;

        List<Cliente> pendientes = gestorColas.getPendientes();

        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("estado", "LISTA_CLIENTES");
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();

        for (Cliente c : pendientes) {
            if (c.getTipo().equals(tipo)) {
                JsonObject item = new JsonObject();
                item.addProperty("ticket", c.getNumeroTicket());
                item.addProperty("dpi", c.getDpi());
                item.addProperty("nombre", c.getNombre() + " " + c.getApellido());
                item.addProperty("hora", c.getHoraIngresoFormateada());
                array.add(item);
            }
        }
        respuesta.add("clientes", array);
        salida.writeUTF(gson.toJson(respuesta));
        salida.flush();
    }

    private void tomarClienteEspecifico(JsonObject json) throws IOException {
        if (!tieneCampos(json, "ticket")) {
            enviarRespuesta("ERROR", "Se requiere ticket");
            return;
        }

        String ticket = json.get("ticket").getAsString();
        Cliente cliente = null;

        List<Cliente> pendientes = gestorColas.getPendientes();
        for (Cliente c : pendientes) {
            if (c.getNumeroTicket().equals(ticket)) {
                cliente = c;
                break;
            }
        }

        if (cliente != null) {
            gestorColas.removerCliente(ticket);
            
            JsonObject respuesta = new JsonObject();
            respuesta.addProperty("estado", "CLIENTE_ASIGNADO");
            respuesta.addProperty("ticket", cliente.getNumeroTicket());
            respuesta.addProperty("dpi", cliente.getDpi());
            respuesta.addProperty("nombre", cliente.getNombre());
            respuesta.addProperty("apellido", cliente.getApellido());
            respuesta.addProperty("tipo", cliente.getTipo());
            salida.writeUTF(gson.toJson(respuesta));
            salida.flush();

            ui.logs("Cliente específico asignado: " + ticket + " a " + identificador);

            javax.swing.SwingUtilities.invokeLater(() -> {
                ui.actualizarContadores(servidor.tamanioNormal(), servidor.tamanioPremium());
                servidor.cargarPendientesEnUI();
            });
        } else {
            enviarRespuesta("ERROR", "Cliente no encontrado o ya fue asignado");
        }
    }

    private boolean checkRateLimit(String ip) {
        long ahora = System.currentTimeMillis();
        Queue<Long> timestamps = requestTimestamps.computeIfAbsent(ip, 
            k -> new java.util.LinkedList<>());
        
        synchronized(timestamps) {
            while (!timestamps.isEmpty() && timestamps.peek() < ahora - 1000) {
                timestamps.poll();
            }
            
            if (timestamps.size() >= MAX_COMANDOS_POR_SEGUNDO) {
                ui.logs("⚠️ Rate limit excedido para IP: " + ip);
                return false;
            }
            
            timestamps.offer(ahora);
            return true;
        }
    }

    private void enviarRespuesta(String estado, String mensaje) throws IOException {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("estado", estado);
        respuesta.addProperty("mensaje", mensaje);
        salida.writeUTF(gson.toJson(respuesta));
        salida.flush();
    }
    
    public void enviarRaw(String jsonStr) throws IOException {
        salida.writeUTF(jsonStr);
        salida.flush();
    }
    
    private void cambiarModo(JsonObject json) throws IOException {
        if (!tieneCampos(json, "modo", "agente")) {
            enviarRespuesta("ERROR", "Faltan campos para cambiar modo");
            return;
        }

        String nuevoModo = json.get("modo").getAsString().toLowerCase();
        String agente = json.get("agente").getAsString();

        if (!nuevoModo.equals("normal") && !nuevoModo.equals("premium")) {
            enviarRespuesta("ERROR", "Modo inválido. Use 'normal' o 'premium'");
            return;
        }

        // Actualizar el modo en el servidor
        this.tipoCliente = nuevoModo;
        servidor.actualizarModoAgente(identificador, nuevoModo);

        ui.logs("🔄 Agente " + agente + " cambió a modo " + nuevoModo.toUpperCase());

        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("estado", "MODO_CAMBIADO");
        respuesta.addProperty("modo", nuevoModo);
        respuesta.addProperty("mensaje", "Modo cambiado a " + nuevoModo.toUpperCase());
        salida.writeUTF(gson.toJson(respuesta));
        salida.flush();
    }
    
    private void cerrarConexion() {
        if (heartbeatTimeout != null) {
            heartbeatTimeout.stop();
        }
        servidor.removerEquipo(identificador);
        servidor.removerUsuarioChat(identificador);
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            ui.logs("Error cerrando conexión: " + e.getMessage());
        }
    }
}
