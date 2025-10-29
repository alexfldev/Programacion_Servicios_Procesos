package U2ProgramacionConcurrenteMultiHilo;

public class U2P03SleepingThreads implements Runnable{

    @Override
    public void run(){
        // Desde la ejecucción del thread (run) vamos a dormir el thread
        String nombreThread = "[" + Thread.currentThread().getName() + "]";
        System.out.println(nombreThread + " Iniciando ejecución...");
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println(nombreThread + " Despertando (1)");
        }
        while(!Thread.interrupted()){}
        System.out.println(nombreThread + " Despertando (2)");
    }

    public static void main (String [] args){
        Thread hilo = new Thread(new U2P03SleepingThreads(), "Sleeping Thread");
        hilo.start();

        String nombreThread = "[" + Thread.currentThread().getName() + "]";
        System.out.println(nombreThread + " Iniciando ejecución");
        for (int i = 3; i < 5; i++){
            try {
                System.out.println(nombreThread + " Durmiendo 5 segundos");
                hilo.sleep(5000);
                System.out.println(nombreThread + " Despertando");
                hilo.interrupt();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
