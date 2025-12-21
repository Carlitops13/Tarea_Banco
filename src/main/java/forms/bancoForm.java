package forms;

import com.tareabanco.model.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class bancoForm extends JFrame {
    private JPanel mainPanel;
    private JLabel lblSaldo;
    private JLabel lblUsuario;
    private JButton btnDeposito;
    private JButton btnRetiro;
    private JButton btnTransferencia;
    private JButton SALIRButton;
    private JTextArea txtHistorial;

    private long userId;
    private String username;
    private double saldoActual;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public bancoForm(long userId, String username) {
        super("PoliBanco - " + username);
        this.userId = userId;
        this.username = username;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        cargarSaldo();
        buildUI();
        setLocationRelativeTo(null);
        attachHandlers();
        cargarHistorial();
        setVisible(true);
    }

    private void cargarSaldo() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT balance FROM accounts WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        saldoActual = rs.getDouble("balance");
                    } else {
                        saldoActual = 0.0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar saldo: " + e.getMessage());
        }
    }

    private void buildUI() {
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Título, usuario y saldo
        JPanel topPanel = new JPanel(new GridLayout(3, 1));

        JLabel title = new JLabel("Bienvenido a PoliBanco");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(50, 50, 50));
        topPanel.add(title);

        lblUsuario = new JLabel("Usuario: " + username);
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 14));
        lblUsuario.setForeground(new Color(100, 100, 100));
        topPanel.add(lblUsuario);

        lblSaldo = new JLabel("Saldo Actual: $" + String.format("%.2f", saldoActual));
        lblSaldo.setFont(new Font("Arial", Font.BOLD, 18));
        lblSaldo.setForeground(new Color(0, 100, 0));
        topPanel.add(lblSaldo);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6;
        gbc.weighty = 0.2;
        mainPanel.add(topPanel, gbc);

        // Panel de historial
        JScrollPane scrollPane = new JScrollPane();
        txtHistorial = new JTextArea();
        txtHistorial.setEditable(false);
        txtHistorial.setLineWrap(true);
        txtHistorial.setWrapStyleWord(true);
        txtHistorial.setFont(new Font("Courier New", Font.PLAIN, 11));
        scrollPane.setViewportView(txtHistorial);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridheight = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6;
        gbc.weighty = 0.8;
        mainPanel.add(scrollPane, gbc);

        // Botones
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.4;
        gbc.weighty = 0.1;

        SALIRButton = new JButton("SALIR");
        SALIRButton.setBackground(new Color(100, 100, 100));
        SALIRButton.setForeground(Color.WHITE);
        SALIRButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        mainPanel.add(SALIRButton, gbc);

        btnDeposito = new JButton("DEPÓSITO");
        btnDeposito.setBackground(new Color(100, 100, 100));
        btnDeposito.setForeground(Color.WHITE);
        btnDeposito.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 1;
        mainPanel.add(btnDeposito, gbc);

        btnRetiro = new JButton("RETIRO");
        btnRetiro.setBackground(new Color(100, 100, 100));
        btnRetiro.setForeground(Color.WHITE);
        btnRetiro.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 2;
        mainPanel.add(btnRetiro, gbc);

        btnTransferencia = new JButton("TRANSFERENCIA");
        btnTransferencia.setBackground(new Color(100, 100, 100));
        btnTransferencia.setForeground(Color.WHITE);
        btnTransferencia.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 3;
        mainPanel.add(btnTransferencia, gbc);

        setContentPane(mainPanel);
    }

    private void attachHandlers() {
        SALIRButton.addActionListener((ActionEvent e) -> {
            dispose();
            new loginForm();
        });

        btnDeposito.addActionListener((ActionEvent e) -> {
            realizarDeposito();
        });

        btnRetiro.addActionListener((ActionEvent e) -> {
            realizarRetiro();
        });

        btnTransferencia.addActionListener((ActionEvent e) -> {
            realizarTransferencia();
        });
    }

    private void realizarDeposito() {
        String input = JOptionPane.showInputDialog(this, "Ingrese la cantidad a depositar:", "");
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        try {
            double monto = Double.parseDouble(input);
            if (monto <= 0) {
                JOptionPane.showMessageDialog(this, "El monto debe ser mayor a cero");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String updateSql = "UPDATE accounts SET balance = balance + ? WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setDouble(1, monto);
                    ps.setLong(2, userId);
                    ps.executeUpdate();
                }

                saldoActual += monto;
                lblSaldo.setText("Saldo Actual: $" + String.format("%.2f", saldoActual));
                agregarAlHistorial("DEPÓSITO", "+$" + String.format("%.2f", monto), saldoActual);
                JOptionPane.showMessageDialog(this, "Depósito realizado exitosamente.\nNuevo saldo: $" + String.format("%.2f", saldoActual));
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto válido");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al realizar depósito: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void realizarRetiro() {
        String input = JOptionPane.showInputDialog(this, "Ingrese la cantidad a retirar:", "");
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        try {
            double monto = Double.parseDouble(input);
            if (monto <= 0) {
                JOptionPane.showMessageDialog(this, "El monto debe ser mayor a cero");
                return;
            }

            if (monto > saldoActual) {
                JOptionPane.showMessageDialog(this, "Saldo insuficiente.\nSaldo actual: $" + String.format("%.2f", saldoActual));
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String updateSql = "UPDATE accounts SET balance = balance - ? WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setDouble(1, monto);
                    ps.setLong(2, userId);
                    ps.executeUpdate();
                }

                saldoActual -= monto;
                lblSaldo.setText("Saldo Actual: $" + String.format("%.2f", saldoActual));
                agregarAlHistorial("RETIRO", "-$" + String.format("%.2f", monto), saldoActual);
                JOptionPane.showMessageDialog(this, "Retiro realizado exitosamente.\nNuevo saldo: $" + String.format("%.2f", saldoActual));
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto válido");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al realizar retiro: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void realizarTransferencia() {
        String usuarioDestino = JOptionPane.showInputDialog(this, "Ingrese el usuario destino:", "");
        if (usuarioDestino == null || usuarioDestino.trim().isEmpty()) {
            return;
        }

        String montoStr = JOptionPane.showInputDialog(this, "Ingrese el monto a transferir:", "");
        if (montoStr == null || montoStr.trim().isEmpty()) {
            return;
        }

        try {
            double monto = Double.parseDouble(montoStr);
            if (monto <= 0) {
                JOptionPane.showMessageDialog(this, "El monto debe ser mayor a cero");
                return;
            }

            if (monto > saldoActual) {
                JOptionPane.showMessageDialog(this, "Saldo insuficiente.\nSaldo actual: $" + String.format("%.2f", saldoActual));
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                // Obtener ID del usuario destino
                Long userIdDestino = null;
                String selectSql = "SELECT id FROM users WHERE username = ?";
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setString(1, usuarioDestino);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            userIdDestino = rs.getLong("id");
                        }
                    }
                }

                if (userIdDestino == null) {
                    JOptionPane.showMessageDialog(this, "Usuario destino no encontrado");
                    return;
                }

                if (userIdDestino == userId) {
                    JOptionPane.showMessageDialog(this, "No puedes transferir a tu propia cuenta");
                    return;
                }

                // Restar de la cuenta origen
                String updateOrigen = "UPDATE accounts SET balance = balance - ? WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateOrigen)) {
                    ps.setDouble(1, monto);
                    ps.setLong(2, userId);
                    ps.executeUpdate();
                }

                // Sumar a la cuenta destino
                String updateDestino = "UPDATE accounts SET balance = balance + ? WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateDestino)) {
                    ps.setDouble(1, monto);
                    ps.setLong(2, userIdDestino);
                    ps.executeUpdate();
                }

                saldoActual -= monto;
                lblSaldo.setText("Saldo Actual: $" + String.format("%.2f", saldoActual));
                agregarAlHistorial("TRANSFERENCIA", "A " + usuarioDestino + ": -$" + String.format("%.2f", monto), saldoActual);
                JOptionPane.showMessageDialog(this, "Transferencia realizada exitosamente.\nNuevo saldo: $" + String.format("%.2f", saldoActual));
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto válido");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al realizar transferencia: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void agregarAlHistorial(String tipo, String descripcion, double saldoResultante) {
        String timestamp = LocalDateTime.now().format(formatter);
        String linea = String.format("[%s] %s - %s | Saldo: $%.2f\n", timestamp, tipo, descripcion, saldoResultante);
        txtHistorial.append(linea);
    }

    private void cargarHistorial() {
        txtHistorial.setText("=== HISTORIAL DE TRANSACCIONES ===\n\n");
        agregarAlHistorial("INICIO", "Sesión iniciada", saldoActual);
    }
}

