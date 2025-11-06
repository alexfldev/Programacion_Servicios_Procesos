package U2ProgramacionConcurrenteMultiHilo;

public class U2P06ThreadDemonio {
    public static void main(String[] args) {
       long tiempo = System.currentTimeMillis()+10000;
       //Los 2 thread imprimen saludos por tiempo
        Thread t1 = new Thread(()->{
           while (tiempo > System.currentTimeMillis() ){
               System.out.println("t1:saludo");
               try {
                   Thread.sleep(500);
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }
           }

        });
        Thread t2 = new Thread(()->{
            while (tiempo > System.currentTimeMillis() ){
                System.out.println("t2:saludo");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        Thread latido = new Thread(()->{
            while(true){
                System.out.println("Boom Boom");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        //Este Thread seguira vivo mientras haya algun Thread que esta ejecutandose
        latido.setDaemon(true);
        System.out.println("Inicio ejecucion Threads");
        t1.start(); //t1.setPriority(Thread.MAX_PRIORITY); sirve para darle prioridad a los Threads
        t2.start();
        latido.start();
        System.out.println("Threads ejecutandose");

    }
}