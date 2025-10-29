package PracticaExamen;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 🏭 Simulación de una fábrica multihilo (versión mejorada tipo examen)
 *
 * ✅ Varios trabajadores (threads) fabrican productos.
 * ✅ Cada producto tiene un ID único generado con AtomicInteger.
 * ✅ Solo 3 trabajadores pueden usar la línea de montaje a la vez (Semaphore).
 * ✅ Se controla la condición de carrera con ReentrantLock.
 * ✅ Al final se muestran estadísticas completas.
 */
public class FabricaCondicionCarrera implements Runnable {

    // ⏱️ Duración total de la simulación (en milisegundos)
    private static final long TIEMPO_SIMULACION = System.currentTimeMillis() + 5000;

    // 🚦 Semáforo que limita el número de líneas de montaje (máx 3 hilos a la vez)
    private static final Semaphore semaforo = new Semaphore(3, true);

    // 🧮 Contadores seguros (AtomicInteger evita condición de carrera)
    private static final AtomicInteger totalProductosFabricados = new AtomicInteger(0);
    private static final AtomicInteger enProduccion = new AtomicInteger(0);

    // 🔢 Generador de IDs únicos para productos
    private static final AtomicInteger generadorID = new AtomicInteger(0);

    // 📊 Registro de producción por trabajador
    private static final Map<String, Integer> produccionPorTrabajador = new HashMap<>();

    // 🔒 Lock para proteger el acceso al mapa compartido
    private static final ReentrantLock lock = new ReentrantLock();

    // 🧱 Clase interna: representa un producto fabricado
    static class Producto {
        private final int id;
        private final String trabajador;

        public Producto(int id, String trabajador) {
            this.id = id;
            this.trabajador = trabajador;
        }

        @Override
        public String toString() {
            return "🆔 Producto " + id + " fabricado por " + trabajador;
        }
    }

    public static void main(String[] args) {
        System.out.println("🏭 Iniciando simulación de la fábrica con IDs únicos...\n");

        // Lista para almacenar los threads
        List<Thread> trabajadores = new ArrayList<>();

        // 🔁 Creamos 8 trabajadores (hilos)
        for (int i = 1; i <= 8; i++) {
            Thread t = new Thread(new FabricaCondicionCarrera(), "Trabajador_" + i);
            trabajadores.add(t);
            t.start();
        }

        // ⏸️ Esperamos a que todos los hilos terminen
        for (Thread t : trabajadores) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Error esperando al hilo: " + t.getName());
            }
        }

        // 🧾 Mostrar estadísticas finales
        mostrarEstadisticas();
    }

    @Override
    public void run() {
        String nombre = Thread.currentThread().getName();
        Random random = new Random();

        while (System.currentTimeMillis() < TIEMPO_SIMULACION) {
            try {
                // 🚦 El trabajador pide acceso a la línea de montaje
                semaforo.acquire();
                enProduccion.incrementAndGet();

                System.out.println("🔧 " + nombre + " está fabricando un producto...");

                // Simulamos tiempo de producción (200 a 500 ms)
                Thread.sleep(200 + random.nextInt(300));

                // 🔢 Crear un nuevo producto con ID único
                int idProducto = generadorID.incrementAndGet();
                Producto nuevo = new Producto(idProducto, nombre);

                System.out.println("✅ " + nuevo);

                // 🧮 Incrementamos contadores
                totalProductosFabricados.incrementAndGet();

                // 🔒 Actualizamos el mapa protegido por el lock
                lock.lock();
                try {
                    produccionPorTrabajador.put(nombre,
                            produccionPorTrabajador.getOrDefault(nombre, 0) + 1);
                } finally {
                    lock.unlock();
                }

            } catch (InterruptedException e) {
                System.err.println("⚠️ " + nombre + " fue interrumpido.");
            } finally {
                // 🔓 Libera el semáforo
                enProduccion.decrementAndGet();
                semaforo.release();
            }
        }

        System.out.println("🏁 " + nombre + " terminó su jornada laboral.");
    }

    // 📊 Estadísticas finales
    private static void mostrarEstadisticas() {
        System.out.println("\n===== 📊 ESTADÍSTICAS DE PRODUCCIÓN =====");

        int totalPorMapa = 0;

        lock.lock();
        try {
            for (String t : produccionPorTrabajador.keySet()) {
                int cantidad = produccionPorTrabajador.get(t);
                totalPorMapa += cantidad;
                System.out.println("👷 " + t + " fabricó " + cantidad + " productos.");
            }
        } finally {
            lock.unlock();
        }

        System.out.println("--------------------------------------------");
        System.out.println("📦 Total según mapa (sumando trabajadores): " + totalPorMapa);
        System.out.println("🧮 Total global contado (AtomicInteger): " + totalProductosFabricados.get());
        System.out.println("============================================\n");
    }
}
