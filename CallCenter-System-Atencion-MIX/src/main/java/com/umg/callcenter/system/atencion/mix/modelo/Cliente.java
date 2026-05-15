/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.atencion.mix.modelo;

/**
 *
 * @author mk
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.annotations.Expose;


public class Cliente {
    private String ticket;
    private String dpi;
    private String nombre;
    private String apellido;
    private String tipo;
    
    public Cliente(String ticket, String dpi, String nombre, String apellido, String tipo) {
        this.ticket = ticket;
        this.dpi = dpi;
        this.nombre = nombre;
        this.apellido = apellido;
        this.tipo = tipo;
    }
    
    public String getTicket() { return ticket; }
    public String getDpi() { return dpi; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTipo() { return tipo; }
    public String getNombreCompleto() { return nombre + " " + apellido; }
    
    @Override
    public String toString() {
        return "╔══════════════════════════════════════════════════╗\n" +
               "║              DATOS DEL CLIENTE                   ║\n" +
               "╠══════════════════════════════════════════════════╣\n" +
               String.format("║ Ticket: %-36s ║\n", ticket) +
               String.format("║ DPI:    %-36s ║\n", dpi) +
               String.format("║ Nombre: %-36s ║\n", getNombreCompleto()) +
               String.format("║ Tipo:   %-36s ║\n", tipo.toUpperCase()) +
               "╚══════════════════════════════════════════════════╝";
    }
}
