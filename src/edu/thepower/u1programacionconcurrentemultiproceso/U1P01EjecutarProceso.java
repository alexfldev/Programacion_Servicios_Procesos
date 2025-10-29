package edu.thepower.u1programacionconcurrentemultiproceso;

import java.io.IOException;

// Proceso: programa en ejecución
// Servicio: programa en segundo plano
// Programación concurrente: programas ejecutados de manera simultánea con un objetivo común

public class U1P01EjecutarProceso {
    public static void main(String[] args) {
        U1P01EjecutarProceso p = new U1P01EjecutarProceso();
        p.launcher("C:\\Program Files\\FileZilla FTP Client\\filezilla");
    }

    public void launcher(String programa){
        // Clase para chutar procesos
        ProcessBuilder pb = new ProcessBuilder(programa);
        try {
            pb.start();
        } catch (IOException e) {
            //  err muestra la información del error
            System.err.println("Error al iniciar el proceso" + programa);
            e.printStackTrace();
        }
    }
}