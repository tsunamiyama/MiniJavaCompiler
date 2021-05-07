import cs132.vapor.ast.*;

public class mipsVisitor extends VInstr.VisitorP<VFunction, Exception> {

    @Override
    public void visit(VFunction func, VAssign vAssign) throws Exception {
        if(!vAssign.source.toString().contains("$")){
            if(!vAssign.source.toString().contains(":")) {
                System.out.println("  li " + vAssign.dest.toString() + " " + vAssign.source.toString());
            }else{
                System.out.println("  la " + vAssign.dest.toString() + " " + vAssign.source.toString().substring(1));
            }
        }else {
            System.out.println("  move " + vAssign.dest.toString() + " " + vAssign.source.toString());
        }

    }

    @Override
    public void visit(VFunction func, VCall vCall) throws Exception {
        if(vCall.addr.toString().contains("$")){
            System.out.print("  jalr ");
            System.out.println(vCall.addr.toString());
        } else{
            System.out.println("  jal " + vCall.addr.toString().substring(1));
        }
    }

    @Override
    public void visit(VFunction func, VBuiltIn vBuiltIn) throws Exception {
        if(vBuiltIn.op.name.toString().equals("HeapAllocZ")){
            if(vBuiltIn.args[0].toString().contains("$")){
                System.out.println("  move $a0 " + vBuiltIn.args[0].toString());
            } else {
                System.out.println("  li $a0 " + vBuiltIn.args[0].toString());
            }
            System.out.println("  jal _heapAlloc");
            System.out.println("  move " + vBuiltIn.dest.toString() + " $v0");
        }
        if(vBuiltIn.op.name.toString().equals("Error")){
            System.out.print("  la $a0 ");
            if(vBuiltIn.args[0].toString().contains("null")){
                System.out.println("_str0");
            }
            if(vBuiltIn.args[0].toString().contains("array index out of bounds")){
                System.out.println("_str1");
            }
            System.out.println("  j _error");
        }
        if(vBuiltIn.op.name.toString().equals("PrintIntS")) {
            if(!vBuiltIn.args[0].toString().contains("$")){
                System.out.println("  li $a0 " + vBuiltIn.args[0].toString());
            }else {
                System.out.println("  move $a0 " + vBuiltIn.args[0].toString());
            }
            System.out.println("  jal _print");
        }
        if(vBuiltIn.op.name.toString().equals("LtS")){
            boolean isConstant = false;
            for(int i = 0; i < vBuiltIn.args.length; ++i){
                if(!vBuiltIn.args[i].toString().contains("$")){
                    isConstant = true;
                }
            }
            if(isConstant){
                System.out.print("  slti " + vBuiltIn.dest.toString() + " ");
                for(int i = 0; i < vBuiltIn.args.length; ++i){
                    System.out.print(vBuiltIn.args[i]);
                    if(i < vBuiltIn.args.length-1){
                        System.out.print(" ");
                    }
                }
                System.out.println();
            } else{
                System.out.print("  slt " + vBuiltIn.dest.toString() + " ");
                for(int i = 0; i < vBuiltIn.args.length; ++i){
                    System.out.print(vBuiltIn.args[i]);
                    if(i < vBuiltIn.args.length-1){
                        System.out.print(" ");
                    }
                }
                System.out.println();
            }
        }
        if(vBuiltIn.op.name.toString().equals("Sub")){
            if(!vBuiltIn.args[0].toString().contains("$") && !vBuiltIn.args[1].toString().contains("$")){
                System.out.println("  li " + vBuiltIn.dest.toString() + " " + (Integer.parseInt(vBuiltIn.args[0].toString()) - Integer.parseInt(vBuiltIn.args[1].toString())));
            } else {
                String s = ("  subu " + vBuiltIn.dest.toString() + " ");
                if (vBuiltIn.args[1].toString().contains("$")) {
                    System.out.println("  li $t9 " + vBuiltIn.args[0].toString());
                    s += "$t9 " + vBuiltIn.args[1].toString();
                } else {
                    s += vBuiltIn.args[0].toString() + " " + vBuiltIn.args[1].toString();
                }
                System.out.println(s);
            }
        }
        if(vBuiltIn.op.name.toString().equals("MulS")){
            if(!vBuiltIn.args[0].toString().contains("$") && !vBuiltIn.args[1].toString().contains("$")){
                System.out.println("  li " + vBuiltIn.dest.toString() + " " + (Integer.parseInt(vBuiltIn.args[0].toString()) * Integer.parseInt(vBuiltIn.args[1].toString())));
            } else {
                System.out.print("  mul " + vBuiltIn.dest.toString() + " ");
                if (!vBuiltIn.args[0].toString().contains("$")) {
                    System.out.println(vBuiltIn.args[1] + " " + vBuiltIn.args[0]);
                } else {
                    System.out.println(vBuiltIn.args[0] + " " + vBuiltIn.args[1]);
                }
            }
        }
        if(vBuiltIn.op.name.toString().equals("Lt")){
            String s = ("  sltu " + vBuiltIn.dest.toString() + " ");
            if (!vBuiltIn.args[0].toString().contains("$")) {
                System.out.println("  li $t9 " + vBuiltIn.args[0].toString());
                s += "$t9 " + vBuiltIn.args[1].toString();
            } else {
                s += vBuiltIn.args[0].toString() + " " + vBuiltIn.args[1].toString();
            }
            System.out.println(s);
        }
        if(vBuiltIn.op.name.toString().equals("Add")){
            System.out.print("  addu " + vBuiltIn.dest.toString() + " ");
            for(int i = 0; i < vBuiltIn.args.length; ++i){
                System.out.print(vBuiltIn.args[i]);
                if(i < vBuiltIn.args.length-1){
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    @Override
    public void visit(VFunction func, VMemWrite vMemWrite) throws Exception {
        if(vMemWrite.source.toString().contains(":")){
            System.out.println("  la $t9 " + vMemWrite.source.toString().substring(1));
            System.out.println("  sw $t9 " + ((VMemRef.Global)vMemWrite.dest).byteOffset + "(" + ((VMemRef.Global)vMemWrite.dest).base.toString() + ")");
        }else {
            if(vMemWrite.dest.getClass().getSimpleName().equals("Stack") && ((VMemRef.Stack)vMemWrite.dest).region.toString().equals("Local")){
                System.out.println("  sw " + vMemWrite.source.toString() + " " + ((((VMemRef.Stack)vMemWrite.dest).index + func.stack.out)*4) + "($sp)");
            }
            else{
                if(vMemWrite.dest.getClass().getSimpleName().equals("Stack") && ((VMemRef.Stack)vMemWrite.dest).region.toString().equals("Out")){
                    if(!vMemWrite.source.toString().contains("$")) {
                        System.out.println("  li $t9 " + vMemWrite.source.toString());
                        System.out.println("  sw $t9 " + ((VMemRef.Stack)vMemWrite.dest).index*4 + "($sp)");
                    }else {
                        System.out.println("  sw " + vMemWrite.source.toString() + " " + ((VMemRef.Stack) vMemWrite.dest).index * 4 + "($sp)");
                    }
                }
                else{
                    if(!vMemWrite.source.toString().contains("$") && vMemWrite.dest.getClass().getSimpleName().equals("Global")){
                        System.out.println("  li $t9 " + vMemWrite.source.toString());
                        System.out.println("  sw $t9 " + ((VMemRef.Global) vMemWrite.dest).byteOffset + "(" + ((VMemRef.Global) vMemWrite.dest).base.toString() + ")");
                    } else {
                        if (vMemWrite.dest.getClass().getSimpleName().equals("Global")) {
                            System.out.println("  sw " + vMemWrite.source.toString() + " " + ((VMemRef.Global) vMemWrite.dest).byteOffset + "(" + ((VMemRef.Global) vMemWrite.dest).base.toString() + ")");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void visit(VFunction func, VMemRead vMemRead) throws Exception {
        if(vMemRead.source.getClass().getSimpleName().equals("Global")){
            System.out.println("  lw " + vMemRead.dest.toString() + " " + ((VMemRef.Global)vMemRead.source).byteOffset + "(" + ((VMemRef.Global)vMemRead.source).base.toString() + ")");
        }else {
            if(((VMemRef.Stack)vMemRead.source).region.toString().equals("Local")){
                System.out.println("  lw " + vMemRead.dest.toString() + " " + ((VMemRef.Stack)vMemRead.source).index*4 + "($sp)");
            }
            else {
                if(((VMemRef.Stack)vMemRead.source).region.toString().equals("In")){
                    System.out.println("  lw " + vMemRead.dest.toString() + " " + ((VMemRef.Stack)vMemRead.source).index*4 + "($fp)");
                }

            }
        }
    }

    @Override
    public void visit(VFunction func, VBranch vBranch) throws Exception {
        if(vBranch.positive){
            System.out.println("  bnez " + vBranch.value.toString() + " " + vBranch.target.toString().substring(1));
        } else{
            System.out.println("  beqz " + vBranch.value.toString() + " " + vBranch.target.toString().substring(1));
        }
    }

    @Override
    public void visit(VFunction func, VGoto vGoto) throws Exception {
        System.out.println("  j " + vGoto.target.toString().substring(1));
    }

    @Override
    public void visit(VFunction func, VReturn vReturn) throws Exception {

    }
}
