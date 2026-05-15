/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.persistence;

/**
 *
 * @author mk
 */

import com.umg.callcenter.system.model.Cliente;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenciaClientes {    
    private static final String ARCHIVO = "clientes.json";
    
    // Gson configurado correctamente para LocalDateTime
    private static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDateTime.class, new com.google.gson.JsonSerializer<LocalDateTime>() {
            @Override
            public com.google.gson.JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
                return new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        })
        .registerTypeAdapter(LocalDateTime.class, new com.google.gson.JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
                return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        })
        .create();
    
    private static Map<String, Cliente> clientesPorTicket = new ConcurrentHashMap<>();
    
    public static synchronized void guardar(Cliente cliente) {
        try {
            Map<String, Cliente> clientes = cargarTodos();
            clientes.put(cliente.getNumeroTicket(), cliente);
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
                bw.write(gson.toJson(clientes.values()));
            }
            
            clientesPorTicket.put(cliente.getNumeroTicket(), cliente);
            System.out.println("[PERSISTENCIA] Cliente guardado: " + cliente.getNumeroTicket());
        } catch (IOException e) {
            System.err.println("[PERSISTENCIA] Error guardando: " + e.getMessage());
        }
    }
    
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
            System.err.println("[PERSISTENCIA] Error cargando: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }
    
    public static List<Cliente> buscarPorDPI(String dpi) {
        List<Cliente> resultados = new ArrayList<>();
        for (Cliente c : clientesPorTicket.values()) {
            if (c.getDpi().equals(dpi)) {
                resultados.add(c);
            }
        }
        return resultados;
    }
    
    public static Cliente buscarPorTicket(String ticket) {
        return clientesPorTicket.get(ticket);
    }
    
    public static List<Cliente> getClientesPendientes() {
        List<Cliente> pendientes = new ArrayList<>();
        for (Cliente c : clientesPorTicket.values()) {
            if (c.estaPendiente()) {
                pendientes.add(c);
            }
        }
        return pendientes;
    }
    
    public static synchronized void marcarComoAtendido(String ticket) {
        Cliente c = clientesPorTicket.get(ticket);
        if (c != null && c.estaPendiente()) {
            c.marcarAtendido();
            guardar(c);
            System.out.println("[PERSISTENCIA] Cliente marcado como ATENDIDO: " + ticket);
        }
    }
}
