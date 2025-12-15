package U3ComunicacionEnRed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class U3P08ClientLat {
    public static void main(String[] args){

        // 1. INICIAR SOCKET
        try(DatagramSocket ds = new DatagramSocket()){

            // --- ¡IMPORTANTE PARA EL EXAMEN! ---
            // Si no pones esto, el catch(SocketTimeoutException) NUNCA funcionará.
            // Establecemos que si en 1000ms (1 seg) no llega respuesta, salte el error.
            ds.setSoTimeout(1000);

            // 2. PREPARAR DATOS
            String ping = "ping";
            byte[] pingBytes = ping.getBytes();
            InetAddress inetAddress = InetAddress.getLocalHost();
            int port = 2300;

            // Paquete de envío (Carta con dirección)
            DatagramPacket dp =  new DatagramPacket(pingBytes, pingBytes.length, inetAddress, port);

            // 3. BUCLE DE PRUEBAS (Lanzamos 10 pings)
            for (int i = 0; i < 10; i++){

                // A. MARCAR HORA DE SALIDA
                // Usamos nanoTime() porque es mucho más preciso que currentTimeMillis()
                long tiempo = System.nanoTime();

                try {
                    // B. ENVIAR
                    ds.send(dp);

                    // C. PREPARAR RECEPCIÓN
                    byte[] dataACK = new byte[1024];
                    DatagramPacket dpAck = new DatagramPacket(dataACK, dataACK.length);

                    // D. ESPERAR RESPUESTA
                    // Aquí se espera máximo 1 segundo (por el setSoTimeout).
                    ds.receive(dpAck);

                    // E. MARCAR HORA DE LLEGADA
                    long tiempoAck = System.nanoTime();

                    // F. CÁLCULO MATEMÁTICO
                    // Restamos tiempo final - tiempo inicial.
                    // Dividimos por 1.000.000 porque 1 milisegundo = 1 millón de nanosegundos.
                    double latenciaMs = (tiempoAck - tiempo) / 1_000_000.0;

                    // Imprimimos formateando a 2 decimales (%.2f)
                    System.out.println("Ping " + (i + 1) + " - Latencia: " + String.format("%.2f", latenciaMs) + "ms");

                }catch (SocketTimeoutException e){
                    // Si pasaron 1000ms y nadie respondió, entra aquí.
                    System.err.println("Ping " + (i+1) + ": Tiempo de espera agotado (Paquete perdido).");
                }
            }
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
    }
}