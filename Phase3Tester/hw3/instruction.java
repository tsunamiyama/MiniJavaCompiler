import java.util.Vector;

public class instruction{
    int insNum = 0;
    boolean converge = false;
    Vector<String> in = new Vector<String>();
    Vector<String> out = new Vector<String>();
    Vector<String> inPrime = new Vector<String>();
    Vector<String> outPrime = new Vector<String>();
    Vector<String> use = new Vector<String>();
    Vector<String> def = new Vector<String>();
    Vector<instruction> in_nodes = new Vector<>();
    Vector<instruction> out_nodes = new Vector<>();
    Vector<String> active_variables = new Vector<>();
    String goto_label_name = "";
    int goto_label = 0;
}

