package U2ProgramacionConcurrenteMultiHilo;

// Monitores los tienen los objetos. Los objetos/clases se bloquean con el monitor y se bloquea el código correspondiente. Syncrhonized

public class U2P04CondicionDeCarreraMonitorSynchronized {
    private static int contador = 0;

    //SYNCRONIZED -> BLOQUEA LA CLASE(EN ESTE CASO) / OBJETO PARA QUE LOS MÉTODOS NO SE EJECUTEN AL MISMO TIEMPO
    public static synchronized void incrementarContador(int num){

        System.out.println("Entrando en incrementarContador");
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        contador += num;
        System.out.println("Saliendo de incrementarContador");
    }

    public static synchronized int getContador(){

        System.out.println("Entrando en getContador");
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Saliendo de getContador");
        return contador;
    }

    public static void main(String[] args) {

        //LOS GUIONES BAJOS SON COMO PUNTOS DE LOS MILES IMAGINARIOS (NO AFECTA EN NADA)
        final int ITERACIONES = 1_000_000;
        final int VALOR = 10;
/*
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
   */
        Thread accesor1 = new Thread(() -> {
            getContador();
        });

        Thread accesor2 = new Thread(() -> {
            incrementarContador(VALOR);
        });

        accesor1.start();
        accesor2.start();
/*
        incrementador.start();
        decrementador.start();


        try {
            incrementador.join();
            decrementador.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("El valor final de contador es: " + getContador());

 */
    }
}
