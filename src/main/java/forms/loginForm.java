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

public class loginForm extends JFrame {
    private JPanel loginPanel;
    private JLabel icon;
    private JLabel logo;
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton button1;
    private JButton CREARCUENTAButton;
    private int failedAttempts = 0;

    public loginForm() {
        super("Login - Poli Banco");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(830, 633);
        buildUI();
        setLocationRelativeTo(null);
        attachHandlers();
        setVisible(true);
    }

    private void buildUI() {
        loginPanel = new JPanel(new GridLayout(1, 2));
        JPanel leftPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        logo = new JLabel();
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

        icon = new JLabel();
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

        JLabel welcome = new JLabel("Bienvenido a tu Poli Banco");
        welcome.setFont(new Font("Georgia", Font.BOLD, 20));
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        rightPanel.add(welcome, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2;
        JLabel userLabel = new JLabel("Usuario");
        userLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        rightPanel.add(userLabel, gbc);

        gbc.gridy = 3;
        textField1 = new JTextField(20);
        rightPanel.add(textField1, gbc);

        gbc.gridy = 4;
        JLabel passLabel = new JLabel("Contraseña");
        passLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        rightPanel.add(passLabel, gbc);

        gbc.gridy = 5;
        passwordField1 = new JPasswordField(20);
        rightPanel.add(passwordField1, gbc);

        gbc.gridy = 6;
        gbc.gridwidth = 2;
        button1 = new JButton("Ingresar");
        button1.setBackground(new Color(100, 100, 100));
        button1.setForeground(Color.WHITE);
        button1.setFont(new Font("Georgia", Font.PLAIN, 12));
        rightPanel.add(button1, gbc);

        gbc.gridy = 7;
        CREARCUENTAButton = new JButton("CREAR CUENTA");
        CREARCUENTAButton.setBackground(new Color(100, 100, 100));
        CREARCUENTAButton.setForeground(Color.WHITE);
        CREARCUENTAButton.setFont(new Font("Georgia", Font.BOLD, 12));
        rightPanel.add(CREARCUENTAButton, gbc);

        loginPanel.add(leftPanel);
        loginPanel.add(rightPanel);

        setContentPane(loginPanel);
    }

    private void attachHandlers() {
        button1.addActionListener((ActionEvent e) -> {
            String username = textField1.getText().trim();
            String enteredPassword = new String(passwordField1.getPassword());

            if (username.isEmpty() || enteredPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese usuario y contraseña");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT id, password, active FROM users WHERE username = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            failedAttempts++;
                            handleFailedAttempt();
                            return;
                        }

                        long userId = rs.getLong("id");
                        String dbHash = rs.getString("password");
                        boolean active = rs.getBoolean("active");

                        if (!active) {
                            JOptionPane.showMessageDialog(this, "Usuario inhabilitado. Contacte con el administrador.");
                            return;
                        }

                        if (EncryptionUtil.verifyPassword(enteredPassword, dbHash)) {
                            JOptionPane.showMessageDialog(this, "Bienvenido " + username);
                            dispose();
                            new bancoForm(userId, username);
                        } else {
                            failedAttempts++;
                            handleFailedAttempt();
                        }
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
            }
        });

        CREARCUENTAButton.addActionListener(e -> {
            new CreateAccountForm();
        });
    }

    private void handleFailedAttempt() {
        if (failedAttempts >= 3) {
            JOptionPane.showMessageDialog(this, "Máximos intentos alcanzados. Acceso bloqueado.");
            button1.setEnabled(false);
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos. Intentos: " + failedAttempts + "/3");
        }
    }
}