import java.util.Scanner;

public class Main {
 
    
    public static void main(String[] args) {
        
        var scanner = new Scanner(System.in);
        System.out.println("Quantos anos voce tem? ");
        var age = scanner.nextInt();
        System.out.println("Voce é emancipado? ");
        var emancipado = scanner.nextBoolean();
        var candrive = age >= 18 || (emancipado && age >= 16);
        System.out.printf("oce pode dirigir? (%s) \n ", candrive);

        
       
        

    }
}
