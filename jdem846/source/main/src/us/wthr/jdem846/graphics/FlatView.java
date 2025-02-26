package us.wthr.jdem846.graphics;

import us.wthr.jdem846.gis.exceptions.MapProjectionException;
import us.wthr.jdem846.gis.projections.MapPoint;
import us.wthr.jdem846.math.Vector;

public class FlatView extends AbstractView implements View
{

	private MapPoint mapPoint = new MapPoint();
	
	@Override
	public void project(double latitude, double longitude, double elevation, Vector point)
	{
		if (scaler != null) {
			elevation = scaler.scale(elevation);
		}
		
		
		if (this.mapProjection != null) {
			try {
				this.mapProjection.getPoint(latitude, longitude, elevation, mapPoint);
			} catch (MapProjectionException ex) {
				// TODO Add error handling
				ex.printStackTrace();
			}
			longitude = mapPoint.column;
			latitude = mapPoint.row;
			
		} 

		point.x = -(0.5 - longitudeToColumn(longitude));
		point.z = (0.5 / (this.resolution / 1000.0)) - ((getMaxElevation() - elevation) / (getMaxElevation() - getMinElevation()) / (this.resolution / 1000.0));
		point.z = (!Double.isNaN(point.z)) ? point.z : 0.0;
		point.y = (0.5 - latitudeToRow(latitude));
		
	}
	
	protected double latitudeToRow(double latitude)
	{
		return (((this.north - latitude) / (this.north - this.south)));
	}
	
	protected double longitudeToColumn(double longitude)
	{
		return (((longitude - this.west) / (this.east - this.west)));
	}
	
	
	/*
	protected double latitudeToRow(double latitude)
	{
		return (this.height * ((this.north - latitude) / (this.north - this.south)));
	}
	
	protected double longitudeToColumn(double longitude)
	{
		return (this.width * ((longitude - this.west) / (this.east - this.west)));
	}
	*/
	
	@Override
	public double radiusTrue()
	{
		return 0.5;
	}
	
	@Override
	public double radius()
	{
		// TODO Auto-generated method stub
		return 0.5;
	}

	@Override
	public double horizFieldOfView()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double elevationFromSurface()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double nearClipDistance()
	{
		// TODO Auto-generated method stub
		return scaleElevation(modelGrid.getMaximum());
	}

	@Override
	public double farClipDistance()
	{
		// TODO Auto-generated method stub
		return scaleElevation(modelGrid.getMinimum());
	}

	@Override
	public double eyeZ()
	{
		// TODO Auto-generated method stub
		return 0;
	}


}
