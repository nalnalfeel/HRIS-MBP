package com.paralel.hrismbp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.xml.transform.Result;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    private void handleLogin() throws IOException {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()){
            lblError.setText("Username dan Password tidak boleh kosong!");
            return;
        }

        if (validateLogin(username, password)){
            openDashboard();
        }else {
            lblError.setText("Username atau Password salah!");
        }
    }

    // Menggunakan LOWER() agar username bersifat Case-Insensitive
    private boolean validateLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?) AND password = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            lblError.setText("Database Error: " + e.getMessage());
            return false;
        }
    }

    private void openDashboard() throws IOException {
        try {
            Stage stage = (Stage) txtUsername.getScene().getWindow();

            //Loading page
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1100, 650);

            stage.setTitle("HRIS System - PT Multi Bina Prakarsa");
            stage.setScene(scene);
            stage.centerOnScreen();

            InputStream iconStream = MainApp.class.getResourceAsStream("/com/paralel/hrismbp/logo.png");
            if (iconStream != null && stage.getIcons().isEmpty()){
                stage.getIcons().add(new Image(iconStream));
            }

            // --- PENGATURAN RESPONSIF JENDELA UTAMA ---
            stage.setResizable(true);   // Izinkan ubah ukuran (Minimize & Maximize aktif)
            stage.setMaximized(true);   // Tampilan awal langsung Maximize
            stage.setMinWidth(1000);    // Batas minimal lebar aplikasi
            stage.setMinHeight(650);    // Batas minimal tinggi aplikasi

        } catch (Exception e){
            e.printStackTrace();
            lblError.setText("Gagal memuat halaman utama: " + e.getMessage());
        }

    }
}
