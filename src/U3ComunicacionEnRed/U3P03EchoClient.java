package U3ComunicacionEnRed;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class U3P03EchoClient {
    public static void main(String[] args) {
        // Configuración de conexión (coincidiendo con tu servidor)
        // Nota: En un entorno real, estos valores deberían venir de args[], como hiciste en el servidor.
        String host = "localhost";
        int puerto = 3000;

        System.out.println("--- Iniciando Cliente Echo ---");
        System.out.println("Conectando a " + host + ":" + puerto + "...");

        // Try-with-resources: Cierra socket, scanner y streams automáticamente al salir
        try (Socket socket = new Socket(host, puerto);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Conexión establecida. Escribe '/salir' para terminar.");

            String msg;
            String respuesta;

            // Bucle infinito controlado por breaks
            while (true) {
                System.out.print("Tú: "); // Prompt visual
                msg = sc.nextLine().trim();

                // 1. Enviamos el mensaje al servidor
                pw.println(msg);

                // Si el usuario quiere salir, rompemos el bucle antes de esperar respuesta
                // (Opcional: puedes esperar la confirmación del servidor si prefieres)
                if (msg.equalsIgnoreCase("/salir")) {
                    break;
                }

                // 2. Esperamos la respuesta del servidor (bloqueante)
                respuesta = bf.readLine();

                // 3. Validación crítica: Si el servidor se apaga, respuesta será null
                if (respuesta == null) {
                    System.err.println("El servidor ha cerrado la conexión inesperadamente.");
                    break;
                }

                System.out.println("Servidor: " + respuesta);
            }

        } catch (IOException e) {
            System.err.println("Error de conexión (¿Está el servidor encendido?): " + e.getMessage());
        }

        System.out.println("Comunicación finalizada.");
    }
}