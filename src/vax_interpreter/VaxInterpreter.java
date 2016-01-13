package vax_interpreter;

import java.util.*;
import java.io.IOException;

public class VaxInterpreter {

    public static void main(String[] args) {
        boolean debugFlag = false;
        List<String> processArgs;

        int argi;
        for (argi = 0; argi < args.length; argi++) {
            if (args[argi].equals("-d")) {
                debugFlag = true;
            } else if (args[argi].equals("-rp")) {
                if (argi + 1 > args.length - 1) {
                    throw new IllegalArgumentException(args[argi]);
                }
                ++argi;
                Kernel.rootPath = args[argi];
            } else {
                break;
            }
        }

        processArgs = Arrays.asList(Arrays.copyOfRange(args, argi, args.length));
        if (processArgs.isEmpty()) {
            throw new IllegalArgumentException("No input file.");
        }

        try {
            Process newProc = new Process(debugFlag, processArgs);
            new Thread(newProc).start();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}

class Instruction {
    public final Opcode opcode;
    public final List<Operand> operands;

    protected Instruction(Opcode opc, List<Operand> oprs) {
        this.opcode = opc;
        this.operands = oprs;
    }

    public static Instruction fetch(Context context) {
        Opcode opc = Opcode.fetch(context);

        List<Operand> oprs = new ArrayList<>(opc.operands().length);
        for (DataType type : opc.operands()) {
            Operand opr = Operand.fetch(context, type);
            if (opr == null) {
                return null;
            }
            oprs.add(opr);
        }

        return new Instruction(opc, oprs);
    }

    public void execute(Context context) {
        opcode.execute(operands, context);
    }

    public String mnemonic() {
        int nOpr = operands.size();
        StringBuilder sb = new StringBuilder(opcode.mnemonic());
        if (nOpr > 0) {
            sb.append(" ");
            sb.append(operands.get(0).mnemonic());
        }
        for (int i = 1; i < nOpr; i++) {
            sb.append(",");
            sb.append(operands.get(i).mnemonic());
        }
        return sb.toString();
    }

    public int len() {
        int l = opcode.len();
        for (Operand opr : operands) {
            l += opr.len();
        }
        return l;
    }
}

class Util {
    public static final int AP = 0xc;
    public static final int FP = 0xd;
    public static final int SP = 0xe;
    public static final int PC = 0xf;
    public static final int MEM_SIZE = 0x80000;

    // Debug
    public static void printVal(byte[] val) {
        StringBuilder sb = new StringBuilder("0x");
        for (int i = val.length - 1; i >= 0; i--) {
            sb.append(String.format("%02x", val[i]));
        }
        System.out.println("'' " + sb.toString());
    }
}

