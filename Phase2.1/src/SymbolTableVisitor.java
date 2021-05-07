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

    public String getParams(){
        String resp = "this ";
        for (Map.Entry<String, String> entry : methParameters.entrySet()) {
            resp += entry.getKey() + " ";
        }
        resp = resp.substring(0, resp.length()-1);
        return resp;
    }

    public String getNumParams(){
        int size = methParameters.size();
        return String.valueOf(size);
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

    public void printMethods(){
        for(Map.Entry<String,MethodInfo> entry : methodTable.entrySet()){
            System.out.println(":" + id + "." + entry.getKey());
        }
    }
}

class cRecord{
    int size = 0;
    Map<String, Integer>crecord = new HashMap<String, Integer>();
}
class VTable{
    String orig = "";
    Map<String, Integer>vtable = new HashMap<String, Integer>();

    public void print(){
        SortedMap<Integer, String>tab = new TreeMap<Integer, String>();
        if(!orig.equals("")) {
            System.out.println("const vmt_" + orig);
            for (Map.Entry<String, Integer> entry : vtable.entrySet()) {
                tab.put(entry.getValue(), entry.getKey());
            }
            for (Map.Entry<Integer, String> entry : tab.entrySet()) {
                System.out.println("\t:" + orig + "." + entry.getValue());
            }
            System.out.println();
        }
    }
}

public class SymbolTableVisitor extends DepthFirstVisitor{
    Map<String, VTable> vTables = new HashMap<String, VTable>();
    Map<String, cRecord>cRecords = new HashMap<String, cRecord>();
    int methoffsetCounter = 0;
    int fieldoffsetCounter = 0;

    Map<String,ClassInfo> symbolTable = new HashMap<String, ClassInfo>();
    ClassInfo lastClass;
    MethodInfo lastMethod;
    boolean isClass = false;
    boolean isMeth = false;
    boolean field = false;
    String vt;

    public void printVtablesFinal(){
        for(Map.Entry<String,VTable> entry : vTables.entrySet()){
            entry.getValue().print();
        }
    }

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
        String result = "error";
        for(Map.Entry<String,ClassInfo> entry : symbolTable.entrySet()){
            if(entry.getKey().equals(c)) {
                result = entry.getValue().find(c,m,s);
            }
        }
        if(result.equals("error")) {
            for (Map.Entry<String, ClassInfo> entry : symbolTable.entrySet()) {
                if (!entry.getValue().find(c, m, s).equals("error")) {
                    result = entry.getValue().find(c, m, s);
                }
            }
        }
        if(result.equals("error")) {
            for (Map.Entry<String, ClassInfo> entry : symbolTable.entrySet()) {
                if (entry.getKey().equals(s)) {
                    result = entry.getKey();
                }
            }
        }
        return result;
    }

    public void visit(MainClass n){
        VTable newTab = new VTable();
        vTables.put(n.f1.f0.toString(), newTab);
        cRecord newRec = new cRecord();
        cRecords.put(n.f1.f0.toString(), newRec);

        ClassInfo newClass = new ClassInfo();
        newClass.id = n.f1.f0.toString();
        symbolTable.put(newClass.id, newClass);
        lastClass = newClass;

        isClass = true;
        n.f14.accept(this);
        isClass = false;
        newTab.vtable.put("main", methoffsetCounter);
        methoffsetCounter = 0;
    }

    public void visit(ClassDeclaration n){
        VTable newTab = new VTable();
        vTables.put(n.f1.f0.toString(), newTab);
        cRecord newRec = new cRecord();
        cRecords.put(n.f1.f0.toString(), newRec);
        newTab.orig = n.f1.f0.toString();

        ClassInfo newClass = new ClassInfo();
        newClass.id = n.f1.f0.toString();
        symbolTable.put(newClass.id, newClass);
        lastClass = newClass;
        //System.out.println("CLASS: " + newClass.id);
        isClass = true;
        n.f3.accept(this);
        isClass = false;
        n.f4.accept(this);
        methoffsetCounter = 0;
    }

    public void visit(VarDeclaration n){
        if(isClass) {
            cRecords.get(lastClass.id).crecord.put(n.f1.f0.toString(), fieldoffsetCounter);
            fieldoffsetCounter++;
            cRecords.get(lastClass.id).size++;
        }

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
        vTables.get(lastClass.id).vtable.put(n.f2.f0.toString(), methoffsetCounter);
        methoffsetCounter++;

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

