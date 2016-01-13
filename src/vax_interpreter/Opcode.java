package vax_interpreter;

import java.util.*;
import static vax_interpreter.Util.*;
import static vax_interpreter.DataType.*;

enum DataType {
    B(1), W(2), L(4), Q(8), O(16),
    F(4, " [f-float]"), D(8, " [d-float]"), G(8, " [g-float]"), H(16, " [h-float]"),
    BrB(1), BrW(2);

    public final int size;
    public final String annotation;

    private DataType(int sz) {
        this(sz, "");
    }
    private DataType(int sz, String ann) {
        this.size = sz;
        this.annotation = ann;
    }
}

class Opcode {
    private final static HashMap<Integer, ICode> codeMap;
    static {
        ICode[][] codes = {
            Nop.values(),
            Mov.values(),
            Movz.values(),
            Push.values(),
            MovA.values(),
            PushA.values(),
            Mcom.values(),
            Mneg.values(),
            FMneg.values(),
            Add.values(),
            Sub.values(),
            Mul.values(),
            Div.values(),
            Bit.values(),
            Bis.values(),
            Bic.values(),
            Xor.values(),
            Clr.values(),
            Inc.values(),
            Dec.values(),
            Ash.values(),
            Tst.values(),
            Cmp.values(),
            Ext.values(),
            Insv.values(),
            Jmp.values(),
            Br.values(),
            Bb.values(),
            Blb.values(),
            Call.values(),
            Ret.values(),
            Chmk.values(),
            Case.values(),
            Aob.values(),
            Sob.values(),
            Cvt.values(),
            Cvtlp.values(),
            Acb.values(),
            Movc.values(),
            Cmpc.values(),
            Locc.values(),
            Movp.values(),
            Editpc.values()
        };
        codeMap = new HashMap<>();
        for (ICode[] codesArray : codes) {
            for (ICode opc : codesArray) {
                codeMap.put(opc.bin(), opc);
            }
        }
    }

    private final ICode code;
    private static Context context;
    protected Opcode(ICode code) {
        this.code = code;
    }

    public static Opcode fetch(Context context) {
        int bin = 0;
        for (int i = 0; i < 2; i++) {
            int val = context.readText();
            if (val == -1) {
                return null;
            }
            bin += val << i * 8;
            ICode opc = codeMap.get(bin);
            if (opc != null) {
                return new Opcode(opc);
            }
        }
        return new Nullcode(bin);
    }

    public DataType[] operands() {
        return code.operands();
    }

    public String mnemonic() {
        return code.mnemonic();
    }

    public int len() {
        return code.bin() <= 0xff ? 1 : 2;
    }

    public void execute(List<Operand> oprs, Context c) {
        context = c;
        code.execute(oprs);
    }


    interface ICode {
        public int bin();
        public DataType[] operands();
        public String mnemonic();
        public void execute(List<Operand> oprs);
    }


    enum Nop implements ICode {
        NOP (0x1);

        private final int bin;
        private final DataType[] operands;

        private Nop(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
        }
    }

    enum Mov implements ICode {
        MOVB (0x90, B,B),   MOVW (0xb0, W,W),
        MOVL (0xd0, L,L),   MOVQ (0x7d, Q,Q),
        MOVO (0x7dfd, O,O),
        MOVF (0x50, F,F),   MOVD (0x70, D,D),
        MOVG (0x50fd, G,G), MOVH (0x70fd, H,H);

        private final int bin;
        private final DataType[] operands;

        private Mov(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData srcVal = oprs.get(0).getValue();
            Operand dest = oprs.get(1);
            dest.setValue(srcVal);
            context.flagN.set( srcVal.isNegValue() );
            context.flagZ.set( srcVal.isZeroValue() );
            context.flagV.clear();
        }
    }

    enum Movz implements ICode {
        MOVZBW (0x9b, B,W), MOVZBL (0x9a, B,L),
        MOVZWL (0x3c, W,L);

        private final int bin;
        private final DataType[] operands;

        private Movz(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData srcVal = oprs.get(0).getValue();
            IntData setVal = new IntData(srcVal.uint(), operands[1]);
            Operand dest = oprs.get(1);
            dest.setValue(setVal);
            context.flagN.clear();
            context.flagZ.set( setVal.isZeroValue() );
            context.flagV.clear();
            context.flagC.clear();
        }
    }

    enum Push implements ICode {
        PUSHL (0xdd, L);

        private final int bin;
        private final DataType[] operands;

        private Push(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData srcVal = oprs.get(0).getValue();
            context.push(srcVal);
            context.flagN.set( srcVal.isNegValue() );
            context.flagZ.set( srcVal.isZeroValue() );
            context.flagV.clear();
        }
    }

    enum MovA implements ICode {
        MOVAB (0x9e, B,L), MOVAW (0x3e, W,L),
        MOVAL (0xde, L,L), MOVAQ (0x7e, Q,L),
        MOVAO (0x7efd, O,L);

        private final int bin;
        private final DataType[] operands;

        private MovA(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData addr = new IntData( ((Address)oprs.get(0)).getAddress() );
            Operand dest = oprs.get(1);
            dest.setValue(addr);
            context.flagN.set( addr.isNegValue() );
            context.flagZ.set( addr.isZeroValue() );
            context.flagV.clear();
        }
    }

    enum PushA implements ICode {
        PUSHAB (0x9f, B), PUSHAW (0x3f, W),
        PUSHAL (0xdf, L),
        PUSHAQ (0x7f, Q), PUSHAO (0x7ffd, O);

        private final int bin;
        private final DataType[] operands;

        private PushA(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData addr = new IntData( ((Address)oprs.get(0)).getAddress() );
            context.push(addr);
            context.flagN.set( addr.isNegValue() );
            context.flagZ.set( addr.isZeroValue() );
            context.flagV.clear();
        }
    }

    enum Mcom implements ICode {
        MCOMB (0x92, B,B), MCOMW (0xb2, W,W),
        MCOML (0xd2, L,L);

        private final int bin;
        private final DataType[] operands;

        private Mcom(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData srcVal = oprs.get(0).getValue();
            Operand dest = oprs.get(1);
            IntData com = IntData.bitInvert(srcVal);
            dest.setValue(com);
            context.flagN.set( com.isNegValue() );
            context.flagZ.set( com.isZeroValue() );
            context.flagV.clear();
            context.flagC.clear();
        }
    }

    enum Mneg implements ICode {
        MNEGB (0x8e, B,B), MNEGW (0xae, W,W),
        MNEGL (0xce, L,L);

        private final int bin;
        private final DataType[] operands;

        private Mneg(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData srcVal = oprs.get(0).getValue();
            Operand dest = oprs.get(1);
            IntData neg = Calculator.sub(new IntData(0, srcVal.dataType()), srcVal, context);
            dest.setValue(neg);
        }
    }

    enum FMneg implements ICode {
        MNEGF (0x52, F,F),   MNEGD (0x72, D,D),
        MNEGG (0x52fd, G,G), MNEGH (0x72fd, H,H);

        private final int bin;
        private final DataType[] operands;

        private FMneg(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData srcVal = oprs.get(0).getValue();

            assert !srcVal.isMinusZeroFloatValue() : "Reserved operand fault";

            IntData neg;
            if (srcVal.isZeroValue()) {
                neg = srcVal;
            } else {
                neg = IntData.negativeFloat(srcVal);
            }
            Operand dest = oprs.get(1);
            dest.setValue(neg);
            context.flagN.set( neg.isNegValue() );
            context.flagZ.set( neg.isZeroValue() );
            context.flagV.clear();
            context.flagC.clear();
        }
    }

    enum Add implements ICode {
        ADDB2 (0x80, B,B), ADDB3 (0x81, B,B,B),
        ADDW2 (0xa0, W,W), ADDW3 (0xa1, W,W,W),
        ADDL2 (0xc0, L,L), ADDL3 (0xc1, L,L,L);

        private final int bin;
        private final DataType[] operands;

        private Add(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData arg1 = oprs.get(1).getValue();
            IntData arg2 = oprs.get(0).getValue();
            Operand dest = oprs.size() == 3 ? oprs.get(2) : oprs.get(1);
            IntData sum = Calculator.add(arg1, arg2, context);
            dest.setValue(sum);
        }
    }

    /*enum FAdd implements ICode {
      ADDF2 (0x40, F,F),   ADDF3 (0x41, F,F,F),
      ADDD2 (0x60, D,D),   ADDD3 (0x61, D,D,D),
      ADDG2 (0x40fd, G,G), ADDG3 (0x41fd, G,G,G),
      ADDH2 (0x60fd, H,H), ADDH3 (0x61fd, H,H,H);

      private final int bin;
      private final DataType[] operands;

      private FAdd(int bin, DataType... oprs) {
      this.bin = bin;
      this.operands = oprs;
      }

      @Override public int bin() {
      return bin;
      }

      @Override public DataType[] operands() {
      return operands;
      }

      @Override public String mnemonic() {
      return name().toLowerCase(Locale.ENGLISH);
      }

      @Override public void execute(List<Operand> oprs) {
      }
      }*/

    enum Sub implements ICode {
        SUBB2 (0x82, B,B), SUBB3 (0x83, B,B,B),
        SUBW2 (0xa2, W,W), SUBW3 (0xa3, W,W,W),
        SUBL2 (0xc2, L,L), SUBL3 (0xc3, L,L,L);

        private final int bin;
        private final DataType[] operands;

        private Sub(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData arg1 = oprs.get(1).getValue();
            IntData arg2 = oprs.get(0).getValue();
            Operand dest = oprs.size() == 3 ? oprs.get(2) : oprs.get(1);
            IntData diff = Calculator.sub(arg1, arg2, context);
            dest.setValue(diff);
        }
    }

    /*enum FSub implements ICode {
      SUBF2 (0x42, F,F),   SUBF3 (0x43, F,F,F),
      SUBD2 (0x62, D,D),   SUBD3 (0x63, D,D,D),
      SUBG2 (0x42fd, G,G), SUBG3 (0x43fd, G,G,G),
      SUBH2 (0x62fd, H,H), SUBH3 (0x63fd, H,H,H);

      private final int bin;
      private final DataType[] operands;

      private FSub(int bin, DataType... oprs) {
      this.bin = bin;
      this.operands = oprs;
      }

      @Override public int bin() {
      return bin;
      }

      @Override public DataType[] operands() {
      return operands;
      }

      @Override public String mnemonic() {
      return name().toLowerCase(Locale.ENGLISH);
      }

      @Override public void execute(List<Operand> oprs) {
      }
      }*/

    enum Mul implements ICode {
        MULB2 (0x84, B,B), MULB3 (0x85, B,B,B),
        MULW2 (0xa4, W,W), MULW3 (0xa5, W,W,W),
        MULL2 (0xc4, L,L), MULL3 (0xc5, L,L,L); 

        private final int bin;
        private final DataType[] operands;

        private Mul(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData arg1 = oprs.get(0).getValue();
            IntData arg2 = oprs.get(1).getValue();
            Operand dest = oprs.size() == 3 ? oprs.get(2) : oprs.get(1);
            IntData prod = Calculator.mul(arg1, arg2, context);
            dest.setValue(prod);
        }
    }

    enum Div implements ICode {
        DIVB2 (0x86, B,B), DIVB3 (0x87, B,B,B),
        DIVW2 (0xa6, W,W), DIVW3 (0xa7, W,W,W),
        DIVL2 (0xc6, L,L), DIVL3 (0xc7, L,L,L);

        private final int bin;
        private final DataType[] operands;

        private Div(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData divisor = oprs.get(0).getValue();
            IntData dividend = oprs.get(1).getValue();
            Operand dest = oprs.size() == 3 ? oprs.get(2) : oprs.get(1);
            IntData quo = Calculator.div(dividend, divisor, context);

            if (quo != null) {
                dest.setValue(quo);
            } else {
                if (oprs.size() == 3) {
                    dest.setValue(dividend);
                }
                context.flagN.set( dest.getValue().isNegValue() );
                context.flagZ.set( dest.getValue().isZeroValue() );
                context.flagC.clear();
            }
        }
    }

    enum Bit implements ICode {
        BITB (0x93, B,B), BITW (0xb3, W,W),
        BITL (0xd3, L,L);

        private final int bin;
        private final DataType[] operands;

        private Bit(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData arg1 = oprs.get(1).getValue();
            IntData arg2 = oprs.get(0).getValue();
            IntData testVal = new IntData(arg1.uint() & arg2.uint(), arg1.dataType());
            context.flagN.set( testVal.isNegValue() );
            context.flagZ.set( testVal.isZeroValue() );
            context.flagV.clear();
        }
    }

    enum Bis implements ICode {
        BISB2 (0x88, B,B), BISB3 (0x89, B,B,B),
        BISW2 (0xa8, W,W), BISW3 (0xa9, W,W,W),
        BISL2 (0xc8, L,L), BISL3 (0xc9, L,L,L);

        private final int bin;
        private final DataType[] operands;

        private Bis(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData arg1 = oprs.get(1).getValue();
            IntData arg2 = oprs.get(0).getValue();
            IntData bisVal = new IntData(arg1.uint() | arg2.uint(), arg1.dataType());
            Operand dest = oprs.size() == 3 ? oprs.get(2) : oprs.get(1);
            dest.setValue(bisVal);
            context.flagN.set( bisVal.isNegValue() );
            context.flagZ.set( bisVal.isZeroValue() );
            context.flagV.clear();
        }
    }

    enum Bic implements ICode {
        BICB2 (0x8a, B,B), BICB3 (0x8b, B,B,B),
        BICW2 (0xaa, W,W), BICW3 (0xab, W,W,W),
        BICL2 (0xca, L,L), BICL3 (0xcb, L,L,L);

        private final int bin;
        private final DataType[] operands;

        private Bic(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData arg1 = oprs.get(1).getValue();
            IntData arg2 = oprs.get(0).getValue();
            IntData bicVal = new IntData(arg1.uint() & ~arg2.uint(), arg1.dataType());
            Operand dest = oprs.size() == 3 ? oprs.get(2) : oprs.get(1);
            dest.setValue(bicVal);
            context.flagN.set( bicVal.isNegValue() );
            context.flagZ.set( bicVal.isZeroValue() );
            context.flagV.clear();
        }
    }

    enum Xor implements ICode {
        XORB2 (0x8c, B,B), XORB3 (0x8d, B,B,B),
        XORW2 (0xac, W,W), XORW3 (0xad, W,W,W),
        XORL2 (0xcc, L,L), XORL3 (0xcd, L,L,L);

        private final int bin;
        private final DataType[] operands;

        private Xor(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData arg1 = oprs.get(1).getValue();
            IntData arg2 = oprs.get(0).getValue();
            IntData xorVal = new IntData(arg1.uint() ^  arg2.uint(), arg1.dataType());
            Operand dest = oprs.size() == 3 ? oprs.get(2) : oprs.get(1);
            dest.setValue(xorVal);
            context.flagN.set( xorVal.isNegValue() );
            context.flagZ.set( xorVal.isZeroValue() );
            context.flagV.clear();
        }
    }

    enum Clr implements ICode {
        CLRB (0x94, B), CLRW (0xb4, W),
        CLRL (0xd4, L),
        CLRQ (0x7c, Q), CLRO (0x7cfd, O);

        private final int bin;
        private final DataType[] operands;

        private Clr(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            Operand dest = oprs.get(0);
            IntData zero = new IntData(0, operands[0]);
            dest.setValue(zero);
            context.flagN.clear();
            context.flagZ.set();
            context.flagV.clear();
        }
    }

    enum Inc implements ICode {
        INCB (0x96, B), INCW (0xb6, W),
        INCL (0xd6, L);

        private final int bin;
        private final DataType[] operands;

        private Inc(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData arg = oprs.get(0).getValue();
            Operand dest = oprs.get(0);
            IntData sum = Calculator.add(arg, new IntData(1, arg.dataType()), context);
            dest.setValue(sum);
        }
    }

    enum Dec implements ICode {
        DECB (0x97, B), DECW (0xb7, W),
        DECL (0xd7, L);

        private final int bin;
        private final DataType[] operands;

        private Dec(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData arg = oprs.get(0).getValue();
            Operand dest = oprs.get(0);
            IntData diff = Calculator.sub(arg, new IntData(1, arg.dataType()), context);
            dest.setValue(diff);
        }
    }

    enum Ash implements ICode {
        ASHL (0x78, B,L,L), ASHQ (0x79, B,Q,Q);

        private final int bin;
        private final DataType[] operands;

        private Ash(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            int count = oprs.get(0).getValue().sint();
            IntData srcVal = oprs.get(1).getValue();
            long src = srcVal.slong();
            Operand dest = oprs.get(2);

            long val;
            if (count >= 0) {
                val = count > maxCount() ? 0 : src << count;
            } else {
                int rcount = count < minCount() ? -minCount() : -count;
                val = src >> rcount;
            }
            IntData shifted = new IntData(val, srcVal.dataType());

            dest.setValue(shifted);
            context.flagN.set( shifted.isNegValue() );
            context.flagZ.set( shifted.isZeroValue() );
            context.flagV.set( srcVal.isNegValue() != shifted.isNegValue() );
            context.flagC.clear();
        }

        private int maxCount() {
            if (ordinal() == ASHL.ordinal()) {
                return 31;
            } else {
                return 63;
            }
        }

        private int minCount() {
            if (ordinal() == ASHL.ordinal()) {
                return -31;
            } else {
                return -63;
            }
        }
    }

    enum Tst implements ICode {
        TSTB (0x95, B),   TSTW (0xb5, W),
        TSTL (0xd5, L),
        TSTF (0x53, F),   TSTD (0x73, D),
        TSTG (0x53fd, G), TSTH (0x73fd, H);

        private final int bin;
        private final DataType[] operands;

        private Tst(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData srcVal = oprs.get(0).getValue();
            context.flagN.set( srcVal.isNegValue() );
            context.flagZ.set( srcVal.isZeroValue() );
            context.flagV.clear();
            context.flagC.clear();
        }
    }

    enum Cmp implements ICode {
        CMPB (0x91, B,B),   CMPW (0xb1, W,W),
        CMPL (0xd1, L,L);

        private final int bin;
        private final DataType[] operands;

        private Cmp(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData lhs = oprs.get(0).getValue();
            IntData rhs = oprs.get(1).getValue();
            context.flagN.set( lhs.sint() < rhs.sint() );
            context.flagZ.set( lhs.sint() == rhs.sint() );
            context.flagV.clear();
            context.flagC.set( lhs.uint() < rhs.uint() );
        }
    }

    /*enum FCmp implements ICode {
      CMPF (0x51, F,F),   CMPD (0x71, D,D),
      CMPG (0x51fd, G,G), CMPH (0x71fd, H,H);

      private final int bin;
      private final DataType[] operands;

      private FCmp(int bin, DataType... oprs) {
      this.bin = bin;
      this.operands = oprs;
      }

      @Override public int bin() {
      return bin;
      }

      @Override public DataType[] operands() {
      return operands;
      }

      @Override public String mnemonic() {
      return name().toLowerCase(Locale.ENGLISH);
      }

      @Override public void execute(List<Operand> oprs) {
      }
      }*/

    enum Ext implements ICode {
        EXTV (0xee, L,B,B,L), EXTZV (0xef, L,B,B,L);

        private final int bin;
        private final DataType[] operands;

        private Ext(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            int pos = oprs.get(0).getValue().uint();
            int size = oprs.get(1).getValue().uint();
            Operand base = oprs.get(2);
            Operand dest = oprs.get(3);

            assert size <= 32 : "Reserved operand fault";

            IntData extVal;
            if (size == 0) {
                extVal = new IntData(0);
            }
            else {
                long srcVal;
                if (base instanceof Register) {
                    assert (pos & 0xffffffffL) <= 31 : "Reserved operand fault";

                    int regNum = ((Register)base).regNum;
                    srcVal = ((long)context.register[regNum + 1] << 32) | context.register[regNum];
                } else {
                    int addr = ((Address)base).getAddress() + (pos >>> 5);
                    pos = pos & 31;
                    srcVal =
                        ((long)context.memory.load(addr + 4, DataType.L).uint() << 32) |
                        context.memory.load(addr, DataType.L).uint();
                }

                int lSpace = 64 - (pos + size);
                int eVal = (int)(isSignExt() ?
                                 srcVal << lSpace >> (lSpace + pos) :
                                 srcVal << lSpace >>> (lSpace + pos));
                extVal = new IntData(eVal, DataType.L);
            }

            dest.setValue(extVal);
            context.flagN.set( extVal.isNegValue() );
            context.flagZ.set( extVal.isZeroValue() );
            context.flagV.clear();
        }

        private boolean isSignExt() {
            return ordinal() == EXTV.ordinal();
        }
    }

    enum Insv implements ICode {
        INSV (0xf0, L,L,B,B);

        private final int bin;
        private final DataType[] operands;

        private Insv(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            int size = oprs.get(2).getValue().uint();

            assert size <= 32 : "Reserved operand fault";

            if (size != 0) {
                int pos = oprs.get(1).getValue().uint();
                long srcVal = (long)oprs.get(0).getValue().uint() << pos;
                Operand base = oprs.get(3);
                if (base instanceof Register) {
                    assert (pos & 0xffffffffL) <= 31 : "Reserved operand fault";

                    int regNum = ((Register)base).regNum;
                    long orgVal =
                        ((long)context.register[regNum + 1] << 32) |
                        context.register[regNum];
                    long mask = (~(0xffffffffffffffffL << size)) << pos;
                    long insVal = (orgVal & ~mask) | (srcVal & mask);
                    context.setRegisterValue(regNum, new IntData(insVal, DataType.Q));
                } else {
                    int addr = ((Address)base).getAddress() + (pos >>> 5);
                    pos = pos & 31;

                    long orgVal =
                        ((long)context.memory.load(addr + 4, DataType.L).uint() << 32) |
                        context.memory.load(addr, DataType.L).uint();
                    long mask = (~(0xffffffffffffffffL << size)) << pos;
                    long insVal = (orgVal & ~mask) | (srcVal & mask);
                    context.memory.store(addr, new IntData(insVal, DataType.Q));
                }
            }
        }
    }

    enum Jmp implements ICode {
        JMP (0x17, B);

        private final int bin;
        private final DataType[] operands;

        private Jmp(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            context.register[PC] = ((Address)oprs.get(0)).getAddress();
        }
    }


    enum Br implements ICode {
        BRB (0x11, BrB) {
            @Override public boolean check(Context context) {
                return true;
            }
        },
        BRW (0x31, BrW) {
            @Override public boolean check(Context context) {
                return true;
            }
        },
        BNEQ (0x12, BrB) {
            @Override public boolean check(Context context) {
                return !context.flagZ.get();
            }
        },
        BEQL (0x13, BrB) {
            @Override public boolean check(Context context) {
                return context.flagZ.get();
            }
        },
        BGTR (0x14, BrB) {
            @Override public boolean check(Context context) {
                return !context.flagN.get() && !context.flagZ.get();
            }
        },
        BLEQ (0x15, BrB) {
            @Override public boolean check(Context context) {
                return context.flagN.get() || context.flagZ.get();
            }
        },
        BGEQ (0x18, BrB) {
            @Override public boolean check(Context context) {
                return !context.flagN.get();
            }
        },
        BLSS (0x19, BrB) {
            @Override public boolean check(Context context) {
                return context.flagN.get();
            }
        },
        BGTRU (0x1a, BrB) {
            @Override public boolean check(Context context) {
                return !context.flagC.get() && !context.flagZ.get();
            }
        },
        BLEQU (0x1b, BrB) {
            @Override public boolean check(Context context) {
                return context.flagC.get() || context.flagZ.get();
            }
        },
        BVC (0x1c, BrB) {
            @Override public boolean check(Context context) {
                return !context.flagV.get();
            }
        },
        BVS (0x1d, BrB) {
            @Override public boolean check(Context context) {
                return context.flagV.get();
            }
        },
        BCC (0x1e, BrB) {
            @Override public boolean check(Context context) {
                return !context.flagC.get();
            }
        },
        BLSSU (0x1f, BrB) {
            @Override public boolean check(Context context) {
                return context.flagC.get();
            }
        };

        private final int bin;
        private final DataType[] operands;

        private Br(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            if (check(context)) {
                context.register[PC] = ((Address)oprs.get(0)).getAddress();
            }
        }

        protected abstract boolean check(Context context);
    }

    enum Bb implements ICode {
        BBS   (0xe0, L,B,BrB),
        BBC   (0xe1, L,B,BrB),
        BBSS  (0xe2, L,B,BrB),
        BBCS  (0xe3, L,B,BrB),
        BBSC  (0xe4, L,B,BrB),
        BBCC  (0xe5, L,B,BrB),
        BBSSI (0xe6, L,B,BrB),
        BBCCI (0xe7, L,B,BrB);

        private final int bin;
        private final DataType[] operands;

        private Bb(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            int pos = oprs.get(0).getValue().uint();
            Operand base = oprs.get(1);
            Address dest = (Address)oprs.get(2);

            boolean isSet;
            if (base instanceof Register) {
                assert (pos & 0xffffffffL) <= 31 : "Reserved operand fault";

                int regNum = ((Register)base).regNum;
                int bit = 1 << (pos & 0x1f);
                isSet = (context.register[regNum] & bit) != 0;
                if (doesSetBit()) {
                    context.register[regNum] |= bit;
                } else if (doesClearBit()) {
                    context.register[regNum] &= ~bit;
                }
            } else {
                int addr = ((Address)base).getAddress() + (pos >> 3);
                int bit = 1 << (pos & 7);
                int targetByte = context.memory.load(addr, DataType.B).uint();
                isSet = (targetByte & bit) != 0;
                if (doesSetBit()) {
                    context.memory.store(addr, new IntData(targetByte | bit, DataType.B));
                } else if (doesClearBit()) {
                    context.memory.store(addr, new IntData(targetByte & ~bit, DataType.B));
                }
            }

            if (isSet == doesBranchOnSet()) {
                context.register[PC] = dest.getAddress();
            }
        }

        private boolean doesBranchOnSet() {
            int enumOrd = ordinal();
            return enumOrd == BBS.ordinal() ||
                   enumOrd == BBSS.ordinal() ||
                   enumOrd == BBSC.ordinal() ||
                   enumOrd == BBSSI.ordinal();
        }

        private boolean doesSetBit() {
            int enumOrd = ordinal();
            return enumOrd == BBSS.ordinal() ||
                   enumOrd == BBCS.ordinal() ||
                   enumOrd == BBSSI.ordinal();
        }

        private boolean doesClearBit() {
            int enumOrd = ordinal();
            return enumOrd == BBSC.ordinal() ||
                   enumOrd == BBCC.ordinal() ||
                   enumOrd == BBCCI.ordinal();
        }
    }

    enum Blb implements ICode {
        BLBS (0xe8, L,BrB),
        BLBC (0xe9, L,BrB);

        private final int bin;
        private final DataType[] operands;

        private Blb(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData srcVal = oprs.get(0).getValue();
            boolean isSet = (srcVal.uint() & 1) == 1;
            if (isSet == doesBranchOnSet()) {
                Address dest = (Address)oprs.get(1);
                context.register[PC] = dest.getAddress();
            }
        }

        private boolean doesBranchOnSet() {
            return ordinal() == BLBS.ordinal();
        }
    }


    enum Call implements ICode {
        CALLG (0xfa, B,B),
        CALLS (0xfb, L,B);

        private final int bin;
        private final DataType[] operands;

        private Call(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            if (callType() == 'S') {
                IntData nArgs = oprs.get(0).getValue();
                context.push(nArgs);
            }
            int preSp = context.register[SP];
            context.register[SP] &= ~0x3;

            int addr = ((Address)oprs.get(1)).getAddress();
            int entryMask = context.memory.load(addr, DataType.W).uint();
            for (int i = 11; i >= 0; i--) {
                if ((entryMask & 1 << i) != 0) {
                    context.push(context.register[i]);
                }
            }
            context.push(context.register[PC]);
            context.push(context.register[FP]);
            context.push(context.register[AP]);

            context.flagN.clear();
            context.flagZ.clear();
            context.flagV.clear();
            context.flagC.clear();

            int status = 0;
            status |= preSp << 30;               // low 2 bits of the SP
            if (callType() == 'S') {
                status |= 0b1 << 29;             // S flag
            }
            status |= (entryMask & 0xfff) << 16; // procedure entry mask[0..12]
            status |= context.psl & 0xffef;      // processor status register[0..15] with T cleard
            context.push(status);

            context.push(0);

            context.register[FP] = context.register[SP];
            if (callType() == 'G') {
                context.register[AP] = ((Address)oprs.get(0)).getAddress();
            } else {
                context.register[AP] = preSp;
            }

            context.flagIV.set( (entryMask & 0x4000) != 0 );
            context.flagDV.set( (entryMask & 0x8000) != 0 );
            context.flagFU.clear();

            context.register[PC] = addr + 2;
        }

        private char callType() {
            if (ordinal() == CALLG.ordinal()) {
                return 'G';
            } else {
                return 'S';
            }
        }
    }

    enum Ret implements ICode {
        RET (0x4);

        private final int bin;
        private final DataType[] operands;

        private Ret(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            context.register[SP] = context.register[FP] + 4;
            int tmp = context.pop();
            context.register[AP] = context.pop();
            context.register[FP] = context.pop();
            context.register[PC] = context.pop();

            int entryMask = (tmp >> 16) & 0xfff;
            for (int i = 0; i <= 11; i++) {
                if ((entryMask & 1 << i) != 0) {
                    context.register[i] = context.pop();
                }
            }

            context.register[SP] |= tmp >>> 30;

            context.psl = tmp & 0xffff;

            boolean isCalledWithS = (tmp & 0b1 << 29) != 0;
            if (isCalledWithS) {
                int nArgs = context.pop() & 0xff;
                context.register[SP] += nArgs * 4;
            }
        }
    }

    enum Chmk implements ICode {
        CHMK  (0xbc, W);

        private final int bin;
        private final DataType[] operands;

        private Chmk(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            int codeNum = oprs.get(0).getValue().uint();
            Kernel.syscall(codeNum, context);
        }
    }

    enum Case implements ICode {
        CASEB (0x8f, B,B,B), CASEW (0xaf, W,W,W),
        CASEL (0xcf, L,L,L);

        private final int bin;
        private final DataType[] operands;

        private Case(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData sel = oprs.get(0).getValue();
            IntData base = oprs.get(1).getValue();
            IntData limit = oprs.get(2).getValue();
            IntData offset = new IntData(sel.sint() - base.sint(), sel.dataType());

            Calculator.sub(offset, limit, context);
            context.flagV.clear();

            if (context.flagC.get() || context.flagZ.get()) {
                int dispAddr = context.register[PC] + offset.uint() * 2;
                IntData disp = context.memory.load(dispAddr, DataType.W);
                context.register[PC] += disp.sint();
            } else {
                context.register[PC] += (limit.uint() + 1) * 2;
            }
        }
    }

    enum Aob implements ICode {
        AOBLSS(0xf2, L,L,BrB) {
            @Override protected boolean check(IntData index, IntData limit) {
                return index.sint() < limit.sint();
            }
        },
        AOBLEQ(0xf3, L,L,BrB) {
            @Override protected boolean check(IntData index, IntData limit) {
                return index.sint() <= limit.sint();
            }
        };

        private final int bin;
        private final DataType[] operands;

        private Aob(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData limit = oprs.get(0).getValue();
            Operand indexOpr = oprs.get(1);
            IntData index = indexOpr.getValue();
            Address dest = (Address)oprs.get(2);
            boolean preFlagC = context.flagC.get();

            index = Calculator.add(index, new IntData(1), context);
            indexOpr.setValue(index);
            context.flagC.set(preFlagC);

            if (check(index, limit)) {
                context.register[PC] = dest.getAddress();
            }
        }

        protected abstract boolean check(IntData index, IntData limit);
    }

    enum Sob implements ICode {
        SOBGEQ (0xf4, L,BrB) {
            @Override protected boolean check(Context context) {
                return !context.flagN.get();
            }
        },
        SOBGTR(0xf5, L,BrB) {
            @Override protected boolean check(Context context) {
                return !context.flagN.get() && !context.flagZ.get();
            }
        };

        private final int bin;
        private final DataType[] operands;

        private Sob(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            Operand indexOpr = oprs.get(0);
            IntData index = indexOpr.getValue();
            Address dest = (Address)oprs.get(1);
            boolean preFlagC = context.flagC.get();

            index = Calculator.sub(index, new IntData(1), context);
            indexOpr.setValue(index);
            context.flagC.set(preFlagC);

            if (check(context)) {
                context.register[PC] = dest.getAddress();
            }
        }

        protected abstract boolean check(Context context);
    }

    enum Cvt implements ICode {
        CVTBW (0x99, B,W), CVTBL (0x98, B,L),
        CVTWB (0x33, W,B), CVTWL (0x32, W,L),
        CVTLB (0xf6, L,B), CVTLW (0xf7, L,W);

        private final int bin;
        private final DataType[] operands;

        private Cvt(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData srcVal = oprs.get(0).getValue();
            Operand dest = oprs.get(1);
            IntData cvtVal = new IntData(srcVal.sint(), operands[1]);
            dest.setValue(cvtVal);

            context.flagN.set( cvtVal.isNegValue() );
            context.flagZ.set( cvtVal.isZeroValue() );
            context.flagV.set( srcVal.isNegValue() != cvtVal.isNegValue() );
            context.flagC.clear();
        }
    }
    /*
      enum FCvt implements ICode {
      CVTBF (0x4c, B,F),   CVTBD (0x6c, B,D),
      CVTBG (0x4cfd, B,G), CVTBH (0x6cfd, B,H),
      CVTWF (0x4d, W,F),   CVTWD (0x6d, W,D),
      CVTWG (0x4dfd, W,G), CVTWH (0x6dfd, W,H),
      CVTLF (0x4e, L,F),   CVTLD (0x6e, L,D),
      CVTLG (0x4efd, L,G), CVTLH (0x6efd, L,H),
      CVTFB (0x48, F,B),   CVTDB (0x68, D,B),
      CVTGB (0x48fd, G,B), CVTHB (0x68fd, H,B),
      CVTFW (0x49, F,W),   CVTDW (0x69, D,W),
      CVTGW (0x49fd, G,W), CVTHW (0x69fd, H,W),
      CVTFL (0x4a, F,L),   CVTRFL(0x4b, F,L),
      CVTDL (0x6a, D,L),   CVTRDL(0x6b, D,L),
      CVTGL (0x4afd, G,L), CVTRGL(0x4bfd, G,L),
      CVTHL (0x6afd, H,L), CVTRHL(0x6bfd, H,L),
      CVTFD (0x56, F,D),   CVTFG (0x99fd, F,G),
      CVTFH (0x98fd, F,H), CVTDF (0x76, D,F),
      CVTDH (0x32fd, D,H), CVTGF (0x33fd, G,F),
      CVTGH (0x56fd, G,H), CVTHF (0xf6fd, H,F),
      CVTHD (0xf7fd, H,D), CVTHG (0x76fd, H,G);

      private final int bin;
      private final DataType[] operands;

      private FCvt(int bin, DataType... oprs) {
      this.bin = bin;
      this.operands = oprs;
      }

      @Override public int bin() {
      return bin;
      }

      @Override public DataType[] operands() {
      return operands;
      }

      @Override public String mnemonic() {
      return name().toLowerCase(Locale.ENGLISH);
      }

      @Override public void execute(List<Operand> oprs) {
      }
      }*/

    enum Cvtlp implements ICode {
        CVTLP (0xf9, L,W,B);

        private final int bin;
        private final DataType[] operands;

        private Cvtlp(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData srcVal = oprs.get(0).getValue();
            long src = (long)srcVal.sint();
            int len = oprs.get(1).getValue().uint();
            Address dest = (Address)oprs.get(2);

            byte tail = src >= 0 ? (byte)12 : (byte)13;
            src = Math.abs(src);
            if (len != 0) {
                tail += src % 10 << 4;
                src /= 10;
                --len;
            }

            Deque<Byte> bytes = new ArrayDeque<>();
            bytes.addFirst(tail);

            while (len-- > 0) {
                byte val = (byte)(src % 10);
                src /= 10;
                if (len-- > 0) {
                    val |= (byte)(src % 10 << 4);
                    src /= 10;
                }
                bytes.addFirst(val);
            }

            int destAddr = dest.getAddress();
            while (!bytes.isEmpty()) {
                byte val = bytes.removeFirst();
                context.memory.store(destAddr++, new IntData(val, DataType.B));
            }

            context.register[0] = 0;
            context.register[1] = 0;
            context.register[2] = 0;
            context.register[3] = dest.getAddress();

            context.flagN.set( srcVal.isNegValue() );
            context.flagZ.set( srcVal.isZeroValue() );
            context.flagC.clear();
        }
    }

    enum Acb implements ICode {
        ACBB (0x9d, B,B,B,BrW), ACBW (0x3d, W,W,W,BrW),
        ACBL (0xf1, L,L,L,BrW);

        private final int bin;
        private final DataType[] operands;

        private Acb(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData limit = oprs.get(0).getValue();
            IntData addend = oprs.get(1).getValue();
            Operand indexOpr = oprs.get(2);
            IntData index = indexOpr.getValue();
            Address dest = (Address)oprs.get(3);
            boolean preFlagC = context.flagC.get();

            index = Calculator.add(index, addend, context);
            indexOpr.setValue(index);
            context.flagC.set(preFlagC);

            if (!addend.isNegValue()) {
                if (index.sint() <= limit.sint()) {
                    context.register[PC] = dest.getAddress();
                }
            } else {
                if (index.sint() >= limit.sint()) {
                    context.register[PC] = dest.getAddress();
                }
            }
        }
    }

    enum Movc implements ICode {
        MOVC3 (0x28, W,B,B), MOVC5 (0x2c, W,B,B,W,B);

        private final int bin;
        private final DataType[] operands;

        private Movc(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData srclen = oprs.get(0).getValue();
            int srcAddr = ((Address)oprs.get(1)).getAddress();
            IntData fillVal;
            IntData destlen;
            int destAddr;
            if (oprs.size() == 5) {
                fillVal = oprs.get(2).getValue();
                destlen = oprs.get(3).getValue();
                destAddr = ((Address)oprs.get(4)).getAddress();
            } else {
                fillVal = null;
                destlen = srclen;
                destAddr = ((Address)oprs.get(2)).getAddress();
            }

            int slen = srclen.uint();
            int dlen = destlen.uint();
            for (; slen > 0 && dlen >0; slen--, dlen--) {
                IntData byteVal = context.memory.load(srcAddr++, DataType.B);
                context.memory.store(destAddr++, byteVal);
            }
            for (; dlen > 0; dlen--) {
                context.memory.store(destAddr++, fillVal);
            }

            context.register[0] = slen;
            context.register[1] = srcAddr;
            context.register[2] = 0;
            context.register[3] = destAddr;
            context.register[4] = 0;
            context.register[5] = 0;
            // Set flags
            Calculator.sub(srclen, destlen, context);
            context.flagV.clear();
        }
    }

    enum Cmpc implements ICode {
        CMPC3 (0x29, W,B,B), CMPC5 (0x2d, W,B,B,W,B);

        private final int bin;
        private final DataType[] operands;

        private Cmpc(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData str1len = oprs.get(0).getValue();
            int str1Addr = ((Address)oprs.get(1)).getAddress();
            IntData fillVal;
            IntData str2len;
            int str2Addr;
            if (oprs.size() == 5) {
                fillVal = oprs.get(2).getValue();
                str2len = oprs.get(3).getValue();
                str2Addr = ((Address)oprs.get(4)).getAddress();
            } else {
                fillVal = null;
                str2len = str1len;
                str2Addr = ((Address)oprs.get(2)).getAddress();
            }

            int s1len = str1len.uint();
            int s2len = str2len.uint();
            COMPC: {
                for (; s1len > 0 && s2len >0; s1len--, s2len--, str1Addr++, str2Addr++) {
                    IntData str1Val = context.memory.load(str1Addr, DataType.B);
                    IntData str2Val = context.memory.load(str2Addr, DataType.B);
                    Calculator.sub(str1Val, str2Val, context);
                    if (!context.flagZ.get()) {
                        break COMPC;
                    }
                }
                for (; s1len > 0; s1len--, str1Addr++) {
                    IntData str1Val = context.memory.load(str1Addr, DataType.B);
                    Calculator.sub(str1Val, fillVal, context);
                    if (!context.flagZ.get()) {
                        break COMPC;
                    }
                }
                for (; s2len > 0; s2len--, str2Addr++) {
                    IntData str2Val = context.memory.load(str2Addr, DataType.B);
                    Calculator.sub(fillVal, str2Val, context);
                    if (!context.flagZ.get()) {
                        break COMPC;
                    }
                }
            }

            context.register[0] = s1len;
            context.register[1] = str1Addr;
            context.register[2] = s2len;
            context.register[3] = str2Addr;
            context.flagV.clear();
        }
    }

    enum Locc implements ICode {
        LOCC (0x3a, B,W,B) {
            @Override protected boolean isDetected(IntData actual, IntData target) {
                return actual.uint() == target.uint();
            }
        },
        SKPC (0x3b, B,W,B) {
            @Override protected boolean isDetected(IntData actual, IntData target) {
                return actual.uint() != target.uint();
            }
        };

        private final int bin;
        private final DataType[] operands;

        private Locc(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            IntData target = oprs.get(0).getValue();
            int len = oprs.get(1).getValue().uint();
            int addr = ((Address)oprs.get(2)).getAddress();

            for (; len > 0; len--, addr++) {
                IntData byteVal = context.memory.load(addr, DataType.B);
                if (isDetected(target, byteVal)) {
                    break;
                }
            }
            context.register[0] = len;
            context.register[1] = addr;
            context.flagN.clear();
            context.flagZ.set( context.register[0] == 0 );
            context.flagV.clear();
            context.flagC.clear();
        }

        protected abstract boolean isDetected(IntData actual, IntData target);
    }

    enum Movp implements ICode {
        MOVP (0x34, W,B,B);

        private final int bin;
        private final DataType[] operands;

        private Movp(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        @Override public void execute(List<Operand> oprs) {
            int len = oprs.get(0).getValue().uint();
            int srcAddr = ((Address)oprs.get(1)).getAddress();
            int destAddr = ((Address)oprs.get(2)).getAddress();
            int mostSigSrcAddr = srcAddr;
            int mostSigDestAddr = destAddr;

            int byteslen = len / 2 + 1;
            for (; byteslen > 0; byteslen--) {
                IntData byteVal = context.memory.load(srcAddr++, DataType.B);
                context.memory.store(destAddr++, byteVal);
            }

            context.register[0] = 0;
            context.register[1] = mostSigSrcAddr;
            context.register[2] = 0;
            context.register[3] = mostSigDestAddr;
            context.flagN.set( isNegativePacked(destAddr, len) );
            context.flagZ.set( isZeroPacked(destAddr, len) );
            context.flagV.clear();
        }

        private boolean isNegativePacked(int addr, int len) {
            int signAddr = addr + len / 2;
            byte sign = (byte)(context.memory.load(signAddr, DataType.B).uint() & 0xf);
            switch (sign) {
            case 0xa: case 0xc: case 0xe: case 0xf:
                return false;
            case 0xb: case 0xd:
                return true;
            default:
                assert false : "Invalid Packed decimal string";
                return false;
            }
        }

        private boolean isZeroPacked(int addr, int len) {
            int numslen = len / 2;
            for (; numslen > 0; numslen--) {
                int val = context.memory.load(addr++, DataType.B).uint();
                if (val != 0) {
                    return false;
                }
            }
            return (addr & 0xf0) == 0;
        }
    }

    enum Editpc implements ICode {
        EDITPC (0x38, W,B,B,B);

        private final int bin;
        private final DataType[] operands;

        private Editpc(int bin, DataType... oprs) {
            this.bin = bin;
            this.operands = oprs;
        }

        @Override public int bin() {
            return bin;
        }

        @Override public DataType[] operands() {
            return operands;
        }

        @Override public String mnemonic() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        private final Queue<Byte> srcDigits = new ArrayDeque<>();
        private final List<Byte> destChars = new ArrayList<>();
        private int mostSignificantDigitAddr;

        @Override public void execute(List<Operand> oprs) {
            int srcLen = oprs.get(0).getValue().uint();
            int srcAddr = ((Address)oprs.get(1)).getAddress();
            int ptnAddr = ((Address)oprs.get(2)).getAddress();

            assert srcLen <= 31 : "Reserved operand fault";

            context.flagN.set( isNegativePacked(srcAddr, srcLen) );
            setSignChar((byte)(context.flagN.get() ? '-' : ' '));
            setFillChar((byte)' ');
            initDigits(srcAddr, srcLen);
            context.flagV.clear();
            context.flagC.clear();

            int endOpAddr = execOps(ptnAddr);

            // after execution
            byte[] charBytes = new byte[destChars.size()];
            for (int i = 0; i < destChars.size(); ++i) {
                charBytes[i] = destChars.get(i);
            }

            int destAddr = ((Address)oprs.get(3)).getAddress();
            context.memory.storeBytes(destAddr, charBytes, charBytes.length);

            context.register[0] = srcLen;
            context.register[1] = srcAddr;
            context.register[2] = 0;
            context.register[3] = endOpAddr;
            context.register[4] = 0;
            context.register[5] = destAddr + charBytes.length;
        }

        private int execOps(int ptnAddr) {
            do {
                int code = context.memory.load(ptnAddr, DataType.B).uint();
                if (code == 0x0) {
                    doEnd(context);
                    return ptnAddr;
                } else if (code == 0x1) {
                    doEndFloat(context);
                } else if (0x91 <= code && code <= 0x9f) {
                    doMove(code & 0xf);
                } else if (0xa1 <= code && code <= 0xaf) {
                    doFloat(code & 0xf);
                } else {
                    assert false : "Unimplemented editpc operand :" + code;
                }
                ++ptnAddr;
            } while (true);
        }

        private void doEnd(Context context) {
            assert srcDigits.size() == 0 : "Reserved operand abort";
            if (context.flagN.get()) {
                context.flagZ.clear();
            }
        }

        private void doEndFloat(Context context) {
            if (!hasSignificanceSet(context)) {
                destChars.add(getSignChar(context));
                setSignificance(context);
            }
        }

        private void doMove(int count) {
            assert count <= srcDigits.size() : "Reserved operand abort";

            for (; count > 0; count--) {
                byte digit = srcDigits.remove();
                if (digit != 0) {
                    setSignificance(context);
                    clearZero(context);
                }
                if (!hasSignificanceSet(context)) {
                    destChars.add(getFillChar(context));
                } else {
                    destChars.add(asciiNumCode(digit));
                }
            }
        }

        private void doFloat(int count) {
            assert count <= srcDigits.size() : "Reserved operand abort";

            for (; count > 0; count--) {
                byte digit = srcDigits.remove();
                if (digit != 0) {
                    if (!hasSignificanceSet(context)) {
                        destChars.add(getSignChar(context));
                    }
                    setSignificance(context);
                    clearZero(context);
                }
                if (!hasSignificanceSet(context)) {
                    destChars.add(getFillChar(context));
                } else {
                    destChars.add(asciiNumCode(digit));
                }
            }
        }

        private byte getFillChar(Context context) {
            return (byte)context.register[2];
        }

        private void setFillChar(byte c) {
            context.register[2] &= ~0xff;
            context.register[2] |= c;
        }

        private byte getSignChar(Context context) {
            return (byte)(context.register[2] >> 8);
        }

        private void setSignChar(byte c) {
            context.register[2] &= ~0xff00;
            context.register[2] |= c << 8;
        }

        private boolean hasSignificanceSet(Context context) {
            return context.flagC.get();
        }

        private void setSignificance(Context context) {
            context.flagC.set();
        }

        private void clearZero(Context context) {
            context.flagZ.clear();
        }

        private byte asciiNumCode(byte val) {
            return (byte)(val + '0');
        }

        private boolean isNegativePacked(int addr, int len) {
            int signAddr = addr + len / 2;
            byte sign = (byte)(context.memory.load(signAddr, DataType.B).uint() & 0xf);
            switch (sign) {
            case 0xa: case 0xc: case 0xe: case 0xf:
                return false;
            case 0xb: case 0xd:
                return true;
            default:
                assert false : "Invalid Packed decimal string";
                return false;
            }
        }

        private void initDigits(int addr, int len) {
            srcDigits.clear();
            destChars.clear();

            if (len % 2 == 0) {
                byte firstDigit = (byte)(context.memory.load(addr++, DataType.B).uint() & 0xf);
                srcDigits.add(firstDigit);
                --len;
            }

            while (len > 0) {
                int twoDigits = context.memory.load(addr++, DataType.B).uint();
                srcDigits.add((byte)(twoDigits >>> 4));
                --len;
                if (len <= 0) {
                    break;
                }
                srcDigits.add((byte)(twoDigits & 0xf));
                --len;
            }
        }
    }
}

class Nullcode extends Opcode {
    private final short val;
    private final DataType[] operands = new DataType[0];

    protected Nullcode(int val) {
        super(null);
        this.val = (short)val;
    }

    @Override public DataType[] operands() {
        return operands;
    }

    @Override public String mnemonic() {
        return String.format(".word 0x%x", val);
    }

    @Override public int len() {
        return 2;
    }

    @Override public void execute(List<Operand> oprs, Context context) {
        System.err.printf("Error: unknown code: 0x%x%n", val);
        throw new RuntimeException();
    }
}

class Calculator {
    public static IntData add(IntData arg1, IntData arg2, boolean addCarry, Context context) {
        byte[] sumBytes = new byte[arg1.size()];
        byte[] arg1Bytes = arg1.bytes();
        byte[] arg2Bytes = arg2.bytes();
        int carry = addCarry ? 1 : 0;
        for (int i = 0; i < arg1.size(); i++) {
            int tmp = (arg1Bytes[i] & 0xff) + (arg2Bytes[i] & 0xff) + carry;
            sumBytes[i] = (byte)tmp;
            carry = tmp >> 8;
        }
        IntData sum = new IntData(sumBytes, arg1.dataType());
        context.flagN.set( sum.isNegValue() );
        context.flagZ.set( sum.isZeroValue() );
        context.flagV.set( arg1.isNegValue() == arg2.isNegValue() &&
                           arg1.isNegValue() != sum.isNegValue() );
        context.flagC.set( carry == 1 );
        return sum;
    }

    public static IntData add(IntData arg1, IntData arg2, Context context) {
        return add(arg1, arg2, false, context);
    }

    public static IntData sub(IntData arg1, IntData arg2, Context context) {
        IntData diff = add(arg1, IntData.bitInvert( arg2 ), true, context);
        context.flagC.set( !context.flagC.get() );
        return diff;
    }

    public static IntData mul(IntData arg1, IntData arg2, Context context) {
        long product64b = (long)arg1.sint() * arg2.sint();
        IntData prod = new IntData((int)product64b, arg1.dataType());

        context.flagN.set( prod.isNegValue() );
        context.flagZ.set( prod.isZeroValue() );
        int highHalf = (int)(product64b >>> 32);
        context.flagV.set( (prod.isNegValue() && highHalf == 0xffffffff) ||
                           (!prod.isNegValue() && highHalf == 0) );
        context.flagC.clear();
        return prod;
    }

    public static IntData div(IntData dividend, IntData divisor, Context context) {
        if (divisor.uint() == 0 ||
            (dividend.isLargestNegativeInteger() && divisor.sint() == -1)) {
            context.flagV.set();
            return null;
        } else {
            IntData quo = new IntData(dividend.sint() / divisor.sint(), divisor.dataType());
            context.flagN.set( quo.isNegValue() );
            context.flagZ.set( quo.isZeroValue() );
            context.flagV.clear();
            context.flagC.clear();
            return quo;
        }
    }
}

/* Not Implemented
    HALT (0x0), REI   (0x2),
    BPT (0x3), RET   (0x4),
    RSB (0x5), LDPCTX (0x6),
    SVPCTX (0x7), CVTPS (0x8, W,B,W,B),
    CVTSP (0x9,W,B,W,B), INDEX (0xa, L,L,L,L,L,L),
    CRC (0xb, B,L,W,B), PROBER (0xc, B,W,B),
    PROBEW (0xd, B,W,B), INSQUE (0xe, B,B),
    REMQUE (0xf, B,W), BSBB (0x10, BrB),
    JSB (0x16, B),
    ADDP4 (0x20, W,B,W,B), ADDP6 (0x21, W,B,W,B,W,B),
    SUBP4 (0x22, W,B,W,B), SUBP6 (0x23, W,B,W,B,W,B),
    CVTPT (0x24, W,B,B,W,B), MULP (0x25, W,B,W,B,W,B),
    CVTTP (0x26, W,B,B,W,B), DIVP (0x27, W,B,W,B,W,B),
    SCANC (0x2a, W,B,B,B), SPANC (0x2b, W,B,B,B),
    MOVTC (0x2e, W,B,B,B,W,B), MOVTUC (0x2f, W,B,B,B,W,B),
    BSBW  (0x30, BrW), CMPP3 (0x35, W,B,B),
    CVTPL (0x36, W,B,L), CMPP4 (0x37, W,B,W,B),
    MATCHC(0x39, W,B,W,B),
    MULF2 (0x44, F,F), MULG2 (0x44fd, G,G),
    MULF3 (0x45, F,F,F), MULG3 (0x45fd, G,G,G),
    DIVF2 (0x46, F,F), DIVG2 (0x46fd, G,G),
    DIVF3 (0x47, F,F,F), DIVG3 (0x47fd, G,G,G),
    ACBF (0x4f, F,F,F,BrW), ACBG (0x4ffd, G,G,G,BrW),
    EMODF (0x54, F,B,F,L,F), EMODG (0x54fd, G,W,G,L,G),
    POLYF (0x55, F,W,B), POLYG (0x55fd, G,W,B),
    ADAWI (0x58, W,W),
    INSQHI (0x5c, B,Q), INSQTI (0x5d, B,Q),
    REMQHI (0x5e, Q,L), REMQTI (0x5f, Q,L),
    MULD2 (0x64, D,D), MULH2 (0x64fd, H,H),
    MULD3 (0x65, D,D,D), MULH3 (0x65fd, H,H,H),
    DIVD2 (0x66, D,D), DIVH2 (0x66fd, H,H),
    DIVD3 (0x67, D,D,D), DIVH3 (0x67fd, H,H,H),
    ACBD (0x6f, D,D,D,BrW), ACBH (0x6ffd, H,H,H,BrW),
    EMODD (0x74, D,B,D,L,D), EMODH (0x74fd, H,W,H,L,H),
    POLYD (0x75, D,W,B), POLYH (0x75fd, H,W,B),
    EMUL (0x7a, L,L,L,Q), EDIV (0x7b, L,Q,L,L),
    ROTL  (0x9c, B,L,L),
    BISPSW (0xb8, W), BICPSW (0xb9, W),
    POPR (0xba, W), PUSHR (0xbb, W),
    CHME (0xbd, W), CHMS (0xbe, W),
    CHMU (0xbf, W),
    ADWC (0xd8, L,L), SBWC (0xd9, L,L),
    MTPR (0xda, L,L), MFPR (0xdb, L,L),
    MOVPSL (0xdc, L),
    FFS (0xea, L,B,B,L), FFC (0xeb, L,B,B,L),
    CMPV (0xec, L,B,B,L), CMPZV (0xed, L,B,B,L),
    ASHP (0xf8, B,W,B,B,W,B), XFC (0xfc),
    BUGL (0xfdff, L), BUGW (0xfeff, W);
*/
