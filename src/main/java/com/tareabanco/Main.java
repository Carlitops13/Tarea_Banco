package com.tareabanco;

import javax.swing.SwingUtilities;
import forms.loginForm;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new loginForm());
    }
}

