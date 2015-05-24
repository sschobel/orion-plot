package bio.comp.orion.ui;

import static bio.comp.orion.OrionEvents.makeEventRecord;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
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

import bio.comp.orion.OrionConstants;
import bio.comp.orion.OrionEvents;
import bio.comp.orion.model.DataLine;
import bio.comp.orion.model.MatrixReader;
import bio.comp.orion.model.MatrixReaders;
import bio.comp.orion.model.OrionModel;
import bio.comp.orion.model.Preference;
import bio.comp.orion.model.SubCellFalseColorCoder;
import bio.comp.orion.ui.ColorIndexTableModel.ColorModelEvent;
import bio.comp.orion.ui.ColorIndexTableModel.ColorModelListener;
import bio.comp.orion.ui.ColorIndexTableModel.ReplacementStrategy;

import com.google.common.base.MoreObjects;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class OrionFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7094780974562172798L;
	private JPanel contentPane;
	private OrionPlotPanel orionPlotPanel = new OrionPlotPanel();
	private JDialog colorEditFrame;
	private File currentFile = null;
	private final Action openFileAction = new OpenFileAction();
	private final Action reloadFileAction = new ReloadFileAction();
	private final Action imgAction = new SaveAsImageAction();
	private final Action svgAction = new SaveAsSVGAction();
	private final Action quitAction = new QuitAction();
	private final Action colorEditorAction = new ShowColorEditorAction();
	private static Preferences prefs = Preferences.userRoot();
	private ColorIndexTableModel colorAssignmentGridModel;
	private final Logger dlog = Logger.getAnonymousLogger();
	private final JProgressBar progressBar = new OrionStatusBar();
	private final JMenu windowMenu = new JMenu("Window");

	/**
	 * Create the frame.
	 */
	public OrionFrame() {
		dlog.setLevel(Level.WARNING);
		OrionConstants.STATUS_EVENT_BUS.get().register(progressBar);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1440, 900);

		JMenuBar menuBar = new JMenuBar() {
			{
				add(new JMenu("File") {
					{
						setMnemonic(KeyEvent.VK_F);
						add(new JMenuItem("Open...") {
							{
								setAction(openFileAction);
								setMnemonic(KeyEvent.VK_O);
								setAccelerator(KeyStroke.getKeyStroke(
										KeyEvent.VK_O,
										InputEvent.META_DOWN_MASK));
							}
						});
						add(new JMenuItem("save"){
							{
								setMnemonic(KeyEvent.VK_S);
							}
						});
						add(new JMenuItem("Reload") {
							{
								setAction(reloadFileAction);
								setMnemonic(KeyEvent.VK_R);
								setAccelerator(KeyStroke.getKeyStroke(
										KeyEvent.VK_R,
										InputEvent.META_DOWN_MASK));
							}
						});
						add(new JMenu("Export") {
							{
								setMnemonic(KeyEvent.VK_E);
								add(new JMenuItem("Export Image...") {
									{
										setAction(OrionFrame.this.imgAction);
										setName("Image");
										setMnemonic(KeyEvent.VK_I);
										setAccelerator(KeyStroke.getKeyStroke(
												KeyEvent.VK_E,
												InputEvent.META_DOWN_MASK));
									}
								});
								add(new JMenuItem("Export SVG...") {
									{
										setAction(svgAction);
										setName("SVG...");
										setMnemonic(KeyEvent.VK_S);
										setAccelerator(KeyStroke
												.getKeyStroke(
														KeyEvent.VK_E,
														InputEvent.META_DOWN_MASK
																| InputEvent.SHIFT_DOWN_MASK));
									}
								});

							}
						});
						add(new JMenuItem("Quit") {
							{
								setAction(quitAction);
								setAccelerator(KeyStroke.getKeyStroke(
										KeyEvent.VK_Q,
										InputEvent.META_DOWN_MASK));
							}
						});
					}
				});
				{
					JMenu wm = OrionFrame.this.windowMenu;
					wm.setMnemonic(KeyEvent.VK_W);
					wm.add(new JMenuItem("Plot") {
						{
							setAction(new AbstractOrionAction() {
								{
									putValue(NAME, "Plot");
									putValue(SHORT_DESCRIPTION, "activates the main plot window");
								}
								@Override
								protected void respondToAction(ActionEvent ae) {
									OrionFrame.this.setVisible(true);
									OrionFrame.this.requestFocus();
								}
							});
							setMnemonic(KeyEvent.VK_P);
							setAccelerator(KeyStroke.getKeyStroke(
									KeyEvent.VK_Q, InputEvent.META_DOWN_MASK));
						}

					});
					wm.add(new JMenuItem("Color Editor") {
						{
							setAction(colorEditorAction);
							setMnemonic(KeyEvent.VK_C);
							setAccelerator(KeyStroke.getKeyStroke(
									KeyEvent.VK_C, InputEvent.META_DOWN_MASK
											| InputEvent.SHIFT_DOWN_MASK));
						}
					});
					add(wm);
				}
			};
		};
		setJMenuBar(menuBar);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JButton button = new JButton("Choose...");
		button.setAction(openFileAction);
		JSVGScrollPane scroller = new JSVGScrollPane(orionPlotPanel);

		final JTable colorAssignmentGrid = new JTable();
		colorAssignmentGridModel = new ColorIndexTableModel();
		colorAssignmentGridModel
				.addTableModelListener(new TableModelListener() {

					@Override
					public void tableChanged(TableModelEvent tme) {
						orionPlotPanel.reload();

					}
				});
		colorAssignmentGrid.setModel(colorAssignmentGridModel);
		colorAssignmentGrid.setDefaultRenderer(Color.class,
				colorAssignmentGridModel
						.createColorCellRenderer(colorAssignmentGrid
								.getDefaultRenderer(Color.class)));
		JScrollPane colorAssignmentTable = new JScrollPane(colorAssignmentGrid);

		colorEditFrame = new JDialog(this,"Color Editor");
		JPanel colorEditPanel = new JPanel() {
			{
				setLayout(new BorderLayout());
				final JColorChooser colorChooser = new JColorChooser() {
					{

					}
				};
				colorAssignmentGrid.getSelectionModel()
						.addListSelectionListener(new ListSelectionListener() {

							@Override
							public void valueChanged(ListSelectionEvent arg0) {

								int sel = colorAssignmentGrid.getSelectedRow();
								Color clr = (Color) colorAssignmentGridModel
										.getValueAt(
												sel,
												ColorIndexConstants.COLOR_TABLE_COLUMN);
								colorChooser.setColor(clr);

							}
						});

				colorAssignmentGrid.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2
								&& !OrionFrame.this.colorEditFrame.isVisible()) {
							OrionFrame.this.colorEditFrame
									.setLocationByPlatform(true);
							OrionFrame.this.colorEditFrame.setVisible(true);
						}
						super.mouseClicked(e);
					}

				});
				colorChooser.getSelectionModel().addChangeListener(
						new ChangeListener() {

							@Override
							public void stateChanged(ChangeEvent change) {

								String desc = String.format(
										"ColorSelect change event %s", change);
								if (change.getSource() instanceof ColorSelectionModel) {
									ColorSelectionModel model = (ColorSelectionModel) change
											.getSource();
									Color selection = model.getSelectedColor();
									dlog.fine(String.format(
											"%s : selected %s\n", desc,
											selection));
									int selectedRow = colorAssignmentGrid
											.getSelectedRow();
									if (selectedRow >= 0) {
										colorAssignmentGridModel
												.setValueAt(
														selection,
														selectedRow,
														ColorIndexConstants.COLOR_TABLE_COLUMN);

									}
								}
								dlog.fine(desc);
							}
						});
				add(colorChooser, BorderLayout.CENTER);
				add(new JPanel() {
					{
						JPanel buttonPanel = this;
						BoxLayout buttonLayout = new BoxLayout(buttonPanel,
								BoxLayout.LINE_AXIS);
						setLayout(buttonLayout);
						add(Box.createHorizontalGlue());
						JButton doneButton = new JButton("Done");
						add(doneButton);
						doneButton.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {

								colorEditFrame.setVisible(false);

							}
						});

					}
				}, BorderLayout.SOUTH);
			}
		};
		colorEditFrame.getContentPane().add(colorEditPanel);
		colorEditFrame.pack();
		colorEditFrame.setAlwaysOnTop(true);

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane
				.setHorizontalGroup(gl_contentPane
						.createParallelGroup(Alignment.TRAILING)
						.addGroup(
								gl_contentPane
										.createSequentialGroup()
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.TRAILING)
														.addGroup(
																gl_contentPane
																		.createSequentialGroup()
																		.addComponent(
																				scroller,
																				GroupLayout.DEFAULT_SIZE,
																				434,
																				Short.MAX_VALUE)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addGroup(
																				gl_contentPane
																						.createParallelGroup(
																								Alignment.TRAILING)
																						.addComponent(
																								colorAssignmentTable,
																								GroupLayout.PREFERRED_SIZE,
																								100,
																								Short.MAX_VALUE)))

														.addGroup(
																gl_contentPane
																		.createSequentialGroup()
																		.addComponent(
																				progressBar,
																				GroupLayout.DEFAULT_SIZE,
																				325,
																				Short.MAX_VALUE)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				button)))
										.addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(
				Alignment.TRAILING).addGroup(
				gl_contentPane
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								gl_contentPane
										.createParallelGroup(
												Alignment.TRAILING, true)
										.addComponent(scroller,
												GroupLayout.PREFERRED_SIZE,
												195, Short.MAX_VALUE)
										.addComponent(colorAssignmentTable,
												GroupLayout.PREFERRED_SIZE,
												150, Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(
								gl_contentPane
										.createParallelGroup(
												Alignment.TRAILING, false)
										.addComponent(progressBar,
												Alignment.LEADING,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(button,
												Alignment.LEADING,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE))
						.addContainerGap()));
		contentPane.setLayout(gl_contentPane);
	}

	static private boolean writeFileWithModel(File file, OrionModel model) {
		BufferedWriter bw = null;
		if (file == null) {
			return false;
		}

		try {
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			for (DataLine line : model) {
				bw.write(line.toFileString());
				bw.write('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
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

	private final class OrionStatusBar extends JProgressBar {
		private Map<String, Object> _lastEventMap;

		public Map<String, Object> getLastEventMap() {
			return _lastEventMap;
		}

		public OrionStatusBar() {
			super();
			addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					if (e.getClickCount() == 2) {
						OrionStatusBar.this.showLastEventInDialog();
					}
				}

			});
		}

		public void showLastEventInDialog() {
			Map<String, Object> eventMap = getLastEventMap();
			if (eventMap == null) {
				return;
			} else {
				OrionEvents.makeEventDialog(this, eventMap).setVisible(true);
			}
		}

		@Subscribe
		public void statusEventPosted(final Map<String, Object> eventMap) {
			final JProgressBar self = this;
			_lastEventMap = eventMap;

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					self.setStringPainted(true);
					self.setString(eventMap.get(
							OrionConstants.STATUS_EVENT_DESCRIPTION_KEY)
							.toString());
				}
			});
		}
	}

	private interface OrionAction {
	}

	private abstract class AbstractOrionAction extends AbstractAction implements
			OrionAction {

		private EventBus statusBus = OrionConstants.STATUS_EVENT_BUS.get();

		public AbstractOrionAction() {
			super();
		}

		abstract protected void respondToAction(ActionEvent arg0);

		@Override
		public void actionPerformed(ActionEvent arg0) {
			String actionName = MoreObjects.firstNonNull(getValue(NAME),
					this.getClass().getName()).toString();
			try {
				statusBus.post(makeEventRecord(actionName + " started."));
				respondToAction(arg0);
				statusBus.post(makeEventRecord(actionName + " complete."));
			} catch (Throwable e) {
				String desc = String.format("Action (%s) failed!", actionName);
				statusBus.post(makeEventRecord(desc, this, e));
			}
		}

	}
	protected void doSaveFile(){
			String saveFolderParent = Preference.SAVE_FOLDER.getPreference(
					prefs, String.class);
			JFileChooser jfc = new JFileChooser(saveFolderParent);
			jfc.setSelectedFile(new File("plot.csv"));
			int result = jfc.showOpenDialog(OrionFrame.this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = jfc.getSelectedFile();
				saveFolderParent = selectedFile.getParent();
				Preference.PREVIOUS_SESSION_PLOT.setPreference(prefs,
						selectedFile.getAbsolutePath());
				Preference.SAVE_FOLDER.setPreference(prefs, saveFolderParent);
				Preference.flush(prefs);
				if (writeFileWithModel(selectedFile, orionPlotPanel.getModel())) {
					OrionFrame.this.setTitle(selectedFile.getAbsolutePath());
				}
			}
	}

	@SuppressWarnings("unused")
	private class SaveFileAction extends AbstractOrionAction {
		

		public SaveFileAction() {
			putValue(NAME, "Save...");
			putValue(SHORT_DESCRIPTION,
					"Save the contents of a plot to a plot file");
		}

		@Override
		protected void respondToAction(ActionEvent arg0) {
				throw new UnsupportedOperationException(
						"saving to a plot file is not permitted");

		}
	}

	private class SaveAsSVGAction extends AbstractOrionAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4863281097460392446L;

		public SaveAsSVGAction() {
			putValue(NAME, "SVG...");
			putValue(SHORT_DESCRIPTION, "Save the contents of a plot as in SVG");
		}

		@Override
		protected void respondToAction(ActionEvent arg0) {

			if (orionPlotPanel.getDocument() == null) {
				return;
			}
			String saveSVGParent = Preference.IMG_FOLDER.getPreference(prefs,
					String.class);
			JFileChooser jfc = new JFileChooser(saveSVGParent);
			jfc.setSelectedFile(new File("plot.svg"));
			int result = jfc.showSaveDialog(OrionFrame.this);
			if (result == JFileChooser.APPROVE_OPTION) {
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

	private class SaveAsImageAction extends AbstractOrionAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2087972127204740192L;

		public SaveAsImageAction() {
			putValue(NAME, "Image...");
			putValue(SHORT_DESCRIPTION,
					"Save the contents of a plot as an image");
		}

		@Override
		protected void respondToAction(ActionEvent arg0) {

			String saveImageParent = Preference.IMG_FOLDER.getPreference(prefs,
					String.class);
			JFileChooser jfc = new JFileChooser(saveImageParent);
			jfc.setSelectedFile(new File("image.png"));
			int result = jfc.showSaveDialog(OrionFrame.this);
			if (result == JFileChooser.APPROVE_OPTION) {
				// retrieve image
				BufferedImage image = new BufferedImage(
						orionPlotPanel.getWidth(), orionPlotPanel.getHeight(),
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

	private void setCurrentFile(File file) {
		if (file != null && file.exists()) {
			currentFile = file;
			String startingFolder = file.getParent();
			Preference.OPEN_FOLDER.setPreference(prefs, startingFolder);
			Preference.PREVIOUS_SESSION_PLOT.setPreference(prefs,
					file.getAbsolutePath());
			this.setTitle(file.getAbsolutePath());
			reloadFileAction.setEnabled(true);
		}
	}

	private File getCurrentFile() {
		return currentFile;
	}

	public void openFile(File file) {
		setCurrentFile(file);
		MatrixReader reader = MatrixReaders.readerForFile(file);
		final OrionModel fileModel = reader.getModel();
		colorAssignmentGridModel.removeAllColorIndexes();
		colorAssignmentGridModel.addColorIndexes(fileModel.getColorMap(), ReplacementStrategy.REPLACE);
		//This anonymous inner class updates the color map in the OrionModel whenever the color model changes
		//This happens whenever a new color is selected from the user interface
		colorAssignmentGridModel.addColorModelListener(new ColorModelListener() {
			
			@Override
			public void colorModelChanged(ColorModelEvent cme) {
				Map<Integer, Color> map = fileModel.getColorMap();
				if(!map.containsKey(cme.getOldValue().getIndex())){
					dlog.log(Level.SEVERE, String.format("Color model mismatch index %s not found in orionModel %s", cme.getOldValue().getIndex(), fileModel));
					return;
				}
				if(cme.didColorChange()){
					Color newColor = cme.getNewValue().getColor();
					Integer idx = cme.getNewValue().getIndex();
					map.put(idx, newColor);
				}
				//This doesn't currently change as integer values are defined once (in the document file)
				//But if we DO change it, this will save much trouble later down the line
				if(cme.didIndexChange()){
					Integer oldIdx = cme.getOldValue().getIndex();
					Integer newIdx = cme.getNewValue().getIndex();
					Color clr = map.get(oldIdx);
					map.remove(oldIdx);
					map.put(newIdx, clr);
				}
				
			}
		});
		orionPlotPanel.setModel(fileModel);
	}

	private class ReloadFileAction extends AbstractOrionAction {
		public ReloadFileAction() {
			putValue(NAME, "Reload");
			putValue(SHORT_DESCRIPTION, "Reload this plot");
			setEnabled(false);
		}

		protected void respondToAction(ActionEvent e) {
			OrionFrame.this.openFile(OrionFrame.this.getCurrentFile());
		}
	}

	private class OpenFileAction extends AbstractOrionAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1588733846805195076L;

		public OpenFileAction() {
			putValue(NAME, "Open...");
			putValue(SHORT_DESCRIPTION, "Opens a plot file");
		}

		protected void respondToAction(ActionEvent e) {
			String startingFolder = prefs.get(Preference.OPEN_FOLDER.getKey(),
					Preference.SAVE_FOLDER.getPreference(prefs, String.class));
			System.err.format("Getting starting folder preference %s\n",
					startingFolder);
			JFileChooser jfc = new JFileChooser(startingFolder);
			int result = jfc.showOpenDialog(OrionFrame.this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = jfc.getSelectedFile();
				if (file.exists()) {
					try {
						System.err.format(
								"Saving prefs %s canonical %s parent %s\n",
								file.getAbsolutePath(),
								file.getCanonicalPath(), file.getParent());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					openFile(file);
				}
			}

		}
	}

	private class ShowColorEditorAction extends AbstractOrionAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6574490632860865353L;

		public ShowColorEditorAction() {
			putValue(NAME, "Edit Colors...");
			putValue(SHORT_DESCRIPTION, "Modify colors used in the plot");
		}

		@Override
		protected void respondToAction(ActionEvent arg0) {

			colorEditFrame.setLocationRelativeTo(OrionFrame.this);
			colorEditFrame.setVisible(true);
		}

	}

	private class QuitAction extends AbstractOrionAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8519611749851752846L;

		public QuitAction() {
			putValue(NAME, "Quit");
			putValue(SHORT_DESCRIPTION, "Exits this application");
		}

		protected void respondToAction(ActionEvent e) {
			Preference.OPEN_PREVIOUS_SESSION_ON_START.setPreference(prefs,
					Boolean.TRUE);
			Preference.flush(prefs);
			System.exit(0);
		}
	}
}
