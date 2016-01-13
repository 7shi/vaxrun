package ve3.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import ve3.os.FSystem;



public class FileTest {
	
	public static void main(String ...args) {
		try {
			new FileTest().dotest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void dotest() throws Exception {
		
		RandomAccessFile rfile = new RandomAccessFile("arg.c", "r");
		rfile.seek(10);
		byte[] buf = new byte[10];
		rfile.read(buf, 0, buf.length);
		
		for (int i = 0; i < buf.length; ++i) {
			System.out.printf("%c", buf[i]);
		}
		
		rfile.read(buf, 0, buf.length);
		for (int i = 0; i < buf.length; ++i) {
			System.out.printf("%c", buf[i]);
		}
		
		System.in.close();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		//System.out.print(">");		
		//reader.readLine(); // exception
		
		for (int i = 0; i < 5; ++i) {
			int fnum = FSystem.open("arg.c", 0);
			System.out.println("fd = " + fnum);
		}
		
		System.out.println("close 3 and 6");
		FSystem.close(3);
		FSystem.close(6);
		
		for (int i = 0; i < 5; ++i) {
			int fnum = FSystem.open("arg.c", 0);
			System.out.println("fd = " + fnum);
		}
		
		int r = FSystem.close(18);
		//System.out.println(r);
		
		int fd = FSystem.creat("test", 0666);
		System.out.println("fd = " + fd);

	}

}
