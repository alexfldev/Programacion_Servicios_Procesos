package U3ComunicacionEnRed;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SimulacroServerEntradas {

    // ESTADO COMPARTIDO
    private static int entradasDisponibles = 100;

    // --- ZONA CRÍTICA (La clave del examen) ---
    // Usamos 'synchronized' para que SOLO UN HILO pueda entrar aquí a la vez.
    // Si no pones esto, venderás entradas de más.
    private static synchronized String gestionarCompra(int cantidad) {
        if (cantidad <= 0) {
            return "Error: Cantidad inválida.";
        }

        if (cantidad <= entradasDisponibles) {
            entradasDisponibles -= cantidad; // Restamos
            return "Reserva OK. Entradas compradas: " + cantidad + ". Quedan: " + entradasDisponibles;
        } else {
            return "ERROR: No hay suficientes. Solo quedan: " + entradasDisponibles;
        }
    }

    // Método solo de lectura (no necesita ser synchronized estricto, pero es recomendable si hay mucha escritura)
    private static synchronized int verEntradas() {
        return entradasDisponibles;
    }

    // --- GESTOR DE CLIENTES (HILO) ---
    static class GestorVenta implements Runnable {
        private Socket socket;

        public GestorVenta(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)
            ) {
                pw.println("BIENVENIDO AL SISTEMA DE ENTRADAS. Comandos: VER | COMPRAR <n> | SALIR");

                String linea;
                while ((linea = br.readLine()) != null) {
                    // Separamos comando y argumento (ej: "COMPRAR 5")
                    String[] partes = linea.trim().split("\\s+");
                    String comando = partes[0].toUpperCase();
                    String respuesta = "";

                    switch (comando) {
                        case "VER":
                            respuesta = "Entradas disponibles: " + verEntradas();
                            break;

                        case "COMPRAR":
                            if (partes.length > 1) {
                                try {
                                    int cantidad = Integer.parseInt(partes[1]);
                                    // Llamamos al método seguro
                                    respuesta = gestionarCompra(cantidad);
                                } catch (NumberFormatException e) {
                                    respuesta = "Error: Introduce un número válido.";
                                }
                            } else {
                                respuesta = "Uso: COMPRAR <cantidad>";
                            }
                            break;

                        case "SALIR":
                            pw.println("Adiós.");
                            return; // Salimos del run() y cerramos

                        default:
                            respuesta = "Comando no reconocido.";
                    }
                    pw.println(respuesta);
                }
            } catch (IOException e) {
                System.err.println("Error cliente: " + e.getMessage());
            }
        }
    }

    // --- MAIN ---
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(5000)) {
            System.out.println("TAQUILLA ABIERTA - 100 Entradas - Puerto 5000");
            while (true) {
                Socket s = server.accept();
                new Thread(new GestorVenta(s)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
