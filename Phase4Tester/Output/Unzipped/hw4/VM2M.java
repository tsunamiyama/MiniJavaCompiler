import cs132.util.ProblemException;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VaporProgram;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.Vector;

import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.parser.VaporParser;

public class VM2M {
    private static boolean callsFunction = false;
    public static void printHeader(VaporProgram root){
        //Get the class names
        Vector<String> functionNames = new Vector<>();
        Vector<String> classNames = new Vector<>();
        for(int i = 0; i < root.functions.length; ++i){
            if(root.functions[i].ident.contains(".")) {
                String func_name = root.functions[i].ident;
                functionNames.add(func_name);
                String[] parsedName = func_name.split("\\.",2);
                if(!classNames.contains(parsedName[0])) {
                    classNames.add(parsedName[0]);
                }
            }
        }
        //Print Header Info
        System.out.println(".data\n");
        for(int i = 0; i < root.dataSegments.length; ++i){
            System.out.println(root.dataSegments[i].ident + ":");
            for(int j = 0; j < functionNames.size(); ++j){
                if(functionNames.elementAt(j).contains(classNames.elementAt(i))){
                    System.out.println("  " + functionNames.elementAt(j));
                }
            }
        }
        System.out.println();
        System.out.println(".text");
        System.out.println("  jal Main");
        System.out.println("  li $v0 10");
        System.out.println("  syscall");
        System.out.println();
    }
    public static void printTail(){
        System.out.println("_print:");
        System.out.println("  li $v0 1");
        System.out.println("  syscall");
        System.out.println("  la $a0 _newline");
        System.out.println("  li $v0 4");
        System.out.println("  syscall");
        System.out.println("  jr $ra");
        System.out.println();

        System.out.println("_error:");
        System.out.println("  li $v0 4");
        System.out.println("  syscall");
        System.out.println("  li $v0 10");
        System.out.println("  syscall");
        System.out.println();

        System.out.println("_heapAlloc:");
        System.out.println("  li $v0 9");
        System.out.println("  syscall");
        System.out.println("  jr $ra");

        System.out.println();
    }
    public static VaporProgram parseVapor(InputStream in) throws IOException {
        Op[] ops = {
                Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
                Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };

        boolean allowLocals = false;
        String[] registers = {
                "v0", "v1",
                "a0", "a1", "a2", "a3",
                "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
                "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
                "t8",
        };
        boolean allowStack = true;

        VaporProgram tree;
        try {
            tree = VaporParser.run(new InputStreamReader(in), 1, 1, java.util.Arrays.asList(ops), allowLocals, registers, allowStack);
        }
        catch (ProblemException ex) {
            System.out.println(ex.getMessage());
            return null;
        }

        return tree;
    }
    public static void main(String[] args) throws Exception{
        InputStream fileStream = System.in;
        VaporProgram root = parseVapor(fileStream);
        mipsVisitor mv = new mipsVisitor();
        printHeader(root);
        for(int i = 0; i < root.functions.length; ++i){
            VFunction temp = root.functions[i];
            callsFunction = false;

            //Check if function calls another function
            for(int a = 0; a < root.functions[i].body.length; ++a){
                if(temp.body[a].getClass().getSimpleName().contains("VCall") || temp.body[a].getClass().getSimpleName().contains("VBuiltIn")){
                    callsFunction = true;
                }
            }

            System.out.println(temp.ident.toString() + ":");
            //Save $fp to location $sp - 2
            System.out.println("  sw $fp -8($sp)");
            //   Move $fp to point to where $sp is pointing
            System.out.println("  move $fp $sp");
            //   Pushing the frame
            //      Decrease $sp by size = Local + Out + 2
            //         1 for Return address
            //         1 for frame pointer
            System.out.println("  subu $sp $sp " + (temp.stack.local + temp.stack.out + 2)*4);
            //   Saving the return register $ra at $fp - 1 (if the function calls any other function)
            //      Note that $fp now holds the old $sp
            if(callsFunction){
                System.out.println("  sw $ra -4($fp)");
            }

            //translate rest of instructions
            for(int a = 0; a < root.functions[i].body.length; ++a){
                //Print labels in correct spots
                for(int b = 0; b< root.functions[i].labels.length; ++b){
                    if(root.functions[i].labels[b].instrIndex == a){
                        System.out.println(root.functions[i].labels[b].ident + ":");
                    }
                }

                root.functions[i].body[a].accept(temp,mv);
            }
            //Restoring the return register $ra (if the function calls any other function) from $fp - 1
            if(callsFunction){
                System.out.println("  lw $ra -4($fp)");
            }
            //Restore $fp from $fp - 2
            System.out.println("  lw $fp -8($fp)");
            //Popping the frame
            //Increasing $sp by size = Local + Out + 2
            System.out.println("  addu $sp $sp " + (temp.stack.out + 2 + temp.stack.local)*4);
            //Jumping to the return register $ra
            System.out.println("  jr $ra");

            System.out.println();
        }
        printTail();
        //Ending
        System.out.println(".data");
        System.out.println(".align 0");
        System.out.println("_newline: .asciiz \"\\n\"");
        System.out.println("_str0: .asciiz \"null pointer\\n\"");
        System.out.println("_str1: .asciiz \"array index out of bounds\\n\"");
    }
}
