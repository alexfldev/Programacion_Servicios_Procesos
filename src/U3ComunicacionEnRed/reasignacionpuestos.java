package U3ComunicacionEnRed;

import java.util.*;

public class reasignacionpuestos {

    private static String[] nombres = {"Genesis","Pablo","Luisa","Alejandro","Sergio G" ,"Mario",
            "Astrid", "Esteban","Victor","Claudia","Sergio M" ,"Marcos" ,"David","Sebas","aaron","Johan"};

    private static List<Integer> puestos = new ArrayList<>();
    // Arrays.asList crea una lista de tamaño fijo basada en el array
    private static List<String> alumnos = Arrays.asList(nombres);
    private static Map<Integer, String> asignaciones = new TreeMap<>();
    private static final int MAX_ALUMNOS = 16;

    public static void main(String[] args) {

        for (int i = 1; i <= MAX_ALUMNOS; i++) {
            // ERROR ORIGINAL: No se puede añadir a una lista creada con asList.
            // Además, ya tienes los 16 nombres en el array, no necesitas añadir "Alumno i".
            // alumnos.add("Alumno " + i);

            puestos.add(i);
        }

        System.out.println("Reasignando puestos");

        Collections.shuffle(alumnos);
        Collections.shuffle(puestos);

        System.out.println("Resultado del Sorteo");
        Scanner sc = new Scanner(System.in);

        for (int i = 0; i < MAX_ALUMNOS; i++) {
            System.out.print(" El puesto para el alumno " + alumnos.get(i) + " es ......(pulse una tecla)");
            sc.nextLine();

            System.out.println(puestos.get(i));

            asignaciones.put(puestos.get(i), alumnos.get(i));
        }

        System.out.println("-------------------------");
        System.out.println("Resumen de Asignaciones:");

        for(Map.Entry<Integer, String> e : asignaciones.entrySet()) {
            System.out.println("Puesto " + e.getKey() + " : " + e.getValue());
        }
    }
}