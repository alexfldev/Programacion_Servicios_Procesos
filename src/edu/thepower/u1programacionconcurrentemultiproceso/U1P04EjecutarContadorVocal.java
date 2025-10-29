package edu.thepower.u1programacionconcurrentemultiproceso;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class U1P04EjecutarContadorVocal {
    private static final String JAVA = "java";
    private static final String CP = "-cp";
    private static final String CLASSPATH="C:\\Users\\AlumnoAfternoon\\Documents\\2_DAM_Programacion_de_Servicios_y_Procesos\\proyecto_intellij\\psp\\out\\production\\psp";
    private static final String CLASE = "edu.thepower.u1programacionmultiproceso.U1P04ContadorVocal";
    private static final String ARCHIVO = "./resources/vocales.txt";
    private static final String[] VOCALES = {"a", "e", "i", "o", "u"};
    private static final String SALIDA = "./salida/";
    private static final String EXTENSION = ".txt";

    public static void main(String[] args){
        List<Process> procesos = new ArrayList<>();

        // Creación del directorio salida
        File directorioSalida= new File("./salida");
        if(directorioSalida.mkdir()){
            System.out.println("Directorio de salida creado con éxito.");
        } else{
            System.out.println("El directorio de salida ya existe.");
        }

        // Bucle para lanzar los cinco procesos
        for (int i = 0; i < VOCALES.length; i++){
            // Ejecución de comando en la máquina virtual
            ProcessBuilder pb = new ProcessBuilder(JAVA, CP, CLASSPATH, CLASE, VOCALES[i], ARCHIVO);

            // Escritura del archivo en a.txt del directorio salida
            pb.redirectOutput(new File(SALIDA + VOCALES[i] + EXTENSION));
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            // Start del ProcessBuilder
            try {
                procesos.add(pb.start());
            } catch (IOException e) {
                System.out.println("Error al iniciar el archivo" + e.getMessage());
            }
        }
        System.out.println("Salida finalizada correctamente.");

        for (Process proceso : procesos){
            try {
                proceso.waitFor();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        int acumulador = 0;
        for (int i = 0; i < VOCALES.length; i++) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(SALIDA + VOCALES[i] + EXTENSION));
                int n = Integer.parseInt(br.readLine());
                System.out.println("El número de vocales es: " + VOCALES[i] + " es: " + n);
                acumulador += n;
                br.close();
            }catch (NumberFormatException e) {
                System.out.println("El archivo" + SALIDA + VOCALES[i] + EXTENSION + " no contiene un número: " + e.getMessage());
            }catch (FileNotFoundException e) {
                System.err.println("Error al encontrar el archivo " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Error de lectura " + e.getMessage());
            }
        }
        System.out.println("Total de vocales contadas " + acumulador);
    }
}