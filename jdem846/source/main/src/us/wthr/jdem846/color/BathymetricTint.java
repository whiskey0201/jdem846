/*
 * Copyright (C) 2011 Kevin M. Gill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.wthr.jdem846.color;

import us.wthr.jdem846.JDem846Properties;
import us.wthr.jdem846.exception.GradientLoadException;

//@DemColoring(name="us.wthr.jdem846.color.bathymetricTint.name", identifier="bathymetric-tint", needsMinMaxElevation=true)
public class BathymetricTint extends GradientColoring
{
	
	
	public BathymetricTint() throws GradientLoadException
	{
		super(JDem846Properties.getProperty("us.wthr.jdem846.color") + "/bathymetric-2.gradient");
	}
	
}
