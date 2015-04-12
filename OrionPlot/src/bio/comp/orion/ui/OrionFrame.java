package bio.comp.orion.ui;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import bio.comp.orion.model.DataLine;
import bio.comp.orion.model.MatrixReader;
import bio.comp.orion.model.MatrixReaders;
import bio.comp.orion.model.OrionModel;
import bio.comp.orion.model.Preference;
import bio.comp.orion.model.SubCellFalseColorCoder;

public class OrionFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7094780974562172798L;
	private JPanel contentPane;
	private OrionPlotPanel orionPlotPanel = new OrionPlotPanel();
	private JFrame colorEditFrame;
	private final Action action = new OpenFileAction();
	private final Action imgAction = new SaveAsImageAction();
	private final Action svgAction = new SaveAsSVGAction();
	private final Action quitAction = new QuitAction();
	private final Action colorEditorAction = new ShowColorEditorAction();
	private static Preferences prefs = Preferences.userRoot();
	private ColorIndexTableModel colorAssignmentGridModel;
	private SubCellFalseColorCoder colorCoder;
	private final Logger dlog = Logger.getAnonymousLogger();

	/**
	 * Create the frame.
	 */
	@SuppressWarnings("serial")
	public OrionFrame() {
		dlog.setLevel(Level.WARNING);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1440, 900);

		JMenuBar menuBar = new JMenuBar(){{
			add(new JMenu("File"){{
				add(new JMenuItem("Open..."){{
					setAction(action);
				}});
				add(new JMenuItem("Export Image..."){{
					setAction(imgAction);
				}});
				add(new JMenuItem("Export SVG..."){{
					setAction(svgAction);
				}});
				add(new JMenuItem("Quit"){{
					setAction(quitAction);
				}});
			}});
			add(new JMenu("Window"){{
				add(new JMenuItem("Color Editor"){{
					setAction(colorEditorAction);
				}});
			}});
		}};
		setJMenuBar(menuBar);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JButton button = new JButton("Choose...");
		button.setAction(action);
		JSVGScrollPane scroller = new JSVGScrollPane(orionPlotPanel);



		JProgressBar progressBar = new JProgressBar();
		final JTable colorAssignmentGrid = new JTable();
		colorAssignmentGridModel = ColorIndexTableModel.createWithColors(SubCellFalseColorCoder.DEFAULT_CODING);
		colorAssignmentGridModel.addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent arg0) {
				// TODO Auto-generated method stub
				orionPlotPanel.reload();
				
			}
		});
		colorAssignmentGrid.setModel(colorAssignmentGridModel);
		colorAssignmentGrid.setDefaultRenderer(Color.class, colorAssignmentGridModel.createColorCellRenderer(colorAssignmentGrid.getDefaultRenderer(Color.class)));	
		JScrollPane colorAssignmentTable = new JScrollPane(colorAssignmentGrid);
		final OrionPlotPanel panel = orionPlotPanel;
		
		colorCoder = new SubCellFalseColorCoder() {
			
			@Override
			public Color colorForSubCell(int row, int cell, int subCell,
					int subCellValue) {
				// TODO Auto-generated method stub
				Color modelValue = colorAssignmentGridModel.colorForValue(subCellValue);

				return (modelValue != null) ? modelValue : Color.black;

			}

			@Override
			public int[] codesForValues() {
				// TODO Auto-generated method stub
				return colorAssignmentGridModel.allIndexedValues();
			}
		};
		
		colorEditFrame = new JFrame("Color Editor");
		JPanel colorEditPanel = new JPanel(){{
			final JPanel _colorEditThis=this;
			setLayout(new BorderLayout());
			final JColorChooser colorChooser = new JColorChooser(){{
				
			}};
			colorAssignmentGrid.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				// TODO Auto-generated method stub
				int sel = colorAssignmentGrid.getSelectedRow();
				
				Color clr = (Color) colorAssignmentGridModel.getValueAt(sel, ColorIndexConstants.COLOR_TABLE_COLUMN);
				colorChooser.setColor(clr);
				
			}
		});

		colorAssignmentGrid.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getClickCount() == 2 && !OrionFrame.this.colorEditFrame.isVisible()){
					OrionFrame.this.colorEditFrame.setLocationByPlatform(true);
					OrionFrame.this.colorEditFrame.setVisible(true);
				}
				super.mouseClicked(e);
			}
			
		});
			colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent change) {
					// TODO Auto-generated method stub
					String desc = String.format("ColorSelect change event %s", change);
					if(change.getSource() instanceof ColorSelectionModel){
                        ColorSelectionModel model = (ColorSelectionModel) change.getSource();
                        Color selection = model.getSelectedColor();
                        dlog.fine(String.format("%s : selected %s\n", desc, selection));
                        int selectedRow = colorAssignmentGrid.getSelectedRow();
                        if(selectedRow >= 0){
                        	colorAssignmentGridModel.setValueAt(selection, selectedRow, ColorIndexConstants.COLOR_TABLE_COLUMN);
                        }
					}
					dlog.fine(desc);
				}
			});
			add(colorChooser, BorderLayout.CENTER);
			add(new JPanel(){{
				JPanel buttonPanel = this;
				BoxLayout buttonLayout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
				setLayout(buttonLayout);
				add(Box.createHorizontalGlue());
				JButton doneButton = new JButton("Done");
				add(doneButton);
				doneButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						colorEditFrame.setVisible(false);

					}
				});

			}}, BorderLayout.SOUTH);
		}
		};
		colorEditFrame.getContentPane().add(colorEditPanel);
		colorEditFrame.pack();
		colorEditFrame.setLocationRelativeTo(this);
		colorEditFrame.setAlwaysOnTop(true);

		

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
				gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_contentPane.createSequentialGroup()
										.addComponent(scroller, GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
												.addComponent(colorAssignmentTable, GroupLayout.PREFERRED_SIZE, 100, Short.MAX_VALUE))
										)

										.addGroup(gl_contentPane.createSequentialGroup()
												.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(button)))
												.addContainerGap())
				);
		gl_contentPane.setVerticalGroup(
				gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, true)
								.addComponent(scroller, GroupLayout.PREFERRED_SIZE, 195, Short.MAX_VALUE)
								.addComponent(colorAssignmentTable, GroupLayout.PREFERRED_SIZE, 150, Short.MAX_VALUE)
								)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(progressBar, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(button, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addContainerGap())
				);
		contentPane.setLayout(gl_contentPane);
	}




	static private boolean writeFileWithModel(File file, OrionModel model){
		BufferedWriter bw = null;
		if(file == null){
			return false;
		}
		
		try {
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			for(DataLine line : model){
				bw.write(line.toFileString());
				bw.write('\n');
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}


	private class SaveFileAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4367471755331837279L;

		public SaveFileAction() {
			putValue(NAME, "Save...");
			putValue(SHORT_DESCRIPTION, "Save the contents of a plot to a plot file");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if(true)
				throw new NotImplementedException();
			String saveFolderParent = Preference.SAVE_FOLDER.getPreference(prefs, String.class);
			JFileChooser jfc = new JFileChooser(saveFolderParent);
			jfc.setSelectedFile(new File("plot.csv"));
			int result = jfc.showOpenDialog(OrionFrame.this);
			if(result == JFileChooser.APPROVE_OPTION){
				File selectedFile =  jfc.getSelectedFile();
				saveFolderParent = selectedFile.getParent();
				Preference.PREVIOUS_SESSION_PLOT.setPreference(prefs, selectedFile.getAbsolutePath());
				Preference.SAVE_FOLDER.setPreference(prefs, saveFolderParent);
				Preference.flush(prefs);
				if(writeFileWithModel(selectedFile, orionPlotPanel.getModel())){
					OrionFrame.this.setTitle(selectedFile.getAbsolutePath());
				}
			}

		}
	}
	private class SaveAsSVGAction extends AbstractAction{
		public SaveAsSVGAction() {
			putValue(NAME, "Save SVG...");
			putValue(SHORT_DESCRIPTION, "Save the contents of a plot as in SVG");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if(orionPlotPanel.getDocument() == null){
				return;
			}
			String saveSVGParent= Preference.IMG_FOLDER.getPreference(prefs, String.class);
			JFileChooser jfc = new JFileChooser(saveSVGParent);
			jfc.setSelectedFile(new File("plot.svg"));
			int result = jfc.showSaveDialog(OrionFrame.this);
			if(result == JFileChooser.APPROVE_OPTION){
				// retrieve image
				File outputfile = jfc.getSelectedFile();
				saveSVGParent = outputfile.getParent();
				Preference.IMG_FOLDER.setPreference(prefs, saveSVGParent);
				Preference.flush(prefs);
				try {
					SVGTranscoder tcoder = new SVGTranscoder();
					tcoder.transcode(
							new TranscoderInput(orionPlotPanel.getDocument()), 
							new TranscoderOutput(new FileWriter(outputfile)));
				} catch (TranscoderException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	private class SaveAsImageAction extends AbstractAction{
		public SaveAsImageAction() {
			putValue(NAME, "Save Image...");
			putValue(SHORT_DESCRIPTION, "Save the contents of a plot as an image");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			String saveImageParent = Preference.IMG_FOLDER.getPreference(prefs, String.class);
			JFileChooser jfc = new JFileChooser(saveImageParent);
			jfc.setSelectedFile(new File("image.png"));
			int result = jfc.showSaveDialog(OrionFrame.this);
			if(result == JFileChooser.APPROVE_OPTION){
				// retrieve image
				BufferedImage image = new BufferedImage(orionPlotPanel.getWidth(), orionPlotPanel.getHeight(),
						BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics2D = image.createGraphics();
				orionPlotPanel.print(graphics2D);
				File outputfile = jfc.getSelectedFile();
				saveImageParent = outputfile.getParent();
				Preference.IMG_FOLDER.setPreference(prefs, saveImageParent);
				Preference.flush(prefs);
				try {
					ImageIO.write(image, "png", outputfile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	public void openFile(File file){
		String startingFolder = file.getParent();
		Preference.OPEN_FOLDER.setPreference(prefs, startingFolder);
		Preference.PREVIOUS_SESSION_PLOT.setPreference(prefs, file.getAbsolutePath());
		MatrixReader reader = MatrixReaders.readerForFile(file);
		OrionModel fileModel = reader.getModel();
		SubCellFalseColorCoder fileCoder = fileModel.getColorCoder();
		colorAssignmentGridModel.updateColorIndexesWithCoder(fileCoder);
		fileModel.setColorCoder(colorCoder);
		orionPlotPanel.setModel(fileModel);
		this.setTitle(file.getAbsolutePath());
		Set<Integer> uniqs = new HashSet<Integer>();
		for(DataLine dl : orionPlotPanel.getModel()){
			for(int i = 0; i < dl.getLength(); ++i){
				List<Integer> dlv = dl.getValuesAt(i);
				uniqs.addAll(dlv);
			}
		}
		for(Integer uv : uniqs){
			colorAssignmentGridModel.addColorIndexIfAbsent(uv, Color.black);
		}
		orionPlotPanel.repaint();
	}

	private class OpenFileAction extends AbstractAction {
		public OpenFileAction() {
			putValue(NAME, "Open...");
			putValue(SHORT_DESCRIPTION, "Opens a plot file");
		}
		public void actionPerformed(ActionEvent e) {
			String startingFolder = prefs.get(Preference.OPEN_FOLDER.getKey(), Preference.SAVE_FOLDER.getPreference(prefs, String.class)); 
			System.err.format("Getting starting folder preference %s\n", startingFolder);
			JFileChooser jfc = new JFileChooser(startingFolder);
			int result = jfc.showOpenDialog(OrionFrame.this);
			if(result == JFileChooser.APPROVE_OPTION){
				File file = jfc.getSelectedFile();
				if(file.exists()){
					try {
						System.err.format("Saving prefs %s canonical %s parent %s\n", file.getAbsolutePath(), file.getCanonicalPath(), file.getParent());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					openFile(file);
				}
			}

		}
	}

	private class ShowColorEditorAction extends AbstractAction{
		public ShowColorEditorAction(){
			putValue(NAME, "Edit Colors...");
			putValue(SHORT_DESCRIPTION, "Modify colors used in the plot");
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			colorEditFrame.setVisible(true);
		}

	}

	private class QuitAction extends AbstractAction{
		public QuitAction(){
			putValue(NAME, "Quit");
			putValue(SHORT_DESCRIPTION, "Exits this application");
		}
		public void actionPerformed(ActionEvent e){
			Preference.OPEN_PREVIOUS_SESSION_ON_START.setPreference(prefs, Boolean.TRUE);
			Preference.flush(prefs);
			System.exit(0);
		}
	}
}
