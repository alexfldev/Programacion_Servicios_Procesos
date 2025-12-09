package U4ServiciosEnRed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class U4P02SelfHealingServer {

    // --- BLOQUE 1: EL BUCLE DE VIDA ETERNA (MAIN) ---
    public static void main(String[] args) {
        // Bucle infinito: Queremos que el servidor intente revivir por siempre
        while (true) {
            try {
                // Intentamos arrancar el servidor.
                // Si todo va bien, se queda aquí dentro ejecutándose.
                arrancarServidor();

            } catch (Exception e) {
                // Si llegamos aquí, es que arrancarServidor() ha fallado (se rompió).
                // Capturamos la excepción lanzada desde abajo (RuntimeException).
                System.out.println("⚠️ CRASH DETECTADO: " + e.getMessage());
                System.out.println("🔄 Reiniciando sistema en 2 segundos...");

                // Esperamos 2 segundos antes de volver a intentar (para no saturar la CPU)
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
                // Al terminar el catch, el 'while(true)' vuelve a empezar y llama a arrancarServidor() de nuevo.
            }
        }
    }

    // --- BLOQUE 2: LA LÓGICA DEL SERVIDOR ---
    private static void arrancarServidor() throws IOException {

        // try-with-resources: Abre el puerto 2777.
        // Si hay error o se cierra el bloque, libera el puerto automáticamente.
        try (ServerSocket socket = new ServerSocket(2777)) {

            System.out.println("✅ Servidor Iniciado y escuchando en puerto 2777");

            // --- BLOQUE 3: EL SABOTEADOR (THREAD KILLER) ---
            // Este hilo simula un error fatal o un cierre inesperado tras 5 segundos.
            Thread killer = new Thread(() -> {
                System.out.println("   [Killer] 💣 Saboteador iniciado. Cuenta atrás: 5s...");
                try {
                    // Espera 5 segundos
                    Thread.sleep(5000);
                    System.out.println("   [Killer] 💥 BOOM! Cerrando el socket a la fuerza...");

                    // AL CERRAR EL SOCKET AQUÍ, el método socket.accept() del hilo principal fallará.
                    socket.close();

                } catch (InterruptedException | IOException e) {
                    // Ignoramos errores del killer
                }
            });

            // ¡IMPORTANTE! Arrancamos el contador de la bomba
            killer.start();

            // --- BLOQUE 4: BUCLE DE ATENCIÓN A CLIENTES ---
            while (true) {
                // socket.accept() bloquea y espera a un cliente.
                // IMPORTANTE: Si el 'killer' cierra el socket (línea 56), esta línea lanza una IOException inmediatamente.
                Socket sc = socket.accept();

                // Si llega un cliente real (antes de los 5 seg), creamos un hilo para atenderlo
               new  Thread(() -> {
                    try (BufferedReader bf = new BufferedReader(new InputStreamReader(sc.getInputStream()));
                         PrintWriter pr = new PrintWriter(sc.getOutputStream(), true)) {

                        String linea;
                        // Leemos lo que envía el cliente
                        while ((linea = bf.readLine()) != null) {
                            System.out.println("📨 Cliente dice: " + linea);
                            // Le respondemos en minúsculas
                            pr.println(linea.toLowerCase());
                        }

                    } catch (IOException e) {
                        System.err.println("❌ Error comunicando con un cliente individual: " + e.getMessage());
                    }
                }).start();
            }

        } catch (IOException e) {
            // --- BLOQUE 5: GESTIÓN DE LA CAÍDA ---
            // El código entra aquí cuando el 'killer' ejecuta socket.close().
            // El 'while' del servidor se rompe.

            // Lanzamos una RuntimeException hacia arriba (hacia el main)
            // para avisar de que el servidor ha muerto y necesita reinicio.
            throw new RuntimeException("El socket principal ha sido cerrado (Sabotaje exitoso): " + e.getMessage());
        }
    }
}