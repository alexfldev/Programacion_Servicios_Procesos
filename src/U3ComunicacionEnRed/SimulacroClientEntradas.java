package U3ComunicacionEnRed;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SimulacroClientEntradas {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner sc = new Scanner(System.in)) {

            // Leer mensaje de bienvenida
            System.out.println("Servidor: " + br.readLine());

            String input;
            while (true) {
                System.out.print("Tu orden > ");
                input = sc.nextLine();

                pw.println(input);

                if (input.equalsIgnoreCase("SALIR")) break;

                System.out.println(br.readLine());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}