package bio.comp.orion;

import java.awt.EventQueue;
import java.io.File;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import bio.comp.orion.model.Preference;
import bio.comp.orion.ui.OrionFrame;

public class OrionLauncher {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Properties sysprops = System.getProperties();
		String osName = sysprops.getProperty("os.name");
		boolean isAMac = (osName != null && osName.startsWith("Mac")) ? true : false;
		if(isAMac){
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Orion");
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedLookAndFeelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Preferences prefs = Preferences.userRoot();
					OrionFrame frame = new OrionFrame();
					frame.setVisible(true);
					if(Preference.OPEN_PREVIOUS_SESSION_ON_START.getPreference(prefs, Boolean.class).booleanValue()){
						String previousSessionFile = Preference.PREVIOUS_SESSION_PLOT.getPreference(prefs, String.class);
						if(previousSessionFile != null){
							File file = new File(previousSessionFile);
              if(file != null){
                frame.openFile(file);
              }

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
