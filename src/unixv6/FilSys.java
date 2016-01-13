package unixv6;

public class FilSys {
	public int s_isize;
	public int s_fsize;
	public int s_nfree;
	public int s_free[] = new int[100];
	public int s_ninode;
	public int s_inode[] = new int[100];
	public char s_flock;
	public char s_ilock;
	public char s_fmod;
	public char s_ronly;
	public int s_time[] = new int[2];
	public int pad[] = new int[50];
	
	public static FilSys prase(BlockDevice bd) {
		bd.setIndex(1 * BlockDevice.BLOCK_SIZE);
		FilSys filsys = new FilSys();
		filsys.s_isize = bd.readInt();
		filsys.s_fsize = bd.readInt();
		filsys.s_nfree = bd.readInt();
		for (int i = 0; i < filsys.s_free.length; ++i) {
			filsys.s_free[i] = bd.readInt();
		}
		filsys.s_ninode = bd.readInt();
		for (int i = 0; i < filsys.s_inode.length; ++i) {
			filsys.s_inode[i] = bd.readInt();
		}
		filsys.s_flock = bd.readChar();
		filsys.s_ilock = bd.readChar();
		filsys.s_fmod = bd.readChar();
		filsys.s_ronly = bd.readChar();
		for (int i = 0; i < filsys.s_time.length; ++i) {
			filsys.s_time[i] = bd.readInt();
		}
		for (int i = 0; i < filsys.pad.length; ++i) {
			filsys.pad[i] = bd.readInt();
		}
		return filsys;
	}
	
	public void show() {
		System.out.printf("s_isize = %04x\n", (short)s_isize);
		System.out.printf("s_fsize = %04x\n", (short)s_fsize);
		System.out.printf("s_nfree = %04x\n", (short)s_nfree);
		System.out.print("s_free");
		for (int i = 0; i < 100; ++i) {
			if (i % 8 == 0) System.out.println();
			System.out.printf("%04x ", (short)s_free[i]);
		}
		System.out.println();
		System.out.printf("s_ninode = %04x\n", (short)s_ninode);
		
		System.out.print("s_inode");
		for (int i = 0; i < 100; ++i) {
			if (i % 8 == 0) System.out.println();
			System.out.printf("%04x ", (short)s_inode[i]);
		}
		System.out.println();
		System.out.printf("s_flock = %02x\n", (byte)s_flock);
		System.out.printf("s_ilock = %02x\n", (byte)s_ilock);
		System.out.printf("s_fmod = %02x\n", (byte)s_fmod);
		System.out.printf("s_ronly = %02x\n", (byte)s_ronly);
		
		System.out.print("s_time");
		for (int i = 0; i < 2; ++i) {
			if (i % 8 == 0) System.out.println();
			System.out.printf("%04x ", (short)s_time[i]);
		}
		System.out.print("pad");
		for (int i = 0; i < 48; ++i) {
			if (i % 8 == 0) System.out.println();
			System.out.printf("%04x ", (short)pad[i]);
		}
	}
	
	

}
