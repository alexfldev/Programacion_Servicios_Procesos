package U3ComunicacionEnRed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class U3P05ServerDicionario {

    // 1. EL DICCIONARIO COMPARTIDO (THREAD-SAFE)
    // - static: Solo hay UNO para todos los clientes (memoria compartida).
    // - TreeMap: Mantiene las claves ordenadas alfabéticamente (A-Z).
    // - Collections.synchronizedMap: ¡CRÍTICO! Hace que el mapa sea seguro para hilos.
    //   Sin esto, si dos clientes escriben a la vez, el servidor podría crashear.
    private static Map<String,String> diccionario = Collections.synchronizedMap(new TreeMap<>());

    // 2. BLOQUE STATIC (INICIALIZADOR)
    // Este código se ejecuta UNA sola vez al arrancar el programa.
    // Sirve para meter datos de prueba antes de que nadie se conecte.
    static{
        String [] claves = { "house" , "happy" ,"red" ,"monkey", "hello"};
        String [] valores ={"casa", "feliz" ,"rojo" , "mono" ,"hola"};
        for(int i=0;i<claves.length;i++){
            diccionario.put(claves[i],valores[i]);
        }
    }

    // 3. MÉTODO DE GESTIÓN (LÓGICA DEL HILO)
    // En lugar de crear una clase 'Gestor' aparte, usamos un método estático.
    private static void gestionarCliente(Socket socket){
        try(
                // Try-with-resources para cerrar todo al acabar
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)
        ){

            String comando;
            String respuesta;
            String[] partes;

            // Bucle de lectura
            while ((comando = br.readLine()) != null) {

                // 4. PARSEO DEL COMANDO (IMPORTANTE)
                // "inc perro dog" -> split divide el texto por espacios.
                // "\\s+" significa "cualquier espacio en blanco" (espacio, tabulador...).
                // "3" es el límite de trozos.
                // partes[0]="inc", partes[1]="perro", partes[2]="dog"
                partes = comando.split("\\s+", 3);

                // Analizamos la primera palabra (el comando)
                respuesta = switch (partes[0].trim().toLowerCase()) {

                    // CASO: TRADUCIR (trd gato)
                    case "trd" -> {
                        if (partes.length > 1) { // Verificamos que mandó la palabra
                            // getOrDefault: Busca la palabra, si no está devuelve el mensaje de error.
                            yield diccionario.getOrDefault(partes[1], "No existe la palabra en el diccionario");
                        } else {
                            yield "uso trd <palabra>";
                        }
                    }

                    // CASO: INCLUIR (inc gato cat)
                    case "inc" -> {
                        if (partes.length > 2) { // Necesitamos 3 partes: comando, palabra, traducción
                            diccionario.put(partes[1], partes[2]);
                            yield "Palabra insertada en el diccionario";
                        } else {
                            yield "uso: inc <palabra> <traduccion>";
                        }
                    }

                    // CASO: LISTAR TODO
                    case "list", "lis" -> { // Aceptamos "list" o "lis"
                        StringBuffer sb = new StringBuffer();

                        // Recorremos el mapa entero
                        for (Map.Entry<String, String> entrada : diccionario.entrySet()) {
                            // Construimos: "house:casa, "
                            sb.append(entrada.getKey()).append(":").append(entrada.getValue()).append(", ");
                        }

                        // Truco estético: Quitar la última coma y espacio sobrantes del final
                        if(sb.length() > 0){
                            sb.setLength(sb.length() - 2);
                        }
                        yield sb.toString();
                    }

                    // CASO: SALIR
                    case  "sal", "bye" -> "Hasta la vista.";

                    default -> "Error: el comando no existe.";
                };

                // Enviamos la respuesta calculada
                pw.println(respuesta);
            }

        } catch (Exception e) {
            System.err.println("Error en cliente: " + e.getMessage());
        }
    }

    // 5. MAIN CON LAMBDA (TRUCO PRO)
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(4000)){
            System.out.println("Servidor Diccionario Esperando en puerto: " + server.getLocalPort());

            while (true){
                Socket socket = server.accept();

                // AQUÍ ESTÁ LA MAGIA:
                // En vez de "new GestorCliente(socket)", usamos una Expresión Lambda.
                // "() -> gestionarCliente(socket)" crea un Runnable al vuelo que ejecuta nuestro método de arriba.
                // Es más rápido de escribir en un examen que crear una clase entera.
                Thread thread = new Thread(() -> gestionarCliente(socket));
                thread.start();
            }
        }catch (Exception e){
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}