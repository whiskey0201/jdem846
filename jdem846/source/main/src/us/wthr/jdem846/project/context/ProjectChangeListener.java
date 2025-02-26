package us.wthr.jdem846.project.context;

import us.wthr.jdem846.ElevationModel;
import us.wthr.jdem846.model.OptionModelChangeEvent;

public interface ProjectChangeListener {
	
	public void onDataAdded();
	public void onDataRemoved();
	public void onOptionChanged(OptionModelChangeEvent e);
	
	public void onElevationModelAdded(ElevationModel elevationModel);
	public void onElevationModelRemoved(ElevationModel elevationModel);
	
	public void onBeforeProjectLoaded(String filePathOld, String filePathNew);
	public void onProjectLoaded(String filePath);
	
}
