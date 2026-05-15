/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.umg.callcenter.system.ui;

/**
 *
 * @author mk
 */

import com.umg.callcenter.system.controller.CentralServerController;
import com.umg.callcenter.system.model.Cliente;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;


public class CentralServerUI extends javax.swing.JFrame {
    private Map<String, Integer> desconexionesPorIP = new ConcurrentHashMap<>();
    private CentralServerController controller;
    
    public CentralServerUI() {
        initComponents();  // ← NetBeans generado, NO TOCAR
        inicializar();     // ← inicialización limpia
    }
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jSeparator9 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jSeparator11 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        txt_counter_normal = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txt_counter_Premium = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txt_DPI = new javax.swing.JTextField();
        jSeparator10 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        out_DPI = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        out_Connected = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        out_Messages = new javax.swing.JTextArea();
        btn_search = new javax.swing.JButton();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        Table_Recent_Tickets_normal = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        Table_Recent_Tickets_Premium = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        Table_Recent_Tickets_General = new javax.swing.JTable();
        jScrollPane7 = new javax.swing.JScrollPane();
        Table_Recent_Tickets_Attended = new javax.swing.JTable();
        jSeparator7 = new javax.swing.JSeparator();
        jSeparator8 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jSeparator9.setForeground(new java.awt.Color(184, 152, 250));

        jLabel2.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(153, 102, 255));
        jLabel2.setText("Tickets Recientes");

        jSeparator11.setForeground(new java.awt.Color(184, 152, 250));

        jSeparator5.setForeground(new java.awt.Color(184, 152, 250));

        jLabel7.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(153, 102, 255));
        jLabel7.setText("Estado del Sistema");

        jSeparator6.setForeground(new java.awt.Color(184, 152, 250));

        jLabel5.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(102, 102, 255));
        jLabel5.setText("Cola Normal:");

        txt_counter_normal.setFont(new java.awt.Font("sansserif", 1, 20)); // NOI18N
        txt_counter_normal.setForeground(new java.awt.Color(102, 102, 255));
        txt_counter_normal.setText("0");

        jLabel6.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 153, 102));
        jLabel6.setText("Cola Premium:");

        txt_counter_Premium.setFont(new java.awt.Font("sansserif", 1, 20)); // NOI18N
        txt_counter_Premium.setForeground(new java.awt.Color(255, 153, 102));
        txt_counter_Premium.setText("0");

        jLabel8.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(153, 102, 255));
        jLabel8.setText("Busqueda por DPI:");

        jLabel9.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(102, 102, 255));
        jLabel9.setText("DPI:");

        txt_DPI.setBackground(new java.awt.Color(245, 245, 245));
        txt_DPI.setForeground(new java.awt.Color(102, 102, 255));

        jSeparator10.setForeground(new java.awt.Color(184, 152, 250));

        jScrollPane1.setBorder(null);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        out_DPI.setBackground(new java.awt.Color(245, 245, 245));
        out_DPI.setColumns(20);
        out_DPI.setRows(5);
        out_DPI.setRequestFocusEnabled(false);
        jScrollPane1.setViewportView(out_DPI);

        jLabel4.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(153, 102, 255));
        jLabel4.setText("Equipos Conectados:");

        jScrollPane4.setBorder(null);
        jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        out_Connected.setBackground(new java.awt.Color(245, 245, 245));
        out_Connected.setColumns(20);
        out_Connected.setRows(5);
        out_Connected.setRequestFocusEnabled(false);
        jScrollPane4.setViewportView(out_Connected);

        jPanel1.setBackground(new java.awt.Color(102, 102, 255));

        jLabel1.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Terminal");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap())
        );

        jScrollPane2.setBorder(null);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        out_Messages.setBackground(new java.awt.Color(119, 119, 251));
        out_Messages.setColumns(20);
        out_Messages.setForeground(new java.awt.Color(255, 255, 255));
        out_Messages.setRows(5);
        out_Messages.setRequestFocusEnabled(false);
        jScrollPane2.setViewportView(out_Messages);

        btn_search.setBackground(new java.awt.Color(234, 234, 234));
        btn_search.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        btn_search.setForeground(new java.awt.Color(102, 102, 255));
        btn_search.setText("Buscar");
        btn_search.addActionListener(this::btn_searchActionPerformed);

        jTabbedPane2.setBackground(new java.awt.Color(245, 245, 245));
        jTabbedPane2.setForeground(new java.awt.Color(102, 102, 255));
        jTabbedPane2.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N

        jScrollPane6.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane6.setForeground(new java.awt.Color(51, 51, 51));
        jScrollPane6.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        Table_Recent_Tickets_normal.setBackground(new java.awt.Color(245, 245, 245));
        Table_Recent_Tickets_normal.setForeground(new java.awt.Color(51, 51, 51));
        Table_Recent_Tickets_normal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Ticket", "DPI", "Nombre", "Apellido", "Tipo", "Hora Ingreso"
            }
        ));
        Table_Recent_Tickets_normal.setFocusable(false);
        Table_Recent_Tickets_normal.setGridColor(new java.awt.Color(168, 168, 250));
        Table_Recent_Tickets_normal.setOpaque(false);
        Table_Recent_Tickets_normal.setSelectionBackground(new java.awt.Color(180, 180, 255));
        Table_Recent_Tickets_normal.setSelectionForeground(new java.awt.Color(255, 255, 255));
        Table_Recent_Tickets_normal.setShowGrid(false);
        Table_Recent_Tickets_normal.getTableHeader().setReorderingAllowed(false);
        jScrollPane6.setViewportView(Table_Recent_Tickets_normal);

        jTabbedPane2.addTab("Normal", jScrollPane6);

        jScrollPane5.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane5.setForeground(new java.awt.Color(51, 51, 51));
        jScrollPane5.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        Table_Recent_Tickets_Premium.setBackground(new java.awt.Color(245, 245, 245));
        Table_Recent_Tickets_Premium.setForeground(new java.awt.Color(51, 51, 51));
        Table_Recent_Tickets_Premium.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Ticket", "DPI", "Nombre", "Apellido", "Tipo", "Hora Ingreso"
            }
        ));
        Table_Recent_Tickets_Premium.setFocusable(false);
        Table_Recent_Tickets_Premium.setGridColor(new java.awt.Color(168, 168, 250));
        Table_Recent_Tickets_Premium.setOpaque(false);
        Table_Recent_Tickets_Premium.setSelectionBackground(new java.awt.Color(180, 180, 255));
        Table_Recent_Tickets_Premium.setSelectionForeground(new java.awt.Color(255, 255, 255));
        Table_Recent_Tickets_Premium.setShowGrid(false);
        Table_Recent_Tickets_Premium.getTableHeader().setReorderingAllowed(false);
        jScrollPane5.setViewportView(Table_Recent_Tickets_Premium);

        jTabbedPane2.addTab("Premium", jScrollPane5);

        jScrollPane3.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane3.setForeground(new java.awt.Color(51, 51, 51));
        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        Table_Recent_Tickets_General.setBackground(new java.awt.Color(245, 245, 245));
        Table_Recent_Tickets_General.setForeground(new java.awt.Color(51, 51, 51));
        Table_Recent_Tickets_General.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Ticket", "DPI", "Nombre", "Apellido", "Tipo", "Hora Ingreso"
            }
        ));
        Table_Recent_Tickets_General.setFocusable(false);
        Table_Recent_Tickets_General.setGridColor(new java.awt.Color(168, 168, 250));
        Table_Recent_Tickets_General.setOpaque(false);
        Table_Recent_Tickets_General.setSelectionBackground(new java.awt.Color(180, 180, 255));
        Table_Recent_Tickets_General.setSelectionForeground(new java.awt.Color(255, 255, 255));
        Table_Recent_Tickets_General.setShowGrid(false);
        Table_Recent_Tickets_General.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(Table_Recent_Tickets_General);

        jTabbedPane2.addTab("All", jScrollPane3);

        jScrollPane7.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane7.setForeground(new java.awt.Color(51, 51, 51));
        jScrollPane7.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        Table_Recent_Tickets_Attended.setBackground(new java.awt.Color(245, 245, 245));
        Table_Recent_Tickets_Attended.setForeground(new java.awt.Color(51, 51, 51));
        Table_Recent_Tickets_Attended.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Ticket", "DPI", "Nombre", "Apellido", "Tipo", "Hora Ingreso"
            }
        ));
        Table_Recent_Tickets_Attended.setFocusable(false);
        Table_Recent_Tickets_Attended.setGridColor(new java.awt.Color(168, 168, 250));
        Table_Recent_Tickets_Attended.setOpaque(false);
        Table_Recent_Tickets_Attended.setSelectionBackground(new java.awt.Color(180, 180, 255));
        Table_Recent_Tickets_Attended.setSelectionForeground(new java.awt.Color(255, 255, 255));
        Table_Recent_Tickets_Attended.setShowGrid(false);
        Table_Recent_Tickets_Attended.getTableHeader().setReorderingAllowed(false);
        jScrollPane7.setViewportView(Table_Recent_Tickets_Attended);

        jTabbedPane2.addTab("Atendidos", jScrollPane7);

        jSeparator7.setForeground(new java.awt.Color(184, 152, 250));

        jSeparator8.setForeground(new java.awt.Color(184, 152, 250));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jSeparator7)
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(108, 108, 108)
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(txt_counter_normal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txt_counter_Premium)
                        .addGap(120, 120, 120))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator11))
                            .addComponent(jTabbedPane2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addGap(162, 162, 162)
                                .addComponent(jSeparator6)))
                        .addGap(5, 5, 5))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jScrollPane1)
                                .addGap(11, 11, 11))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4)
                                .addGap(3, 3, 3))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(39, 39, 39)
                                        .addComponent(jSeparator10, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addGap(12, 12, 12)
                                        .addComponent(txt_DPI, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(12, 12, 12)
                                        .addComponent(btn_search, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(19, 19, 19)))
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
                        .addGap(5, 5, 5))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator8)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator11, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txt_counter_normal, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txt_counter_Premium, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_DPI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_search))
                        .addGap(1, 1, 1)
                        .addComponent(jSeparator10, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE))
                    .addComponent(jScrollPane4))
                .addGap(5, 5, 5)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

     
        
    private void inicializar() {
        // 1. Crear controlador
        controller = new CentralServerController(this);

        // 2. Configurar tablas (solo UI, no necesita servidor)
        controller.inicializarTablas(
            Table_Recent_Tickets_General,
            Table_Recent_Tickets_normal,
            Table_Recent_Tickets_Premium,
            Table_Recent_Tickets_Attended
        );

        controller.aplicarEstiloPestanas(jTabbedPane2);

        // 3. INICIALIZAR EL SERVIDOR (esto crea ServidorCentral)
        controller.inicializarServidor();  // ← AGREGAR ESTO ANTES

        // 4. AHORA SÍ, cargar datos en las tablas
        controller.getServidor().inicializarDatosUI();

        out_Messages.setEditable(false);
        out_DPI.setEditable(false);
        out_Connected.setEditable(false);
        txt_DPI.setText("");

        // 5. Iniciar el servidor (escuchar conexiones)
        iniciarServidor();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.detenerServidor();
                System.exit(0);
            }
        });

        logs("Interfaz del servidor inicializada");
    }

    private void iniciarServidor() {
        controller.iniciarServidor();
    }
    
    // ========== MÉTODOS PÚBLICOS PARA EL CONTROLADOR ==========
    
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void logs(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            out_Messages.append("[" + timestamp + "] " + mensaje + "\n");
            out_Messages.setCaretPosition(out_Messages.getDocument().getLength());
            limitarLogs();
        });
    }
    
    private void limitarLogs() {
        String texto = out_Messages.getText();
        String[] lineas = texto.split("\n");
        int maxLineas = 500;
        
        if (lineas.length > maxLineas) {
            StringBuilder sb = new StringBuilder();
            for (int i = lineas.length - maxLineas; i < lineas.length; i++) {
                sb.append(lineas[i]).append("\n");
            }
            out_Messages.setText(sb.toString());
        }
    }
    
    private void registrarDesconexion(String ip) {
        int count = desconexionesPorIP.getOrDefault(ip, 0) + 1;
        desconexionesPorIP.put(ip, count);
        if (count > 10) {
            logs("⚠️ ALERTA: IP " + ip + " se ha desconectado " + count + " veces");
        }
    }
    
    // ========== MÉTODOS DE UI PARA EL CONTROLADOR ==========
    
    public void agregarTicketTabla(Cliente cliente) {
        controller.agregarTicketATablas(cliente);
    }

    public void cargarClientesPendientes(List<Cliente> pendientes) {
        controller.cargarClientesPendientes(pendientes);
    }
    
    public void actualizarContadores(int normal, int premium) {
        controller.actualizarContadores(txt_counter_normal, txt_counter_Premium, normal, premium);
    }
    
    public void actualizarEquiposConectados(List<String> equipos) {
        SwingUtilities.invokeLater(() -> {
            out_Connected.setText("");
            for (String equipo : equipos) {
                out_Connected.append("• " + equipo + "\n");
            }
        });
    }
    
    public void actualizarIpServidor(String nuevaIp) {
        logs(">>> Los clientes deben conectarse a: " + nuevaIp + ":" + 
             com.umg.callcenter.system.conexion.Conexion.PUERTO);
    }
    
    private void btn_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_searchActionPerformed
        String dpi = txt_DPI.getText().trim();
        if (dpi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un DPI para buscar", "DPI Requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        logs("Buscando para DPI: " + dpi);
        controller.buscarPorDPI(dpi, out_DPI);
    }//GEN-LAST:event_btn_searchActionPerformed
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        
        java.awt.EventQueue.invokeLater(() -> new CentralServerUI().setVisible(true));
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Table_Recent_Tickets_Attended;
    private javax.swing.JTable Table_Recent_Tickets_General;
    private javax.swing.JTable Table_Recent_Tickets_Premium;
    private javax.swing.JTable Table_Recent_Tickets_normal;
    private javax.swing.JButton btn_search;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTextArea out_Connected;
    private javax.swing.JTextArea out_DPI;
    private javax.swing.JTextArea out_Messages;
    private javax.swing.JTextField txt_DPI;
    public javax.swing.JLabel txt_counter_Premium;
    public javax.swing.JLabel txt_counter_normal;
    // End of variables declaration//GEN-END:variables
// </editor-fold>  
}

