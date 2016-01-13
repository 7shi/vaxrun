package vfs.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class VNode {
	protected String name;
	protected Map<String, VNode> children;
	protected VNode parent;
	
	public VNode() {
		children = new HashMap<String, VNode>();
	}
	
	public VNode(String name) {
		this();
		this.name = name;
	}
	
	public void addChild(VNode child) {
		children.put(child.getName(), child);
	}
	
	public boolean hasChild(String name) {
		return children.containsKey(name);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public void setParent(VNode parent) {
		this.parent = parent;
	}
	
	public Collection<VNode> getChildren() {
		return children.values();
	}
	
	public String getAbsolutePath() {
		return (parent != null) ? parent.getAbsolutePath() + "/" + name : "";
	}
	
	
	public abstract VNode getChild(String name);
	public abstract boolean isDirectory();
	public abstract void show(String pinfo); // only check
}
