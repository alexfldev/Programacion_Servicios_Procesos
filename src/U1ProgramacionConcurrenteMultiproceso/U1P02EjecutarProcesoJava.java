package U1ProgramacionConcurrenteMultiproceso;

// stdout -> standard out
// stderr -> standard error

public class U1P02EjecutarProcesoJava {

    private static final String JAVA = "java";
    private static final String VERSION = "-version";

    public static void main(String[] args) {
        ProcessBuilder pb = new ProcessBuilder(JAVA, VERSION);
        // 1. Redirect la salida de la información que hereda del proceso hijo
        /*pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        // Muestra los errores (en este caso, el comando version se muestra con los errores)
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/


        // 2. Usando un BufferReader (canal entre el proceso que se lanza y el nuevo que se ejecuta)
        /*try {
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            //  err muestra la información del error
            System.err.println("Error al iniciar el proceso");
            e.printStackTrace();
        }*/


        // 3. Volcar salida a fichero
        /*pb.redirectOutput(new File("./resources/salida.txt"));
        pb.redirectError(new File("./resources/error.txt"));
        try {
            pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }
}
