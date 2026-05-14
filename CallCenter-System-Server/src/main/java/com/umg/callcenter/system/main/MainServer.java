/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.main;

import com.umg.callcenter.system.ui.CentralServerUI;
import java.io.IOException;

/**
 *
 * @authors mk, natr, olga, jimem
 */

import javax.swing.SwingUtilities;

public class MainServer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                CentralServerUI servidorUI = new CentralServerUI();
                servidorUI.setVisible(true);
                servidorUI.setTitle("Servidor Central - Call Center UMG");
                servidorUI.setLocationRelativeTo(null);
                System.out.println("Servidor Central iniciado correctamente");
            } catch (Exception e) {
                System.err.println("Error al iniciar servidor: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
