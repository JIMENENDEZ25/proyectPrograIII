/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.conexion;

/**
 *
 * @authors mk, natr, olga, jimem
 */
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class Conexion {
    public static final int PUERTO = 5000;
    
    // NOTA: Esta constante es SOLO para los CLIENTES cuando no se especifica IP
    // El servidor NO usa esta IP, escucha en todas las interfaces (0.0.0.0)
    // Los clientes deben usar la IP REAL que muestra el servidor en su UI
    public static final String IP_SERVIDOR_POR_DEFECTO = "localhost";
    
    protected String host;
    public ServerSocket ss;
    protected Socket cs;
    protected DataOutputStream salidaServidor, salidaCliente;

    public Conexion(String tipo, String host) throws IOException {
        this.host = host;
        
        if (tipo.equalsIgnoreCase("servidor")) {
            // El servidor escucha en todas las interfaces (no necesita IP específica)
            ss = new ServerSocket(PUERTO);
            cs = new Socket();
            System.out.println("[SERVIDOR] Escuchando en puerto " + PUERTO + " en todas las interfaces");
        } else {
            // El cliente necesita saber a qué IP conectarse
            String hostFinal = (host == null || host.isEmpty()) ? IP_SERVIDOR_POR_DEFECTO : host;
            cs = new Socket(hostFinal, PUERTO);
            System.out.println("[CLIENTE] Conectado a " + hostFinal + ":" + PUERTO);
        }
    }
}
