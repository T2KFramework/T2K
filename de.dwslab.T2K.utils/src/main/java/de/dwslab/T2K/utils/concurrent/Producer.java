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

import java.util.concurrent.ThreadPoolExecutor;

import de.dwslab.T2K.utils.concurrent.Consumer;

public abstract class Producer<T> {

	public abstract void execute();
	
	private ThreadPoolExecutor pool;
	private Consumer<T> consumer;
	protected boolean runsSingleThreaded = false;
	protected void setRunSingleThreaded(boolean singleThreaded) {
	    runsSingleThreaded = singleThreaded;
	}
	protected boolean isSingleThreaded() {
	    return runsSingleThreaded;
	}
	
	public void setPool(ThreadPoolExecutor pool)
	{
		this.pool = pool;
	}
	
	public void setConsumer(Consumer<T> consumer)
	{
		this.consumer = consumer;
	}
	
	protected void produce(final T value)
	{
	    if(!runsSingleThreaded) {
    		pool.execute(new Runnable() {
    			
    			public void run() {
    				consumer.execute(value);
    			}
    		});
	    } else {
	        consumer.execute(value);
	    }
	}
	
}
