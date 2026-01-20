package U3ComunicacionEnRed;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
public class TiendaHTML {




    public class ServerTiendaHTTP {

        // 1. Estructura Producto (Nombre, Precio)
        // Cambio respecto al original: Usamos un double para el precio.
        static class Producto {
            String nombre;
            double precio;

            public Producto(String nombre, double precio) {
                this.nombre = nombre;
                this.precio = precio;
            }
        }

        // Lista de productos
        private static ArrayList<Producto> ListaProductos = new ArrayList<>();

        public static void main(String[] args) {
            // 2. Cargamos el inventario
            ListaProductos.add(new Producto("Portátil Gaming", 1200.50));
            ListaProductos.add(new Producto("Ratón Óptico", 25.00));
            ListaProductos.add(new Producto("Teclado Mecánico", 89.99));
            ListaProductos.add(new Producto("Monitor 24 pulgadas", 150.00));
            ListaProductos.add(new Producto("Disco SSD 1TB", 65.50));

            try {
                // a) Escuchamos en el puerto 8081 (cambiado para no chocar con el otro)
                ServerSocket servidor = new ServerSocket(8081);
                System.out.println("Servidor Tienda HTTP arrancado en http://localhost:8081");

                while (true) {
                    // b) Aceptamos conexiones concurrentes
                    Socket cliente = servidor.accept();
                    HiloDespachador hilo = new HiloDespachador(cliente);
                    hilo.start();
                }
            } catch (IOException e) {
                System.err.println("Error al iniciar el servidor.");
            }
        }

        // Clase Hilo para manejar cada petición
        static class HiloDespachador extends Thread {
            Socket socket;

            public HiloDespachador(Socket socket) {
                this.socket = socket;
            }

            @Override
            public void run() {
                try {
                    BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter salida = new PrintWriter(socket.getOutputStream());

                    // c) Leer la primera línea de la petición (GET /ruta HTTP/1.1)
                    String primeraLinea = entrada.readLine();
                    if (primeraLinea == null) return;

                    System.out.println("Cliente conectado solicitando: " + primeraLinea);

                    // Consumir el resto de cabeceras HTTP hasta la línea vacía
                    while (entrada.readLine().length() > 0) {
                        // No hacemos nada con las cabeceras, solo las leemos para limpiar el buffer
                    }

                    // Separamos la línea para obtener método y ruta
                    String[] partes = primeraLinea.split(" ");
                    String metodo = partes[0];
                    String ruta = partes[1];

                    String htmlRespuesta = "";
                    String codigoHttp = "200 OK";

                    // d) Solo aceptamos GET
                    if (!metodo.equals("GET")) {
                        codigoHttp = "405 Method Not Allowed";
                        htmlRespuesta = "<html><body><h1>Error 405: Método no permitido</h1></body></html>";
                    } else {
                        // f) Rutas disponibles
                        if (ruta.equals("/")) {
                            // Página Principal
                            htmlRespuesta = """
                                <!DOCTYPE html>
                                <html lang='es'>
                                <head><title>Tienda Informática</title></head>
                                <body>
                                    <h1>Bienvenido a PC-Componentes Fake</h1>
                                    <p>Selecciona una opción:</p>
                                    <ul>
                                        <li><a href='/productos'>Ver inventario de productos</a></li>
                                        <li><a href='/valor_total'>Ver valor total del almacén</a></li>
                                    </ul>
                                </body>
                                </html>
                                """;

                        } else if (ruta.equals("/productos")) {
                            // Listar productos
                            htmlRespuesta = """
                                <!DOCTYPE html>
                                <html lang='es'>
                                <head><meta charset='UTF-8'><title>Inventario</title></head>
                                <body>
                                    <h1>Listado de Componentes</h1>
                                    <table border='1'>
                                        <tr><th>Producto</th><th>Precio</th></tr>
                                """;

                            // Bucle para añadir filas a la tabla
                            for (Producto p : ListaProductos) {
                                htmlRespuesta += "<tr><td>" + p.nombre + "</td><td>" + p.precio + " €</td></tr>";
                            }

                            htmlRespuesta += """
                                    </table>
                                    <p><a href='/'>Volver al inicio</a></p>
                                </body>
                                </html>
                                """;

                        } else if (ruta.equals("/valor_total")) {
                            // Lógica extra: Calcular la suma de precios
                            double total = 0;
                            for (Producto p : ListaProductos) {
                                total += p.precio;
                            }

                            htmlRespuesta = "<html><body>" +
                                    "<h1>Estadísticas del Almacén</h1>" +
                                    "<p>Items en inventario: " + ListaProductos.size() + "</p>" +
                                    "<p><strong>Valor total acumulado: " + total + " €</strong></p>" +
                                    "<p><a href='/'>Volver</a></p>" +
                                    "</body></html>";

                        } else {
                            // Error 404
                            codigoHttp = "404 Not Found";
                            htmlRespuesta = "<html><body><h1>404 Recurso no encontrado</h1><p>La ruta no existe.</p></body></html>";
                        }
                    }

                    // Escribir cabeceras y respuesta
                    salida.println("HTTP/1.1 " + codigoHttp);
                    salida.println("Content-Type: text/html; charset=utf-8");
                    salida.println("Content-Length: " + htmlRespuesta.getBytes().length);
                    salida.println(); // Línea vacía obligatoria entre headers y body
                    salida.print(htmlRespuesta);

                    salida.flush();
                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

