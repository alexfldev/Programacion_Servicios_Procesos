package examenpryueba2;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerBanco {

    // 1. EL DINERO COMPARTIDO (RECURSO CRÍTICO)
    // - static: Es una variable DE CLASE. Todos los hilos comparten la misma variable.
    // - Si no fuera static, cada cliente tendría su propio saldo de 1000€ (error grave).
    private static int saldoActual = 1000;

    // 2. LA CAJA FUERTE (MÉTODO SINCRONIZADO)
    // - synchronized: Esta es la palabra MÁS IMPORTANTE del examen.
    // - Significa: "Solo un hilo puede ejecutar este método a la vez".
    // - Si entra el Cliente A, el Cliente B se espera fuera hasta que A termine.
    private static synchronized String realizarOperacion(String operacion, int cantidad) {

        if (operacion.equals("SACAR")) {
            // Lógica de seguridad: No dejar sacar si no hay fondos
            if (cantidad > saldoActual) {
                return "ERROR: Fondos insuficientes. Saldo actual: " + saldoActual;
            } else {
                saldoActual = saldoActual - cantidad; // Restamos
                return "OK. Retirado: " + cantidad + "€. Saldo restante: " + saldoActual + "€";
            }
        }
        else if (operacion.equals("INGRESAR")) {
            saldoActual = saldoActual + cantidad; // Sumamos
            return "OK. Ingresado: " + cantidad + "€. Nuevo saldo: " + saldoActual + "€";
        }
        return "Error: Operación no válida";
    }

    // Método solo de lectura (también sincronizado por si acaso consultan mientras otro ingresa)
    private static synchronized int verSaldo() {
        return saldoActual;
    }

    // 3. EL HILO DEL CAJERO (Gestor de Cliente)
    // Esta clase se encarga de hablar con UN usuario.
    static class HiloCajero implements Runnable {
        private Socket socket;

        public HiloCajero(Socket s) {
            this.socket = s;
        }

        @Override
        public void run() {
            String nombreCliente = socket.getInetAddress().toString();
            System.out.println("Cliente conectado: " + nombreCliente);

            try (
                    // Preparamos las tuberías de comunicación
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)
            ) {
                // Mensaje de bienvenida (Protocolo)
                pw.println("BIENVENIDO A CAIXA-DAM. Comandos: VER | SACAR <n> | INGRESAR <n> | SALIR");

                String linea;
                // Bucle: Leemos comandos hasta que el cliente se vaya
                while ((linea = br.readLine()) != null) {

                    // PARSEO DEL COMANDO
                    // split("\\s+"): Corta la frase por los espacios.
                    // Ejemplo: "SACAR 50" -> partes[0]="SACAR", partes[1]="50"
                    String[] partes = linea.trim().split("\\s+");
                    String comando = partes[0].toUpperCase(); // A mayúsculas para evitar errores
                    String respuesta = "";

                    switch (comando) {
                        case "VER":
                            respuesta = "Tu saldo es: " + verSaldo() + "€";
                            break;

                        case "SACAR":
                        case "INGRESAR":
                            // Verificamos que el usuario haya puesto una cantidad
                            if (partes.length > 1) {
                                try {
                                    // Convertimos el texto "50" a número 50
                                    int cantidad = Integer.parseInt(partes[1]);

                                    // Validación básica: No se pueden usar números negativos
                                    if(cantidad > 0) {
                                        // LLAMADA AL MÉTODO SEGURO (CRÍTICO)
                                        // Aquí es donde los hilos hacen cola si coinciden
                                        respuesta = realizarOperacion(comando, cantidad);
                                    } else {
                                        respuesta = "La cantidad debe ser mayor que 0.";
                                    }
                                } catch (NumberFormatException e) {
                                    // Si el usuario escribe "SACAR mil", entra aquí
                                    respuesta = "Error: La cantidad debe ser un número entero.";
                                }
                            } else {
                                respuesta = "Falta la cantidad. Ejemplo: " + comando + " 50";
                            }
                            break;

                        case "SALIR":
                            pw.println("Gracias por usar nuestros servicios.");
                            return; // El return mata el hilo y cierra la conexión

                        default:
                            respuesta = "Comando desconocido. Use VER, SACAR, INGRESAR o SALIR.";
                    }

                    // Enviamos la respuesta final al cliente
                    pw.println(respuesta);
                }
            } catch (IOException e) {
                System.err.println("Error o cliente desconectado: " + e.getMessage());
            }
        }
    }

    // 4. MAIN (EL BANCO)
    public static void main(String[] args) {
        int puerto = 6000;
        try (ServerSocket server = new ServerSocket(puerto)) {
            System.out.println("--- SERVIDOR BANCARIO INICIADO ---");
            System.out.println("Saldo inicial compartido: " + saldoActual + "€");
            System.out.println("Esperando clientes en puerto " + puerto + "...");

            // Bucle Infinito: El banco nunca cierra
            while (true) {
                // 1. Esperar cliente
                Socket s = server.accept();

                // 2. Crear al empleado (Hilo) para atenderle
                HiloCajero cajero = new HiloCajero(s);

                // 3. Arrancar el hilo
                // ¡IMPORTANTE! Usar .start(), nunca .run()
                new Thread(cajero).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}