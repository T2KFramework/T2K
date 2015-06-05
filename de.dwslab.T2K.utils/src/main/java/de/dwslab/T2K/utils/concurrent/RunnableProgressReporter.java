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
package de.dwslab.T2K.utils.concurrent;

import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.time.DurationFormatUtils;
//import org.apache.log4j.Logger;

public class RunnableProgressReporter
	implements Runnable
{

	private ThreadPoolExecutor pool;
	private Thread thread;
	private Task userTask;
	private boolean stop;
	//private Logger logger;
	private String message;
	
	public RunnableProgressReporter()
	{
		
		//logger = Logger.getLogger(RunnableProgressReporter.class);
	}
	
	public Task getUserTask() {
		return userTask;
	}
	
	public void setUserTask(Task userTask) {
		this.userTask = userTask;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public ThreadPoolExecutor getPool() {
		return pool;
	}

	public void setPool(ThreadPoolExecutor pool) {
		this.pool = pool;
	}
	
	public void run() {
		try
		{
			long start = System.currentTimeMillis();
			long tasks = pool.getTaskCount();
			long done = pool.getCompletedTaskCount();
			int stuckIterations = 0;;
			long last = 0;
			while(!stop)
			{
			    long lastTime = System.currentTimeMillis();
				Thread.sleep(10000);
				
				if(!stop)
				{
					tasks = pool.getTaskCount();
					done = pool.getCompletedTaskCount();
					
					long soFar = System.currentTimeMillis() - start;
					long pauseTime = System.currentTimeMillis() - lastTime;
					//long perItem = (long) (((float) soFar) / done);
					long left = (long) (((float) soFar / done) * (tasks - done));
					float itemsPerSecAvg = (float)done / (float)(soFar / 1000.0f);
					float itemsPerSecNow = (float)(done - last) / (pauseTime / 1000.0f);
					
					if((((float) soFar) / done)==Float.POSITIVE_INFINITY)
					{
						//perItem = -1;
						left = -1;
					}
					String ttl = DurationFormatUtils.formatDuration(soFar, "HH:mm:ss.S");
					//String each = DurationFormatUtils.formatDuration(perItem, "HH:mm:ss.S");
					String remaining = DurationFormatUtils.formatDuration(left, "HH:mm:ss.S");
					
					String usrMsg = message==null ? "" : message + ": ";
					//logger.info(usrMsg + done + " of " + tasks + " tasks completed after " + ttl + " (" + pool.getActiveCount() + "/" + pool.getPoolSize() + " active threads). " + each + " per item, " + remaining + " left.");
					//System.err.println(usrMsg + done + " of " + tasks + " tasks completed after " + ttl + " (" + pool.getActiveCount() + "/" + pool.getPoolSize() + " active threads). " + each + " per item, " + remaining + " left.");
					//System.err.println(String.format("%s%,d of %,d tasks completed after %s (%d/%d active threads). %s per item, %s left.", usrMsg, done, tasks, ttl, pool.getActiveCount(), pool.getPoolSize(), each, remaining));
					System.err.println(String.format("%s%,d of %,d tasks completed after %s (%d/%d active threads). Avg: %.4f items/s, Current: %.4f items/s, %s left.", usrMsg, done, tasks, ttl, pool.getActiveCount(), pool.getPoolSize(), itemsPerSecAvg, itemsPerSecNow, remaining));
	
					if(userTask!=null)
						userTask.execute();
					
					if(done == last) {
					    stuckIterations++;
					} else {
					    last = done;
					    stuckIterations=0;
					}
					
					if(stuckIterations>=3) {
					    System.err.println("ThreadPool seems to be stuck!");
					    int threadCnt = 0;
					    for(Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
					        if(e.getKey().getName().contains("Parallel")) {
					            threadCnt++;
					            
					            System.err.println(e.getKey().getName());
					            
					            for(StackTraceElement elem : e.getValue()) {
					                System.err.println("\t" + elem.toString());
					            }
					            
					        }
					    }
					    
					    System.err.println(String.format("%s %d Parallel.X threads --- %d total", pool.isTerminated() ? "[pool terminated]" : "", threadCnt, Thread.getAllStackTraces().size()));
					}
				}
			}
		}
		catch(Exception e)
		{
		}
	}
	
	public void start()
	{
		stop = false;
		thread = new Thread(this);
		thread.start();
	}
	
	public void stop()
	{
		stop = true;
	}

}
