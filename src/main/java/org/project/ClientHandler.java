package org.project;

import org.project.common.Message; //Es necesario añadir el Message.jar a las librerias...

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * La clase ClientHandler gestiona la comunicación con un cliente individual en un servidor de chat.
 * Implementa Runnable para ser ejecutada en un hilo separado.
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;               // Socket para la comunicación con el cliente.
    private ObjectInputStream objectInputStream;  // Flujo de entrada para recibir mensajes del cliente.
    private ObjectOutputStream objectOutputStream; // Flujo de salida para enviar mensajes al cliente.
    private String userName;                   // Nombre de usuario asignado al cliente.
    private static List<ClientHandler> clients = new ArrayList<>(); // Lista estática de todos los manejadores de clientes.

    /**
     * Constructor de ClientHandler.
     * Inicializa los flujos de entrada y salida para la comunicación con el cliente.
     *
     * @param socket Socket del cliente conectado.
     */
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Ejecuta el hilo y maneja el ciclo de vida de la conexión con el cliente.
     */
    @Override
    public void run() {
        try {
            while (true) {
                Message message = (Message) objectInputStream.readObject();
                String sender = message.getSender();
                String content = message.getContent();

                // Procesa mensajes especiales del sistema como login, registro y desconexión.
                if ("System".equalsIgnoreCase(sender)) {
                    if (content.startsWith("#LOGIN")) {
                        handleLogin(message);
                    } else if (content.startsWith("#REGISTER")) {
                        handleRegister(message);
                    } else if (content.startsWith("#DISCONNECT")) {
                        break; // Salir del bucle para cerrar la conexión.
                    }
                } else {
                    // Maneja mensajes normales de los usuarios.
                    if (userName != null && Objects.equals(message.getSender(), userName) && !content.trim().isEmpty()) {
                        broadcastMessage(message);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeConnections(); // Cierra las conexiones al finalizar.
        }
    }

    /**
     * Maneja el proceso de inicio de sesión del cliente.
     *
     * @param loginMessage Mensaje con las credenciales de inicio de sesión.
     */
    private void handleLogin(Message loginMessage) {
        String[] credentials = loginMessage.getContent().split(" ", 3);
        if (credentials.length == 3) {
            String username = credentials[1];
            String password = credentials[2];
            // Autentica al usuario y actualiza el estado si es exitoso.
            if (authenticateUser(username, password)) {
                this.userName = username;
                clients.add(this);
                sendConfirmationMessage("Login successful");
                broadcastMessage(new Message("Server", userName + " has joined the chat!"));
            } else {
                sendConfirmationMessage("Login failed");
            }
        }
    }

    /**
     * Maneja el proceso de registro de un nuevo cliente.
     *
     * @param registerMessage Mensaje con las credenciales de registro.
     */
    private void handleRegister(Message registerMessage) {
        String[] credentials = registerMessage.getContent().split(" ", 3);
        if (credentials.length == 3) {
            String username = credentials[1];
            String password = credentials[2];
            boolean registrationSuccessful = registerUser(username, password);
            if (registrationSuccessful) {
                this.userName = username;
                clients.add(this);
                sendConfirmationMessage("Registration successful");
                broadcastMessage(new Message("Server", userName + " has joined the chat!"));
            } else {
                sendConfirmationMessage("Registration failed");
            }
        }
    }

    /**
     * Envía un mensaje de confirmación al cliente.
     *
     * @param message Mensaje de confirmación.
     */
    private void sendConfirmationMessage(String message) {
        try {
            objectOutputStream.writeObject(new Message("System", message));
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Transmite un mensaje a todos los clientes conectados.
     *
     * @param message Mensaje a transmitir.
     */
    private void broadcastMessage(Message message) {
        for (ClientHandler client : clients) {
            try {
                client.objectOutputStream.writeObject(message);
                client.objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logMessage(userName, message.getContent());
    }

    /**
     * Registra un mensaje en el archivo de logs.
     *
     * @param sender  Remitente del mensaje.
     * @param message Mensaje a registrar.
     */
    private void logMessage(String sender, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = timestamp + " " + sender + ": " + message + "\n";
        ServerConfig config = new ServerConfig();

        try {
            Files.write(Paths.get(config.getProgramFilesPath() + "/chats/chat.txt"), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Cierra todas las conexiones y recursos asociados con el cliente.
     */
    private void closeConnections() {
        try {
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }

            clients.remove(this);
            broadcastDisconnectMessage();
            System.out.println(userName + " has disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Transmite un mensaje a todos los clientes cuando un usuario se desconecta.
     */
    private void broadcastDisconnectMessage() {
        Message disconnectMessage = new Message("Server", userName + " has left the chat!");
        for (ClientHandler client : clients) {
            if (!client.equals(this)) { // Avoid sending to the disconnecting client
                try {
                    client.objectOutputStream.writeObject(disconnectMessage);
                    client.objectOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña del usuario.
     * @return Verdadero si el registro es exitoso, falso en caso contrario.
     */
    public static boolean registerUser(String username, String password) {
        ServerConfig config = new ServerConfig();
        Path usersFilePath = Paths.get(config.getProgramFilesPath() + "/users.txt");

        try {
            // Check if the file exists and create it if it doesn't
            if (!Files.exists(usersFilePath)) {
                Files.createFile(usersFilePath);
            }

            // Read all lines (users) from the file
            List<String> users = Files.readAllLines(usersFilePath);

            // Check if username already exists
            for (String user : users) {
                if (user.split(":")[0].equals(username)) {
                    return false; // Username already exists
                }
            }

            // Username is new, register the user
            String userEntry = username + ":" + password + "\n"; // Password should be hashed in a real application
            Files.write(usersFilePath, userEntry.getBytes(), StandardOpenOption.APPEND);
            return true; // User registered successfully

        } catch (IOException e) {
            e.printStackTrace();
            return false; // Return false in case of an IO error
        }
    }
    /**
     * Autentica a un usuario existente.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña del usuario.
     * @return Verdadero si la autenticación es exitosa, falso en caso contrario.
     */
    public static boolean authenticateUser(String username, String password) {
        ServerConfig config = new ServerConfig();

        try {
            List<String> lines = Files.readAllLines(Paths.get(config.getProgramFilesPath() + "/users.txt"));
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true; // Password check should be against a hashed password in a real application
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}