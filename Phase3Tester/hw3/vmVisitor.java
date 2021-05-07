import cs132.vapor.ast.*;

import java.util.Vector;

public class vmVisitor extends VInstr.VisitorP<Vector<instruction>, Exception> {

    @Override
    public void visit(Vector<instruction> o, VAssign vAssign) throws Exception {
        //Define use and def sets
        if(vAssign.source.toString().matches("(.*[a-z].*)|(.*[A-Z].*)")) {
            o.lastElement().use.add(vAssign.source.toString());
        }
        o.lastElement().def.add(vAssign.dest.toString());
    }

    @Override
    public void visit(Vector<instruction> o, VCall vCall) throws Exception {
        o.lastElement().def.add(vCall.dest.toString());
        for(int i = 0; i < vCall.args.length; ++i){
            if(vCall.args[i].toString().matches("(.*[a-z].*)|(.*[A-Z].*)")) {
                if(!vCall.args[i].toString().contains(":")) {
                    o.lastElement().use.add(vCall.args[i].toString());
                } else{
                    System.out.println("const " + vCall.args[i].toString().substring(1));
                }
            }
        }
        if(!vCall.addr.toString().contains(":")) {
            o.lastElement().use.add(vCall.addr.toString());
        }

        /*System.out.println("vCAll");
        System.out.println(vCall.addr.toString());
        for(int i = 0; i < vCall.args.length; ++i){
            System.out.println(vCall.args[i] + " arg");
        }
        System.out.println(vCall.dest + " dest");
        */
    }

    @Override
    public void visit(Vector<instruction> o, VBuiltIn vBuiltIn) throws Exception {
        for(int i = 0; i < vBuiltIn.args.length; ++i){
            if(vBuiltIn.args[i].toString().matches("(.*[a-z].*)|(.*[A-Z].*)")) {
                if(!vBuiltIn.args[i].toString().contains(" ")) {
                    //System.out.println(vBuiltIn.args[i].toString());
                    o.lastElement().use.add(vBuiltIn.args[i].toString());
                }
            }
        }
        if(vBuiltIn.dest != null) {
            //System.out.println(vBuiltIn.dest.toString());
            o.lastElement().def.add(vBuiltIn.dest.toString());
        } /*else{
            System.out.println("builtin end");
        }*/
    }

    @Override
    public void visit(Vector<instruction> o, VMemWrite vMemWrite) throws Exception {
        //System.out.println(((VMemRef.Global)vMemWrite.dest).base.toString());
        if(((VMemRef.Global)vMemWrite.dest).base.toString().matches("(.*[a-z].*)|(.*[A-Z].*)")) {
            o.lastElement().def.add(((VMemRef.Global) vMemWrite.dest).base.toString());
        }
        if(!vMemWrite.source.toString().contains(":")) {
            if(vMemWrite.source.toString().matches("(.*[a-z].*)|(.*[A-Z].*)")) {
                o.lastElement().use.add(vMemWrite.source.toString());
            }
        }
    }

    @Override
    public void visit(Vector<instruction> o, VMemRead vMemRead) throws Exception {
        //System.out.println(((VMemRef.Global)vMemRead.source).base.toString());
        o.lastElement().use.add(((VMemRef.Global)vMemRead.source).base.toString());
        o.lastElement().def.add(vMemRead.dest.toString());
    }

    @Override
    public void visit(Vector<instruction> o, VBranch vBranch) throws Exception {
        o.lastElement().use.add(vBranch.value.toString());
        o.lastElement().goto_label = vBranch.target.getTarget().instrIndex;
        o.lastElement().goto_label_name = vBranch.target.toString();
        //System.out.println(vBranch.target.toString() + ":" + vBranch.target.getTarget().instrIndex);
        //o.out_nodes.add()
    }

    @Override
    public void visit(Vector<instruction> o, VGoto vGoto) throws Exception {
        //o.use.add(vGoto.target.toString());
        //System.out.print(((VAddr.Label)vGoto.target).label.ident);
        o.lastElement().goto_label_name = ((VAddr.Label)vGoto.target).label.ident;
        //System.out.println(((VCodeLabel)(((VAddr.Label)vGoto.target).label).getTarget()).instrIndex);
        o.lastElement().goto_label = ((VCodeLabel)(((VAddr.Label)vGoto.target).label).getTarget()).instrIndex;
    }

    @Override
    public void visit(Vector<instruction> o, VReturn vReturn) throws Exception {
        if(vReturn.value != null && vReturn.value.toString().matches("(.*[a-z].*)|(.*[A-Z].*)")) {
            o.lastElement().use.add(vReturn.value.toString());
        }
    }
}
