package vfs;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class FileChooseView extends JPanel implements ActionListener {
	
	private final MainView mainView;
		
	public FileChooseView(MainView mainView) {		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		this.mainView = mainView;	
		JButton button = new JButton("File");
		button.addActionListener(this);
		add(button);
		
		JButton extButton = new JButton("Extract");
		extButton.addActionListener((ActionEvent e)-> {
			mainView.serialize();
		});
		add(extButton);
		
		JButton disasButton = new JButton("Disasm");
		disasButton.addActionListener((ActionEvent e) -> {
			mainView.disassm();
		});
		add(disasButton);
	}	
	public void actionPerformed(ActionEvent event) {
		
		JFileChooser filechooser = new JFileChooser();
	    int selected = filechooser.showOpenDialog(this);
	    if (selected == JFileChooser.APPROVE_OPTION){
	      File file = filechooser.getSelectedFile();
	      //System.out.println(file.getAbsolutePath());
	      mainView.loadImage(file.getAbsolutePath());
	    }		
	}
}
