package U4ServiciosEnRed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class U4P01PoolServer {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(5);

        try (ServerSocket socket = new ServerSocket(2777)) {
            System.out.println("Servidor Iniciado");

            while (true) {
                Socket sc = socket.accept();
                pool.submit(() -> {
                    // La lógica debe ir DENTRO del hilo y del try-with-resources
                    try (BufferedReader bf = new BufferedReader(new InputStreamReader(sc.getInputStream()));
                         PrintWriter pr = new PrintWriter(sc.getOutputStream(), true)) {

                        String linea;
                        while ((linea = bf.readLine()) != null) {
                            System.out.println("Recibido de cliente: " + linea);
                            // Cambié 'pw' por 'pr' porque así la llamaste en la declaración
                            pr.println(linea.toLowerCase());
                        }

                    } catch (IOException e) {
                        System.err.println("Error en el servidor: " + e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}