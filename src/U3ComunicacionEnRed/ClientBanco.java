package U3ComunicacionEnRed;


import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientBanco {
    public static void main(String[] args) {

        // Configuración de conexión
        String host = "localhost";
        int puerto = 6000;

        System.out.println("Conectando al cajero...");

        // Try-with-resources: Cierra todo automáticamente al salir
        try (Socket socket = new Socket(host, puerto);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); // true = autoFlush
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner sc = new Scanner(System.in)) {

            // 1. LEER BIENVENIDA
            // Nada más conectar, el servidor nos manda el mensaje de "BIENVENIDO..."
            System.out.println("BANCO: " + br.readLine());

            String miComando;
            String respuestaBanco;

            // 2. BUCLE DE INTERACCIÓN
            while (true) {
                System.out.print("Usuario > "); // Decoración visual
                miComando = sc.nextLine();      // Leemos teclado

                // Enviamos lo que hemos escrito al servidor
                pw.println(miComando);

                // Si queremos salir, cortamos el bucle del cliente
                if (miComando.equalsIgnoreCase("SALIR")) {
                    System.out.println("Cerrando sesión...");
                    break;
                }

                // 3. ESPERAR RESPUESTA
                // El programa se detiene aquí hasta que el Banco contesta
                respuestaBanco = br.readLine();

                // Medida de seguridad: Si el servidor se cae, devuelve null
                if (respuestaBanco == null) {
                    System.err.println("Error: Se perdió la conexión con el banco.");
                    break;
                }

                System.out.println("BANCO: " + respuestaBanco);
            }

        } catch (IOException e) {
            System.err.println("No se puede conectar con el banco (¿Está encendido el servidor?)");
        }
    }
}
