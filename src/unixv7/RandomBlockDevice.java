package unixv7;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomBlockDevice extends BlockDevice {
	
	private RandomAccessFile rfile;
	private int p;
	
	public RandomBlockDevice(String path) throws Exception {
		rfile = new RandomAccessFile(path, "r");
	}
	
	@Override
	public void setIndex(int p) {
		try {
			rfile.seek(this.p = p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public char readChar() {
		try {
			++p;
			return (char)rfile.readByte();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	@Override
	public int readInt() {
		try {
			p += 4;
			return (int)(rfile.readByte() & 0xff | (rfile.readByte() & 0xff) << 8 |
					(rfile.readByte() & 0xff) << 16 | (rfile.readByte() & 0xff) << 24);						
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	@Override
	public short readShort() {
		try {
			p += 2;
			return (short)(rfile.readByte() & 0xff | (rfile.readByte() & 0xff) << 8);
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	@Override
	public int readUShort() {
		try {
			p += 2;
			return (int)(rfile.readByte() & 0xff | (rfile.readByte() & 0xff) << 8);
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	@Override
	public int seekZero() {
		//System.out.printf("seekbegin: %x\n", p);
		try {
			for (; rfile.readByte() != 0; ++p);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.printf("seekend  : %x\n", p);
		return p;
	}
	
	@Override
	public byte[] getBytes(int begin, int end) {
//		System.out.printf("begin:0x%x, end:%x\n", begin, end);
		
		byte[] buf = new byte[end - begin];
		try {
			rfile.seek(this.p = begin);
			rfile.read(buf, 0, buf.length);
		} catch (IOException e) {
			e.printStackTrace();			
		}
		
		return buf; 
	}
	
	
	
	
	
	
	
	

}
