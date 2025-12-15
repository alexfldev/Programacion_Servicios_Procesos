package U3ComunicacionEnRed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class U3P05ClienteDiccionario{

    // 1. CONSTANTES
    // Siempre es mejor tener esto arriba para cambios rápidos en el examen.
    private static final String HOST = "localhost";
    private static final int PORT = 4000;

    public static void main(String[] args) {

        // 2. CONEXIÓN (Try-with-resources)
        // Se conecta y abre las tuberías. Al terminar, cierra todo solo.
        try(Socket socket = new Socket(HOST, PORT);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)){ // 'true' es vital para el autoFlush

            System.out.println("Conectado con el servidor de Diccionario");

            Scanner sc = new Scanner(System.in);
            String comando;

            // 3. BUCLE DE INTERACCIÓN
            do{
                // A. Instrucciones al usuario
                // El servidor espera formatos específicos:
                // - "trd gato" -> Traducir gato
                // - "inc gato cat" -> Incluir palabra nueva
                // - "lis" -> Listar todo
                System.out.print("Introduzca un comando (trd <palabra> /inc <palabra> <traduccion>/lis/sal|bye): ");

                // B. Lectura de teclado
                comando = sc.nextLine();

                // C. Envío al servidor
                // Enviamos la línea entera (ej: "inc mesa table").
                // Será el SERVIDOR quien tenga que trocear esa frase para entenderla.
                pw.println(comando);

                // D. Respuesta del servidor
                // Bloqueante: Esperamos hasta que el servidor conteste (la traducción o el error).
                System.out.println(br.readLine());

                // 4. CONDICIÓN DE SALIDA DOBLE
                // El bucle sigue mientras NO escribas "bye" Y TAMPOCO "sal".
                // .trim() quita espacios y .equalsIgnoreCase ignora mayúsculas.
            }while (!comando.trim().equalsIgnoreCase("bye") && !comando.trim().equalsIgnoreCase("sal"));

        }catch (IOException e){
            System.err.println("Error en la conexión: " + e.getMessage());
        }
    }
}