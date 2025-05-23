package edu.vidura.inclass.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class ClientController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String clientId;
    private ExecutorService senderExecutor;

    public void initialize() {
        try {
            socket = new Socket("localhost", 5000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            String welcomeMessage = reader.readLine();
            if (welcomeMessage != null && welcomeMessage.startsWith("Welcome: ")) {
                clientId = welcomeMessage.substring(9);
                Platform.runLater(() -> chatArea.appendText(welcomeMessage + "\n"));
            } else {
                clientId = "Client2";
                Platform.runLater(() -> chatArea.appendText("Connected to server.\n"));
            }

            Thread readerThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = reader.readLine()) != null) {
                        String finalMessage = message;
                        Platform.runLater(() -> chatArea.appendText(finalMessage + "\n"));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> chatArea.appendText("Connection closed.\n"));
                    closeConnection();
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

        } catch (IOException e) {
            Platform.runLater(() -> chatArea.appendText("Unable to connect to server.\n"));
            closeConnection();
        }
    }

    @FXML
    void sendMessage(ActionEvent event) {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty() && writer != null) {
            writer.println(clientId + ": " + msg);
            Platform.runLater(() -> {
                chatArea.appendText("Sent: " + msg + "\n");
                messageField.clear();
            });
        }
    }

    private void closeConnection() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}