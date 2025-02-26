package us.wthr.jdem846.model;

import us.wthr.jdem846.canvas.CanvasProjectionTypeEnum;
import us.wthr.jdem846.gis.exceptions.MapProjectionException;
import us.wthr.jdem846.gis.projections.MapProjection;
import us.wthr.jdem846.gis.projections.MapProjectionEnum;
import us.wthr.jdem846.gis.projections.MapProjectionProviderFactory;
import us.wthr.jdem846.graphics.PerspectiveTypeEnum;
import us.wthr.jdem846.model.annotations.Order;
import us.wthr.jdem846.model.annotations.ProcessOption;
import us.wthr.jdem846.model.annotations.ValueBounds;
import us.wthr.jdem846.model.listModels.ElevationScalerListModel;
import us.wthr.jdem846.model.listModels.PerspectiveTypeListModel;
import us.wthr.jdem846.model.listModels.PlanetListModel;
import us.wthr.jdem846.model.listModels.RenderProjectionListModel;
import us.wthr.jdem846.model.processing.ModelHeightWidthValidator;
import us.wthr.jdem846.model.processing.render.MapProjectionListModel;
import us.wthr.jdem846.scaling.ElevationScalerEnum;

public class GlobalOptionModel implements OptionModel
{

	private boolean useScripting = false;
	private int width = 2000;
	private int height = 2000;
	private boolean maintainAspectRatio = true;
	private String planet = "Earth";
	private boolean estimateElevationRange = true;
	private boolean limitCoordinates = false;
	private double northLimit = 90.0;
	private double southLimit = -90.0;
	private double eastLimit = 180.0;
	private double westLimit = -180.0;
	private RgbaColor backgroundColor = new RgbaColor(255, 255, 255, 255);
	private double elevationMultiple = 1.0;
	
	
	private ViewPerspective viewAngle = ViewPerspective.fromString("rotate:[30.0,0,0];shift:[0,0,0];zoom:[1.0]");
	private ViewerPosition viewerPosition = new ViewerPosition();
	
	
	private String elevationScale = ElevationScalerEnum.LINEAR.identifier();
	private String mapProjection = MapProjectionEnum.EQUIRECTANGULAR.identifier();
	private String renderProjection = CanvasProjectionTypeEnum.PROJECT_FLAT.identifier();
	private String perspectiveType = PerspectiveTypeEnum.ORTHOGRAPHIC.identifier();

	private double modelQuality = 1.0;
	private double textureQuality = 1.0;

	private double fieldOfView = 45.0;
	private double eyeDistance = 19.07e6;

	private boolean getStandardResolutionElevation = false;
	private boolean interpolateData = true;
	private boolean averageOverlappedData = true;
	private String precacheStrategy = "tiled";
	private int tileSize = 1000;

	private int numberOfThreads = 1;

	private boolean saveModelGrid = false;
	private FilePath modelGridSavePath = new FilePath("");
	
	private boolean useDiskCachedModelGrid = false;
	private boolean disposeGridOnComplete = true;
	private boolean createJdemElevationModel = true;

	private boolean previewRendering = false;
	private boolean forceResetAndRunFilters = false;

	
	
	public GlobalOptionModel()
	{

	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.useScripting"
				, label = "Use Scripting"
				, tooltip = "Enable scripting during the modeling process"
				, visible = true)
	@Order(0)
	public boolean getUseScripting()
	{
		return useScripting;
	}

	public void setUseScripting(boolean useScripting)
	{
		this.useScripting = useScripting;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.width"
				, label = "Width"
				, tooltip = "Model image width"
				, visible = true
				, validator = ModelHeightWidthValidator.class
				, enabler = GlobalOptionModelEnabler.class)
	@Order(10)
	@ValueBounds(minimum = 1)
	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.height"
				, label = "Height"
				, tooltip = "Model image height"
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(20)
	@ValueBounds(minimum = 1)
	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	@ProcessOption(
			id = "us.wthr.jdem846.model.GlobalOptionModel.maintainAspectRatio",
			label = "Maintain Aspect Ratio",
			tooltip = "Maintain model dimensions aspect ratio in relation to raster data bounds",
			visible = true,
			enabler = GlobalOptionModelEnabler.class)
	@Order(30)
	public boolean getMaintainAspectRatio()
	{
		return maintainAspectRatio;
	}

	public void setMaintainAspectRatio(boolean maintainAspectRatio)
	{
		this.maintainAspectRatio = maintainAspectRatio;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.planet"
				, label = "Planet"
				, tooltip = ""
				, visible = true
				, listModel = PlanetListModel.class)
	@Order(40)
	public String getPlanet()
	{
		return planet;
	}

	public void setPlanet(String planet)
	{
		this.planet = planet;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.estimateElevationRange"
				, label = "Estimate Elevation Min/Max"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(50)
	public boolean isEstimateElevationRange()
	{
		return estimateElevationRange;
	}

	public void setEstimateElevationRange(boolean estimateElevationRange)
	{
		this.estimateElevationRange = estimateElevationRange;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.limitCoordinates"
				, label = "Limit Coordinates"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(60)
	public boolean getLimitCoordinates()
	{
		return limitCoordinates;
	}

	public void setLimitCoordinates(boolean limitCoordinates)
	{
		this.limitCoordinates = limitCoordinates;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.northLimit"
				, label = "North Limit"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(70)
	@ValueBounds(minimum = -90.0, maximum = 90.0, stepSize = .1)
	public double getNorthLimit()
	{
		return northLimit;
	}

	public void setNorthLimit(double northLimit)
	{
		this.northLimit = northLimit;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.southLimit"
				, label = "South Limit"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(80)
	@ValueBounds(minimum = -90.0, maximum = 90.0, stepSize = .1)
	public double getSouthLimit()
	{
		return southLimit;
	}

	public void setSouthLimit(double southLimit)
	{
		this.southLimit = southLimit;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.eastLimit"
				, label = "East Limit"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(90)
	@ValueBounds(minimum = -360.0, maximum = 360.0, stepSize = .1)
	public double getEastLimit()
	{
		return eastLimit;
	}

	public void setEastLimit(double eastLimit)
	{
		this.eastLimit = eastLimit;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.westLimit"
				, label = "West Limit"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(100)
	@ValueBounds(minimum = -360.0, maximum = 360.0, stepSize = .1)
	public double getWestLimit()
	{
		return westLimit;
	}

	public void setWestLimit(double westLimit)
	{
		this.westLimit = westLimit;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.backgroundColor"
				, label = "Background Color"
				, tooltip = "Model image background color"
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(110)
	public RgbaColor getBackgroundColor()
	{
		return backgroundColor;
	}

	public void setBackgroundColor(RgbaColor backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.elevationMultiple"
				, label = "Elevation Multiple"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(120)
	@ValueBounds(minimum = 0, stepSize = 0.1)
	public double getElevationMultiple()
	{
		return elevationMultiple;
	}

	public void setElevationMultiple(double elevationMultiple)
	{
		this.elevationMultiple = elevationMultiple;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.elevationScale"
				, label = "Elevation Scale"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class
				, listModel = ElevationScalerListModel.class)
	@Order(130)
	public String getElevationScale()
	{
		return elevationScale;
	}

	public void setElevationScale(String elevationScale)
	{
		this.elevationScale = elevationScale;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.mapProjection"
				, label = "Map Projection"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class
				, listModel = MapProjectionListModel.class)
	@Order(140)
	public String getMapProjection()
	{
		return mapProjection;
	}

	public void setMapProjection(String mapProjection)
	{
		this.mapProjection = mapProjection;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.renderProjection"
				, label = "Render Projection"
				, tooltip = ""
				, visible = true
				, listModel = RenderProjectionListModel.class)
	@Order(150)
	public String getRenderProjection()
	{
		return renderProjection;
	}

	public void setRenderProjection(String renderProjection)
	{
		this.renderProjection = renderProjection;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.perspectiveType"
				, label = "Perspective Type"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class
				, listModel = PerspectiveTypeListModel.class)
	@Order(160)
	public String getPerspectiveType()
	{
		return perspectiveType;
	}

	public void setPerspectiveType(String perspectiveType)
	{
		this.perspectiveType = perspectiveType;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.viewAngle"
				, label = "View Angle"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(170)
	public ViewPerspective getViewAngle()
	{
		return viewAngle;
	}

	public void setViewAngle(ViewPerspective viewAngle)
	{
		this.viewAngle = viewAngle;
	}
	
	
	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.viewerPosition"
			, label = "Viewer Position"
			, tooltip = ""
			, visible = true
			, enabler = GlobalOptionModelEnabler.class)
	@Order(175)
	public ViewerPosition getViewerPosition()
	{
		return viewerPosition;
	}

	public void setViewerPosition(ViewerPosition viewerPosition)
	{
		this.viewerPosition = viewerPosition;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.fieldOfView"
				, label = "Field of View"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(180)
	@ValueBounds(minimum = 1, maximum = 90)
	public double getFieldOfView()
	{
		return fieldOfView;
	}

	public void setFieldOfView(double fieldOfView)
	{
		this.fieldOfView = fieldOfView;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.eyeDistance"
				, label = "Eye Distance"
				, tooltip = "Viewer distance from center of model in meters"
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@ValueBounds(minimum = 1, maximum = 5000.07e6)
	@Order(190)
	public double getEyeDistance()
	{
		return eyeDistance;
	}

	public void setEyeDistance(double eyeDistance)
	{
		this.eyeDistance = eyeDistance;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.textureQuality"
				, label = "Texture Quality"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(220)
	@ValueBounds(minimum = 0, maximum = 10.0, stepSize = 0.05)
	public double getTextureQuality()
	{
		return textureQuality;
	}

	public void setTextureQuality(double textureQuality)
	{
		this.textureQuality = textureQuality;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.modelQuality"
				, label = "Model Quality"
				, tooltip = ""
				, visible = true
				, enabler = GlobalOptionModelEnabler.class)
	@Order(230)
	@ValueBounds(minimum = 0, maximum = 10.0, stepSize = 0.05)
	public double getModelQuality()
	{
		return modelQuality;
	}

	public void setModelQuality(double modelQuality)
	{
		this.modelQuality = modelQuality;
	}

	public boolean getStandardResolutionElevation()
	{
		return getStandardResolutionElevation;
	}

	public void setGetStandardResolutionElevation(boolean getStandardResolutionElevation)
	{
		this.getStandardResolutionElevation = getStandardResolutionElevation;
	}

	public boolean getInterpolateData()
	{
		return interpolateData;
	}

	public void setInterpolateData(boolean interpolateData)
	{
		this.interpolateData = interpolateData;
	}

	public boolean getAverageOverlappedData()
	{
		return averageOverlappedData;
	}

	public void setAverageOverlappedData(boolean averageOverlappedData)
	{
		this.averageOverlappedData = averageOverlappedData;
	}

	public String getPrecacheStrategy()
	{
		return precacheStrategy;
	}

	public void setPrecacheStrategy(String precacheStrategy)
	{
		this.precacheStrategy = precacheStrategy;
	}

	public int getTileSize()
	{
		return tileSize;
	}

	public void setTileSize(int tileSize)
	{
		this.tileSize = tileSize;
	}

	// @ProcessOption(id="us.wthr.jdem846.model.GlobalOptionModel.useDiskCachedModelGrid",
	// label="Use Disk Cache",
	// tooltip="",
	// enabled=true)
	// @Order(25)
	public boolean getUseDiskCachedModelGrid()
	{
		return useDiskCachedModelGrid;
	}

	public void setUseDiskCachedModelGrid(boolean useDiskCachedModelGrid)
	{
		this.useDiskCachedModelGrid = useDiskCachedModelGrid;
	}

	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.numberOfThreads"
				, label = "Number of Threads"
				, tooltip = ""
				, visible = true)
	@Order(260)
	@ValueBounds(minimum = 1, maximum = 10, stepSize = 1)
	public int getNumberOfThreads()
	{
		return numberOfThreads;
	}

	public void setNumberOfThreads(int numberOfThreads)
	{
		this.numberOfThreads = numberOfThreads;
	}
	
	
	
	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.saveModelGrid"
			, label = "Save Model Grid"
			, tooltip = ""
			, visible = true)
	@Order(270)
	public boolean getSaveModelGrid() 
	{
		return saveModelGrid;
	}

	public void setSaveModelGrid(boolean saveModelGrid) 
	{
		this.saveModelGrid = saveModelGrid;
	}
	
	
	
	@ProcessOption(id = "us.wthr.jdem846.model.GlobalOptionModel.modelGridSavePath"
			, label = "Save Model Grid To"
			, tooltip = ""
			, visible = true)
	@Order(280)
	public FilePath getModelGridSavePath()
	{
		return modelGridSavePath;
	}

	public void setModelGridSavePath(FilePath modelGridSavePath)
	{
		this.modelGridSavePath = modelGridSavePath;
	}

	public boolean getDisposeGridOnComplete()
	{
		return disposeGridOnComplete;
	}

	public void setDisposeGridOnComplete(boolean disposeGridOnComplete)
	{
		this.disposeGridOnComplete = disposeGridOnComplete;
	}

	public boolean isPreviewRendering()
	{
		return previewRendering;
	}

	public void setPreviewRendering(boolean previewRendering)
	{
		this.previewRendering = previewRendering;
	}

	public boolean getCreateJdemElevationModel()
	{
		return createJdemElevationModel;
	}

	public void setCreateJdemElevationModel(boolean createJdemElevationModel)
	{
		this.createJdemElevationModel = createJdemElevationModel;
	}

	public boolean getForceResetAndRunFilters()
	{
		return forceResetAndRunFilters;
	}

	public void setForceResetAndRunFilters(boolean forceResetAndRunFilters)
	{
		this.forceResetAndRunFilters = forceResetAndRunFilters;
	}

	public MapProjection getMapProjectionInstance() throws MapProjectionException
	{
		MapProjection mapProjection = MapProjectionProviderFactory.getMapProjection(getMapProjection(), getNorthLimit(), getSouthLimit(), getEastLimit(), getWestLimit(), getWidth(), getHeight());

		return mapProjection;
	}

	public GlobalOptionModel copy()
	{
		GlobalOptionModel copy = new GlobalOptionModel();

		copy.useScripting = this.useScripting;
		copy.width = this.width;
		copy.height = this.height;
		copy.maintainAspectRatio = this.maintainAspectRatio;
		copy.planet = this.planet;
		copy.estimateElevationRange = this.estimateElevationRange;
		copy.limitCoordinates = this.limitCoordinates;
		copy.northLimit = this.northLimit;
		copy.southLimit = this.southLimit;
		copy.eastLimit = this.eastLimit;
		copy.westLimit = this.westLimit;
		copy.backgroundColor = this.backgroundColor.copy();
		copy.elevationMultiple = this.elevationMultiple;
		copy.elevationScale = this.elevationScale;
		copy.mapProjection = this.mapProjection;
		copy.renderProjection = this.renderProjection;
		copy.perspectiveType = this.perspectiveType;
		copy.viewAngle = this.viewAngle.copy();
		copy.viewerPosition = this.viewerPosition.copy();
		copy.modelQuality = this.modelQuality;
		copy.textureQuality = this.textureQuality;
		copy.averageOverlappedData = this.averageOverlappedData;
		copy.getStandardResolutionElevation = this.getStandardResolutionElevation;
		copy.interpolateData = this.interpolateData;
		copy.precacheStrategy = this.precacheStrategy;
		copy.useDiskCachedModelGrid = this.useDiskCachedModelGrid;
		copy.createJdemElevationModel = this.createJdemElevationModel;
		copy.disposeGridOnComplete = this.disposeGridOnComplete;
		copy.previewRendering = this.previewRendering;
		copy.forceResetAndRunFilters = this.forceResetAndRunFilters;
		copy.numberOfThreads = this.numberOfThreads;
		copy.fieldOfView = this.fieldOfView;
		copy.eyeDistance = this.eyeDistance;
		copy.saveModelGrid = this.saveModelGrid;
		copy.modelGridSavePath = (modelGridSavePath != null) ? this.modelGridSavePath.copy() : null;
		return copy;
	}

}
