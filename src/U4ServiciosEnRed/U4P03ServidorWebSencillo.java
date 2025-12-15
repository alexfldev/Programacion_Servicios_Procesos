package U4ServiciosEnRed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class U4P03ServidorWebSencillo {

    private static final String HTML = """
            <html>
                <head>
                    <title>Servidor web simple</title>
                </head>
                <body>
                    <h1>¡Hola mundo!</h1>
                    <p>Esto es un servidor web simple.</p>
                    <p>Eres el visitante Nº: %d</p>
                </body>
            </html>
            """;

    private static final int PUERTO = 8080;
    private static AtomicInteger contador = new AtomicInteger();

    public static void main(String[] args) {
        try (ServerSocket svs = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);

            while (true) {
                Socket socket = svs.accept();
                Thread t = new Thread(() -> atenderSolicitud(socket));
                t.start();
            }

        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    private static void atenderSolicitud(Socket socket) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter pw = new PrintWriter(socket.getOutputStream())) {

            String linea = br.readLine();

            if (linea != null && !linea.isBlank()) {

                System.out.println(linea);

                // Corregido: separa por espacios correctamente
                String[] partes = linea.split("\\s+");

                String metodo = partes[0];
                String ruta = partes.length > 1 ? partes[1].trim().toLowerCase() : "/";

                // Leer cabeceras
                while ((linea = br.readLine()) != null && !linea.isBlank()) {
                    System.out.println(linea);
                }

                if (metodo.equalsIgnoreCase("get")) {

                    // No contar favicon
                    if (!ruta.contains("favicon")) {
                        contador.incrementAndGet();
                    }

                    System.out.println("Devolviendo respuesta HTML: ");
                    String respuesta = String.format(HTML, contador.get());
                    pw.println("HTTP/1.1 200 OK");
                    pw.println("Content-Type: text/html;charset=UTF-8");
                    pw.println("Content-Length: " + respuesta.getBytes().length);
                    pw.println();
                    pw.print(respuesta);

                    pw.flush();

                } else {
                    // Respuesta para métodos no permitidos
                    pw.println("HTTP/1.1 405 Method Not Allowed");
                    pw.println();
                    pw.flush();
                }
            }

        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}
