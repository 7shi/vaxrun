package ve3.disassm;

import ve3.disassm.V32Disassm.OT;
import ve3.os.OpInfo;
import ve3.os.OpInfo.Type;

public class Dump {
	private static final String[] regs = {
			"r0", "r1", "r2", "r3", "r4", "r5", "r6", "r7", "r8",
			"r9", "r10", "r11", "ap", "fp", "sp", "pc"
	};
	
	private static String getArgStr(OT ot, long arg) {
		
		if (arg < 0) return String.format("0x%x", (int)arg); // correct?
		
		switch(ot) {
		case b:
		case vb:
			return String.format("0x%x", (byte)arg);
		//case ebl: {
//			return String.format("0x%x", (int)arg);
		//}
		case w: 
			return String.format("0x%x", (short)arg);
		case l: 
		case f:{			
			return String.format("0x%x", (int)arg);
		}
		case q:
		case df: {
			return String.format("0x%x", (long)arg);
		}
		default: {
			System.out.println("unrecognised optype(OT) in getARgStr");
			System.exit(1);
		}
		}
		return "";		
	}
	
	private static String getImmedArgStr(OT ot, String fmt, long arg) {
		
		switch(ot) {
		case b:
		case vb:
			return String.format(fmt, (byte)arg);
		//case ebl: {
//			return String.format(fmt, (int)arg);
		//}
		case w: 
			return String.format(fmt, (short)arg);
		case l: 
		case f:{			
			return String.format(fmt, (int)arg);
		}
		case q:
		case df: {
			return String.format(fmt, (long)arg);
		}
		default: {
			System.out.println("unrecognised optype(OT) in getImmedArgStr");
			System.exit(1);
		}
		}
		return "";
	}
	
	private static String createOperand(Type type, byte operand, long arg, OT ot) {
		
		switch(type) {
		case Literal: { // 0~3
			String fmt = "$0x%x";
			switch(ot) {			
			case df: {
				fmt += " [d-float]";
				break;
			}
			case f: {
				fmt += " [f-float]";
				break;
			}
			}
			return String.format(fmt, operand);
		}
		case Index: {
			return "[" + regs[operand] + "]";
		}
		case Register: { // 5
			return regs[operand];
		}
		case RegDefer: { // 6
			return "(" + regs[operand] + ")";
		}
		case AutoDec: { // 7
			return "-(" + regs[operand] + ")";
		}
		case AutoInc: { // 8
			return "(" + regs[operand] + ")+";
		}		
		case AutoIncDefer: {
			return "@(" + regs[operand] + ")+";
		}
		case ByteDisp: { // 0xa
			//return String.format("0x%x(%s)", arg, regs[operand]);			
			//String ds = (ot == OT.ebl) ? getArgStr(ot, arg) : getArgStr(OT.b, arg);
			String ds = getArgStr(OT.b, arg);
			return ds + String.format("(%s)", regs[operand]);
		}
		case ByteDispDefer: { // 0x0b
			return "*" + getArgStr(OT.b, arg) + String.format("(%s)", regs[operand]);
			//return "*" + String.format("0x%x", arg) + String.format("(%s)", regs[operand]);
		}
		case WordDisp: { // 0x0c
			//return String.format("0x%x(%s)", arg, regs[operand]);
			return getArgStr(OT.w, arg) + String.format("(%s)", regs[operand]);
			//return String.format("0x%x", arg) + String.format("(%s)", regs[operand]);
		}
		case WordDispDefer: { // 0x0d
			//return String.format("*0x%x(%s)", arg, regs[operand]);
			return "*" + getArgStr(OT.w, arg) + String.format("(%s)", regs[operand]);
			//return "*" + String.format("0x%x", arg) + String.format("(%s)", regs[operand]);
		}
		case LongDisp: { // 0x0e
			return getArgStr(OT.l, arg) + String.format("(%s)", regs[operand]);
		}
		case LongDispDefer: { // 0x0f
			return "*" + getArgStr(OT.l, arg) + String.format("(%s)", regs[operand]);
		}
		case Immed: {
			/*
			switch(ot) {
			case f: {
				return "$" + getArgStr(ot, arg) + " [f-float]";
			}
			case df: {
				return "$" + getArgStr(ot, arg) + " [d-float]";
			}
			default: {
				return "$" + getArgStr(OT.l, arg);
			}
			}
			*/
			
			
			String fmt = "$0x%x";
			switch(ot) {
			case b:
				fmt = "$0x%02x";
				break;
			case w: 
				fmt = "$0x%04x";
				break;
			case l: {
				fmt = "$0x%08x";
				break;
			}
			case q: {
				fmt = "$0x%016x";
				break;
			}
			case df: {
				fmt = "$0x%016x [d-float]";
				break;
			}
			case f: {
				fmt = "$0x%08x [f-float]";
				break;
			}
			}
			//return String.format(fmt, arg);
			return getImmedArgStr(ot, fmt, arg);
			
		}
		case Abs: { //Absolute mode (in program counter mode)
			return String.format("*0x%x", (int)arg);
		}
		case WordRel: { // program counter mode
			return String.format("0x%x", arg);
		}
		case WordRelDefer: { // program counter mode
			return String.format("*0x%x", (int)arg);
		}
		case LongRel: { // 0xe : program conter mode
			return String.format("0x%x", arg);
		}
		case LongRelDefer: { // 0x0f program counter mode
			return String.format("*0x%x", (int)arg);
		}
		case Branch1: { // branch disp byte
			return String.format("0x%x", (int)arg);
		}
		case Branch2: {
			return String.format("0x%x", (int)arg);
		}
		default: {
			System.out.println("unrecognized type in Dump");
			System.exit(1);
			break;
		}
		}		
		return "";
	}
	
	
	
	public static String dump(OpInfo opinfo, String opname) {
		
		switch(opinfo.minfo.size) {
		case 0: {
			return opname;	
		}
		case 1: {
			return opname + " " + dump1(opinfo);
		}
		case 2: {
			return opname + " " + dump2(opinfo);
		}
		case 3: {
			return opname + " " + dump3(opinfo);
		}
		case 4: {
			return opname + " " + dump4(opinfo);
		}
		case 5: {
			return opname + " " + dump5(opinfo);
		}
		case 6: {
			return opname + " " + dump6(opinfo);
		}
		default: {
			System.out.println("unrecognized size in dump: " + opinfo.minfo.size);
			System.exit(1);
			return "";
			
		}
		}		
	}
	
	private static String dump1(OpInfo opinfo) {
		String s = createOperand(opinfo.getType1(), opinfo.getOpe1(), opinfo.getArg1(), opinfo.minfo.arg1);
		while (opinfo.hasIdx1()) {
			s += createOperand(Type.Index, opinfo.popIdx1(), 0, null);
		}		
		return s;
	}
	
	private static String dump2(OpInfo opinfo) {
		String s = dump1(opinfo) + "," + createOperand(opinfo.getType2(), opinfo.getOpe2(), opinfo.getArg2(), opinfo.minfo.arg2);
		while (opinfo.hasIdx2()) {
			s += createOperand(Type.Index, opinfo.popIdx2(), 0, null);
		}		
		return s;
	}
	
	private static String dump3(OpInfo opinfo) {
		String s = dump2(opinfo) + "," + createOperand(opinfo.getType3(), opinfo.getOpe3(), opinfo.getArg3(), opinfo.minfo.arg3);
		while (opinfo.hasIdx3()) {
			s += createOperand(Type.Index, opinfo.popIdx3(), 0, null);
		}		
		return s;
	}
	
	private static String dump4(OpInfo opinfo) {
		String s = dump3(opinfo) + "," + createOperand(opinfo.getType4(), opinfo.getOpe4(), opinfo.getArg4(), opinfo.minfo.arg4);
		while (opinfo.hasIdx4()) {
			s += createOperand(Type.Index, opinfo.popIdx4(), 0, null);
		}		
		return s;
	}
	
	private static String dump5(OpInfo opinfo) {
		String s = dump4(opinfo) + "," + createOperand(opinfo.getType5(), opinfo.getOpe5(), opinfo.getArg5(), opinfo.minfo.arg5);
		while (opinfo.hasIdx5()) {
			s += createOperand(Type.Index, opinfo.popIdx5(), 0, null);
		}		
		return s;
	}
	
	private static String dump6(OpInfo opinfo) {
		String s = dump5(opinfo) + "," + createOperand(opinfo.getType6(), opinfo.getOpe6(), opinfo.getArg6(), opinfo.minfo.arg6);
		while (opinfo.hasIdx6()) {
			s += createOperand(Type.Index, opinfo.popIdx6(), 0, null);
		}		
		return s;
	}
}
