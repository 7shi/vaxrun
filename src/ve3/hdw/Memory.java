package ve3.hdw;

public interface Memory {
	public byte[] getRawMemory();
	public int load(byte[] rawdata, int offset, int size); // for only initial text loading
	public int load(byte[] rawdata, int roffset, int moffset, int size); // for only initial memory loading
	public void dump();	
	public MVal fetch();
	public MVal fetch2();
	public MVal fetch4();
	public MVal fetch8();
	public int getCurrentPc();
	public int savePc();
	public void setPc(int pc);
	
	
	public void writeInt(int offset, int data);
	public void writeByte(int offset, byte data);
	public void rawWrite(byte[] rawdata, int roffset, int moffset, int size);
	public byte[] rawRead(int offset, int len);
	
	public byte readByte(int offset);
	public short readShort(int offset);
	public int readInt(int offset);
	public long readLong(int offset);
	
	public byte[] rawdump();
	public byte[] rawdump_rem();
	public boolean remaining();
	public int getPrevPc();
	
	public void dump(int offset, int len);
	public int seekZero(int offset);

}
