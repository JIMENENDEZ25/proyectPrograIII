/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.atencion.premium.controller;

/**
 *
 * @author mk
 */

import com.umg.callcenter.atencion.premium.conexion.ConexionServidor;
import com.umg.callcenter.atencion.premium.modelo.Atencion;
import com.umg.callcenter.atencion.premium.modelo.Cliente;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AtencionController {
     
    private final String tipoAgente;
    private final String nombreAgente;
    private final ConexionServidor conexion;
    private final Gson gson;
    private Cliente clienteActual;
    
    
    // Callbacks para la UI
    private Runnable onConectar;
    private Runnable onDesconectar;
    private Runnable onAtencionRegistrada;
    private java.util.function.Consumer<JsonObject> onClienteAsignado;
    private Runnable onClienteFinalizado;
    private Runnable onErrorConexion;
    private java.util.function.Consumer<String> onLog;
    private java.util.function.BiConsumer<Integer, Integer> onActualizarContadores;
    private java.util.function.Consumer<Cliente> onMostrarCliente;
    private java.util.function.Consumer<java.util.List<JsonObject>> onActualizarTabla;
    private java.util.function.Consumer<String> onMensajeChat;
    private java.util.function.Consumer<com.google.gson.JsonArray> onActualizarUsuariosChat;
    private java.util.function.Consumer<String> onError;

    
    private Timer heartbeatTimer;
    private Timer estadoColasTimer;
    private Timer actualizarTablaTimer;
    
    public AtencionController(String tipoAgente, String nombreAgente) {
        this.tipoAgente = tipoAgente.toLowerCase();
        this.nombreAgente = nombreAgente;
        this.conexion = new ConexionServidor();
        this.gson = new Gson();
    }
    
    // ========== CONFIGURACIÓN DE CALLBACKS ==========
    public void setOnAtencionRegistrada(Runnable callback) {
        this.onAtencionRegistrada = callback;
    }
    public void setOnConectar(Runnable callback) { this.onConectar = callback; }
    public void setOnDesconectar(Runnable callback) { this.onDesconectar = callback; }
    public void setOnClienteAsignado(java.util.function.Consumer<JsonObject> callback) {
        this.onClienteAsignado = callback;
    }
    public void setOnClienteFinalizado(Runnable callback) { this.onClienteFinalizado = callback; }
    public void setOnErrorConexion(Runnable callback) { this.onErrorConexion = callback; }
    public void setOnLog(java.util.function.Consumer<String> callback) { this.onLog = callback; }
    public void setOnActualizarContadores(java.util.function.BiConsumer<Integer, Integer> callback) { 
        this.onActualizarContadores = callback; 
    }
    public void setOnMostrarCliente(java.util.function.Consumer<Cliente> callback) { 
        this.onMostrarCliente = callback; 
    }
    public void setOnActualizarTabla(java.util.function.Consumer<java.util.List<JsonObject>> callback) { 
        this.onActualizarTabla = callback; 
    }
    public void setOnMensajeChat(java.util.function.Consumer<String> callback) {
        this.onMensajeChat = callback;
    }
    public void setOnActualizarUsuariosChat(java.util.function.Consumer<com.google.gson.JsonArray> callback) {
        this.onActualizarUsuariosChat = callback;
    }

    public void setOnError(java.util.function.Consumer<String> callback) {
        this.onError = callback;
    }

    
    
    private void log(String mensaje) {
        if (onLog != null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            onLog.accept("[" + timestamp + "] " + mensaje);
        }
    }
    
    // ========== MÉTODOS PÚBLICOS ==========
    
    public void conectar() {
        new Thread(() -> {
            boolean conectado = conexion.descubrirServidor();
            SwingUtilities.invokeLater(() -> {
                if (conectado) {
                    log("✅ Conectado al servidor como agente " + tipoAgente.toUpperCase());
                    enviarIdentificacion();
                    iniciarReceptorChat();
                    if (onConectar != null) onConectar.run();
                } else {
                    log("❌ Error al conectar: " + conexion.getUltimoError());
                    if (onErrorConexion != null) onErrorConexion.run();
                    reintentarConexion();
                }
            });
        }).start();
    }
    
    private void reintentarConexion() {
        Timer timer = new Timer(5000, (e) -> conectar());
        timer.setRepeats(false);
        timer.start();
    }
    
    private void enviarIdentificacion() {
        JsonObject identificacion = new JsonObject();
        identificacion.addProperty("comando", "IDENTIFICAR");
        identificacion.addProperty("tipo", tipoAgente);
        identificacion.addProperty("nombre", nombreAgente);
        conexion.enviarComando(identificacion);
        iniciarTimers();
    }
    
    private void iniciarTimers() {
        // Detener timers existentes si los hay
        if (heartbeatTimer != null) heartbeatTimer.stop();
        if (actualizarTablaTimer != null) actualizarTablaTimer.stop();
        if (estadoColasTimer != null) estadoColasTimer.stop();

        heartbeatTimer = new Timer(30000, (e) -> {
            if (conexion.estaConectado()) {
                JsonObject heartbeat = new JsonObject();
                heartbeat.addProperty("comando", "HEARTBEAT");
                conexion.enviarComando(heartbeat);
            }
        });
        heartbeatTimer.start();

        actualizarTablaTimer = new Timer(3000, (e) -> actualizarListaClientes());
        actualizarTablaTimer.start();

        estadoColasTimer = new Timer(5000, (e) -> actualizarEstadoColas());
        estadoColasTimer.start();
    }
    
    public void actualizarListaClientes() {
        if (!conexion.estaConectado()) return;
        JsonObject comando = new JsonObject();
        comando.addProperty("comando", "SOLICITAR_LISTA_CLIENTES");
        comando.addProperty("tipo", tipoAgente);
        conexion.enviarComando(comando);
    }

    private void actualizarEstadoColas() {
        if (!conexion.estaConectado()) return;
        JsonObject comando = new JsonObject();
        comando.addProperty("comando", "ESTADO_COLAS");
        conexion.enviarComando(comando);
    }
    
    public void tomarSiguienteCliente() {
        if (!conexion.estaConectado()) {
            log("❌ No hay conexión con el servidor");
            return;
        }
        JsonObject solicitud = new JsonObject();
        solicitud.addProperty("comando", "SOLICITAR_CLIENTE");
        solicitud.addProperty("tipo", tipoAgente);
        conexion.enviarComando(solicitud);
    }

    public void tomarClienteSeleccionado(String ticket) {
        if (!conexion.estaConectado()) {
            log("❌ No hay conexión con el servidor");
            return;
        }
        JsonObject solicitud = new JsonObject();
        solicitud.addProperty("comando", "TOMAR_CLIENTE_ESPECIFICO");
        solicitud.addProperty("ticket", ticket);
        conexion.enviarComando(solicitud);
    }
    
    public void finalizarAtencion(String motivo, int duracion) {
        if (clienteActual == null) {
            log("❌ No hay cliente actual para finalizar");
            return;
        }

        JsonObject finalizacion = new JsonObject();
        finalizacion.addProperty("comando", "FINALIZAR_ATENCION");
        finalizacion.addProperty("ticket", clienteActual.getTicket());
        finalizacion.addProperty("dpi", clienteActual.getDpi());
        finalizacion.addProperty("motivo", motivo);
        finalizacion.addProperty("duracion", duracion);
        finalizacion.addProperty("agente", nombreAgente);
        finalizacion.addProperty("tipoAtencion", tipoAgente);

        conexion.enviarComandoAsync(finalizacion);
        log("📤 Enviando finalización para " + clienteActual.getTicket());
    }

    public void cambiarIpServidor(String nuevaIp) {
        if (nuevaIp == null || nuevaIp.isEmpty()) return;
        ConexionServidor.setIpServidor(nuevaIp);
        log("🔄 IP cambiada a: " + nuevaIp + ", reconectando...");
        desconectar();
        conectar();
    }
    
    public void desconectar() {
        if (heartbeatTimer != null) heartbeatTimer.stop();
        if (estadoColasTimer != null) estadoColasTimer.stop();
        if (actualizarTablaTimer != null) actualizarTablaTimer.stop();
        conexion.desconectar();
    }
    
    public String getNombreAgente() {
        return nombreAgente;
    }
    
    public boolean estaConectado() {
        return conexion.estaConectado();
    }
    
    public String getHostActual() {
        return conexion.getHostActual();
    }
    
    public Cliente getClienteActual() {
        return clienteActual;
    }   
    
    public void iniciarReceptorChat() {
        System.out.println("[DEBUG] Iniciando receptor de chat en Atención");
        conexion.addListener(respuesta -> {
            System.out.println("[DEBUG] Respuesta recibida: " + respuesta);
            try {
                JsonObject json = gson.fromJson(respuesta, JsonObject.class);
                String estado = json.get("estado").getAsString();

                switch (estado) {
                    case "NUEVO_MENSAJE_CHAT":
                        String emisor = json.get("emisor").getAsString();
                        String mensaje = json.get("mensaje").getAsString();
                        String hora = json.get("hora").getAsString();
                        if (onMensajeChat != null) {
                            SwingUtilities.invokeLater(()
                                    -> onMensajeChat.accept("[" + hora + "] " + emisor + ": " + mensaje)
                            );
                        }
                        break;

                    case "ACTUALIZAR_USUARIOS_CHAT":  // ← CLAVE: manejar este estado
                    case "LISTA_USUARIOS_CHAT":
                        if (onActualizarUsuariosChat != null) {
                            SwingUtilities.invokeLater(()
                                    -> onActualizarUsuariosChat.accept(json.get("usuarios").getAsJsonArray())
                            );
                        }
                        break;

                    case "IDENTIFICADO":
                        log("✅ " + json.get("mensaje").getAsString());
                        break;

                    case "ATENCION_REGISTRADA":
                        log("✅ " + json.get("mensaje").getAsString());
                        this.clienteActual = null;
                        if (onAtencionRegistrada != null) {
                            SwingUtilities.invokeLater(() -> onAtencionRegistrada.run());
                        }
                        break;

                    case "MODO_CAMBIADO":
                        String nuevoModo = json.get("modo").getAsString();
                        log("✅ Modo cambiado a: " + nuevoModo.toUpperCase());
                        break;

                    case "CLIENTE_ASIGNADO":
                        if (onClienteAsignado != null) {
                            SwingUtilities.invokeLater(() -> onClienteAsignado.accept(json));
                        }
                        break;

                    case "LISTA_CLIENTES":
                        if (onActualizarTabla != null) {
                            var clientes = json.get("clientes").getAsJsonArray();
                            java.util.List<JsonObject> lista = new java.util.ArrayList<>();
                            for (var elem : clientes) {
                                lista.add(elem.getAsJsonObject());
                            }
                            SwingUtilities.invokeLater(() -> onActualizarTabla.accept(lista));
                        }
                        break;

                    case "ESTADO_COLAS":
                        if (onActualizarContadores != null) {
                            int normal = json.get("normal").getAsInt();
                            int premium = json.get("premium").getAsInt();
                            SwingUtilities.invokeLater(() -> onActualizarContadores.accept(normal, premium));
                        }
                        break;

                    case "ERROR":
                        if (onError != null) {
                            String errorMsg = json.has("mensaje") ? json.get("mensaje").getAsString() : "Error desconocido";
                            SwingUtilities.invokeLater(() -> onError.accept(errorMsg));
                        }
                        break;

                    default:
                        System.out.println("[DEBUG] Estado no manejado: " + estado);
                }
            } catch (Exception e) {
                System.err.println("[DEBUG] Error parseando: " + e.getMessage());
            }
        });
    }
    
    public void enviarMensajeChat(String mensaje) {
        JsonObject comando = new JsonObject();
        comando.addProperty("comando", "ENVIAR_MENSAJE_CHAT");
        comando.addProperty("mensaje", mensaje);
        conexion.enviarComando(comando);
    }

    public void solicitarUsuariosChat() {
        JsonObject comando = new JsonObject();
        comando.addProperty("comando", "SOLICITAR_USUARIOS_CHAT");
        conexion.enviarComando(comando);
    }
    
    public void setClienteActual(Cliente cliente) {
        this.clienteActual = cliente;
        System.out.println("[CONTROLLER] Cliente actual establecido: " + 
            (cliente != null ? cliente.getTicket() : "null"));
    }
}
