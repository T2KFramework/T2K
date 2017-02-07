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
package de.dwslab.T2K.utils.log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class DataLogger {

	private boolean isEnabled;
	
	public DataLogger()
	{
		isEnabled=true;
	}

	public void logMap(Map<?,?> map, String name)
	{
		if(isEnabled)
		{
			try {
				BufferedWriter w = new BufferedWriter(new FileWriter(name + ".csv"));
				
				w.write("key\tvalue\n");
				
				for(Entry<?, ?> e : map.entrySet())
					w.write(e.getKey().toString() + "\t" + e.getValue().toString() + "\n");
				
				w.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void logMapMap(Map<?,?> map, String name)
	{
		if(isEnabled)
		{
			try {
				BufferedWriter w = new BufferedWriter(new FileWriter(name + ".csv"));
				
				w.write("key1\tkey2\tvalue\n");
				
				for(Entry<?, ?> e0 : map.entrySet())
					for(Entry<?,?> e : ((Map<?,?>)e0.getValue()).entrySet())
						w.write(e0.getKey().toString() + "\t" + e.getKey().toString() + "\t" + e.getValue().toString() + "\n");
				
				w.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
