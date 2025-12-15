package U3ComunicacionEnRed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class U3P07BroadcastingServer {
    public static void main(String[] args) {

        // 1. ABRIR EL PUERTO (La "Ventana")
        // IMPORTANTE: Este puerto (8453) TIENE que ser el mismo al que apunta el Cliente.
        // El servidor no necesita 'setBroadcast(true)'. Solo el que envía necesita permiso.
        try(DatagramSocket dgs = new DatagramSocket(8453)){

            System.out.println("Servidor escuchando en el puerto " + dgs.getLocalPort());

            // 2. PREPARAR EL CUBO (Buffer)
            // Un array de bytes vacío para recoger el agua (datos) que nos lancen.
            byte[] data = new byte[1024];

            // 3. PREPARAR EL PAQUETE RECEPTOR
            // Recordatorio: Constructor de 2 argumentos para RECIBIR.
            DatagramPacket dgp = new DatagramPacket(data, data.length);

            // 4. ESPERAR EL GRITO (Receive)
            // El programa se queda congelado aquí hasta que llegue algo al puerto 8453.
            dgs.receive(dgp);

            // 5. LEER EL MENSAJE
            // Extraemos los datos del paquete y los convertimos a texto legible.
            // Usamos dgp.getLength() para leer solo lo que nos han mandado y no basura extra.
            String mensaje = new String(dgp.getData(), 0, dgp.getLength());

            System.out.println("Mensaje recibido: " + mensaje);

        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}