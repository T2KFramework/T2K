/**
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
package de.dwslab.T2K.utils.io;

import java.io.BufferedWriter;
import java.io.FileWriter;

import au.com.bytecode.opencsv.CSVWriter;

public class SynchronizedCsvWriter
 extends SynchronizedWriter<String[]>
{

	private CSVWriter writer;
	private long length=0;
	
	public SynchronizedCsvWriter(String file) throws Exception {
		super(file);
	}

	@Override
	protected void createWriter(String file, boolean append) throws Exception {
		writer = new CSVWriter(new BufferedWriter(new FileWriter(file, append)));
		length=0;
	}

	@Override
	public long getApproximateLength() throws Exception {
		if(writer!=null)
			return length;
		else
			return 0;
	}
	
	@Override
	protected void writeData(String[] data) throws Exception {
		writer.writeNext(data);
		
		for(String s : data)
			if(s!=null)
				length += s.length();
		
		length += (data.length-1); // separator char
	}

	@Override
	protected void flushWriter() throws Exception {
		writer.flush();
	}

	@Override
	protected void closeWriter() throws Exception {
		writer.close();
	}

}
