/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.persistence;

/**
 *
 * @author mk
 */

import com.umg.callcenter.system.model.Atencion;
import java.util.ArrayList;
import java.util.List;

public class HashTableAtenciones {
 
    private static class Nodo {
        String dpi;
        List<Atencion> atenciones;
        Nodo siguiente;

        public Nodo(String dpi, Atencion atencion) {
            this.dpi = dpi;
            this.atenciones = new ArrayList<>();
            this.atenciones.add(atencion);
        }
    }

    private Nodo[] tabla;
    private int capacidad;
    private int elementos;  // Para factor de carga

    public HashTableAtenciones(int capacidad) {
        this.capacidad = capacidad;
        this.tabla = new Nodo[capacidad];
        this.elementos = 0;
    }

    private int hash(String dpi) {
        return Math.abs(dpi.hashCode() % capacidad);
    }
    
    private double factorCarga() {
        return (double) elementos / capacidad;
    }
    
    private void resize() {
        int nuevaCapacidad = capacidad * 2;
        Nodo[] nuevaTabla = new Nodo[nuevaCapacidad];
        
        for (Nodo nodo : tabla) {
            Nodo actual = nodo;
            while (actual != null) {
                int nuevoIndice = Math.abs(actual.dpi.hashCode() % nuevaCapacidad);
                Nodo nuevoNodo = new Nodo(actual.dpi, actual.atenciones.get(0));
                nuevoNodo.atenciones = new ArrayList<>(actual.atenciones);
                nuevoNodo.siguiente = nuevaTabla[nuevoIndice];
                nuevaTabla[nuevoIndice] = nuevoNodo;
                
                actual = actual.siguiente;
            }
        }
        
        this.tabla = nuevaTabla;
        this.capacidad = nuevaCapacidad;
        System.out.println("[HASH] Tabla redimensionada a capacidad: " + capacidad);
    }

    public synchronized void insertar(Atencion atencion) {
        // Verificar factor de carga
        if (factorCarga() > 0.75) {
            resize();
        }
        
        String dpi = atencion.getDpi();
        int indice = hash(dpi);

        Nodo actual = tabla[indice];
        while (actual != null) {
            if (actual.dpi.equals(dpi)) {
                actual.atenciones.add(atencion);
                elementos++;
                return;
            }
            actual = actual.siguiente;
        }

        Nodo nuevo = new Nodo(dpi, atencion);
        nuevo.siguiente = tabla[indice];
        tabla[indice] = nuevo;
        elementos++;
    }

    public synchronized List<Atencion> buscar(String dpi) {
        int indice = hash(dpi);
        Nodo actual = tabla[indice];

        while (actual != null) {
            if (actual.dpi.equals(dpi)) {
                return new ArrayList<>(actual.atenciones);
            }
            actual = actual.siguiente;
        }
        return new ArrayList<>();
    }
    
    public synchronized int tamanio() {
        return elementos;
    }
}
