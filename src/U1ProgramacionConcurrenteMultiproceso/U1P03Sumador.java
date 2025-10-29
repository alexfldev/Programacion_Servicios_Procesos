package U1ProgramacionConcurrenteMultiproceso;

public class U1P03Sumador {

    // Sumar todos los números situados entre num1 y num2
    private void sumar(int num1, int num2){
        int resultado = 0;
        if (num1 > num2){
            int aux = num1;
            num1 = num2;
            num2 = aux;
        }
        for (int i = num1; i <= num2; i++) {
            resultado += i;
        }
        System.out.println("La suma de los números situados entre num1 y num2 es: " + resultado);
    }

    // String[] args -> permite enviar argumentos al main
    public static void main(String[] args) {

        // Instancia de la clase
        U1P03Sumador test = new U1P03Sumador();
        test.sumar(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

        // Prueba para ver el funcionamiento de la VVMM
        /*while (true){

        }*/
    }
}