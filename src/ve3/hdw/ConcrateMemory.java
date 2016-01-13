package ve3.hdw;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ConcrateMemory implements Memory {
	
//	private int size;
	private int end;
	private int maxSize;
	private byte[] memory;
	private int pc;
	private int prevPc;
	private ByteArrayOutputStream buf;
	private PrintStream rawout;
	private MVal mval;
	
	private boolean remain;
	
	
	public ConcrateMemory(int maxSize) {
		remain = false;
		memory = new byte[maxSize];
		this.maxSize = maxSize;		
		buf = new ByteArrayOutputStream(30);		
		rawout = new PrintStream(buf);		
		mval = new MVal();
	}
	
	public byte[] getRawMemory() {
		return memory;
	}
	
	public int load(byte[] rawdata, int offset, int size) {
		System.arraycopy(rawdata, offset, memory, 0, size);
		pc = 0;
		//this.size = size;
		return (end = size);
		
	}
	
	public int load(byte[] rawdata, int roffset, int moffset, int size) {
		System.arraycopy(rawdata, roffset, memory, moffset, size);		
		pc = 0;
		return (end = moffset + size);
	}
	public void rawWrite(byte[] rawdata, int roffset, int moffset, int size) {
		System.arraycopy(rawdata, roffset, memory, moffset, size);
	}
	
	public byte[] rawRead(int offset, int len) {
		byte[] rdata = new byte[len];
		System.arraycopy(memory, offset, rdata, 0, len);
		return rdata;
	}
	
	public MVal fetch() {
		mval.bval = memory[pc++];
		mval.pc = pc;
		return mval;
	}
	
	public MVal fetch2() {
		byte b1 = (byte)(memory[pc++] & 0xff);
		mval.sval = (short)((memory[pc++] & 0xff) << 8 | b1 & 0xff);
		mval.pc = pc;
		return mval;
	}
	
	public MVal fetch4() {
		byte b1 = (byte)(memory[pc++]);
		byte b2 = (byte)(memory[pc++]);
		byte b3 = (byte)(memory[pc++]);
		byte b4 = (byte)(memory[pc++]);
		mval.ival = (int)((b4 & 0xff) << 24 | (b3 & 0xff) << 16 | (b2 & 0xff) << 8 | b1 & 0xff);
		mval.pc = pc;
		return mval;
	}
	
	public MVal fetch8() {
		byte b1 = (byte)(memory[pc++]);
		byte b2 = (byte)(memory[pc++]);
		byte b3 = (byte)(memory[pc++]);
		byte b4 = (byte)(memory[pc++]);		
		byte b5 = (byte)(memory[pc++]);
		byte b6 = (byte)(memory[pc++]);
		byte b7 = (byte)(memory[pc++]);
		byte b8 = (byte)(memory[pc++]);		
		mval.lval =  (long)((b8 & 0xff) << 56 | (b7 & 0xff) << 48 | (b6 & 0xff) << 40 | (b5 & 0xff) << 32 | 
				(b4 & 0xff) << 24 | (b3 & 0xff) << 16 | (b2 & 0xff) << 8 | b1 & 0xff);
		mval.pc = pc;
		return mval;		
	}
	
	public void writeByte(int offset, byte data) {
		memory[offset] = data;
	}
	public void writeInt(int offset, int data) {
		memory[offset++] = (byte)(data & 0xff);
		memory[offset++] = (byte)((data >> 8) & 0xff);
		memory[offset++] = (byte)((data >> 16) & 0xff);
		memory[offset++] = (byte)((data >> 24) & 0xff);
	}
	
	public byte readByte(int offset) {
		return memory[offset];
	}
	public short readShort(int offset) {
		byte b1 = (byte)(memory[offset++] & 0xff);
		return (short)((memory[offset] & 0xff) << 8 | b1 & 0xff);
	}
	public int readInt(int offset) {
		byte b1 = (byte)(memory[offset++]);
		byte b2 = (byte)(memory[offset++]);
		byte b3 = (byte)(memory[offset++]);
		byte b4 = (byte)(memory[offset++]);
		return (int)((b4 & 0xff) << 24 | (b3 & 0xff) << 16 | (b2 & 0xff) << 8 | b1 & 0xff);
	}
	public long readLong(int offset) {
		byte b1 = (byte)(memory[offset++]);
		byte b2 = (byte)(memory[offset++]);
		byte b3 = (byte)(memory[offset++]);
		byte b4 = (byte)(memory[offset++]);		
		byte b5 = (byte)(memory[offset++]);
		byte b6 = (byte)(memory[offset++]);
		byte b7 = (byte)(memory[offset++]);
		byte b8 = (byte)(memory[offset++]);
		/*
		System.out.printf("b1 = %x\n", b1);
		System.out.printf("b2 = %x\n", b2);
		System.out.printf("b3 = %x\n", b3);
		System.out.printf("b4 = %x\n", b4);
		System.out.printf("b5 = %x\n", b5);
		System.out.printf("b6 = %x\n", b6);
		System.out.printf("b7 = %x\n", b7);
		System.out.printf("b8 = %x\n", b8);
		*/
		int v1 = (b8 & 0xff) << 24 | (b7 & 0xff) << 16 | (b6 & 0xff) << 8 | b5 & 0xff;
		int v2 = (b4 & 0xff) << 24 | (b3 & 0xff) << 16 | (b2 & 0xff) << 8 | b1 & 0xff;
		return (long)(((long)v1 << 32) | v2);
		
		
		//return (long)((b8 & 0xff) << 56 | (b7 & 0xff) << 48 | (b6 & 0xff) << 40 | (b5 & 0xff) << 32 | 
					//(b4 & 0xff) << 24 | (b3 & 0xff) << 16 | (b2 & 0xff) << 8 | b1 & 0xff);		
	}
	
	
	
	public int savePc() {
		remain = false;
		return prevPc = pc;
	}
	
	public int getCurrentPc() {
		return pc;
	}
	
	public void setPc(int pc) {
		this.pc = pc;
	}
	
	public int getPrevPc() {
		return prevPc;
	}
	
	public void dump() {
		for (int i = 0; i < end; ++i) {
			if (i % 16 == 0) System.out.printf("\n%04x: ", i);
			System.out.printf("%02x ", memory[i]);		
		}
	}
	
	public int seekZero(int offset) {
		int pos = offset;
		for (;; ++pos) {
			if (memory[pos] == 0) {
				return pos;
			}
		}
	}
	
	public void dump(int offset, int len) {
		for (int i = offset; i < offset+len; ++i) {
			if (i % 16 == 0) System.out.printf("\n%04x: ", i);
			System.out.printf("%02x ", memory[i]);		
		}
		System.out.println();
	}
	
	
	public byte[] rawdump() {
		int count = 0;
		int limit = (pc - prevPc) <= 4 ? pc : prevPc+4;
		
		//for (int i = ti = prevPc; i < limit; ++i, ++count, ++ti) {
//			rawout.printf("%02x ", memory[i]);
		//}
		
		for(; prevPc < limit; ++prevPc, ++count) {
			rawout.printf("%02x ", memory[prevPc]);
		}
		remain = (prevPc != pc);
		
		for (int i = 0; i < 4-count; ++i) {
			rawout.printf("   ");
		}		
		
		byte[] rawdata = buf.toByteArray();
		buf.reset();
		return rawdata;		
	}
	
	public boolean remaining() {
		return remain;
	}
	
	public byte[] rawdump_rem() {
		int limit = (pc - prevPc) <= 4 ? pc : prevPc+4;
		
		for(; prevPc < limit; ++prevPc) {
			rawout.printf("%02x ", memory[prevPc]);
		}		
		remain = (prevPc != pc);		
		byte[] rawdata = buf.toByteArray();
		buf.reset();
		return rawdata;	
	}

}
