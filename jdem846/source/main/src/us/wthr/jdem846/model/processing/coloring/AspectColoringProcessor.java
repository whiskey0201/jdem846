package us.wthr.jdem846.model.processing.coloring;

import java.util.LinkedList;
import java.util.List;

import us.wthr.jdem846.exception.RenderEngineException;
import us.wthr.jdem846.gis.planets.PlanetsRegistry;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.math.Vector;
import us.wthr.jdem846.model.annotations.GridProcessing;
import us.wthr.jdem846.model.processing.GridFilter;
import us.wthr.jdem846.model.processing.GridProcessingTypesEnum;
import us.wthr.jdem846.model.processing.util.Aspect;
import us.wthr.jdem846.model.processing.util.SurfaceNormalCalculator;


@GridProcessing(id="us.wthr.jdem846.model.processing.coloring.AspectColoringProcessor",
				name="Aspect Color Process",
				type=GridProcessingTypesEnum.COLORING,
				optionModel=AspectColoringOptionModel.class,
				enabled=true,
				isFilter=true
)
public class AspectColoringProcessor extends GridFilter
{
	private static Log log = Logging.getLog(AspectColoringProcessor.class);
	
	private int[] rgbaBuffer = new int[4];
	
	private Vector normal = new Vector();
	private SurfaceNormalCalculator normalsCalculator;
	
	private List<AspectColorCategory> colorCategoryList = new LinkedList<AspectColorCategory>();
	
	public AspectColoringProcessor()
	{
		
	}

	@Override
	public void prepare() throws RenderEngineException
	{
		AspectColoringOptionModel optionModel = (AspectColoringOptionModel) this.getOptionModel();
		
		colorCategoryList.clear();
		
		// TODO: Make these part of the option model
		colorCategoryList.add(new AspectColorCategory("Flat", -1, -1, new int[] {176, 176, 176, 255}));
		colorCategoryList.add(new AspectColorCategory("North", 0, 22.5, new int[] {255, 0, 0, 255}));
		colorCategoryList.add(new AspectColorCategory("Northeast", 22.5, 67.5, new int[] {255, 166, 0, 255}));
		colorCategoryList.add(new AspectColorCategory("East", 67.5, 112.5, new int[] {255, 255, 0, 255}));
		colorCategoryList.add(new AspectColorCategory("Southeast", 112.5, 157.5, new int[] {0, 255, 0, 255}));
		colorCategoryList.add(new AspectColorCategory("South", 157.5, 202.5, new int[] {0, 255, 255, 255}));
		colorCategoryList.add(new AspectColorCategory("Southwest", 202.5, 247.5, new int[] {0, 166, 255, 255}));
		colorCategoryList.add(new AspectColorCategory("West", 247.5, 292.5, new int[] {0, 0, 255, 255}));
		colorCategoryList.add(new AspectColorCategory("Northwest", 292.5, 337.5, new int[] {255, 0, 255, 255}));
		colorCategoryList.add(new AspectColorCategory("North", 337.5, 360.0, new int[] {255, 0, 0, 255}));
		
		
		normalsCalculator = new SurfaceNormalCalculator(modelGrid, 
				PlanetsRegistry.getPlanet(getGlobalOptionModel().getPlanet()), 
				getModelDimensions().getTextureLatitudeResolution(), 
				getModelDimensions().getTextureLongitudeResolution());
	}
	

	
	
	@Override
	public void onModelPoint(double latitude, double longitude)
			throws RenderEngineException
	{
		
		normalsCalculator.calculateNormal(latitude, longitude, normal);
		double degrees = Aspect.aspectInDegrees(normal);

		getCategoryColor(degrees, rgbaBuffer);
		this.modelGrid.setRgba(latitude, longitude, rgbaBuffer);
	}
	
	
	
	public void getCategoryColor(double degrees, int[] rgba)
	{
		
		for (AspectColorCategory category : colorCategoryList) {
			
			if (degrees >= category.getStart() && degrees < category.getEnd()) {
				category.getRgba(rgba);
				//log.info("Direction: " + degrees + " (" + category.getDirection() + ")");
				return;
			}
			
		}
		
		rgba[0] = 0;
		rgba[1] = 0;
		rgba[2] = 0;
		rgba[3] = 255;
		
	}


	@Override
	public void onProcessBefore() throws RenderEngineException
	{
		
	}

	@Override
	public void onProcessAfter() throws RenderEngineException
	{
		
	}

	@Override
	public void dispose() throws RenderEngineException
	{
		
	}
	
	
}
