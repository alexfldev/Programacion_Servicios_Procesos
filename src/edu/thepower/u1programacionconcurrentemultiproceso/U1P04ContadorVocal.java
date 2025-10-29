package edu.thepower.u1programacionconcurrentemultiproceso;

// Programación concurrente multiproceso

// 5 procesos, el programa cuenta vocales de un archivo, las clasifica aunque sean mayus o con tilde

// Se introduce una vocal y hay que contar cuantas veces aparece en el contenido de un archivo
// Un proceso para contar cada vocal
// El conteo de cada vocal se guardan en archivos diferentes
// Se muestra por consola el recuento de veces que aparece cada vocal

// Run > Edit configurations > Aplications > Arguments (le pasamos los argumentos de String[] args del main)

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class U1P04ContadorVocal {

    private static final Map <Character, Character>VOCALES;
    static{ // el boxing y el unboxing nos permite introducir los datos primitivos char al objeto Character del mapa
        VOCALES = new HashMap();
        VOCALES.put('a', 'á');
        VOCALES.put('e', 'é');
        VOCALES.put('i', 'í');
        VOCALES.put('o', 'ó');
        VOCALES.put('u', 'ú');
    }

    public static void main(String[] args) {
        U1P04ContadorVocal test = new U1P04ContadorVocal();
        test.contarVocales(args[0].charAt(0), args[1]);
    }

    private void contarVocales(char vocal, String archivo) {
        int contador = 0;

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
        System.out.println(contador);
    }
}