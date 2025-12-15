package U3ComunicacionEnRed;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class U3P03EchoClient {
    public static void main(String[] args) {

        // 1. CONFIGURACIÓN INICIAL
        // Es buena práctica poner esto en variables al principio por si te piden cambiar la IP o puerto rápido.
        String host = "localhost";
        int puerto = 3000; // ¡Ojo! Asegúrate de que el servidor escucha en el 3000

        System.out.println("--- Iniciando Cliente Echo ---");
        System.out.println("Conectando a " + host + ":" + puerto + "...");

        // 2. CONEXIÓN SEGURA (Try-with-resources EXTENDIDO)
        // Fíjate que aquí declaramos TODO dentro del paréntesis del try.
        // Al terminar el programa (o si hay error), Java cerrará el Socket, el Scanner y los Buffers automáticamente.
        try (Socket socket = new Socket(host, puerto);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); // true = autoFlush (envío inmediato)
             BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Conexión establecida. Escribe '/salir' para terminar.");

            String msg;
            String respuesta;

            // 3. BUCLE INFINITO CONTROLADO
            // Usamos while(true) y cortamos manualmente con 'break'.
            // A veces es más limpio que un do-while si hay muchas condiciones de salida.
            while (true) {
                System.out.print("Tú: "); // Decoración visual para saber cuándo escribir
                msg = sc.nextLine().trim();

                // A. ENVIAR MENSAJE
                pw.println(msg);

                // CONDICIÓN DE SALIDA 1: EL USUARIO QUIERE IRSE
                if (msg.equalsIgnoreCase("/salir")) {
                    break; // Rompe el while y va al final del programa
                }

                // B. LEER RESPUESTA (Bloqueante)
                // El programa se para aquí esperando al servidor.
                respuesta = bf.readLine();

                // CONDICIÓN DE SALIDA 2: EL SERVIDOR SE MUERE (¡IMPORTANTE!)
                // Si el servidor se apaga o cierra el socket, readLine() devuelve NULL.
                // Si no pones este if, tu programa podría entrar en un bucle infinito de errores o lanzar una excepción fea.
                if (respuesta == null) {
                    System.err.println("El servidor ha cerrado la conexión inesperadamente.");
                    break;
                }

                System.out.println("Servidor: " + respuesta);
            }

        } catch (IOException e) {
            // Error típico: El servidor no está encendido o la IP está mal.
            System.err.println("Error de conexión (¿Está el servidor encendido?): " + e.getMessage());
        }

        System.out.println("Comunicación finalizada.");
    }
}