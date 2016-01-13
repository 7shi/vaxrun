package unixv7;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Inode {
	
	public static final int INODE_SIZE = 64;
	public static final int DIRINFO_SIZE = 16;
	
	public int di_mode; //ushort
	public short di_nlink;
	public short di_uid;
	public short di_gid;
	public int di_size;
	public byte[] di_addr = new byte[40];
	public int di_atime;
	public int di_mtime;
	public int di_ctime;
	
	public boolean isDirectory;
	public boolean isRegular;
	
	private Map<String, Integer> f_table;
	
	public static List<Inode> parse(BlockDevice bd, int b_size) {
		bd.setIndex(2 * BlockDevice.BLOCK_SIZE);
		List<Inode> inodes = new ArrayList<Inode>();
		inodes.add(new Inode()); // dummy
		
		for (int i = 0; i < (BlockDevice.BLOCK_SIZE * (b_size-2) / INODE_SIZE); ++i) {		
		//for (int i = 0; i < 2; ++i) {						
			Inode inode = new Inode();
			inode.di_mode = bd.readUShort();
			inode.di_nlink = bd.readShort();
			inode.di_uid = bd.readShort();
			inode.di_gid = bd.readShort();
			inode.di_size = bd.readInt();
			for (int j = 0; j < inode.di_addr.length; ++j) {
				inode.di_addr[j] = (byte)(bd.readChar() & 0xff);
			}
			inode.di_atime = bd.readInt();
			inode.di_mtime = bd.readInt();
			inode.di_ctime = bd.readInt();			
			inode.isDirectory = ((inode.di_mode >> 12) & 0xf) == 4;
			inode.isRegular = ((inode.di_mode >> 12) & 0xf) == 8;
			inodes.add(inode);
		}		
		return inodes;
	}
	
	public void createIndex(BlockDevice bd) {
		f_table = new HashMap<String, Integer>();
		boolean remaining = true;
		for (int i = 0; i < di_addr.length-1; i += 3) {
			int offset = (di_addr[i] & 0xff | (di_addr[i+1] & 0xff) << 8 | (di_addr[i+2] & 0xff) << 16) * BlockDevice.BLOCK_SIZE;
			//System.out.printf("offset = 0x%x\n", offset);
			
			for (int j = 0; j < (BlockDevice.BLOCK_SIZE / Inode.DIRINFO_SIZE); ++j) { // j < 32
				if (j >= ((di_size - ((i/3) *BlockDevice.BLOCK_SIZE)) / Inode.DIRINFO_SIZE)) {
					remaining = false;
					break;
				}
				int p = j * Inode.DIRINFO_SIZE + offset;
				bd.setIndex(p);
				int fi_num = bd.readUShort();
				p += 2;
				int end = bd.seekZero();
				String s = new String(bd.getBytes(p, end));
				//System.out.printf("%04x: %s\n", fi_num, s);
				f_table.put(s,  fi_num);
			}			
			if (!remaining) break;
		}
	}
	
	public String createInfo(int num) {
		StringWriter writer = new StringWriter();
		writer.write("inode  : "+ num);
		writer.write("\n");
		writer.write("mode   : ");
		for (int i = 2; i >= 0; --i) {
			writer.write(Integer.toString((di_mode >> (i*3)) & 7));
		}
		if (isDirectory) writer.write(", IDIR");
		if (isRegular) writer.write(", Regular");
		writer.write("\n");
		writer.write("offset : 0x" + Integer.toHexString((num - 1) * 0x40 + 0x400));
		writer.write("\n");
		writer.write("nlink  : " + di_nlink);
		writer.write("\n");
		writer.write("uid    : " + di_uid);
		writer.write("\n");
		writer.write("gid    : " + di_gid);
		writer.write("\n");
		writer.write("size   : " + di_size);
		writer.write("\n");
		writer.write("addr   : ");		
		for (int i = 0; i < di_addr.length-1; i += 3) {
			int offset = (di_addr[i] & 0xff | (di_addr[i+1] & 0xff) << 8 | (di_addr[i+2] & 0xff) << 16);			
			if (i != 0) writer.write(",");
			writer.write(Integer.toString(offset));
		}
		writer.write("\n");
		writer.write("atime  : " + new Date(di_atime)); // I don't know it is correct or not
		writer.write("\n");
		writer.write("mtime  : " + new Date(di_mtime));
		writer.write("\n");
		writer.write("ctime  : " + new Date(di_ctime));
		
		return writer.toString();
	}
	
	public void showTable() {
		System.out.print("\nBlocks: ");
		for (int i = 0; i < di_addr.length-1; i += 3) {
			int offset = (di_addr[i] & 0xff | (di_addr[i+1] & 0xff) << 8 | (di_addr[i+2] & 0xff) << 16) * BlockDevice.BLOCK_SIZE;
			if (i * BlockDevice.BLOCK_SIZE > di_size) break;
			System.out.printf("0x%x ", offset);
		}
		System.out.println();
		for (Map.Entry<String, Integer> entry : f_table.entrySet()) {
			System.out.printf("%s : %d\n", entry.getKey(), entry.getValue());
		}
	}
	
	public Map<String, Integer> getTable() {
		return f_table;
	}
	
	public int getTargetInode(String path) {
		return f_table.containsKey(path) ? f_table.get(path) : -1;
	}
	
	
	public void show() {
		System.out.printf("di_mode = %04x\n", (short)di_mode);
		System.out.printf("di_nlink = %04x\n", di_nlink);
		System.out.printf("di_uid = %04x\n", di_uid);
		System.out.printf("di_gid = %04x\n", di_gid);
		System.out.printf("di_size = %08x\n", di_size);
		System.out.println("di_addr");
		for (int i = 0; i < di_addr.length; ++i) {
			if (i % 16 == 0) System.out.println();
			System.out.printf("%02x ", di_addr[i]);
		}
		System.out.println();
		System.out.printf("di_atime = %08x\n", di_atime);
		System.out.printf("di_mtime = %08x\n", di_mtime);
		System.out.printf("di_ctime = %08x\n", di_ctime);
		System.out.println();
	}
	
	
	private List<Integer> createBlockTable(BlockDevice bd, int index, int level) {
		List<Integer> table = new ArrayList<Integer>();
		if (level == 0) {
			table.add(index);
			return table;
		}
		bd.setIndex(index * BlockDevice.BLOCK_SIZE);
		for (int i = 0; i < 128; ++i) {
			table.add(bd.readInt());			
		}		
		if (level > 1) {
			for (int i = 0; i < 128; ++i) {
				int addr = table.remove(0);
				table.addAll(createBlockTable(bd, (short)addr, level-1));
			}
		}
		return table;
	}
	
	
	
	public byte[] extract(BlockDevice bd) {
		byte[] buf = new byte[di_size];
		int p = 0;
		int remainsize = di_size;
		
		for (int i = 0; i < di_addr.length-1; i += 3) {
			int offset = (di_addr[i] & 0xff | (di_addr[i+1] & 0xff) << 8 | (di_addr[i+2] & 0xff) << 16);
			//System.out.printf("%d = %x\n", i/3, offset);
			int level = (i / 3 < 10) ? 0 : ((i / 3) % 10 + 1);
			//System.out.printf("i = %d, level = %d\n", i, level);
			List<Integer> table = createBlockTable(bd, offset, level);
			while (!table.isEmpty()) {
				int offset2 = table.remove(0) & 0xffffffff; // maybe 32bit need to use long instead int
				//System.out.printf("offset2 = %x\n", offset2);
				offset2 *= BlockDevice.BLOCK_SIZE;
				if (remainsize < BlockDevice.BLOCK_SIZE) {
					byte[] tmp = bd.getBytes(offset2,  offset2+remainsize);
					System.arraycopy(tmp, 0, buf, p, tmp.length);
					p += tmp.length;
					remainsize = 0;
					break;
				} else {
					byte[] tmp = bd.getBytes(offset2, offset2+BlockDevice.BLOCK_SIZE);
					System.arraycopy(tmp, 0, buf, p, tmp.length);
					p += tmp.length;
					remainsize -= BlockDevice.BLOCK_SIZE;
				}
			}
			if (remainsize == 0) break;			
		}
		return buf;
	}
	
	@Deprecated
	private byte[] extractDirect(BlockDevice bd) {
		byte[] buf = new byte[di_size];
		int p = 0;
		int remainsize = di_size;
		for (int i = 0; i < di_addr.length; i += 3) {
			int offset = (di_addr[i] & 0xff | (di_addr[i+1] & 0xff) << 8 | (di_addr[i+2] & 0xff) << 16) * BlockDevice.BLOCK_SIZE;
			if (remainsize < BlockDevice.BLOCK_SIZE) {
				byte[] tmp = bd.getBytes(offset, offset+remainsize);
				System.arraycopy(tmp, 0, buf, p, tmp.length);
				p += tmp.length;
				break;
			} else {
				byte[] tmp = bd.getBytes(offset,  offset+BlockDevice.BLOCK_SIZE);
				System.arraycopy(tmp, 0, buf, p, tmp.length);
				p += tmp.length;
				remainsize -= BlockDevice.BLOCK_SIZE;
			}			
		}			
		return buf;
	}

}
