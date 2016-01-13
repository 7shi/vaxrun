package unixv6;

import java.io.FileOutputStream;
import java.util.List;

public class V6Extractor {
	
	private BlockDevice bd;
	private List<Inode> inodes;
	

	public static void main(String[] args) {
		V6Extractor v6e = new V6Extractor();
		v6e.process();
		//v6e.extract("/hoge.txt");
		//v6e.extract("/test/hoge.txt");
		//v6e.extract("/test/hello");
		//v6e.extract("/test/hello2.c");
		//v6e.extract("/test/a.out");
		v6e.extract("/test/result");
	}
	
	public void process() {
		try {
			bd = new BlockDevice("v6root");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		FilSys filsys = FilSys.prase(bd);
		//filsys.show();
		inodes = Inode.parse(bd, filsys.s_isize);
		//System.out.println(inodes.size());
		
		
		int dummy = 0;
		for (int i = 1; i < inodes.size(); ++i) {
			Inode inode = inodes.get(i);
			if (inode.isDirectory) {				
				//if (++dummy == 3) {
					System.out.printf("\ninode[%d] file size = %d\n", i, inode.size);				
					inode.createIndex(bd);
					inode.showTable();
					//break;
				//}
				
			}			
		}
		
		/*
		System.out.println("---------------");
		System.out.printf("offset = 0x%x\n", 362*32 + 0x400);
		inodes.get(362).show();
		System.out.println("\n---------------");
		System.out.printf("offset = 0x%x\n", 355*32 + 0x400);
		inodes.get(356).show();
		System.out.println("\n---------------");
		*/
	}
	
	public void extract(String path) {
		System.out.println("try to find " + path);		
		
		
		
		Inode parent = inodes.get(1);		
		Inode target = null;
		String[] paths = path.substring(1, path.length()).split("/");
		for (String str : paths) {
			int num = parent.getTargetInode(str);
			if (num == -1) {
				System.out.println("cannot find inode for " + str);
				System.exit(1);;
			}			
			target = inodes.get(num);
			System.out.println("num = " + num);
			if (target.isDirectory) {
				parent = target;
			}
		}
		
		if (target != null) {
			System.out.println("isDirectory = " + target.isDirectory);
			System.out.println("isLarge = " + target.isLarge);
			System.out.println("size = " + target.size);
			target.show();
		} else {
			System.out.println("inode not found");
		}
		
		//target.extract(bd);
		
		byte[] data = target.extract(bd);
		
		try {
			FileOutputStream fout = new FileOutputStream(paths[paths.length-1]);
			fout.write(data, 0, data.length);
			fout.flush();
			fout.close();
			System.out.println("Write data to > " + paths[paths.length-1]);
		} catch (Exception e) {
			e.printStackTrace();
		}				
		
		/*
		for (int i = 0; i < data.length; ++i) {
			if (i % 16 == 0) System.out.println();
			System.out.printf("%02x ", data[i]);
		}
		*/
		
		
		

		
		/*
		Inode root = inodes.get(1);
		int n_num = root.getTargetInode(path.substring(1, path.length()));
		System.out.println("n_num= " + n_num);
		if (n_num == -1) System.exit(1);
		
		Inode targetNode = inodes.get(n_num);
		System.out.println("isDirectory = " + targetNode.isDirectory);
		System.out.println("isLarge = " + targetNode.isLarge);
		System.out.println("size = " + targetNode.size);
		
		byte[] data = targetNode.extract(bd);
		
		for (int i = 0; i < data.length; ++i) {
			if (i % 16 == 0) System.out.println();
			System.out.printf("%02x ", data[i]);
		}
		*/
		
		
		
	}
}
