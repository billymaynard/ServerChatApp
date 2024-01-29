package org.project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * La clase ServerDiscoveryThread extiende de Thread y se encarga de la detección del servidor en la red.
 * Utiliza un socket de datagrama para escuchar solicitudes de descubrimiento y responder a ellas.
 */
public class ServerDiscoveryThread extends Thread {
    public static final int DISCOVERY_PORT = 8888; // Puerto utilizado para el descubrimiento del servidor

    /**
     * El método run se ejecuta cuando el hilo comienza.
     * Escucha en el puerto de descubrimiento y responde a las solicitudes de descubrimiento.
     */
    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            while (!this.isInterrupted()) {
                // Prepara un paquete para recibir mensajes de descubrimiento
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

                // Recibe un paquete de descubrimiento
                socket.receive(packet);

                // Convierte el contenido del paquete a String
                String received = new String(packet.getData(), 0, packet.getLength());

                // Comprueba si el mensaje recibido es una solicitud de descubrimiento
                if ("DISCOVER_SERVER_REQUEST".equals(received)) {
                    // Prepara y envía una respuesta al mensaje de descubrimiento
                    byte[] sendData = "DISCOVER_SERVER_RESPONSE".getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket); // Enviar respuesta
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}