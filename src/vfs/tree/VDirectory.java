package vfs.tree;

import java.util.Collection;

public class VDirectory extends VNode {
	
	public VDirectory() {
		super();
	}
	public VDirectory(String name) {
		super(name);
	}
	
	@Override
	public boolean isDirectory() {
		return true;
	}
	
	@Override
	public VNode getChild(String name) {
		return children.get(name);
	}
	@Override
	public void show(String pinfo) {
		Collection<VNode> values = children.values();		
		if (values.isEmpty()) {
			System.out.println(pinfo.substring(1, pinfo.length()) + "/" + name + "/");
		} else {
			for (VNode child : values) {
				child.show(pinfo + "/" + name);
			}
		}
	}
}
