package U3ComunicacionEnRed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class U3P08ServerLat {
    // 'throws Exception' permite ahorrarse algunos try-catch, pero mejor usar el bloque try abajo.
    public static void main(String[] args) throws Exception {

        // 1. ABRIR PUERTO
        // Debe coincidir con el puerto al que apunta el Cliente (2300)
        try(DatagramSocket ds = new DatagramSocket(2300)){
            System.out.println("Servidor Latencia (Espejo) iniciado en puerto 2300");

            // Buffer reutilizable (el cubo)
            byte[] data = new byte[1024];
            DatagramPacket dp;

            // 2. BUCLE INFINITO (Servidor siempre atento)
            while(true){

                // --- TRUCO DE EXAMEN ---
                // Reiniciamos el paquete en cada vuelta del bucle.
                // ¿Por qué? Porque cuando haces .receive(), el paquete se "achica" al tamaño del mensaje recibido.
                // Si no lo renuevas, podría dar problemas con mensajes siguientes.
                dp = new DatagramPacket(data, data.length);

                // 3. RECIBIR (Bloqueante)
                ds.receive(dp);

                // Leemos mensaje (solo para log, realmente no nos importa qué dice)
                String mensaje  = new String(dp.getData(), 0, dp.getLength());
                System.out.println("Recibido: " + mensaje);

                // 4. PREPARAR RESPUESTA (PONG)
                String respuesta = "pong";
                byte[] dataRes =  respuesta.getBytes();

                // 5. OBTENER REMITENTE (¿A quién se lo devuelvo?)
                // Sacamos la dirección de la carta que nos acaba de llegar
                InetAddress inetAddress = dp.getAddress();
                int port = dp.getPort();

                // 6. ENVIAR REBOTE
                // Creamos el paquete de vuelta y lo enviamos INMEDIATAMENTE.
                DatagramPacket dp2 = new DatagramPacket(dataRes, dataRes.length, inetAddress, port);
                ds.send(dp2);
            }
        }catch(IOException e){
            System.err.println("Error: " + e.getMessage());
        }
    }
}