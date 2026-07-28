package com.paralel.hrismbp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Inisialisasi Database SQLite
        Database.initDatabase();

        //Tampilan halaman login
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 420, 480);

        stage.setTitle("Login - HRIS Manpower");
        stage.setScene(scene);

        InputStream iconStream = MainApp.class.getResourceAsStream("/com/paralel/hrismbp/LOGO.png");
        if (iconStream != null){
            stage.getIcons().add(new Image(iconStream));
        }

        // --- PENGATURAN UKURAN JENDELA ---
        stage.setResizable(true);   // Mengizinkan ubah ukuran (Minimize / Maximize / Drag)
        stage.setMaximized(true);   // Langsung penuhi layar saat pertama kali terbuka
        // Opsional: Batas ukuran minimal jendela agar layout tidak berantakan jika dikecilkan
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
