package vfs;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import unixv7.V7Driver;
import vfs.tree.VNode;

public class DetailSplitView extends JSplitPane {
	private final MainView mainView;
	private final JTextArea inodeArea;
	private final JTextArea dataArea;
	
	public DetailSplitView(MainView mainView) {
		super(JSplitPane.VERTICAL_SPLIT, false);
		this.mainView = mainView;
		
		JPanel upperPanel = new JPanel();
		inodeArea = new JTextArea("inode information is shown here...");
		inodeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		inodeArea.setEditable(false);
		inodeArea.setPreferredSize(new Dimension(500, 10000));
		JScrollPane iscroll = new JScrollPane(inodeArea);
		iscroll.setPreferredSize(new Dimension(500, 800));
		upperPanel.add(iscroll);
		upperPanel.setPreferredSize(new Dimension(500, 400));
		setLeftComponent(upperPanel);
		
		JPanel belowPanel = new JPanel();
		dataArea = new JTextArea("data is shown here as text...");
		dataArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		dataArea.setEditable(false);
		dataArea.setPreferredSize(new Dimension(500, 10000));
		JScrollPane dscroll = new JScrollPane(dataArea);
		dscroll.setPreferredSize(new Dimension(500, 800));
		belowPanel.add(dscroll);
		belowPanel.setPreferredSize(new Dimension(500, 400));
		setRightComponent(belowPanel);
		
		
		setDividerSize(5);
		setResizeWeight(0.5);
		setPreferredSize(new Dimension(500, 800));

	}
	
	public void inodeChanged(VNode target, V7Driver driver) {
		if (driver != null) {
			String inodeInfo = driver.getInodeInfo(target.getAbsolutePath());
			//inodeArea.setText(inodeInfo);
			updateInode(inodeInfo);
			//setDividerSize(5);
			//setResizeWeight(0.5);
		}		
	}
	
	public void dataChanged(VNode target, V7Driver driver) {
		if (driver != null && !target.isDirectory()) {
			byte[] rawdata = driver.getData(target.getAbsolutePath());
			//System.out.println(new String(rawdata));
			//dataArea.setText(new String(rawdata));
			dataArea.setText(StringUtil.translate(rawdata));
			mainView.setCurrent(rawdata, target.getAbsolutePath());
		} else {
			mainView.setCurrent(null, null);
		}
	}
	
	public void updateInode(String text) {
		inodeArea.setText(text);
	}

}
