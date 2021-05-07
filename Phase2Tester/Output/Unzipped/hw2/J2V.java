import syntaxtree.*;
import java.io.*;
import java.util.Scanner;

public class J2V {
    public static void main(String[] args) {
        /*Scanner sc = new Scanner(System.in);
        System.out.println("Printing the file passed in:");
        while(sc.hasNextLine()) System.out.println(sc.nextLine());
         */

        InputStream fileStream = System.in;
        new MiniJavaParser(fileStream);
        SymbolTableVisitor sv = new SymbolTableVisitor();
        vaporVisitor vv = new vaporVisitor();
        try {
            Node root = MiniJavaParser.Goal();
            root.accept(sv);
            sv.printVtablesFinal();
            root.accept(vv, sv);
            if(vv.arrayAllo){
                vv.printAllo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}