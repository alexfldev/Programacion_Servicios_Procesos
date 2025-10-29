package PracticaExamen;



import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 🏦 Simulación de un banco con 7 personas (recursos) que se transfieren
 * dinero entre ellas usando múltiples hilos (gestores).
 * * Se evita el DEADLOCK (bloqueo mutuo) al establecer un "orden de bloqueo"
 * fijo (alfabético por nombre) al transferir entre dos personas.
 */
public class GranSimulacionBancaria {

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

    // --- MÉTODO CON LA LÓGICA ANTI-DEADLOCK ---
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

        // 1. Evitar transferencias a uno mismo (no tiene sentido y complica el bloqueo)
        if (origen.getNombre().equals(destino.getNombre())) {
            System.out.printf("ℹ️ (%s) %s intentó transferirse dinero a sí mismo.%n", gestor, origen.getNombre());
            return;
        }

        // --- 2. ESTRATEGIA ANTI-DEADLOCK: ORDENACIÓN DE RECURSOS ---
        // Para evitar el deadlock (Fulanito -> Carlos vs Carlos -> Fulanito),
        // TODOS los hilos deben acordar un orden de bloqueo.
        // Usaremos el orden alfabético del nombre.

        // Determinamos quién es 'primero' y quién 'segundo' alfabéticamente.
        Persona primero = (origen.getNombre().compareTo(destino.getNombre()) < 0) ? origen : destino;
        Persona segundo = (origen.getNombre().compareTo(destino.getNombre()) < 0) ? destino : origen;

        // EJEMPLO:
        // Si Fulanito (hilo 1) transfiere a Carlos:
        //    primero = Carlos, segundo = Fulanito
        // Si Carlos (hilo 2) transfiere a Fulanito:
        //    primero = Carlos, segundo = Fulanito
        //
        // ¡Ambos hilos intentarán bloquear PRIMERO a Carlos y LUEGO a Fulanito!
        // El que llegue primero, gana. El otro espera. No hay deadlock.

        // 3. Adquirir los bloqueos (monitores) EN ORDEN.
        synchronized (primero) {
            // El hilo tiene el candado de 'primero' (ej: Carlos)

            // Pausa breve para simular latencia y hacer más probable el deadlock (si existiera)
            try { Thread.sleep(1); } catch (InterruptedException e) {}

            synchronized (segundo) {
                // El hilo tiene AMBOS candados (ej: Carlos y Fulanito)
                // --- INICIO DE LA SECCIÓN CRÍTICA ---

                // Ahora que tenemos ambos bloqueos, la transferencia es segura.
                if (origen.retirar(cantidad)) {
                    // El origen tenía dinero, así que lo retiramos...
                    destino.ingresar(cantidad); // ...y lo ingresamos en el destino.

                    // Imprimimos el resultado de la operación
                    System.out.printf("💸 (%s) %s le dio %.2f€ a %s.%n",
                            gestor, origen.getNombre(), cantidad, destino.getNombre());
                } else {
                    // El origen no tenía saldo suficiente
                    System.out.printf("⚠️ (%s) %s intentó darle %.2f€ a %s, pero no tiene saldo.%n",
                            gestor, origen.getNombre(), cantidad, destino.getNombre());
                }

                // --- FIN DE LA SECCIÓN CRÍTICA ---
            } // 4. Se libera el candado de 'segundo' (Fulanito)
        } // 5. Se libera el candado de 'primero' (Carlos)
    }

    // --- MÉTODO HELPER PARA MOSTRAR SALDOS ---
    /**
     * Muestra los saldos de todas las personas en la lista y calcula el total.
     * @param personas La lista de personas.
     * @return El saldo total sumado.
     */
    private static double mostrarSaldos(List<Persona> personas) {
        double total = 0;
        for (Persona p : personas) {
            // getSaldo() está sincronizado, por lo que es seguro llamarlo
            // incluso si otros hilos están operando (aunque aquí esperamos que no).
            double saldo = p.getSaldo();
            System.out.printf("  -> %-8s tiene %.2f€%n", p.getNombre() + ":", saldo);
            total += saldo;
        }
        return total;
    }

    // --- PUNTO DE ENTRADA DEL PROGRAMA ---
    public static void main(String[] args) throws InterruptedException {
        System.out.println("🏦 Iniciando simulación de transferencias bancarias...");

        // 1. Crear las 7 personas (recursos)
        // Usamos List.of() para crear una lista inmutable.
        List<Persona> personas = List.of(
                new Persona("Fulanito", 10000),
                new Persona("Carlos", 2000),
                new Persona("Ana", 5000),
                new Persona("Beatriz", 7000),
                new Persona("David", 3000),
                new Persona("Elena", 15000),
                new Persona("Miguel", 8000)
        );
        // Guardamos el saldo inicial total para comprobar que no perdemos dinero.
        final double saldoTotalInicial = 10000+2000+5000+7000+3000+15000+8000;

        // 2. Mostrar estado inicial
        System.out.println("👥 Lista de clientes inicial:");
        mostrarSaldos(personas);
        System.out.printf("💰 Saldo total inicial: %.2f€%n%n", saldoTotalInicial);

        // 3. Crear los "gestores" (hilos) que harán las transferencias
        // Usamos un ExecutorService (un pool de hilos) para manejar 4 hilos a la vez.
        int numGestores = 4;
        ExecutorService gestores = Executors.newFixedThreadPool(numGestores);
        // Cada gestor (hilo) realizará 100 transferencias aleatorias.
        int transferenciasPorGestor = 100;

        long inicio = System.currentTimeMillis();

        // 4. Poner a los gestores a trabajar
        for (int i = 0; i < numGestores; i++) {
            // Damos un nombre a cada gestor
            final String nombreGestor = "Gestor-" + (i + 1);

            // submit() envía una tarea (un Runnable) al pool de hilos.
            gestores.submit(() -> {
                // Cada hilo usa su propio generador de números aleatorios.
                Random random = ThreadLocalRandom.current();

                // Bucle de trabajo del gestor
                for (int j = 0; j < transferenciasPorGestor; j++) {
                    // Elige dos personas AL AZAR de la lista
                    Persona origen = personas.get(random.nextInt(personas.size()));
                    Persona destino = personas.get(random.nextInt(personas.size()));

                    // Elige una cantidad aleatoria (entre 50 y 550)
                    double cantidad = random.nextDouble() * 500 + 50;

                    // Realizar la transferencia usando nuestro método seguro
                    transferir(origen, destino, cantidad, nombreGestor);

                    // Pequeña pausa para simular el "papeleo"
                    try { Thread.sleep(random.nextInt(10) + 5); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            });
        }

        // 5. Esperar a que todos los gestores terminen
        gestores.shutdown(); // Decimos al pool que no acepte más tareas
        // El hilo "main" se bloquea aquí hasta que terminen todas las tareas,
        // o hasta que pase 1 minuto (timeout).
        gestores.awaitTermination(1, TimeUnit.MINUTES);

        long fin = System.currentTimeMillis();

        // 6. Mostrar resultados finales
        System.out.println("\n🏁 Simulación terminada en " + (fin - inicio) + "ms.");
        System.out.println("=============================================");
        System.out.println("📊 RESULTADOS FINALES DE SALDO");
        System.out.println("=============================================");

        // Mostramos saldos finales y calculamos el total
        double saldoTotalFinal = mostrarSaldos(personas);

        // 7. Comprobación de integridad (el dinero no debe crearse ni destruirse)
        System.out.println("---------------------------------------------");
        System.out.printf("💰 Saldo total inicial: %.2f€%n", saldoTotalInicial);
        System.out.printf("💰 Saldo total final:   %.2f€%n", saldoTotalFinal);

        // Comparamos los decimales con un pequeño margen de error (epsilon)
        if (Math.abs(saldoTotalInicial - saldoTotalFinal) < 0.01) {
            System.out.println("✅ ¡El dinero se ha conservado! No se perdió ni se creó dinero.");
        } else {
            System.out.println("❌ ¡ERROR! El saldo total no coincide. Se ha perdido o creado dinero.");
        }
        System.out.println("=============================================");
    }
}