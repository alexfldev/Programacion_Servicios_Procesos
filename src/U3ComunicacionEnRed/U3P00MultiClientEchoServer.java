package U3ComunicacionEnRed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

// CLASE "TRABAJADORA" (HILO)
// Implementa Runnable para que pueda ejecutarse en paralelo.
// Esta clase se encarga de atender a UN solo cliente.
class GestorCliente implements Runnable {

    private Socket socket; // Aquí guardamos la "tubería" específica de este cliente

    // CONSTRUCTOR: Recibe el socket que el servidor aceptó en el main
    public GestorCliente(Socket socket) {
        this.socket = socket;
    }

    // MÉTODO RUN: Es lo que ejecutará el hilo. Todo el código de comunicación va aquí.
    @Override
    public void run() {
        // Obtenemos el nombre del hilo (ej: Thread-0) para identificarlo en la consola
        String nombre = "[" + Thread.currentThread().getName() + "]";
        System.out.println(nombre + " Cliente conectado:" + socket.getInetAddress() + ":" + socket.getPort());

        try {
            // 1. OBTENER CANALES DE COMUNICACIÓN
            // InputStream = Oído (recibir bytes)
            InputStream in = socket.getInputStream();
            // OutputStream = Boca (enviar bytes)
            OutputStream out = socket.getOutputStream();

            // 2. WRAPPERS (Decoradores) para facilitar la lectura/escritura
            // BufferedReader: Permite leer líneas enteras de texto.
            BufferedReader bf = new BufferedReader(new InputStreamReader(in));
            // PrintWriter: Permite enviar texto. 'true' activa el autoFlush (envío inmediato).
            PrintWriter pw = new PrintWriter(out, true);


            String line;
            // 3. BUCLE DE CONVERSACIÓN
            // bf.readLine() lee lo que manda el cliente.
            // Si devuelve null, significa que el cliente cerró la conexión.
            while ((line = bf.readLine()) != null) {
                System.out.println(nombre +" Recibido de cliente: " + line);

                // Aquí procesamos el mensaje (en este caso, pasarlo a minúsculas)
                pw.println(line.toLowerCase());
            }

        } catch (IOException e){
            System.err.println(nombre + " Error en la conexión del servidor:" + e.getMessage());
        }
        // Si salimos del while, es que el cliente se ha ido.
        System.out.println("Cliente desconectado:" + socket.getInetAddress() + ":" + socket.getPort());
    }
}

// CLASE PRINCIPAL DEL SERVIDOR (EL JEFE)
public class U3P00MultiClientEchoServer {
    public static void main(String[] args) {
        int puerto = 0;

        // Validación del puerto (usa tu clase auxiliar Validacion)
        try {
            puerto = Validacion.validarPuerto(args);
        } catch (Exception e){
            System.err.println("Error en el formato del puerto: " + e.getMessage());
            System.exit(1);
        }

        // INICIO DEL SERVIDOR
        // ServerSocket se encarga de esperar conexiones nuevas.
        try(ServerSocket servidor = new ServerSocket(puerto);){
            System.out.println("Servidor Iniciado; esperando connection con el puerto: " + puerto);

            // BUCLE INFINITO: El servidor principal nunca duerme, siempre espera nuevos clientes.
            while(true) {

                // 1. ESPERA ACTIVA (Bloqueante)
                // El programa se detiene aquí hasta que alguien intenta conectarse.
                // Cuando alguien entra, crea un objeto 'socket' exclusivo para él.
                Socket socket = servidor.accept();

                // 2. PREPARAR EL HILO
                // Creamos una instancia de nuestra clase trabajadora pasándole el socket.
                GestorCliente gestor = new GestorCliente(socket);
                // Creamos el Hilo (Thread) asignándole esa tarea.
                Thread t = new Thread(gestor);

                // 3. LANZAR EL HILO
                // ¡IMPORTANTE! Usamos .start(). Esto arranca el proceso en paralelo
                // y permite que el bucle while continúe INMEDIATAMENTE para esperar al siguiente cliente.
                t.start();
            }

        } catch (IOException e){
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}