package PracticaExamen;




import java.util.*;
        import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 🏭 Simulación: cada trabajador (hilo) fabrica una cantidad específica de coches.
 *
 * Ejemplo tipo examen:
 * - Control de concurrencia con semáforo
 * - Uso de AtomicInteger y Lock
 * - Estadísticas al final
 */
public class FabricaPorCantidad implements Runnable {

    // 🚦 Solo 3 líneas de montaje (máximo 3 trabajadores a la vez)
    private static final Semaphore semaforo = new Semaphore(3, true);

    // 🧮 Contadores globales
    private static final AtomicInteger totalFabricado = new AtomicInteger(0);
    private static final AtomicInteger enProduccion = new AtomicInteger(0);

    // 📊 Registro por trabajador
    private static final Map<String, Integer> produccionPorTrabajador = new HashMap<>();

    // 🔒 Candado para proteger el mapa
    private static final ReentrantLock lock = new ReentrantLock();

    // 🔢 Cantidades que fabricará cada trabajador
    // Puedes pedirlas por teclado o definirlas aquí
    private static final Map<String, Integer> asignaciones = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("🏭 Iniciando simulación con cantidades por trabajador...\n");

        // Asignamos cuántos coches fabricará cada trabajador
        asignaciones.put("Trabajador_1", 4);
        asignaciones.put("Trabajador_2", 8);
        asignaciones.put("Trabajador_3", 6);
        asignaciones.put("Trabajador_4", 10);
        asignaciones.put("Trabajador_5", 3);
        asignaciones.put("Trabajador_6", 5);
        asignaciones.put("Trabajador_7", 9);
        asignaciones.put("Trabajador_8", 2);

        // Lista de hilos (trabajadores)
        List<Thread> trabajadores = new ArrayList<>();

        // Creamos un hilo por trabajador
        for (String nombre : asignaciones.keySet()) {
            Thread t = new Thread(new FabricaPorCantidad(), nombre);
            trabajadores.add(t);
            t.start();
        }

        // Esperamos que todos terminen (join)
        for (Thread t : trabajadores) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Error esperando al hilo: " + t.getName());
            }
        }

        // Mostramos estadísticas
        mostrarEstadisticas();
    }

    @Override
    public void run() {
        String nombre = Thread.currentThread().getName();
        int cantidadAsignada = asignaciones.get(nombre); // cuántos debe fabricar

        for (int i = 1; i <= cantidadAsignada; i++) {
            try {
                semaforo.acquire(); // pide una línea de montaje
                enProduccion.incrementAndGet();

                System.out.println("🔧 " + nombre + " fabricando coche " + i + "/" + cantidadAsignada);
                Thread.sleep(200 + new Random().nextInt(300)); // simula trabajo

                totalFabricado.incrementAndGet(); // coche terminado

                lock.lock();
                try {
                    produccionPorTrabajador.put(nombre,
                            produccionPorTrabajador.getOrDefault(nombre, 0) + 1);
                } finally {
                    lock.unlock();
                }

                System.out.println("✅ " + nombre + " terminó coche " + i + "/" + cantidadAsignada);

            } catch (InterruptedException e) {
                System.err.println("⚠️ " + nombre + " interrumpido.");
            } finally {
                enProduccion.decrementAndGet();
                semaforo.release();
            }
        }

        System.out.println("🏁 " + nombre + " terminó sus " + cantidadAsignada + " coches asignados.");
    }

    // 📊 Muestra los resultados finales
    private static void mostrarEstadisticas() {
        System.out.println("\n===== 📊 ESTADÍSTICAS DE PRODUCCIÓN =====");
        int totalPorMapa = 0;

        lock.lock();
        try {
            for (String trabajador : produccionPorTrabajador.keySet()) {
                int num = produccionPorTrabajador.get(trabajador);
                totalPorMapa += num;
                System.out.println("👷 " + trabajador + " fabricó " + num + " coches.");
            }
        } finally {
            lock.unlock();
        }

        System.out.println("--------------------------------------------");
        System.out.println("📦 Total según mapa: " + totalPorMapa);
        System.out.println("🧮 Total global contado: " + totalFabricado.get());
        System.out.println("============================================");
    }
}
