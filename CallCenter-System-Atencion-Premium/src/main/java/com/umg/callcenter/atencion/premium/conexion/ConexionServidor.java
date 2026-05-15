/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.atencion.premium.conexion;

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
import java.util.function.Consumer;

public class ConexionServidor {
        
    public static final int PUERTO = 5000;
    public static String IP_SERVIDOR = "localhost";
    public static final String IP_PREDEFINIDA = "25.4.40.151";
    public static boolean usarIpPredefinida = false;
    
    private List<Consumer<String>> listeners = new ArrayList<>();
    private Thread receptorThread;
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
    
    public void addListener(Consumer<String> listener) {
        listeners.add(listener);
    }
    
    private void notificarListeners(String mensaje) {
        for (Consumer<String> listener : listeners) {
            try {
                listener.accept(mensaje);
            } catch (Exception e) {}
        }
    }
    
    
    
    private void iniciarReceptor() {
        if (receptorThread != null && receptorThread.isAlive()) return;
        
        receptorThread = new Thread(() -> {
            try {
                while (conectado && socket != null && !socket.isClosed()) {
                    try {
                        String respuesta = entrada.readUTF();
                        System.out.println("[CONEXION] Respuesta recibida: " + respuesta);
                        notificarListeners(respuesta);
                    } catch (SocketTimeoutException e) {
                        continue;
                    } catch (IOException e) {
                        System.err.println("[CONEXION] Error en receptor: " + e.getMessage());
                        break;
                    }
                }
            } finally {
                conectado = false;
                System.out.println("[CONEXION] Receptor detenido");
            }
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
            } catch (IOException e) {}
        }
        
        String envIp = System.getenv("SERVIDOR_IP");
        if (envIp != null && !envIp.trim().isEmpty()) {
            IP_SERVIDOR = envIp.trim();
            System.out.println("[ENV] IP del servidor desde variable: " + IP_SERVIDOR);
        }
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
            socket.setSoTimeout(60000);
            salida = new DataOutputStream(socket.getOutputStream());
            entrada = new DataInputStream(socket.getInputStream());
            conectado = true;
            ultimoError = "";
            
            iniciarReceptor();
            
            System.out.println("[CONEXION] Conectado a " + ip + ":" + PUERTO);
            return true;
            
        } catch (IOException e) {
            conectado = false;
            ultimoError = e.getMessage();
            System.err.println("[CONEXION] Error al conectar: " + e.getMessage());
            return false;
        }
    }
    
    public boolean descubrirServidor() {
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
        ultimoError = "No se pudo conectar al servidor";
        return false;
    }
    
    // Método para enviar comandos (TODOS los comandos usan este método)
    public void enviarComando(JsonObject comando) {
        if (!conectado) return;
        
        try {
            String jsonStr = gson.toJson(comando);
            System.out.println("[CONEXION] Enviando: " + jsonStr);
            salida.writeUTF(jsonStr);
            salida.flush();
        } catch (IOException e) {
            System.err.println("[CONEXION] Error enviando: " + e.getMessage());
            conectado = false;
        }
    }
    // Agrega este método después de enviarComando()

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
        } catch (IOException e) {}
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
        } catch (IOException e) {}
    }
}