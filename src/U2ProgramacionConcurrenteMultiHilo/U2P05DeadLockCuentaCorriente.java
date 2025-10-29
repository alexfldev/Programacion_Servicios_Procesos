// Define el paquete o carpeta lógica donde se agrupa esta clase.
package U2ProgramacionConcurrenteMultiHilo;

/**
 * 🔁 Clase Recurso: CuentaCorriente
 * Esta clase representa el recurso compartido (el dinero en la cuenta).
 *
 * IMPORTANTE: Sus métodos (retirar, ingresar) NO están sincronizados.
 * La sincronización se manejará externamente (en el método 'transferencia').
 * Esto se hace para permitir operaciones más complejas que involucran *dos* cuentas,
 * como una transferencia.
 */
class CuentaCorriente {
    // Saldo de la cuenta. Es 'private' para encapsulación.
    private float saldo;

    /**
     * Constructor para inicializar la cuenta con un saldo inicial.
     * @param saldo El saldo inicial.
     */
    public CuentaCorriente(float saldo) {
        this.saldo = saldo;
    }

    /**
     * Retira un importe de la cuenta.
     * Esta operación es un "check-then-act" (comprobar y luego actuar).
     * Si se llamara sin un 'synchronized' externo, podría causar una 'race condition'.
     * @param importe Cantidad a retirar.
     */
    public void retirar(float importe) {
        if (saldo >= importe) {
            saldo -= importe;
        }
        // Nota: no hay 'else'. Si no hay saldo, simplemente no se hace nada.
    }

    /**
     * Ingresa un importe en la cuenta.
     * Esto también es una 'race condition' potencial (saldo = saldo + importe)
     * si no se sincroniza externamente.
     * @param importe Cantidad a ingresar.
     */
    public void ingresar(float importe) {
        saldo += importe;
    }

    /**
     * Devuelve el saldo actual de la cuenta.
     * @return El saldo.
     */
    public float getSaldo() {
        return saldo;
    }
}

/**
 * 🏦 Clase Principal: U2P05DeadLockCuentaCorriente
 * Simula transferencias bancarias concurrentes entre dos cuentas.
 * Demuestra cómo evitar un "Deadlock" (bloqueo mutuo) usando la técnica de
 * "Ordenación de Bloqueos" (Lock Ordering).
 */
public class U2P05DeadLockCuentaCorriente {

    // Comentarios originales del ejercicio:
    // Crear dos cuentas corrientes y dos threads que hagan transferencias entre cuentas.
    // Evitar deadlock, pues el hilo 1 transfiere a la cuenta 2 y el hilo 2 transfiere a la cuenta 1.
    // El monitor bloquea por clase o por objeto -> [Respuesta: En este caso, bloquea por OBJETO].

    /**
     * 🔄 Método estático para realizar una transferencia segura entre dos cuentas.
     * Este es el núcleo de la solución anti-deadlock.
     *
     * @param origen  La cuenta desde la que se retira el dinero.
     * @param destino La cuenta a la que se ingresa el dinero.
     * @param importe La cantidad a transferir.
     */
    public static void transferencia(CuentaCorriente origen, CuentaCorriente destino, float importe) {

        // --- INICIO DE LA SOLUCIÓN ANTI-DEADLOCK (Lock Ordering) ---
        // Para evitar un deadlock, debemos asegurar que TODOS los hilos
        // adquieran los bloqueos (locks) de las cuentas en el MISMO ORDEN.

        // 1. Establecemos un orden canónico (fijo) usando el hashCode de los objetos.
        //    (hashCode es un número que suele ser único para cada objeto).

        // Asigna a 'cuenta1' la cuenta con el hashCode más bajo.
        CuentaCorriente cuenta1 = origen.hashCode() < destino.hashCode() ? origen : destino;
        // Asigna a 'cuenta2' la cuenta con el hashCode más alto.
        CuentaCorriente cuenta2 = origen.hashCode() < destino.hashCode() ? destino : origen;

        // 2. Adquirimos los bloqueos SIEMPRE en ese orden: primero 'cuenta1', luego 'cuenta2'.

        // Hilo 1 (cc1 -> cc2): Pide lock de cc1 (cuenta1), luego pide lock de cc2 (cuenta2).
        // Hilo 2 (cc2 -> cc1): Pide lock de cc1 (cuenta1), luego pide lock de cc2 (cuenta2).

        // El Hilo 2 no puede bloquear cc2 y esperar por cc1 (lo que causaría el deadlock),
        // está FORZADO a pedir primero cc1.

        synchronized (cuenta1) { // 🔒 Bloquea el objeto con el hashCode MÁS BAJO
            synchronized (cuenta2) { // 🔒 Bloquea el objeto con el hashCode MÁS ALTO

                // --- INICIO DE LA SECCIÓN CRÍTICA ---
                // Solo cuando el hilo posee AMBOS bloqueos, puede operar con seguridad.

                origen.retirar(importe);
                destino.ingresar(importe);

                // --- FIN DE LA SECCIÓN CRÍTICA ---
            } // libera el bloqueo de 'cuenta2'
        } // libera el bloqueo de 'cuenta1'
    }

    /**
     * 🏁 Método principal (main) - Punto de entrada del programa.
     */
    public static void main(String[] args) {
        // Creamos las dos cuentas corrientes (recursos compartidos).
        CuentaCorriente cc1 = new CuentaCorriente(100_000);
        CuentaCorriente cc2 = new CuentaCorriente(100_000);

        // 🧵 Hilo 1 (t1): Transfiere 1000 veces 10€ de cc1 a cc2.
        // Total: cc1 pierde 10,000€, cc2 gana 10,000€
        Thread t1 = new Thread(() -> { // Expresión Lambda para definir la tarea del hilo
            for (int i = 0; i < 1_000; i++)
                transferencia(cc1, cc2, 10);
        });

        // 🧵 Hilo 2 (t2): Transfiere 1000 veces 20€ de cc2 a cc1.
        // Esta es la operación conflictiva que podría causar el deadlock.
        // Total: cc2 pierde 20,000€, cc1 gana 20,000€
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1_000; i++)
                transferencia(cc2, cc1, 20);
        });

        // Inicia la ejecución de ambos hilos. Comienza la "carrera".
        t1.start();
        t2.start();

        try {
            // El hilo 'main' espera a que 't1' termine.
            t1.join();
            // El hilo 'main' espera a que 't2' termine.
            t2.join();
        } catch (InterruptedException e) {
            // Manejo de la excepción si el hilo 'main' es interrumpido mientras espera.
            throw new RuntimeException(e);
        }

        // --- RESULTADOS ---
        // Esta sección solo se ejecuta después de que AMBOS hilos hayan terminado.

        // Saldo final esperado de cc1: 100,000 - 10,000 (de t1) + 20,000 (de t2) = 110,000
        System.out.println("Saldo cc1: " + cc1.getSaldo());

        // Saldo final esperado de cc2: 100,000 + 10,000 (de t1) - 20,000 (de t2) = 90,000
        System.out.println("Saldo cc2: " + cc2.getSaldo());

        // El saldo total debe conservarse (200,000)
        System.out.println("Saldo total de las cuentas: " + (cc1.getSaldo() + cc2.getSaldo()));
    }
}