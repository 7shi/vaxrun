package unixv7;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

public class BlockDevice {
public static final int BLOCK_SIZE = 512;
	
	private byte[] data;
	private int p;
	
	private byte[] dummy = {(byte)0xff, (byte)0xff};
	
	public BlockDevice() {		
	}
	
	public BlockDevice(String path) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(path));
		byte buf[] = new byte[1024];
		int count = 0;
		while ((count = bin.read(buf, 0, buf.length)) != -1) {
			bout.write(buf, 0, count);
		}		
		data = bout.toByteArray();		
	}
	
	public void setIndex(int p) {
		this.p = p;
	}
	
	public int readInt() {
		return (int)(data[p++] & 0xff | ((data[p++] & 0xff) << 8)) | 
				((data[p++] & 0xff) << 16) | ((data[p++] & 0xff) << 24);
	}
	
	public short readShort() {
		return (short)(data[p++] & 0xff | (data[p++] & 0xff) << 8);
	}
	
	public int readUShort() {
		return (int)(data[p++] & 0xff | (data[p++] & 0xff) << 8);		
	}
	
	public char readChar() {
		return (char)data[p++];
	}
	
	public int seekZero() {
		while (data[p++] != 0);
		return --p;
	}
	
	public byte[] getBytes(int begin, int end) {
		byte buf[] = new byte[end - begin];
		/*
		System.out.println("buf.length = " + buf.length);
		System.out.printf("end = %x\n", end);
		System.out.printf("begin = %x\n", begin);
		*/
		System.arraycopy(data, begin, buf, 0, buf.length);		
		return buf;
	}
	

}
