/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.registro.main;

/**
 *
 * @author mk
 */

import com.umg.callcenter.registro.ui.ui;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class MainRegistro {
    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al establecer look and feel: " + e.getMessage());
        }
        
        String nombreOperador;

        // ← PREGUNTAR HASTA QUE INGRESE ALGO O CONFIRME SALIR
        while (true) {
            nombreOperador = JOptionPane.showInputDialog(null,
                    "Ingrese su nombre:",
                    "Identificación del Operador",
                    JOptionPane.QUESTION_MESSAGE);

            // Si canceló o cerró el diálogo
            if (nombreOperador == null) {
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
            if (!nombreOperador.trim().isEmpty()) {
                break;
            }

            // Si ingresó vacío, mostrar advertencia y seguir preguntando
            JOptionPane.showMessageDialog(null,
                    "El nombre no puede estar vacío. Por favor ingrese un nombre.",
                    "Nombre requerido",
                    JOptionPane.WARNING_MESSAGE);
        }
        
        final String nombreFinal = nombreOperador;
        
        SwingUtilities.invokeLater(() -> {
            ui ventana = new ui(nombreFinal);
            ventana.setVisible(true);
        });
    }
}
