package us.wthr.jdem846.modelgrid;

import java.io.File;
import java.io.IOException;

import us.wthr.jdem846.buffers.IIntBuffer;
import us.wthr.jdem846.exception.DataSourceException;
import us.wthr.jdem846.graphics.IColor;
import us.wthr.jdem846.model.ElevationHistogramModel;
import us.wthr.jdem846.model.processing.GridFilter;
import us.wthr.jdem846.model.processing.GridFilterMethodStack;
import us.wthr.jdem846.rasterdata.RasterDataContext;

public class UserProvidedModelGrid implements IModelGrid, IFillControlledModelGrid
{
	
	private File file;
	private ModelGridHeader modelGridHeader;
	private IModelGrid modelGrid;
	
	private boolean isDisposed = false;
	
	public UserProvidedModelGrid(String filePath) throws DataSourceException
	{
		this(new File(filePath));
	}
	
	public UserProvidedModelGrid(File file) throws DataSourceException
	{
		this.file = file;
		
		try {
			this.modelGridHeader = ModelGridReader.readHeader(file);
		} catch (IOException ex) {
			throw new DataSourceException("Error reading data grid header: " + ex.getMessage(), ex);
		}
		
	}
	
	@Override
	public boolean isCompleted()
	{
		return true;
	}
	
	@Override
	public void setCompleted(boolean completed)
	{
		// Do nothing. user provided model grids are read-only and completed by virtue of their original creation and export.
	}
	
	public String getFilePath()
	{
		return file.getAbsolutePath();
	}
	
	public ModelGridHeader getModelGridHeader()
	{
		return this.modelGridHeader;
	}
	
	
	public void load() throws DataSourceException
	{
		try {
			this.modelGrid = ModelGridReader.read(file);
		} catch (IOException ex) {
			throw new DataSourceException("Error reading grid data: " + ex.getMessage(), ex);
		}
	}
	
	public void unload()
	{
		this.modelGrid = null;
	}
	
	
	protected IModelGrid getInternalModelGrid() throws DataSourceException
	{
		if (modelGrid == null) {
			load();
		}
		return modelGrid;
	}
	
	@Override
	public IIntBuffer getModelTexture()
	{
		return getInternalModelGrid().getModelTexture();
	}

	@Override
	public void dispose()
	{
		
		isDisposed = true;
	}

	@Override
	public boolean isDisposed()
	{
		return isDisposed;
	}

	@Override
	public void reset()
	{
		// Do nothing. user provided model grids are read-only
	}

	@Override
	public double getElevationByIndex(int index) throws DataSourceException
	{
		return getInternalModelGrid().getElevationByIndex(index);
	}

	@Override
	public void setElevationByIndex(int index, double elevation) throws DataSourceException
	{
		// Do nothing. user provided model grids are read-only
	}
	
	@Override
	public double getElevation(double latitude, double longitude) throws DataSourceException
	{
		return getInternalModelGrid().getElevation(latitude, longitude);
	}

	@Override
	public double getElevation(double latitude, double longitude, boolean basic) throws DataSourceException
	{
		return getInternalModelGrid().getElevation(latitude, longitude, basic);

	}

	@Override
	public void setElevation(double latitude, double longitude, double elevation) throws DataSourceException
	{
		// Do nothing. user provided model grids are read-only
	}
	
	
	@Override
	public void getRgbaByIndex(int index, int[] fill) throws DataSourceException
	{
		getInternalModelGrid().getRgbaByIndex(index, fill);
	}

	@Override
	public IColor getRgbaByIndex(int index) throws DataSourceException
	{
		return getInternalModelGrid().getRgbaByIndex(index);
	}

	@Override
	public void setRgbaByIndex(int index, IColor rgba) throws DataSourceException
	{
		// Do nothing. user provided model grids are read-only
	}

	@Override
	public void setRgbaByIndex(int index, int[] rgba) throws DataSourceException
	{
		// Do nothing. user provided model grids are read-only
	}
	
	
	public IColor getRgba(int x, int y) throws DataSourceException
	{
		return getInternalModelGrid().getRgba(x, y);
	}
	
	@Override
	public void getRgba(double latitude, double longitude, int[] fill) throws DataSourceException
	{
		getRgba(latitude, longitude).toArray(fill);
	}

	@Override
	public IColor getRgba(double latitude, double longitude) throws DataSourceException
	{
		return getInternalModelGrid().getRgba(latitude, longitude);
	}

	@Override
	public void setRgba(double latitude, double longitude, IColor rgba) throws DataSourceException
	{
		// Do nothing. user provided model grids are read-only
	}

	@Override
	public void setRgba(double latitude, double longitude, int[] rgba) throws DataSourceException
	{
		// Do nothing. user provided model grids are read-only
	}

	@Override
	public ElevationHistogramModel getElevationHistogramModel() throws DataSourceException
	{
		return getInternalModelGrid().getElevationHistogramModel();
	}

	@Override
	public int getWidth()
	{
		return modelGridHeader.width;
	}

	@Override
	public int getHeight()
	{
		return modelGridHeader.height;
	}

	@Override
	public double getNorth()
	{
		return modelGridHeader.north;
	}

	@Override
	public double getSouth()
	{
		return modelGridHeader.south;
	}

	@Override
	public double getEast()
	{
		return modelGridHeader.east;
	}

	@Override
	public double getWest()
	{
		return modelGridHeader.west;
	}

	@Override
	public double getLatitudeResolution()
	{
		return modelGridHeader.latitudeResolution;
	}

	@Override
	public double getLongitudeResolution()
	{
		return modelGridHeader.longitudeResolution;
	}

	@Override
	public long getGridLength()
	{
		return modelGridHeader.width * modelGridHeader.height;
	}

	@Override
	public double getMinimum()
	{
		return modelGridHeader.minimum;
	}

	@Override
	public void setMinimum(double minimum)
	{
		// Do nothing. user provided model grids are read-only
	}

	@Override
	public double getMaximum()
	{
		return modelGridHeader.maximum;
	}

	@Override
	public void setMaximum(double maximum)
	{
		// Do nothing. user provided model grids are read-only
	}

	@Override
	public boolean getForceResetAndRunFilters()
	{
		// Do nothing. user provided model grids are read-only
		return false;
	}

	@Override
	public void setForceResetAndRunFilters(boolean forceResetAndRunFilters)
	{
		// Do nothing. user provided model grids are read-only
	}

	@Override
	public IFillControlledModelGrid createDependentInstance(RasterDataContext rasterDataContext) throws DataSourceException
	{
		return this;
	}

	@Override
	public void processFiltersOnPoint(double latitude, double longitude)
	{
		// Do nothing. user provided model grids are read-only
	}

	@Override
	public void setGridFilters(GridFilterMethodStack gridFilters)
	{
		// Do nothing. user provided model grids are read-only
	}

	@Override
	public void addGridFilter(GridFilter gridFilter)
	{
		// Do nothing. user provided model grids are read-only
	}





}
