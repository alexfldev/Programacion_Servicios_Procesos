package U3ComunicacionEnRed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class U3P06ClientUDP {
    // Corrección pequeña: Te faltaba el 'public' antes del static void main
    public static void main(String[] args) {

        // 1. EL "BUZÓN" (DatagramSocket)
        // En el cliente no hace falta poner puerto en el new DatagramSocket().
        // El sistema operativo te asignará uno libre automáticamente para enviar.
        try(DatagramSocket ds = new DatagramSocket()){

            // 2. PREPARAR EL MENSAJE
            String mensaje = "Aaron es malo en valorant";
            // UDP solo entiende de BYTES, no de Strings. Hay que convertirlo.
            byte[] data = mensaje.getBytes();

            // 3. DATOS DEL DESTINATARIO
            InetAddress host = InetAddress.getByName("localhost");
            int port = 2100; // El puerto donde escucha el servidor

            // 4. CREAR EL PAQUETE (EL SOBRE)
            // Constructor para ENVIAR: (datos, longitud, DESTINATARIO, PUERTO)
            DatagramPacket dp = new DatagramPacket(data, data.length, host, port);

            // 5. ENVIAR
            ds.send(dp);
            System.out.println("Paquete enviado a " + host + ":" + port);

            // --- FASE DE RESPUESTA (ACK) ---

            // 6. PREPARAR UN ENVASE VACÍO
            // Necesitamos un array limpio donde el servidor volcará su respuesta.
            byte[] repuesta = new byte[1024]; // Espacio para 1024 bytes (1KB)

            // Constructor para RECIBIR: (buffer vacío, longitud del buffer)
            // Fíjate que aquí NO ponemos IP ni puerto, porque no sabemos quién nos responderá aún.
            DatagramPacket dp2 = new DatagramPacket(repuesta, repuesta.length);

            // 7. RECIBIR (Bloqueante)
            // El programa se para aquí hasta que llegue un paquete al buzón.
            ds.receive(dp2);

            // 8. PROCESAR RESPUESTA (IMPORTANTE PARA EL EXAMEN)
            // dp2.getData() -> Devuelve el array de 1024 bytes.
            // dp2.getLength() -> Devuelve CUÁNTOS bytes reales llegaron (ej: 5 bytes).
            // Si no usas getLength(), el String tendrá mucha "basura" o espacios vacíos al final.
            String mensajeServer = new String(dp2.getData(), 0 , dp2.getLength());

            System.out.println("Mensaje recibido: " + mensajeServer);

        }catch (IOException e){
            System.out.println("Error: "+e.getMessage());
        }
    }
}