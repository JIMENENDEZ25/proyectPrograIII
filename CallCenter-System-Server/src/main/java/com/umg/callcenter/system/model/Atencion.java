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

public class Atencion {
    private String numeroTicket;
    private String dpi;
    private String fechaHora;
    private String motivo;
    private int duracionMinutos;
    private String usuarioAgente;
    private String tipoAtencion; // "general" / "premium"

    public Atencion(String numeroTicket, String dpi, String motivo, int duracionMinutos, String usuarioAgente, String tipoAtencion) {
        this.numeroTicket = numeroTicket;
        this.dpi = dpi;
        this.motivo = motivo;
        this.duracionMinutos = duracionMinutos;
        this.usuarioAgente = usuarioAgente;
        this.tipoAtencion = tipoAtencion;
        this.fechaHora = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }

    public Atencion(String numeroTicket, String dpi, String fechaHora, String motivo, int duracionMinutos, String usuarioAgente, String tipoAtencion) {
        this.numeroTicket = numeroTicket;
        this.dpi = dpi;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.duracionMinutos = duracionMinutos;
        this.usuarioAgente = usuarioAgente;
        this.tipoAtencion = tipoAtencion;
    }
    
    public String toCSV() { return numeroTicket + "," + dpi + "," + fechaHora + "," + motivo + "," + duracionMinutos + "," + usuarioAgente + "," + tipoAtencion; }

    // Método estático para crear Atencion desde línea CSV
    public static Atencion fromCSV(String linea) {
        String[] partes = linea.split(",");
        if (partes.length == 7) {
            return new Atencion(
                partes[0], // numeroTicket
                partes[1], // dpi
                partes[2], // fechaHora
                partes[3], // motivo
                Integer.parseInt(partes[4]), // duracionMinutos
                partes[5], // usuarioAgente
                partes[6]  // tipoAtencion
            );
        }
        return null;
    }
       
    // Getters
    public String getDpi() {
        return dpi;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getMotivo() {
        return motivo;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public String getUsuarioAgente() {
        return usuarioAgente;
    }

    public String getTipoAtencion() {
        return tipoAtencion;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public void setUsuarioAgente(String usuarioAgente) {
        this.usuarioAgente = usuarioAgente;
    }

    @Override
    public String toString() {
        return "Ticket: " + numeroTicket + " | DPI: " + dpi
                + " | Agente: " + usuarioAgente + " | Duración: " + duracionMinutos + "min"
                + " | Tipo: " + tipoAtencion;
    }
}
