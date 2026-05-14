/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.registro.controller;

/**
 *
 * @authors mk, natr, olga, jimem
 */

import com.umg.callcenter.registro.conexion.ConexionServidor;
import com.umg.callcenter.registro.modelo.Cliente;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RegistroController {
        
    private final ConexionServidor conexion;
    private final Gson gson;
    private String nombreOperador;  // ← NUEVO
    private String ultimoTicket;
    private String ultimoDPI;
    private java.util.function.Consumer<String> onMensajeChat;
    private java.util.function.Consumer<com.google.gson.JsonArray> onActualizarUsuariosChat;
    private Timer heartbeatTimer;
    private Timer reconectarTimer;
    
    // Callbacks para la UI
    private Runnable onConectar;
    private Runnable onDesconectar;
    private Runnable onErrorConexion;
    private java.util.function.Consumer<String> onLog;
    private java.util.function.Consumer<String> onMostrarExito;
    private java.util.function.Consumer<String> onMostrarError;
    private List<String> historialChat = new ArrayList<>(100);
    private static final int MAX_HISTORIAL = 100;
    private Queue<String> mensajesPendientes = new LinkedList<>();
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    
    public RegistroController(String nombreOperador) {  // ← MODIFICAR CONSTRUCTOR
        this.conexion = new ConexionServidor();
        this.gson = new Gson();
        this.nombreOperador = nombreOperador;
    }
    
    // ========== CONFIGURACIÓN DE CALLBACKS ==========
    
    public void setOnConectar(Runnable callback) { this.onConectar = callback; }
    
    public void setOnDesconectar(Runnable callback) { this.onDesconectar = callback; }
    
    public void setOnErrorConexion(Runnable callback) { this.onErrorConexion = callback; }
    
    public void setOnLog(java.util.function.Consumer<String> callback) { this.onLog = callback; }
    
    public void setOnMostrarExito(java.util.function.Consumer<String> callback) { this.onMostrarExito = callback; }
    
    public void setOnMostrarError(java.util.function.Consumer<String> callback) { this.onMostrarError = callback; }
    
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
                    log("✅ Conectado al servidor en " + conexion.getHostActual());
                    enviarIdentificacion();
                    iniciarReceptorChat();
                    iniciarHeartbeat();  // ← AGREGAR
                    if (onConectar != null) {
                        onConectar.run();
                    }
                } else {
                    log("❌ Error al conectar: " + conexion.getUltimoError());
                    if (onErrorConexion != null) {
                        onErrorConexion.run();
                    }
                    reconectar();  // ← AGREGAR
                }
            });
        }).start();
    }
    
    private void iniciarHeartbeat() {
        if (heartbeatTimer != null) heartbeatTimer.stop();

        heartbeatTimer = new Timer(30000, e -> {
            if (estaConectado()) {
                JsonObject heartbeat = new JsonObject();
                heartbeat.addProperty("comando", "HEARTBEAT");
                conexion.enviarComandoAsync(heartbeat);
            } else {
                log("⚠️ Heartbeat falló - intentando reconectar...");
                reconectar();
                enviarMensajesPendientes();
            }
        });
        heartbeatTimer.start();
    }

    private void reconectar() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            log("❌ Máximos intentos de reconexión alcanzados. Reinicie la aplicación.");
            return;
        }
        reconnectAttempts++;
        if (reconectarTimer != null && reconectarTimer.isRunning()) {
            return;
        }

        reconectarTimer = new Timer(5000, e -> {
            log("🔄 Intentando reconectar al servidor...");
            conectar();
            reconectarTimer.stop();
        });
        reconectarTimer.setRepeats(false);
        reconectarTimer.start();
    }

    private void reintentarConexion() {
        Timer timer = new Timer(5000, (e) -> conectar());
        timer.setRepeats(false);
        timer.start();
    }
    
    private void enviarIdentificacion() {
        JsonObject identificacion = new JsonObject();
        identificacion.addProperty("comando", "IDENTIFICAR");
        identificacion.addProperty("tipo", "registro");
        identificacion.addProperty("nombre", nombreOperador);  // ← usar variable
        conexion.enviarComando(identificacion);
    }
    
    public void cambiarIpServidor(String nuevaIp) {
        if (nuevaIp == null || nuevaIp.isEmpty()) return;
        if (!nuevaIp.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}") && !nuevaIp.equals("localhost")) {
            log("⚠️ IP no válida: " + nuevaIp);
            return;
        }
        ConexionServidor.setIpServidor(nuevaIp);
        log("🔄 IP cambiada a: " + nuevaIp + ", reconectando...");
        desconectar();
        conectar();
    }
    
    public void registrarCliente(String dpi, String nombre, String apellido, String tipo) {
        if (!conexion.estaConectado()) {
            if (onMostrarError != null) {
                onMostrarError.accept("No hay conexión con el servidor");
            }
            return;
        }
        
        Cliente cliente = new Cliente(dpi, nombre, apellido, tipo);
        ultimoDPI = dpi;
        
        new Thread(() -> {
            JsonObject comando = cliente.toJsonRegistro();
            String respuesta = conexion.enviarComando(comando);
            
            SwingUtilities.invokeLater(() -> {
                if (respuesta == null || respuesta.startsWith("ERROR")) {
                    String error = respuesta != null ? respuesta : "Error desconocido";
                    if (onMostrarError != null) {
                        onMostrarError.accept("Error del servidor: " + error);
                    }
                    return;
                }
                
                try {
                    JsonObject respuestaJson = gson.fromJson(respuesta, JsonObject.class);
                    String estado = respuestaJson.get("estado").getAsString();
                    
                    if (estado.equals("TICKET_GENERADO")) {
                        ultimoTicket = respuestaJson.get("ticket").getAsString();
                        cliente.setNumeroTicket(ultimoTicket);
                        
                        // Construir mensaje de éxito
                        StringBuilder sb = new StringBuilder();
                        sb.append("========================================\n");
                        sb.append("     CLIENTE REGISTRADO EXITOSAMENTE\n");
                        sb.append("========================================\n");
                        sb.append("Ticket: ").append(ultimoTicket).append("\n");
                        sb.append("DPI:    ").append(dpi).append("\n");
                        sb.append("Nombre: ").append(nombre).append(" ").append(apellido).append("\n");
                        sb.append("Tipo:   ").append(tipo.toUpperCase()).append("\n");
                        sb.append("Hora:   ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
                        sb.append("Estado: 🟡 PENDIENTE\n");
                        sb.append("========================================");
                        
                        if (onMostrarExito != null) {
                            onMostrarExito.accept(sb.toString());
                        }
                        log("Nuevo cliente registrado: " + ultimoTicket);
                    } else {
                        String mensaje = respuestaJson.has("mensaje") 
                            ? respuestaJson.get("mensaje").getAsString() 
                            : "Error desconocido";
                        if (onMostrarError != null) {
                            onMostrarError.accept(mensaje);
                        }
                    }
                } catch (Exception e) {
                    if (onMostrarError != null) {
                        onMostrarError.accept("Error procesando respuesta: " + e.getMessage());
                    }
                }
            });
        }).start();
    }
    
    public void desconectar() {
        if (heartbeatTimer != null) {
            heartbeatTimer.stop();
            heartbeatTimer = null;
        }
        if (reconectarTimer != null) {
            reconectarTimer.stop();
            reconectarTimer = null;
        }
        conexion.desconectar();
        if (onDesconectar != null) {
            onDesconectar.run();
        }
    }
    
    public boolean estaConectado() {
        return conexion.estaConectado();
    }
    
    public String getHostActual() {
        return conexion.getHostActual();
    }
    
    public String getIpActual() {
        return conexion.IP_SERVIDOR;
    }
    
    // ========== MÉTODOS DE CHAT ==========

    public void enviarMensajeChat(String mensaje) {
        if (!estaConectado()) {
            mensajesPendientes.offer(mensaje);  // ← Guardar si no hay conexión
            log("📝 Mensaje guardado para enviar cuando reconecte");
            return;
        }
        JsonObject comando = new JsonObject();
        comando.addProperty("comando", "ENVIAR_MENSAJE_CHAT");
        comando.addProperty("mensaje", mensaje);
        new Thread(() -> conexion.enviarComandoAsync(comando)).start();  // ← Usar Async
    }

    public void solicitarUsuariosChat() {
        JsonObject comando = new JsonObject();
        comando.addProperty("comando", "SOLICITAR_USUARIOS_CHAT");
        new Thread(() -> conexion.enviarComandoAsync(comando)).start();  // ← Usar Async
    }

    public void iniciarReceptorChat() {
        System.out.println("[DEBUG] Iniciando receptor de chat"); // ← AGREGAR
        conexion.addListener(respuesta -> {
            System.out.println("[DEBUG] Respuesta recibida: " + respuesta); // ← AGREGAR
            try {
                JsonObject json = gson.fromJson(respuesta, JsonObject.class);
                String estado = json.get("estado").getAsString();

                if (estado.equals("NUEVO_MENSAJE_CHAT")) {
                    String emisor = json.get("emisor").getAsString();
                    String mensaje = json.get("mensaje").getAsString();
                    String hora = json.get("hora").getAsString();
                    if (onMensajeChat != null) {
                        SwingUtilities.invokeLater(() -> 
                            onMensajeChat.accept("[" + hora + "] " + emisor + ": " + mensaje)
                        );
                    }
                } else if (estado.equals("ACTUALIZAR_USUARIOS_CHAT")) {
                    if (onActualizarUsuariosChat != null) {
                        SwingUtilities.invokeLater(() -> 
                            onActualizarUsuariosChat.accept(json.get("usuarios").getAsJsonArray())
                        );
                    }
                } else if (estado.equals("LISTA_USUARIOS_CHAT")) {
                    System.out.println("[DEBUG] Procesando LISTA_USUARIOS_CHAT"); // ← AGREGAR
                    if (onActualizarUsuariosChat != null) {
                        com.google.gson.JsonArray usuarios = json.get("usuarios").getAsJsonArray();
                        System.out.println("[DEBUG] Llamando callback con " + usuarios.size() + " usuarios");
                        SwingUtilities.invokeLater(() -> onActualizarUsuariosChat.accept(usuarios));
                    } else {
                        System.err.println("[DEBUG] onActualizarUsuariosChat es NULL");
                    }
                }
            } catch (Exception e) {
                // No es un mensaje de chat, ignorar
            }
        });
    }
    
    public void setOnMensajeChat(java.util.function.Consumer<String> callback) {
        this.onMensajeChat = callback;
    }

    public void setOnActualizarUsuariosChat(java.util.function.Consumer<com.google.gson.JsonArray> callback) {
        this.onActualizarUsuariosChat = callback;
    }

    public void guardarMensajeLocal(String mensaje) {
        historialChat.add(mensaje);
        if (historialChat.size() > MAX_HISTORIAL) {
            historialChat.remove(0);
        }
    }
    
    private void enviarMensajesPendientes() {
        while (!mensajesPendientes.isEmpty()) {
            String msg = mensajesPendientes.poll();
            enviarMensajeChat(msg);
        }
    }
    
}
