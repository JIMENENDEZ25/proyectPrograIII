/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.atencion.normal.modelo;

/**
 *
 * @authors mk, natr, olga, jimem
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Atencion {
    private String ticket;
    private String dpi;
    private String motivo;
    private int duracion;
    private String agente;
    private String tipoAtencion;
    
    public Atencion(String ticket, String dpi, String motivo, int duracion, String agente, String tipoAtencion) {
        this.ticket = ticket;
        this.dpi = dpi;
        this.motivo = motivo;
        this.duracion = duracion;
        this.agente = agente;
        this.tipoAtencion = tipoAtencion;
    }
    
    public String getTicket() { return ticket; }
    public String getDpi() { return dpi; }
    public String getMotivo() { return motivo; }
    public int getDuracion() { return duracion; }
    public String getAgente() { return agente; }
    public String getTipoAtencion() { return tipoAtencion; }
}
