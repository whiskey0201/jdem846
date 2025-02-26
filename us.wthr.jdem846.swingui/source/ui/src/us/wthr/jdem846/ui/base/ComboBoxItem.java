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

package us.wthr.jdem846.ui.base;

public class ComboBoxItem<E>
{
	
	private String label;
	private E value;
	
	public ComboBoxItem(String label, E value)
	{
		this.label = label;
		this.value = value;
	}
	
	public String toString()
	{
		return label;
	}

	public String getLabel() 
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public E getValue()
	{
		return value;
	}

	public void setValue(E value) 
	{
		this.value = value;
	}
	
	
	
}
