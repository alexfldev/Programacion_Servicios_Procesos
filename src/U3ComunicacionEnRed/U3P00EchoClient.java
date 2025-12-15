package U3ComunicacionEnRed;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class U3P00EchoClient {
    public static void main(String[] args) throws IOException {

        // PASO 1: CONEXIÓN
        // El try con paréntesis (try-with-resources) cierra el socket automáticamente al final.
        // "localhost" es la IP (tu propia máquina) y 4000 es el puerto donde escucha el servidor.
        try (Socket socket = new Socket("localhost", 4000)){

            // PASO 2: PREPARAR TUBERÍAS (STREAMS)
            // InputStream = Para RECIBIR datos del servidor (El "oído" del programa).
            InputStream in = socket.getInputStream();
            // OutputStream = Para ENVIAR datos al servidor (La "boca" del programa).
            OutputStream out = socket.getOutputStream();

            // PASO 3: MEJORAR LAS TUBERÍAS (DECORADORES)
            // BufferedReader: Nos permite leer líneas completas de texto (readLine) en lugar de bytes sueltos.
            BufferedReader bf = new BufferedReader(new InputStreamReader(in));

            // PrintWriter: Nos permite enviar texto cómodamente (println).
            // ¡IMPORTANTE! El 'true' activa el autoFlush. Sin esto, el mensaje se puede quedar atascado en el buffer y no salir.
            PrintWriter pw = new PrintWriter(out, true);

            // Scanner para leer lo que tú escribes por teclado.
            Scanner sc = new Scanner(System.in);
            String msg;

            // PASO 4: BUCLE DE COMUNICACIÓN
            do {
                // A. Leemos lo que el usuario escribe en consola
                System.out.println("Introduzca un texto: ");
                msg = sc.nextLine().trim(); // .trim() quita espacios sobrantes al inicio/final

                // B. Se lo ENVIAMOS al servidor
                pw.println(msg);

                // C. Esperamos y LEEMOS la respuesta del servidor
                // El programa se detiene aquí (bloqueante) hasta que el servidor responde algo.
                System.out.println("Devuelto por el servidor: " + bf.readLine());

                // El bucle se repite hasta que escribas "/salir" (ignorando mayúsculas/minúsculas)
            } while(!msg.equalsIgnoreCase("/salir"));

        } catch (IOException e){
            // GESTIÓN DE ERRORES: Si el servidor está apagado o falla la red, entra aquí.
            System.err.println("Error de conexión: " + e.getMessage());
        }

        System.out.println("Comunicación finalizada");
    }
}