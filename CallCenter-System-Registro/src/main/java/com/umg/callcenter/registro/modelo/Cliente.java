/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.registro.modelo;

/**
 *
 * @authors mk, natr, olga, jimem
 */

import com.google.gson.JsonObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Cliente {
        
    private String dpi;
    private String nombre;
    private String apellido;
    private String tipo;
    private String numeroTicket;
    private String horaIngreso;
    
    public Cliente(String dpi, String nombre, String apellido, String tipo) {
        this.dpi = dpi;
        this.nombre = nombre;
        this.apellido = apellido;
        this.tipo = tipo.toLowerCase();
        this.numeroTicket = null;
        this.horaIngreso = null;
    }
    
    public JsonObject toJsonRegistro() {
        JsonObject json = new JsonObject();
        json.addProperty("comando", "REGISTRAR");
        json.addProperty("dpi", dpi);
        json.addProperty("nombre", nombre);
        json.addProperty("apellido", apellido);
        json.addProperty("tipo", tipo);
        return json;
    }
    
    // Getters
    public String getDpi() { return dpi; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTipo() { return tipo; }
    public String getNumeroTicket() { return numeroTicket; }
    public String getHoraIngreso() { return horaIngreso; }
    
    public void setNumeroTicket(String numeroTicket) {
        this.numeroTicket = numeroTicket;
    }
    
    public void setHoraIngreso(String horaIngreso) {
        this.horaIngreso = horaIngreso;
    }
    
    // Validaciones estáticas
    public static boolean validarDPI(String dpi) {
        if (dpi == null || dpi.trim().isEmpty()) return false;
        return dpi.matches("\\d{13}");
    }
    
    public static boolean validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return false;
        return nombre.matches("[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ\\s]{2,50}");
    }
    
    public static String sanitizar(String texto) {
        if (texto == null) return "";
        return texto.trim().replaceAll("[<>\"\'&]", "");
    }
}