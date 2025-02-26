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

package us.wthr.jdem846.exception;

/** Thrown for file format exceptions.
 * 
 * @author Kevin M. Gill
 *
 */
@SuppressWarnings("serial")
public class InvalidFileFormatException extends Exception
{
	/** Extension of the invalid file.
	 * 
	 */
	private String extension;
	
	public InvalidFileFormatException(String extension)
	{
		this.extension = extension;
	}
	
	/** Extension of the invalid file.
	 * 
	 * @return
	 */
	public String getExtension()
	{
		return extension;
	}
	
}
