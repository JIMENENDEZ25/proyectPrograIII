/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.atencion.normal.main;

/**
 *
 * @author mk
 */

import com.umg.callcenter.atencion.normal.ui.ui;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public class MainAtencion {
    public static void main(String[] args) {
        String tipoAgente = "normal";
        String nombreAgente = "Agente";
        
        if (args.length >= 1) {
            tipoAgente = args[0].toLowerCase();
            if (!tipoAgente.equals("normal") && !tipoAgente.equals("premium")) {
                System.err.println("Tipo inválido. Use 'normal' o 'premium'");
                System.exit(1);
            }
        }
        
        if (args.length >= 2) {
            nombreAgente = args[1];
        } else {
            while (true) {
                nombreAgente = JOptionPane.showInputDialog(null,
                        "Ingrese su nombre:",
                        "Identificación del Agente",
                        JOptionPane.QUESTION_MESSAGE);

                // Si canceló o cerró el diálogo
                if (nombreAgente == null) {
                    int confirm = JOptionPane.showConfirmDialog(null,
                            "¿Desea salir de la aplicación?",
                            "Confirmar salida",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
                    // Si elige NO, sigue preguntando
                    continue;
                }

                // Si ingresó un nombre válido (no vacío)
                if (!nombreAgente.trim().isEmpty()) {
                    break;
                }

                // Si ingresó vacío, mostrar advertencia y seguir preguntando
                JOptionPane.showMessageDialog(null,
                        "El nombre no puede estar vacío. Por favor ingrese un nombre.",
                        "Nombre requerido",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
        
        final String tipo = tipoAgente;
        final String nombre = nombreAgente;
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> {
            ui ventana = new ui(tipo, nombre);
            ventana.setVisible(true);
        });
    }
}
