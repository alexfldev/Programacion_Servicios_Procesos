package U3ComunicacionEnRed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class U3P07BroadcastingClient {
    public static void main(String[] args) {
        int puertoServer = 8453; // Puerto donde los demás escucharán el grito

        try (DatagramSocket dgs = new DatagramSocket()) {
            String mensaje = "Mensaje de broadcast";
            byte[] data = mensaje.getBytes();

            // CLAVE 1: LA DIRECCIÓN IP ESPECIAL
            // No ponemos la IP de un ordenador concreto. Ponemos la dirección de DIFUSIÓN.
            // - "255.255.255.255" suele ser la dirección para "todos en mi red local".
            // - "10.255.255.255" es la dirección de broadcast específica para redes clase A (10.x.x.x).
            // Si en el examen estás en una red normal (192.168.1.x), usa "192.168.1.255" o "255.255.255.255".
            InetAddress broadcastIP = InetAddress.getByName("10.255.255.255");

            // Creamos el paquete igual que siempre
            DatagramPacket dgp = new DatagramPacket(data, data.length, broadcastIP, puertoServer);

            // CLAVE 2: PERMISO PARA GRITAR (CRÍTICO)
            // Por seguridad, los Sockets bloquean el broadcast por defecto.
            // Tienes que activar esto explícitamente o dará error al enviar.
            dgs.setBroadcast(true);

            // Enviamos el paquete
            dgs.send(dgp);
            System.out.println("Mensaje enviado a TODOS en " + broadcastIP);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}