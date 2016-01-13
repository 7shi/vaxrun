package vfs;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import unixv7.V7Driver;
import ve3.disassm.V32Disassm;
import vfs.tree.VNode;
import vfs.tree.VNodeCreator;

public class MainView {
	
	private JFrame mainFrame;
	private VNode rootNode;
	private TreeView treeView;
	private TableView tableView;
	//private DetailView detailView;
	private DetailSplitView detailView;
	private FileChooseView fileView;
	
	private V7Driver driver;
	private String diskPath;
	
	private byte[] currentData;
	private String currentPath;
	
	private String baseDir = "output";
	
	public static void main(String ...args) {
		MainView main = null;
		switch(args.length) {
		case 1: {
			main = new MainView(args[0]);
		}
		default: {
			main = new MainView("rp06.disk");
			break;
		}
		}
		
		
		main.init();
		main.process();
	}
	
	public MainView(String diskPath) {
		this.diskPath = diskPath;
	}
	
	private void initTreeView() {
		//rootNode = VNodeCreator.getInstance().createFromImage(driver);		
		treeView = new TreeView(this);
		//treeView.load(rootNode);
	}
	private void initTableView() {
		tableView = new TableView(this);
	}
	private void initDetailView() {
		//detailView = new DetailView(this);
		detailView = new DetailSplitView(this);
	}
	
	private void initDriver() {
		driver = new V7Driver(diskPath);
	}
	
	private void initFileView() {
		fileView = new FileChooseView(this);
	}
	
	public void init() {
		initFileView();
		//initDriver();
		initTableView();
		initDetailView();
		initTreeView();
	}
	
	public void process() {
		mainFrame = new JFrame("VFSViewer");
		mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		mainFrame.getContentPane().setLayout(new BorderLayout());
		mainFrame.getContentPane().add(fileView, BorderLayout.NORTH);		
		mainFrame.getContentPane().add(treeView, BorderLayout.WEST);
		mainFrame.getContentPane().add(tableView, BorderLayout.CENTER);
		mainFrame.getContentPane().add(detailView, BorderLayout.EAST);
		mainFrame.pack();
		mainFrame.setSize(1200, 800);		
		
		mainFrame.setVisible(true);
	}
	
	public void loadImage(String path) {
		this.diskPath = path;
		initDriver();
		rootNode = VNodeCreator.getInstance().createFromImage(driver);
		mainFrame.getContentPane().remove(treeView);
		treeView = new TreeView(this);		
		treeView.load(rootNode);
		mainFrame.getContentPane().add(treeView, BorderLayout.WEST);
		mainFrame.pack();
	}
	
	public void updateTable(List<VNode> newnodes) {
		tableView.updateModel(newnodes, driver);
		setCurrent(null, null);
	}
	
	public void updateDetail(VNode tnode) {
		detailView.inodeChanged(tnode, driver);
		detailView.dataChanged(tnode, driver);
		//String inodeInfo = driver.getInodeInfo(tnode.getAbsolutePath());
		//System.out.println(inodeInfo);
	}
		
	public void setCurrent(byte[] data, String path) {
		this.currentData = data;
		this.currentPath = path;
	}
	
	public void serialize() {
		if ((currentData != null) && (currentPath != null)) {
			System.out.println(currentPath);
			int pos = currentPath.substring(1,  currentPath.length()).lastIndexOf("/");
			if (pos != -1) {
				String dirName = baseDir + currentPath.substring(0, pos+1);
				File dir = new File(dirName);
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
			
			try {
				FileOutputStream fout = new FileOutputStream(baseDir + currentPath);
				fout.write(currentData, 0, currentData.length);
				fout.flush();
				fout.close();				
			} catch (Exception e) {
				e.printStackTrace();
			}									
		}
	}
	
	public void disassm() {
		if ((currentData != null) && (currentPath != null)) {
			System.out.println("disassm");
			V32Disassm disassm = new V32Disassm(currentData);
			String log = disassm.disassm();
			if (log != null) {
				detailView.updateInode(log);
			}
			
		}
	}

}
