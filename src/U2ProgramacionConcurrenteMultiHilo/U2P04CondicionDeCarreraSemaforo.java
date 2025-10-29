// Define el paquete o carpeta lógica donde se agrupa esta clase.
package U2ProgramacionConcurrenteMultiHilo;

// --- IMPORTACIONES ---
// Importa las clases necesarias de la biblioteca estándar de Java.

import java.util.ArrayList; // Para usar la clase ArrayList (una implementación de List)
import java.util.HashMap;   // Para usar la clase HashMap (una implementación de Map no segura para hilos)
import java.util.List;      // Para usar la interfaz List (para la lista de hilos)
import java.util.Map;       // Para usar la interfaz Map (para el mapa de contadores)
import java.util.concurrent.Semaphore; // Importa el Semáforo, un limitador de accesos concurrentes.
import java.util.concurrent.atomic.AtomicInteger; // Importa un Entero Atómico (seguro para hilos).
import java.util.concurrent.locks.ReentrantLock; // Importa el Lock (candado) manual.

// --- COMENTARIOS DE CLASE ---
// Semáforos permite acceder a varios hilos
// Deberes 23/10/2025: usar mapa normal (HashMap), en lugar de AtomicInteger, Integer. -> private static Map<String, Integer> mapa = new HashMap<>();
// Usar ReentrantLock en lugar de computeIfAbsent
// Al final hecho en clase

/**
 * Clase que demuestra el uso de Semáforos y ReentrantLock para controlar la concurrencia.
 * Implementa 'Runnable', lo que significa que sus instancias pueden ser ejecutadas por un Hilo (Thread).
 */
public class U2P04CondicionDeCarreraSemaforo implements Runnable {

    // --- VARIABLES ESTÁTICAS (COMPARTIDAS POR TODOS LOS HILOS) ---

    // Define el tiempo de finalización de la simulación (100 milisegundos desde ahora).
    // 'static' significa que todos los hilos comparten esta misma variable.
    private static long tiempoPrueba = System.currentTimeMillis() + 100;

    // Crea un Semáforo con 5 "permisos".
    // El 'true' (fairness) indica que los hilos adquirirán el permiso en el orden en que llegaron (FIFO).
    private static Semaphore semaforo = new Semaphore(5, true);

    // Un contador Atómico (seguro para hilos).
    // Se usa para verificar cuántos hilos están *simultáneamente* dentro de la sección crítica.
    private static AtomicInteger contador = new AtomicInteger();

    // Comentario original: ConcurrentHashMap es una clase ThreadSafe que garantiza la exclusión mutua
    // NOTA: Se usa un HashMap normal (NO es ThreadSafe) como pedía el ejercicio.
    // Este mapa guardará cuántas veces cada hilo (String) ha usado el semáforo (Integer).
    private static Map<String, Integer> mapa = new HashMap<>();

    // Un contador simple (NO es ThreadSafe).
    // Se usará para contar el número total de accesos al semáforo.
    // Debe ser protegido por un Lock.
    private static int contadorSemaforo = 0;

    // Un "candado" o Lock reentrante.
    // Lo usaremos para proteger las variables no-seguras para hilos: 'mapa' y 'contadorSemaforo'.
    private static ReentrantLock lock = new ReentrantLock();


    /**
     * Método principal (main) - El punto de entrada del programa.
     * Este código se ejecuta en el hilo "main".
     */
    public static void main(String[] args) {
        // Creamos la lista para hacer los joins de los threads y así controlar que el hilo haya acabado (bucle for each posterior)
        // Esta lista guardará todos los hilos que creemos.
        List<Thread> lista = new ArrayList<>();

        // Bucle para crear y lanzar 10 hilos.
        for (int i = 0; i < 10; i++) {
            // 1. Creamos una nueva "tarea" (una instancia de esta clase).
            // 2. Creamos un nuevo Hilo (Thread) pasándole la tarea y un nombre único (ej: "thread_0").
            // 3. Añadimos el hilo recién creado a nuestra lista.
            lista.add(new Thread(new U2P04CondicionDeCarreraSemaforo(), "thread_" + i));

            // Inicia el hilo (llama a su método run() de forma concurrente).
            lista.get(i).start();
        }

        // Bucle de "join" (espera).
        // El hilo "main" esperará aquí a que todos los hilos de la lista terminen.
        for (Thread h : lista) {
            try {
                // h.join() pausa el hilo "main" hasta que el hilo 'h' haya terminado su método run().
                h.join();
            } catch (InterruptedException e) {
                // Captura una excepción si el hilo "main" es interrumpido mientras espera.
                throw new RuntimeException(e);
            }
        }

        // --- SECCIÓN DE ESTADÍSTICAS (se ejecuta después de que TODOS los hilos terminen) ---
        System.out.println("*** Uso del semáforo por los threads.");

        // Acumulador local para sumar los valores del mapa y verificar el total.
        int acumulador = 0;

        // Itera sobre todas las "claves" (nombres de hilos) que se guardaron en el mapa.
        for (String n : mapa.keySet()) {
            // Acumula los conteos de cada hilo.
            acumulador += mapa.get(n);
            // Imprime cuántas veces cada hilo usó el semáforo.
            System.out.println("El thread " + n + " ha usado el semáforo " + mapa.get(n) + " veces.");
        }

        // Imprime el total contado por la variable 'contadorSemaforo'
        System.out.println("Usos de semáforo: " + contadorSemaforo);
        // Imprime el total contado sumando los valores del mapa.
        System.out.println("Total usos del semáforo: " + acumulador);
        // Ambos totales deberían coincidir si el Lock funcionó correctamente.
    }

    /**
     * Método run() - El código que ejecutará CADA HILO de forma concurrente.
     */
    @Override
    public void run() {
        // Obtiene el nombre del hilo actual (ej: "[thread_0]") y lo guarda en una variable local.
        String nombre = "[" + Thread.currentThread().getName() + "]";

        // Bucle principal del hilo: se ejecuta mientras no se haya superado el tiempo de prueba.
        while (System.currentTimeMillis() < tiempoPrueba) {
            try {
                // 1. ADQUIRIR PERMISO
                // El hilo intenta "adquirir" un permiso del semáforo.
                // Si hay permisos (semáforo > 0), sigue.
                // Si no (semáforo = 0), el hilo se BLOQUEA aquí y espera.
                semaforo.acquire();

                // --- INICIO DE LA SECCIÓN CRÍTICA (PROTEGIDA POR LOCK) ---
                // 2. BLOQUEAR EL LOCK MANUAL
                // Solo UN hilo a la vez puede pasar de esta línea.
                lock.lock();
                try {
                    // Actualizamos las variables compartidas NO-ThreadSafe.

                    // Incrementa el contador global simple.
                    contadorSemaforo++;

                    // Actualiza el mapa:
                    // 1. Obtiene el valor actual para 'nombre' (o 0 si no existe).
                    // 2. Le suma 1.
                    // 3. Lo vuelve a guardar (put) en el mapa.
                    mapa.put(nombre, mapa.getOrDefault(nombre, 0) + 1);

                } finally {
                    // 3. LIBERAR EL LOCK MANUAL
                    // Es VITAL hacer esto en un 'finally' para asegurar que el candado
                    // se libere siempre, incluso si ocurre un error dentro del 'try'.
                    lock.unlock();
                }
                // --- FIN DE LA SECCIÓN CRÍTICA (PROTEGIDA POR LOCK) ---


                // Línea comentada: era la forma alternativa de hacerlo con un ConcurrentHashMap.
                //mapa.computeIfAbsent(nombre, k -> new AtomicInteger()).incrementAndGet();

                // Imprime el valor que ACABA de insertar. (Esta lectura es fuera del lock).
                System.out.println(nombre + " Valor insertado en el mapa: " + mapa.get(nombre));

                // Incrementa el contador atómico y muestra cuántos hilos hay "dentro" ahora mismo.
                System.out.println(nombre + " Adquirido semáforo número: " + contador.incrementAndGet());

            } catch (InterruptedException e) {
                // Se lanza si el hilo es interrumpido mientras esperaba en 'semaforo.acquire()'.
                throw new RuntimeException(e);
            }

            // --- SECCIÓN DE VERIFICACIÓN Y LIBERACIÓN ---

            // Comprobación de seguridad:
            // Si el contador atómico (que cuenta hilos activos) es > 5...
            if (contador.get() > 5)
                // ...significa que el semáforo falló y dejó pasar a más hilos de los permitidos.
                throw new RuntimeException("Semáforo sobrepasado");

            // 4. DECREMENTAR CONTADOR ATÓMICO
            // El hilo indica que está a punto de salir.
            contador.decrementAndGet();

            // 5. LIBERAR PERMISO
            // El hilo devuelve el permiso al semáforo.
            // Esto permite que otro hilo que estaba bloqueado en 'acquire()' pueda entrar.
            semaforo.release();

            // Mensaje de depuración.
            System.out.println(nombre + " Semáforo liberado");

        } // Fin del bucle while (se acabó el tiempo)

    } // Fin del método run()

} // Fin de la clase