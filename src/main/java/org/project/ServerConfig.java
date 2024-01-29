package org.project;

import java.util.Properties;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.*;

/**
 * La clase ServerConfig gestiona la configuración del servidor, leyendo y escribiendo en un archivo de propiedades.
 */
public class ServerConfig {
    private Properties properties; // Propiedades de configuración del servidor
    private String configFilePath = "config.properties"; // Ruta al archivo de configuración

    /**
     * Constructor de ServerConfig. Carga la configuración desde el archivo.
     */
    public ServerConfig() {
        properties = new Properties();
        loadConfig();
    }

    /**
     * Carga la configuración del servidor desde el archivo de propiedades.
     * Si el archivo no existe, se crea uno nuevo.
     */
    private void loadConfig() {
        File configFile = new File(configFilePath);
        if (configFile.exists()) {
            try (InputStream inputStream = new FileInputStream(configFile)) {
                properties.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Obtiene la ruta al directorio de archivos del programa desde las propiedades.
     *
     * @return La ruta al directorio de archivos del programa.
     */
    public String getProgramFilesPath() {
        return properties.getProperty("programFilesPath");
    }

    /**
     * Establece y guarda la ruta al directorio de archivos del programa en el archivo de configuración.
     *
     * @param path La ruta al directorio de archivos del programa.
     * @throws IOException Si ocurre un error al escribir en el archivo de configuración.
     */
    public void setProgramFilesPath(String path) throws IOException {
        properties.setProperty("programFilesPath", path);
        properties.store(new FileOutputStream(configFilePath), null);
    }
}