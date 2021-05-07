import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import cs132.util.ProblemException;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.parser.VaporParser;

public class V2VM {
    public static boolean containsCall = false;

    public static VaporProgram parseVapor(InputStream in) throws IOException {
        VBuiltIn.Op[] ops = {
                Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
                Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };
        boolean allowLocals = true;
        String[] registers = null;
        boolean allowStack = false;

        VaporProgram tree;
        try {
            tree = VaporParser.run(new InputStreamReader(in),1,1,java.util.Arrays.asList(ops),allowLocals,registers,allowStack);
        }
        catch (ProblemException ex) {
            System.out.println(ex.getMessage());
            return null;
        }

        return tree;
    }

    public static void printHeader(VaporProgram root){
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
        for(int i = 0; i < classNames.size(); ++i){
            System.out.println("const vmt_" + classNames.elementAt(i));
            for(int j = 0; j < functionNames.size(); ++j){
                if(functionNames.elementAt(j).contains(classNames.elementAt(i))){
                    System.out.println("\t:" + functionNames.elementAt(j));
                }
            }
        }
        //Error on tree infinite loops
        for(int i = 0; i < classNames.size(); ++i){
            if(classNames.elementAt(i).contains("Tree")){
                System.out.println("error");
            }
        }
        System.out.println();
    }

    public static int getNumArgs(VCall s){
        return s.args.length;
    }

    public static Vector<instruction> createFlowAnalysis(VFunction s) throws Exception {
        boolean isConv = false;
        //Create new instruction nodes for each instruction and put list
        Vector<instruction> insList = new Vector<>();
        for(int i = 0; i < s.body.length; ++i){
            instruction newIns = new instruction();
            newIns.insNum = i;
            insList.add(newIns);
            vmVisitor vm = new vmVisitor();
            //System.out.println(s.body[i].getClass().getSimpleName());
            s.body[i].accept(insList, vm);
            if(s.body[i].getClass().getSimpleName().equals("VCall")){
                containsCall = true;
            }
        }
        //Connect all nodes to create flow diagram
        for(int i = 0; i < insList.size() - 1; ++i){
            insList.elementAt(i).out_nodes.add(insList.elementAt(i+1));
            //System.out.println("Node " + i + "->" + "Node " + (i+1));
        }
        for(int i = 1; i < insList.size(); ++i){
            insList.elementAt(i).in_nodes.add(insList.elementAt(i-1));
            //System.out.println("Node " + i + "<-" + "Node " + (i-1));
        }

        //Connect nodes with branch and goto statements to the correct nodes
        for(int i = 0; i < insList.size(); i++){
            if(insList.elementAt(i).goto_label != 0){
                /*System.out.print(insList.elementAt(i).goto_label_name + ":");
                System.out.println(insList.elementAt(i).goto_label);
                for(int a = 0; a < insList.elementAt(insList.elementAt(i).goto_label).def.size(); ++a){
                    System.out.println(insList.elementAt(insList.elementAt(i).goto_label).def.elementAt(a));
                }*/
                insList.elementAt(i).out_nodes.add(insList.elementAt(insList.elementAt(i).goto_label));
                insList.elementAt(insList.elementAt(i).goto_label).in_nodes.add(insList.elementAt(i));
            }
        }
        //TEST
        /*for(int i = 0; i < insList.size(); ++i){
            System.out.println("Node" + i);
            System.out.print("IN ->");
            for(int a = 0; a < insList.elementAt(i).in_nodes.size(); ++a){
                System.out.print(insList.elementAt(i).in_nodes.elementAt(a).insNum + " ");
            }
            System.out.println();
            System.out.print("OUT <-");
            for(int a = 0; a < insList.elementAt(i).out_nodes.size(); ++a){
                System.out.print(insList.elementAt(i).out_nodes.elementAt(a).insNum + " ");
            }
            System.out.println();
        }*/

        //Change all in and out sets until convergence
        while(!isConv){
            for(int i = 0; i < insList.size(); ++i){
                //Clear in' and Out'
                insList.elementAt(i).inPrime.clear();
                insList.elementAt(i).outPrime.clear();
                //Copy in and out into in' and out'
                insList.elementAt(i).inPrime.addAll(insList.elementAt(i).in);
                insList.elementAt(i).outPrime.addAll(insList.elementAt(i).out);
                //Clear in and out?
                //insList.elementAt(i).in.clear();
                //insList.elementAt(i).out.clear();
                //Insert new values into in set
                //insList.elementAt(i).in.addAll(insList.elementAt(i).use); //use set
                for(int c = 0; c < insList.elementAt(i).use.size(); ++c){ //add use set
                    String newval1 = insList.elementAt(i).use.elementAt(c);
                    if(!insList.elementAt(i).in.contains(newval1)){
                        insList.elementAt(i).in.add(newval1);
                    }
                }
                for(int c = 0; c < insList.elementAt(i).out.size(); ++c){ //add out set
                    String newval = insList.elementAt(i).out.elementAt(c);
                    if((!insList.elementAt(i).in.contains(newval)) && (!insList.elementAt(i).def.contains(newval))){
                        insList.elementAt(i).in.add(newval);
                    }
                }
                //Add values into out set ->go to each node connected outwards from current one and add its in set
                for(int a = 0; a < insList.elementAt(i).out_nodes.size(); ++a){
                    for(int b = 0; b < insList.elementAt(i).out_nodes.elementAt(a).in.size(); ++b){
                        String newVar = insList.elementAt(i).out_nodes.elementAt(a).in.elementAt(b);
                        if(!insList.elementAt(i).out.contains(newVar)) {
                            insList.elementAt(i).out.add(newVar);
                        }
                    }
                }
                //Check if in/out sets are equal to in'/out' sets
                if(insList.elementAt(i).inPrime.equals(insList.elementAt(i).in) && insList.elementAt(i).outPrime.equals(insList.elementAt(i).out)){
                    insList.elementAt(i).converge = true;
                } else{
                    insList.elementAt(i).converge = false;
                }
            }
            //Check if all instruction sets have converged
            int count = 0;
            for(int e = 0; e < insList.size(); ++e) {
                if(insList.elementAt(e).converge){
                    ++count;
                }
            }
            if(count == insList.size()){
                isConv = true;
            }
        }
         //This Prints The IN and OUT sets for each node
        /*for(int i = 0; i < insList.size(); ++i){
            System.out.println("Node " + i);
            System.out.print("IN:");
            for(int a = 0; a < insList.elementAt(i).in.size(); ++a){
                System.out.print(insList.elementAt(i).in.elementAt(a) + ", ");
            }
            System.out.print("OUT:");
            for(int b = 0; b < insList.elementAt(i).out.size(); ++b){
                System.out.print(insList.elementAt(i).out.elementAt(b) + ", ");
            }
            System.out.println();
        }*/
        return insList;
    }

    public static Vector<instruction> activeVariables(VFunction s) throws Exception {
        Vector<instruction> flowDiagram = createFlowAnalysis(s);
        //Add in[n] U def[n] into active set of each node n
        for(int i = 0; i < flowDiagram.size(); ++i){
            flowDiagram.elementAt(i).active_variables.addAll(flowDiagram.elementAt(i).in);
            for(int c = 0; c < flowDiagram.elementAt(i).def.size(); ++c){
                String newval = flowDiagram.elementAt(i).def.elementAt(c);
                if(!flowDiagram.elementAt(i).active_variables.contains(newval)){
                    flowDiagram.elementAt(i).active_variables.add(newval);
                }
            }
        }
        // This prints the active variables for testing
        /*for(int i = 0; i < flowDiagram.size(); ++i){
            System.out.println("Node " + i);
            for(int a = 0; a < flowDiagram.elementAt(i).active_variables.size(); ++a){
                System.out.print(flowDiagram.elementAt(i).active_variables.elementAt(a) + ", ");
            }
            System.out.println();
        }*/
        return flowDiagram;
    }

    public static Vector<interval> activeIntervals(VFunction s) throws Exception{
        Vector<instruction> flowDiagram = activeVariables(s);
        Vector<String> variables = new Vector<>();
        Vector<interval> intervalList = new Vector<>();

        for(int i = 0; i < flowDiagram.size(); ++i){
            for(int j = 0; j < flowDiagram.elementAt(i).active_variables.size(); ++j){
                if(!variables.contains(flowDiagram.elementAt(i).active_variables.elementAt(j))){
                    variables.add(flowDiagram.elementAt(i).active_variables.elementAt(j));
                }
            }
        }

        //Print out list of variables which we will be analyzing
        /*for(int i = 0; i < variables.size(); ++i){
            System.out.println(variables.elementAt(i));
        }*/

        for(int i = 0; i < variables.size(); ++i){
            String var = variables.elementAt(i);
            for(int a = 0; a < flowDiagram.size(); ++a){
                if(flowDiagram.elementAt(a).active_variables.contains(var)){
                    interval newInterval = new interval();
                    newInterval.var = var;
                    newInterval.start = a;
                    for(int b = a; b < flowDiagram.size(); ++b){
                        if(!flowDiagram.elementAt(b).active_variables.contains(var)){
                            newInterval.end = b-1;
                            intervalList.add(newInterval);
                            a=b;
                            break;
                        }
                        if(b == flowDiagram.size()-1){
                            newInterval.end = b;
                            intervalList.add(newInterval);
                            a=b;
                            break;
                        }
                    }
                }
            }
        }
        /*for(int i = 0; i < flowDiagram.size(); ++i){
            System.out.println("Node " + i);
            for(int a = 0; a < flowDiagram.elementAt(i).active_variables.size(); ++a){
                System.out.print(flowDiagram.elementAt(i).active_variables.elementAt(a) + ", ");
            }
            System.out.println();
        }*/
        Vector<interval> adjustedList = new Vector<interval>();
        Vector<interval> intervalList2 = new Vector<>();
        intervalList2.addAll(intervalList);
        for(int i = 0; i < intervalList2.size(); ++i){
            interval newInterval = new interval();
            newInterval.var = intervalList2.elementAt(i).var;
            newInterval.start = intervalList2.elementAt(i).start;
            newInterval.end = intervalList2.elementAt(i).end;
            for(int a = i+1; a < intervalList2.size(); ++a){
                if(intervalList2.elementAt(i).var.equals(intervalList2.elementAt(a).var)){
                    if(intervalList2.elementAt(i).start < intervalList2.elementAt(a).start){
                        newInterval.start = intervalList2.elementAt(i).start;
                    } else {
                        newInterval.start = intervalList2.elementAt(a).start;
                    }
                    if(intervalList2.elementAt(i).end > intervalList2.elementAt(a).end){
                        newInterval.end = intervalList2.elementAt(i).end;
                    } else{
                        newInterval.end = intervalList2.elementAt(a).end;
                    }
                    intervalList2.removeElementAt(a);
                    a = i;
                }
            }
            adjustedList.add(newInterval);
        }
        /*for(int i = 0; i < intervalList.size(); ++i){
            System.out.println(intervalList.elementAt(i).var + ": " + intervalList.elementAt(i).start + "," + intervalList.elementAt(i).end);
        }
        System.out.println("\nADJUSTED:");*/
        /*for(int i = 0; i < adjustedList.size(); ++i){
            System.out.println(adjustedList.elementAt(i).var + ": " + adjustedList.elementAt(i).start + "," + adjustedList.elementAt(i).end);
        }*/
        //return intervalList;
        return adjustedList;
    }

    public static func linearScan(VFunction s) throws Exception{
        Vector<interval> intervalList = activeIntervals(s);
        Vector<interval> activeList = new Vector<>();
        Vector<String> regList = new Vector<String>();
        Vector<String> inStack = new Vector<String>();
        Vector<String> localStack = new Vector<String>();
        HashMap<String, String> regAllocation = new HashMap<>();
        func currentFunc = new func();

        //Add $s and $t registers to regList
        for(int i = 0; i < 9; ++i){
            regList.add("$t" + i);
        }
        for(int i = 0; i < 8; ++i){
            regList.add("$s" + i);
        }

        //Parameters are mapped to $a, excess params left in inStack, excess $a registers added to end of pool of registers
        int paramRegisters = 0;
        for(int i = 0; i < s.params.length; ++i){
            inStack.add(s.params[i].toString());
            if(paramRegisters < 4){
                regAllocation.put(s.params[i].toString(),"$a" + paramRegisters);
                //Remove parameters from interval list so they don't get removed
                for(int z = 0; z < intervalList.size(); ++z){
                    if((intervalList.elementAt(z).var).equals(s.params[i].toString())){
                        //intervalList.removeElementAt(z);
                        intervalList.elementAt(z).var = "$a" + paramRegisters;
                    }
                }
                ++paramRegisters;
            } else{
                regAllocation.put(s.params[i].toString(),"in["+ i +"]");
                for(int z = 0; z < intervalList.size(); ++z){
                    if((intervalList.elementAt(z).var).equals(s.params[i].toString())){
                        //intervalList.removeElementAt(z);
                        intervalList.elementAt(z).var = "in["+ i +"]";
                    }
                }
            }
        }/*
        if(paramRegisters < 4){
            for(int i = paramRegisters; i < 4; ++i){
                regList.add("$a" + i);
            }
        }*/
        currentFunc.inStack.addAll(inStack);

        //Sort intervalList by start time
        interval temp = new interval();
        for(int i = 0; i < intervalList.size()-1; ++i){
            for(int a = i+1; a < intervalList.size(); ++a){
                if(intervalList.elementAt(a).start < intervalList.elementAt(i).start){
                    temp = intervalList.elementAt(i);
                    intervalList.setElementAt(intervalList.elementAt(a), i);
                    intervalList.setElementAt(temp, a);
                }
            }
        }

        //LinearScanRegisterAllocation
        for(int i = 0; i < intervalList.size(); ++i){
            //ExpireOldIntervals
                //Sort activeList by increasing endpoint
            for(int a = 0; a < activeList.size()-1; ++a){
                for(int b = a+1; b < activeList.size(); ++b){
                    if(activeList.elementAt(b).end < activeList.elementAt(a).end){
                        temp = activeList.elementAt(a);
                        activeList.setElementAt(activeList.elementAt(b), a);
                        activeList.setElementAt(temp, b);
                    }
                }
            }
            //foreach interval j in active, in order or increasing end point
            for(int j = 0; j < activeList.size(); ++j){
                if(activeList.elementAt(j).end > intervalList.elementAt(i).start){
                    break;
                } else{
                    regList.add(regAllocation.get(activeList.elementAt(j).var));
                    activeList.remove(activeList.elementAt(j));
                }
            }
            //End of ExpireOldIntervals
            if(activeList.size() == regList.size()){
                //SpillAtInterval(i)
                localStack.add(activeList.lastElement().var);
                if(activeList.lastElement().end > intervalList.elementAt(i).end){
                    String spillReg = regAllocation.get(localStack.lastElement());
                    regAllocation.put(intervalList.elementAt(i).var,spillReg);
                    regAllocation.remove(localStack.lastElement());
                    regAllocation.put(localStack.lastElement(),"local[" + (localStack.size()-1) + "]");
                    activeList.remove(activeList.lastElement());
                    activeList.add(intervalList.elementAt(i));
                    //Resort Active List by end point
                    for(int d = 0; d < activeList.size()-1; ++d){
                        for(int f = d+1; f < activeList.size(); ++f){
                            if(activeList.elementAt(f).end < activeList.elementAt(d).end){
                                temp = activeList.elementAt(d);
                                activeList.setElementAt(activeList.elementAt(f), d);
                                activeList.setElementAt(temp, f);
                            }
                        }
                    }
                } else {
                    localStack.add(intervalList.elementAt(i).var);
                    regAllocation.put(intervalList.elementAt(i).var, "local[" + (localStack.size()-1) + "]");
                }
            } else{
                regAllocation.put(intervalList.elementAt(i).var,regList.firstElement());
                regList.remove(0);
                activeList.add(intervalList.elementAt(i));
            }
        }
        //Get the size of the out stack -> largest size of parameters of called functions
        int maxSize = 0;
        for(int i = 0; i < s.body.length; ++i){
            if(s.body[i].getClass().getSimpleName().equals("VCall")){
                if(getNumArgs((VCall) s.body[i]) > maxSize) {
                    maxSize = getNumArgs((VCall) s.body[i]);
                }
            }
        }
        currentFunc.outStack = new Vector<>(maxSize);
        for(int i = 0; i < maxSize; ++i){
            currentFunc.outStack.add(null);
        }

        //Add all $s and $t registers to local stack since they are saved
        for(Map.Entry<String,String> entry : regAllocation.entrySet()){
            if(entry.getValue().contains("$t") || entry.getValue().contains("$s")){
                currentFunc.localStack.add(entry.getValue());
            }
        }

        //Print all info for testing purposes
        /*System.out.println("IN STACK:");
        for(int i = 0; i < currentFunc.inStack.size(); ++i){
            System.out.print(currentFunc.inStack.elementAt(i) + " ");
        }
        System.out.println("\nLOCAL STACK:");
        for(int i = 0; i < currentFunc.localStack.size(); ++i){
            System.out.print(currentFunc.localStack.elementAt(i) + " ");
        }
        System.out.println("\nOUT STACK SIZE = " + currentFunc.outStack.size());
        System.out.println("REG ALLOCATION: ");
        for(Map.Entry<String,String> entry : regAllocation.entrySet()){
            System.out.println(entry.getKey() + " " + entry.getValue());
        }*/

        //Add in the interval list for use to determine when $t vars are used later
        currentFunc.intervalList = intervalList;
        currentFunc.regAlloc = regAllocation;

        return currentFunc;
    }

    public static void main(String[] args) throws Exception {
        InputStream fileStream = System.in;
        VaporProgram root = parseVapor(fileStream);
        //For Testing of Liveness Analysis/Register Allocation
        //for(int i = 0; i < root.functions.length; ++i) {
        /*for(int i = 21; i <= 21; ++i) {
            func currentFunc = linearScan(root.functions[i]);
            printVisitor pv = new printVisitor();
            System.out.print("func " + root.functions[i].ident + " [");
            System.out.print("in " + currentFunc.inStack.size() + ", ");
            System.out.print("out " + currentFunc.outStack.size() + ", ");
            System.out.println("local " + currentFunc.localStack.size() + "]");
            for(int a = 0; a < root.functions[i].body.length; ++a){
                //Print label
                for(int b = 0; b < root.functions[i].labels.length; ++b) {
                    if (a == root.functions[i].labels[b].instrIndex) {
                        System.out.println(root.functions[i].labels[b].ident + ":");
                    }
                }

                //Print Translated Instruction
                currentFunc.currInst = a;
                root.functions[i].body[a].accept(currentFunc, pv);
            }
        }*/
        printHeader(root);
        for(int i = 0; i < root.functions.length; ++i){
            containsCall = false;
            func currentFunc = linearScan(root.functions[i]);
            printVisitor pv = new printVisitor();

            //Print out the func header with array sizes
            System.out.print("func " + root.functions[i].ident + " [");
            System.out.print("in " + currentFunc.inStack.size() + ", ");
            System.out.print("out " + currentFunc.outStack.size() + ", ");
            System.out.println("local " + currentFunc.localStack.size() + "]");

            //Print out statements to save all $s register values we use
            for(int s = 0; s < currentFunc.localStack.size() ; ++s){
                if(currentFunc.localStack.elementAt(s).contains("$s")){
                    System.out.println("\tlocal[" + s + "] = " + currentFunc.localStack.elementAt(s));
                }
            }

            //load parameters
            for(int p = 0; p < root.functions[i].params.length; ++p){
                for(Map.Entry<String,String> entry : currentFunc.regAlloc.entrySet()){
                    if(entry.getKey().equals(root.functions[i].params[p].toString())){
                        if(!(currentFunc.regAlloc.get(entry.getValue()) == null)) {
                            System.out.println("\t" + currentFunc.regAlloc.get(entry.getValue()) + " = " + entry.getValue());
                        }
                    }
                }
            }

            //Print out the translated body instructions
            for(int a = 0; a < root.functions[i].body.length; ++a){
                //Print label
                for(int b = 0; b < root.functions[i].labels.length; ++b) {
                    if (a == root.functions[i].labels[b].instrIndex) {
                        System.out.println(root.functions[i].labels[b].ident + ":");
                    }
                }

                //Print Translated Instruction
                currentFunc.currInst = a;
                root.functions[i].body[a].accept(currentFunc, pv);
            }

            //Print out the loading of the old $s values into appropriate registers
            for(int s = 0; s < currentFunc.localStack.size() ; ++s){
                if(currentFunc.localStack.elementAt(s).contains("$s")){
                    System.out.println("\t" + currentFunc.localStack.elementAt(s) + " = local[" + s + "]");
                }
            }

            //Now we can print the ret statement
            System.out.println("\tret\n");
        }
    }
}
