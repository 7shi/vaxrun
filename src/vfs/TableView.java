package vfs;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import unixv7.V7Driver;
import vfs.tree.VNode;

public class TableView extends JPanel {
	private final MainView mainView;
	
	private final String[][] tabledata = {};
	private final String[] columnNames = {"Type", "Name", "Size"};
	private final JTable table;
	
	private List<VNode> currentList;
	
	public void updateModel(List<VNode> newnodes, V7Driver driver) {
		currentList = newnodes;
		DefaultTableModel newModel = new DefaultTableModel();
		 newModel.setColumnIdentifiers(columnNames);
		 for (VNode node : newnodes) {
			 String[] data = new String[3];
			 data[0] = (node.isDirectory()) ? "dir" : "";
			 data[1] = node.getName();			 
			 System.out.println(node.getAbsolutePath());
			 if (driver != null) {
				 data[2] = Integer.toString(driver.getSize(node.getAbsolutePath()));				 
			 } else {
				 data[2] = "1000";	 
			 }
			 
			 newModel.addRow(data);
		 }
		 table.setModel(newModel);
	}
	
	public TableView(MainView mainView) {
		this.mainView = mainView;
		DefaultTableModel tableModel = new DefaultTableModel(tabledata, columnNames);
		table = new JTable();
		table.setModel(tableModel);
		table.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
			    int row = table.getSelectedRow();
//			    int col = table.getSelectedColumn();			 
			    VNode targetNode = currentList.get(row);
			    System.out.println("target = " + targetNode.getAbsolutePath());
			    System.out.println("isDir = " + targetNode.isDirectory());
			    mainView.updateDetail(targetNode);
			}
		});
		JScrollPane sp = new JScrollPane(table);
		sp.setPreferredSize(new Dimension(400, 800));
		add(sp);		
	}

	



}
