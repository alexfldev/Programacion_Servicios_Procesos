// Define el paquete o carpeta lógica donde se agrupa esta clase.
package PracticaExamen;

// Importaciones necesarias para colecciones (Map), concurrencia (concurrent.*) y contadores atómicos (atomic.*).
// Estas importaciones ahora sirven para la clase principal Y sus clases internas.
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 🏭 Clase principal: fábrica de coches con empleados concurrentes.
 * ESTA VERSIÓN CONTIENE TODAS LAS CLASES (Coche y Empleado) DENTRO.
 */
public class FabricaCoche6 {

    // -----------------------------------------------------------------
    // --- CLASE INTERNA 1: COCHE ---
    // -----------------------------------------------------------------
    /**
     * 🚗 Representa un coche fabricado.
     * Al ser "static class", es una clase interna que pertenece a FabricaCoche6
     * pero no necesita una instancia de ella para ser creada.
     */
    static class Coche6 {
        // Generador de IDs "thread-safe" (seguro para hilos).
        // "static" significa que es compartido por todas las instancias de Coche6.
        // "AtomicInteger" garantiza que cada coche reciba un ID único, incluso si
        // se crean desde hilos (Empleados) diferentes al mismo tiempo.
        private static final AtomicInteger generadorId = new AtomicInteger(0);

        // Atributos "final" porque no cambian una vez que el coche es creado.
        private final int id;                 // ID único del coche
        private final String modelo;          // Modelo (ej: "Sedán", "SUV")
        private final String empleado;        // Empleado que lo fabricó
        private final int piezasUsadas;       // Cantidad aleatoria de piezas
        private final long tiempoProduccion;  // Tiempo aleatorio que tardó (en ms)

        // Constructor del coche. Se llama al hacer "new Coche6(...)".
        public Coche6(String modelo, String empleado) {
            // Asigna el próximo ID único disponible (ej: 0 -> 1, 1 -> 2, etc.) de forma atómica.
            this.id = generadorId.incrementAndGet();
            this.modelo = modelo;
            this.empleado = empleado;

            // Asigna valores aleatorios usando "ThreadLocalRandom", que es la
            // forma recomendada de generar aleatorios en entornos concurrentes.
            this.piezasUsadas = ThreadLocalRandom.current().nextInt(50, 151); // Entre 50 y 150
            this.tiempoProduccion = ThreadLocalRandom.current().nextLong(500, 2000); // Entre 500ms y 1999ms
        }

        // Método que simula el tiempo que se tarda en fabricar el coche.
        public void fabricar() {
            try {
                // "Duerme" el hilo actual (el del empleado) durante el tiempo de producción asignado.
                Thread.sleep(tiempoProduccion);
            } catch (InterruptedException e) {
                // Si el hilo es "interrumpido" mientras dormía (ej. si el programa se cierra).
                // Buena práctica: restaurar el estado de interrupción del hilo.
                Thread.currentThread().interrupt();
            }
        }

        // --- Métodos "getter" para acceder a los datos privados del coche ---
        public int getId() { return id; }
        public String getModelo() { return modelo; }
        public String getEmpleado() { return empleado; }
        public int getPiezasUsadas() { return piezasUsadas; }
        public long getTiempoProduccion() { return tiempoProduccion; }

        // Sobrescribe el método toString() para dar una representación en texto del coche.
        // Es útil para imprimir el objeto (ej: System.out.println(coche)).
        @Override
        public String toString() {
            return String.format("Coche{id=%d, modelo='%s', empleado='%s', piezas=%d, tiempo=%dms}",
                    id, modelo, empleado, piezasUsadas, tiempoProduccion);
        }
    } // --- FIN DE LA CLASE INTERNA COCHE6 ---


    // -----------------------------------------------------------------
    // --- CLASE INTERNA 2: EMPLEADO ---
    // -----------------------------------------------------------------
    /**
     * 👷 Clase que representa un empleado (tarea concurrente) que fabrica coches.
     * Implementa "Runnable", lo que significa que su método run() define una tarea
     * que puede ser ejecutada por un hilo (un "trabajador" del ExecutorService).
     * También es "static" por la misma razón que Coche6.
     */
    static class Empleado6 implements Runnable {
        private final String nombre;
        private final String modeloAsignado;
        private final int cantidadCoches; // Cuántos coches debe fabricar este empleado

        // Referencia al mapa GLOBAL donde se guardarán los coches fabricados.
        // Es el *mismo* mapa para todos los empleados, por eso debe ser "thread-safe".
        private final Map<Integer, Coche6> registroGlobal;

        // Constructor para inicializar al empleado con sus datos y la tarea.
        public Empleado6(String nombre, String modeloAsignado, int cantidadCoches, Map<Integer, Coche6> registroGlobal) {
            this.nombre = nombre;
            this.modeloAsignado = modeloAsignado;
            this.cantidadCoches = cantidadCoches;
            this.registroGlobal = registroGlobal;
        }

        // Este es el "corazón" del Runnable. Es el código que ejecutará el hilo del pool.
        @Override
        public void run() {
            // Bucle para fabricar la cantidad de coches asignada (ej: 5 coches).
            for (int i = 0; i < cantidadCoches; i++) {
                // 1. Crea un nuevo objeto Coche (llamando al constructor de Coche6).
                //    Puede acceder a Coche6 porque está en la misma clase "padre" (FabricaCoche6).
                Coche6 coche = new Coche6(modeloAsignado, nombre);

                // 2. Llama al método que "duerme" el hilo (simula el trabajo de fabricación).
                coche.fabricar();

                // 3. Guarda el coche fabricado en el mapa global.
                // Esta operación es "thread-safe" porque el mapa que pasamos en main
                // es un ConcurrentHashMap.
                registroGlobal.put(coche.getId(), coche);

                // 4. Imprime un mensaje de progreso (formateado).
                System.out.printf("👷 %-8s fabricó -> %s%n", nombre, coche);
            }
            // El hilo termina su bucle e informa que ha completado su tarea.
            System.out.println("✅ " + nombre + " terminó de fabricar " + cantidadCoches + " coches.");
        }
    } // --- FIN DE LA CLASE INTERNA EMPLEADO6 ---


    // -----------------------------------------------------------------
    // --- MÉTODO PRINCIPAL (MAIN) ---
    // -----------------------------------------------------------------

    // Punto de entrada del programa.
    // "throws InterruptedException" es necesario por el 'fabrica.awaitTermination'.
    public static void main(String[] args) throws InterruptedException {

        // 1. CREAR ALMACÉN GLOBAL
        // Mapa donde se guardarán TODOS los coches fabricados por TODOS los empleados.
        // Se usa "ConcurrentHashMap" porque es una implementación de Map "thread-safe".
        // Varios hilos (empleados) pueden escribir (put) en él a la vez sin conflictos.
        // Ahora usamos la clase interna Coche6 (FabricaCoche6.Coche6)
        Map<Integer, Coche6> registroCoches = new ConcurrentHashMap<>();

        // 2. CREAR EL POOL DE HILOS (LA FÁBRICA)
        // Crea un "pool de hilos" (un grupo de trabajadores).
        // "newFixedThreadPool(4)" significa que habrá 4 hilos (trabajadores) disponibles
        // para ejecutar las tareas (Empleados) en paralelo.
        ExecutorService fabrica = Executors.newFixedThreadPool(4);

        // 3. ASIGNAR TAREAS (CONTRATAR EMPLEADOS)
        // Asigna las tareas (los 4 Empleados) al pool de hilos.
        // Ahora usamos la clase interna Empleado6 (FabricaCoche6.Empleado6)
        // Todos los empleados reciben la referencia al *mismo* mapa 'registroCoches'.
        fabrica.execute(new Empleado6("Carlos", "Sedán", 5, registroCoches));
        fabrica.execute(new Empleado6("Lucía", "SUV", 5, registroCoches));
        fabrica.execute(new Empleado6("Miguel", "Deportivo", 5, registroCoches));
        fabrica.execute(new Empleado6("Ana", "Camioneta", 5, registroCoches));

        // 4. ESPERAR A QUE TERMINEN
        // Inicia el apagado "ordenado" del pool. No acepta nuevas tareas.
        // Los hilos que están trabajando (fabricando coches) terminarán sus tareas.
        fabrica.shutdown();

        // El hilo "main" (este) se bloquea y espera aquí.
        // Espera a que TODOS los hilos del pool terminen sus tareas,
        // o hasta un máximo de 10 segundos.
        fabrica.awaitTermination(10, TimeUnit.SECONDS);

        // 5. CALCULAR ESTADÍSTICAS (CUANDO YA HAN TERMINADO)
        // Este código solo se ejecuta DESPUÉS de que 'awaitTermination' haya terminado.
        // En este punto, todos los hilos de empleados han terminado su trabajo.

        System.out.println("\n===== 📊 ESTADÍSTICAS FINALES =====");

        // Obtenemos el número total de coches fabricados (el tamaño del mapa).
        int totalCoches = registroCoches.size();

        // Se usa la API de Streams de Java 8 para calcular estadísticas.
        // 1. .values(): Obtiene una colección de todos los objetos Coche6 del mapa.
        // 2. .stream(): Convierte la colección en un "flujo" de datos.
        // 3. .mapToInt(Coche6::getPiezasUsadas): Transforma el flujo de Coches en un flujo de Integers (las piezas).
        // 4. .sum(): Suma todos los valores del flujo de Integers.
        int totalPiezas = registroCoches.values().stream()
                .mapToInt(Coche6::getPiezasUsadas)
                .sum();

        // Cálculo similar para el tiempo promedio.
        // .mapToLong(): Convierte a flujo de Longs (tiempos).
        // .average(): Calcula el promedio del flujo.
        // .orElse(0): Devuelve 0 si el mapa estuviera vacío (para evitar errores).
        double tiempoPromedio = registroCoches.values().stream()
                .mapToLong(Coche6::getTiempoProduccion)
                .average()
                .orElse(0);

        // Creamos dos nuevos mapas para agrupar los resultados.
        // (Aquí podrían ser HashMaps normales, ya que solo el hilo "main" los usa).
        Map<String, Long> produccionPorModelo = new ConcurrentHashMap<>();
        Map<String, Long> produccionPorEmpleado = new ConcurrentHashMap<>();

        // Recorremos cada coche 'c' en el registro total.
        registroCoches.values().forEach(c -> {
            // .merge() es un método útil para contar/agrupar.
            // Para la clave (ej: "Sedán"), añade 1L. Si ya existía, suma el valor antiguo + 1L.
            // 'Long::sum' es una referencia al método que suma dos números Long.
            produccionPorModelo.merge(c.getModelo(), 1L, Long::sum);

            // Hace lo mismo, pero agrupando por el nombre del empleado.
            produccionPorEmpleado.merge(c.getEmpleado(), 1L, Long::sum);
        });

        // 6. MOSTRAR RESULTADOS
        // Imprime todos los resultados finales de la simulación.
        System.out.println("Total coches producidos: " + totalCoches);
        System.out.println("Total piezas utilizadas: " + totalPiezas);
        // 'printf' permite formatear la salida (ej: "%.2f" para 2 decimales).
        System.out.printf("Tiempo promedio de producción: %.2f ms%n", tiempoPromedio);

        System.out.println("\nProducción por modelo:");
        // Recorre el mapa de modelos e imprime cada par (clave, valor).
        produccionPorModelo.forEach((modelo, cantidad) ->
                System.out.println(" - " + modelo + ": " + cantidad + " unidades"));

        System.out.println("\nProducción por empleado:");
        // Recorre el mapa de empleados e imprime cada par (clave, valor).
        produccionPorEmpleado.forEach((empleado, cantidad) ->
                System.out.println(" - " + empleado + ": " + cantidad + " coches fabricados"));
    }
}