package U2ProgramacionConcurrenteMultiHilo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//Este codigo sirve para Optimizar uso de recursos
public class U2P07ThreadsPool {
    public static void main(String[] args) {
        final int MAX_POOL_SIZE = 10;
        //queremos saber cuantas veces se ejecuta cada Thread (con un Map)
        Map<String, AtomicInteger> map = new ConcurrentHashMap<>();
        // ExecutorService Nos crea un Pool de Threads siempre disponibles para ejecutarse
        ExecutorService pool = Executors.newFixedThreadPool(MAX_POOL_SIZE);
        for (int i = 0; i < 50; i++) {
            //pedimos a un Thread del pool que ejecute una tarea
            pool.submit(()->{
                //añade el nombre al mapa como clave junto a un contador que se va incrementado
                map.computeIfAbsent(Thread.currentThread().getName(), k -> new AtomicInteger()).incrementAndGet();
                System.out.println("["+Thread.currentThread().getName()+"] Saludos");

            });

        }
        pool.shutdown(); // No acepta mas trabajos y termina de forma ordenada
        try { //pool termina los trabajos pendientes con tiempo 10 segundos
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)){
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
             pool.shutdownNow();
        }
        map.forEach((k,v)->{
            System.out.println("EL Thread: "+k + " Se ha ejecutado " + v.get());
        });
        System.out.println("Total ejecuciones Threads: " + map.values().stream().mapToInt(v->v.get()).sum());

    }
}

