package org.project;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * La clase ServerChatApp implementa un servidor para una aplicación de chat.
 */
public class ServerChatApp {
    // Número de puerto del servidor
    private static final int PORT = 49444;

    /**
     * El método principal para iniciar el servidor.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     * @throws IOException Si ocurre un error de E/S.
     */
    public static void main(String[] args) throws IOException {
        // Configuración para el servidor
        ServerConfig config = new ServerConfig();
        // Recupera la ruta de los archivos del programa de la configuración
        String programFilesPath = config.getProgramFilesPath();

        // Verifica si la ruta de los archivos del programa está establecida, de lo contrario solicita al usuario
        if (programFilesPath == null || programFilesPath.isEmpty()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            boolean validPath = false;
            while (!validPath) {
                System.out.println("Ingrese una ruta de directorio vacía para los archivos del programa:");
                programFilesPath = reader.readLine();

                File directory = new File(programFilesPath);
                // Valida la ruta del directorio ingresada
                if (!directory.exists()) {
                    System.out.println("El directorio no existe. Por favor, ingrese un directorio válido.");
                } else if (!directory.isDirectory()) {
                    System.out.println("La ruta no es un directorio. Por favor, ingrese un directorio válido.");
                } else {
                    File[] files = directory.listFiles();
                    if (files != null && files.length > 0) {
                        System.out.println("El directorio no está vacío. ¿Está seguro de que desea eliminar todo su contenido? (si/no)");
                        String response = reader.readLine();
                        if ("si".equalsIgnoreCase(response)) {
                            Util.deleteDirectoryRecursively(directory); // Elimina el contenido si se confirma
                            directory.mkdir(); // Recrea el directorio después de eliminarlo
                            validPath = true;
                        }
                    } else {
                        validPath = true;
                    }
                }
            }

            config.setProgramFilesPath(programFilesPath);
            Util.createDefaultFilesDirectories(programFilesPath);
        }

        // Inicia el hilo de descubrimiento del servidor
        ServerDiscoveryThread discoveryThread = new ServerDiscoveryThread();
        discoveryThread.start();

        // Crea un socket de servidor y comienza a escuchar conexiones
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("El servidor está escuchando en el puerto " + PORT);

        try {
            while (true) {
                // Acepta una conexión de un cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado");
                // Maneja al cliente conectado en un hilo separado
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } finally {
            // Cierra el socket del servidor cuando termina
            serverSocket.close();
        }
    }
}