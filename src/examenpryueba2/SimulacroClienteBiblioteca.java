package examenpryueba2;


import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SimulacroClienteBiblioteca {
    public static void main(String[] args) {
        // Conexión al puerto 5000 (Biblioteca)
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner sc = new Scanner(System.in)) {

            // Mensaje de bienvenida
            System.out.println(br.readLine());

            String entrada;
            while (true) {
                System.out.print("¿Qué quieres leer/donar? > ");
                entrada = sc.nextLine();

                pw.println(entrada); // Enviar

                if (entrada.equalsIgnoreCase("SALIR")) break;

                // Leer respuesta del bibliotecario
                // TRUCO: Como el comando VER devuelve varias líneas, aquí las leemos una a una.
                // En un examen simple, basta con leer una línea, pero si "VER" manda muchas,
                // el println solo mostrará la primera.
                // Si quieres asegurarte de leer todo, podrías hacer un bucle pequeño,
                // pero para el examen básico, con esto suele valer:
                String respuesta = br.readLine();

                // Si el servidor manda saltos de línea (\n), el readLine podría cortarse.
                // En este código he diseñado el servidor para que VER mande todo en un solo bloque String.
                // pero recuerda que readLine lee hasta encontrar un \n.
                while(br.ready()){
                    respuesta += "\n" + br.readLine();
                }

                System.out.println("Biblioteca:\n" + respuesta);
            }

        } catch (Exception e) {
            System.err.println("La biblioteca está cerrada.");
        }
    }
}