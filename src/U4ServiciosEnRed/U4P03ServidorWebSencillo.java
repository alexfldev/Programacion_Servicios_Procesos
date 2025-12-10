package U4ServiciosEnRed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class U4P03ServidorWebSencillo {



        private static final String HTML_1 = """
            <html>
                <head>
                    <title>Servidor web simple</title>
                </head>
                <body>
                    <h1>¡Hola mundo!</h1>
                    <p>Esto es un servidor web simple.</p>
                    <p>Eres el visitante Nº: """;
        private static final String HTML_2 = """
                </p>
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
                    contador.incrementAndGet();
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
                String linea;
                linea = br.readLine();
                System.out.println(linea);

                System.out.println("Devolviendo respuesta HTML: ");
                StringBuffer respuesta = new StringBuffer();
                respuesta.append(HTML_1).append(contador.get()).append(HTML_2);
                pw.println("HTTP/1.1 200 OK");
                pw.println("Content-Type: text/html;charset=UTF-8");
                pw.println("Content-Length: " + respuesta.toString().getBytes().length);
                pw.println();
                pw.println(respuesta.toString());

                pw.flush();
            } catch (IOException e) {
                System.err.println("Error en el servidor " + e.getMessage());
            }
        }
    }