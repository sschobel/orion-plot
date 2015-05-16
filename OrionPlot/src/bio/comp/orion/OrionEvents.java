package bio.comp.orion;

import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public class OrionEvents implements OrionConstants {

	private OrionEvents(){
		
	}
	public static final Map<String, Object> makeEventRecord(String desc, Throwable thrown){
		return makeEventRecord(desc, null, thrown);
	}
	public static final Map<String, Object> makeEventRecord(String desc){
		return makeEventRecord(desc, null, null);
	}
	public static final Map<String, Object> makeEventRecord(String desc, Object source, Throwable thrown){
               Map<String, Object> eventmap = Maps.newHashMap();
               if(thrown != null){
                       eventmap.put(OrionConstants.STATUS_EVENT_EXCEPTION_KEY, thrown);
               }
               if(source != null){
                       eventmap.put(OrionConstants.STATUS_EVENT_SOURCE_KEY, source);
               }
               if(desc!= null){
                       eventmap.put(OrionConstants.STATUS_EVENT_DESCRIPTION_KEY, desc);	
               }
               return eventmap;
	}
	public static final String getEventDescription(Map<String, Object> eventMap){
		return eventMap.getOrDefault(STATUS_EVENT_DESCRIPTION_KEY, "No Description").toString();
	}
	public static final Throwable getEventException(Map<String, Object> eventMap){
		Object ex = eventMap.get(STATUS_EVENT_EXCEPTION_KEY);
		return (ex != null && ex instanceof Throwable) ? (Throwable)ex : null;

	}
	public static final Object getEventSource(Map<String, Object> eventMap){
		 return eventMap.get(STATUS_EVENT_SOURCE_KEY);
	}
	public static class OrionEventDialog extends JDialog{
		Map<String, Object> eventMap;
		private void completeConstruction(Map<String, Object> eventMap){
			this.eventMap = eventMap;
			setTitle(String.format("Event Viewer : %s", getEventDescription(eventMap)));
			JPanel contentPane = new JPanel(){{
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				Object src = getEventSource(OrionEventDialog.this.eventMap);
				if(src != null){
					add(new JLabel("Event Source: "));
					add(new JLabel(src != null ? src.getClass().getName() : "unknown"));
				}
				Throwable th = getEventException(OrionEventDialog.this.eventMap);
				if(th != null){
					add(new JLabel("Exception Stacktrace:"));
					add(new JScrollPane(new JTextArea(Throwables.getStackTraceAsString(th))));
				}
				}};
			setContentPane(contentPane);
			pack();
		}
		public OrionEventDialog(Container parent, Map<String, Object>eventMap){
			super((parent instanceof Window) ? (Window)parent : null);
			completeConstruction(eventMap);
		}
		public OrionEventDialog(Frame parent, Map<String, Object>eventMap){
			super(parent);
			completeConstruction(eventMap);
		}
		public OrionEventDialog(JComponent parent, Map<String, Object>eventMap){
			this(parent.getTopLevelAncestor(), eventMap);
		}
		public OrionEventDialog(Map<String, Object>eventMap){
			super();
			completeConstruction(eventMap);
		}
	}
        public static final JDialog makeEventDialog(JComponent comp, Map<String, Object>m){
                return new OrionEventDialog(comp, m);
        }
}
