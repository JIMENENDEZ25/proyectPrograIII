/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.registro.conexion;

/**
 *
 * @author mk
 */

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.swing.Timer;

public class ConexionServidor {

    private static final int SO_TIMEOUT = 30000;      // 30 seg (bien)
    private static final int HEARTBEAT_INTERVAL = 25000; // 25 seg (bien)
    private static final int RECONNECT_DELAY = 5000;  // 5 seg (bien)
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    public static final int PUERTO = 5000;
    public static String IP_SERVIDOR = "localhost";
    public static final String IP_PREDEFINIDA = "25.4.40.151";
    public static boolean usarIpPredefinida = false;
    
    private List<Consumer<String>> listeners = new ArrayList<>();
    private Thread receptorThread;
    private Map<String, BlockingQueue<String>> respuestasComando = new ConcurrentHashMap<>();
    private Timer pingTimer;
    private long ultimaActividad = System.currentTimeMillis();

    public void addListener(Consumer<String> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<String> listener) {
        listeners.remove(listener);
    }

    private void notificarListeners(String mensaje) {
        // Intentar asociar respuesta con un comando pendiente
        try {
            JsonObject json = gson.fromJson(mensaje, JsonObject.class);

            // Verificar si la respuesta tiene requestId
            if (json.has("requestId")) {
                String id = json.get("requestId").getAsString();
                BlockingQueue<String> cola = respuestasComando.get(id);
                if (cola != null) {
                    cola.offer(mensaje);
                    return; // No notificar a listeners generales
                }
            }
        } catch (Exception e) {
            // No es JSON o no tiene requestId
        }

        // Notificar a listeners generales
        for (Consumer<String> listener : listeners) {
            try {
                listener.accept(mensaje);
            } catch (Exception e) {
                System.err.println("Error en listener: " + e.getMessage());
            }
        }
    }

    private void iniciarReceptor() {
        if (receptorThread != null && receptorThread.isAlive()) {
            return;
        }

        receptorThread = new Thread(() -> {
            while (conectado && socket != null && !socket.isClosed()) {
                try {
                    // Usar read() con timeout en lugar de readUTF()
                    socket.setSoTimeout(30000);
                    String respuesta = entrada.readUTF();
                    ultimaActividad = System.currentTimeMillis();  // ← AGREGAR
                    System.out.println("[CONEXION] Respuesta recibida: " + respuesta);
                    notificarListeners(respuesta);
                } catch (SocketTimeoutException e) {
                    // Timeout normal, verificar si sigue conectado
                    if (System.currentTimeMillis() - ultimaActividad > 120000) {
                        System.err.println("[CONEXION] 2 minutos sin actividad - cerrando");
                        break;
                    }
                    continue;
                } catch (IOException e) {
                    if (conectado) {
                        System.err.println("[CONEXION] Error en receptor: " + e.getMessage());
                    }
                    break;
                }
            }
            conectado = false;
            System.out.println("[CONEXION] Receptor detenido");
            notificarListeners("{\"estado\":\"CONEXION_PERDIDA\"}");
        });
        receptorThread.setDaemon(true);
        receptorThread.start();
    }
    
    static {
        java.io.File configFile = new java.io.File("server.ip");
        if (configFile.exists()) {
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(configFile))) {
                String ip = br.readLine();
                if (ip != null && !ip.trim().isEmpty()) {
                    IP_SERVIDOR = ip.trim();
                    System.out.println("[CONFIG] IP del servidor cargada: " + IP_SERVIDOR);
                }
            } catch (IOException e) {
                System.err.println("[CONFIG] Error al leer server.ip: " + e.getMessage());
            }
        }
        
        String envIp = System.getenv("SERVIDOR_IP");
        if (envIp != null && !envIp.trim().isEmpty()) {
            IP_SERVIDOR = envIp.trim();
            System.out.println("[ENV] IP del servidor desde variable: " + IP_SERVIDOR);
        }
    }
    
    private Socket socket;
    private DataOutputStream salida;
    private DataInputStream entrada;
    private Gson gson;
    private boolean conectado;
    private String ultimoError;
    private String hostActual;
    
    public ConexionServidor() {
        this.gson = new Gson();
        this.conectado = false;
        this.ultimoError = "";
        this.hostActual = IP_SERVIDOR;
    }
    
    public boolean conectar() {
        return conectar(IP_SERVIDOR);
    }
    
    public boolean conectar(String ip) {
        try {
            if (socket != null && !socket.isClosed()) {
                desconectar();
            }

            this.hostActual = ip;
            socket = new Socket(ip, PUERTO);
            socket.setSoTimeout(30000);
            salida = new DataOutputStream(socket.getOutputStream());
            entrada = new DataInputStream(socket.getInputStream());
            conectado = true;
            ultimoError = "";

            // Iniciar hilo receptor
            iniciarReceptor();

            System.out.println("[CONEXION] Conectado a " + ip + ":" + PUERTO);
            return true;

        } catch (IOException e) {
            conectado = false;
            ultimoError = e.getMessage();
            System.err.println("[CONEXION] Error al conectar a " + ip + ":" + PUERTO + " - " + e.getMessage());
            return false;
        }
    }
    
    public boolean descubrirServidor() {
        // ← AGREGAR ESTO PRIMERO
        if (usarIpPredefinida) {
            System.out.println("[CONFIG] Usando IP predefinida: " + IP_PREDEFINIDA);
            if (conectar(IP_PREDEFINIDA)) {
                return true;
            }
        }

        if (conectar(IP_SERVIDOR)) {
            return true;
        }
        if (conectar("localhost")) {
            IP_SERVIDOR = "localhost";
            return true;
        }
        if (conectar("127.0.0.1")) {
            IP_SERVIDOR = "127.0.0.1";
            return true;
        }
        ultimoError = "No se pudo conectar al servidor. Verifique que el servidor esté corriendo en " + IP_SERVIDOR;
        return false;
    }
    
    public String enviarComando(JsonObject comando) {
        if (!conectado) {
            return "ERROR|No conectado al servidor";
        }

        String id = java.util.UUID.randomUUID().toString();
        comando.addProperty("requestId", id);  // ← AGREGAR ID al comando

        BlockingQueue<String> cola = new LinkedBlockingQueue<>();
        respuestasComando.put(id, cola);

        try {
            String jsonStr = gson.toJson(comando);
            System.out.println("[CONEXION] Enviando: " + jsonStr);

            salida.writeUTF(jsonStr);
            salida.flush();

            String respuesta = cola.poll(10, TimeUnit.SECONDS);
            return respuesta != null ? respuesta : "ERROR|Timeout";

        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        } finally {
            respuestasComando.remove(id);
        }
    }
    
    public void enviarComandoAsync(JsonObject comando) {
        if (!conectado) {
            return;
        }

        try {
            String jsonStr = gson.toJson(comando);
            System.out.println("[CONEXION] Enviando async: " + jsonStr);
            salida.writeUTF(jsonStr);
            salida.flush();
        } catch (IOException e) {
            System.err.println("[CONEXION] Error enviando async: " + e.getMessage());
            conectado = false;
        }
    }
    
    public void desconectar() {
        conectado = false;
        if (receptorThread != null) {
            receptorThread.interrupt();
            receptorThread = null;
        }
        try {
            if (salida != null) salida.close();
            if (entrada != null) entrada.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("[CONEXION] Desconectado");
        } catch (IOException e) {
            System.err.println("[CONEXION] Error al desconectar: " + e.getMessage());
        }
    }
    
    public boolean estaConectado() {
        return conectado && socket != null && !socket.isClosed();
    }
    
    public String getUltimoError() {
        return ultimoError;
    }
    
    public String getHostActual() {
        return hostActual;
    }
    
    public static void setIpServidor(String ip) {
        IP_SERVIDOR = ip;
        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter("server.ip"))) {
            bw.write(ip);
            System.out.println("[CONFIG] IP guardada en server.ip: " + ip);
        } catch (IOException e) {
            System.err.println("[CONFIG] Error al guardar server.ip: " + e.getMessage());
        }
    }
}