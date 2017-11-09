package de.dwslab.T2K.utils.concurrent;

import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

public class RunnableProgressReporter
	implements Runnable
{

	private ThreadPoolExecutor pool;
	private Thread thread;
	private Task userTask;
	private boolean stop;
	private Logger logger;
	private String message;
	private boolean reportIfStuck = true;
	
	public RunnableProgressReporter()
	{
		
		logger = Logger.getLogger(RunnableProgressReporter.class);
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
	
	public void setReportIfStuck(boolean reportIfStuck) {
        this.reportIfStuck = reportIfStuck;
    }
	
	public boolean getReportIfStuck() {
        return reportIfStuck;
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
					System.err.println(String.format("%s%,d of %,d tasks completed after %s (%d/%d active threads). Avg: %.2f items/s, Current: %.2f items/s, %s left.", usrMsg, done, tasks, ttl, pool.getActiveCount(), pool.getPoolSize(), itemsPerSecAvg, itemsPerSecNow, remaining));
	
					if(userTask!=null)
						userTask.execute();
					
					if(done == last) {
					    stuckIterations++;
					} else {
					    last = done;
					    stuckIterations=0;
					}
					
					if(stuckIterations>=3 && reportIfStuck) {
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
		    e.printStackTrace();
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
