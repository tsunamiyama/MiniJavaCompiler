import syntaxtree.*;
import visitor.DepthFirstVisitor;
import java.util.*;

class MethodInfo{
    String methType;
    String methID;
    Map<String,String> methVariables = new HashMap<String, String>();
    Map<String,String> methParameters = new HashMap<String, String>();

    public void print(){
        System.out.println("NEW METHOD: " + methType + " " + methID);
        for(Map.Entry<String,String> entry : methParameters.entrySet()){
            System.out.print("    ");
            System.out.println("PARAMETER: " + entry.getValue().toString() + ":" + entry.getKey());
        }
        for(Map.Entry<String,String> entry : methVariables.entrySet()){
            System.out.print("        ");
            System.out.println("METHOD VAR: " + entry.getValue().toString() + ":" + entry.getKey());
        }
    }
    public String find(String c, String m, String s){
        String result = "error";
        for(Map.Entry<String,String> entry : methVariables.entrySet()){
            if(entry.getKey().equals(s)){
                result = entry.getValue();
            }
        }
        if(result.equals("error")) {
            for (Map.Entry<String, String> entry : methParameters.entrySet()) {
                if (entry.getKey().equals(s)) {
                    return entry.getValue();
                }
            }
        }
        return result;
    }
}

class ClassInfo{
    String id;
    Map<String,MethodInfo> methodTable = new HashMap<String, MethodInfo>();
    Map<String,String> localVarTables = new HashMap<String, String>();

    public void print(){
        System.out.println("NEW CLASS: " + id);
        for(Map.Entry<String,String> entry : localVarTables.entrySet()){
            System.out.println("LOCAL VAR: " + entry.getValue().toString() + ":" + entry.getKey());
        }
        for(Map.Entry<String,MethodInfo> entry : methodTable.entrySet()){
            System.out.print("    ");
            entry.getValue().print();
        }
    }

    public String find(String c, String m, String s){
        String result = "error";
        for (Map.Entry<String, MethodInfo> entry : methodTable.entrySet()) {
            if (entry.getKey().equals(m)) {
                result = entry.getValue().find(c, m, s);
            }
        }
        if(result.equals("error")) {
            for (Map.Entry<String, MethodInfo> entry : methodTable.entrySet()) {
                if (entry.getKey().equals(s)) {
                    result = entry.getValue().methType;
                }
            }
        }
        if(result.equals("error")) {
            for (Map.Entry<String, String> entry : localVarTables.entrySet()) {
                if (entry.getKey().equals(s)) {
                    result = entry.getValue();
                }
            }
        }
        return result;
    }

    public boolean checkMethods(String s){
        for(Map.Entry<String,MethodInfo> entry : methodTable.entrySet()){
            if(entry.getKey().equals(s)){
                return true;
            }
        }
        return false;
    }
}

public class SymbolTableVisitor extends DepthFirstVisitor{
    Map<String,ClassInfo> symbolTable = new HashMap<String, ClassInfo>();
    ClassInfo lastClass;
    MethodInfo lastMethod;
    boolean isClass = false;
    boolean isMeth = false;
    String vt;

    public boolean checkClassForMethod(String c, String m){
        for(Map.Entry<String,ClassInfo> entry : symbolTable.entrySet()){
            if(entry.getKey().equals(c)){
                return entry.getValue().checkMethods(m);
            }
        }
        return false;
    }

    public boolean checkClasses(String s){
        for(Map.Entry<String,ClassInfo> entry : symbolTable.entrySet()){
            if(entry.getKey().equals(s)){
                return true;
            }
        }
        return false;
    }
    public void print(){
        for(Map.Entry<String,ClassInfo> entry : symbolTable.entrySet()){
            entry.getValue().print();
        }
    }

    public String find(String c,String m, String s){
        for(Map.Entry<String,ClassInfo> entry : symbolTable.entrySet()){
            if(entry.getKey().equals(c)) {
                if (!entry.getValue().find(c, m, s).equals("error")) {
                    return entry.getValue().find(c,m,s);
                }
            }
        }
        for(Map.Entry<String,ClassInfo> entry : symbolTable.entrySet()){
            if (!entry.getValue().find(c, m, s).equals("error")) {
                return entry.getValue().find(c,m,s);
            }
        }
        return "error";
    }

    public void visit(ClassDeclaration n){
        ClassInfo newClass = new ClassInfo();
        newClass.id = n.f1.f0.toString();
        symbolTable.put(newClass.id, newClass);
        lastClass = newClass;
        //System.out.println("CLASS: " + newClass.id);
        isClass = true;
        n.f3.accept(this);
        isClass = false;
        n.f4.accept(this);
    }

    public void visit(VarDeclaration n){
        n.f0.accept(this);
        String varType = vt;
        String varID = n.f1.f0.toString();
        if(isClass && !isMeth) {
            lastClass.localVarTables.put(varID, varType);
            //System.out.println("CLASS VAR: " + varType + " " + varID);
        } else if(isMeth){
                lastMethod.methVariables.put(varID,varType);
            //System.out.println("METH VAR: " + varType + " " + varID);
            }
    }

    public void visit(MethodDeclaration n){
        MethodInfo newMethod = new MethodInfo();
        lastMethod = newMethod;
        n.f1.accept(this);
        newMethod.methType = vt;
        newMethod.methID = n.f2.f0.toString();
        //System.out.println("NEW METH: " + newMethod.methType +" "+newMethod.methID);
        n.f4.accept(this);
        isMeth = true;
        n.f7.accept(this);
        isMeth = false;
        lastClass.methodTable.put(newMethod.methID, newMethod);
    }

    public void visit(FormalParameterList n){
        n.f0.accept(this);
        n.f1.accept(this);
    }

    public void visit(FormalParameter n){
        n.f0.accept(this);
        String type = vt;
        String id = n.f1.f0.toString();
        lastMethod.methParameters.put(id, type);
        //System.out.println("PARAM: " + type + " " + id);
    }

    public void visit(FormalParameterRest n){
        n.f1.accept(this);
    }

    public void visit(Type n){
        n.f0.accept(this);
    }

    public void visit(ArrayType n){
        vt = "int array";
    }

    public void visit(IntegerType n){
        vt = n.f0.toString();
    }

    public void visit(BooleanType n){
        vt = n.f0.toString();
    }

    public void visit(Identifier n){
        vt = n.f0.toString();
    }

}

