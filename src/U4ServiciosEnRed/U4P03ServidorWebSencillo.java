package U4ServiciosEnRed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class U4P03ServidorWebSencillo {

    // Contenido HTML que enviaremos al navegador
    private static final String HTML = """ 
            <html>
                <head>
                    <title>Servidor Web Sencillo</title>
                </head>
                <body>
                    <h1>Hola Mundo</h1>
                    <p>Parrafo</p>
                </body>
            </html>
            """;

    private static final int Puerto = 8080;

    public static void main(String[] args) {

        // El bloque try debe estar DENTRO del main
        try (ServerSocket socket = new ServerSocket(Puerto)) {
            System.out.println("Escuchando en el puerto " + Puerto);

            while (true) {
                // ERROR CORREGIDO: Es socket.accept() (la variable), no Socket.accept() (la clase)
                Socket sc = socket.accept();

                // Creamos el hilo para atender al cliente
                Thread t = new Thread(() -> atenderSolicitud(sc));
                t.start(); // FALTABA ESTO: Hay que iniciar el hilo
            }

        } catch (IOException e) {
            System.out.println("Error en el puerto " + Puerto);
            e.printStackTrace();
        }
    }

    // Este método maneja la conexión con el navegador
    private static void atenderSolicitud(Socket sc) {
        try ( BufferedReader bf = new BufferedReader(new InputStreamReader(sc.getInputStream()));
                PrintWriter pw = new PrintWriter(sc.getOutputStream())) {

            // 1. Escribimos las cabeceras HTTP obligatorias
            pw.println("HTTP/1.1 200 OK");
            pw.println("Content-Type: text/html; charset=UTF-8");
            pw.println("Content-Length: " + HTML.getBytes().length);
            pw.println(); // Importante: Línea en blanco separadora
            pw.flush();

            // 2. Escribimos el contenido HTML
            pw.println(HTML);

            // Nota: En un servidor web simple, no necesitamos leer (BufferedReader)
            // lo que envía el navegador, solo necesitamos responderle con el HTML.

        } catch (IOException e) {
            System.err.println("Error al atender cliente: " + e.getMessage());
        } finally {
            try {
                // Importante: Cerrar el socket del cliente tras enviar la web
                sc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}