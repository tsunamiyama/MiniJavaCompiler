import syntaxtree.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import visitor.GJDepthFirst;

public class vaporVisitor extends GJDepthFirst<String, SymbolTableVisitor>{
    String currentClass = "";
    int varCounter = 0;
    int nullCounter = 1;
    String newObjClass = "";
    int ifcounter = 1;
    int whilecounter = 1;
    int andCounter = 1;
    int tabCount = 1;
    int boundCounter = 1;
    int misCounter = 1;
    boolean isLeft = false;
    boolean arrayAllo = false;

    public void printAllo(){
        System.out.println("func AllocArray(size)");
        System.out.println("\tbytes = MulS(size 4)");
        System.out.println("\tbytes = Add(bytes 4)");
        System.out.println("\tv = HeapAllocZ(bytes)");
        System.out.println("\t[v] = size");
        System.out.println("\tret v");
    }

    public void printRecords(SymbolTableVisitor s){
        for(Map.Entry<String,cRecord> entry : s.cRecords.entrySet()){
            System.out.println(entry.getKey() + " " + entry.getValue().size);
        }
    }

    public void printTables(SymbolTableVisitor s){
        for(Map.Entry<String,VTable> entry : s.vTables.entrySet()){
            System.out.println(entry.getKey());
            entry.getValue().print();
        }
    }

    public String visit(MainClass n, SymbolTableVisitor s){
        System.out.println("func Main()");
        currentClass = n.f1.f0.toString();
        n.f14.accept(this, s);
        n.f15.accept(this, s);
        System.out.println("\tret\n");
        varCounter = 0;
        return "";
    }

    public String visit(ClassDeclaration n, SymbolTableVisitor s){
        currentClass = n.f1.f0.toString();
        n.f3.accept(this, s);
        n.f4.accept(this, s);
        return "";
    }

    public String visit(VarDeclaration n, SymbolTableVisitor s){
        return "";
    }

    public String visit(MethodDeclaration n, SymbolTableVisitor s){
        System.out.print("func " + currentClass + "." + n.f2.f0.toString());
        System.out.println("(" + s.symbolTable.get(currentClass).methodTable.get(n.f2.f0.toString()).getParams() +")");
        n.f8.accept(this, s);
        varCounter = 0;

        String returnValue = n.f10.accept(this, s);
        if(returnValue == null){
            returnValue = "";
        }
        if(returnValue.contains("this")){
            String tempVar = "t." + varCounter;
            varCounter++;
            System.out.println(tempVar + " = " + returnValue);
            returnValue = tempVar;
        }
        System.out.println("\tret " + returnValue + "\n");
        return "";
    }

    public String visit(Statement n, SymbolTableVisitor s){
        n.f0.accept(this, s);
        return "";
    }

    public String visit(AssignmentStatement n, SymbolTableVisitor s){
        isLeft = true;
        String ident = n.f0.accept(this, s);
        isLeft = false;
        String exp = n.f2.accept(this,s);
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(ident + " = " + exp);
        return "";
    }

    public String visit(Identifier n, SymbolTableVisitor s){
        String id = "";
        if(s.cRecords.get(currentClass).crecord.containsKey(n.f0.toString()) && isLeft){
            id = "[this+" + (s.cRecords.get(currentClass).crecord.get(n.f0.toString())*4+4) + "]";
        } else {
            id = n.f0.toString();
        }
        return id;
    }

    public String visit(Expression n, SymbolTableVisitor s){
        isLeft = true;
        String a = (n.f0.accept(this, s));

        String tempVar = "t." + varCounter;
        varCounter++;

        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar + " = " + a);
        isLeft = false;
        return tempVar;
    }

    public String visit(PrimaryExpression n, SymbolTableVisitor s){
        //isLeft = true;
        String a = (n.f0.accept(this, s));

        String tempVar = "t." + varCounter;
        varCounter++;

        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar + " = " + a);
        //isLeft = false;
        return tempVar;
    }

    public String visit(AllocationExpression n, SymbolTableVisitor s){
        String tempVar = "t." + varCounter;
        varCounter++;
        int offset = s.cRecords.get(n.f1.f0.toString()).size;
        newObjClass = n.f1.f0.toString();
        offset = offset*4+4;
        String st = "";
        for(int i = 0; i < tabCount; ++i){
            st += "\t";
        }
        st += tempVar + " = HeapAllocZ(" + offset + ")";
        String st2 = "";
        for(int i = 0; i < tabCount; ++i){
            st2 += "\t";
        }
        st2 += "[" + tempVar + "] = :vmt_" + n.f1.f0.toString();
        String fin = st + "\n" + st2 + "\n";
        System.out.print(fin);
        return tempVar;
    }

    public String visit(MessageSend n, SymbolTableVisitor s){
        String id = n.f0.accept(this, s);
        String meth = n.f2.accept(this, s);
        String temps = n.f4.accept(this,s);
        String args = "";
        //System.out.println(temps);
        if(temps != null){
            args = " " + temps;
        }
        int off = s.vTables.get(newObjClass).vtable.get(meth);
        if(!id.equals("this")) {
            for(int i = 0; i < tabCount; ++i){
                System.out.print("\t");
            }
            System.out.println("if " + id + " goto " + ":null" + nullCounter);
            for(int i = 0; i < tabCount + 1; ++i){
                System.out.print("\t");
            }
            System.out.println("Error(\"null pointer\")");
            for(int i = 0; i < tabCount; ++i){
                System.out.print("\t");
            }
            System.out.println("null" + nullCounter + ":");
            nullCounter++;
        }

        String tempVar = "t." + varCounter;
        varCounter++;
        String s1 = "";
        for(int i = 0; i < tabCount; ++i){
            s1 += "\t";
        }
        s1 += tempVar + " = [" + id + "]" + "\n";
        System.out.print(s1);

        String s2 = "";
        for(int i = 0; i < tabCount; ++i){
            s2 += "\t";
        }
        s2 += tempVar + " = [" + tempVar + "+" + off*4 + "]\n";
        System.out.print(s2);

        String tempVar2 = "t." + varCounter;
        varCounter++;

        String s3 ="";
        for(int i = 0; i < tabCount; ++i){
            s3 += "\t";
        }
        s3 += tempVar2 + " = call " + tempVar + "(" + id + args + ")";
        //String s3 ="call " + tempVar + "(" + id + args + ")";
        System.out.println(s3);

        return tempVar2;
    }

    public String visit(PrintStatement n, SymbolTableVisitor s){
        String a = n.f2.accept(this, s);
        /*String tempVar = "t." + varCounter;
        varCounter++;*/
        //System.out.println("\t" + tempVar + " = " + a);
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("PrintIntS(" + a + ")");
        return "";
    }

    public String visit(ExpressionList n, SymbolTableVisitor s){
        String s1 = n.f0.accept(this, s);
        String s2 = "";
        int size = n.f1.size();
        for(int i = 0; i < size; ++i){
            s2 += n.f1.elementAt(i).accept(this, s);
        }
        return s1 + s2;
    }

    public String visit(ExpressionRest n, SymbolTableVisitor s){
        String s1 = n.f1.accept(this, s);
        s1 = " " + s1;
        return s1;
    }

    public String visit(IntegerLiteral n, SymbolTableVisitor s){
        return n.f0.toString();
    }

    public String visit(TrueLiteral n, SymbolTableVisitor s){
        String r = "1";
        return r;
    }

    public String visit(FalseLiteral n, SymbolTableVisitor s){
        String r = "0";
        return r;
    }

    public String visit(PlusExpression n, SymbolTableVisitor s){
        String s1 = n.f0.accept(this, s);
        String s2 = n.f2.accept(this, s);
        String tempVar = "t." + varCounter;
        varCounter++;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar + " = Add(" + s1 + " " + s2 + ")");
        return tempVar;
    }

    public String visit(IfStatement n, SymbolTableVisitor s){
        String cond = n.f2.accept(this, s);
        String tempVar = "t." + varCounter;
        varCounter++;
        String iflabel = "if" + ifcounter + "_else";
        String ifEnd = "if" + ifcounter + "_end";
        ifcounter++;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar + " = " + cond);
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("if0 " + tempVar + " goto :" + iflabel);
        tabCount++;
        n.f4.accept(this,s);
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("goto :" + ifEnd);
        tabCount--;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(iflabel + ":");
        tabCount++;
        n.f6.accept(this, s);
        tabCount--;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(ifEnd + ":");
        return "";
    }

    public String visit(CompareExpression n, SymbolTableVisitor s){
        String tempVar = "t." + varCounter;
        varCounter++;
        String tempVar2 = "t." + varCounter;
        varCounter++;
        String cond1 = n.f0.accept(this, s);
        String cond2 = n.f2.accept(this, s);
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar + " = " + cond1);
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar2 + " = " + cond2);
        return "LtS(" + tempVar + " " + tempVar2 + ")";
    }

    public String visit(NotExpression n, SymbolTableVisitor s){
        String tempVar = "t." + varCounter;
        varCounter++;
        String exp = n.f1.accept(this,s);
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar + " = " + exp);
        String tempVar2 = "t." + varCounter;
        varCounter++;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar2 + " = Sub(1 " + tempVar + ")");
        return tempVar2;
    }

    public String visit(BracketExpression n, SymbolTableVisitor s){
        return n.f1.accept(this, s);
    }

    public String visit(ThisExpression n, SymbolTableVisitor s){
        return "this";
    }

    public String visit(WhileStatement n, SymbolTableVisitor s){
        String whileLabel = "while" + whilecounter + "_top";
        String whileEnd = "while" + whilecounter + "_end";
        whilecounter++;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(whileLabel + ":");
        String arg = n.f2.accept(this, s);
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("if0 " + arg + " goto :" + whileEnd);
        tabCount++;
        n.f4.accept(this,s);
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("goto :" + whileLabel);
        tabCount--;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(whileEnd + ":");
        return "";
    }

    public String visit(AndExpression n, SymbolTableVisitor s){
        String andLabel = "ss" + andCounter + "_else";
        String andEnd = "ss" + andCounter + "_end";
        andCounter++;
        String tempVar = "t." + varCounter;
        varCounter++;

        String arg1 = n.f0.accept(this, s);
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("if0 " + arg1 + " goto :" + andLabel);
        tabCount++;
        String arg2 = n.f2.accept(this, s);
        tabCount--;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("goto :" + andEnd);
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(andLabel + ":");
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar + " = 0");
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(andEnd + ":");

        return tempVar;
    }

    public String visit(ArrayAllocationExpression n, SymbolTableVisitor s){
        String arg = n.f3.accept(this, s);
        String tempVar = "t." + varCounter;
        varCounter++;

        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar + " = call :AllocArray(" + arg + ")");
        arrayAllo = true;
        return tempVar;
    }

    public String visit(ArrayAssignmentStatement n, SymbolTableVisitor s){
        String arr = n.f0.accept(this, s);
        String ind = n.f2.accept(this, s);
        String newVal = n.f5.accept(this, s);
        String tempVar = "t." + varCounter;
        varCounter++;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar + " = " +  arr);
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("if " + tempVar + " goto " + ":null" + nullCounter);
        for(int i = 0; i < tabCount + 1; ++i){
            System.out.print("\t");
        }
        System.out.println("Error(\"null pointer\")");
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("null" + nullCounter + ":");
        nullCounter++;

        String tempVar2 = "t." + varCounter;
        varCounter++;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar2 + " = " +  "[" + tempVar + "]");
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar2 + " = Lt(" + ind + " " + tempVar2 + ")");

        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("if " + tempVar2 + " goto " + ":bounds" + boundCounter);
        for(int i = 0; i < tabCount + 1; ++i){
            System.out.print("\t");
        }
        System.out.println("Error(\"array index out of bounds\")");
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("bounds" + boundCounter + ":");
        boundCounter++;

        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar2 + " = MulS(" + ind + " 4)");
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar2 + " = Add(" + tempVar2 + " " + tempVar + ")");
        for(int i = 0; i < tabCount ; ++i){
            System.out.print("\t");
        }
        System.out.println("[" + tempVar2 + "+4] = " + newVal);
        return "";
    }

    public String visit(MinusExpression n, SymbolTableVisitor s){
        String s1 = n.f0.accept(this, s);
        String s2 = n.f2.accept(this, s);
        String tempVar = "t." + varCounter;
        varCounter++;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar + " = Sub(" + s1 + " " + s2 + ")");
        return tempVar;
    }

    public String visit(TimesExpression n, SymbolTableVisitor s){
        String s1 = n.f0.accept(this, s);
        String s2 = n.f2.accept(this, s);
        String tempVar = "t." + varCounter;
        varCounter++;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar + " = MulS(" + s1 + " " + s2 + ")");
        return tempVar;
    }

    public String visit(ArrayLookup n, SymbolTableVisitor s){
        String arr = n.f0.accept(this, s);
        String ind = n.f2.accept(this, s);
        String tempVar = "t." + varCounter;
        varCounter++;

        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar + " = " +  arr);

        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("if " + tempVar + " goto " + ":null" + nullCounter);
        for(int i = 0; i < tabCount + 1; ++i){
            System.out.print("\t");
        }
        System.out.println("Error(\"null pointer\")");
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("null" + nullCounter + ":");
        nullCounter++;

        String tempVar2 = "t." + varCounter;
        varCounter++;
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar2 + " = " +  "[" + tempVar + "]");
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar2 + " = Lt(" + ind + " " + tempVar2 + ")");

        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("if " + tempVar2 + " goto " + ":bounds" + boundCounter);
        for(int i = 0; i < tabCount + 1; ++i){
            System.out.print("\t");
        }
        System.out.println("Error(\"array index out of bounds\")");
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println("bounds" + boundCounter + ":");
        boundCounter++;

        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar2 + " = MulS(" + ind + " 4)");
        for(int i = 0; i < tabCount; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar2 + " = Add(" + tempVar2 + " " + tempVar + ")");

        String tempVar3 = "t." + varCounter;
        varCounter++;

        for(int i = 0; i < tabCount ; ++i){
            System.out.print("\t");
        }
        System.out.println(tempVar3 + " = [" + tempVar2 + "+4]");
        return tempVar3;
    }
}
