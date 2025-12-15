package U3ComunicacionEnRed;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SimulacroServerBiblioteca {

    // 1. INVENTARIO COMPARTIDO
    // Guardamos "Título del Libro" -> "Número de Copias"
    // static: Para que sea el mismo inventario para todos los clientes.
    private static Map<String, Integer> biblioteca = new HashMap<>();

    // Iniciamos con algunos libros de prueba
    static {
        biblioteca.put("JAVA", 3);
        biblioteca.put("REDES", 1); // Solo 1, ideal para probar conflictos
        biblioteca.put("SISTEMAS", 5);
    }

    // 2. MÉTODO SINCRONIZADO (ZONA CRÍTICA)
    // Gestiona tanto DONAR (sumar) como COGER (restar) de forma segura.
    private static synchronized String gestionarLibros(String accion, String titulo) {
        titulo = titulo.toUpperCase(); // Normalizamos a mayúsculas

        // CASO A: ALGUIEN DONA UN LIBRO (SUMAR)
        if (accion.equals("DONAR")) {
            // .getOrDefault: Si existe dame su cantidad, si no, dame 0.
            int cantidadActual = biblioteca.getOrDefault(titulo, 0);
            biblioteca.put(titulo, cantidadActual + 1);
            return "Gracias por donar '" + titulo + "'. Total copias: " + (cantidadActual + 1);
        }

        // CASO B: ALGUIEN QUIERE LLEVARSE UN LIBRO (RESTAR)
        else if (accion.equals("COGER")) {
            // Verificamos si existe y si quedan copias (> 0)
            if (biblioteca.containsKey(titulo) && biblioteca.get(titulo) > 0) {
                int stock = biblioteca.get(titulo);
                biblioteca.put(titulo, stock - 1); // Restamos 1
                return "Préstamo realizado: '" + titulo + "'. Quedan: " + (stock - 1);
            } else {
                return "ERROR: El libro '" + titulo + "' no está disponible o no existe.";
            }
        }
        return "Comando interno error";
    }

    // Método de lectura (Sincronizado para ver datos reales)
    private static synchronized String verCatalogo() {
        if (biblioteca.isEmpty()) return "La biblioteca está vacía.";

        StringBuilder sb = new StringBuilder("--- CATÁLOGO ---\n");
        // Recorremos el mapa
        for (String key : biblioteca.keySet()) {
            sb.append(key).append(" -> ").append(biblioteca.get(key)).append(" copias\n");
        }
        return sb.toString();
    }

    // 3. HILO BIBLIOTECARIO (Atiende al cliente)
    static class HiloBibliotecario implements Runnable {
        private Socket socket;

        public HiloBibliotecario(Socket s) {
            this.socket = s;
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)) {

                pw.println("BIBLIOTECA ABIERTA. Comandos: VER | DONAR <titulo> | COGER <titulo> | SALIR");

                String linea;
                while ((linea = br.readLine()) != null) {

                    // Parseo: "COGER El Quijote" -> ["COGER", "El", "Quijote"]
                    // OJO: Un título puede tener espacios. Usamos el límite del split.
                    // split(" ", 2) divide solo en el PRIMER espacio.
                    // Parte 0: "COGER", Parte 1: "El Quijote"
                    String[] partes = linea.trim().split("\\s+", 2);
                    String comando = partes[0].toUpperCase();
                    String respuesta = "";

                    switch (comando) {
                        case "VER":
                            respuesta = verCatalogo();
                            break;

                        case "DONAR":
                        case "COGER":
                            if (partes.length > 1) {
                                String titulo = partes[1];
                                // Llamamos al método seguro
                                respuesta = gestionarLibros(comando, titulo);
                            } else {
                                respuesta = "Debes escribir el título. Ej: " + comando + " Java";
                            }
                            break;

                        case "SALIR":
                            return; // Adiós

                        default:
                            respuesta = "Comando no entendido.";
                    }
                    pw.println(respuesta);
                }
            } catch (IOException e) {
                System.err.println("Lector desconectado.");
            }
        }
    }

    // 4. MAIN
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(5000)) {
            System.out.println("BIBLIOTECA INICIADA EN PUERTO 5000");
            while (true) {
                Socket s = server.accept();
                new Thread(new HiloBibliotecario(s)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}