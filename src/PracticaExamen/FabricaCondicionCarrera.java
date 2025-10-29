// Define el paquete o carpeta lógica donde se agrupa esta clase.
package PracticaExamen;

// Importaciones necesarias:
import java.util.*; // Para List, Map, HashMap, ArrayList, Random
import java.util.concurrent.Semaphore; // Para limitar el número de hilos que acceden a un recurso
import java.util.concurrent.atomic.AtomicInteger; // Para contadores seguros en hilos (thread-safe)
import java.util.concurrent.locks.ReentrantLock; // Para crear secciones críticas (bloqueo manual)

/**
 * 🏭 Simulación de una fábrica multihilo (versión mejorada tipo examen)
 *
 * ✅ Varios trabajadores (threads) fabrican productos.
 * ✅ Cada producto tiene un ID único generado con AtomicInteger.
 * ✅ Solo 3 trabajadores pueden usar la línea de montaje a la vez (Semaphore).
 * ✅ Se controla la condición de carrera con ReentrantLock.
 * ✅ Al final se muestran estadísticas completas.
 */
// La clase implementa "Runnable", lo que significa que su método run() 
// puede ser ejecutado por un hilo (Thread).
public class FabricaCondicionCarrera implements Runnable {

    // --- RECURSOS Y CONTADORES COMPARTIDOS (static) ---

    // ⏱️ Duración total de la simulación (en milisegundos)
    // "static final": es una constante compartida por todos los hilos.
    // Coge la hora actual y le suma 5 segundos (5000 ms).
    private static final long TIEMPO_SIMULACION = System.currentTimeMillis() + 5000;

    // 🚦 Semáforo que limita el número de líneas de montaje (máx 3 hilos a la vez)
    // new Semaphore(3, true) -> 3 "permisos".
    // "true" (fairness): los hilos que lleguen primero, entrarán primero.
    private static final Semaphore semaforo = new Semaphore(3, true);

    // 🧮 Contadores seguros (AtomicInteger evita condición de carrera)
    // Usar "int++" en un entorno multihilo daría resultados incorrectos.
    // "AtomicInteger" garantiza que las operaciones (como incrementar) sean indivisibles.
    private static final AtomicInteger totalProductosFabricados = new AtomicInteger(0);
    private static final AtomicInteger enProduccion = new AtomicInteger(0); // Para saber cuántos hilos están DENTRO del semáforo

    // 🔢 Generador de IDs únicos para productos
    // Asegura que cada producto tenga un ID único (1, 2, 3...) sin repeticiones.
    private static final AtomicInteger generadorID = new AtomicInteger(0);

    // 📊 Registro de producción por trabajador
    // Un "HashMap" NO ES thread-safe. Si varios hilos escriben en él a la vez,
    // puede corromperse. Por eso necesitaremos un "Lock" para protegerlo.
    private static final Map<String, Integer> produccionPorTrabajador = new HashMap<>();

    // 🔒 Lock para proteger el acceso al mapa compartido "produccionPorTrabajador"
    // "ReentrantLock" es un candado manual.
    private static final ReentrantLock lock = new ReentrantLock();


    // --- CLASE INTERNA ---
    /**
     * 🧱 Clase interna "static": representa un producto fabricado.
     * Es una simple clase de datos (POJO).
     */
    static class Producto {
        private final int id;         // ID único del producto
        private final String trabajador; // Nombre del hilo que lo fabricó

        // Constructor para crear el producto
        public Producto(int id, String trabajador) {
            this.id = id;
            this.trabajador = trabajador;
        }

        // Sobrescribe el método toString() para que al imprimir el objeto,
        // muestre un mensaje formateado y legible.
        @Override
        public String toString() {
            return "🆔 Producto " + id + " fabricado por " + trabajador;
        }
    }


    // --- PUNTO DE ENTRADA DEL PROGRAMA (HILO PRINCIPAL) ---
    public static void main(String[] args) {
        System.out.println("🏭 Iniciando simulación de la fábrica con IDs únicos...\n");

        // Lista para almacenar los threads (trabajadores) y poder esperar a que terminen.
        List<Thread> trabajadores = new ArrayList<>();

        // 🔁 Creamos 8 trabajadores (hilos)
        for (int i = 1; i <= 8; i++) {
            // Creamos un nuevo Hilo (Thread).
            // Le pasamos una "tarea": una *nueva instancia* de FabricaCondicionCarrera (un Runnable).
            // Le ponemos un "nombre": ("Trabajador_1", "Trabajador_2", etc.)
            Thread t = new Thread(new FabricaCondicionCarrera(), "Trabajador_" + i);
            trabajadores.add(t); // Lo guardamos en la lista
            t.start(); // Iniciamos el hilo. Ahora empieza a ejecutar su método run().
        }

        // ⏸️ Esperamos a que todos los hilos terminen
        // El hilo "main" (este) se queda aquí esperando.
        for (Thread t : trabajadores) {
            try {
                // t.join() pausa el hilo "main" hasta que el hilo 't' haya terminado
                // (es decir, hasta que su método run() acabe).
                t.join();
            } catch (InterruptedException e) {
                // Esto pasaría si el hilo "main" es interrumpido mientras espera.
                System.err.println("Error esperando al hilo: " + t.getName());
            }
        }

        // 🧾 Mostrar estadísticas finales
        // Esta línea solo se ejecuta DESPUÉS de que todos los hilos trabajadores
        // hayan terminado (gracias al bucle de "join").
        mostrarEstadisticas();
    }


    // --- CÓDIGO QUE EJECUTARÁ CADA HILO (TRABAJADOR) ---
    @Override
    public void run() {
        // Obtenemos el nombre del hilo actual (ej: "Trabajador_1")
        String nombre = Thread.currentThread().getName();
        // Cada hilo tendrá su propio generador de números aleatorios (para el sleep)
        Random random = new Random();

        // Bucle principal del trabajador:
        // El hilo seguirá trabajando mientras la hora actual sea menor que el tiempo final.
        while (System.currentTimeMillis() < TIEMPO_SIMULACION) {
            try {
                // --- INICIO DE LA SECCIÓN CRÍTICA (CONTROLADA POR SEMÁFORO) ---

                // 1. 🚦 El trabajador pide acceso a la línea de montaje
                // Intenta "adquirir" un permiso del semáforo.
                // Si hay permisos (semáforo > 0), lo coge y sigue.
                // Si no hay permisos (semáforo = 0, las 3 líneas están ocupadas),
                // el hilo se BLOQUEA aquí y espera.
                semaforo.acquire();

                // 2. Permiso concedido
                enProduccion.incrementAndGet(); // Incremento atómico
                System.out.println("🔧 " + nombre + " está fabricando un producto..."
                        + " [Líneas ocupadas: " + enProduccion.get() + "]");

                // 3. Simulamos tiempo de producción (trabajo)
                // El hilo se "duerme" un tiempo aleatorio (entre 200 y 499 ms).
                Thread.sleep(200 + random.nextInt(300));

                // 4. 🔢 Crear un nuevo producto con ID único
                // "incrementAndGet()" suma 1 y devuelve el nuevo valor (ej: 1, 2, 3...)
                // Es una operación atómica, por lo que es segura.
                int idProducto = generadorID.incrementAndGet();
                Producto nuevo = new Producto(idProducto, nombre); // Creamos el objeto

                System.out.println("✅ " + nuevo); // Imprime usando el método .toString()

                // 5. 🧮 Incrementamos el contador global (atómico)
                totalProductosFabricados.incrementAndGet();

                // 6. 🔒 Actualizamos el mapa protegido por el lock
                // Esta es la "condición de carrera" que protegemos.
                lock.lock(); // Echamos el candado. Solo 1 hilo puede estar aquí a la vez.
                try {
                    // Esta operación (put) sobre el HashMap NO es segura sin el "lock".
                    produccionPorTrabajador.put(nombre,
                            produccionPorTrabajador.getOrDefault(nombre, 0) + 1);
                } finally {
                    // Es VITAL liberar el candado en un 'finally'
                    // para asegurar que se libere incluso si hay un error dentro del 'try'.
                    lock.unlock(); // Soltamos el candado
                }

            } catch (InterruptedException e) {
                // Esto pasa si el hilo es "interrumpido" (por ej. desde fuera)
                // mientras estaba en sleep() o en acquire().
                System.err.println("⚠️ " + nombre + " fue interrumpido.");
            } finally {
                // --- FIN DE LA SECCIÓN CRÍTICA (CONTROLADA POR SEMÁFORO) ---

                // 7. 🔓 Libera el semáforo (la línea de montaje)
                // Este bloque 'finally' se ejecuta SIEMPRE,
                // tanto si el 'try' ha ido bien como si ha fallado (catch).
                // Es crucial para evitar "fugas" de permisos del semáforo.
                enProduccion.decrementAndGet(); // Decremento atómico
                semaforo.release(); // El hilo devuelve el permiso al semáforo.
                // Ahora, otro hilo que estaba esperando en acquire() puede entrar.
            }
        } // Fin del bucle while (se acabó el tiempo)

        System.out.println("🏁 " + nombre + " terminó su jornada laboral.");
    } // El método run() termina y el hilo "muere".


    // --- MÉTODO ESTÁTICO DE ESTADÍSTICAS ---
    /**
     * 📊 Muestra los resultados finales de la producción.
     * Se llama desde "main" cuando ya todos los hilos han terminado.
     */
    private static void mostrarEstadisticas() {
        System.out.println("\n===== 📊 ESTADÍSTICAS DE PRODUCCIÓN =====");

        int totalPorMapa = 0; // Contador local para verificar

        // Bloqueamos el candado (lock) para leer el mapa.
        // Aunque los hilos trabajadores ya han terminado, es una buena práctica
        // usar el "lock" al leer, para asegurar la "visibilidad"
        // de los cambios hechos por otros hilos (garantiza que leemos la última versión).
        lock.lock();
        try {
            // Recorremos el mapa de estadísticas, trabajador por trabajador
            for (String t : produccionPorTrabajador.keySet()) {
                int cantidad = produccionPorTrabajador.get(t); // Obtenemos lo que fabricó
                totalPorMapa += cantidad; // Sumamos al total local
                System.out.println("👷 " + t + " fabricó " + cantidad + " productos.");
            }
        } finally {
            lock.unlock(); // Liberamos el candado de lectura
        }

        System.out.println("--------------------------------------------");
        // Mostramos el total contado sumando los valores del mapa
        System.out.println("📦 Total según mapa (sumando trabajadores): " + totalPorMapa);
        // Mostramos el total contado por el AtomicInteger
        System.out.println("🧮 Total global contado (AtomicInteger): " + totalProductosFabricados.get());

        // NOTA: Ambos totales (totalPorMapa y totalProductosFabricados) DEBEN ser iguales.
        // Si no lo fueran, tendríamos un error de concurrencia.

        System.out.println("============================================\n");
    }
}