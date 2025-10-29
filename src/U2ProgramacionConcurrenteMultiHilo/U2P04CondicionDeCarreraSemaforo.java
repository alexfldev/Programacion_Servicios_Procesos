package U2ProgramacionConcurrenteMultiHilo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

// Semáforos permite acceder a varios hilos

// Deberes 23/10/2025: usar mapa normal (HashMap), en lugar de AtomicInteger, Integer. -> private static Map<String, Integer> mapa = new HashMap<>();
// Usar ReentrantLock en lugar de computeIfAbsent
// Al final hecho en clase

public class U2P04CondicionDeCarreraSemaforo implements Runnable{
    private static long tiempoPrueba = System.currentTimeMillis() + 100;
    private static Semaphore semaforo = new Semaphore(5, true);
    private static AtomicInteger contador = new AtomicInteger();
    // ConcurrentHashMap es una clase ThreadSafe que garantiza la exclusión mutua
    private static Map<String, Integer> mapa = new HashMap<>();
    private static int contadorSemaforo = 0;
    private static ReentrantLock lock = new ReentrantLock();


    public static void main(String[] args) {
        // Creamos la lista para hacer los joins de los threads y así controlar que el hilo haya acabado (bucle for each posterior)
        List<Thread> lista = new ArrayList<>();

        for (int i = 0; i < 10;i++){
            lista.add(new Thread(new U2P04CondicionDeCarreraSemaforo(), "thread_" + i));
            lista.get(i).start();
        }
        for (Thread h : lista){
            try {
                h.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("*** Uso del semáforo por los threads.");
        int acumulador = 0;
        for(String n: mapa.keySet()){
            acumulador += mapa.get(n);
            System.out.println("El thread " + n + " ha usado el semáforo " + mapa.get(n) + " veces.");
        }
        System.out.println("Usos de semáforo: " + contadorSemaforo);
        System.out.println("Total usos del semáforo: " + acumulador);
    }

    @Override
    public void run() {
        String nombre = "[" + Thread.currentThread().getName() + "]";
        while(System.currentTimeMillis() < tiempoPrueba){
        try {
            semaforo.acquire();
            lock.lock();
            try {
                contadorSemaforo++;
                mapa.put(nombre, mapa.getOrDefault(nombre, 0)+1);
            } finally {
                lock.unlock();
            }
            //mapa.computeIfAbsent(nombre, k -> new AtomicInteger()).incrementAndGet();
            System.out.println(nombre + " Valor insertado en el mapa: " + mapa.get(nombre));
            System.out.println(nombre + " Adquirido semáforo número: " + contador.incrementAndGet());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Contador para saber si el semáforo sobrepasa y por ende no funciona correctamente
        if(contador.get() > 5)
            throw new RuntimeException("Semáforo sobrepasado");

        contador.decrementAndGet();
        semaforo.release();
        System.out.println(nombre + " Semáforo liberado");
        }
    }
}
