package U3ComunicacionEnRed;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class U3P02EchoClient {
    public static void main(String[] args) {
        // Scanner para leer lo que tú escribes por teclado
        Scanner sc = new Scanner(System.in);

        // 1. CONEXIÓN
        // Intentamos conectar a "localhost" (tu PC) por el puerto 1025.
        // El try(...) cierra el socket automáticamente si hay error o al terminar.
        try (Socket socket = new Socket("localhost", 1025)) {

            // 2. PREPARAR CANAL DE ENVÍO (Salida)
            OutputStream os = socket.getOutputStream();
            // PrintWriter con 'true' para enviar datos inmediatamente (autoFlush)
            PrintWriter pw = new PrintWriter(os, true);

            // 3. PREPARAR CANAL DE RECEPCIÓN (Entrada)
            InputStream is = socket.getInputStream();
            // BufferedReader para leer líneas completas de texto que llegan del servidor
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String entrada;

            // 4. BUCLE DE COMUNICACIÓN (Escribir -> Leer)
            do{
                // A. Leemos lo que escribes en la consola
                entrada = sc.nextLine();

                // B. Se lo enviamos al servidor
                pw.println(entrada);

                // C. Esperamos respuesta del servidor
                // CUIDADO: br.readLine() detiene el programa hasta que el servidor contesta.
                System.out.println("Recibido del servidor: " + br.readLine());

                // 5. CONDICIÓN DE SALIDA
                // El bucle se repite mientras NO escribas "0".
                // Si escribes "0", sale del bucle y el programa termina.
            }while (!entrada.equals("0"));


        } catch (IOException e) {
            // Si falla la conexión (ej: servidor apagado), salta este error.
            throw new RuntimeException(e);
        }
    }
}