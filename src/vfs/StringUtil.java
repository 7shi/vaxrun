package vfs;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StringUtil {
	
	public static String translate(byte[] data) {
		if (data[0] == 0x8 && data[1] == 0x1) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(bout);
			for (int i = 0; i < data.length; ++i) {
				if (i % 16 == 0) {					
					ps.println();
					ps.printf("%08x: ", i);
				}
				ps.printf("%02x ", data[i]);
			}
			return new String(bout.toByteArray());
		} else {
			return new String(data);
		}
		
		
	}

}
