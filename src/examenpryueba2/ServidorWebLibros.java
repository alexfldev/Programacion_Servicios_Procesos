package examenpryueba2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorWebLibros {

    // HTML usando Text Blocks (Java 15+)
    private static final String HTML_CONTENT = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <title>Biblioteca DAM</title>
                <style>
                    body { font-family: 'Segoe UI', sans-serif; background-color: #f4f4f9; color: #333; text-align: center; padding: 50px; }
                    .card { background: white; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); display: inline-block; padding: 20px; max-width: 400px; }
                    h1 { color: #2c3e50; }
                    .book-title { color: #e67e22; font-size: 1.5em; font-weight: bold; }
                    .footer { margin-top: 20px; font-size: 0.8em; color: #777; }
                </style>
            </head>
            <body>
                <div class="card">
                    <h1>📖 Libro del Día</h1>
                    <p>La recomendación para hoy es:</p>
                    <p class="book-title">"El Código Da Vinci"</p>
                    <p>Un thriller apasionante sobre misterios ocultos.</p>
                </div>
                <div class="footer">Servidor Java de Esteban - DAM</div>
            </body>
            </html>
            """;

    public static void main(String[] args) {
        int port = 8080;
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("🌍 Servidor Web escuchando en http://localhost:" + port);

            while (true) {
                Socket socket = server.accept();
                // Atendemos en un hilo para no bloquear
                new Thread(() -> manejarPeticionWeb(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void manejarPeticionWeb(Socket socket) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter pw = new PrintWriter(socket.getOutputStream())) {

            // Leer la petición del navegador (importante aunque no la usemos para limpiar el stream)
            String linea = br.readLine();
            System.out.println("Petición recibida: " + linea);
            
            // Consumir el resto de cabeceras hasta la línea en blanco
            while (linea != null && !linea.isEmpty()) {
                linea = br.readLine();
            }

            // --- RESPUESTA HTTP ---
            // 1. Línea de estado
            pw.println("HTTP/1.1 200 OK");
            // 2. Cabeceras
            pw.println("Content-Type: text/html; charset=UTF-8");
            pw.println("Content-Length: " + HTML_CONTENT.getBytes().length);
            pw.println("Connection: close");
            // 3. Línea en blanco obligatoria
            pw.println();
            // 4. Cuerpo (HTML)
            pw.println(HTML_CONTENT);
            
            pw.flush();

        } catch (IOException e) {
            System.err.println("Error atendiendo cliente web: " + e.getMessage());
        }
    }
}