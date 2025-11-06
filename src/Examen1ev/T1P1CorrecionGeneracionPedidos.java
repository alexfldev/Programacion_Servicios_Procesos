package Examen1ev;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class T1P1CorrecionGeneracionPedidos {
    static class Pedido {
        private static AtomicInteger generadorid;
        private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        private String id;
        private String cliente;
        private long fecha;

        // inicializador estático: se ejecuta una sola vez
        static {
            generadorid = new AtomicInteger(0);
        }

        public Pedido(String cliente) {
            this.id = String.valueOf(generadorid.incrementAndGet());
            this.cliente = cliente;
            this.fecha = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "Pedido{" + "id=" + id + ", cliente=" + cliente + ", fecha=" + sdf.format(fecha) + '}';
        }
    }

    public static void main(String[] args) {
        final int MAX_THREADS = 10;
        final int NUM_PEDIDOS = 10;
        List<Pedido> pedidos = new ArrayList<Pedido>();
        List<Thread> threads = new ArrayList<Thread>();
        Map<String,AtomicInteger> PedidosPorCliente = new ConcurrentHashMap<String,AtomicInteger>();
        Random random = new Random();

        // 1. Declaración y ejecución de los Threads
        for (int i = 0; i < MAX_THREADS; i++) {
            Thread hilo = new Thread(() -> {
                for (int j = 0; j < NUM_PEDIDOS; j++) {
                    //Crear cliente aleatorio
                    String cliente = "Cliente-" + random.nextInt(10);
                    //creo pedido y lo guardo
                    Pedido pedido = new Pedido(cliente);
                    synchronized (pedidos) {
                        pedidos.add(pedido);
                    }

                    //System.out.println(pedido);
                    //Contabilizamos el pedido para el cliente que lo ha contabilizado
                    PedidosPorCliente.computeIfAbsent(cliente, k -> new AtomicInteger()).incrementAndGet();
                }
            });
            hilo.start();
            threads.add(hilo);
        }

        System.out.println("Todos los Threads están ejecutándose");

        // 2. Esperando a que todos los hilos acaben
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Todos los Threads han terminado");

        // 3. Mostrar estadísticas
        System.out.println("Pedidos realizados por los clientes***");
        for (Pedido pedido : pedidos) {
            System.out.println(pedido);
        }
        System.out.println("Número total de pedidos: " + pedidos.size());
        System.out.println("*** Cantidad de pedidos por cliente: ");
        int cont = 0;
        for (String cliente: PedidosPorCliente.keySet()){
            System.out.println("El cliente: " + cliente+ " Ha realizado:" + PedidosPorCliente.get(cliente));
            cont += PedidosPorCliente.get(cliente).get();
        }


    }

}
