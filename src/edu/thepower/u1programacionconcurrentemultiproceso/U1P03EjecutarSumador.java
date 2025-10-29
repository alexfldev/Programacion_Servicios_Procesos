package edu.thepower.u1programacionconcurrentemultiproceso;

import java.io.IOException;

public class U1P03EjecutarSumador {

    private static final String JAVA = "java";
    private static  final  String CP ="-cp";
    private static final String  CLASSPATH="C:\\Users\\AlumnoAfternoon\\Documents\\2_DAM_Programacion_de_Servicios_y_Procesos\\proyecto_intellij\\psp\\out\\production\\psp";
    private static final String CLASE = "edu.thepower.u1programacionmultiproceso.U1P03Sumador";

    public static void main(String[] args) {
        ProcessBuilder pb = new ProcessBuilder(JAVA,CP,CLASSPATH,CLASE,"10","20");

        try {
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); //Tenemos la salida Standar
            pb.redirectError(ProcessBuilder.Redirect.INHERIT); //Tenemos la salida Error
            pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}