package U2ProgramacionConcurrenteMultiHilo;

public class U2P05DeadlockCorregido implements  Runnable{
    // Objetos genéricos
    private static Object obj1 = new Object();
    private static Object obj2 = new Object();

    public static void main(String[] args) {
        Thread t1 = new Thread(new U2P03SleepingThreads());
        Thread t2 = new Thread(new U2P03SleepingThreads());

        // Ejercicio para casa: optimizar el código. Implements Runnable, sobreescribir Run con el código

        t1.start();
        t2.start();
    }

    @Override
    public void run() {
        Thread t1 = new Thread(() -> {
            synchronized (obj1){
                System.out.println("t1: Dentro del bloque obj1");
                synchronized (obj2){
                    System.out.println("t2: Dentro del bloque obj2");
                }
            };
        });
    }
}
