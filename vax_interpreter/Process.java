package vax_interpreter;

import java.util.*;
import java.io.IOException;
import static vax_interpreter.Kernel.Constant.*;

class Process implements Runnable {
    public final Context context;
    private static boolean debugFlag;


    public Process(boolean debug, List<String> argStrs) throws IOException {
        debugFlag = debug;
        context = new Context();

        String textPath = argStrs.get(0);
        boolean loadTextFileSucceeded = context.memory.loadTextfile(textPath);
        if (!loadTextFileSucceeded) {
            throw new IllegalArgumentException("Can't read the program file.");
        }

        context.memory.setArgs(argStrs);
    }

    public Process(Context parentContext) {
        context = new Context(parentContext);
    }


    @Override public void run() {
        while (context.u.u_procp.p_stat == SRUN) {
            int pc = 0;
            String[] dump = null;
            if (debugFlag) {
                pc = context.pc();
                dump = dumpDatas();
            }

            synchronized(Context.class) {
                Instruction ins = Instruction.fetch(context);

                if (debugFlag) {
                    printDebug(pc, ins, dump);
                }

                ins.execute(context);
            }
        }
    }

    public String[] dumpDatas() {
        String[] lines = new String[3];
        lines[0] = String.format("%08x %08x %08x %08x %08x %08x",
                                 context.register[0], context.register[1], context.register[2],
                                 context.register[3], context.register[4], context.register[5]);
        lines[1] = String.format("%08x %08x %08x %08x %08x %08x",
                                 context.register[6], context.register[7], context.register[8],
                                 context.register[9], context.register[10], context.register[11]);
        lines[2] = String.format("%08x %08x %08x %08x %c%c%c%c",
                                 context.register[12], context.register[13], context.register[14],
                                 context.register[15],
                                 context.flagN.get() ? 'N' : '-',
                                 context.flagZ.get() ? 'Z' : '-',
                                 context.flagV.get() ? 'V' : '-',
                                 context.flagC.get() ? 'C' : '-');
        return lines;
    }

    private void printDebug(int pc, Instruction ins, String[] dump) {
        byte[] binBytes = context.memory.loadBytes(pc, ins.len());
        String binary = dumpBytes(binBytes);

        StringBuilder sb = new StringBuilder();
        for (Operand opr : ins.operands) {
            if (opr instanceof Address) {
                Address adr = (Address)opr;
                sb.append(String.format("[%02x]%02x", adr.getAddress(), adr.getValue().uint()));
            }
        }

        System.err.println(dump[0] + " : " + ins.mnemonic());
        System.err.println(dump[1] + " : " + binary);
        System.err.println(dump[2] + " : " + sb.toString());
    }

    private String dumpBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }
}

