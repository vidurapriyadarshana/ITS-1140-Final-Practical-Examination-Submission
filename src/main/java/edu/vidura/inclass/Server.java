package edu.vidura.inclass;

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Server {

    private static final int PORT = 5000;
    private static final Map<String, PrintWriter> clients = Collections.synchronizedMap(new HashMap<>());
    private static final long startTime = System.currentTimeMillis();

    public static void main(String[] args) {
        System.out.println("Server Started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            int clientCount = 0;

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientCount++;
                String clientId = "Client" + clientCount;
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

                synchronized (clients) {
                    clients.put(clientId, writer);
                }

                writer.println("Welcome: " + clientId);

                Thread clientThread = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                        String message;
                        while ((message = reader.readLine()) != null) {
                            String command = message.toLowerCase().startsWith(clientId.toLowerCase() + ": ")
                                    ? message.substring(clientId.length() + 2).trim().toLowerCase()
                                    : message.trim().toLowerCase();

                            String response;
                            boolean closeConnection = false;

                            switch (command) {
                                case "time":
                                    LocalTime time = LocalTime.now();
                                    response = "Server time: " + time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                                    break;
                                case "date":
                                    LocalDate date = LocalDate.now();
                                    response = "Server date: " + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                                    break;
                                case "bye":
                                    response = "Connection closing. Goodbye, " + clientId;
                                    closeConnection = true;
                                    break;
                                case "help":
                                    response = "Commands: time,date,bye,uptime,help";
                                    break;
                                case "uptime":
                                    long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;
                                    response = "Server uptime: " + uptimeSeconds + " seconds";
                                    break;
                                default:
                                    response = "Unknown command. Type 'help' for commands list";
                                    break;
                            }

                            writer.println(response);

                            if (closeConnection) {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        System.out.println(clientId + " disconnected");
                    } finally {
                        synchronized (clients) {
                            clients.remove(clientId);
                        }
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                clientThread.setDaemon(true);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}