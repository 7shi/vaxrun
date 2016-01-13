package vfs.tree;

public class VFile extends VNode {
	
	public VFile() {
		super();
	}
	public VFile(String name) {
		super(name);
	}

	@Override
	public boolean isDirectory() {
		return false;
	}
	
	@Override
	public void show(String pinfo) {
		System.out.println(pinfo.substring(1,  pinfo.length()) + "/" + name);
	}
	@Override
	public VNode getChild(String name) {
		throw new RuntimeException();
	}

}
