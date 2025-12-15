package examenpryueba2;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteNotas {

    public static void main(String[] args) {
        // Datos de conexión (Deben coincidir con el Servidor)
        String host = "localhost";
        int port = 6000;

        System.out.println("Conectando al Sistema de Notas...");

        try (Socket socket = new Socket(host, port);
             // IMPORTANTE EXAMEN: 'true' para autoFlush (enviar datos al instante)
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner sc = new Scanner(System.in)) {

            // 1. LEER MENSAJE DE BIENVENIDA
            // El servidor envía "BIENVENIDO..." nada más conectar. Hay que leerlo primero.
            System.out.println("Servidor: " + br.readLine());

            String comando;
            String respuesta;

            // 2. BUCLE DE COMANDOS
            while (true) {
                // A. Leemos del teclado
                System.out.print("Comando (PONER/CONSULTAR/MEDIA/SALIR) > ");
                comando = sc.nextLine();

                // B. Enviamos al servidor
                pw.println(comando);

                // C. Si es SALIR, cortamos el bucle del cliente
                if (comando.trim().equalsIgnoreCase("SALIR")) {
                    System.out.println("Cerrando sesión...");
                    break;
                }

                // D. Leemos la respuesta del servidor
                respuesta = br.readLine();
                System.out.println("Respuesta: " + respuesta);
            }

        } catch (IOException e) {
            System.err.println("Error: No se puede conectar al servidor (Revisa si está encendido).");
        }
    }
}