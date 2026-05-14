/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.queue;

/**
 *
 * @authors mk, natr, olga, jimem
 */

import com.umg.callcenter.system.model.Cliente;
import com.umg.callcenter.system.persistence.PersistenciaClientes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class GestorColas {
    
    // Una sola cola priorizada - premium tiene prioridad sobre normal
    private final PriorityBlockingQueue<Cliente> colaPriorizada;
    private static final int MAX_COLA_SIZE = 100;
    
    public GestorColas() {
        // Cliente ya implementa Comparable
        this.colaPriorizada = new PriorityBlockingQueue<>(100);
    }
    
    public void encolar(Cliente cliente) {
        if (colaPriorizada.size() >= MAX_COLA_SIZE) {
            System.out.println("[COLA] Cola llena, rechazando cliente: " + cliente.getNumeroTicket());
            return;
        }

        if (cliente.estaPendiente()) {
            colaPriorizada.offer(cliente);
            System.out.println("[COLA] Cliente encolado: " + cliente.getNumeroTicket());
        }
    }
    
    public void cargarPendientesACola() {
        List<Cliente> pendientes = PersistenciaClientes.getClientesPendientes();
        System.out.println("[DEBUG] Clientes pendientes encontrados en JSON: " + pendientes.size());
        for (Cliente c : pendientes) {
            colaPriorizada.offer(c);
            System.out.println("[DEBUG] Cargado a cola: " + c.getNumeroTicket() + " - " + c.getTipo());
        }
        System.out.println("[DEBUG] Total en cola después de carga: " + colaPriorizada.size());
    }
    
    public Cliente desencolar() {
        return colaPriorizada.poll();
    }
    
    public Cliente desencolarPremium() {
        // El mismo método, la prioridad la maneja la cola
        return colaPriorizada.poll();
    }
    
    public Cliente desencolarNormal() {
        // El mismo método, la prioridad la maneja la cola
        return colaPriorizada.poll();
    }
    
    public int tamanio() {
        return colaPriorizada.size();
    }
    
    public int tamanioNormal() {
        // Contar clientes normales (más lento, usar solo para UI)
        return (int) colaPriorizada.stream().filter(c -> c.getTipo().equals("normal")).count();
    }
    
    public int tamanioPremium() {
        // Contar clientes premium (más lento, usar solo para UI)
        return (int) colaPriorizada.stream().filter(c -> c.getTipo().equals("premium")).count();
    }
    
    public boolean estaVacia() {
        return colaPriorizada.isEmpty();
    }
    public List<Cliente> getPendientes() {
        List<Cliente> pendientes = new ArrayList<>();
        pendientes.addAll(colaPriorizada);
        return pendientes;
    }
public void removerCliente(String ticket) {
    boolean removed = colaPriorizada.removeIf(c -> c.getNumeroTicket().equals(ticket));
    if (removed) {
        System.out.println("[COLA] Cliente removido: " + ticket);
        System.out.println("[COLA] Tamaño restante: " + colaPriorizada.size());
    } else {
        System.out.println("[COLA] Cliente NO encontrado para remover: " + ticket);
    }
}
}
