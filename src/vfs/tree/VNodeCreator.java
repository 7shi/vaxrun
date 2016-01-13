package vfs.tree;




import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import unixv7.V7Driver;


public class VNodeCreator {
	
	private static VNodeCreator instance = new VNodeCreator();
	
	private VNodeCreator() {		
	}
	
	public static VNodeCreator getInstance() {
		return instance;
	}
	
	public VNode createTree() {
		return createFromFile(); // temporary
	}
		
	
	private void register(VNode node, String name) {
		int pos = name.indexOf("/");
		if (pos == -1) {                         // file
			VNode child = new VFile(name);
			child.setParent(node);
			node.addChild(child);
		} else if(pos == (name.length() - 1)) {  // end by directory
			VNode child = new VDirectory(name.substring(0, name.length()-1));
			child.setParent(node);
			node.addChild(child);
		} else {                                 // continue
			String dirName = name.substring(0, pos);
			VNode dir = null;
			if (node.hasChild(dirName)) {
				dir = node.getChild(dirName);
			} else {				
				dir = new VDirectory(name.substring(0, pos));
				dir.setParent(node);
			}			
			node.addChild(dir);					
			register(dir, name.substring(pos+1, name.length()));
		}
	}
	
	public VNode createFromImage(V7Driver driver) {
		VNode root = new VDirectory("/");
		try {
			BufferedReader bin = new BufferedReader(new StringReader(driver.getAllFiles()));
			String line = "";
			while ((line = bin.readLine()) != null) {
				register(root, line.substring(1, line.length()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}				
		return root;
	}
	
	private VNode createFromFile() {
		VNode root = new VDirectory("");
		try {
			BufferedReader bin = new BufferedReader(new InputStreamReader(new FileInputStream("input.txt")));			
			String line = "";
			while((line = bin.readLine()) != null) {
//				System.out.println(line);
				register(root, line.substring(1, line.length()));
			}
			root.show("");
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return root;
	}

}
