/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.server;

/**
 *
 * @authors mk, natr, olga, jimem
 */

import com.umg.callcenter.system.conexion.Conexion;
import com.umg.callcenter.system.controller.CentralServerController;
import com.umg.callcenter.system.model.Atencion;
import com.umg.callcenter.system.queue.GestorColas;
import com.umg.callcenter.system.model.Cliente;
import com.umg.callcenter.system.persistence.HashTableAtenciones;
import com.umg.callcenter.system.persistence.PersistenciaAtenciones;
import com.umg.callcenter.system.persistence.PersistenciaClientes;
import com.umg.callcenter.system.ui.CentralServerUI;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ServidorCentral {
        
    private ServerSocket serverSocket;
    private CentralServerController controller;
    private GestorColas gestorColas;
    private HashTableAtenciones tablaHash;
    private PersistenciaWorker persistenciaWorker;
    private List<String> equiposConectados;
    private volatile boolean ejecutando;
    private CentralServerUI ui;
    private String ipServidorReal;
    private Map<String, String> tipoEquipos = new ConcurrentHashMap<>();
    private Map<String, String> nombreEquipos = new ConcurrentHashMap<>(); 
    private Map<String, ClienteHandler> usuariosChat = new ConcurrentHashMap<>();
    private Map<String, String> nombresChat = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();
    private ExecutorService threadPool;
    private Semaphore connectionLimiter; 
    private static final int MAX_CONEXIONES = 20;
    private final AtomicInteger conexionesActivas = new AtomicInteger(0);
    
    public ServidorCentral(CentralServerUI ui, CentralServerController controller) {
        this.ui = ui;
        this.controller = controller;
        this.gestorColas = new GestorColas();
        this.tablaHash = new HashTableAtenciones(100);
        this.persistenciaWorker = new PersistenciaWorker(tablaHash);    
        this.connectionLimiter = new Semaphore(MAX_CONEXIONES);
        this.threadPool = Executors.newCachedThreadPool(); 
        this.equiposConectados = new CopyOnWriteArrayList<>();
        this.ejecutando = false;
        this.ipServidorReal = detectarIpReal();

        PersistenciaClientes.cargarTodos();
        gestorColas.cargarPendientesACola();

        PersistenciaAtenciones.cargar(tablaHash);

        ui.logs("Cargados " + gestorColas.tamanio() + " clientes pendientes");
        ui.logs("Cargadas " + tablaHash.tamanio() + " atenciones previas");
    }

    public void inicializarDatosUI() {
        cargarPendientesEnUI();
        cargarAtendidosEnUI();
        ui.actualizarContadores(tamanioNormal(), tamanioPremium());
    }
    
    private String detectarIpReal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            String mejorCandidato = null;
            String candidatoWifiEthernet = null;
            String candidatoNormal = null;
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }
                
                String nombre = ni.getDisplayName().toLowerCase();
                String nombreOriginal = ni.getName().toLowerCase();
                
                boolean esVirtual = nombre.contains("docker") || 
                                   nombre.contains("virtual") || 
                                   nombre.contains("veth") || 
                                   nombre.contains("vmnet") ||
                                   nombre.contains("virbr") ||
                                   nombre.contains("vbox") ||
                                   nombreOriginal.contains("docker") ||
                                   nombreOriginal.contains("veth") ||
                                   nombreOriginal.contains("vmnet");
                
                if (esVirtual) {
                    continue;
                }
                
                boolean esInterfazReal = nombre.contains("wlan") || 
                                        nombre.contains("wifi") || 
                                        nombre.contains("ethernet") ||
                                        nombre.contains("enp") ||
                                        nombre.contains("ens") ||
                                        nombre.contains("wlo") ||
                                        nombreOriginal.contains("wlan") ||
                                        nombreOriginal.contains("enp") ||
                                        nombreOriginal.contains("wlo");
                
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String ip = addr.getHostAddress();
                    
                    if (ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                        if (esInterfazReal) {
                            if (ip.startsWith("192.168.") || ip.startsWith("10.") || 
                                (ip.startsWith("172.") && esIpPrivada172(ip))) {
                                ui.logs("[DETECCION] Encontrada interfaz real: " + nombre + " -> " + ip);
                                return ip;
                            }
                            if (candidatoWifiEthernet == null) {
                                candidatoWifiEthernet = ip;
                            }
                        } else {
                            if (candidatoNormal == null && !esVirtual) {
                                candidatoNormal = ip;
                            }
                        }
                        
                        if (mejorCandidato == null && !esVirtual) {
                            mejorCandidato = ip;
                        }
                    }
                }
            }
            
            if (candidatoWifiEthernet != null) {
                ui.logs("[DETECCION] Usando interfaz WiFi/Ethernet: " + candidatoWifiEthernet);
                return candidatoWifiEthernet;
            }
            if (candidatoNormal != null) {
                ui.logs("[DETECCION] Usando interfaz: " + candidatoNormal);
                return candidatoNormal;
            }
            if (mejorCandidato != null) {
                ui.logs("[DETECCION] Usando candidato: " + mejorCandidato);
                return mejorCandidato;
            }
            
            String localhost = InetAddress.getLocalHost().getHostAddress();
            ui.logs("[DETECCION] Usando localhost: " + localhost);
            return localhost;
            
        } catch (Exception e) {
            ui.logs("[DETECCION] Error detectando IP: " + e.getMessage());
            return "127.0.0.1";
        }
    }
    
    private boolean esIpPrivada172(String ip) {
        try {
            String[] partes = ip.split("\\.");
            if (partes.length == 4) {
                int segundoOcteto = Integer.parseInt(partes[1]);
                return segundoOcteto >= 16 && segundoOcteto <= 31;
            }
        } catch (NumberFormatException e) {}
        return false;
    }
    
    public List<Atencion> buscarAtencionesPorDPI(String dpi) {
        return tablaHash.buscar(dpi);
    }
    
    public void iniciar() throws IOException {
        Conexion conexion = new Conexion("servidor", null);
        this.serverSocket = conexion.ss;
        this.ejecutando = true;
        this.persistenciaWorker.start();
        
        ui.logs("========================================");
        ui.logs("SERVIDOR CALL CENTER UMG");
        ui.logs("========================================");
        ui.logs("IP REAL del servidor: " + ipServidorReal);
        ui.logs("Puerto: " + Conexion.PUERTO);
        ui.logs("");
        ui.logs(">>> LOS CLIENTES DEBEN CONECTARSE A: <<<");
        ui.logs(">>> " + ipServidorReal + ":" + Conexion.PUERTO + " <<<");
        ui.logs("");
        ui.logs("========================================");
        
        new Thread(() -> {
            while (ejecutando) {
                try {
                    connectionLimiter.acquire();
                    var socketCliente = serverSocket.accept();
                    socketCliente.setSoTimeout(30000);
                    
                    String identificador = socketCliente.getInetAddress().getHostAddress();
                    equiposConectados.add(identificador);
                    ui.actualizarEquiposConectados(equiposConectados);
                    ui.logs("✓ Nuevo equipo conectado: " + identificador);
                    
                    ClienteHandler handler = new ClienteHandler(
                        socketCliente, identificador, gestorColas, 
                        persistenciaWorker, tablaHash, this, ui
                    );
                    
                    threadPool.execute(() -> {
                        try {
                            handler.run();
                        } finally {
                            connectionLimiter.release();
                        }
                    });
                    
                } catch (IOException e) {
                    if (ejecutando) {
                        ui.logs("Error aceptando conexión: " + e.getMessage());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
    
    public void registrarUsuarioChat(String identificador, String nombre, String tipo, ClienteHandler handler) {
        usuariosChat.put(identificador, handler);
        nombresChat.put(identificador, nombre + " (" + getIconoChat(tipo) + ")");
        broadcastListaUsuarios();
        logChat("🟢 " + nombre + " se ha unido al chat");
    }

    public void removerUsuarioChat(String identificador) {
        String nombre = nombresChat.remove(identificador);
        usuariosChat.remove(identificador);
        if (nombre != null) {
            broadcastListaUsuarios();
            logChat("🔴 " + nombre + " ha salido del chat");
        }
    }

    private String getIconoChat(String tipo) {
        switch (tipo) {
            case "registro": return "📝";
            case "normal": return "📞";
            case "premium": return "⭐";
            default: return "🟢";
        }
    }
    
    // Mapa para almacenar el modo actual de cada agente
    private Map<String, String> modoAgentes = new ConcurrentHashMap<>();

    public void actualizarModoAgente(String identificador, String modo) {
        modoAgentes.put(identificador, modo);
        System.out.println("[SERVIDOR] Agente " + identificador + " ahora en modo " + modo);
    }

    public String getModoAgente(String identificador) {
        return modoAgentes.getOrDefault(identificador, "normal");
    }
    
    public void broadcastMensajeChat(ClienteHandler emisor, String mensaje) {
        String emisorId = null;
        String emisorNombre = null;
        String emisorTipo = "general";

        for (var entry : usuariosChat.entrySet()) {
            if (entry.getValue() == emisor) {
                emisorId = entry.getKey();
                emisorNombre = nombresChat.get(emisorId);
                if (emisorId != null) {
                    emisorTipo = tipoEquipos.getOrDefault(emisorId, "general");
                }
                break;
            }
        }

        if (emisorNombre == null) {
            System.err.println("[CHAT] No se encontró el emisor");
            return;
        }

        for (ClienteHandler handler : usuariosChat.values()) {
            handler.recibirMensajeChat(emisorNombre, mensaje, emisorTipo);
        }

        logChat("💬 " + emisorNombre + ": " + mensaje);
    }

    public void broadcastListaUsuarios() {
        JsonObject lista = new JsonObject();
        lista.addProperty("estado", "ACTUALIZAR_USUARIOS_CHAT");
        JsonArray array = new JsonArray();

        for (var entry : nombresChat.entrySet()) {
            array.add(entry.getValue());
        }
        lista.add("usuarios", array);

        String jsonStr = gson.toJson(lista);
        for (ClienteHandler handler : usuariosChat.values()) {
            try {
                handler.enviarRaw(jsonStr);
            } catch (Exception e) {}
        }
    }

    private void logChat(String mensaje) {
        ui.logs("[CHAT] " + mensaje);
    }
    
    public void detener() {
        ejecutando = false;
        persistenciaWorker.detener();
        threadPool.shutdown();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            ui.logs("Error cerrando servidor: " + e.getMessage());
        }
    }
    
    public void removerEquipo(String identificador) {
        equiposConectados.remove(identificador);
        ui.actualizarEquiposConectados(equiposConectados);
    }
    
    public int tamanioNormal() {
        return gestorColas.tamanioNormal();
    }
    
    public int tamanioPremium() {
        return gestorColas.tamanioPremium();
    }
    
    public void actualizarUI() {
        ui.actualizarContadores(tamanioNormal(), tamanioPremium());
    }
    
    public Cliente desencolar() {
        Cliente cliente = gestorColas.desencolar();
        if (cliente != null) {
            System.out.println("[SERVIDOR] Cliente desencolado: " + cliente.getNumeroTicket());
            javax.swing.SwingUtilities.invokeLater(() -> {
                ui.actualizarContadores(tamanioNormal(), tamanioPremium());
                cargarPendientesEnUI();
            });
        }
        return cliente;
    }
    
    public void encolarCliente(Cliente cliente) {
        gestorColas.encolar(cliente);
    }
    
    public List<Cliente> getClientesPendientes() {
        return gestorColas.getPendientes();
    }
    
    public String getIpServidorReal() {
        return ipServidorReal;
    }
    
    public void cargarPendientesEnUI() {
        List<Cliente> pendientes = gestorColas.getPendientes();
        controller.cargarClientesPendientes(pendientes);
        controller.actualizarContadores(ui.txt_counter_normal, ui.txt_counter_Premium, 
                                        tamanioNormal(), tamanioPremium());
    }
    
    public void cargarAtendidosEnUI() {
        List<Cliente> todos = PersistenciaClientes.cargarTodos().values().stream()
            .filter(c -> !c.estaPendiente())
            .collect(java.util.stream.Collectors.toList());
        controller.cargarClientesAtendidos(todos);
    }
    
    public void moverClienteAAntendidos(String ticket) {
        var cliente = PersistenciaClientes.buscarPorTicket(ticket);
        if (cliente != null && !cliente.estaPendiente()) {
            controller.agregarClienteAtendido(cliente);
        }
    }
    
    public void actualizarTipoEquipo(String identificador, String tipo, String nombre) {
        tipoEquipos.put(identificador, tipo);
        if (!nombre.isEmpty()) {
            nombreEquipos.put(identificador, nombre);
        }
        actualizarListaEquipos();
    }
    
    public JsonArray getUsuariosChat() {
        JsonArray array = new JsonArray();
        for (var entry : nombresChat.entrySet()) {
            array.add(entry.getValue());
        }
        return array;
    }
    
    private void actualizarListaEquipos() {
        List<String> listaFormateada = new ArrayList<>();
        for (String id : equiposConectados) {
            String tipo = tipoEquipos.getOrDefault(id, "?");
            String nombre = nombreEquipos.getOrDefault(id, "");

            String icono;
            switch (tipo) {
                case "registro":
                    icono = "📝";
                    break;
                case "normal":
                    icono = "📞";
                    break;
                case "premium":
                    icono = "⭐";
                    break;
                case "admin":
                    icono = "🔧";
                    break;
                default:
                    icono = "❓";
            }

            String display = icono + " " + id;
            if (!nombre.isEmpty()) {
                display += " (" + nombre + ")";
            }
            listaFormateada.add(display);
        }
        ui.actualizarEquiposConectados(listaFormateada);
    }
}
