package U3ComunicacionEnRed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class U3P02EchoServer {

    // 1. FORMATEADOR DE FECHA
    // Define cómo queremos ver la hora: Día/Mes/Año Hora:Minuto:Segundo
    public static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    // Método auxiliar para obtener la hora actual formateada rápidamente
    public static String getFecha(){
        return sdf.format(System.currentTimeMillis());
    }

    public static void main(String[] args) {

        // 2. HILO SECUNDARIO (El "Latido" o Heartbeat)
        // Este hilo se crea para ejecutarse EN PARALELO al servidor principal.
        // Su única misión es imprimir "Servidor activo" cada 5 segundos.
        // Se usa una "Lambda" () -> {} para no tener que crear una clase aparte.
        Thread demonio = new Thread(() -> {
            while (true) {
                System.out.println(getFecha() + " Servidor activo"); // Usa nuestra fecha bonita
                try {
                    Thread.sleep(5000); // Se duerme 5000ms (5 segundos) antes de repetir
                }catch (InterruptedException e){
                    throw new RuntimeException(e);
                }
            }
        });

        // 3. INICIO DEL SERVIDOR
        try (ServerSocket server = new ServerSocket(1025)){

            // ¡IMPORTANTE! Aquí arrancamos el hilo secundario.
            // A partir de esta línea, verás mensajes en la consola cada 5s, pase lo que pase abajo.
            demonio.start();

            System.out.println(getFecha() + " Servidor escuchando en puerto 1025");

            // 4. ACEPTAR CLIENTE (Solo uno)
            // OJO: Este código NO tiene un bucle while(true) rodeando el accept().
            // Significa que acepta UN cliente, habla con él, y cuando ese cliente se va, el servidor se apaga.
            Socket socket = server.accept();

            System.out.println(getFecha() + " Recibido solicitud de comunicación");

            // 5. CANALES DE COMUNICACIÓN
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os, true); // true = autoFlush (envío inmediato)
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line = null;

            // 6. BUCLE DE CHARLA
            // Mientras el cliente siga conectado y enviando cosas...
            while ((line = br.readLine()) != null) {
                System.out.println("Recibido del cliente: " + line);

                // Lógica: Devolver lo mismo pero en minúsculas
                pw.println(line.toLowerCase());
            }

        } catch (IOException e) {
            System.err.println("Error al arrancar el servidor " + e.getMessage());
        }
        // Al salir del try, se cierra el ServerSocket.
    }
}