/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.umg.callcenter.registro.ui;

/**
 *
 * @authors mk, natr, olga, jimem
 */

import com.umg.callcenter.registro.controller.RegistroController;
import com.umg.callcenter.registro.modelo.Cliente;
import com.google.gson.JsonArray;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;


public class ui extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ui.class.getName());

    private RegistroController controller;
    private String nombreOperador;

    // Para el chat
    private JFrame ventanaChat;
    private JTextArea txtAreaChat;
    private JTextField txtMensajeChat;
    private DefaultListModel<String> modeloUsuarios;
    private Timer actualizarUsuariosTimer;
    private Color colorTema = new Color(101, 102, 246);
    
    /**
     * Creates new form ui
     */
    public ui() {
        this("Operador");
    }
    
    public ui(String nombreOperador) {
        this.nombreOperador = nombreOperador;
        initComponents();
        inicializarPersonalizado();
    }
    private void inicializarPersonalizado() {
        // 1. Crear el controller
        this.controller = new RegistroController(nombreOperador);
        
        // 2. Configurar componentes
        txt_operador.setText(nombreOperador);
        txt_ipactual.setText(controller.getIpActual());
        jTextAreaResultadoRegistro.setEditable(false);
        jTextAreaResultadoRegistro.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // 3. Configurar combo box
        combo_tipoCliente.setModel(new DefaultComboBoxModel<>(new String[]{"normal", "premium"}));
        
        // 4. Configurar eventos
        configurarEventos();
        
        // 5. Configurar callbacks
        configurarCallbacks();
        
        // 6. Conectar al servidor
        controller.conectar();
    }
    
    private void configurarEventos() {
        // Botón Aplicar IP
        btn_applyIP.addActionListener(e -> {
            String nuevaIp = txt_ip.getText().trim();
            if (!nuevaIp.isEmpty()) {
                controller.cambiarIpServidor(nuevaIp);
            }
        });
        
        // Botón IP Predefinida
        btn_presentIP.addActionListener(e -> {
            String ipPredefinida = "25.4.40.151";
            txt_ip.setText(ipPredefinida);
            controller.cambiarIpServidor(ipPredefinida);
            agregarLog("🔧 Usando IP predefinida: " + ipPredefinida + "\n");
        });
        
        // Botón Reconectar
        btn_Reconectar.addActionListener(e -> {
            btn_Reconectar.setEnabled(false);
            btn_Reconectar.setText("Conectando...");
            controller.conectar();
            Timer timer = new Timer(2000, evt -> {
                btn_Reconectar.setEnabled(true);
                btn_Reconectar.setText("Reconectar");
            });
            timer.setRepeats(false);
            timer.start();
        });
        
        // Botón Registrar
        btn_Registrar.addActionListener(this::registrarCliente);
        
        // Botón Limpiar
        btn_limpiar.addActionListener(e -> limpiarFormulario());
        
        // Botón Chat
        btn_chat.addActionListener(e -> abrirChat());
    }
    
    private void configurarCallbacks() {
        controller.setOnConectar(() -> {
            txt_estado_servidor.setText("🟢 Conectado");
            txt_estado_servidor.setForeground(new Color(0, 150, 0));
            txt_ipactual.setText(controller.getHostActual());
            txt_ip.setText(controller.getHostActual());
            btn_Registrar.setEnabled(true);
        });
        
        controller.setOnDesconectar(() -> {
            txt_estado_servidor.setText("🔴 Desconectado");
            txt_estado_servidor.setForeground(Color.RED);
            btn_Registrar.setEnabled(false);
        });
        
        controller.setOnErrorConexion(() -> {
            txt_estado_servidor.setText("🔴 Error de conexión");
            txt_estado_servidor.setForeground(Color.RED);
            btn_Registrar.setEnabled(false);
        });
        
        controller.setOnLog(this::agregarLog);
        
        controller.setOnMostrarExito(mensaje -> {
            jTextAreaResultadoRegistro.setText("");
            jTextAreaResultadoRegistro.setForeground(new Color(0, 100, 0));
            jTextAreaResultadoRegistro.setText(mensaje);
            limpiarFormulario();
        });
        
        controller.setOnMostrarError(mensaje -> {
            jTextAreaResultadoRegistro.setText("");
            jTextAreaResultadoRegistro.setForeground(Color.RED);
            jTextAreaResultadoRegistro.append("========================================\n");
            jTextAreaResultadoRegistro.append("          ✗ ERROR DE REGISTRO\n");
            jTextAreaResultadoRegistro.append("========================================\n\n");
            jTextAreaResultadoRegistro.append(mensaje + "\n");
        });
        
        // Callbacks para chat
        controller.setOnMensajeChat(mensaje -> {
            SwingUtilities.invokeLater(() -> {
                if (txtAreaChat != null) {
                    txtAreaChat.append(mensaje + "\n");
                    txtAreaChat.setCaretPosition(txtAreaChat.getDocument().getLength());
                }
            });
        });
        
        controller.setOnActualizarUsuariosChat(usuarios -> {
            SwingUtilities.invokeLater(() -> {
                if (modeloUsuarios != null) {
                    modeloUsuarios.clear();
                    for (var u : usuarios) {
                        modeloUsuarios.addElement(u.getAsString());
                    }
                }
            });
        });
    }
    
    private void registrarCliente(ActionEvent evt) {
        String dpi = Cliente.sanitizar(txt_DPI.getText());
        String nombre = Cliente.sanitizar(txt_nombre.getText());
        String apellido = Cliente.sanitizar(txt_apellido.getText());
        String tipo = (String) combo_tipoCliente.getSelectedItem();
        
        if (!Cliente.validarDPI(dpi)) {
            mostrarErrorTemporal("DPI inválido. Debe tener 13 dígitos numéricos.");
            txt_DPI.requestFocus();
            return;
        }
        
        if (!Cliente.validarNombre(nombre)) {
            mostrarErrorTemporal("Nombre inválido. Solo letras y mínimo 2 caracteres.");
            txt_nombre.requestFocus();
            return;
        }
        
        if (!Cliente.validarNombre(apellido)) {
            mostrarErrorTemporal("Apellido inválido. Solo letras y mínimo 2 caracteres.");
            txt_apellido.requestFocus();
            return;
        }
        
        btn_Registrar.setEnabled(false);
        btn_Registrar.setText("Registrando...");
        
        controller.registrarCliente(dpi, nombre, apellido, tipo);
        
        Timer timer = new Timer(2000, e -> {
            btn_Registrar.setEnabled(true);
            btn_Registrar.setText("Registrar Cliente");
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void mostrarErrorTemporal(String mensaje) {
        jTextAreaResultadoRegistro.setText("");
        jTextAreaResultadoRegistro.setForeground(Color.RED);
        jTextAreaResultadoRegistro.append("========================================\n");
        jTextAreaResultadoRegistro.append("          ✗ ERROR\n");
        jTextAreaResultadoRegistro.append("========================================\n\n");
        jTextAreaResultadoRegistro.append(mensaje + "\n");
        
        Timer timer = new Timer(3000, e -> {
            jTextAreaResultadoRegistro.setText("");
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void limpiarFormulario() {
        txt_DPI.setText("");
        txt_nombre.setText("");
        txt_apellido.setText("");
        combo_tipoCliente.setSelectedIndex(0);
        txt_DPI.requestFocus();
    }
    
    private void agregarLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            String linea = "[" + timestamp + "] " + mensaje + "\n";
            jTextAreaResultadoRegistro.append(linea);
            jTextAreaResultadoRegistro.setCaretPosition(jTextAreaResultadoRegistro.getDocument().getLength());
            
            // Limitar líneas
            if (jTextAreaResultadoRegistro.getText().split("\n").length > 200) {
                String[] lineas = jTextAreaResultadoRegistro.getText().split("\n");
                StringBuilder sb = new StringBuilder();
                for (int i = lineas.length - 150; i < lineas.length; i++) {
                    if (i >= 0) sb.append(lineas[i]).append("\n");
                }
                jTextAreaResultadoRegistro.setText(sb.toString());
            }
        });
    }
    
    // ========== MÉTODOS DE CHAT ==========
    private void abrirChat() {
        if (ventanaChat == null || !ventanaChat.isVisible()) {
            crearVentanaChat();
        }
        ventanaChat.setVisible(true);
        ventanaChat.toFront();
        
        Timer timer = new Timer(500, e -> controller.solicitarUsuariosChat());
        timer.setRepeats(false);
        timer.start();
    }
    
    private void crearVentanaChat() {
        ventanaChat = new JFrame("Chat - Call Center UMG");
        ventanaChat.setSize(500, 600);
        ventanaChat.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        ventanaChat.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        modeloUsuarios = new DefaultListModel<>();
        JList<String> listaUsuarios = new JList<>(modeloUsuarios);
        listaUsuarios.setPreferredSize(new Dimension(150, 0));
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        scrollUsuarios.setBorder(BorderFactory.createTitledBorder("Usuarios Conectados"));
        
        txtAreaChat = new JTextArea();
        txtAreaChat.setEditable(false);
        txtAreaChat.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollChat = new JScrollPane(txtAreaChat);
        scrollChat.setBorder(BorderFactory.createTitledBorder("Mensajes"));
        
        JPanel panelEnvio = new JPanel(new BorderLayout(5, 5));
        txtMensajeChat = new JTextField();
        txtMensajeChat.addActionListener(e -> enviarMensajeChat());
        JButton btnEnviar = new JButton("Enviar");
        btnEnviar.addActionListener(e -> enviarMensajeChat());
        panelEnvio.add(txtMensajeChat, BorderLayout.CENTER);
        panelEnvio.add(btnEnviar, BorderLayout.EAST);
        
        panel.add(scrollChat, BorderLayout.CENTER);
        panel.add(scrollUsuarios, BorderLayout.EAST);
        panel.add(panelEnvio, BorderLayout.SOUTH);
        
        ventanaChat.add(panel);
        
        actualizarUsuariosTimer = new Timer(5000, e -> controller.solicitarUsuariosChat());
        actualizarUsuariosTimer.start();
        
        ventanaChat.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (actualizarUsuariosTimer != null) actualizarUsuariosTimer.stop();
            }
        });
    }
    
    private void enviarMensajeChat() {
        String mensaje = txtMensajeChat.getText().trim();
        if (mensaje.isEmpty()) return;
        controller.enviarMensajeChat(mensaje);
        txtMensajeChat.setText("");
    }
    
    @Override
    public void dispose() {
        if (controller != null) controller.desconectar();
        super.dispose();
    }
    
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
            logger.log(java.util.logging.Level.SEVERE, null, e);
        }
        
        String nombreOperador = JOptionPane.showInputDialog(null,
                "Ingrese su nombre:",
                "Identificación del Operador",
                JOptionPane.QUESTION_MESSAGE);
        
        if (nombreOperador == null || nombreOperador.trim().isEmpty()) {
            System.exit(0);
        }
        
        final String nombre = nombreOperador;
        
        java.awt.EventQueue.invokeLater(() -> new ui(nombre).setVisible(true));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txt_operador = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txt_ipactual = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaResultadoRegistro = new javax.swing.JTextArea();
        txt_ip = new javax.swing.JTextField();
        btn_applyIP = new javax.swing.JButton();
        btn_presentIP = new javax.swing.JButton();
        txt_estado_servidor = new javax.swing.JLabel();
        btn_Reconectar = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        txt_DPI = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txt_nombre = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txt_apellido = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        combo_tipoCliente = new javax.swing.JComboBox<>();
        btn_Registrar = new javax.swing.JButton();
        btn_limpiar = new javax.swing.JButton();
        btn_chat = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("sansserif", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(101, 102, 246));
        jLabel1.setText("Registro Clientes");

        jLabel2.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(101, 102, 246));
        jLabel2.setText("Operador:");

        txt_operador.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        txt_operador.setForeground(new java.awt.Color(101, 102, 246));
        txt_operador.setText("--");

        jLabel4.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(153, 153, 153));
        jLabel4.setText("Servidor:");

        txt_ipactual.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        txt_ipactual.setForeground(new java.awt.Color(153, 153, 153));
        txt_ipactual.setText("--");

        jLabel5.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(153, 153, 153));
        jLabel5.setText("Servidor:");

        jTextAreaResultadoRegistro.setColumns(20);
        jTextAreaResultadoRegistro.setRows(5);
        jScrollPane1.setViewportView(jTextAreaResultadoRegistro);

        txt_ip.setBackground(new java.awt.Color(245, 245, 245));
        txt_ip.setForeground(new java.awt.Color(102, 102, 255));

        btn_applyIP.setBackground(new java.awt.Color(101, 102, 246));
        btn_applyIP.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        btn_applyIP.setForeground(new java.awt.Color(255, 255, 255));
        btn_applyIP.setText("Aplicar IP");

        btn_presentIP.setBackground(new java.awt.Color(244, 162, 120));
        btn_presentIP.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        btn_presentIP.setForeground(new java.awt.Color(255, 255, 255));
        btn_presentIP.setText("IP Predefinida");

        txt_estado_servidor.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        txt_estado_servidor.setForeground(new java.awt.Color(153, 153, 153));
        txt_estado_servidor.setText("Estado:");

        btn_Reconectar.setBackground(new java.awt.Color(101, 102, 246));
        btn_Reconectar.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        btn_Reconectar.setForeground(new java.awt.Color(255, 255, 255));
        btn_Reconectar.setText("Reconectar");

        jLabel6.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("DPI (13 dígitos):");

        txt_DPI.setBackground(new java.awt.Color(245, 245, 245));
        txt_DPI.setForeground(new java.awt.Color(102, 102, 255));

        jLabel7.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Nombre:");

        txt_nombre.setBackground(new java.awt.Color(245, 245, 245));
        txt_nombre.setForeground(new java.awt.Color(102, 102, 255));

        jLabel8.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("Apellido:");

        txt_apellido.setBackground(new java.awt.Color(245, 245, 245));
        txt_apellido.setForeground(new java.awt.Color(102, 102, 255));

        jLabel9.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("Tipo de Cliente:");

        combo_tipoCliente.setBackground(new java.awt.Color(245, 245, 245));
        combo_tipoCliente.setForeground(new java.awt.Color(102, 102, 255));
        combo_tipoCliente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btn_Registrar.setBackground(new java.awt.Color(119, 119, 121));
        btn_Registrar.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        btn_Registrar.setForeground(new java.awt.Color(255, 255, 255));
        btn_Registrar.setText("Registrar Cliente");

        btn_limpiar.setBackground(new java.awt.Color(204, 204, 204));
        btn_limpiar.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        btn_limpiar.setForeground(new java.awt.Color(0, 0, 0));
        btn_limpiar.setText("Limpiar");

        btn_chat.setBackground(new java.awt.Color(101, 102, 246));
        btn_chat.setFont(new java.awt.Font("sansserif", 1, 13)); // NOI18N
        btn_chat.setForeground(new java.awt.Color(255, 255, 255));
        btn_chat.setText("Chat");

        jLabel3.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(101, 102, 246));
        jLabel3.setText("Resultado del Registro");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_chat)
                .addGap(19, 19, 19))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txt_operador)
                                .addGap(39, 39, 39)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_ipactual))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_ip, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)))
                        .addGap(12, 12, 12)
                        .addComponent(btn_applyIP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_presentIP, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txt_nombre)
                                .addContainerGap())
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(txt_DPI)
                                .addGap(6, 6, 6))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_apellido)
                            .addComponent(combo_tipoCliente, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txt_estado_servidor, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_Reconectar))
                            .addComponent(jLabel3))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btn_Registrar, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btn_limpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_chat, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(jLabel1))
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txt_operador)
                    .addComponent(jLabel4)
                    .addComponent(txt_ipactual))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txt_ip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_applyIP)
                    .addComponent(btn_presentIP))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_estado_servidor)
                    .addComponent(btn_Reconectar))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txt_DPI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txt_nombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txt_apellido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(combo_tipoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_Registrar)
                    .addComponent(btn_limpiar))
                .addGap(36, 36, 36)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addGap(22, 22, 22))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_Reconectar;
    private javax.swing.JButton btn_Registrar;
    private javax.swing.JButton btn_applyIP;
    private javax.swing.JButton btn_chat;
    private javax.swing.JButton btn_limpiar;
    private javax.swing.JButton btn_presentIP;
    private javax.swing.JComboBox<String> combo_tipoCliente;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextAreaResultadoRegistro;
    private javax.swing.JTextField txt_DPI;
    private javax.swing.JTextField txt_apellido;
    private javax.swing.JLabel txt_estado_servidor;
    private javax.swing.JTextField txt_ip;
    private javax.swing.JLabel txt_ipactual;
    private javax.swing.JTextField txt_nombre;
    private javax.swing.JLabel txt_operador;
    // End of variables declaration//GEN-END:variables
}
