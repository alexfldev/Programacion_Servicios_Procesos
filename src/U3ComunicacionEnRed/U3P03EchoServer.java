package U3ComunicacionEnRed;



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class Validacion {
    public static int validarPuerto(String[] args) {
        // 1. Verificamos que hay argumentos
        if (args.length != 1) {
            throw new IllegalArgumentException("Uso: java U3P03EchoServer <puerto>");
        }

        int puerto;
        try {
            // 2. Intentamos convertir a entero
            puerto = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El puerto debe ser un número entero.");
        }

        // 3. Validamos rango de puertos (no reservados)
        if (puerto < 1024 || puerto > 65535) {
            throw new IllegalArgumentException("El puerto debe estar entre 1024 y 65535.");
        }

        return puerto;
    }
}

public class U3P03EchoServer {
    public static void main(String[] args) {
        int puerto = 0;

        // Validación de entrada
        try {
            puerto = Validacion.validarPuerto(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Error en los argumentos: " + e.getMessage());
            System.exit(1);
        }

        System.out.println("--- Servidor Echo Iniciando ---");

        // Arranque del servidor (Try-with-resources para el ServerSocket)
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Esperando conexiones en el puerto: " + puerto);

            // Aceptamos la conexión (Try-with-resources para el Socket del cliente y streams)
            // Esto asegura que el socket del cliente se cierre al terminar
            try (Socket socket = servidor.accept();
                 BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)) {

                System.out.println("Cliente conectado desde: " + socket.getInetAddress() + ":" + socket.getPort());



                String entrada;
                // Bucle de lectura
                while ((entrada = bf.readLine()) != null) {
                    System.out.println("Recibido: " + entrada);
                    // Enviamos respuesta (Echo en minúsculas)
                    pw.println(entrada.toLowerCase());
                }

                System.out.println("Cliente desconectado.");
            }

        } catch (IOException e) {
            System.err.println("Error de conexión o I/O: " + e.getMessage());
        }
    }
}