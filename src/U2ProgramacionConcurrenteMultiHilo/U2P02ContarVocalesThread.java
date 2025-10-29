package U2ProgramacionConcurrenteMultiHilo;

import edu.thepower.u1programacionconcurrentemultiproceso.U1P04EjecutarContadorVocal;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class U2P02ContarVocalesThread implements Runnable {

    private static final Map<Character, Character> VOCALES;
    static{ // el boxing y el unboxing nos permite introducir los datos primitivos char al objeto Character del mapa
        VOCALES = new HashMap();
        VOCALES.put('a', 'á');
        VOCALES.put('e', 'é');
        VOCALES.put('i', 'í');
        VOCALES.put('o', 'ó');
        VOCALES.put('u', 'ú');
    }

    private char vocal;
    private String archivo;
    private String salida = "./salidaThread/";

    public U2P02ContarVocalesThread(char vocal, String archivo, String salida) {
        this.vocal = vocal;
        this.archivo = archivo;
        this.salida = salida;
    }

    @Override
    public void run() {
        int contador = 0;

        System.out.println("[" + Thread.currentThread().getName() + "] Iniciado cuenta vocal " + vocal);

        try (BufferedReader in = new BufferedReader(new FileReader(archivo))) {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.toLowerCase();
                // Leer la línea por caracteres
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == vocal || line.charAt(i) == VOCALES.get(vocal)) {
                        contador++;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("No existe el archivo: " + archivo);
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + archivo);
            throw new RuntimeException(e);
        }
        System.out.println("[" + Thread.currentThread().getName() + "] Finalizada cuenta vocal " + vocal + ": " + contador + " resultados.");
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(salida + vocal + ".txt"))){

        } catch (IOException e) {
            System.err.println("Error al escribir el archivo " + vocal + ".txt");
        }
    }

    public static void main(String[] args){
        List<Thread> threads = new ArrayList<>();
        final String ARCHIVO_ENTRADA = "./resources/vocales.txt";
        final String DIR_SALIDA = "./salida";

        // Creación del directorio salida
        File directorioSalida= new File("./salida");
        if(directorioSalida.mkdir()){
            System.out.println("Directorio de salida creado con éxito.");
        } else{
            System.out.println("El directorio de salida ya existe.");
            for(File a : directorioSalida.listFiles()){ // si existe el directorio, elimina los archivos que contiene
                a.delete();
            }
        }

        for(char v : VOCALES.keySet()){
            Thread hilo = new Thread(new U2P02ContarVocalesThread(v, ARCHIVO_ENTRADA, DIR_SALIDA));

            //AÑADIMOS CADA HILO EN LA COLECCIÓN
            threads.add(hilo);

            hilo.start();
        }

        for (Thread t : threads){
            try {
                //METODO JOIN: METODO "AWAIT", ESPERA A QUE TERMINE CADA THEARD PARA TERMINAR EL BUCLE
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        int contador = 0;

        for (char v : VOCALES.keySet()) {

            BufferedReader br = null;

            try {
                br = new BufferedReader(new FileReader("./salida/" + v + ".txt"));
                int numero = Integer.parseInt(br.readLine());
                System.out.println("El numero de (" + v + ") es: " + numero);
                contador += numero;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NumberFormatException e) {
                System.err.println("El archivo" + "./salida/" + v + ".txt" + " no contenia un numero");
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        System.out.println("El numero total de vocales es: " + contador);
    }
}
