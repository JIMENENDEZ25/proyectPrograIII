/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.model;

/**
 *
 * @authors mk, natr, olga, jimem
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.annotations.Expose;

public class Cliente implements Comparable<Cliente> {

    public enum EstadoCliente {
        PENDIENTE,   // En cola esperando atención
        ATENDIDO     // Ya fue atendido
    }
    
    private String dpi;
    private String nombre;
    private String apellido;
    private String tipo;
    private String numeroTicket;
    private LocalDateTime horaIngreso;
    private String origen;
    private EstadoCliente estado;  // ← NUEVO
    private LocalDateTime fechaAtencion;  // ← NUEVO (cuándo fue atendido)

    public Cliente(String dpi, String nombre, String apellido, String tipo) {
        this.dpi = dpi;
        this.nombre = nombre;
        this.apellido = apellido;
        this.tipo = tipo.toLowerCase();
        this.horaIngreso = LocalDateTime.now();
        this.numeroTicket = generarTicket();
        this.origen = "registro";
        this.estado = EstadoCliente.PENDIENTE;  // ← NUEVO
        this.fechaAtencion = null;
    }

    public EstadoCliente getEstado() { return estado; }
    
    public void setEstado(EstadoCliente estado) { this.estado = estado; }
    
    public LocalDateTime getFechaAtencion() { return fechaAtencion; }
   
    public void setFechaAtencion(LocalDateTime fechaAtencion) { this.fechaAtencion = fechaAtencion; }
    
    private String generarTicket() {

        String sufijo;

        if (dpi.length() >= 4) {
            sufijo = dpi.substring(dpi.length() - 4);
        } else {
            sufijo = String.format("%4s", dpi).replace(' ', '0');
        }

        return "TKT-" + System.currentTimeMillis() + "-" + sufijo;
    }

    public String getDpi() {
        return dpi;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public LocalDateTime getHoraIngreso() {
        return horaIngreso;
    }

    public String getHoraIngresoFormateada() {

        return horaIngreso.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setNumeroTicket(String numeroTicket) {
        this.numeroTicket = numeroTicket;
    }

    @Override
    public int compareTo(Cliente otro) {
        // Premium tiene prioridad (0 = mayor prioridad, 1 = menor)
        int prioridadEste = this.tipo.equals("premium") ? 0 : 1;
        int prioridadOtro = otro.tipo.equals("premium") ? 0 : 1;
        
        if (prioridadEste != prioridadOtro) {
            return Integer.compare(prioridadEste, prioridadOtro);
        }
        return this.horaIngreso.compareTo(otro.horaIngreso);
    }

    @Override
    public String toString() {

        return "Ticket: " + numeroTicket
                + " | "
                + nombre + " " + apellido
                + " | DPI: " + dpi;
    }

    public String toCSV() {

        return numeroTicket + ","
                + dpi + ","
                + nombre + ","
                + apellido + ","
                + tipo + ","
                + getHoraIngresoFormateada();
    }
    
    public boolean estaPendiente() {
        return estado == EstadoCliente.PENDIENTE;
    }
    
    // Para marcar como atendido
    public void marcarAtendido() {
        this.estado = EstadoCliente.ATENDIDO;
        this.fechaAtencion = LocalDateTime.now();
    }
    
    
}
