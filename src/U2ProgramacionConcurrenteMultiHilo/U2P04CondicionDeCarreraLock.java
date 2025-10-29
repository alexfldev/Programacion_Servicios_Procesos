package U2ProgramacionConcurrenteMultiHilo;

import java.util.concurrent.locks.ReentrantLock;

public class U2P04CondicionDeCarreraLock {
    private static int contador = 0;

    // Bloqueos/Locks bloqueamos de manera explícita una parte del código y luego la desbloqueamos
    // Objetos ReentrantLock permiten implementar controles sobre código que queremos que sea thread save (así un solo thread puede acceder a un recurso). Evitando condiciones de carrera
    // Exclusión mutua en el código que hay después del .lock() y antes del .unlock()
    private static ReentrantLock candado = new ReentrantLock();

    public static void incrementarContador(int num){
        System.out.println("Entrando en incrementarContador");

        candado.lock(); // recurso bloqueado hasta que se desbloquee
        // Más granulado, permite mayor control que el monitor
        // Se libera el bloqueo cuando se debe
        try {
            contador += num;
        }finally {
            candado.unlock(); // recurso desbloqueado (hay que hacer este unlock implícito, si no el código se bloqueará eternamente
        }
        System.out.println("Saliendo de incrementarContador");
    }

    public static int getContador(){
        System.out.println("Entrando en getContador");
        System.out.println("Saliendo de getContador");
        return contador;
    }

    public static void main(String[] args) {

        // LOS GUIONES BAJOS SON COMO PUNTOS DE LOS MILES IMAGINARIOS (NO AFECTA EN NADA)
        final int ITERACIONES = 1_000_000;
        final int VALOR = 10;

        // THREAD PARA INCREMENTAR EL VALOR DE LA VARIABLE CONTADOR EN "ITERACIONES" VECES
        Thread incrementador = new Thread(() -> {

            System.out.println("Iniciando ejecucion incremetador");
            for (int i = 0; i < ITERACIONES; i++) {
                incrementarContador(VALOR);
            }
            System.out.println("Acabando ejecucion incremetador");
        });

        // THREAD PARA DECREMENTAR EL VALOR DE LA VARIABLE CONTADOR EN "ITERACIONES" VECES
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
            incrementador.join(); // Espera a que terminen los threads
            decrementador.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("El valor final de contador es: " + getContador());
    }
}
