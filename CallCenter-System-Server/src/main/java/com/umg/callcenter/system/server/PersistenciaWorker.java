/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.server;

/**
 *
 * @author mk
 */

import com.umg.callcenter.system.model.Atencion;
import com.umg.callcenter.system.persistence.HashTableAtenciones;
import com.umg.callcenter.system.persistence.PersistenciaAtenciones;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PersistenciaWorker extends Thread {
        
    private final BlockingQueue<Atencion> colaPersistencia;
    private final HashTableAtenciones tablaHash;
    private volatile boolean ejecutando;
    
    public PersistenciaWorker(HashTableAtenciones tablaHash) {
        this.colaPersistencia = new LinkedBlockingQueue<>();
        this.tablaHash = tablaHash;
        this.ejecutando = true;
    }
    
    public void guardarAtencion(Atencion atencion) {
        try {
            colaPersistencia.put(atencion);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void run() {
        while (ejecutando) {
            try {
                Atencion atencion = colaPersistencia.take();
                PersistenciaAtenciones.guardar(atencion);
                tablaHash.insertar(atencion);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public void detener() {
        ejecutando = false;
        this.interrupt();
    }
}
