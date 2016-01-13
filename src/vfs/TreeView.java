package vfs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;

import vfs.tree.VNode;

public class TreeView extends JPanel implements TreeWillExpandListener, TreeSelectionListener {
	private FileSystemView fsView = FileSystemView.getFileSystemView();
	private JTree tree;
	
	private final MainView mainView;

	public TreeView(MainView mainView) {
		super(new BorderLayout());
		this.mainView = mainView;		
		add(new JScrollPane());
		setPreferredSize(new Dimension(300, 400));	
	}
	
	public void load(VNode rootNode) {
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(rootNode);
		for (VNode child : rootNode.getChildren()) {
			if (child.isDirectory()) {
				node.add(new DefaultMutableTreeNode(child));
			}
		}
		root.add(node);
		tree = new JTree(treeModel);
		tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		tree.setRootVisible(false);
		
		// add listener
		tree.addTreeSelectionListener(this);
		tree.addTreeWillExpandListener(this);
		tree.setCellRenderer(new CellRenderer(tree.getCellRenderer()));
		
		
		tree.expandRow(0);
		
		//setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(new JScrollPane(tree));
		setPreferredSize(new Dimension(300, 400));		
		
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		final JTree tree = (JTree) e.getSource();
	    final DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
	     	     
	    final VNode vnode = (VNode)node.getUserObject();
	    System.out.println("vnode = " + vnode.getAbsolutePath());
	    List<VNode> list = new ArrayList<VNode>();
	    for (VNode child : vnode.getChildren()) {
	    	list.add(child);	    	 
	    }
	    mainView.updateTable(list);
	}
	
	
	@Override
	public void treeWillExpand(TreeExpansionEvent event)
			throws ExpandVetoException {
		
		final JTree tree = (JTree) event.getSource();		
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
		final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		for (int i = 0; i < node.getChildCount(); ++i) {
			final DefaultMutableTreeNode newnode = (DefaultMutableTreeNode)node.getChildAt(i);
			final VNode vnode = (VNode) newnode.getUserObject();
			
			if (newnode.getChildCount() > 0)
				continue;
			
			SwingWorker<String, VNode> worker = new SwingWorker<String, VNode>() {				
				@Override
				public String doInBackground() {
					Collection<VNode> children = vnode.getChildren();
					for (VNode child : children) {
						if (child.isDirectory()) {
							publish(child);
						}
					}					
					return "done";
				}
				@Override
				protected void process(List<VNode> chunks) {
					for (VNode vnode : chunks) {
						newnode.add(new DefaultMutableTreeNode(vnode));
					}
					model.nodeStructureChanged(newnode);
				}
			};
			worker.execute();
			
		}
	}
	
	@Override
	public void treeWillCollapse(TreeExpansionEvent event)
			throws ExpandVetoException {
	}
	
	private class CellRenderer extends DefaultTreeCellRenderer {
		private final TreeCellRenderer renderer;
		private final File dummyFile = new File("/");
		
		public CellRenderer(TreeCellRenderer renderer) {
			super();
			this.renderer = renderer;
		}
		@Override 
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			JLabel c = (JLabel) renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
	        if (isSelected) {
	            c.setOpaque(false);
	            c.setForeground(getTextSelectionColor());
	            //c.setBackground(Color.BLUE); //getBackgroundSelectionColor());
	        } else {
	            c.setOpaque(true);
	            c.setForeground(getTextNonSelectionColor());
	            c.setBackground(getBackgroundNonSelectionColor());
	        }
	        if (value instanceof DefaultMutableTreeNode) {
	            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;	            
	            Object o = node.getUserObject();	            
	            if (o instanceof VNode) {            	
	                VNode vfile = (VNode) o;
	                c.setIcon(fsView.getSystemIcon(dummyFile));
	                c.setText(vfile.getName());
	            }
	        }
	        return c;		
		}		
	}
}

