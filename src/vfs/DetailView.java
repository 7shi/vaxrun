package vfs;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import unixv7.V7Driver;
import vfs.tree.VNode;

public class DetailView extends JPanel {
	
	private final MainView mainView;
	private final JTextArea inodeArea;
	private final JTextArea dataArea;
	
	public DetailView(MainView mainView) {
		this.mainView = mainView;
		
		setLayout(new GridLayout(2, 1));
		inodeArea = new JTextArea("inode information is shown here...");
		inodeArea.setEditable(false);
		inodeArea.setPreferredSize(new Dimension(500, 400));
		
		dataArea = new JTextArea("data is shown here as text...");
		dataArea.setEditable(false);
		dataArea.setPreferredSize(new Dimension(500, 400));
		
		add(new JScrollPane(inodeArea));
		add(new JScrollPane(dataArea));		
	}
	
	public void inodeChanged(VNode target, V7Driver driver) {
		if (driver != null) {
			String inodeInfo = driver.getInodeInfo(target.getAbsolutePath());
			//inodeArea.setText(inodeInfo);
			updateInode(inodeInfo);
		}		
	}
	
	public void updateInode(String text) {
		inodeArea.setText(text);
	}
	
	public void dataChanged(VNode target, V7Driver driver) {
		if (driver != null && !target.isDirectory()) {
			byte[] rawdata = driver.getData(target.getAbsolutePath());
			//System.out.println(new String(rawdata));
			dataArea.setText(new String(rawdata));
		}
	}

}
