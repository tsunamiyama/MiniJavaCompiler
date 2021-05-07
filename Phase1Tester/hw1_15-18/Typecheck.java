import syntaxtree.*;
import java.io.*;
import java.util.Scanner;

public class Typecheck {
    public static void main(String[] args) {
        /*Scanner sc = new Scanner(System.in);
        System.out.println("Printing the file passed in:");
        while(sc.hasNextLine()) System.out.println(sc.nextLine());
         */

        InputStream fileStream = System.in;
        new MiniJavaParser(fileStream);
        SymbolTableVisitor sv = new SymbolTableVisitor();
        TypeVisitor tv = new TypeVisitor();
        try {
            Node root = MiniJavaParser.Goal();
            root.accept(sv);
            root.accept(tv, sv);
            tv.checkTypeError();
            if(!tv.hasError) {
                System.out.println("Program type checked successfully");
                System.exit(0);
            }
            System.exit(1);
            //sv.print();
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("Type error");
        }
    }
}