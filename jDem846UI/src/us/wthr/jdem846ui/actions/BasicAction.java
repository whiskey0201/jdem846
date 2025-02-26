package us.wthr.jdem846ui.actions;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchWindow;

import us.wthr.jdem846ui.Activator;

public class BasicAction extends Action
{
	private List<ActionListener> actionListeners = new LinkedList<ActionListener>();
	
	private final String viewId;
	private final IWorkbenchWindow window;
	
	
	public BasicAction(String id, String viewId, String label)
	{
		this(null, id, viewId, label, null);
	}
	
	public BasicAction(IWorkbenchWindow window, String id, String viewId, String label)
	{
		this(window, id, viewId, label, null);
	}
	
	public BasicAction(String id, String viewId, String label, String iconPath)
	{
		this(null, id, viewId, label, iconPath);
	}
	
	public BasicAction(IWorkbenchWindow window, String id, String viewId, String label, String iconPath)
	{
		this.window = window;
		this.viewId = viewId;
        setText(label);

		setId(id);
		setActionDefinitionId(id);
		
		if (iconPath != null) {
			setImageDescriptor(Activator.getImageDescriptor(iconPath));
		}
	}
	
	
	@Override
	public void runWithEvent(Event event)
	{
		super.runWithEvent(event);
		this.fireActionListeners();
	}

	@Override
	public void run() 
	{
		super.run();
		this.fireActionListeners();
	}
	
	protected IWorkbenchWindow getWindow()
	{
		return window;
	}
	

	public void addActionListener(ActionListener l)
	{
		actionListeners.add(l);
	}
	
	public boolean removeActionListener(ActionListener l)
	{
		return actionListeners.remove(l);
	}
	
	protected void fireActionListeners()
	{
		for (ActionListener listener : actionListeners) {
			try {
				listener.onAction();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
}
