package PracticasExamen;

// Define el paquete o carpeta lógica donde se agrupa esta clase.


// Importaciones necesarias:
import java.util.*; // Para List, Map, HashMap, Random
import java.util.concurrent.Semaphore; // Para limitar el número de hilos que acceden a un recurso
import java.util.concurrent.atomic.AtomicInteger; // Para contadores seguros en hilos (thread-safe)
import java.util.concurrent.locks.ReentrantLock; // Para crear secciones críticas (bloqueo manual)

/**
 * 🏦 Ejercicio tipo examen:
 * Simulación de un banco multihilo donde varios cajeros atienden clientes.
 *
 * - Cada cajero tiene un nombre (Ej: "Cajero_1") y un ID numérico (Ej: 1)
 * - Cada cliente tiene un ID único (usando AtomicInteger)
 * - Solo 3 ventanillas pueden atender a la vez (Semaphore)
 * - Evitamos condición de carrera usando ReentrantLock
 * - Al final se muestran estadísticas
 */
// La clase implementa "Runnable", lo que significa que su método run()
// puede ser ejecutado por un hilo (Thread).
public class BancoIdauto implements Runnable {

    // ⏱️ Duración de la simulación (en milisegundos)
    private static final long TIEMPO_SIMULACION = System.currentTimeMillis() + 5000;

    // 💼 Solo 3 ventanillas disponibles (máximo 3 hilos a la vez)
    private static final Semaphore semaforo = new Semaphore(3, true);

    // 🧮 Generador de ID único para cada cliente (seguro entre hilos)
    private static final AtomicInteger generadorID = new AtomicInteger(0);

    // 🧾 Contadores globales atómicos (thread-safe)
    private static final AtomicInteger totalClientesAtendidos = new AtomicInteger(0);
    private static final AtomicInteger ventanillasOcupadas = new AtomicInteger(0); // Para control

    // 📊 Mapa: cuántos clientes atendió cada cajero (la clave es el nombre)
    private static final Map<String, Integer> clientesPorCajero = new HashMap<>();

    // 🔒 Lock para proteger el acceso al mapa "clientesPorCajero"
    private static final ReentrantLock lock = new ReentrantLock();

    // --- Variables de Instancia (propias de cada cajero) ---

    // <-- MODIFICADO: Campo para guardar el ID único de este cajero
    private final int idCajero;

    // <-- MODIFICADO: Constructor para recibir el ID del cajero
    /**
     * Crea una nueva tarea de cajero.
     * @param idCajero El ID numérico para este trabajador/cajero.
     */
    public BancoIdauto(int idCajero) {
        this.idCajero = idCajero;
    }


    // --- PUNTO DE ENTRADA DEL PROGRAMA ---
    public static void main(String[] args) {
        System.out.println("🏦 Iniciando simulación del banco con IDs automáticos...\n");

        // Lista para guardar los threads (cajeros) y poder esperar a que terminen
        List<Thread> cajeros = new ArrayList<>();

        // Creamos 8 cajeros (8 hilos)
        for (int i = 1; i <= 8; i++) {
            // Creamos un hilo.
            // Le pasamos una "tarea": una *nueva instancia* de BancoIDAutomatico
            // Y LE PASAMOS EL ID (i) a su constructor.
            // También le ponemos un nombre (ej: "Cajero_1")

            // <-- MODIFICADO: Pasamos 'i' al constructor
            Thread cajero = new Thread(new BancoIdauto(i), "Cajero_" + i);

            cajeros.add(cajero); // Lo guardamos en la lista
            cajero.start(); // Iniciamos el hilo. Ahora empieza a ejecutar su método run().
        }

        // Esperar a que todos los cajeros terminen su jornada
        for (Thread t : cajeros) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Error esperando al hilo: " + t.getName());
            }
        }

        // Cuando todos los hilos han terminado, mostramos estadísticas.
        mostrarEstadisticas();
    }

    // --- CÓDIGO QUE EJECUTARÁ CADA HILO (CAJERO) ---
    @Override
    public void run() {
        // Cada hilo representa un cajero
        String nombre = Thread.currentThread().getName(); // "Cajero_X"
        // 'this.idCajero' ya tiene el ID numérico (ej: 1)
        Random random = new Random(); // Para simular tiempos de atención

        // Mientras dure la simulación
        while (System.currentTimeMillis() < TIEMPO_SIMULACION) {
            try {
                // 1. PEDIR PERMISO (Adquirir una ventanilla)
                semaforo.acquire();
                ventanillasOcupadas.incrementAndGet();

                // --- INICIO DE LA SECCIÓN CRÍTICA (VENTANILLA OCUPADA) ---

                // 2. CREAR CLIENTE
                int idCliente = generadorID.incrementAndGet();

                // <-- MODIFICADO: Usamos this.idCajero en el mensaje
                System.out.println(nombre + " (ID: " + this.idCajero + ") atendiendo al cliente con ID: " + idCliente
                        + " [Ventanillas: " + ventanillasOcupadas.get() + "]");

                // 3. SIMULAR TIEMPO DE ATENCIÓN
                Thread.sleep(200 + random.nextInt(200));

                // 4. CLIENTE ATENDIDO (Actualizar contadores)
                totalClientesAtendidos.incrementAndGet();

                // 5. ACTUALIZAR MAPA (ESTADÍSTICA POR CAJERO)
                lock.lock();
                try {
                    // Usamos 'nombre' ("Cajero_X") como clave en el mapa
                    clientesPorCajero.put(nombre, clientesPorCajero.getOrDefault(nombre, 0) + 1);
                } finally {
                    lock.unlock();
                }

                // <-- MODIFICADO: Usamos this.idCajero en el mensaje
                System.out.println("✅ " + nombre + " (ID: " + this.idCajero + ") terminó con el cliente ID " + idCliente);

            } catch (InterruptedException e) {
                System.err.println("⚠️ " + nombre + " (ID: " + this.idCajero + ") interrumpido.");
            } finally {
                // --- FIN DE LA SECCIÓN CRÍTICA ---

                // 6. LIBERAR VENTANILLA
                ventanillasOcupadas.decrementAndGet();
                semaforo.release();
            }
        } // Fin del bucle while

        // <-- MODIFICADO: Usamos this.idCajero en el mensaje
        System.out.println("🏁 " + nombre + " (ID: " + this.idCajero + ") terminó su jornada laboral.");
    }

    // ---------------------------------------------------------------

    // 📊 Método estático para mostrar estadísticas al final
    private static void mostrarEstadisticas() {
        System.out.println("\n===== 📊 ESTADÍSTICAS DEL BANCO =====");

        int totalMapa = 0;

        lock.lock();
        try {
            // Ordenamos las claves (Cajero_1, Cajero_10, Cajero_2...)
            // para que se muestren en orden numérico (Cajero_1, Cajero_2, Cajero_10...)
            List<String> nombresCajerosOrdenados = new ArrayList<>(clientesPorCajero.keySet());
            Collections.sort(nombresCajerosOrdenados);

            // Recorremos las claves ya ordenadas
            for (String nombreCajero : nombresCajerosOrdenados) { // ej: "Cajero_1"
                int cantidad = clientesPorCajero.get(nombreCajero);
                totalMapa += cantidad;

                // <-- MODIFICADO: Extraemos el ID del nombre para mostrarlo
                // Hacemos split en "Cajero_1" para quedarnos con el "1"
                String idStr = nombreCajero.split("_")[1];

                System.out.println("👨‍💼 " + nombreCajero + " (ID: " + idStr + ") atendió a " + cantidad + " clientes.");
            }
        } finally {
            lock.unlock(); // Liberamos el candado
        }

        System.out.println("---------------------------------------------");
        System.out.println("🧾 Total clientes (según mapa): " + totalMapa);
        System.out.println("🚀 Total global contado: " + totalClientesAtendidos.get());
        System.out.println("=============================================\n");
    }
}