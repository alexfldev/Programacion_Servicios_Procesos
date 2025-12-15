package U3ComunicacionEnRed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class U3P06ServerUDP {
    public static void main(String[] args) {

        // 1. ABRIR EL PUERTO (El Buzón)
        // Creamos el socket en el puerto 2100.
        // A diferencia de TCP, aquí NO hacemos .accept(). Simplemente esperamos paquetes.
        try (DatagramSocket ds = new DatagramSocket(2100)){
            System.out.println("Servidor UDP escuchando en el puerto " + ds.getLocalPort());

            // --- FASE 1: RECIBIR ---

            // 2. PREPARAR EL ENVASE (Buffer)
            // Creamos un array vacío para guardar lo que llegue.
            byte[] data = new byte[1024];

            // 3. CREAR PAQUETE VACÍO (Receptor)
            // Constructor de 2 argumentos: (donde guardar datos, tamaño máximo)
            DatagramPacket dp = new DatagramPacket(data, data.length);

            // 4. ESPERAR MENSAJE (Bloqueante)
            // El servidor se duerme aquí hasta que alguien le lanza un paquete al puerto 2100.
            ds.receive(dp);

            // 5. LEER EL MENSAJE
            // dp.getData(): Los bytes recibidos.
            // dp.getLength(): ¡IMPORTANTE! Solo leemos los bytes ÚTILES que llegaron, no los 1024 enteros.
            String mensaje = new String(dp.getData(), 0, dp.getLength());
            System.out.println("Mensaje recibido: " + mensaje);

            // --- FASE 2: RESPONDER (La parte delicada) ---

            // 6. PREPARAR RESPUESTA
            String ack = "ACK " + mensaje; // "ACK" significa "Acuse de Recibo" (Confirmado)
            byte[] dataACK = ack.getBytes();

            // 7. ¿A QUIÉN RESPONDO? (Extracción de datos)
            // Como no hay conexión fija, tenemos que sacar la IP y el PUERTO del paquete que nos llegó (dp).
            InetAddress host = dp.getAddress(); // ¿Quién me escribió?
            int port = dp.getPort();            // ¿Desde qué puerto me escribió?

            // 8. CREAR PAQUETE DE RESPUESTA
            // Constructor de 4 argumentos: (mensaje, longitud, DESTINATARIO, PUERTO DESTINO)
            DatagramPacket dtp  =  new DatagramPacket(dataACK, dataACK.length, host, port);

            // 9. ENVIAR
            ds.send(dtp);
            System.out.println("Respuesta enviada a " + host + ":" + port);

        } catch (IOException e){
            System.err.println("Error en el servidor " + e.getMessage());
        }
    }
}