package unixv7;

public class FilSys {

	public int s_isize;  //ushort
	public int s_fsize;
	public short s_nfree;
	public int[] s_free = new int[50];
	public short s_ninode;
	public int[] s_inode = new int[100]; // ushort
	public char s_flock;
	public char s_ilock;
	public char s_fmod;
	public char s_ronly;
	public int s_time;
	public int s_tfree;
	public int s_tinode; // ushort
	public short s_m;
	public short s_n;
	public byte[] s_fname = new byte[6];
	public byte[] s_fpack = new byte[6];
	
	public static FilSys parse(BlockDevice bd) {
		bd.setIndex(1 * BlockDevice.BLOCK_SIZE);
		FilSys filsys = new FilSys();
		filsys.s_isize = bd.readUShort();
		filsys.s_fsize = bd.readInt();
		filsys.s_nfree = bd.readShort();
		for (int i = 0; i < filsys.s_free.length; ++i) {
			filsys.s_free[i] = bd.readInt();
		}
		filsys.s_ninode = bd.readShort();
		for (int i = 0; i < filsys.s_inode.length; ++i) {
			filsys.s_inode[i] = bd.readUShort();
		}
		filsys.s_flock = bd.readChar();
		filsys.s_ilock = bd.readChar();
		filsys.s_fmod = bd.readChar();
		filsys.s_ronly = bd.readChar();
		filsys.s_time = bd.readInt();
		filsys.s_tfree = bd.readInt();
		filsys.s_tinode = bd.readUShort();
		filsys.s_m = bd.readShort();
		filsys.s_n = bd.readShort();
		for (int i = 0; i < filsys.s_fname.length; ++i) {
			filsys.s_fname[i] = (byte)(bd.readChar() & 0xff);
		}
		for (int i = 0; i < filsys.s_fpack.length; ++i) {
			filsys.s_fpack[i] = (byte)(bd.readChar() & 0xff);
		}
				
		return filsys;
	}
	
	public void show() {
		System.out.printf("s_isize = %04x\n", (short)s_isize);
		System.out.printf("s_fsize = %08x\n", s_fsize);
		System.out.printf("s_nfree = %04x\n", s_nfree);
		System.out.println("s_free");
		for (int i = 0; i < 50; ++i) {
			if (i % 4 == 0) System.out.println();
			System.out.printf("%08x ", s_free[i]);
		}
		
		System.out.printf("s_ninode = %04x\n", s_ninode);
		System.out.println("s_inode");
		for (int i = 0; i < 100; ++i) {
			if (i % 8 == 0) System.out.println();
			System.out.printf("%04x ", (short)s_inode[i]);
		}
		System.out.println();
		System.out.printf("s_flock = %02x\n", (byte)s_flock);
		System.out.printf("s_ilock = %02x\n", (byte)s_ilock);
		System.out.printf("s_fmod = %02x\n", (byte)s_fmod);
		System.out.printf("s_ronly = %02x\n", (byte)s_ronly);
		System.out.printf("s_time = %08x\n", s_time);
		System.out.printf("s_tfree = %08x\n", s_tfree);
		System.out.printf("s_tinode = %04x\n", (short)s_tinode);
		System.out.printf("s_m = %04x\n", s_m);
		System.out.printf("s_n = %04x\n", s_n);
		System.out.println("s_fname");
		for (int i = 0; i < 6; ++i) {
			System.out.printf("%02x ", s_fname[i]);
		}
		System.out.println("\ns_fpack");
		for (int i = 0; i < 6; ++i) {
			System.out.printf("%02x ", s_fpack[i]);
		}
	}
	
	
}
