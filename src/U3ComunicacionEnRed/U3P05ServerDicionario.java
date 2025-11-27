package U3ComunicacionEnRed;

import javax.xml.transform.Source;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class U3P05ServerDicionario {

    private static Map<String,String> diccionario = Collections.synchronizedMap(new TreeMap<>());

    //iniciador
    static{
        String [] claves = { "house" , "happy" ," red" ,"monkey", "hello"};
        String [] valores ={"casa", "feliz" ,"rojo" , "mono" ,"hola"};
        for(int i=0;i<claves.length;i++){
            diccionario.put(claves[i],valores[i]);
        }
    }

    private static void gestionarCliente(Socket socket){
        try(
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)
        ){

            String comando;
            String respuesta;
            String[] partes;

            while ((comando = br.readLine()) != null) {

                partes = comando.split("\\s+", 3);

                respuesta = switch (partes[0].trim().toLowerCase()) {

                    case "trd" -> {
                        if (partes.length > 1) {
                            yield diccionario.getOrDefault(partes[1], "No existe la palabra en el diccionario");
                        } else {
                            yield "uso trd <palabra>";
                        }
                    }

                    case "inc" -> {
                        if (partes.length > 2) {
                            diccionario.put(partes[1], partes[2]);
                            yield "Palabra insertado en el diccionario";
                        } else {
                            yield "uso: inc <palabra> <traduccion>";
                        }
                    }

                    case "list" -> {
                        StringBuffer sb = new StringBuffer();

                        for (Map.Entry entrada : diccionario.entrySet()) {

                            sb.append(entrada.getKey()).append(":").append(entrada.getValue()).append(",");



                        }
                        if(sb.length()>0){
                            sb.setLength(sb.length()-2);
                        }
                        yield sb.toString(); // obligatorio para compilar (no añade función)
                        //otra manera de hacerlo
                        // diccionario.entrySet().stream().map(Entry<String,String> e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", "))
                    }
                    case  "sal", "bye" -> "Hasta la vista.";

                       default -> "Error: el comando no existe.";
                    };

                pw.println(respuesta);


            }

        } catch (Exception e) {

        }
    }

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(4000)){
            System.out.println("Servidor Esperando Conexiones en el puerto 4000: " + server.getLocalPort());
            while (true){
                Socket socket = server.accept();
                Thread thread = new Thread(() -> gestionarCliente(socket));
                thread.start();
            }
        }catch (Exception e){
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

}
