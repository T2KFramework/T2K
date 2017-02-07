/*
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dwslab.T2K.utils.data.string;

import java.util.Collection;

public class StringUtils {

	public static String join(Collection<?> values, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		
		boolean first=true;
		for(Object value : values)
		{
			if(!first)
				sb.append(delimiter);
			
			sb.append(value.toString());
			
			first = false;
		}
		
		return sb.toString();
	}
	
	/**
	 * Checks if the string passed as first parameter contains any of the other strings passed as second parameter
	 * @param value
	 * @param testValues
	 * @return
	 */
	public static boolean containsAny(String value, Collection<String> testValues)
	{
		if(value==null)
			return false;
		
		for (String s : testValues)
			if (value.contains(s))
				return true;
		return false;
	}
}
