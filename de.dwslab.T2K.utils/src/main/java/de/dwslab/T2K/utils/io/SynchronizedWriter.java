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

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class SynchronizedWriter<T>
	implements Runnable
{
	private ConcurrentLinkedQueue<T> input;
	private boolean shouldStop;
	private boolean stopAfterFlush;
	private Thread thread;
	
	public SynchronizedWriter(String file) throws Exception 
	{
	       input = new ConcurrentLinkedQueue<T>();
	        shouldStop=false;
	        stopAfterFlush=false;
	        createWriter(file, false);
	        
	        thread = new Thread(this);
	        thread.start();
	}
	
	public SynchronizedWriter(String file, boolean append) throws Exception
	{	
		input = new ConcurrentLinkedQueue<T>();
		shouldStop=false;
		stopAfterFlush=false;
		createWriter(file, append);
		
		thread = new Thread(this);
		thread.start();
	}

	protected abstract void createWriter(String file, boolean append) throws Exception;
	protected abstract void writeData(T data) throws Exception;
	protected abstract void flushWriter() throws Exception;
	protected abstract void closeWriter() throws Exception;
	public abstract long getApproximateLength() throws Exception;
	
	public void write(T data)
	{
		input.add(data);
	}
	
	public void stop()
	{
		shouldStop=true;
	}
	
	public void stopAndBlock() throws InterruptedException
	{
		stop();
		thread.join();
	}

	public void stopAfterFlush()
	{
		stopAfterFlush=true;
	}
	
	public void flushAndBlock() throws InterruptedException
	{
		stopAfterFlush();
		thread.join();
	}
	
	public void run() {
		T data = null;
		
		long sleep = 100;
		
		while(!shouldStop && !(stopAfterFlush && input.isEmpty()))
		{
			data = input.poll();
			
			if(data!=null)
			{
				sleep = 100;
				
				try {
					writeData(data);
				} catch (Exception e) {
					e.printStackTrace();
					// failed to write, add to queue again ...
					write(data);
				}
			} else
				try {
					flushWriter();
					
					Thread.sleep(sleep);
					sleep *= 2; // if the input queue is not filled, increase the time to wait before the next poll
					sleep = Math.max(sleep, 2000);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		

		try {
			closeWriter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
