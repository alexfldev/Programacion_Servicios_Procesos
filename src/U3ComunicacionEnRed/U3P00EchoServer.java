package U3ComunicacionEnRed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class U3P00EchoServer {
    public static void main(String[] args) {
        int puerto = 0;

        // PASO 1: GESTIÓN DEL PUERTO
        // Cuidado: Tu código usa una clase extra "Validacion".
        // Si en el examen no te dan esa clase, cambia todo este bloque try/catch por: int puerto = 4000;
        try {
            puerto = Validacion.validarPuerto(args);
        }catch (Exception e){
            System.err.println("Error en el formato del puerto: " + e.getMessage());
            System.exit(1); // Cierra el programa si el puerto está mal
        }

        // PASO 2: INICIAR EL SERVIDOR (ServerSocket)
        // 'ServerSocket' es diferente a 'Socket'. Este solo escucha, no envía datos.
        try(ServerSocket servidor = new ServerSocket(puerto);){

            System.out.println("Servidor Iniciado; esperando conexión en el puerto: " + puerto);

            // PASO 3: ACEPTAR CLIENTE (CRÍTICO)
            // .accept() es BLOQUEANTE. El programa se congela en esta línea.
            // Espera eternamente hasta que un cliente intente entrar.
            // Cuando entra, crea un objeto 'socket' normal para hablar EXCLUSIVAMENTE con ese cliente.
            Socket socket = servidor.accept();

            System.out.println("Cliente conectado:" +  socket.getInetAddress() + ":" + socket.getPort());

            // PASO 4: PREPARAR TUBERÍAS
            // Igual que en el cliente. Obtenemos los canales del socket que acabamos de aceptar.
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            BufferedReader bf = new BufferedReader(new InputStreamReader(in));
            PrintWriter pw = new PrintWriter(out, true); // Recuerda el 'true' para el autoFlush

            // PASO 5: BUCLE DE ESCUCHA
            String line;

            // Leemos lo que manda el cliente.
            // IMPORTANTE: bf.readLine() devolverá 'null' si el cliente cierra la conexión o desaparece.
            while ((line = bf.readLine()) != null) {

                System.out.println("Recibido de cliente: " + line);

                // PASO 6: PROCESAR Y RESPONDER
                // Aquí está la lógica del examen. Ahora mismo lo pasa a minúsculas (.toLowerCase).
                // Si te piden una calculadora, aquí harías las sumas.
                pw.println(line.toLowerCase());
            }

            // Cuando el while termina (line es null), se cierra el try-with-resources y desconecta todo.

        }catch (IOException e){
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}