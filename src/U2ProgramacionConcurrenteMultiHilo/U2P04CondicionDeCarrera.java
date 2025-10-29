package U2ProgramacionConcurrenteMultiHilo;

public class U2P04CondicionDeCarrera {

    // Variables de clase
    private static int contador = 0;

    private static void incrementarContador(int num){
        contador += num;
    }

    /*private static void decrementarContador(int num){
        contador -= num;
    }*/

    private static int getContador(){
        return contador;
    }

    public static void main (String[] args){
        // Los guiones bajos ponen los puntos
        final int ITERACIONES = 1_000_000;
        final int VALOR = 10;

        // Hilo creado con expresión lambda
        // Este hilo incrementa su valor según el valor establecido el número de iteraciones establecidas
        Thread incrementador = new Thread(() -> {
            System.out.println("[" + Thread.currentThread().getName() + "]" + " Iniciando ejecución");
            for(int i = 0; i < ITERACIONES; i++){
                incrementarContador(VALOR);
            }
            System.out.println("[" + Thread.currentThread().getName() + "]" + " Finalizando ejecución");
        });

        // Este hilo decrementa su valor según el valor establecido el número de iteraciones establecidas
        Thread decrementador = new Thread(() ->{
            System.out.println("[" + Thread.currentThread().getName() + "]" + " Iniciando ejecución");
            for(int i = 0; i < ITERACIONES; i++){
                incrementarContador(-VALOR);
            }
            System.out.println("[" + Thread.currentThread().getName() + "]" + " Finalizando ejecución");
        });

        incrementador.start();
        decrementador.start();

        try {
            incrementador.join();
            decrementador.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("El valor final del contador es: " + getContador());
    }
}
