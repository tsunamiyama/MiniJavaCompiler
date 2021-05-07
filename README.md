# MiniJavaCompiler
A 4 phase compiler from the MiniJava language to MIPS instructions which I built for my final project of CS179E at the University of California Riverside.

Included in the files is a powerpoint presentation in PDF format which highlights some important parts of the project.

## Phase 1: Type-Checking
The goal of phase 1 is to write a type checker for MiniJava. Given a program, the type checker checks at compile time that type mismatch does not happen at run time. It either approves or rejects the program. The set of rules that the type checker checks are represented as a type system.

## Phase 2: Intermediate Code Generation for Object-Oriented Languages
In this phase, we translate programs in the MiniJava language to programs in the Vapor language. Vapor is a low level assembly-like language. Vapor programs are described as a set of functions and data segments. In contrast to architecutal assembly languages that support a finite number of registers, Vapor functions can use an unbounded number of variables. The main challenge in the translation from MiniJava to Vapor is mapping objects to memory and object-oriented method calls to function calls.

### Example
MiniJava Program:

class Factorial{  
  public static void main(String[] a){  
    System.out.println(new Fac().ComputeFac(10));  
  }  
}  

class Fac {  
  public int ComputeFac(int num){  
    int num_aux;  
    if (num < 1)  
      num_aux = 1;  
    else  
      num_aux = num * (this.ComputeFac(num-1));  
    return num_aux;  
  }  
}  

The Resulting Vapor Program:

const vmt_Fac  
  :Fac.ComputeFac  

func Main()  
  t.0 = HeapAllocZ(4)  
  [t.0] = :vmt_Fac  
  if t.0 goto :null1  
  Error("null pointer")  
  null1:  
  t.1 = [t.0]  
  t.1 = [t.1+0]  
  t.2 = call t.1(t.0 10)  
  PrintIntS(t.2)  
  ret  

func Fac.ComputeFac(this num)  
  t.0 = LtS(num 1)  
  if0 t.0 goto :if1_else  
    num_aux = 1  
    goto :if1_end  
  if1_else:  
    t.1 = [this]  
    t.1 = [t.1+0]  
    t.2 = Sub(num 1)  
    t.3 = call t.1(this t.2)  
    num_aux = MulS(num t.3)  
  if1_end:  
  ret num_aux  

## Phase 3: Register Allocation
In this phase, we translate the Vapor language to the Vapor-M language. In contrast to Vapor that provides local variables, Vapor-M provides registers and stacks. The local variables should be mapped to registers and run-time stack elements.

### Example
Vapor Program:

const vmt_Fac  
  :Fac.ComputeFac  

func Main()  
  t.0 = HeapAllocZ(4)  
  [t.0] = :vmt_Fac  
  if t.0 goto :null1  
  Error("null pointer")  
  null1:  
  t.1 = [t.0]  
  t.1 = [t.1+0]  
  t.2 = call t.1(t.0 10)  
  PrintIntS(t.2)  
  ret  

func Fac.ComputeFac(this num)  
  t.0 = LtS(num 1)  
  if0 t.0 goto :if1_else  
    num_aux = 1  
    goto :if1_end  
  if1_else:  
    t.1 = [this]  
    t.1 = [t.1+0]  
    t.2 = Sub(num 1)  
    t.3 = call t.1(this t.2)  
    num_aux = MulS(num t.3)  
  if1_end:  
  ret num_aux  
  
The Resulting Vapor-M Program:

const vmt_Fac  
  :Fac.ComputeFac  

func Main [in 0, out 0, local 0]  
  $t0 = HeapAllocZ(4)  
  [$t0] = :vmt_Fac  
  if $t0 goto :null1  
  Error("null pointer")  
null1:  
  $t1 = [$t0]  
  $t1 = [$t1]  
  $a0 = $t0  
  $a1 = 10  
  call $t1  
  $t1 = $v0  
  PrintIntS($t1)  
  ret  

func Fac.ComputeFac [in 0, out 0, local 1]  
  local[0] = $s0  
  $t0 = $a0  
  $s0 = $a1  
  $t1 = LtS($s0 1)  
  if0 $t1 goto :if1_else  
  $t1 = 1  
  goto :if1_end  
if1_else:  
  $t2 = [$t0]  
  $t2 = [$t2]  
  $t3 = Sub($s0 1)  
  $a0 = $t0  
  $a1 = $t3  
  call $t2  
  $t3 = $v0  
  $t1 = MulS($s0 $t3)  
if1_end:  
  $v0 = $t1  
  $s0 = local[0]  
  ret  

## Phase 4: Activation Records and Instruction Selection
In this phase, the Vapor-M registers and stacks should be mapped to MIPS registers and runtime stack. In addition, the Vapor-M instuctions should be mapped to MIPS instructions.

## Testing
Included in the files are testers for each phase. Each phase has various test cases with instructions on how to run the testers in a Linux environment.
