import syntaxtree.*;
import visitor.DepthFirstVisitor;
import visitor.GJDepthFirst;

public class TypeVisitor extends GJDepthFirst<String, SymbolTableVisitor> {
    boolean hasError = false;
    String errorMsg = "";
    String currentClass = "";
    String currentMethod = "";


    public void checkTypeError() {
        if(hasError) {
            System.out.println("Type error");
        }
    }

    public String visit(ClassDeclaration n, SymbolTableVisitor s){
        currentClass = n.f1.f0.toString();
        n.f3.accept(this, s);
        n.f4.accept(this, s);
        return n.f1.f0.toString();
    }

    public String visit(MethodDeclaration n, SymbolTableVisitor s){
        currentMethod = n.f2.f0.toString();
        String methType = n.f1.accept(this, s);
        n.f4.accept(this, s);
        n.f7.accept(this, s);
        n.f8.accept(this, s);
        String returnType = n.f10.accept(this, s);

        if(methType == returnType){
            currentMethod = "";
            return methType;
        } else{
            return "error";
        }
    }

    public String visit(Statement n, SymbolTableVisitor s){
        String arg = n.f0.accept(this, s);
        return arg;
    }

    public String visit(AndExpression n, SymbolTableVisitor s){
        String arg1 = n.f0.accept(this, s);
        String arg2 = n.f2.accept(this, s);
        if((arg1 == arg2) && arg1 == "boolean"){
            return "boolean";
        } else{
            hasError = true;
            //errorMsg = arg1 + arg2 + "AndExpression";
            //System.out.println(errorMsg);
            return "error";
        }
    }

    public String visit(CompareExpression n, SymbolTableVisitor s){
        String arg1 = n.f0.accept(this, s);
        String arg2 = n.f2.accept(this, s);
        if((arg1 == arg2) && arg1 == "int"){
            return "boolean";
        } else{
            hasError = true;
            //errorMsg = arg1 + arg2 + "CompareExpression";
            //System.out.println(errorMsg);
            return "error";
        }
    }

    public String visit(PlusExpression n, SymbolTableVisitor s){
        String arg1 = n.f0.accept(this, s);
        String arg2 = n.f2.accept(this, s);
        if((arg1 == arg2) && arg1 == "int"){
            return "int";
        } else{
            hasError = true;
            //errorMsg = arg1 + arg2 + "PlusExpression";
            //System.out.println(arg1);
            //System.out.println(arg2);
            //System.out.println(errorMsg);
            return "error";
        }
    }

    public String visit(MinusExpression n, SymbolTableVisitor s){
        String arg1 = n.f0.accept(this, s);
        String arg2 = n.f2.accept(this, s);
        if((arg1 == arg2) && arg1 == "int"){
            return "int";
        } else{
            hasError = true;
            //errorMsg = arg1 + arg2 + "MinusExpression";
            //System.out.println(errorMsg);
            return "error";
        }
    }

    public String visit(TimesExpression n, SymbolTableVisitor s){
        String arg1 = n.f0.accept(this, s);
        String arg2 = n.f2.accept(this, s);
        if((arg1 == arg2) && arg1 == "int"){
            return "int";
        } else{
            hasError = true;
            //errorMsg = arg1 + arg2 + "TimesExpression";
            //System.out.println(errorMsg);
            return "error";
        }
    }

    public String visit(ArrayLookup n, SymbolTableVisitor s){
        String arg1 = n.f0.accept(this, s);
        String arg2 = n.f2.accept(this, s);
        if(arg1 == "int array" && arg2 == "int"){
            return "int";
        } else{
            hasError = true;
            //errorMsg = arg1 + arg2 + "ArrayLookup";
            //System.out.println(errorMsg);
            return "error";
        }
    }

    public String visit(ArrayLength n, SymbolTableVisitor s){
        String arg1 = n.f0.accept(this, s);
        if(arg1 == "int array"){
            return "int";
        } else{
            hasError = true;
            //errorMsg = arg1 + "ArrayLength";
            //System.out.println(errorMsg);
            return "error";
        }
    }

    public String visit(AssignmentStatement n, SymbolTableVisitor s){
        String arg1 = n.f0.accept(this, s);
        String arg2 = n.f2.accept(this, s);
        //System.out.println(arg1);
        //System.out.println(arg2);

        if(arg1.equals(arg2)){
            return arg1;
        } else{
            hasError = true;
            //errorMsg = arg1 + arg2 + "AssignmentStatement";
            //System.out.println(errorMsg);
            return "error";
        }
    }

    public String visit(Expression n, SymbolTableVisitor s){
        return n.f0.accept(this, s);
    }

    public String visit(PrimaryExpression n, SymbolTableVisitor s){
        String a = n.f0.accept(this, s);
        return a;
    }

    public String visit(TrueLiteral n, SymbolTableVisitor s){
        return "boolean";
    }

    public String visit(FalseLiteral n, SymbolTableVisitor s){
        return "boolean";
    }

    public String visit(Identifier n, SymbolTableVisitor s){
        //System.out.println(currentClass + ":" +currentMethod);
        //System.out.println(n.f0.toString());
        //System.out.println(s.find(currentClass, currentMethod, n.f0.toString()));
        return s.find(currentClass, currentMethod, n.f0.toString());
    }

    public String visit(IntegerLiteral n, SymbolTableVisitor s){
        return "int";
    }

    public String visit(BracketExpression n, SymbolTableVisitor s){
        return n.f1.accept(this, s);
    }

    public String visit(NotExpression n, SymbolTableVisitor s){
        String arg = n.f1.accept(this,s);
        if(arg == "boolean"){
            return "boolean";
        } else{
            hasError = true;
            //errorMsg = arg + "NotExpression";
            //System.out.println(errorMsg);
            return "error";
        }
    }

    public String visit(AllocationExpression n, SymbolTableVisitor s){
        //return n.f1.accept(this, s);
        return n.f1.f0.toString();
    }

    public String visit(ArrayAllocationExpression n, SymbolTableVisitor s){
        String arg1 = n.f3.accept(this, s);
        if(arg1 == "int"){
            return "int array";
        } else{
            hasError = true;
            //errorMsg = arg1 + "ArrayAllocationExpression";
            //System.out.println(errorMsg);
            return "error";
        }
    }

    public String visit(MessageSend n, SymbolTableVisitor s){
        String arg1 = n.f0.accept(this, s); //class type
        String arg2 = n.f2.accept(this, s); //method type
        String arg3 = n.f4.accept(this, s); //method parameters
        String methName = n.f2.f0.toString();
        //System.out.println(arg1);
        //System.out.println(methName);
        //System.out.println(arg2);

        if(!s.checkClasses(arg1)){
            hasError = true;
            //errorMsg = arg1 + " " +  methName + " MessageSend, Primitive Class";
            //System.out.println(errorMsg);
            return "error";
        }
        if(!s.checkClassForMethod(arg1, methName)){
            hasError = true;
            //errorMsg = arg1 + " " + methName + " MessageSend, Class Doesn't have Method";
            //System.out.println(errorMsg);
            return "error";
        } else{
            //System.out.println(arg2);
            return arg2;
        }
    }

    public String visit(ThisExpression n, SymbolTableVisitor s){
        //System.out.println(currentClass);
        return currentClass;
    }

    public String visit(ArrayAssignmentStatement n, SymbolTableVisitor s){
        String arg1 = n.f0.accept(this, s);
        String arg2 = n.f2.accept(this, s);
        String arg3 = n.f5.accept(this, s);

        if(arg1 == "int array" && arg2 == "int" && arg3 == "int"){
            return "int";
        } else{
            hasError = true;
            //errorMsg = arg1 + arg2 + arg3 + "ArrayAssignmentStatement";
            //System.out.println(errorMsg);
            return "error";
        }
    }
}