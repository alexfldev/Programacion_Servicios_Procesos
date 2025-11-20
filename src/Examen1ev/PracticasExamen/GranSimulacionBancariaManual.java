package Examen1ev.PracticasExamen;



// Importaciones necesarias: List, Map, Queue, Random, y todo el paquete concurrent
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 🏦 Simulación de un banco con 7 personas (recursos) que se transfieren
 * dinero entre ellas usando múltiples hilos (gestores).
 * * --- VERSIÓN MEJORADA CON TAREAS PREDEFINIDAS ---
 * * Los hilos (gestores) ya no inventan transferencias aleatorias, sino que
 * procesan una "cola de tareas" (Queue<Operacion>) que definimos en el main.
 * * Se evita el DEADLOCK (bloqueo mutuo) al establecer un "orden de bloqueo"
 * fijo (alfabético por nombre) al transferir entre dos personas.
 */
public class GranSimulacionBancariaManual {

    // --- CLASE INTERNA QUE REPRESENTA EL RECURSO ---
    /**
     * 💰 Representa a una persona con una cuenta bancaria.
     * Este objeto será el "recurso" compartido que los hilos intentarán bloquear.
     */
    static class Persona {
        // El nombre es 'final' porque no cambia y servirá como 'ID' para el bloqueo.
        private final String nombre;
        private double saldo;

        /**
         * Constructor para crear una persona con su saldo inicial.
         * @param nombre Nombre de la persona (ej: "Fulanito")
         * @param saldoInicial El dinero que tiene al empezar.
         */
        public Persona(String nombre, double saldoInicial) {
            this.nombre = nombre;
            this.saldo = saldoInicial;
        }

        /**
         * Obtiene el nombre (será la clave para ordenar los bloqueos).
         * @return El nombre de la persona.
         */
        public String getNombre() {
            return nombre;
        }

        /**
         * Ingresa dinero en la cuenta.
         * 'synchronized' asegura que solo un hilo pueda modificar el saldo a la vez.
         * @param cantidad Dinero a ingresar.
         */
        public synchronized void ingresar(double cantidad) {
            saldo += cantidad;
        }

        /**
         * Retira dinero de la cuenta, si hay fondos.
         * 'synchronized' para proteger el saldo.
         * @param cantidad Dinero a retirar.
         * @return true si la operación fue exitosa, false si no había saldo.
         */
        public synchronized boolean retirar(double cantidad) {
            if (saldo >= cantidad) {
                saldo -= cantidad;
                return true; // ¡Retirada exitosa!
            }
            return false; // No hay suficiente dinero
        }

        /**
         * Consulta el saldo actual.
         * 'synchronized' para asegurar que leemos un valor consistente.
         * @return El saldo actual.
         */
        public synchronized double getSaldo() {
            return saldo;
        }
    }

    // --- NUEVO "RECORD" PARA DEFINIR UNA TAREA ---
    /**
     * 📦 Un "record" simple para almacenar una transferencia predefinida.
     * Es una clase de datos inmutable.
     * Contiene quién envía, quién recibe y cuánto.
     */
    record Operacion(Persona origen, Persona destino, double cantidad) {}


    // --- MÉTODO CON LA LÓGICA ANTI-DEADLOCK (SIN CAMBIOS) ---
    /**
     * Realiza una transferencia de 'origen' a 'destino' evitando deadlocks.
     * Esta es la parte MÁS IMPORTANTE del ejercicio.
     *
     * @param origen La persona que envía el dinero.
     * @param destino La persona que recibe el dinero.
     * @param cantidad La cantidad a transferir.
     * @param gestor El nombre del hilo (gestor) que realiza la operación (para logs).
     */
    public static void transferir(Persona origen, Persona destino, double cantidad, String gestor) {

        // 1. Evitar transferencias a uno mismo
        if (origen.getNombre().equals(destino.getNombre())) {
            System.out.printf("ℹ️ (%s) %s intentó transferirse dinero a sí mismo.%n", gestor, origen.getNombre());
            return;
        }

        // --- 2. ESTRATEGIA ANTI-DEADLOCK: ORDENACIÓN DE RECURSOS ---
        // Se bloquean las cuentas por orden alfabético de nombre.
        Persona primero = (origen.getNombre().compareTo(destino.getNombre()) < 0) ? origen : destino;
        Persona segundo = (origen.getNombre().compareTo(destino.getNombre()) < 0) ? destino : origen;

        // 3. Adquirir los bloqueos (monitores) EN ORDEN.
        synchronized (primero) {
            // El hilo tiene el candado de 'primero'

            // Pausa breve para simular latencia y hacer más probable el deadlock (si existiera)
            try { Thread.sleep(1); } catch (InterruptedException e) {}

            synchronized (segundo) {
                // El hilo tiene AMBOS candados
                // --- INICIO DE LA SECCIÓN CRÍTICA ---

                if (origen.retirar(cantidad)) {
                    destino.ingresar(cantidad);
                    System.out.printf("💸 (%s) %s le dio %.2f€ a %s.%n",
                            gestor, origen.getNombre(), cantidad, destino.getNombre());
                } else {
                    System.out.printf("⚠️ (%s) %s intentó darle %.2f€ a %s, pero no tiene saldo.%n",
                            gestor, origen.getNombre(), cantidad, destino.getNombre());
                }

                // --- FIN DE LA SECCIÓN CRÍTICA ---
            } // 4. Se libera el candado de 'segundo'
        } // 5. Se libera el candado de 'primero'
    }

    // --- MÉTODO HELPER PARA MOSTRAR SALDOS ---
    /**
     * Muestra los saldos de todas las personas en la lista y calcula el total.
     * (Modificado para aceptar cualquier Colección, como los valores de un Map).
     * @param personas La colección de personas.
     * @return El saldo total sumado.
     */
    private static double mostrarSaldos(Collection<Persona> personas) {
        double total = 0;
        for (Persona p : personas) {
            double saldo = p.getSaldo();
            System.out.printf("  -> %-8s tiene %.2f€%n", p.getNombre() + ":", saldo);
            total += saldo;
        }
        return total;
    }

    // --- PUNTO DE ENTRADA DEL PROGRAMA (MODIFICADO) ---
    public static void main(String[] args) throws InterruptedException {
        System.out.println("🏦 Iniciando simulación de transferencias bancarias...");

        // 1. Crear las 7 personas (recursos) USANDO UN MAPA
        // Usamos un Mapa para acceder a ellas fácilmente por nombre.
        Map<String, Persona> mapaPersonas = new HashMap<>();
        mapaPersonas.put("Fulanito", new Persona("Fulanito", 10000));
        mapaPersonas.put("Carlos",   new Persona("Carlos", 2000));
        mapaPersonas.put("Ana",      new Persona("Ana", 5000));
        mapaPersonas.put("Beatriz",  new Persona("Beatriz", 7000));
        mapaPersonas.put("David",    new Persona("David", 3000));
        mapaPersonas.put("Elena",    new Persona("Elena", 15000));
        mapaPersonas.put("Miguel",   new Persona("Miguel", 8000));

        // Guardamos el saldo inicial total para comprobar que no perdemos dinero.
        final double saldoTotalInicial = 10000+2000+5000+7000+3000+15000+8000;

        // 2. Mostrar estado inicial
        System.out.println("👥 Lista de clientes inicial:");
        // mapaPersonas.values() devuelve una Colección con todas las Personas
        mostrarSaldos(mapaPersonas.values());
        System.out.printf("💰 Saldo total inicial: %.2f€%n%n", saldoTotalInicial);

        // 3. Crear la COLA DE TAREAS (¡Aquí defines tus transferencias!)
        // Usamos una ConcurrentLinkedQueue, que es una cola "thread-safe"
        // (segura para que varios hilos la usen a la vez).
        Queue<Operacion> colaDeTareas = new ConcurrentLinkedQueue<>();

        System.out.println("📬 Encolando transferencias predefinidas...");

        // --- MODIFICA AQUÍ LAS TRANSFERENCIAS QUE QUIERAS ---
        colaDeTareas.add(new Operacion(mapaPersonas.get("Fulanito"), mapaPersonas.get("Ana"), 200));
        colaDeTareas.add(new Operacion(mapaPersonas.get("Carlos"), mapaPersonas.get("David"), 200));

        // ¡Transferencias conflictivas para probar el Deadlock!
        colaDeTareas.add(new Operacion(mapaPersonas.get("Ana"), mapaPersonas.get("Fulanito"), 200)); // Ana devuelve a Fulanito
        colaDeTareas.add(new Operacion(mapaPersonas.get("Beatriz"), mapaPersonas.get("Miguel"), 500));
        colaDeTareas.add(new Operacion(mapaPersonas.get("Miguel"), mapaPersonas.get("Beatriz"), 300)); // Miguel devuelve a Beatriz

        // Añadimos más tareas para que los hilos tengan trabajo
        colaDeTareas.add(new Operacion(mapaPersonas.get("Elena"), mapaPersonas.get("Carlos"), 10000));
        colaDeTareas.add(new Operacion(mapaPersonas.get("David"), mapaPersonas.get("Ana"), 50));
        colaDeTareas.add(new Operacion(mapaPersonas.get("Fulanito"), mapaPersonas.get("Elena"), 2000));
        colaDeTareas.add(new Operacion(mapaPersonas.get("Miguel"), mapaPersonas.get("Carlos"), 150));
        colaDeTareas.add(new Operacion(mapaPersonas.get("Beatriz"), mapaPersonas.get("David"), 700));
        colaDeTareas.add(new Operacion(mapaPersonas.get("Ana"), mapaPersonas.get("Elena"), 300));
        colaDeTareas.add(new Operacion(mapaPersonas.get("Fulanito"), mapaPersonas.get("Carlos"), 100));

        // Intento de transferencia sin saldo
        colaDeTareas.add(new Operacion(mapaPersonas.get("Carlos"), mapaPersonas.get("Fulanito"), 50000));
        // ---------------------------------------------------

        System.out.println("...listas " + colaDeTareas.size() + " transferencias en la cola.\n");

        // 4. Crear los "gestores" (hilos) que procesarán la cola
        int numGestores = 4; // 4 hilos trabajando a la vez
        ExecutorService gestores = Executors.newFixedThreadPool(numGestores);

        long inicio = System.currentTimeMillis();

        // 5. Poner a los gestores a trabajar
        for (int i = 0; i < numGestores; i++) {
            // Damos un nombre a cada gestor
            final String nombreGestor = "Gestor-" + (i + 1);

            // submit() envía una tarea (un Runnable) al pool de hilos.
            gestores.submit(() -> {
                // Esta es la nueva lógica del gestor:
                // Mientras la cola de tareas no esté vacía...
                Operacion op;
                // poll() saca un elemento de la cola (o devuelve null si está vacía)
                // Esta operación es "atómica" y segura entre hilos.
                while ( (op = colaDeTareas.poll()) != null ) {

                    // ¡Tenemos una tarea! La ejecutamos.
                    transferir(op.origen(), op.destino(), op.cantidad(), nombreGestor);

                    // Simular un pequeño tiempo de "gestión"
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(50, 150));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                // Si salimos del bucle, es porque la cola está vacía (op == null).
                System.out.println("✅ " + nombreGestor + " ha terminado, no hay más tareas.");
            });
        }

        // 6. Esperar a que todos los gestores terminen
        gestores.shutdown(); // Decimos al pool que no acepte más tareas
        // El hilo "main" espera a que terminen las tareas encoladas
        gestores.awaitTermination(1, TimeUnit.MINUTES);

        long fin = System.currentTimeMillis();

        // 7. Mostrar resultados finales
        System.out.println("\n🏁 Simulación terminada en " + (fin - inicio) + "ms.");
        System.out.println("=============================================");
        System.out.println("📊 RESULTADOS FINALES DE SALDO");
        System.out.println("=============================================");

        // Mostramos saldos finales y calculamos el total
        double saldoTotalFinal = mostrarSaldos(mapaPersonas.values());

        // 8. Comprobación de integridad
        System.out.println("---------------------------------------------");
        System.out.printf("💰 Saldo total inicial: %.2f€%n", saldoTotalInicial);
        System.out.printf("💰 Saldo total final:   %.2f€%n", saldoTotalFinal);

        if (Math.abs(saldoTotalInicial - saldoTotalFinal) < 0.01) {
            System.out.println("✅ ¡El dinero se ha conservado! No se perdió ni se creó dinero.");
        } else {
            System.out.println("❌ ¡ERROR! El saldo total no coincide. Se ha perdido o creado dinero.");
        }
        System.out.println("=============================================");
    }
}