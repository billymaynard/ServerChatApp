package org.project;

import java.io.File;
import java.io.IOException;

/**
 * La clase Util contiene métodos utilitarios para operaciones comunes en el servidor de chat.
 */
public class Util {

    /**
     * Elimina un directorio y todo su contenido de manera recursiva.
     *
     * @param directory El directorio a eliminar.
     */
    static void deleteDirectoryRecursively(File directory) {
        File[] allContents = directory.listFiles(); // Lista todos los archivos y directorios dentro del directorio.
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectoryRecursively(file); // Llamada recursiva para eliminar subdirectorios y archivos.
            }
        }
        directory.delete(); // Elimina el directorio después de eliminar su contenido.
    }

    /**
     * Crea los archivos y directorios predeterminados para el servidor de chat.
     *
     * @param path La ruta base donde se crearán los archivos y directorios.
     */
    public static void createDefaultFilesDirectories(String path) {
        // Crea el directorio 'chats'.
        new File(path, "chats").mkdir();

        // Crea el archivo 'users.txt'.
        try {
            new File(path, "users.txt").createNewFile();
        } catch (IOException e) {
            // Maneja la potencial IOException silenciosamente o regístrala según sea necesario.
        }
    }
}