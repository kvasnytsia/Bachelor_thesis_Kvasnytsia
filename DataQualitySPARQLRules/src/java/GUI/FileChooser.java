package GUI;

import java.io.*;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;

import excel.ExcelComparatorAndCopying;
import query.Request;

public class FileChooser extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static private final String newline = "\n";
	JButton openButtonFile1, openButtonFile2, executeButton;
	JTextArea log;
	JFileChooser fc;
	ArrayList<File> fileList = new ArrayList<File>();
	// File file1;
	// File file2;
	String file2Path;

	public FileChooser() {
		super(new BorderLayout());

		// Create the log first, because the action listeners
		// need to refer to it.

		log = new JTextArea(20, 30);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(log);

		// Create a file chooser
		fc = new JFileChooser();

		// Create the open button.
		openButtonFile1 = new JButton("Select edited file");
		openButtonFile1.addActionListener(this);

		// Create the save button.
		openButtonFile2 = new JButton("Save as...");
		openButtonFile2.addActionListener(this);

		// Create the execute button.
		executeButton = new JButton("Execute");
		executeButton.addActionListener(this);

		// For layout purposes, put the buttons in a separate panel
		JPanel buttonPanel = new JPanel(); // use FlowLayout
		buttonPanel.add(openButtonFile1);
		buttonPanel.add(openButtonFile2);
		buttonPanel.add(executeButton);

		// Add the buttons and the log to this panel.
		add(buttonPanel, BorderLayout.PAGE_START);
		add(logScrollPane, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {

		// Handle open button action.
		if (e.getSource() == openButtonFile1) {
			int returnVal = fc.showOpenDialog(FileChooser.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// file1 = fc.getSelectedFile();
				fileList.add(fc.getSelectedFile());
				// This is where a real application would open the file.
				log.append("Opening: " + fileList.get(fileList.size() - 1).getName() + "." + newline);
			} else {
				log.append("Open command cancelled by user." + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());

			// Handle save button action.
		}

		if (e.getSource() == openButtonFile2) {
			int returnVal = fc.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file2Path = fc.getSelectedFile().getPath();
				// This is where a real application would save the file.
				log.append("Saving to: " + file2Path + "." + newline);
			} else {
				log.append("Open command cancelled by user." + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());
		}
		if (e.getSource() == executeButton) {
			// if (!(file1 == null) && !(file2==null)) {
			if (!(file2Path == null)) {

				try {
					Request request = new Request();
					request.duRequest(file2Path+".xlsx");
					File file2 = new File(file2Path+".xlsx");
					log.append("Writing data to " + file2.getName() + newline);
					if (!(fileList.isEmpty())) {
						for (File file : fileList) {
							new ExcelComparatorAndCopying(file, file2);
						}
					} 
					log.append("Writing is successful done" + newline);
					fileList.clear();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					log.append("Compilation error, check the content of your file or the connection to the server" + newline);
				}

			} else {
				log.append("Save directory wasn't chosen" + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event dispatch thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("CNT Excel Comparator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		// Add content to the window.
		frame.add(new FileChooser());

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();

			}
		});
	}
}