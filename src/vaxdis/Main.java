// This file is licensed under the CC0.
package vaxdis;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

class Memory {

    protected final byte[] text;
    protected final ByteBuffer buf;
    protected int pc, offset;

    public Memory(String path) throws IOException {
        this(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path)));
    }

    public Memory(byte[] text) throws IOException {
        this.text = text;
        buf = ByteBuffer.wrap(text).order(ByteOrder.LITTLE_ENDIAN);
    }

    public int fetch() {
        return Byte.toUnsignedInt(text[(pc++) - offset]);
    }

    public int fetchSigned(int size) {
        int oldpc = pc;
        pc += size;
        switch (size) {
            case 1:
                return text[oldpc - offset];
            case 2:
                return buf.getShort(oldpc - offset);
            case 4:
                return buf.getInt(oldpc - offset);
        }
        return 0;
    }

    public String fetchHex(int size, String suffix) {
        int[] bs = new int[size];
        for (int i = 0; i < size; ++i) {
            bs[i] = fetch();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = size - 1; i >= 0; --i) {
            sb.append(String.format("%02x", bs[i]));
        }
        String x = sb.toString();
        int p = 0;
        while (p < x.length() - 1 && x.charAt(p) == '0') {
            ++p;
        }
        String xx = x.substring(p);
        String prefix = "0x";
        if (xx.length() == 1 && Character.isDigit(xx.charAt(0))) {
            prefix = "";
        }
        return prefix + x.substring(p) + suffix;
    }

    public void output(PrintStream out, int pc, int len, String asm) {
        for (int i = 0; i < len; ++i) {
            if ((i & 7) == 0) {
                if (i > 0 && i == 8) {
                    out.println("  " + asm);
                }
                out.printf("%08x:", pc + i);
            }
            out.printf(" %02x", text[(pc + i) - offset]);
        }
        if (len <= 8) {
            for (int i = len; i < 8; ++i) {
                out.print("   ");
            }
            out.print("  " + asm);
        }
        out.println();
    }
}

enum VAXType {

    BYTE('b', 1, ""), WORD('w', 2, ""), LONG('l', 4, ""),
    QWORD('q', 8, ""), OWORD('o', 16, ""),
    FFLOAT('f', 4, " [f-float]"), DFLOAT('d', 8, " [d-float]"),
    GFLOAT('g', 8, " [g-float]"), HFLOAT('h', 16, " [h-float]"),
    RELB('1', 1, ""), RELW('2', 2, "");

    public static final VAXType[] table = new VAXType[128];
    public final char suffix;
    public final int size;
    public final String valueSuffix;

    static {
        for (VAXType t : VAXType.values()) {
            table[(int) t.suffix] = t;
        }
    }

    private VAXType(char suffix, int size, String valueSuffix) {
        this.suffix = suffix;
        this.size = size;
        this.valueSuffix = valueSuffix;
    }
}

enum VAXOp {

    HALT(0x00, ""), NOP(0x01, ""), REI(0x02, ""), BPT(0x03, ""),
    RET(0x04, ""), RSB(0x05, ""), LDPCTX(0x06, ""), SVPCTX(0x07, ""),
    CVTPS(0x08, "wbwb"), CVTSP(0x09, "wbwb"), INDEX(0x0a, "llllll"), CRC(0x0b, "blwb"),
    PROBER(0x0c, "bwb"), PROBEW(0x0d, "bwb"), INSQUE(0x0e, "bb"), REMQUE(0x0f, "bl"),
    BSBB(0x10, "1"), BRB(0x11, "1"), BNEQ(0x12, "1"), BEQL(0x13, "1"),
    BGTR(0x14, "1"), BLEQ(0x15, "1"), JSB(0x16, "b"), JMP(0x17, "b"),
    BGEQ(0x18, "1"), BLSS(0x19, "1"), BGTRU(0x1a, "1"), BLEQU(0x1b, "1"),
    BVC(0x1c, "1"), BVS(0x1d, "1"), BCC(0x1e, "1"), BLSSU(0x1f, "1"),
    ADDP4(0x20, "wbwb"), ADDP6(0x21, "wbwbwb"), SUBP4(0x22, "wbwb"), SUBP6(0x23, "wbwbwb"),
    CVTPT(0x24, "wbbwb"), MULP(0x25, "wbwbwb"), CVTTP(0x26, "wbbwb"), DIVP(0x27, "wbwbwb"),
    MOVC3(0x28, "wbb"), CMPC3(0x29, "wbb"), SCANC(0x2a, "wbbb"), SPANC(0x2b, "wbbb"),
    MOVC5(0x2c, "wbbwb"), CMPC5(0x2d, "wbbwb"), MOVTC(0x2e, "wbbbwb"), MOVTUC(0x2f, "wbbbwb"),
    BSBW(0x30, "2"), BRW(0x31, "2"), CVTWL(0x32, "wl"), CVTWB(0x33, "wb"),
    MOVP(0x34, "wbb"), CMPP3(0x35, "wbb"), CVTPL(0x36, "wbl"), CMPP4(0x37, "wbwb"),
    EDITPC(0x38, "wbbb"), MATCHC(0x39, "wbwb"), LOCC(0x3a, "bwb"), SKPC(0x3b, "bwb"),
    MOVZWL(0x3c, "wl"), ACBW(0x3d, "www2"), MOVAW(0x3e, "wl"), PUSHAW(0x3f, "w"),
    ADDF2(0x40, "ff"), ADDF3(0x41, "fff"), SUBF2(0x42, "ff"), SUBF3(0x43, "fff"),
    MULF2(0x44, "ff"), MULF3(0x45, "fff"), DIVF2(0x46, "ff"), DIVF3(0x47, "fff"),
    CVTFB(0x48, "fb"), CVTFW(0x49, "fw"), CVTFL(0x4a, "fl"), CVTRFL(0x4b, "fl"),
    CVTBF(0x4c, "bf"), CVTWF(0x4d, "wf"), CVTLF(0x4e, "lf"), ACBF(0x4f, "fff2"),
    MOVF(0x50, "ff"), CMPF(0x51, "ff"), MNEGF(0x52, "ff"), TSTF(0x53, "f"),
    EMODF(0x54, "fbflf"), POLYF(0x55, "fwb"), CVTFD(0x56, "fd"),
    ADAWI(0x58, "ww"),
    INSQHI(0x5c, "bq"), INSQTI(0x5d, "bq"), REMQHI(0x5e, "ql"), REMQTI(0x5f, "ql"),
    ADDD2(0x60, "dd"), ADDD3(0x61, "ddd"), SUBD2(0x62, "dd"), SUBD3(0x63, "ddd"),
    MULD2(0x64, "dd"), MULD3(0x65, "ddd"), DIVD2(0x66, "dd"), DIVD3(0x67, "ddd"),
    CVTDB(0x68, "db"), CVTDW(0x69, "dw"), CVTDL(0x6a, "dl"), CVTRDL(0x6b, "dl"),
    CVTBD(0x6c, "bd"), CVTWD(0x6d, "wd"), CVTLD(0x6e, "ld"), ACBD(0x6f, "ddd2"),
    MOVD(0x70, "dd"), CMPD(0x71, "dd"), MNEGD(0x72, "dd"), TSTD(0x73, "d"),
    EMODD(0x74, "dbdld"), POLYD(0x75, "dwb"), CVTDF(0x76, "df"),
    ASHL(0x78, "bll"), ASHQ(0x79, "bqq"), EMUL(0x7a, "lllq"), EDIV(0x7b, "lqll"),
    CLRD(0x7c, "d"), MOVQ(0x7d, "qq"), MOVAQ(0x7e, "ql"), PUSHAQ(0x7f, "q"),
    ADDB2(0x80, "bb"), ADDB3(0x81, "bbb"), SUBB2(0x82, "bb"), SUBB3(0x83, "bbb"),
    MULB2(0x84, "bb"), MULB3(0x85, "bbb"), DIVB2(0x86, "bb"), DIVB3(0x87, "bbb"),
    BISB2(0x88, "bb"), BISB3(0x89, "bbb"), BICB2(0x8a, "bb"), BICB3(0x8b, "bbb"),
    XORB2(0x8c, "bb"), XORB3(0x8d, "bbb"), MNEGB(0x8e, "bb"), CASEB(0x8f, "bbb"),
    MOVB(0x90, "bb"), CMPB(0x91, "bb"), MCOMB(0x92, "bb"), BITB(0x93, "bb"),
    CLRB(0x94, "b"), TSTB(0x95, "b"), INCB(0x96, "b"), DECB(0x97, "b"),
    CVTBL(0x98, "bl"), CVTBW(0x99, "bw"), MOVZBL(0x9a, "bl"), MOVZBW(0x9b, "bw"),
    ROTL(0x9c, "bll"), ACBB(0x9d, "bbb2"), MOVAB(0x9e, "bl"), PUSHAB(0x9f, "b"),
    ADDW2(0xa0, "ww"), ADDW3(0xa1, "www"), SUBW2(0xa2, "ww"), SUBW3(0xa3, "www"),
    MULW2(0xa4, "ww"), MULW3(0xa5, "www"), DIVW2(0xa6, "ww"), DIVW3(0xa7, "www"),
    BISW2(0xa8, "ww"), BISW3(0xa9, "www"), BICW2(0xaa, "ww"), BICW3(0xab, "www"),
    XORW2(0xac, "ww"), XORW3(0xad, "www"), MNEGW(0xae, "ww"), CASEW(0xaf, "www"),
    MOVW(0xb0, "ww"), CMPW(0xb1, "ww"), MCOMW(0xb2, "ww"), BITW(0xb3, "ww"),
    CLRW(0xb4, "w"), TSTW(0xb5, "w"), INCW(0xb6, "w"), DECW(0xb7, "w"),
    BISPSW(0xb8, "w"), BICPSW(0xb9, "w"), POPR(0xba, "w"), PUSHR(0xbb, "w"),
    CHMK(0xbc, "w"), CHME(0xbd, "w"), CHMS(0xbe, "w"), CHMU(0xbf, "w"),
    ADDL2(0xc0, "ll"), ADDL3(0xc1, "lll"), SUBL2(0xc2, "ll"), SUBL3(0xc3, "lll"),
    MULL2(0xc4, "ll"), MULL3(0xc5, "lll"), DIVL2(0xc6, "ll"), DIVL3(0xc7, "lll"),
    BISL2(0xc8, "ll"), BISL3(0xc9, "lll"), BICL2(0xca, "ll"), BICL3(0xcb, "lll"),
    XORL2(0xcc, "ll"), XORL3(0xcd, "lll"), MNEGL(0xce, "ll"), CASEL(0xcf, "lll"),
    MOVL(0xd0, "ll"), CMPL(0xd1, "ll"), MCOML(0xd2, "ll"), BITL(0xd3, "ll"),
    CLRF(0xd4, "f"), TSTL(0xd5, "l"), INCL(0xd6, "l"), DECL(0xd7, "l"),
    ADWC(0xd8, "ll"), SBWC(0xd9, "ll"), MTPR(0xda, "ll"), MFPR(0xdb, "ll"),
    MOVPSL(0xdc, "l"), PUSHL(0xdd, "l"), MOVAL(0xde, "ll"), PUSHAL(0xdf, "l"),
    BBS(0xe0, "lb1"), BBC(0xe1, "lb1"), BBSS(0xe2, "lb1"), BBCS(0xe3, "lb1"),
    BBSC(0xe4, "lb1"), BBCC(0xe5, "lb1"), BBSSI(0xe6, "lb1"), BBCCI(0xe7, "lb1"),
    BLBS(0xe8, "l1"), BLBC(0xe9, "l1"), FFS(0xea, "lbbl"), FFC(0xeb, "lbbl"),
    CMPV(0xec, "lbbl"), CMPZV(0xed, "lbbl"), EXTV(0xee, "lbbl"), EXTZV(0xef, "lbbl"),
    INSV(0xf0, "llbb"), ACBL(0xf1, "lll2"), AOBLSS(0xf2, "ll1"), AOBLEQ(0xf3, "ll1"),
    SOBGEQ(0xf4, "l1"), SOBGTR(0xf5, "l1"), CVTLB(0xf6, "lb"), CVTLW(0xf7, "lw"),
    ASHP(0xf8, "bwbbwb"), CVTLP(0xf9, "lwb"), CALLG(0xfa, "bb"), CALLS(0xfb, "lb"),
    XFC(0xfc, ""),
    CVTDH(0xfd32, "dh"), CVTGF(0xfd33, "gh"),
    ADDG2(0xfd40, "gg"), ADDG3(0xfd41, "ggg"), SUBG2(0xfd42, "gg"), SUBG3(0xfd43, "ggg"),
    MULG2(0xfd44, "gg"), MULG3(0xfd45, "ggg"), DIVG2(0xfd46, "gg"), DIVG3(0xfd47, "ggg"),
    CVTGB(0xfd48, "gb"), CVTGW(0xfd49, "gw"), CVTGL(0xfd4a, "gl"), CVTRGL(0xfd4b, "gl"),
    CVTBG(0xfd4c, "bg"), CVTWG(0xfd4d, "wg"), CVTLG(0xfd4e, "lg"), ACBG(0xfd4f, "ggg2"),
    MOVG(0xfd50, "gg"), CMPG(0xfd51, "gg"), MNEGG(0xfd52, "gg"), TSTG(0xfd53, "g"),
    EMODG(0xfd54, "gwglg"), POLYG(0xfd55, "gwb"), CVTGH(0xfd56, "gh"),
    ADDH2(0xfd60, "hh"), ADDH3(0xfd61, "hhh"), SUBH2(0xfd62, "hh"), SUBH3(0xfd63, "hhh"),
    MULH2(0xfd64, "hh"), MULH3(0xfd65, "hhh"), DIVH2(0xfd66, "hh"), DIVH3(0xfd67, "hhh"),
    CVTHB(0xfd68, "hb"), CVTHW(0xfd69, "hw"), CVTHL(0xfd6a, "hl"), CVTRHL(0xfd6b, "hl"),
    CVTBH(0xfd6c, "bh"), CVTWH(0xfd6d, "wh"), CVTLH(0xfd6e, "lh"), ACBH(0xfd6f, "hhh2"),
    MOVH(0xfd70, "hh"), CMPH(0xfd71, "hh"), MNEGH(0xfd72, "hh"), TSTH(0xfd73, "h"),
    EMODH(0xfd74, "hwhlh"), POLYH(0xfd75, "hwb"), CVTHG(0xfd76, "hg"),
    CLRH(0xfd7c, "h"), MOVO(0xfd7d, "oo"), MOVAH(0xfd7e, "hl"), PUSHAH(0xfd7f, "h"),
    CVTFH(0xfd98, "fh"), CVTFG(0xfd99, "fg"),
    CVTHF(0xfdf6, "hf"), CVTHD(0xfdf7, "hd"),
    BUGL(0xfffd, "l"), BUGW(0xfffe, "w");

    public static final VAXOp[] table = new VAXOp[0x10000];
    public final int op;
    public final String mne;
    public final char[] oprs;

    static {
        for (VAXOp op : VAXOp.values()) {
            table[op.op] = op;
        }
    }

    private VAXOp(int op, String oprs) {
        this.op = op;
        this.mne = toString().toLowerCase();
        this.oprs = oprs.toCharArray();
    }
}

class VAXDisasm extends Memory {

    private static final String[] regs = {
        "r0", "r1", "r2", "r3", "r4", "r5", "r6", "r7",
        "r8", "r9", "r10", "r11", "ap", "fp", "sp", "pc"
    };

    private AOut aout;

    public VAXDisasm(String path) throws IOException {
        super(path);
    }

    public VAXDisasm(byte[] text) throws IOException {
        super(text);
    }

    public VAXDisasm(AOut aout) throws IOException {
        super(aout.text);
        this.aout = aout;
        if (aout.a_entry < 0) {
            pc = offset = 0x80000000;
        }
    }

    public String getOpr(VAXType t) {
        if (t == VAXType.RELB || t == VAXType.RELW) {
            int rel = fetchSigned(t.size);
            return String.format("0x%x", pc + rel);
        }
        int b = fetch(), b1 = b >> 4, b2 = b & 15;
        String r = regs[b2];
        switch (b1) {
            case 0:
            case 1:
            case 2:
            case 3:
                return "$" + hex(b) + t.valueSuffix;
            case 4:
                return getOpr(t) + "[" + r + "]";
            case 5:
                return r;
            case 6:
                return "(" + r + ")";
            case 7:
                return "-(" + r + ")";
            case 8:
                if (b2 == 15) {
                    return "$" + fetchHex(t.size, t.valueSuffix);
                } else {
                    return "(" + r + ")+";
                }
            case 9:
                if (b2 == 15) {
                    return "*" + addr(fetchSigned(4));
                } else {
                    return "*(" + r + ")+";
                }
            default: {
                String prefix = (b1 & 1) == 1 ? "*" : "";
                int disp = fetchSigned(1 << ((b1 - 0xa) >> 1));
                if (b2 == 15) {
                    return prefix + addr(pc + disp);
                } else {
                    return String.format("%s%s(%s)", prefix, hex(disp), r);
                }
            }
        }
    }

    public String disasm1() {
        int opc = fetch();
        VAXOp op = VAXOp.table[opc];
        if (op == null) {
            op = VAXOp.table[opc = opc << 8 | fetch()];
        }
        if (op == null) {
            return String.format(".word 0x%x", opc);
        }
        StringBuilder sb = new StringBuilder(op.mne);
        for (int i = 0; i < op.oprs.length; ++i) {
            sb.append(i == 0 ? " " : ",");
            sb.append(getOpr(VAXType.table[op.oprs[i]]));
        }
        return sb.toString();
    }

    public void disasm(PrintStream out) {
        while (pc - offset < text.length) {
            int oldpc = pc;
            String asm = null;
            if (aout != null) {
                String o = aout.symO.get(oldpc);
                if (o != null) {
                    System.out.printf("[%s]\n", o);
                }
                if (aout.symT.containsKey(oldpc)) {
                    System.out.printf("%s:\n", aout.symT.get(oldpc));
                    asm = word1();
                } else if (oldpc == aout.a_entry) {
                    asm = word1();
                }
            }
            if (asm == null) {
                asm = disasm1();
            }
            output(out, oldpc, pc - oldpc, asm);
        }
    }

    public String word1() {
        return ".word " + hex(Short.toUnsignedInt((short) fetchSigned(2)));
    }

    public String addr(int ad) {
        String ret = String.format("0x%x", ad);
        if (aout != null && aout.symT.containsKey(ad)) {
            ret += "<" + aout.symT.get(ad) + ">";
        }
        return ret;
    }

    public static String hex(int v) {
        String sign = "";
        if (v < 0) {
            sign = "-";
            v = -v;
        }
        String x = v < 10 ? "" : "0x";
        return String.format("%s%s%x", sign, x, v);
    }
}

class Symbol {

    public final String name;
    public final int type, other, desc, value;
    public final char tchar;

    public Symbol(ByteBuffer buf, int p) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; ++i) {
            char ch = (char) buf.get(p + i);
            if (ch == 0) {
                break;
            }
            sb.append(ch);
        }
        name = sb.toString();
        type = buf.get(p + 8);
        other = buf.get(p + 9);
        desc = buf.getShort(p + 10);
        value = buf.getInt(p + 12);
        tchar = type < 10 ? "uUaAtTdDbB".charAt(type) : '?';
    }

    @Override
    public String toString() {
        return String.format("%08x %c %s", value, tchar, name);
    }
}

class AOut {

    public final ByteBuffer header;
    public final int a_magic, a_text, a_data, a_bss, a_syms, a_entry, a_trsize, a_drsize;
    public final byte[] text, data;
    public final Symbol[] syms;
    public final HashMap<Integer, String> symO = new HashMap<>();
    public final HashMap<Integer, String> symT = new HashMap<>();

    public AOut(String path) throws IOException {
        try (FileInputStream fis = new FileInputStream(path)) {
            byte[] h = new byte[0x20];
            fis.read(h);
            ByteBuffer hdr = ByteBuffer.wrap(h).order(ByteOrder.LITTLE_ENDIAN);
            a_magic = hdr.getInt();
            if (a_magic == 0x108) {
                header = hdr;
                a_text = hdr.getInt(4);
                a_data = hdr.getInt(8);
                a_bss = hdr.getInt(12);
                a_syms = hdr.getInt(16);
                a_entry = hdr.getInt(20);
                a_trsize = hdr.getInt(24);
                a_drsize = hdr.getInt(28);
                text = new byte[a_text];
                data = new byte[a_data];
                fis.read(text);
                fis.read(data);
                if (a_syms > 0) {
                    byte[] sym = new byte[a_syms];
                    fis.read(sym);
                    ByteBuffer sbuf = ByteBuffer.wrap(sym).order(ByteOrder.LITTLE_ENDIAN);
                    ArrayList<Symbol> list = new ArrayList<>();
                    for (int p = 0; p <= a_syms - 16; p += 16) {
                        Symbol s = new Symbol(sbuf, p);
                        list.add(s);
                        if (4 <= s.type && s.type <= 9) {
                            if (s.name.endsWith(".o")) {
                                symO.put(s.value, s.name);
                            } else {
                                symT.put(s.value, s.name);
                            }
                        }
                    }
                    syms = new Symbol[list.size()];
                    list.toArray(syms);
                } else {
                    syms = null;
                }
                return;
            }
        }
        header = null;
        a_text = a_data = a_bss = a_syms = a_entry = a_trsize = a_drsize = 0;
        text = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path));
        data = null;
        syms = null;
    }

    @Override
    public String toString() {
        if (a_magic != 0x108) {
            return String.format("unknown format: %08x", a_magic);
        }
        return String.format(
                "magic = %08x, text  = %08x, data   = %08x, bss    = %08x\n"
                + "syms  = %08x, entry = %08x, trsize = %08x, drsize = %08x",
                a_magic, a_text, a_data, a_bss,
                a_syms, a_entry, a_trsize, a_drsize);
    }
}

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("usage: vaxdis a.out");
            System.exit(1);
        }
        try {
            for (int i = 0; i < args.length; ++i) {
                if (i > 0) {
                    System.out.println();
                }
                if (args.length > 1) {
                    System.out.println(args[i]);
                }
                AOut aout = new AOut(args[i]);
                System.out.println(aout);
                new VAXDisasm(aout).disasm(System.out);
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }
}
