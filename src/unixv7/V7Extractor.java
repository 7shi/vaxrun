package unixv7;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class V7Extractor {
	
	private BlockDevice bd;
	private List<Inode> inodes;
	private List<String> allFiles;
	
	public static void main(String[] args) {
		String targetName = "";
		String disk = "";
		boolean all = false;
		switch(args.length) {
		case 0: {
			//targetName = "/test/a.out";
			//targetName = "/test/result"; // blow 5012 byte
			//targetName = "/test/result2";  
			//targetName = "/fboot";  
			//targetName = "/unix";
			all = true;
			disk = "rp06.disk";
			break;
		}
		case 1: {
			disk = "rp06.disk";
			all = true;
			targetName = args[0];
			break;
		}
		case 2: {
			disk = args[0];
			targetName = args[1];
			break;
		}
		default: {
			System.exit(1);
		}
		}
		
		V7Extractor v7e = new V7Extractor();
		v7e.process(disk);
		if (all) {
			v7e.extractAll();
		} else {
			v7e.extract(targetName, true);
		}
				
	}
	
	public void process(String disk) {
		try {
			//bd = new BlockDevice(disk); // read all data on memory
			bd = new RandomBlockDevice(disk); // use random access file 
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		FilSys filsys = FilSys.parse(bd);
		inodes = Inode.parse(bd, filsys.s_isize);
		int dummy = 0;
		for (int i = 0; i < inodes.size(); ++i) {
			Inode inode = inodes.get(i);
			if (inode.isDirectory) {
				//if (++dummy == 1) {
					//System.out.printf("offset = 0x%x\n", i * 0x40 + 0x400);
					inode.createIndex(bd);
					//inodes.get(i).show();
					//inode.showTable();					
					//break;
				//}
			}
		}		
	}
	
	private void extract(Inode node, String pname) {		
		if (node.isDirectory) {
			Map<String, Integer> f_table = node.getTable();
			//System.out.printf("%s : %d\n", pname, f_table.size());
			int validCount = 0;
			for (Map.Entry<String, Integer> entry : f_table.entrySet()) {
				String name = entry.getKey();
				int num = entry.getValue();
				if (num > 2 && !name.equals(".") && !name.equals("..")) {
					validCount++;
					extract(inodes.get(num), pname + "/" + name);										
				}				
			}
			if (validCount == 0) {
				allFiles.add(pname + "/");
			}
		} else {
			if (node.isRegular) {
				allFiles.add(pname);
			}
		}
	}
	
	public void extractAll() {
		allFiles = new ArrayList<String>();
		Inode root = inodes.get(2);
		/*
		Map<String, Integer> f_table = root.getTable();
		for (Map.Entry<String, Integer> entry : f_table.entrySet()) {
			String name = entry.getKey();
			int num = entry.getValue();
			if (num > 2 && !name.equals(".") && !name.equals("..")) {
				System.out.printf("%s, %d\n", name, num);
			}
		}		
		System.out.println("-----------");		
		*/

		extract(root, "");		
		for (String s : allFiles) {
			extract(s, true);
		}		
	}
	
	public String getValidFiles() {
		allFiles = new ArrayList<String>();
		Inode root = inodes.get(2);
		extract(root, "");
		StringWriter writer = new StringWriter();
		for (String s : allFiles) {
			writer.write(s);
			writer.write("\n");;
		}
		return writer.toString();
	}
	
	private int getTargetInode(String path) {
		System.out.println("try to find " + path);
		Inode parent = inodes.get(2); // root
		Inode target = null;
		String[] paths = path.substring(1, path.length()).split("/");
		int inode_num = 0;
		for (String str : paths) {
			int num = parent.getTargetInode(str);
			if (num == -1) {
				System.out.println("cannot find inode for " + str);
				System.exit(1);
			}
			target = inodes.get(num);
			inode_num = num;
			if (target.isDirectory) {
				parent = target;
			} 
		}
		
		return inode_num;
	}
	
	public int getSize(String path) {
		Inode targetNode = inodes.get(getTargetInode(path));		
		return targetNode.di_size;
	}
	public String getInodeInfo(String path) {		
		int inode_num = getTargetInode(path);
		return inodes.get(inode_num).createInfo(inode_num);		
	}
	
	/*
	public String getInodeInfo(String path) {
		System.out.println("try to find " + path);
		Inode parent = inodes.get(2); // root
		Inode target = null;
		String[] paths = path.substring(1, path.length()).split("/");
		int inode_num = 0;
		for (String str : paths) {
			int num = parent.getTargetInode(str);
			if (num == -1) {
				System.out.println("cannot find inode for " + str);
				System.exit(1);
			}
			target = inodes.get(num);
			inode_num = num;
			if (target.isDirectory) {
				parent = target;
			} 
		}			
		
		System.out.println("inode_num = " + inode_num);
		System.out.printf("inode offset = 0x%x\n", (inode_num - 1) * 0x40 + 0x400);
		target.show();
		//System.out.println(target.createInfo(inode_num));
		
		
		return target.createInfo(inode_num);
	}
	*/
	
	public byte[] extract(String path, boolean outflg) {
		System.out.println("try to find " + path);
		if (path.endsWith("/")) return null;
		
		
		Inode parent = inodes.get(2); // root node
		Inode target = null;
		int inode_num = 0;
		String[] paths = path.substring(1, path.length()).split("/");
		for (String str : paths) {
			int num = parent.getTargetInode(str);
			if (num == -1) {
				System.out.println("cannot find inode for " + str);
				System.exit(1);
			}
			target = inodes.get(num);
			if (target.isDirectory) {
				parent = target;
			} else {
				//System.out.println("num = " + num);
				inode_num = num;
			}
		}		
		// offset of the target i_node
		//System.out.printf("inode offset = 0x%x\n", (inode_num - 1) * 0x40 + 0x400);
		//System.out.println("size = " + target.di_size);
		//target.show();
		
		//target.extract(bd);
		
		//To extract
		byte[] data = target.extract(bd);
		if (!outflg) {
			return data;
		}
		
		System.out.println("data length = " + data.length);
		String distDir = "output";
		int pos = path.substring(1,  path.length()).lastIndexOf("/");
		if (pos != -1) {
			String dirName = distDir + path.substring(0, pos+1);
			File dir = new File(dirName);
			if (!dir.exists()) {
				System.out.println("mkdir.. " + dirName);
				dir.mkdirs();
			}
		}
				
		try {
			FileOutputStream fout = new FileOutputStream(distDir + path);
			fout.write(data, 0, data.length);
			fout.flush();
			fout.close();
			System.out.println("Write data to > " + distDir + path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return data;
		
	}
}
