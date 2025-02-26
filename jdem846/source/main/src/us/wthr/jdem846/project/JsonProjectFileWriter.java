package us.wthr.jdem846.project;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import us.wthr.jdem846.JDemResourceLoader;
import us.wthr.jdem846.image.SimpleGeoImage;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.rasterdata.RasterDataSource;
import us.wthr.jdem846.shapefile.ShapeFileReference;


public class JsonProjectFileWriter
{
	private static Log log = Logging.getLog(JsonProjectFileWriter.class);
	
	
	protected JsonProjectFileWriter()
	{
		
	}
	
	
	protected static JSONArray createSettingsObject(Map<String, String> globalOptions)
	{
		JSONArray settingsArray = new JSONArray();
		for (String key : globalOptions.keySet()) {
			String value = globalOptions.get(key);
			
			JSONObject settingsObject = new JSONObject();
			settingsObject.element("key", key);
			settingsObject.element("value", value);
			settingsArray.add(settingsObject);
		}
		return settingsArray;
	}
	
	
	
	
	protected static JSONObject createRasterObject(RasterDataSource rasterDataSource)
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.element("type", "raster");
		jsonObject.element("path", rasterDataSource.getFilePath());
		
		jsonObject.element("north", rasterDataSource.getDefinition().getNorth());
		jsonObject.element("south", rasterDataSource.getDefinition().getSouth());
		jsonObject.element("east", rasterDataSource.getDefinition().getEast());
		jsonObject.element("west", rasterDataSource.getDefinition().getWest());
		jsonObject.element("latitudeResolution", rasterDataSource.getDefinition().getLatitudeResolution());
		jsonObject.element("longitudeResolution", rasterDataSource.getDefinition().getLongitudeResolution());
		jsonObject.element("imageWidth", rasterDataSource.getDefinition().getImageWidth());
		jsonObject.element("imageHeight", rasterDataSource.getDefinition().getImageHeight());
		jsonObject.element("numBands", rasterDataSource.getDefinition().getNumBands());
		jsonObject.element("headerSize", rasterDataSource.getDefinition().getHeaderSize());
		jsonObject.element("dataType", rasterDataSource.getDefinition().getDataType());
		jsonObject.element("byteOrder", rasterDataSource.getDefinition().getByteOrder());
		jsonObject.element("interleavingType", rasterDataSource.getDefinition().getInterleavingType());
		jsonObject.element("noData", rasterDataSource.getDefinition().getNoData());
		jsonObject.element("locked", rasterDataSource.getDefinition().isLocked());

		
		
		
		return jsonObject;
	}
	
	protected static JSONObject createImageObject(SimpleGeoImage image)
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.element("type", "image");
		jsonObject.element("path", image.getImageFile());
		jsonObject.element("north", image.getNorth());
		jsonObject.element("south", image.getSouth());
		jsonObject.element("east", image.getEast());
		jsonObject.element("west", image.getWest());
		return jsonObject;
	}
	
	protected static JSONObject createShapeObject(ShapeFileReference shapeFileReq)
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.element("type", "shapefile");
		jsonObject.element("path", shapeFileReq.getPath());
		jsonObject.element("defId", shapeFileReq.getShapeDataDefinitionId());
		return jsonObject;
	}
	
	protected static JSONObject createModelGridObject(String modelGrid)
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.element("type", "modelGrid");
		jsonObject.element("path", modelGrid);
		return jsonObject;
	}
	
	protected static JSONArray createLayersObject(ProjectMarshall projectMarshall)
	{
		JSONArray layersArray = new JSONArray();
		
		for (RasterDataSource rasterDataSource : projectMarshall.getRasterFiles()) {
			JSONObject rasterObj = createRasterObject(rasterDataSource);
			layersArray.add(rasterObj);
		}
		
		for (ShapeFileReference shapeFileReq : projectMarshall.getShapeFiles()) {
			JSONObject shapeObj = createShapeObject(shapeFileReq);
			layersArray.add(shapeObj);
		}
		
		for (SimpleGeoImage image : projectMarshall.getImageFiles()) {
			JSONObject imageObj = createImageObject(image);
			layersArray.add(imageObj);
		}
		
		if (projectMarshall.getModelGrid() != null) {
			JSONObject modelGridObj = createModelGridObject(projectMarshall.getModelGrid());
			layersArray.add(modelGridObj);
		}
		
		return layersArray;
	}
	
	protected static JSONObject createProcessObject(ProcessMarshall processMarshall)
	{
		JSONObject processObject = new JSONObject();
		
		processObject.element("id", processMarshall.getId());
		processObject.element("options", createSettingsObject(processMarshall.getOptions()));
		
		return processObject;
	}
	
	protected static JSONArray createProcessesArray(ProjectMarshall projectMarshall)
	{
		JSONArray processesArray = new JSONArray();
		
		for (ProcessMarshall processMarshall : projectMarshall.getProcesses()) {
			
			processesArray.add(createProcessObject(processMarshall));
			
		}
		
		return processesArray;
	}
	
	
	protected static JSONObject createJsonObject(ProjectMarshall projectMarshall)
	{
		JSONObject jsonObject = new JSONObject();
		
		if (projectMarshall.getProjectType() != null) {
			jsonObject.element("type", projectMarshall.getProjectType().identifier());
		}
		
		if (projectMarshall.getScriptLanguage() != null) {
			jsonObject.element("scriptLanguage", projectMarshall.getScriptLanguage().text());
		}
		
		jsonObject.element("global", createSettingsObject(projectMarshall.getGlobalOptions()));
		jsonObject.element("processes", createProcessesArray(projectMarshall));
		jsonObject.element("layers", createLayersObject(projectMarshall));
		
		return jsonObject;
	}
	
	
	public static void writeProject(ProjectMarshall projectMarshall, String path) throws IOException
	{
		log.info("Writing project file to " + path);
		File file = JDemResourceLoader.getAsFile(path);
		OutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
		writeProject(projectMarshall, fos);
		fos.close();
		
	}
	
	
	public static void writeProject(ProjectMarshall projectMarshall, OutputStream out) throws IOException
	{
		
		
		JSONObject jsonObject = JsonProjectFileWriter.createJsonObject(projectMarshall);
		String json = jsonObject.toString(3);

		out.write(json.getBytes());
		
		//log.info(jsonObject.toString(3));
	}
	
}
