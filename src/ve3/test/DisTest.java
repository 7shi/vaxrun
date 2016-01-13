package ve3.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import ve3.hdw.ConcrateMemory;
import ve3.hdw.Memory;

public class DisTest {
	
	private byte[] rawdata;
	
	public static void main(String[] args) throws Exception {
		new DisTest().doit();
	}
	
	public void doit() throws Exception {
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream("echo"));
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		int count;
		byte[] buffer = new byte[1024];
		while ((count = bin.read(buffer, 0, buffer.length)) != -1) {
			System.out.println("count = " + count);
			bout.write(buffer, 0, count);
		}
		
		rawdata = bout.toByteArray();
		bout.close();
		bin.close();
		System.out.println("size = " + rawdata.length);
		
		for (int i = 0; i < 0x20; ++i) {
			if (i % 16 == 0) System.out.println("");
			System.out.printf("%02x ", rawdata[i]);
		}
		System.out.println("");
		
		int tsize = readInt(4);
		System.out.printf("textsize = 0x%x\n", tsize);
		
		Memory mem = new ConcrateMemory(0xffff);
		mem.load(rawdata, 0x20, tsize);
		mem.dump();
	}
	
	private int readInt(int offset) {
		return rawdata[offset] & 0xff | (rawdata[offset+1] & 0xff) << 8
				| (rawdata[offset+2] & 0xff) << 16 | (rawdata[offset+3] & 0xff) << 24;
	}
	
	

}
