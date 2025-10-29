package U2ProgramacionConcurrenteMultiHilo;

// Programación multihilo es más óptima que multiproceso
// Ojo condición de carrera. Varios hilos pueden ir a un mismo objetivo
// Deadlock / Abrazo mortal

public class U2P00ThreadTesting {
    public static void main(String[] args){
        System.out.println("El nombre del thread es: " + Thread.currentThread().getName());
        System.out.println("El ID del thread es: " + Thread.currentThread().threadId());
        System.out.println("La prioridad del thread es: " + Thread.currentThread().getPriority());
        System.out.println("El estado del thread es: " + Thread.currentThread().getState());
        System.out.println("El grupo del thread es: " + Thread.currentThread().getThreadGroup());
    }
}
