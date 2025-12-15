package examenpryueba2;



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServidorNotas {

    // 1. Recurso Compartido
    private static Map<String, Double> notas = new HashMap<>();

    // 2. Métodos Sincronizados (Lógica)
    private static synchronized String gestionarComando(String comando, String[] partes) {
        // PONER <nombre> <nota>
        if (comando.equals("PONER")) {
            if (partes.length < 3) return "Error: Uso PONER <nombre> <nota>";
            try {
                String nombre = partes[1].toUpperCase();
                double nota = Double.parseDouble(partes[2]);
                notas.put(nombre, nota);
                return "Nota guardada para " + nombre;
            } catch (NumberFormatException e) {
                return "Error: La nota debe ser un número.";
            }
        }
        // CONSULTAR <nombre>
        else if (comando.equals("CONSULTAR")) {
            if (partes.length < 2) return "Error: Uso CONSULTAR <nombre>";
            String nombre = partes[1].toUpperCase();
            if (notas.containsKey(nombre)) {
                return "La nota de " + nombre + " es: " + notas.get(nombre);
            } else {
                return "Error: Alumno no encontrado.";
            }
        }
        // MEDIA
        else if (comando.equals("MEDIA")) {
            if (notas.isEmpty()) return "No hay alumnos registrados.";
            double suma = 0;
            for (double n : notas.values()) {
                suma += n;
            }
            return "Nota media de la clase: " + (suma / notas.size());
        }

        return "Comando desconocido.";
    }

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(6000)) {
            System.out.println("Servidor de Notas en puerto 6000");
            while (true) {
                Socket s = server.accept();
                new Thread(() -> atenderCliente(s)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void atenderCliente(Socket socket) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)) {

            pw.println("BIENVENIDO. Comandos: PONER | CONSULTAR | MEDIA | SALIR");

            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.trim().split("\\s+");
                String comando = partes[0].toUpperCase();

                if (comando.equals("SALIR")) break;

                // Llamamos a la lógica sincronizada
                String respuesta = gestionarComando(comando, partes);
                pw.println(respuesta);
            }
        } catch (IOException e) {
            System.err.println("Cliente desconectado");
        }
    }
}