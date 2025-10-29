package U2ProgramacionConcurrenteMultiHilo;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class U2P02ContadorVocalThread implements Runnable {

    private String archivo;
    private String salida;
    private char vocal;

    private static final Map<Character, Character> VOCALES;

    static {
        VOCALES = new HashMap();
        VOCALES.put('a', 'á');
        VOCALES.put('e', 'é');
        VOCALES.put('i', 'í');
        VOCALES.put('o', 'ó');
        VOCALES.put('u', 'ú');
    }

    public U2P02ContadorVocalThread(char vocal, String archivo, String salida) {

        this.vocal = vocal;
        this.archivo = archivo;
        this.salida = salida;
    }

    @Override
    public void run(){

        int contador = 0;
        System.out.println("[" + Thread.currentThread().getName() + "] iniciando cuenta vocal " + vocal);

        try (BufferedReader in = new BufferedReader(new FileReader(archivo))){
            String line = "";

            // Mientras dentro de la linea hay algo distinto a null entra en el bucle
            while ((line = in.readLine()) != null){

                line = line.toLowerCase();

                for(int i = 0; i < line.length(); i++){
                    // Si la vocal se encuentra entre uno de los
                    // carácteres de la linea sumo uno al contador
                    if(line.charAt(i) == vocal || line.charAt(i) == VOCALES.get(vocal)){
                        contador++;
                    }
                }
            }

        }catch (FileNotFoundException e){
            System.err.println("Archivo "+ archivo+ " no encontrado");
            throw new RuntimeException();
        }catch (IOException e){
            System.err.println("Error en lectura de archivo " + archivo);
            throw new RuntimeException();
        }
        System.out.println("[" + Thread.currentThread().getName() + "] finalizada cuenta vocal " + vocal);

        try (BufferedWriter out = new BufferedWriter(new FileWriter(salida + vocal + ".txt"))) {

            out.write(String.valueOf(contador));

        } catch (IOException e) {
            System.err.println("Error al escribir el archivo " + vocal + ".txt");
        }
    }

    public static void main(String[] args) {

        //COLECCION PARA ALMACENAR LAS REFERENCIAS A LOS THREADS
        List<Thread> threads = new ArrayList();

        final String ARCHIVO_ENTRADA = "./resources/vocales.txt";
        final String DIR_SALIDA = "./salida/";

        //Creacion del directorio salida
        File dirSalida = new File("salida");

        if(dirSalida.mkdir()) {
            System.out.println("El directorio de salida se ha creado correctamente");
        } else {
            System.err.println("El directorio de salida ya existe");
            for (File file : dirSalida.listFiles()) {
                file.delete();
            }
        }

        for(char v : VOCALES.keySet()){
            Thread hilo = new Thread(new U2P02ContadorVocalThread(v, ARCHIVO_ENTRADA, DIR_SALIDA));

            //AÑADIMOS CADA HILO EN LA COLECCIÓN
            threads.add(hilo);

            hilo.start();
        }

        //PROCESAMOS LA SALIDA DE LOS THREADS PARA CONTAR EL NUMERO TOTAL DE VOCALES
        //ANTES DE EJECUTAR ESTA PARTE, HAY QUE ESPERAR A QUE TERMINEN LOS PROCESOS DE CREACION DE ARCHIVOS
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
