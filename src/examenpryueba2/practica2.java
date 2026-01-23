/*
import java.io.*;
        import java.net.*;

public class ServidorCalculadora {

    public static void main(String[] args) {
        try {
            // a) El servidor aceptará conexiones en el puerto 6000
            ServerSocket servidor = new ServerSocket(6000);
            System.out.println("Servidor Calculadora arrancado en puerto 6000.");

            while (true) {
                // Esperamos cliente
                Socket cliente = servidor.accept();
                System.out.println("Cliente conectado.");

                // Creamos el hilo para atenderle
                HiloCliente hilo = new HiloCliente(cliente);
                hilo.start();
            }
        } catch (IOException e) {
            System.out.println("Error en el servidor");
        }
    }

    // Clase para manejar a cada cliente
    static class HiloCliente extends Thread {
        Socket socket;

        public HiloCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);

                // f) Mensaje de bienvenida
                salida.println("Bienvenido a la calculadora. Formato: OPERACION num1 num2 (Ej: SUM 5 5). Escribe FIN para salir.");

                String linea;
                // Leemos lo que manda el cliente
                while ((linea = entrada.readLine()) != null) {

                    // e) Si envía FIN, cerramos
                    if (linea.equalsIgnoreCase("FIN")) {
                        salida.println("Adios.");
                        break;
                    }

                    try {
                        // b) Formato texto, separamos por espacios
                        String[] partes = linea.split(" ");

                        // d) Validar formato (tienen que ser 3 cosas: Operacion, num1, num2)
                        if (partes.length != 3) {
                            salida.println("ERROR: Formato incorrecto. Debe ser: OPERACION num1 num2");
                            continue; // Volvemos al inicio del while
                        }

                        String operacion = partes[0].toUpperCase();
                        // c) Validar que sean números
                        double num1 = Double.parseDouble(partes[1]);
                        double num2 = Double.parseDouble(partes[2]);
                        double resultado = 0;

                        // c) Realizar la operación
                        if (operacion.equals("SUM") || operacion.equals("SUMA")) {
                            resultado = num1 + num2;
                            salida.println("RESULTADO: " + resultado);
                        }
                        else if (operacion.equals("RES") || operacion.equals("RESTA")) {
                            resultado = num1 - num2;
                            salida.println("RESULTADO: " + resultado);
                        }
                        else if (operacion.equals("MUL") || operacion.equals("MULT")) {
                            resultado = num1 * num2;
                            salida.println("RESULTADO: " + resultado);
                        }
                        else if (operacion.equals("DIV")) {
                            // d) Error división por cero
                            if (num2 == 0) {
                                salida.println("ERROR: No se puede dividir por cero.");
                            } else {
                                resultado = num1 / num2;
                                salida.println("RESULTADO: " + resultado);
                            }
                        }
                        else {
                            // d) Operación no reconocida
                            salida.println("ERROR: Operacion no reconocida.");
                        }
import java.io.*;
import java.net.*;
import java.util.Scanner;

                        public class ClienteCalculadora {
                            public static void main(String[] args) {
                                String host = "localhost";
                                int puerto = 6000;

                                try {
                                    Socket socket = new Socket(host, puerto);

                                    // Para leer del servidor
                                    BufferedReader entradaServidor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                    // Para enviar al servidor (true para autoflush)
                                    PrintWriter salidaServidor = new PrintWriter(socket.getOutputStream(), true);
                                    // Para leer del teclado
                                    Scanner teclado = new Scanner(System.in);

                                    // f) Recibimos mensaje de bienvenida
                                    String bienvenida = entradaServidor.readLine();
                                    System.out.println("Servidor: " + bienvenida);

                                    String comando;
                                    while (true) {
                                        System.out.print("Introduce operación: ");
                                        comando = teclado.nextLine();

                                        // Enviamos al servidor
                                        salidaServidor.println(comando);

                                        // Leemos respuesta
                                        String respuesta = entradaServidor.readLine();
                                        System.out.println(respuesta); // g) Mostrar respuesta por consola

                                        // Si era FIN o el servidor se despide, salimos
                                        if (comando.equalsIgnoreCase("FIN") || respuesta == null) {
                                            break;
                                        }
                                    }

                                    socket.close();

                                } catch (IOException e) {
                                    System.out.println("Error al conectar con el servidor.");
                                }
                            }
                        }

                    } catch (NumberFormatException e) {
                        // d) Error si los operandos no son números
                        salida.println("ERROR: Los operandos deben ser numeros reales.");
                    } catch (Exception e) {
                        salida.println("ERROR: Ha ocurrido un error inesperado.");
                    }
                }
                socket.close();
            } catch (IOException e) {
                // Si el cliente se desconecta a lo bruto
            }
        }
    }

*/