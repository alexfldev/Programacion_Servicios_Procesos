// Define el paquete o carpeta lógica donde se agrupa esta clase.
package PracticaExamen;

/**
 * 🚗🔌 Simulación de coches (hilos) que necesitan usar dos estaciones de carga
 * (recursos) a la vez.
 * Demuestra cómo EVITAR un DEADLOCK (bloqueo mutuo) estableciendo un orden
 * fijo para adquirir los bloqueos (monitores) de los recursos.
 */
public class EstacionSinDeadlock {

    // --- CLASE INTERNA ESTÁTICA ---
    /**
     * Representa una estación de carga (un recurso compartido).
     * Es "static class" porque es una clase "ayudante" que pertenece a
     * EstacionSinDeadlock, pero no necesita una instancia de ella para ser creada.
     */
    static class Estacion {
        // El nombre de la estación (ej: "Estacion-A").
        // Es "final" porque no cambiará una vez creado.
        private final String nombre;

        /**
         * Constructor para crear una nueva estación.
         * @param nombre El identificador único de la estación.
         */
        public Estacion(String nombre) {
            this.nombre = nombre;
        }

        /**
         * Método "getter" para obtener el nombre de la estación.
         * @return El nombre de la estación.
         */
        public String getNombre() {
            return nombre;
        }
    }

    // --- MÉTODO ESTÁTICO PRINCIPAL (LÓGICA ANTI-DEADLOCK) ---
    /**
     * Método que simula el uso de dos estaciones (e1 y e2) por un coche.
     * EVITA EL DEADLOCK asegurando que los bloqueos se adquieren SIEMPRE
     * en el mismo orden (alfabético por nombre), sin importar el orden
     * en que fueron solicitados.
     *
     * @param e1 Una de las estaciones a usar.
     * @param e2 La otra estación a usar.
     * @param coche El nombre del hilo (coche) que las va a usar.
     */
    public static void usarEstaciones(Estacion e1, Estacion e2, String coche) {

        // --- INICIO DE LA ESTRATEGIA ANTI-DEADLOCK ---
        // Problema: Si Coche1 bloquea A y espera B, y Coche2 bloquea B y espera A = ¡DEADLOCK!
        // Solución: Establecer un ORDEN FIJO. Todos los hilos deben bloquear
        //           primero la estación con el nombre alfabéticamente menor.

        // Comparamos los nombres alfabéticamente.
        // e1.getNombre().compareTo(e2.getNombre()) < 0 significa "el nombre de e1 va ANTES que el de e2".
        Estacion primero = (e1.getNombre().compareTo(e2.getNombre()) < 0) ? e1 : e2;
        Estacion segundo = (e1.getNombre().compareTo(e2.getNombre()) < 0) ? e2 : e1;

        // "primero" SIEMPRE será Estacion-A
        // "segundo" SIEMPRE será Estacion-B
        // No importa si el coche llamó usarEstaciones(A, B) o usarEstaciones(B, A),
        // el orden de bloqueo interno (synchronized) será el mismo.
        // --- FIN DE LA ESTRATEGIA ANTI-DEADLOCK ---


        // 1. Adquirir el bloqueo (monitor) del recurso "primero" (Estacion-A)
        synchronized (primero) {
            System.out.println("🔒 " + coche + " ha bloqueado " + primero.getNombre());

            // Pausa breve: simula el tiempo que tarda en conectarse a la primera estación
            // y da tiempo al otro hilo para intentar bloquear recursos.
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}

            // 2. Adquirir el bloqueo (monitor) del recurso "segundo" (Estacion-B)
            // Un hilo solo puede llegar aquí si YA tiene el bloqueo de "primero".
            // Como todos los hilos piden A y LUEGO B, nunca se bloquearán mutuamente.
            synchronized (segundo) {
                // El hilo AHORA tiene ambos bloqueos y puede operar de forma segura.
                System.out.println("⚡ " + coche + " está usando " + primero.getNombre() +
                        " y " + segundo.getNombre());

                // Simula el tiempo de carga real usando ambas estaciones
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}

                System.out.println("✅ " + coche + " ha terminado en " + primero.getNombre() +
                        " y " + segundo.getNombre());

            } // 3. Libera el bloqueo de "segundo" (Estacion-B)
        } // 4. Libera el bloqueo de "primero" (Estacion-A)
    }

    // --- MÉTODO MAIN (PUNTO DE ENTRADA) ---
    public static void main(String[] args) {

        // Crear los dos recursos compartidos (las estaciones)
        Estacion estacionA = new Estacion("Estacion-A");
        Estacion estacionB = new Estacion("Estacion-B");

        // Hilo 1: Coche 1 usa A → B
        // Creamos el primer hilo (Coche 1) usando una expresión Lambda.
        Thread coche1 = new Thread(() -> {
            // El Coche 1 intentará usar las estaciones 5 veces.
            for (int i = 0; i < 5; i++) {
                // Llama al método pidiendo las estaciones en el orden A, B.
                usarEstaciones(estacionA, estacionB, "🚗 Coche 1");
            }
        });

        // Hilo 2: Coche 2 usa B → A (El orden de solicitud es inverso)
        // Creamos el segundo hilo (Coche 2).
        Thread coche2 = new Thread(() -> {
            // El Coche 2 también lo intentará 5 veces.
            for (int i = 0; i < 5; i++) {
                // Llama al método pidiendo las estaciones en el orden INVERSO (B, A).
                // ¡Este es el escenario que causaría un deadlock si no tuviéramos
                // la lógica de ordenación dentro de usarEstaciones!
                usarEstaciones(estacionB, estacionA, "🚙 Coche 2");
            }
        });

        // Iniciar los hilos para que se ejecuten en paralelo
        coche1.start(); // Arranca el Coche 1
        coche2.start(); // Arranca el Coche 2 (ahora compiten por los recursos)

        // El hilo "main" (principal) debe esperar a que los otros dos terminen.
        try {
            // El hilo "main" se pausa aquí hasta que "coche1" termine su método run().
            coche1.join();
            // El hilo "main" se pausa aquí hasta que "coche2" termine su método run().
            coche2.join();
        } catch (InterruptedException e) {
            // Obligatorio capturar esta excepción por si el hilo "main" es interrumpido.
            e.printStackTrace();
        }

        // Si el programa llega a esta línea, significa que ambos hilos
        // terminaron su trabajo y no se quedaron bloqueados (no hubo deadlock).
        System.out.println("\n🏁 Todos los coches han terminado sin deadlock.");
    }
}