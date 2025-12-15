package U3ComunicacionEnRed;

// CONSEJO DE EXAMEN:
// Por convención, las clases en Java empiezan con Mayúscula (Validacion).
// Si el profe es estricto, cámbialo a 'class Validacion'.
class validacion {

    // Método estático: Se puede llamar sin hacer 'new validacion()'
    // Ejemplo de uso: int p = validacion.validarPuerto(args);
    public static int validarPuerto(String[] args) {

        // PASO 1: ¿HAY ARGUMENTOS?
        // args[] es lo que escribes al lado del nombre del programa.
        // Ejemplo: "java Servidor 4000" -> args tiene longitud 1.
        // Si no escribes nada ("java Servidor"), la longitud es 0.
        if (args.length != 1) {
            // Lanzamos una excepción para detener el programa inmediatamente.
            throw new IllegalArgumentException("Debe ingresar un único argumento");
        }

        // PASO 2: ¿ES UN NÚMERO?
        // args[0] es un String ("4000"). Hay que convertirlo a int (4000).
        // OJO: Si args[0] es "hola", esta línea explota automáticamente
        // lanzando un 'NumberFormatException'.
        int puerto = Integer.parseInt(args[0]);

        // PASO 3: ¿ES UN PUERTO SEGURO?
        // - 0 al 1023: Puertos reservados para el sistema (HTTP, FTP, SSH...).
        // - 1024 al 65535: Puertos libres para nosotros.
        if (puerto < 1024 || puerto > 65535) {
            throw new IllegalArgumentException("Debe ingresar un puerto entre 1024 y 65535");
        }

        // Si sobrevive a todo, devolvemos el número limpio.
        return puerto;
    }
}