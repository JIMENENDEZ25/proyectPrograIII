/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.persistence;

/**
 *
 * @authors mk, natr, olga, jimem
 */

import com.umg.callcenter.system.model.Cliente;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenciaClientes {    
    private static final String ARCHIVO = "clientes.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    // Mapa de clientes por ticket (para búsqueda rápida)
    private static Map<String, Cliente> clientesPorTicket = new ConcurrentHashMap<>();
    
    // Guardar un cliente (si existe, actualiza)
    public static synchronized void guardar(Cliente cliente) {
        try {
            // Cargar clientes existentes
            Map<String, Cliente> clientes = cargarTodos();
            
            // Actualizar o agregar
            clientes.put(cliente.getNumeroTicket(), cliente);
            
            // Guardar todo de vuelta
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
                bw.write(gson.toJson(clientes.values()));
            }
            
            // Actualizar caché en memoria
            clientesPorTicket.put(cliente.getNumeroTicket(), cliente);
            
            System.out.println("[PERSISTENCIA] Cliente guardado: " + cliente.getNumeroTicket() + " - Estado: " + cliente.getEstado());
            
        } catch (IOException e) {
            System.err.println("[PERSISTENCIA] Error guardando cliente: " + e.getMessage());
        }
    }
    
    // Cargar todos los clientes
    public static synchronized Map<String, Cliente> cargarTodos() {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) return new ConcurrentHashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            Type listType = new TypeToken<List<Cliente>>() {}.getType();
            List<Cliente> clientesList = gson.fromJson(br, listType);
            
            Map<String, Cliente> mapa = new ConcurrentHashMap<>();
            if (clientesList != null) {
                for (Cliente c : clientesList) {
                    mapa.put(c.getNumeroTicket(), c);
                    clientesPorTicket.put(c.getNumeroTicket(), c);
                }
            }
            System.out.println("[PERSISTENCIA] Cargados " + mapa.size() + " clientes");
            return mapa;
        } catch (IOException e) {
            System.err.println("[PERSISTENCIA] Error cargando clientes: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }
    
    // Buscar cliente por DPI (en todos, incluyendo atendidos)
    public static List<Cliente> buscarPorDPI(String dpi) {
        List<Cliente> resultados = new ArrayList<>();
        for (Cliente c : clientesPorTicket.values()) {
            if (c.getDpi().equals(dpi)) {
                resultados.add(c);
            }
        }
        return resultados;
    }
    
    // Buscar cliente por ticket
    public static Cliente buscarPorTicket(String ticket) {
        return clientesPorTicket.get(ticket);
    }
    
    // Obtener solo clientes pendientes
    public static List<Cliente> getClientesPendientes() {
        List<Cliente> pendientes = new ArrayList<>();
        for (Cliente c : clientesPorTicket.values()) {
            if (c.estaPendiente()) {
                pendientes.add(c);
            }
        }
        return pendientes;
    }
    
    // Actualizar estado después de atención
    public static synchronized void marcarComoAtendido(String ticket) {
        Cliente c = clientesPorTicket.get(ticket);
        if (c != null && c.estaPendiente()) {
            c.marcarAtendido();
            guardar(c);
            System.out.println("[PERSISTENCIA] Cliente marcado como ATENDIDO: " + ticket);
        }
    }
}
