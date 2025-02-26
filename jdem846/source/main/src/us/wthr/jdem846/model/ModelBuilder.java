package us.wthr.jdem846.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import us.wthr.jdem846.ElevationModel;
import us.wthr.jdem846.JDem846Properties;
import us.wthr.jdem846.JDemElevationModel;
import us.wthr.jdem846.ModelContext;
import us.wthr.jdem846.SimpleImageElevationModel;
import us.wthr.jdem846.exception.DataSourceException;
import us.wthr.jdem846.exception.GraphicsRenderException;
import us.wthr.jdem846.exception.RenderEngineException;
import us.wthr.jdem846.exception.ScriptingException;
import us.wthr.jdem846.gis.exceptions.MapProjectionException;
import us.wthr.jdem846.gis.projections.MapProjection;
import us.wthr.jdem846.graphics.ImageCapture;
import us.wthr.jdem846.graphics.RenderProcess;
import us.wthr.jdem846.graphics.View;
import us.wthr.jdem846.graphics.ViewFactory;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.math.MathExt;
import us.wthr.jdem846.model.exceptions.ModelContainerException;
import us.wthr.jdem846.model.processing.GridFilterMethodStack;
import us.wthr.jdem846.model.processing.GridProcessMethodStack;
import us.wthr.jdem846.modelgrid.IFillControlledModelGrid;
import us.wthr.jdem846.modelgrid.IModelGrid;
import us.wthr.jdem846.modelgrid.ModelGridFactory;
import us.wthr.jdem846.modelgrid.ModelGridWriter;
import us.wthr.jdem846.rasterdata.RasterDataContext;
import us.wthr.jdem846.render.InterruptibleProcess;
import us.wthr.jdem846.render.ProcessInterruptListener;
import us.wthr.jdem846.scaling.ElevationScaler;
import us.wthr.jdem846.scaling.ElevationScalerEnum;
import us.wthr.jdem846.scaling.ElevationScalerFactory;
import us.wthr.jdem846.scripting.ScriptProxy;

public class ModelBuilder extends InterruptibleProcess implements IModelBuilder
{
	private static Log log = Logging.getLog(ModelBuilder.class);

	private List<ModelProgram> modelPrograms = new ArrayList<ModelProgram>();

	private ModelProcessManifest modelProcessManifest;
	private ModelContext modelContext;
	private IFillControlledModelGrid modelGrid;
	private IModelGrid innerModelGrid = null;

	private ElevationScaler elevationScaler = null;
	private ModelGridDimensions modelDimensions;
	private GlobalOptionModel globalOptionModel;
	private LatitudeProcessedList latitudeProcessedList = null;
	// private ModelCanvas modelCanvas;
	private BufferControlledRasterDataContainer bufferControlledRasterDataContainer;
	//private ManagedConcurrentFrameBufferController frameBufferController;

	private boolean runLoadProcessor = true;
	private boolean runColorProcessor = true;
	private boolean runHillshadeProcessor = true;

	private boolean prepared = false;
	private boolean useScripting = true;

	private boolean isProcessing = false;

	private ProgressTracker progressTracker = null;

	public ModelBuilder()
	{
		this(null);
	}

	public ModelBuilder(ProgressTracker progressTracker)
	{
		this.progressTracker = progressTracker;
	}

	public void dispose()
	{
		modelGrid.dispose();
		modelGrid = null;
	}

	public void prepare(ModelContext modelContext) throws RenderEngineException
	{
		this.modelProcessManifest = modelContext.getModelProcessManifest();
		globalOptionModel = modelProcessManifest.getGlobalOptionModel();
		this.modelContext = modelContext;
		
		if (progressTracker != null) {
			progressTracker.beginTask("Preparing model builder", 6 + globalOptionModel.getNumberOfThreads());
		}

		doesSufficientDataExist();

		modelPrograms.clear();

		if (!globalOptionModel.getLimitCoordinates()) {
			globalOptionModel.setNorthLimit(modelContext.getNorth());
			globalOptionModel.setSouthLimit(modelContext.getSouth());
			globalOptionModel.setEastLimit(modelContext.getEast());
			globalOptionModel.setWestLimit(modelContext.getWest());
		}

		// +
		if (progressTracker != null) {
			progressTracker.worked(1);
		}

		ModelGridDimensions modelDimensions = ModelGridDimensions.getModelDimensions(modelContext);

		
		this.modelDimensions = modelDimensions;
		

		double minimumElevation = modelContext.getRasterDataContext().getDataMinimumValue();
		double maximumElevation = modelContext.getRasterDataContext().getDataMaximumValue();

		ElevationScalerEnum elevationScalerEnum = ElevationScalerEnum.getElevationScalerEnumFromIdentifier(globalOptionModel.getElevationScale());
		try {
			elevationScaler = ElevationScalerFactory.createElevationScaler(elevationScalerEnum, globalOptionModel.getElevationMultiple(), minimumElevation, maximumElevation);
		} catch (Exception ex) {
			throw new RenderEngineException("Error creating elevation scaler: " + ex.getMessage(), ex);
		}
		//modelContext.getRasterDataContext().setElevationScaler(elevationScaler);

		modelContext.getRasterDataContext().setAvgOfAllRasterValues(globalOptionModel.getAverageOverlappedData());
		modelContext.getRasterDataContext().setInterpolate(globalOptionModel.getInterpolateData());

		// +
		if (progressTracker != null) {
			progressTracker.worked(1);
		}

		bufferControlledRasterDataContainer = new BufferControlledRasterDataContainer(modelContext.getRasterDataContext(), globalOptionModel.getPrecacheStrategy(), modelDimensions.getLatitudeResolution(), globalOptionModel.getTileSize());
		modelContext.setRasterDataContext(bufferControlledRasterDataContainer);

		innerModelGrid = modelContext.getModelGridContext().getModelGrid();
		if (innerModelGrid == null) {
			try {
				innerModelGrid = ModelGridFactory.createBufferedModelGrid(modelContext);
			} catch (DataSourceException ex) {
				throw new RenderEngineException("Error creating buffered model grid: " + ex.getMessage(), ex);
			}
			
			modelContext.getModelGridContext().setModelGrid(innerModelGrid);
		}
		
		
		modelGrid = modelContext.getModelGridContext().getFillControlledModelGrid();
		if (modelGrid == null) {
			
			try {
				modelGrid = ModelGridFactory.createFillControlledModelGrid(modelContext);
			} catch (DataSourceException ex) {
				throw new RenderEngineException("Error creating fill controlled model grid: " + ex.getMessage(), ex);
			}
			
			modelGrid.setForceResetAndRunFilters(globalOptionModel.getForceResetAndRunFilters());
			modelContext.getModelGridContext().setFillControlledModelGrid(modelGrid);
			
		//	int dataRows = (int) MathExt.round((globalOptionModel.getNorthLimit() - globalOptionModel.getSouthLimit()) / modelDimensions.getTextureLatitudeResolution());
			
		} 
		
		if (this.latitudeProcessedList == null) {
			this.latitudeProcessedList = new LatitudeProcessedList(globalOptionModel.getNorthLimit(), modelDimensions.getTextureLatitudeResolution(), modelGrid.getHeight());
		} else {
			this.latitudeProcessedList.reset();
		}
	

		// +
		if (progressTracker != null) {
			progressTracker.worked(1);
		}

		useScripting = globalOptionModel.getUseScripting();

		int numberOfThreads = globalOptionModel.getNumberOfThreads();
		//this.frameBufferController = new ManagedConcurrentFrameBufferController(globalOptionModel.getWidth(), globalOptionModel.getHeight(), numberOfThreads);

		// +
		if (progressTracker != null) {
			progressTracker.worked(1);
		}

		for (int i = 0; i < numberOfThreads; i++) {
			ModelProgram modelProgram;

			//View view = ViewFactory.getViewInstance(modelContext, globalOptionModel, modelDimensions, mapProjection, modelContext.getScriptingContext().getScriptProxy(), modelGrid, elevationScaler);

			try {
				modelProgram = modelProcessManifest.createModelProgram(null, null);
			} catch (Exception ex) {
				throw new RenderEngineException("Error creating model program: " + ex.getMessage(), ex);
			}

			RasterDataContext programRasterInstance = null;
			try {
				programRasterInstance = modelContext.getRasterDataContext().copy();
			} catch (DataSourceException ex) {
				throw new RenderEngineException("Error creating raster data context copy: " + ex.getMessage(), ex);
			}
			
			
			IFillControlledModelGrid programModelGrid;
			try {
				programModelGrid = modelGrid.createDependentInstance(programRasterInstance);
			} catch (DataSourceException ex) {
				throw new RenderEngineException("Error creating subinstance of model grid: " + ex.getMessage(), ex);
			}

			GridFilterMethodStack filterStack = modelProgram.getFilterStack();

			programModelGrid.setGridFilters(filterStack);

			try {
				modelProgram.setRasterDataContext(programRasterInstance);
				modelProgram.setModelContext(modelContext);
				modelProgram.setModelGrid(programModelGrid);
				modelProgram.setModelDimensions(modelDimensions);
				modelProgram.setGlobalOptionModel(globalOptionModel);

				if (useScripting && modelContext.getScriptingContext() != null && modelContext.getScriptingContext().getScriptProxy() != null) {

					modelProgram.setScript(modelContext.getScriptingContext().getScriptProxy());
				} else {
					modelProgram.setScript(null);
				}

				modelProgram.prepare();

				modelPrograms.add(modelProgram);
			} catch (Exception ex) {
				throw new RenderEngineException("Error creating model program: " + ex.getMessage(), ex);
			}

			// +
			if (progressTracker != null) {
				progressTracker.worked(1);
			}
		}

		if (useScripting && modelContext.getScriptingContext() != null && modelContext.getScriptingContext().getScriptProxy() != null) {
			// modelContext.getScriptingContext().getScriptProxy().setModelContext(modelContext);
			ScriptProxy scriptProxy = modelContext.getScriptingContext().getScriptProxy();

			try {
				scriptProxy.setProperty("modelContext", modelContext);
				scriptProxy.setProperty("modelGrid", modelGrid);
				scriptProxy.setProperty("globalOptionModel", globalOptionModel);
				scriptProxy.setProperty("modelDimensions", modelDimensions);
				scriptProxy.setProperty("elevationScaler", elevationScaler);
			} catch (ScriptingException ex) {
				throw new RenderEngineException("Error setting script properties: " + ex.getMessage(), ex);
			}

		}

		// +
		if (progressTracker != null) {
			progressTracker.worked(1);
		}

		if (useScripting) {
			onInitialize();
		}

		// +
		if (progressTracker != null) {
			progressTracker.worked(1);
		}

		prepared = true;
	}

	
	protected boolean doesSufficientDataExist() throws RenderEngineException
	{
		if (modelContext.getRasterDataContext().getRasterDataListSize() == 0 && modelContext.getImageDataContext().getImageListSize() == 0 && !modelContext.getModelGridContext().isUserProvided()) {
			throw new RenderEngineException("Insufficient input data provided to generate model");
		} else {
			return true;
		}
	}
	
	@Override
	public ElevationModel process() throws RenderEngineException
	{
		doesSufficientDataExist();
		
		this.onProcessBefore();
		
		if (!this.modelGrid.isCompleted()) {
			this.processModelData();
		} else {
			log.info("Model grid indicates that it is already completed. Skipping...");
		}
		ImageCapture imageCapture = this.processModelRender();
		this.onProcessAfter();
		this.onDestroy();
		return this.createElevationModel(imageCapture);
	}

	@Override
	public void processModelData() throws RenderEngineException
	{
		if (!isPrepared()) {
			throw new RenderEngineException("Model builder not yet prepared!");
		}

		if (!modelContainsData()) {
			log.info("Model contains no data. Skipping model build process");
			return;
		}

		// if (progressTracker != null) {
		// progressTracker.beginTask("Generating Model Data",
		// globalOptionModel.getNumberOfThreads());
		// }

		ProcessInterruptHandler interruptHandler = new ProcessInterruptHandler();
		GlobalOptionModel globalOptionModel = modelProcessManifest.getGlobalOptionModel();

		// setProcessing(true);
		// onProcessBefore();

		int numberOfThreads = globalOptionModel.getNumberOfThreads();
		double north = globalOptionModel.getNorthLimit();
		double south = globalOptionModel.getSouthLimit();
		double east = globalOptionModel.getEastLimit();
		double west = globalOptionModel.getWestLimit();

		double latitudeResolution = modelDimensions.getTextureLatitudeResolution();
		double longitudeResolution = modelDimensions.getTextureLongitudeResolution();

		if (numberOfThreads == 1) {
			ModelProgram modelProgram = this.modelPrograms.get(0);
			GridProcessChunkThread chunkThread = new GridProcessChunkThread(this.latitudeProcessedList, modelProgram, 0, north, south, east, west, latitudeResolution, longitudeResolution);
			chunkThread.run();

			if (progressTracker != null) {
				progressTracker.worked(1);
			}

		} else {
			GridProcessChunkThread[] threads = new GridProcessChunkThread[numberOfThreads];

			int dataRows = (int) MathExt.ceil((north - south) / latitudeResolution);

			int rowsPerThread = (int) MathExt.ceil((double) dataRows / (double) numberOfThreads);

			double chunkNorth = 0;
			double chunkSouth = 0;

			int threadNumber = 0;
			for (int y = 0; y <= dataRows; y += rowsPerThread) {

				chunkNorth = north - ((double) y * latitudeResolution);
				chunkSouth = chunkNorth - ((double) rowsPerThread * latitudeResolution);

				if (chunkSouth < south) {
					chunkSouth = south;
				}

				if (threadNumber >= 0 && threadNumber < modelPrograms.size()) {
					ModelProgram modelProgram = this.modelPrograms.get(threadNumber);
					threads[threadNumber] = new GridProcessChunkThread(this.latitudeProcessedList, modelProgram, threadNumber, chunkNorth, chunkSouth, east, west, latitudeResolution, longitudeResolution);

					threads[threadNumber].start();
				} else {
					log.warn("Invalid thread number: " + threadNumber);
				}

				threadNumber++;

			}

			boolean threadsActive = true;

			while (threadsActive) {
				threadsActive = false;

				for (GridProcessChunkThread thread : threads) {
					if (thread != null && !thread.isCompleted()) {
						threadsActive = true;
						break;
					}
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException ex) {
					log.warn("Thread sleep interrupted while waiting for grid process threads to complete: " + ex.getMessage(), ex);
				}
			}

			if (progressTracker != null) {
				progressTracker.worked(threads.length);
			}
		}
		
		this.modelGrid.setCompleted(true);
		if (!globalOptionModel.isPreviewRendering() && globalOptionModel.getSaveModelGrid()) {
			
			FilePath saveTo = globalOptionModel.getModelGridSavePath();
			if (saveTo != null && saveTo.isValid(true)) {
				try {
					ModelGridWriter.write(saveTo.getPath(), innerModelGrid);
				} catch (Exception ex) {
					log.error("Error writing model grid to file: " + ex.getMessage(), ex);
				}
			}
		}
		
//		try {
//			ModelGridWriter.write("C:\\jdem\\temp\\modelgrid_test.jdemgrid", innerModelGrid);
//		} catch (IOException ex) {
//			// TODO Auto-generated catch block
//			ex.printStackTrace();
//		}

	}

	@Override
	public ImageCapture processModelRender() throws RenderEngineException
	{
		if (!isPrepared()) {
			throw new RenderEngineException("Model builder not yet prepared!");
		}
		// onProcessAfter();

		// Reset the lat processed list to prepare for the render stage
		this.latitudeProcessedList.reset();
		//frameBufferController.start();

		log.info("Initializing final rendering...");
		MapProjection mapProjection = null;

		try {

			mapProjection = globalOptionModel.getMapProjectionInstance();

		} catch (MapProjectionException ex) {
			throw new RenderEngineException("Error creating map projection: " + ex.getMessage(), ex);
		}

		View view = ViewFactory.getViewInstance(modelContext, globalOptionModel, modelDimensions, mapProjection, modelContext.getScriptingContext().getScriptProxy(), modelGrid, elevationScaler);
		
		//ModelRenderer renderer = new ModelRenderer();
		RenderProcess renderer = new RenderProcess(view);
		//renderer.setView(view);
		//renderer.setFrameBuffer(frameBufferController.getPartialBuffer(0));
		//renderer.setOptionModel(new ModelRenderOptionModel());
		renderer.setModelContext(modelContext);
		renderer.setGlobalOptionModel(globalOptionModel);
		renderer.setModelDimensions(modelDimensions);
		renderer.setScript(modelContext.getScriptingContext().getScriptProxy());
		renderer.setModelGrid(modelGrid);
		renderer.setScaler(elevationScaler);
		
		renderer.prepare();

		try {
			renderer.run();
		} catch (GraphicsRenderException ex) {
			throw new RenderEngineException("Error rendering image: " + ex.getMessage(), ex);
		}
		
		ImageCapture imageCapture = renderer.capture();
		
		renderer.dispose();
		
		return imageCapture;

	}

	@Override
	public ElevationModel createElevationModel(ImageCapture imageCapture) throws RenderEngineException
	{
		if (!isPrepared()) {
			throw new RenderEngineException("Model builder not yet prepared!");
		}

		//ImageCapture imageCapture = this.frameBufferController.captureImage(this.globalOptionModel.getBackgroundColor().getRgba());

		if (progressTracker != null) {
			progressTracker.beginTask("Finalizing", 1);
		}

		ElevationModel elevationModel = null;

		if (globalOptionModel.getCreateJdemElevationModel()) {

			log.info("Compiling final elevation model");
			elevationModel = new JDemElevationModel(imageCapture);

			setJDemElevationModelProperties(elevationModel);
		} else {
			elevationModel = new SimpleImageElevationModel(imageCapture);
		}

		return elevationModel;
	}

	protected void setJDemElevationModelProperties(ElevationModel elevationModel) throws RenderEngineException
	{

		elevationModel.setProperty("subject", JDem846Properties.getProperty("us.wthr.jdem846.defaults.subject"));
		elevationModel.setProperty("description", JDem846Properties.getProperty("us.wthr.jdem846.defaults.description"));
		elevationModel.setProperty("author", JDem846Properties.getProperty("us.wthr.jdem846.defaults.author"));
		elevationModel.setProperty("author-contact", JDem846Properties.getProperty("us.wthr.jdem846.defaults.author-contact"));
		elevationModel.setProperty("institution", JDem846Properties.getProperty("us.wthr.jdem846.defaults.institution"));
		elevationModel.setProperty("institution-contact", JDem846Properties.getProperty("us.wthr.jdem846.defaults.institution-contact"));
		elevationModel.setProperty("institution-address", JDem846Properties.getProperty("us.wthr.jdem846.defaults.institution-address"));
		elevationModel.setProperty("render-date", (new Date()).toString());
		elevationModel.setProperty("product-version", JDem846Properties.getProperty("us.wthr.jdem846.version"));

		if (modelGrid != null) {
			try {
				elevationModel.setElevationHistogramModel(modelGrid.getElevationHistogramModel());
			} catch (DataSourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		elevationModel.setProperty("max-model-latitude", "" + this.globalOptionModel.getNorthLimit());
		elevationModel.setProperty("min-model-latitude", "" + this.globalOptionModel.getSouthLimit());

		elevationModel.setProperty("max-model-longitude", "" + this.globalOptionModel.getEastLimit());
		elevationModel.setProperty("min-model-longitude", "" + this.globalOptionModel.getWestLimit());

		elevationModel.setProperty("max-data-latitude", "" + modelContext.getNorth());
		elevationModel.setProperty("min-data-latitude", "" + modelContext.getSouth());

		elevationModel.setProperty("max-data-longitude", "" + modelContext.getEast());
		elevationModel.setProperty("min-data-longitude", "" + modelContext.getWest());

		elevationModel.setProperty("model-resolution-latitude", "" + modelDimensions.modelLatitudeResolution);
		elevationModel.setProperty("model-resolution-longitude", "" + modelDimensions.modelLongitudeResolution);

		elevationModel.setProperty("texture-resolution-latitude", "" + modelDimensions.textureLatitudeResolution);
		elevationModel.setProperty("texture-resolution-longitude", "" + modelDimensions.textureLongitudeResolution);

		elevationModel.setProperty("data-resolution-latitude", "" + modelDimensions.latitudeResolution);
		elevationModel.setProperty("data-resolution-longitude", "" + modelDimensions.longitudeResolution);

		elevationModel.setProperty("elevation-minimum", "" + modelContext.getRasterDataContext().getDataMinimumValue());
		elevationModel.setProperty("elevation-maximum", "" + modelContext.getRasterDataContext().getDataMaximumValue());
		elevationModel.setProperty("elevation-minmax-estimated", "" + globalOptionModel.isEstimateElevationRange());

		elevationModel.setProperty("model-columns", "" + modelDimensions.outputWidth);
		elevationModel.setProperty("model-rows", "" + modelDimensions.outputHeight);

		elevationModel.setProperty("data-columns", "" + modelDimensions.dataColumns);
		elevationModel.setProperty("data-rows", "" + modelDimensions.dataRows);

		elevationModel.setProperty("render-projection", globalOptionModel.getRenderProjection());
		elevationModel.setProperty("elevation-scale", globalOptionModel.getElevationScale());
		elevationModel.setProperty("elevation-multiple", "" + globalOptionModel.getElevationMultiple());
		elevationModel.setProperty("planet", globalOptionModel.getPlanet());

		try {
			String projection = (String) modelContext.getModelProcessManifest().getPropertyById("us.wthr.jdem846.model.ModelRenderOptionModel.mapProjection");
			if (projection != null) {
				elevationModel.setProperty("projection", projection);
			}
		} catch (ModelContainerException ex) {
			log.warn("Error fetching projection: " + ex.getMessage(), ex);
		}

		try {
			ViewPerspective viewPerspective = (ViewPerspective) modelContext.getModelProcessManifest().getPropertyById("us.wthr.jdem846.model.ModelRenderOptionModel.viewAngle");
			if (viewPerspective != null) {
				elevationModel.setProperty("view-perspective", viewPerspective.toString());
			}
		} catch (ModelContainerException ex) {
			log.warn("Error fetching view perspective: " + ex.getMessage(), ex);
		}

	}

	public boolean isPrepared()
	{
		return prepared;
	}

	protected void onInitialize() throws RenderEngineException
	{
		try {
			ScriptProxy scriptProxy = (modelContext != null && modelContext.getScriptingContext() != null) ? modelContext.getScriptingContext().getScriptProxy() : null;
			if (scriptProxy != null && globalOptionModel.getUseScripting()) {
				scriptProxy.initialize();
			}
		} catch (Exception ex) {
			throw new RenderEngineException("Exception thrown in user script", ex);
		}

	}

	public void onProcessBefore() throws RenderEngineException
	{
		for (ModelProgram modelProgram : modelPrograms) {

			GridFilterMethodStack filterStack = modelProgram.getFilterStack();
			GridProcessMethodStack processStack = modelProgram.getProcessStack();

			try {
				filterStack.onProcessBefore();
				processStack.onProcessBefore();
			} catch (Exception ex) {
				throw new RenderEngineException("Exception thrown model worker during 'onProcessBefore': " + ex.getMessage(), ex);
			}

		}

		try {
			ScriptProxy scriptProxy = (modelContext != null && modelContext.getScriptingContext() != null) ? modelContext.getScriptingContext().getScriptProxy() : null;
			if (scriptProxy != null && globalOptionModel.getUseScripting()) {
				scriptProxy.onProcessBefore();
			}
		} catch (Exception ex) {
			throw new RenderEngineException("Exception thrown in user script", ex);
		}

	}

	public void onProcessAfter() throws RenderEngineException
	{
		for (ModelProgram modelProgram : modelPrograms) {

			GridFilterMethodStack filterStack = modelProgram.getFilterStack();
			GridProcessMethodStack processStack = modelProgram.getProcessStack();

			try {
				filterStack.onProcessAfter();
				processStack.onProcessAfter();
			} catch (Exception ex) {
				throw new RenderEngineException("Exception thrown model worker during 'onProcessAfter': " + ex.getMessage(), ex);
			}

		}

		try {
			ScriptProxy scriptProxy = (modelContext != null && modelContext.getScriptingContext() != null) ? modelContext.getScriptingContext().getScriptProxy() : null;
			if (scriptProxy != null && globalOptionModel.getUseScripting()) {
				scriptProxy.onProcessAfter();
			}
		} catch (Exception ex) {
			throw new RenderEngineException("Exception thrown in user script", ex);
		}

	}

	public void onDestroy() throws RenderEngineException
	{
		try {
			ScriptProxy scriptProxy = (modelContext != null && modelContext.getScriptingContext() != null) ? modelContext.getScriptingContext().getScriptProxy() : null;
			if (scriptProxy != null && globalOptionModel.getUseScripting()) {
				scriptProxy.destroy();
			}
		} catch (Exception ex) {
			throw new RenderEngineException("Exception thrown in user script", ex);
		}

	}

	public boolean runLoadProcessor()
	{
		return runLoadProcessor;
	}

	public void setRunLoadProcessor(boolean runLoadProcessor)
	{
		this.runLoadProcessor = runLoadProcessor;
	}

	public boolean runColorProcessor()
	{
		return runColorProcessor;
	}

	public void setRunColorProcessor(boolean runColorProcessor)
	{
		this.runColorProcessor = runColorProcessor;
	}

	public boolean runHillshadeProcessor()
	{
		return runHillshadeProcessor;
	}

	public void setRunHillshadeProcessor(boolean runHillshadeProcessor)
	{
		this.runHillshadeProcessor = runHillshadeProcessor;
	}

	public boolean useScripting()
	{
		return useScripting;
	}

	public void setUseScripting(boolean useScripting)
	{
		this.useScripting = useScripting;
	}

	class ProcessInterruptHandler implements ProcessInterruptListener
	{
		private InterruptibleProcess interruptibleProcess;

		public void setInterruptibleProcess(InterruptibleProcess interruptibleProcess)
		{
			this.interruptibleProcess = interruptibleProcess;
		}

		public void onProcessCancelled()
		{
			if (this.interruptibleProcess != null) {
				interruptibleProcess.cancel();
			}
		}

		public void onProcessPaused()
		{
			if (interruptibleProcess != null) {
				interruptibleProcess.pause();
			}
		}

		public void onProcessResumed()
		{
			if (interruptibleProcess != null) {
				interruptibleProcess.resume();
			}
		}

	}

	protected boolean modelContainsData()
	{
		boolean hasRasterData = modelContext.getRasterDataContext().getRasterDataListSize() > 0;
		boolean hasImageData = modelContext.getImageDataContext().getImageListSize() > 0;
		boolean hasModelGridData = modelContext.getModelGridContext().getGridLoadedFrom() != null;
		
		
		return (hasRasterData || hasImageData || hasModelGridData);
	}

	protected void setProcessing(boolean isProcessing)
	{
		this.isProcessing = isProcessing;
	}

	public boolean isProcessing()
	{
		return isProcessing;
	}

	public IModelGrid getModelGrid()
	{
		return modelGrid;
	}

}
