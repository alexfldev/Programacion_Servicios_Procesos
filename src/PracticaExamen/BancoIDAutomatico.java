// Define el paquete o carpeta lógica donde se agrupa esta clase.
package PracticaExamen;

// Importaciones necesarias:
import java.util.*; // Para List, Map, HashMap, Random
import java.util.concurrent.Semaphore; // Para limitar el número de hilos que acceden a un recurso
import java.util.concurrent.atomic.AtomicInteger; // Para contadores seguros en hilos (thread-safe)
import java.util.concurrent.locks.ReentrantLock; // Para crear secciones críticas (bloqueo manual)

/**
 * 🏦 Ejercicio tipo examen:
 * Simulación de un banco multihilo donde varios cajeros atienden clientes.
 *
 * - Cada cliente tiene un ID único (usando AtomicInteger)
 * - Solo 3 ventanillas pueden atender a la vez (Semaphore)
 * - Evitamos condición de carrera usando ReentrantLock
 * - Al final se muestran estadísticas
 */
// La clase implementa "Runnable", lo que significa que su método run()
// puede ser ejecutado por un hilo (Thread).
public class BancoIDAutomatico implements Runnable {

    // ⏱️ Duración de la simulación (en milisegundos)
    // "static": es una variable de clase, compartida por todos los hilos.
    // "final": es una constante, no se puede cambiar.
    // System.currentTimeMillis() coge la hora actual y le suma 5 segundos.
    private static final long TIEMPO_SIMULACION = System.currentTimeMillis() + 5000;

    // 💼 Solo 3 ventanillas disponibles (máximo 3 hilos a la vez)
    // Un Semáforo inicializado con 3 "permisos".
    // "true" (fairness): los hilos que lleguen primero, entrarán primero.
    private static final Semaphore semaforo = new Semaphore(3, true);

    // 🧮 Generador de ID único para cada cliente (seguro entre hilos)
    // "AtomicInteger" garantiza que la operación "incrementar" sea atómica,
    // es decir, dos hilos no obtendrán el mismo ID.
    private static final AtomicInteger generadorID = new AtomicInteger(0);

    // 🧾 Contadores globales atómicos (thread-safe)
    private static final AtomicInteger totalClientesAtendidos = new AtomicInteger(0);
    private static final AtomicInteger ventanillasOcupadas = new AtomicInteger(0); // Para control

    // 📊 Mapa: cuántos clientes atendió cada cajero
    // Un HashMap NO es "thread-safe". Si varios hilos escriben en él a la vez,
    // puede corromperse. Por eso necesitaremos un "Lock".
    private static final Map<String, Integer> clientesPorCajero = new HashMap<>();

    // 🔒 Lock para proteger el acceso al mapa "clientesPorCajero"
    // "ReentrantLock" es un candado manual.
    private static final ReentrantLock lock = new ReentrantLock();

    // --- PUNTO DE ENTRADA DEL PROGRAMA ---
    public static void main(String[] args) {
        System.out.println("🏦 Iniciando simulación del banco con IDs automáticos...\n");

        // Lista para guardar los threads (cajeros) y poder esperar a que terminen
        List<Thread> cajeros = new ArrayList<>();

        // Creamos 8 cajeros (8 hilos)
        for (int i = 1; i <= 8; i++) {
            // Creamos un hilo.
            // Le pasamos una "tarea": una *nueva instancia* de BancoIDAutomatico.
            // Le ponemos un nombre (ej: "Cajero_1")
            Thread cajero = new Thread(new BancoIDAutomatico(), "Cajero_" + i);
            cajeros.add(cajero); // Lo guardamos en la lista
            cajero.start(); // Iniciamos el hilo. Ahora empieza a ejecutar su método run().
        }

        // Esperar a que todos los cajeros terminen su jornada
        // El hilo "main" (este) se queda aquí esperando.
        for (Thread t : cajeros) {
            try {
                // t.join() pausa el hilo "main" hasta que el hilo 't' haya terminado
                // (es decir, hasta que su método run() acabe).
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Error esperando al hilo: " + t.getName());
            }
        }

        // Cuando todos los hilos han terminado (han salido de join),
        // el hilo "main" continúa y muestra las estadísticas.
        mostrarEstadisticas();
    }

    // --- CÓDIGO QUE EJECUTARÁ CADA HILO (CAJERO) ---
    @Override
    public void run() {
        // Cada hilo representa un cajero
        String nombre = Thread.currentThread().getName(); // "Cajero_X"
        Random random = new Random(); // Para simular tiempos de atención

        // Mientras dure la simulación (mientras la hora actual sea menor al tiempo final)
        while (System.currentTimeMillis() < TIEMPO_SIMULACION) {
            try {
                // 1. PEDIR PERMISO (Adquirir una ventanilla)
                // El hilo intenta "adquirir" un permiso del semáforo.
                // Si hay permisos (semáforo > 0), lo coge y sigue.
                // Si no hay permisos (semáforo = 0), el hilo se BLOQUEA aquí y espera.
                semaforo.acquire();
                ventanillasOcupadas.incrementAndGet(); // (Atómico)

                // --- INICIO DE LA SECCIÓN CRÍTICA (VENTANILLA OCUPADA) ---

                // 2. CREAR CLIENTE
                // incrementAndGet() suma 1 y devuelve el nuevo valor de forma atómica.
                int idCliente = generadorID.incrementAndGet();
                System.out.println(nombre + " atendiendo al cliente con ID: " + idCliente
                        + " [Ventanillas: " + ventanillasOcupadas.get() + "]");

                // 3. SIMULAR TIEMPO DE ATENCIÓN
                // El hilo se "duerme" un tiempo aleatorio entre 200 y 400 ms.
                Thread.sleep(200 + random.nextInt(200));

                // 4. CLIENTE ATENDIDO (Actualizar contadores)
                totalClientesAtendidos.incrementAndGet(); // (Atómico)

                // 5. ACTUALIZAR MAPA (ESTADÍSTICA POR CAJERO)
                // Esta es una zona crítica porque el HashMap NO es thread-safe.
                lock.lock(); // Echamos el candado. Solo 1 hilo puede estar aquí.
                try {
                    // getOrDefault(nombre, 0) obtiene el valor actual del cajero,
                    // o 0 si es la primera vez que atiende a alguien.
                    clientesPorCajero.put(nombre, clientesPorCajero.getOrDefault(nombre, 0) + 1);
                } finally {
                    // Es VITAL liberar el candado en un 'finally'
                    // para asegurar que se libere incluso si hay un error.
                    lock.unlock();
                }

                System.out.println("✅ " + nombre + " terminó con el cliente ID " + idCliente);

            } catch (InterruptedException e) {
                // Esto pasa si el hilo es "interrumpido" (por ej. desde fuera)
                // mientras estaba en sleep() o en acquire().
                System.err.println("⚠️ " + nombre + " interrumpido.");
            } finally {
                // --- FIN DE LA SECCIÓN CRÍTICA ---

                // 6. LIBERAR VENTANILLA
                // Este bloque 'finally' se ejecuta SIEMPRE,
                // tanto si el 'try' ha ido bien como si ha fallado.
                // Es crucial para evitar "fugas" de permisos.
                ventanillasOcupadas.decrementAndGet(); // (Atómico)
                semaforo.release(); // El hilo devuelve el permiso al semáforo.
                // Ahora, otro hilo que estaba esperando en acquire() puede entrar.
            }
        } // Fin del bucle while (se acabó el tiempo)

        System.out.println("🏁 " + nombre + " terminó su jornada laboral.");
    }

    // ---------------------------------------------------------------

    // 📊 Método estático para mostrar estadísticas al final
    private static void mostrarEstadisticas() {
        System.out.println("\n===== 📊 ESTADÍSTICAS DEL BANCO =====");

        int totalMapa = 0; // Contador local

        // Bloqueamos el candado para LEER el mapa de forma segura.
        // Si no lo bloqueamos, podríamos leerlo mientras otro hilo
        // (que se haya retrasado) todavía está escribiendo en él.
        lock.lock();
        try {
            // Recorremos todas las "claves" (nombres de cajero) del mapa
            for (String cajero : clientesPorCajero.keySet()) {
                int cantidad = clientesPorCajero.get(cajero); // Obtenemos el valor
                totalMapa += cantidad; // Sumamos al total local
                System.out.println("👨‍💼 " + cajero + " atendió a " + cantidad + " clientes.");
            }
        } finally {
            lock.unlock(); // Liberamos el candado
        }

        System.out.println("---------------------------------------------");
        // El total sumado del mapa
        System.out.println("🧾 Total clientes (según mapa): " + totalMapa);
        // El total del contador atómico global
        // (Deberían coincidir)
        System.out.println("🚀 Total global contado: " + totalClientesAtendidos.get());
        System.out.println("=============================================\n");
    }
}