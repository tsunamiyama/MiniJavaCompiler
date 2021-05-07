import java.util.HashMap;
import java.util.Vector;

public class func {
    int currInst = 0;
    HashMap<String, String>regAlloc = new HashMap<>();
    Vector<String> inStack = new Vector<>();
    Vector<String> outStack = new Vector<>();
    Vector<String> localStack = new Vector<>();
    Vector<interval> intervalList = new Vector<>();
}
