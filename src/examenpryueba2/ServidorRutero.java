package examenpryueba2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime; // Importante para la fecha
import java.util.concurrent.atomic.AtomicInteger;

public class ServidorRutero {

    // Plantilla HTML genérica (usamos %s para inyectar el título y el contenido)
    private static final String PLANTILLA_HTML = """
            <html>
                <head><title>%s</title></head>
                <body>
                    <h1>Servidor Examen</h1>
                    <div>%s</div>
                    <br>
                    <a href='/'>Volver al inicio</a>
                </body>
            </html>
            """;

    private static final int PUERTO = 8080;
    private static AtomicInteger contador = new AtomicInteger();

    public static void main(String[] args) {
        try (ServerSocket svs = new ServerSocket(PUERTO)) {
            System.out.println("Servidor Rutero escuchando en el puerto " + PUERTO);

            while (true) {
                Socket socket = svs.accept();
                new Thread(() -> atenderSolicitud(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void atenderSolicitud(Socket socket) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter pw = new PrintWriter(socket.getOutputStream())) {

            String linea = br.readLine();

            if (linea != null && !linea.isBlank()) {
                System.out.println("Petición: " + linea);

                String[] partes = linea.split("\\s+");
                String metodo = partes[0];
                // Truco: Usamos partes[1] para saber qué quiere el usuario (/fecha, /inicio, etc)
                String ruta = partes.length > 1 ? partes[1].trim().toLowerCase() : "/";

                // Consumir cabeceras (obligatorio para que no se quede colgado)
                while ((linea = br.readLine()) != null && !linea.isBlank());

                if (metodo.equalsIgnoreCase("get")) {

                    // Ignoramos el favicon para no ensuciar logs ni contadores
                    if (ruta.contains("favicon.ico")) return;

                    String contenidoCuerpo = "";
                    String codigoEstado = "200 OK"; // Por defecto todo va bien
                    String titulo = "Inicio";

                    // --- ZONA DE EXAMEN: EL SWITCH ---
                    // Aquí decidimos qué mostrar según la ruta
                    switch (ruta) {
                        case "/":
                        case "/inicio":
                            contador.incrementAndGet();
                            titulo = "Bienvenida";
                            contenidoCuerpo = "<h2>¡Hola Mundo!</h2><p>Eres el visitante número: " + contador.get() + "</p>";
                            break;

                        case "/fecha":
                            titulo = "Fecha y Hora";
                            // LocalDateTime.now() da la fecha actual
                            contenidoCuerpo = "<h2>Reloj del Sistema</h2><p>La fecha es: " + LocalDateTime.now() + "</p>";
                            break;

                        default:
                            // SI LA RUTA NO EXISTE -> ERROR 404
                            codigoEstado = "404 Not Found";
                            titulo = "Error 404";
                            contenidoCuerpo = "<h2 style='color:red'>Error 404</h2><p>La página que buscas no existe.</p>";
                            break;
                    }

                    // --- GENERAR RESPUESTA FINAL ---
                    // Rellenamos la plantilla HTML con el título y el contenido que hemos decidido arriba
                    String htmlFinal = String.format(PLANTILLA_HTML, titulo, contenidoCuerpo);

                    // Escribimos las cabeceras HTTP
                    pw.println("HTTP/1.1 " + codigoEstado); // Aquí va el 200 o el 404
                    pw.println("Content-Type: text/html;charset=UTF-8");
                    pw.println("Content-Length: " + htmlFinal.getBytes().length);
                    pw.println(); // Salto de línea VITAL
                    pw.print(htmlFinal); // El HTML
                    pw.flush();

                } else {
                    pw.println("HTTP/1.1 405 Method Not Allowed");
                    pw.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}