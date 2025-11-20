// Define el paquete o carpeta lógica donde se agrupa esta clase.
package Examen1ev.PracticasExamen;


// Importaciones necesarias:
import java.util.*; // Para List, Map, HashMap, ArrayList, Random
import java.util.concurrent.Semaphore; // Para limitar el número de hilos que acceden a un recurso
import java.util.concurrent.atomic.AtomicInteger; // Para contadores seguros en hilos (thread-safe)
import java.util.concurrent.locks.ReentrantLock; // Para crear secciones críticas (bloqueo manual)

/**
 * 🏭 Simulación: cada trabajador (hilo) fabrica una cantidad específica de coches.
 *
 * Ejemplo tipo examen:
 * - Control de concurrencia con semáforo (limitar líneas de montaje)
 * - Uso de AtomicInteger (contadores globales seguros)
 * - Uso de ReentrantLock (proteger el mapa de estadísticas)
 * - Estadísticas al final
 */
// La clase implementa "Runnable", lo que significa que su método run() 
// puede ser ejecutado por un hilo (Thread).
public class FabricaPorCantidad implements Runnable {

    // --- RECURSOS Y CONTADORES COMPARTIDOS (static) ---

    // 🚦 Semáforo: Limita cuántos hilos pueden ejecutar una sección de código a la vez.
    // new Semaphore(3, true) -> 3 "permisos" (líneas de montaje).
    // "true" (fairness): los hilos que lleguen primero, entrarán primero.
    private static final Semaphore semaforo = new Semaphore(3, true);

    // 🧮 Contadores globales "atómicos" (thread-safe).
    // Usar "int++" en hilos daría resultados incorrectos (condición de carrera).
    // "AtomicInteger" garantiza que las operaciones (como incrementar) sean indivisibles.
    private static final AtomicInteger totalFabricado = new AtomicInteger(0); // Total de coches
    private static final AtomicInteger enProduccion = new AtomicInteger(0); // Cuántos hilos están trabajando AHORA

    // 📊 Registro por trabajador: Guardará cuántos coches ha hecho CADA hilo.
    // Un "HashMap" NO ES thread-safe. Si varios hilos escriben en él a la vez,
    // puede corromperse. Por eso necesitaremos un "Lock" para protegerlo.
    private static final Map<String, Integer> produccionPorTrabajador = new HashMap<>();

    // 🔒 Candado (Lock) para proteger el "produccionPorTrabajador".
    // "ReentrantLock" es un candado manual. Solo un hilo puede "tener" el candado a la vez.
    private static final ReentrantLock lock = new ReentrantLock();

    // 🔢 Mapa de asignaciones: Define cuántos coches debe fabricar cada trabajador.
    // Es "static" para que todos los hilos puedan leerlo.
    // No necesita "lock" porque se rellena en el "main" ANTES de arrancar los hilos
    // y después solo se "lee" (lo cual es seguro).
    private static final Map<String, Integer> asignaciones = new HashMap<>();


    // --- PUNTO DE ENTRADA DEL PROGRAMA ---
    public static void main(String[] args) {
        System.out.println("🏭 Iniciando simulación con cantidades por trabajador...\n");

        // Asignamos cuántos coches fabricará cada trabajador
        // Esto se hace en el hilo "main", ANTES de crear los hilos "trabajador".
        asignaciones.put("Trabajador_1", 4);
        asignaciones.put("Trabajador_2", 8);
        asignaciones.put("Trabajador_3", 6);
        asignaciones.put("Trabajador_4", 10);
        asignaciones.put("Trabajador_5", 3);
        asignaciones.put("Trabajador_6", 5);
        asignaciones.put("Trabajador_7", 9);
        asignaciones.put("Trabajador_8", 2);

        // Lista para guardar los hilos (trabajadores) y poder "esperar" a que terminen
        List<Thread> trabajadores = new ArrayList<>();

        // Creamos un hilo por cada trabajador definido en el mapa "asignaciones"
        for (String nombre : asignaciones.keySet()) {
            // Creamos un nuevo Hilo (Thread).
            // Le pasamos una "tarea": una *nueva instancia* de FabricaPorCantidad (un Runnable).
            // Le ponemos un "nombre": ("Trabajador_1", "Trabajador_2", etc.)
            Thread t = new Thread(new FabricaPorCantidad(), nombre);
            trabajadores.add(t); // Lo guardamos en la lista
            t.start(); // Iniciamos el hilo. Ahora empieza a ejecutar su método run().
        }

        // Esperamos que todos los hilos terminen (join)
        // El hilo "main" (este) se queda aquí esperando.
        for (Thread t : trabajadores) {
            try {
                // t.join() pausa el hilo "main" hasta que el hilo 't' haya terminado
                // (es decir, hasta que su método run() acabe).
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Error esperando al hilo: " + t.getName());
            }
        }

        // Cuando todos los hilos han terminado (han salido del bucle "join"),
        // el hilo "main" continúa y muestra las estadísticas.
        mostrarEstadisticas();
    }

    // --- CÓDIGO QUE EJECUTARÁ CADA HILO (TRABAJADOR) ---
    @Override
    public void run() {
        // Obtenemos el nombre del hilo actual (ej: "Trabajador_1")
        String nombre = Thread.currentThread().getName();
        // Obtenemos la cantidad de coches que DEBE fabricar este hilo.
        // (Lectura segura del mapa "asignaciones", ya que no se está modificando).
        int cantidadAsignada = asignaciones.get(nombre);

        // Bucle de trabajo: fabricar el número de coches asignado
        // (ej: si le tocaron 4, el bucle va de i=1 a i=4)
        for (int i = 1; i <= cantidadAsignada; i++) {
            try {
                // --- INICIO DE LA SECCIÓN CRÍTICA (CONTROLADA POR SEMÁFORO) ---

                // 1. Pedir permiso (Adquirir una línea de montaje)
                // El hilo intenta "adquirir" un permiso del semáforo.
                // Si hay permisos (semáforo > 0), lo coge y sigue.
                // Si no hay permisos (semáforo = 0, las 3 líneas están ocupadas),
                // el hilo se BLOQUEA aquí y espera.
                semaforo.acquire();

                // 2. Permiso concedido (estamos en una línea de montaje)
                enProduccion.incrementAndGet(); // Incremento atómico
                System.out.println("🔧 " + nombre + " fabricando coche " + i + "/" + cantidadAsignada
                        + " [Líneas ocupadas: " + enProduccion.get() + "]");

                // 3. Simular tiempo de trabajo (fabricación)
                // El hilo se "duerme" un tiempo aleatorio.
                // IMPORTANTE: Mientras duerme, SIGUE teniendo el permiso del semáforo.
                Thread.sleep(200 + new Random().nextInt(300)); // Simula entre 200 y 500 ms

                // 4. Coche terminado (Actualizar contadores)
                totalFabricado.incrementAndGet(); // Incremento atómico

                // 5. Actualizar el MAPA de estadísticas (sección crítica del Lock)
                lock.lock(); // Echamos el candado. Solo 1 hilo puede estar aquí a la vez.
                try {
                    // Esta operación (put) sobre el HashMap NO es segura sin el "lock".
                    // getOrDefault(nombre, 0) -> coge el valor actual, o 0 si no existe
                    produccionPorTrabajador.put(nombre,
                            produccionPorTrabajador.getOrDefault(nombre, 0) + 1);
                } finally {
                    // Es VITAL liberar el candado en un 'finally'
                    // para asegurar que se libere incluso si hay un error dentro del 'try'.
                    lock.unlock(); // Soltamos el candado
                }

                System.out.println("✅ " + nombre + " terminó coche " + i + "/" + cantidadAsignada);

            } catch (InterruptedException e) {
                // Esto pasa si el hilo es "interrumpido" (por ej. desde fuera)
                // mientras estaba en sleep() o en acquire().
                System.err.println("⚠️ " + nombre + " interrumpido.");
            } finally {
                // --- FIN DE LA SECCIÓN CRÍTICA (CONTROLADA POR SEMÁFORO) ---

                // 6. Liberar la línea de montaje (SIEMPRE)
                // Este bloque 'finally' se ejecuta SIEMPRE,
                // tanto si el 'try' ha ido bien como si ha fallado (catch).
                // Es crucial para evitar "fugas" de permisos del semáforo.
                enProduccion.decrementAndGet(); // Decremento atómico
                semaforo.release(); // El hilo devuelve el permiso al semáforo.
                // Ahora, otro hilo que estaba esperando en acquire() puede entrar.
            }
        } // Fin del bucle FOR (el trabajador ha hecho un coche más)

        // El trabajador ha terminado TODOS sus coches asignados
        System.out.println("🏁 " + nombre + " terminó sus " + cantidadAsignada + " coches asignados.");
    } // El método run() termina y el hilo "muere".

    // ---------------------------------------------------------------

    // 📊 Método estático para mostrar estadísticas al final
    // Es "static" porque se llama desde el método "static main".
    private static void mostrarEstadisticas() {
        System.out.println("\n===== 📊 ESTADÍSTICAS DE PRODUCCIÓN =====");
        int totalPorMapa = 0; // Contador local para verificar

        // Aunque los hilos trabajadores ya han terminado, es una buena práctica
        // usar el "lock" al leer el mapa, para asegurar la "visibilidad"
        // de los cambios hechos por otros hilos.
        lock.lock();
        try {
            // Recorremos el mapa de estadísticas
            for (String trabajador : produccionPorTrabajador.keySet()) {
                int num = produccionPorTrabajador.get(trabajador);
                totalPorMapa += num; // Sumamos al total local
                System.out.println("👷 " + trabajador + " fabricó " + num + " coches.");
            }
        } finally {
            lock.unlock(); // Liberamos el candado de lectura
        }

        System.out.println("--------------------------------------------");
        // Mostramos el total contado sumando los valores del mapa
        System.out.println("📦 Total según mapa: " + totalPorMapa);
        // Mostramos el total contado por el AtomicInteger
        System.out.println("🧮 Total global contado: " + totalFabricado.get());

        // NOTA: Ambos totales (totalPorMapa y totalFabricado) DEBEN ser iguales.
        // Si no lo fueran, tendríamos un error de concurrencia.

        System.out.println("============================================");
    }
}