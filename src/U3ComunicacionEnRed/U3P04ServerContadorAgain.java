package U3ComunicacionEnRed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class U3P04ServerContadorAgain {

    // 1. EL TESORO COMPARTIDO (CLAVE DEL EXAMEN)
    // - static: Solo existe UNA variable 'contador' para TODOS los clientes.
    // - AtomicInteger: Es "Thread-Safe" (seguro para hilos).
    //   Si usaras 'int' normal, dos clientes sumando a la vez podrían fallar.
    private static AtomicInteger contador = new AtomicInteger(0);

    // 2. EL TRABAJADOR (HILO)
    // Esta clase atiende a UN cliente individualmente.
    static class GestorClientesContador implements Runnable{
        private Socket socket;
        private String cliente;

        // Mensajes predefinidos (buena práctica para no repetir texto)
        private static final String CONTADOR_ACTUALIZADO = "Contador actualizado. ";
        private static final String VALOR_CONTADOR = "Valor: ";

        public GestorClientesContador(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            cliente = socket.getInetAddress() + ":" + socket.getPort();
            System.out.println("[" + Thread.currentThread().getName() + "] IP: " + cliente);

            try(
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)){

                String comando;
                String respuesta;
                boolean continuar = true;

                // 3. BUCLE DE COMANDOS
                // Leemos lo que manda el cliente mientras quiera seguir (continuar=true)
                while (continuar && (comando = br.readLine()) != null){

                    // 4. CEREBRO DEL PROGRAMA (Switch Moderno)
                    // Este switch devuelve un valor directamente a la variable 'respuesta'.
                    respuesta = switch (comando.trim().toLowerCase()){

                        // Si pide "inc", llamamos al método seguro y devolvemos texto
                        case "inc" -> CONTADOR_ACTUALIZADO + VALOR_CONTADOR + incrementarContador();

                        // Si pide "dec"
                        case "dec" -> CONTADOR_ACTUALIZADO + VALOR_CONTADOR + decrementarContador();

                        // Si pide "get" (solo mirar)
                        case "get" -> VALOR_CONTADOR + getContador();

                        // Si pide "bye"
                        case "bye" -> {
                            continuar = false; // Cambiamos la bandera para salir del while
                            yield "Bye";       // 'yield' es como el 'return' dentro de un switch
                        }

                        // Si escribe cualquier otra cosa
                        default -> "Comando no válido";
                    };

                    // Enviamos la respuesta calculada al cliente
                    pw.println(respuesta);
                }

            }catch (IOException e){
                System.err.println("Error en la conexión: " + e.getMessage());
            }
            System.out.println("Conexión finalizada de " + cliente);
        }
    }

    // --- MÉTODOS SEGUROS (THREAD-SAFE) ---
    // Usamos los métodos propios de AtomicInteger (.get, .incrementAndGet, etc.)

    public static int getContador(){
        return contador.get();
    }

    public static int incrementarContador(){
        return contador.incrementAndGet(); // Suma 1 y devuelve el nuevo valor atómicamente
    }

    public static int decrementarContador(){
        return contador.decrementAndGet(); // Resta 1 y devuelve el nuevo valor atómicamente
    }

    // --- MAIN ---
    public static void main(String[] args) {

        try(ServerSocket serverSocket = new ServerSocket(4000)){
            System.out.println("Servidor de Contador escuchando en puerto: 4000");

            // Bucle infinito aceptando clientes
            while (true) {
                Socket socket = serverSocket.accept();

                // Creamos un hilo nuevo para cada persona que entra
                Thread thread = new Thread(new GestorClientesContador(socket));
                thread.start();
            }

        }catch (IOException e){
            System.err.println("Error: " + e.getMessage());
        }
    }
}