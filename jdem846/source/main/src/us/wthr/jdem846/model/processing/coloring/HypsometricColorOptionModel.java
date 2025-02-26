package us.wthr.jdem846.model.processing.coloring;

import us.wthr.jdem846.model.OptionModel;
import us.wthr.jdem846.model.annotations.ProcessOption;

public class HypsometricColorOptionModel implements OptionModel
{

	private String colorTint = "hypsometric-tint";

	public HypsometricColorOptionModel()
	{

	}

	@ProcessOption(id = "us.wthr.jdem846.model.HypsometricColorOptionModel.colorTint", label = "Color Tinting", tooltip = "", visible = true, listModel = ColorTintsListModel.class)
	public String getColorTint()
	{
		return colorTint;
	}

	public void setColorTint(String colorTint)
	{
		this.colorTint = colorTint;
	}

	public HypsometricColorOptionModel copy()
	{
		HypsometricColorOptionModel copy = new HypsometricColorOptionModel();

		copy.colorTint = this.colorTint;

		return copy;
	}

}
