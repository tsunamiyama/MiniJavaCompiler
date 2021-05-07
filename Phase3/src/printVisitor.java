import cs132.vapor.ast.*;

import java.util.HashMap;
import java.util.Map;

public class printVisitor extends VInstr.VisitorP<func, Exception> {
    @Override
    public void visit(func o, VAssign vAssign) throws Exception {
        if(o.regAlloc.containsKey(vAssign.source.toString())) {
            System.out.println("\t" + o.regAlloc.get(vAssign.dest.toString()) + " = " + o.regAlloc.get(vAssign.source.toString()));
        } else{
            System.out.println("\t" + o.regAlloc.get(vAssign.dest.toString()) + " = " + vAssign.source.toString());
        }
    }

    @Override
    public void visit(func o, VCall vCall) throws Exception {
        //Save $t variables
        for(int t = 0; t < o.localStack.size(); ++t){
            if(o.localStack.elementAt(t).contains("$t")) {
                System.out.println("\tlocal[" + t + "] = " + o.localStack.elementAt(t));
            }
        }
        //Save current argument registers to the in stack
        int regCount = 0;
        for(int a = 0; a < o.inStack.size(); ++a){
            if(regCount < 4){
                System.out.println("\tin[" + a + "] = $a" + a);
                regCount++;
            }
        }
        //Store params
        regCount = 0;
        for(int i = 0; i < vCall.args.length; ++i){
            String ar = vCall.args[i].toString();
            if(regCount < 4){
                if(o.regAlloc.containsKey(ar)) {
                    if(o.regAlloc.get(ar).contains("$a")){
                        System.out.println("\t$a" + regCount + " = " + o.regAlloc.get(o.regAlloc.get(ar)));
                        //System.out.println("\t$a" + regCount + " = in[" + regCount + "]");
                    }else {
                        System.out.println("\t$a" + regCount + " = " + o.regAlloc.get(ar));
                    }
                } else{
                    System.out.println("\t$a" + regCount + " = " + ar);
                }
                regCount++;
            } else{
                if(o.regAlloc.containsKey(ar)) {
                    if(o.regAlloc.get(ar).contains("$a")) {
                        //System.out.println("\tout[" + i + "] = in[" + (vCall.args.length - i) + "]" );
                        System.out.println("\tout[" + i + "] = " + o.regAlloc.get(o.regAlloc.get(ar)));
                    } else {
                        if(o.regAlloc.get(ar).contains("in")){
                            System.out.println("\tout[" + i + "] = " + o.regAlloc.get(o.regAlloc.get(ar)));
                        }else {
                            System.out.println("\tout[" + i + "] = " + o.regAlloc.get(ar));
                        }
                    }
                } else{
                    System.out.println("\tout[" + i + "] = " + ar);
                }
            }
        }
        //Call Statement
        if(!o.regAlloc.containsKey(vCall.addr.toString())){
            System.out.println("\tcall " + vCall.addr.toString());
        } else {
            System.out.println("\tcall " + o.regAlloc.get(vCall.addr.toString()));
        }

        //load all $t variables
        for(int t = 0; t < o.localStack.size(); ++t){
            if(o.localStack.elementAt(t).contains("$t")) {
                System.out.println("\t" + o.localStack.elementAt(t) + " = local[" + t + "]");
            }
        }

        //load current argument registers from the in stack
        regCount = 0;
        for(int a = 0; a < o.inStack.size(); ++a){
            if(regCount < 4){
                System.out.println("\t$a" + regCount + " = " + "in[" + regCount + "]");
                regCount++;
            }
        }

        System.out.println("\t" + o.regAlloc.get(vCall.dest.toString()) + " = $v0");
    }

    @Override
    public void visit(func o, VBuiltIn vBuiltIn) throws Exception {
        System.out.print("\t");
        if(vBuiltIn.dest != null){
            System.out.print(o.regAlloc.get(vBuiltIn.dest.toString()) + " = ");
        }
        System.out.print(vBuiltIn.op.name + "(");
        for(int i = 0; i < vBuiltIn.args.length; ++i){
            if(o.regAlloc.containsKey(vBuiltIn.args[i].toString())){
                if(o.regAlloc.get(vBuiltIn.args[i].toString()).contains("in")){
                    System.out.print(o.regAlloc.get(o.regAlloc.get(vBuiltIn.args[i].toString())));
                }else {
                    System.out.print(o.regAlloc.get(vBuiltIn.args[i].toString()));
                }
            } else {
                System.out.print(vBuiltIn.args[i]);
            }
            if(!(i == vBuiltIn.args.length-1)){
                System.out.print(" ");
            }
        }
        System.out.println(")");
    }

    @Override
    public void visit(func o, VMemWrite vMemWrite) throws Exception {
        int byteOff = ((VMemRef.Global)vMemWrite.dest).byteOffset;
        if(byteOff != 0){
            if(o.regAlloc.get(((VMemRef.Global) vMemWrite.dest).base.toString()).contains("in")) {
                System.out.print("\t[" + o.regAlloc.get(o.regAlloc.get(((VMemRef.Global) vMemWrite.dest).base.toString())) + "+" + byteOff + "] = ");
            } else{
                if(o.regAlloc.get(((VMemRef.Global) vMemWrite.dest).base.toString()).contains("$a")){
                    String temp = o.regAlloc.get(((VMemRef.Global) vMemWrite.dest).base.toString());
                    System.out.print("\t[" + o.regAlloc.get(temp) + "+" + byteOff + "] = ");
                } else {
                    System.out.print("\t[" + o.regAlloc.get(((VMemRef.Global) vMemWrite.dest).base.toString()) + "+" + byteOff + "] = ");
                }
            }
        }else {
            System.out.print("\t[" + o.regAlloc.get(((VMemRef.Global) vMemWrite.dest).base.toString()) + "] = ");
        }
        if(o.regAlloc.containsKey(vMemWrite.source.toString())){
            System.out.println(o.regAlloc.get(vMemWrite.source.toString()));
        }else {
            System.out.println(vMemWrite.source.toString());
        }
    }

    @Override
    public void visit(func o, VMemRead vMemRead) throws Exception {
        int byteOff = ((VMemRef.Global)vMemRead.source).byteOffset;
        if(byteOff != 0){
            if(o.regAlloc.get(((VMemRef.Global)vMemRead.source).base.toString()).contains("$a")){
                System.out.println("\t" + o.regAlloc.get(vMemRead.dest.toString()) + " = [" + o.regAlloc.get(o.regAlloc.get(((VMemRef.Global)vMemRead.source).base.toString())) + "+" + byteOff + "]");
            }else {
                System.out.println("\t" + o.regAlloc.get(vMemRead.dest.toString()) + " = [" + o.regAlloc.get(((VMemRef.Global) vMemRead.source).base.toString()) + "+" + byteOff + "]");
            }
        }else {
            if(o.regAlloc.get(((VMemRef.Global)vMemRead.source).base.toString()).contains("$a")){
                System.out.println("\t" + o.regAlloc.get(vMemRead.dest.toString()) + " = [" + o.regAlloc.get(o.regAlloc.get(((VMemRef.Global) vMemRead.source).base.toString())) + "]");
            }else {
                System.out.println("\t" + o.regAlloc.get(vMemRead.dest.toString()) + " = [" + o.regAlloc.get(((VMemRef.Global) vMemRead.source).base.toString()) + "]");
            }
        }
    }

    @Override
    public void visit(func o, VBranch vBranch) throws Exception {
        //Check positive for if/if0
        if(vBranch.positive) {
            if(o.regAlloc.get(vBranch.value.toString()).contains("$a")){
                System.out.println("\tif " + o.regAlloc.get(o.regAlloc.get(vBranch.value.toString())) + " goto " + vBranch.target.toString());
            }else {
                System.out.println("\tif " + o.regAlloc.get(vBranch.value.toString()) + " goto " + vBranch.target.toString());
            }
        } else{
            if(o.regAlloc.get(vBranch.value.toString()).contains("$a")){
                System.out.println("\tif0 " + o.regAlloc.get(o.regAlloc.get(vBranch.value.toString())) + " goto " + vBranch.target.toString());
            } else {
                System.out.println("\tif0 " + o.regAlloc.get(vBranch.value.toString()) + " goto " + vBranch.target.toString());
            }
        }
    }

    @Override
    public void visit(func o, VGoto vGoto) throws Exception {
        System.out.println("\tgoto " + vGoto.target.toString());
    }

    @Override
    public void visit(func o, VReturn vReturn) throws Exception {
        if(vReturn.value == null) {
            //do nothing
            System.out.print("");
        } else{
            if(o.regAlloc.containsKey(vReturn.value.toString())){
                System.out.print("\t");
                System.out.println("$v0 = " + o.regAlloc.get(vReturn.value.toString()));
            } else{
                System.out.print("\t");
                System.out.println("$v0 = " + vReturn.value.toString());
            }
        }
    }
}
