package EXAMEN_2ºEVALUACION;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ServerLibroHTTP {

    //  Estructura Libros (Título Autor)
    static class Libro {
        String titulo;
        String autor;

        public Libro(String titulo, String autor) {
            this.titulo = titulo;
            this.autor = autor;
        }
    }


    // Lista  de libros
    private static ArrayList<Libro> ListaLibros = new ArrayList<>();

    public static void main(String[] args) {
        // Cargamos los libros
        //2. Lista de libros:
        //El Quijote - Miguel de Cervantes
        //Cien años de soledad - Gabriel García Márquez
        //1984 - George Orwell
        //Pantaleón y las visitadoras – Mario Vargas Llosa
        //Dune – Frank Herbert
        ListaLibros.add(new Libro("El Quijote", "Miguel de Cervantes"));
        ListaLibros.add(new Libro("Cien años de soledad", "Gabriel Garcia Marquez"));
        ListaLibros.add(new Libro("1984", "George Orwell"));
        ListaLibros.add(new Libro("Pantaleon y las visitadoras", "Mario Vargas Llosa"));
        ListaLibros.add(new Libro("Dune", "Frank Herbert"));

        try {
            // a ) escucharemos en el puerto 8080
            ServerSocket servidor = new ServerSocket(8080);
            System.out.println("Servidor HTTP en http://localhost:8080");

            while (true) {
                // Multiclientes
                Socket cliente = servidor.accept();
                HiloHttp hilo = new HiloHttp(cliente);
                hilo.start();
            }
        } catch (IOException e) {
            System.err.println("Error no se ha podido iniciar el  servidor.");
        }
    }
    //b) Aceptará conexiones de múltiples clientes de forma concurrente, creando un hilo
    //independiente para cada conexión.


    // Hilo
    static class HiloHttp extends Thread {
        Socket socket;

        public HiloHttp(Socket socket) {
            this.socket = socket;

        }

        @Override
        public void run() {
            try {
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(socket.getOutputStream());

                // c) Leerá la primera línea de la petición HTTP (por ejemplo: GET /libros HTTP/1.1).
                String primeraLinea = entrada.readLine();
                if (primeraLinea == null) return;

                System.out.println("Peticion recibida: " + primeraLinea);


                while (entrada.readLine().length() > 0) {

                }

                // Separamos la linea:
                String[] partes = primeraLinea.split(" ");
                String metodo = partes[0];
                String ruta = partes[1];

                String htmlRespuesta = "";
                String codigoHttp = "200 OK";



                // d) olo aceptará el método GET. Para cualquier otro, devolverá una respuesta HTTP
                //con el código 405 Method Not Allowed.
                if (!metodo.equals("GET")) {
                    codigoHttp = "405 Method Not Allowed";
                    htmlRespuesta = "<html><body><h1>405 Metodo no permitido</h1></body></html>";
                }
                else {
                    // f) Se considerarán las siguientes rutas:
                    if (ruta.equals("/")) {
                        //Pagina principal /
                        //Pagina principal /
                        htmlRespuesta = """
                                <!DOCTYPE html> 
                                "<html lang='es'> 
                                "<head><title>Catálogo de libros</title></head> 
                                "<body> 
                                "<h1>Catálogo de libros</h1> 
                                "<p>Opciones disponibles:</p> 
                                "<ul>
                                "<li><a href='/libros'>Ver lista completa de libros</a></li> 
                                "<li><a href='/libros_total'>Número total de libros</a></li> 
                                "</ul> 
                                "</body></html>;
                                """;
                    }
                    else if (ruta.equals("/libros")) {
                        // Pagina que muestra la nota de libros  (ruta/libros)
                        htmlRespuesta = """ 
                                <!DOCTYPE html>" 
                                "<html lang='es'>" 
                                <head><meta charset='UTF-8'><title>Lista de libros</title></head>" 
                                <body> 
                                <h1>Lista de libros</h1>" 
                                <ul>
                                """;

                        // añadimos books
                        for (Libro l : ListaLibros) {
                            htmlRespuesta = htmlRespuesta + "<li>" + l.titulo + " - " + l.autor + "</li>";
                        }

                        htmlRespuesta = htmlRespuesta + "</ul>" +
                                "<p><a href='/'>Volver al inicio</a></p>" +
                                "</body></html>";
                    }
                    else if (ruta.equals("/libros_total")) {
                        // AQUI VAMOS A contar los libros Totales
                        htmlRespuesta = "<html><body>" +
                                "<h1>Total de libros</h1>" +
                                "<p>Hay un total de: " + ListaLibros.size() + " libros en el catalogo.</p>" +
                                "<p><a href='/'>Volver</a></p>" +
                                "</body></html>";
                    }
                    else {
                        // otra cosa sera error 404
                        codigoHttp = "404 Not Found";
                        htmlRespuesta = "<html><body><h1>404 Not Found</h1><p>Pagina no encontrada</p></body></html>";
                    }
                }


                salida.println("HTTP/1.1 " + codigoHttp);
                salida.println("Content-Type: text/html; charset=utf-8");
                salida.println("Content-Length: " + htmlRespuesta.getBytes().length);
                salida.println();
                salida.print(htmlRespuesta);

                salida.flush();
                socket.close();

            } catch (IOException e) {

            }
        }
    }
}





