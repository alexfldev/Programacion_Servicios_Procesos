// Define el "paquete" o carpeta lógica donde se agrupa esta clase.
package Examen1ev.PracticasExamen;

// Importa la clase AtomicInteger, que se usa para crear IDs únicos 
// de forma segura en entornos con múltiples hilos.
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 🏦 Simulación de transferencias entre dos cuentas bancarias usando threads.
 * Evita deadlock al transferir dinero entre cuentas.
 */
// Clase principal que contendrá toda la lógica.
public class BancoDeadlock {

    // Define una "clase interna estática" para representar una Cuenta Bancaria.
    // "static class" significa que esta clase "Cuenta" pertenece a "BancoDeadlock"
    // pero no necesita una instancia de BancoDeadlock para ser creada.
    static class Cuenta {
        private double saldo; // El dinero que tiene la cuenta
        private final int id; // Un ID único para cada cuenta

        // Un generador de IDs "atómico" y "estático".
        // "static": Es compartido por TODAS las instancias de Cuenta.
        // "AtomicInteger": Garantiza que si dos hilos crean una cuenta
        // al mismo tiempo, ambos obtendrán un ID único sin conflictos.
        private static final AtomicInteger idGenerator = new AtomicInteger(1);

        // Constructor: se llama al crear una nueva Cuenta (ej: new Cuenta(1000))
        public Cuenta(double saldoInicial) {
            this.saldo = saldoInicial;
            // Asigna un ID único a esta cuenta.
            // "getAndIncrement()" obtiene el valor actual (ej: 1) y 
            // luego lo incrementa (a 2) para la próxima vez que se llame.
            this.id = idGenerator.getAndIncrement();
        }

        // Método simple para obtener el ID de la cuenta.
        public int getId() {
            return id;
        }

        // Método para añadir dinero.
        // "synchronized": Es la palabra clave para la seguridad en hilos.
        // Significa que solo UN hilo puede ejecutar este método (o cualquier
        // otro método "synchronized") en ESTA instancia de cuenta a la vez.
        // Evita que dos hilos ingresen dinero al mismo tiempo y corrompan el saldo.
        public synchronized void ingresar(double cantidad) {
            saldo += cantidad;
        }

        // Método para sacar dinero.
        // "synchronized": Igual que antes, bloquea la cuenta para este hilo.
        public synchronized void retirar(double cantidad) {
            if (saldo >= cantidad) {
                saldo -= cantidad;
            } else {
                // Informa si no hay fondos suficientes
                System.out.println("⚠️ Cuenta " + id + ": saldo insuficiente para retirar " + cantidad);
            }
        }

        // Método para consultar el saldo.
        // "synchronized": Importante para asegurar que leemos un valor "estable"
        // y no un saldo a medio actualizar por otro hilo.
        public synchronized double getSaldo() {
            return saldo;
        }
    }

    // --- EL MÉTODO MÁS IMPORTANTE PARA EVITAR EL DEADLOCK ---

    // Transfiere dinero de una cuenta a otra.
    public static void transferir(Cuenta origen, Cuenta destino, double cantidad) {

        // --- INICIO DE LA ESTRATEGIA ANTI-DEADLOCK ---
        // Problema: Si Hilo1 transfiere CC1 -> CC2, bloquea CC1 y luego CC2.
        //           Si Hilo2 transfiere CC2 -> CC1, bloquea CC2 y luego CC1.
        //           Si lo hacen a la vez, Hilo1 tiene CC1 y espera por CC2,
        //           mientras Hilo2 tiene CC2 y espera por CC1. ¡DEADLOCK!

        // Solución: Establecer un ORDEN FIJO de bloqueo.
        // Siempre bloquearemos primero la cuenta con el ID más bajo.

        // Comparamos los IDs de las cuentas
        Cuenta primero = (origen.getId() < destino.getId()) ? origen : destino;
        Cuenta segundo = (origen.getId() < destino.getId()) ? destino : origen;

        // EJEMPLO:
        // Hilo1 (CC1 -> CC2): ID 1 < ID 2. primero=CC1, segundo=CC2
        // Hilo2 (CC2 -> CC1): ID 1 < ID 2. primero=CC1, segundo=CC2

        // Ambos hilos intentarán bloquear PRIMERO a "primero" (CC1)
        synchronized (primero) {
            // Solo el hilo que consiga el bloqueo de "primero" (CC1)
            // puede intentar bloquear a "segundo" (CC2).
            synchronized (segundo) {
                // Una vez que el hilo tiene AMBOS bloqueos,
                // la transferencia es segura.
                origen.retirar(cantidad);
                destino.ingresar(cantidad);
            } // Libera el bloqueo de "segundo"
        } // Libera el bloqueo de "primero"
        // --- FIN DE LA ESTRATEGIA ANTI-DEADLOCK ---
    }

    // El punto de entrada principal del programa
    public static void main(String[] args) {
        // Creamos dos cuentas, cada una con 100,000.
        // cc1 tendrá ID=1
        Cuenta cc1 = new Cuenta(100_000);
        // cc2 tendrá ID=2
        Cuenta cc2 = new Cuenta(100_000);

        System.out.println("💰 Saldo inicial CC1: " + cc1.getSaldo());
        System.out.println("💰 Saldo inicial CC2: " + cc2.getSaldo());

        // --- HILO 1 ---
        // Crea un nuevo hilo (Thread) y le da una tarea (un "lambda")
        Thread hilo1 = new Thread(() -> {
            // La tarea es: repetir 1000 veces...
            for (int i = 0; i < 1000; i++) {
                // ...transferir 10 de cc1 a cc2
                transferir(cc1, cc2, 10);
            }
        }, "Hilo-1"); // Le damos un nombre al hilo para depuración

        // --- HILO 2 ---
        // Crea un segundo hilo
        Thread hilo2 = new Thread(() -> {
            // La tarea es: repetir 1000 veces...
            for (int i = 0; i < 1000; i++) {
                // ...transferir 20 de cc2 a cc1
                transferir(cc2, cc1, 20);
            }
        }, "Hilo-2"); // Nombre del segundo hilo

        // Inicia ambos hilos. Ahora empiezan a ejecutarse "a la vez".
        hilo1.start();
        hilo2.start();

        // --- ESPERA ---
        // El hilo "main" (el principal) debe esperar a que los otros dos terminen.
        try {
            hilo1.join(); // Pausa el hilo "main" hasta que "hilo1" termine.
            hilo2.join(); // Pausa el hilo "main" hasta que "hilo2" termine.
        } catch (InterruptedException e) {
            // Obligatorio manejar esta excepción por si el hilo es interrumpido
            e.printStackTrace();
        }

        // --- RESULTADOS ---
        // Este código solo se ejecuta DESPUÉS de que ambos hilos hayan terminado.
        System.out.println("\n🏦 Resultados finales:");

        // Cálculo esperado:
        // CC1: 100,000 - (1000 * 10) + (1000 * 20) = 100,000 - 10,000 + 20,000 = 110,000
        System.out.println("Saldo CC1: " + cc1.getSaldo());
        // CC2: 100,000 + (1000 * 10) - (1000 * 20) = 100,000 + 10,000 - 20,000 = 90,000
        System.out.println("Saldo CC2: " + cc2.getSaldo());
        // Total: 110,000 + 90,000 = 200,000 (el dinero no se crea ni se destruye)
        System.out.println("Saldo total (CC1 + CC2): " + (cc1.getSaldo() + cc2.getSaldo()));
    }
}