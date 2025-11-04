
package Examen1ev;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;




public class GeneradorPedidos implements Runnable {



    private static final long TIEMPO_SIMULACION = System.currentTimeMillis() + 5000;

    private static final Semaphore semaforo = new Semaphore(10, true);

   //Uso de atomicinteger
    private static final AtomicInteger totalProductosPedidos = new AtomicInteger(0);
    private static final AtomicInteger enPedido = new AtomicInteger(0); // Para saber cuántos hilos están DENTRO del semáforo

    private static final AtomicInteger generadorID = new AtomicInteger(0);

    private static final Map<String, Integer> pedidoPorCliente = new HashMap<>();

    private static final ReentrantLock lock = new ReentrantLock();

    private static final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yy HH:mm:ss");



    // --- CLASE INTERNA ---

    static class Producto {

        private final int id;
        private final String Cliente;





        public Producto(int id, String Cliente) {
            this.id = id;
            this.Cliente = Cliente;
        }

        @Override
        public String toString() {
            return "Producto " + id + " pedido " + Cliente;
        }
    }


    public static void main(String[] args) {
        System.out.println("Generando Pedidos...\n");


        List<Thread> Cliente = new ArrayList<>();


        for (int i = 1; i <= 10; i++) {

            Thread t = new Thread(new GeneradorPedidos(), "Cliente" + i);
           Cliente.add(t);
            t.start();
        }


        for (Thread t : Cliente) {
            try {

                t.join();
            } catch (InterruptedException e) {

                System.err.println("Error esperando al hilo: " + t.getName());
            }
        }


        mostrarEstadisticas();
    }



    @Override
    public void run() {

        String nombre = Thread.currentThread().getName();

        Random random = new Random();


        while (System.currentTimeMillis() < TIEMPO_SIMULACION) {
            try {



                semaforo.acquire();


                enPedido.incrementAndGet(); // Incremento atómico
                System.out.println(nombre + " Esta haciendo un pedido..."
                        + " [Escogiendo: " + enPedido.get() + "]");
                System.out.println(formato.format(DateFormat.LONG));


                Thread.sleep(200 + random.nextInt(300));


                int idProducto = generadorID.incrementAndGet();
                Producto nuevo = new Producto(idProducto, nombre); // Creamos el objeto

                System.out.println( nuevo); // Imprime usando el método .toString()


                totalProductosPedidos.incrementAndGet();


                lock.lock();
                try {

                    pedidoPorCliente.put(nombre,
                            pedidoPorCliente.getOrDefault(nombre, 0) + 1);
                } finally {


                    lock.unlock();
                }

            } catch (InterruptedException e) {

                System.err.println(nombre + " fue interrumpido.");
            } finally {

                enPedido.decrementAndGet(); // Decremento atómico
                semaforo.release(); // El hilo devuelve el permiso al semáforo.

            }
        }

        System.out.println(nombre + " terminó de realizar su pedido.");
    }



    private static void mostrarEstadisticas() {
        System.out.println("\n=====  ESTADÍSTICAS DE GESTION DE PEDIDOS =====");

        int totalPorMapa = 0; // Contador local para verificar


        lock.lock();
        try {

            for (String t : pedidoPorCliente.keySet()) {
                int cantidad = pedidoPorCliente.get(t); // Obtenemos lo que pide
                totalPorMapa += cantidad; // Sumamos al total local
                System.out.println(t + " Escogio " + cantidad + " productos.");


            }
        } finally {
            lock.unlock();
        }

        System.out.println("--------------------------------------------");
        // Mostramos el total contado sumando los valores del mapa
        System.out.println("Total Pedidos (sumando Clientes): " + totalPorMapa);
        // Mostramos el total contado por el AtomicInteger
        System.out.println("Total global Pedidos (AtomicInteger): " + totalProductosPedidos.get());

        // NOTA: Ambos totales (totalPorMapa y totalProductosFabricados) DEBEN ser iguales.
        // Si no lo fueran, tendríamos un error de concurrencia.

        System.out.println("============================================\n");
    }
}