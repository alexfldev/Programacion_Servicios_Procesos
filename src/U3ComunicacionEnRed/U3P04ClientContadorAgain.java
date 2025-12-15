package U3ComunicacionEnRed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class U3P04ClientContadorAgain {

    // 1. CONSTANTES DE CONFIGURACIÓN
    // Es mejor usar esto que poner "localhost" y 4000 directamente en el código.
    // Si en el examen te piden cambiar el puerto, solo tocas aquí.
    private static final String HOST = "localhost";
    private static final int PORT = 4000;

    public static void main(String[] args) {

        // 2. CONEXIÓN Y TUBERÍAS (Try-with-resources)
        // Abrimos el Socket y los canales de lectura/escritura en el mismo bloque.
        // Al terminar el main, Java cerrará todo esto automáticamente.
        try(Socket socket = new Socket(HOST, PORT);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)){ // 'true' para enviar al instante

            System.out.println("Conectado con el servidor de Contador");

            Scanner sc = new Scanner(System.in);
            String comando;

            // 3. BUCLE DE COMANDOS
            // Usamos do-while para asegurar que entramos al menos una vez.
            do{
                // A. Pedimos la orden al usuario
                // Las órdenes esperadas son: inc (sumar), dec (restar), get (ver valor), bye (salir)
                System.out.print("Introduzca un comando (inc/dec/get/bye): ");
                comando = sc.nextLine();

                // B. Enviamos la orden al servidor
                pw.println(comando);

                // C. LEEMOS LA RESPUESTA
                // Aquí el servidor nos devolverá el valor actual del contador (ej: "5", "6", "Usuario desconectado")
                // El programa se detiene (bloquea) esperando esa respuesta.
                System.out.println(br.readLine());

                // 4. CONDICIÓN DE SALIDA
                // El bucle sigue mientras NO escribamos "bye" (ignorando mayúsculas/minúsculas)
            }while (!comando.trim().equalsIgnoreCase("bye"));

        }catch (IOException e){
            // Si el servidor no está encendido, caeremos aquí.
            System.err.println("Error en la conexión: " + e.getMessage());
        }
        // Al salir del try, el socket se cierra solo.
    }
}