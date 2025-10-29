package U2ProgramacionConcurrenteMultiHilo;

import java.util.concurrent.atomic.AtomicInteger;

public class U2P04CondicionDeCarreraAtomicVars {
    // ATOMICINTEGER -> TIPO DE DATO QUE EVITA LA CONCURRENCIA DE THREADS
    // Solamente dejan que un thread la modifique simultaneamente
    private static AtomicInteger contador = new AtomicInteger(0);

    // SYNCRONIZED -> BLOQUEA LA CLASE(EN ESTE CASO) / OBJETO PARA QUE LOS MÉTODOS NO SE EJECUTEN AL MISMO TIEMPO
    public static void incrementarContador(int num){
        contador.addAndGet(num);
    }

    public static int getContador(){
        return contador.get();
    }

    public static void main(String[] args) {

        //LOS GUIONES BAJOS SON COMO PUNTOS DE LOS MILES IMAGINARIOS (NO AFECTA EN NADA)
        final int ITERACIONES = 1_000_000;
        final int VALOR = 10;

        //THREAD PARA INCREMENTAR EL VALOR DE LA VARIABLE CONTADOR EN "ITERACIONES" VECES
        Thread incrementador = new Thread(() -> {

            System.out.println("Iniciando ejecucion incremetador");
            for (int i = 0; i < ITERACIONES; i++) {
                incrementarContador(VALOR);
            }
            System.out.println("Acabando ejecucion incremetador");
        });

        //THREAD PARA DECREMENTAR EL VALOR DE LA VARIABLE CONTADOR EN "ITERACIONES" VECES
        Thread decrementador = new Thread(() -> {

            System.out.println("Iniciando ejecucion decrementador");
            for (int i = 0; i < ITERACIONES; i++) {
                incrementarContador(-VALOR);
            }
            System.out.println("Acabando ejecucion decrementador");
        });

        incrementador.start();
        decrementador.start();

        try {
            incrementador.join();
            decrementador.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("El valor final de contador es: " + getContador());


    }
}
