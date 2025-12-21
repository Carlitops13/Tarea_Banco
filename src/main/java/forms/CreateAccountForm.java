package forms;

import com.tareabanco.model.DBConnection;
import com.tareabanco.util.EncryptionUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateAccountForm extends JFrame {
    private JPanel mainPanel;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtInitialBalance;
    private JButton btnCreate;
    private JButton btnCancel;

    public CreateAccountForm() {
        super("Crear Cuenta - Poli Banco");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(830, 633);
        buildUI();
        setLocationRelativeTo(null);
        attachHandlers();
        setVisible(true);
    }

    private void buildUI() {
        mainPanel = new JPanel(new GridLayout(1, 2));
        JPanel leftPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel logo = new JLabel();
        try {
            ImageIcon logoIcon = new ImageIcon("src/img/logo.png");
            Image scaledLogo = logoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            logo.setIcon(new ImageIcon(scaledLogo));
        } catch (Exception e) {
            logo.setText("LOGO");
            logo.setFont(new Font("Georgia", Font.BOLD, 24));
        }
        gbc.gridy = 0;
        leftPanel.add(logo, gbc);

        JLabel icon = new JLabel();
        try {
            ImageIcon iconImage = new ImageIcon("src/img/icon.png");
            Image scaledIcon = iconImage.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            icon.setIcon(new ImageIcon(scaledIcon));
        } catch (Exception e) {
            icon.setText("ICONO");
            icon.setFont(new Font("Georgia", Font.PLAIN, 16));
        }
        gbc.gridy = 1;
        leftPanel.add(icon, gbc);

        JLabel slogan = new JLabel("\" Llevamos tus metas al siguiente nivel \"");
        slogan.setFont(new Font("Georgia", Font.ITALIC, 16));
        gbc.gridy = 2;
        leftPanel.add(slogan, gbc);

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(220, 220, 220));
        rightPanel.setLayout(new GridBagLayout());

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Crear Nueva Cuenta");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        rightPanel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2;
        JLabel userLabel = new JLabel("Usuario");
        userLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        rightPanel.add(userLabel, gbc);

        gbc.gridy = 3;
        txtUsername = new JTextField(20);
        rightPanel.add(txtUsername, gbc);

        gbc.gridy = 4;
        JLabel passLabel = new JLabel("Contraseña");
        passLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        rightPanel.add(passLabel, gbc);

        gbc.gridy = 5;
        txtPassword = new JPasswordField(20);
        rightPanel.add(txtPassword, gbc);

        gbc.gridy = 6;
        JLabel balanceLabel = new JLabel("Saldo Inicial");
        balanceLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        rightPanel.add(balanceLabel, gbc);

        gbc.gridy = 7;
        txtInitialBalance = new JTextField("0", 20);
        rightPanel.add(txtInitialBalance, gbc);

        gbc.gridy = 8;
        gbc.gridwidth = 2;
        btnCreate = new JButton("Crear Cuenta");
        btnCreate.setBackground(new Color(100, 100, 100));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setFont(new Font("Georgia", Font.BOLD, 12));
        rightPanel.add(btnCreate, gbc);

        gbc.gridy = 9;
        btnCancel = new JButton("Cancelar");
        btnCancel.setBackground(new Color(100, 100, 100));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Georgia", Font.BOLD, 12));
        rightPanel.add(btnCancel, gbc);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        setContentPane(mainPanel);
    }

    private void attachHandlers() {
        btnCreate.addActionListener((ActionEvent e) -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String balanceStr = txtInitialBalance.getText().trim();

            if (username.isEmpty() || password.isEmpty() || balanceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor completa todos los campos");
                return;
            }

            double initialBalance;
            try {
                initialBalance = Double.parseDouble(balanceStr);
                if (initialBalance < 0) {
                    JOptionPane.showMessageDialog(this, "El saldo no puede ser negativo");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Saldo inválido");
                return;
            }

            String hashedPassword = EncryptionUtil.hashPassword(password);

            try (Connection conn = DBConnection.getConnection()) {
                String insUser = "INSERT INTO users(username, password, active) VALUES (?, ?, true) ON CONFLICT (username) DO NOTHING RETURNING id";
                Long userId = null;
                try (PreparedStatement ps = conn.prepareStatement(insUser)) {
                    ps.setString(1, username);
                    ps.setString(2, hashedPassword);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getLong(1);
                        }
                    }
                }

                if (userId == null) {
                    String sel = "SELECT id FROM users WHERE username = ?";
                    try (PreparedStatement ps2 = conn.prepareStatement(sel)) {
                        ps2.setString(1, username);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            if (rs2.next()) userId = rs2.getLong(1);
                        }
                    }
                }

                if (userId == null) {
                    JOptionPane.showMessageDialog(this, "No fue posible crear o recuperar el usuario");
                    return;
                }

                String insAccount = "INSERT INTO accounts(user_id, balance) SELECT ?, ? WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE user_id = ?);";
                try (PreparedStatement ps3 = conn.prepareStatement(insAccount)) {
                    ps3.setLong(1, userId);
                    ps3.setDouble(2, initialBalance);
                    ps3.setLong(3, userId);
                    ps3.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Cuenta creada correctamente. Usuario id=" + userId);
                dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al crear la cuenta: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener((ActionEvent e) -> {
            dispose();
        });
    }
}