/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.controller;

/**
 *
 * @author mk
 */

import com.umg.callcenter.system.model.Cliente;
import com.umg.callcenter.system.server.ServidorCentral;
import com.umg.callcenter.system.ui.CentralServerUI;
import com.umg.callcenter.system.persistence.PersistenciaClientes;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Font;
import java.util.List;

public class CentralServerController {
    
    private final CentralServerUI ui;
    private ServidorCentral servidor;
    private DefaultTableModel modeloTablaGeneral;
    private DefaultTableModel modeloTablaNormal;
    private DefaultTableModel modeloTablaPremium;
    private DefaultTableModel modeloTablaAtendidos;  // ← NUEVO
    
    public CentralServerController(CentralServerUI ui) {
        this.ui = ui;
    }
    
    public void inicializarServidor() {
        this.servidor = new ServidorCentral(ui, this);
    }
    
    public ServidorCentral getServidor() {
        return servidor;
    }
    
    public void iniciarServidor() {
        if (servidor == null) {
            ui.logs("ERROR: Servidor no inicializado");
            return;
        }
        try {
            servidor.iniciar();
        } catch (Exception e) {
            ui.mostrarError("Error al iniciar servidor: " + e.getMessage());
        }
    }
    
    public void detenerServidor() {
        if (servidor != null) {
            servidor.detener();
        }
    }
    
    // ========== INICIALIZACIÓN DE UI ==========
    
    public void inicializarTablas(JTable tablaGeneral, JTable tablaNormal, 
                                  JTable tablaPremium, JTable tablaAtendidos) {
        String[] columnas = {"Ticket", "DPI", "Nombre", "Apellido", "Tipo", "Hora Ingreso", "Estado"};
        
        // Tabla GENERAL
        modeloTablaGeneral = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaGeneral.setModel(modeloTablaGeneral);
        
        // Tabla NORMAL
        modeloTablaNormal = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaNormal.setModel(modeloTablaNormal);
        
        // Tabla PREMIUM
        modeloTablaPremium = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaPremium.setModel(modeloTablaPremium);
        
        // Tabla ATENDIDOS
        modeloTablaAtendidos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaAtendidos.setModel(modeloTablaAtendidos);
        
        // Aplicar estilos
        aplicarEstiloTabla(tablaGeneral, new Color(102, 102, 255));
        aplicarEstiloTabla(tablaNormal, new Color(102, 187, 106));
        aplicarEstiloTabla(tablaPremium, new Color(255, 153, 102));
        aplicarEstiloTabla(tablaAtendidos, new Color(100, 100, 100));
    }
    
    private void aplicarEstiloTabla(JTable tabla, Color colorEncabezado) {
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Resetear estilos por defecto
                c.setForeground(Color.BLACK);
                c.setFont(new Font("SansSerif", Font.PLAIN, 12));
                
                // Columna Tipo (índice 4) - Color y negrita según tipo
                if (column == 4 && value != null) {
                    String tipo = value.toString();
                    if (tipo.contains("PREMIUM")) {
                        c.setForeground(new Color(255, 140, 0));  // Naranja
                        c.setFont(new Font("SansSerif", Font.BOLD, 12));
                    } else if (tipo.contains("NORMAL")) {
                        c.setForeground(new Color(0, 102, 204));  // Azul oscuro
                        c.setFont(new Font("SansSerif", Font.BOLD, 12));
                    }
                }
                // Columna Estado (índice 6)
                else if (column == 6 && value != null) {
                    String estado = value.toString();
                    if (estado.contains("PENDIENTE")) {
                        c.setForeground(new Color(204, 102, 0));  // Naranja oscuro
                    } else if (estado.contains("ATENDIDO")) {
                        c.setForeground(new Color(0, 120, 0));    // Verde oscuro
                    }
                }
                
                // Fondo alternado para mejor legibilidad
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }
                return c;
            }
        });
        
        // Configurar encabezado
        javax.swing.table.JTableHeader header = tabla.getTableHeader();
        header.setBackground(colorEncabezado);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setPreferredSize(new java.awt.Dimension(header.getWidth(), 30));
    }
    
    public void aplicarEstiloPestanas(JTabbedPane tabbedPane) {
        tabbedPane.setBackground(new Color(245, 245, 245));
        
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Color colorPestana;
            String titulo = tabbedPane.getTitleAt(i);
            switch (titulo) {
                case "Premium":
                    colorPestana = new Color(255, 153, 102);
                    break;
                case "Normal":
                    colorPestana = new Color(137, 196, 138);
                    break;
                case "Atendidos":
                    colorPestana = new Color(100, 100, 100);
                    break;
                default:
                    colorPestana = new Color(160, 160, 250);
                    break;
            }
            tabbedPane.setBackgroundAt(i, colorPestana);
            tabbedPane.setForegroundAt(i, Color.WHITE);
        }
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 12));
    }
    
    // ========== MÉTODOS PARA AGREGAR A TABLAS ==========
    
    public void agregarTicketATablas(Cliente cliente) {
        SwingUtilities.invokeLater(() -> {
            if (modeloTablaGeneral == null) return;
            
            String estadoTexto = cliente.estaPendiente() ? "PENDIENTE" : "ATENDIDO";
            Object[] fila = new Object[]{
                cliente.getNumeroTicket(),
                cliente.getDpi(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getTipo().toUpperCase(),
                cliente.getHoraIngresoFormateada(),
                estadoTexto
            };
            
            // Insertar en GENERAL
            modeloTablaGeneral.insertRow(0, fila);
            
            // Insertar en tabla específica según tipo
            if (cliente.getTipo().equals("premium")) {
                modeloTablaPremium.insertRow(0, fila);
            } else {
                modeloTablaNormal.insertRow(0, fila);
            }
            
            // Limitar filas
            while (modeloTablaGeneral.getRowCount() > 100) {
                modeloTablaGeneral.removeRow(modeloTablaGeneral.getRowCount() - 1);
            }
            while (modeloTablaPremium.getRowCount() > 100) {
                modeloTablaPremium.removeRow(modeloTablaPremium.getRowCount() - 1);
            }
            while (modeloTablaNormal.getRowCount() > 100) {
                modeloTablaNormal.removeRow(modeloTablaNormal.getRowCount() - 1);
            }
        });
    }
    
    // NUEVO: Agregar cliente a la tabla de ATENDIDOS
    public void agregarClienteAtendido(Cliente cliente) {
        SwingUtilities.invokeLater(() -> {
            if (modeloTablaAtendidos == null) return;
            
            Object[] fila = new Object[]{
                cliente.getNumeroTicket(),
                cliente.getDpi(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getTipo().toUpperCase(),
                cliente.getHoraIngresoFormateada(),
                "ATENDIDO"
            };
            
            modeloTablaAtendidos.insertRow(0, fila);
            
            // Limitar a 200 filas en atendidos
            while (modeloTablaAtendidos.getRowCount() > 200) {
                modeloTablaAtendidos.removeRow(modeloTablaAtendidos.getRowCount() - 1);
            }
        });
    }
    
    // NUEVO: Mover cliente de pendiente a atendido
    public void moverClienteAAntendidos(String ticket) {
        // Buscar el cliente en persistencia y agregarlo a atendidos
        var cliente = PersistenciaClientes.buscarPorTicket(ticket);
        if (cliente != null && !cliente.estaPendiente()) {
            agregarClienteAtendido(cliente);
        }
    }
    
    public void cargarClientesPendientes(List<Cliente> pendientes) {
        SwingUtilities.invokeLater(() -> {
            if (modeloTablaGeneral == null) return;
            
            for (Cliente c : pendientes) {
                String estadoTexto = c.estaPendiente() ? "PENDIENTE" : "ATENDIDO";
                Object[] fila = new Object[]{
                    c.getNumeroTicket(),
                    c.getDpi(),
                    c.getNombre(),
                    c.getApellido(),
                    c.getTipo().toUpperCase(),
                    c.getHoraIngresoFormateada(),
                    estadoTexto
                };
                
                modeloTablaGeneral.insertRow(0, fila);
                
                if (c.getTipo().equals("premium")) {
                    modeloTablaPremium.insertRow(0, fila);
                } else {
                    modeloTablaNormal.insertRow(0, fila);
                }
            }
            if (!pendientes.isEmpty()) {
                ui.logs("📋 Cargados " + pendientes.size() + " clientes pendientes en las tablas");
            }
        });
    }
    
    public void cargarClientesAtendidos(List<Cliente> atendidos) {
        SwingUtilities.invokeLater(() -> {
            if (modeloTablaAtendidos == null) return;
            
            for (Cliente c : atendidos) {
                Object[] fila = new Object[]{
                    c.getNumeroTicket(),
                    c.getDpi(),
                    c.getNombre(),
                    c.getApellido(),
                    c.getTipo().toUpperCase(),
                    c.getHoraIngresoFormateada(),
                    "ATENDIDO"
                };
                modeloTablaAtendidos.insertRow(0, fila);
            }
            if (!atendidos.isEmpty()) {
                ui.logs("📋 Cargados " + atendidos.size() + " clientes atendidos");
            }
        });
    }
    
    public void actualizarContadores(JLabel normalLabel, JLabel premiumLabel, int normal, int premium) {
        SwingUtilities.invokeLater(() -> {
            normalLabel.setText(String.valueOf(normal));
            premiumLabel.setText(String.valueOf(premium));
        });
    }
    
    public void buscarPorDPI(String dpi, JTextArea outArea) {
        if (servidor == null) {
            outArea.setText("Servidor no inicializado");
            return;
        }
        
        var atenciones = servidor.buscarAtencionesPorDPI(dpi);
        var clientes = PersistenciaClientes.buscarPorDPI(dpi);
        
        outArea.setText("");
        
        if (atenciones.isEmpty() && clientes.isEmpty()) {
            outArea.append("No se encontraron registros para el DPI: " + dpi + "\n");
            outArea.append("========================================\n");
            return;
        }
        
        if (!clientes.isEmpty()) {
            outArea.append("📋 HISTORIAL DE CLIENTES (" + clientes.size() + ")\n");
            outArea.append("========================================\n\n");
            for (int i = 0; i < clientes.size(); i++) {
                var c = clientes.get(i);
                outArea.append("Cliente #" + (i + 1) + "\n");
                outArea.append("  Ticket: " + c.getNumeroTicket() + "\n");
                outArea.append("  Nombre: " + c.getNombre() + " " + c.getApellido() + "\n");
                outArea.append("  Tipo: " + c.getTipo().toUpperCase() + "\n");
                outArea.append("  Estado: " + (c.estaPendiente() ? "🟡 PENDIENTE" : "✅ ATENDIDO") + "\n");
                outArea.append("  Ingreso: " + c.getHoraIngresoFormateada() + "\n");
                if (!c.estaPendiente() && c.getFechaAtencion() != null) {
                    outArea.append("  Atendido: " + c.getFechaAtencion().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
                }
                outArea.append("----------------------------------------\n");
            }
        }
        
        if (!atenciones.isEmpty()) {
            outArea.append("\n📞 ATENCIONES REALIZADAS (" + atenciones.size() + ")\n");
            outArea.append("========================================\n\n");
            for (int i = 0; i < atenciones.size(); i++) {
                var a = atenciones.get(i);
                outArea.append("Atención #" + (i + 1) + "\n");
                outArea.append("  Ticket: " + a.getNumeroTicket() + "\n");
                outArea.append("  Fecha: " + a.getFechaHora() + "\n");
                outArea.append("  Motivo: " + a.getMotivo() + "\n");
                outArea.append("  Agente: " + a.getUsuarioAgente() + "\n");
                outArea.append("  Duración: " + a.getDuracionMinutos() + " minutos\n");
                outArea.append("----------------------------------------\n");
            }
        }
    }
}
