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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class TimeoutOperation<T> {

	public abstract T doOperation();
	
	public T run(long timeoutMillis)
	{
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(new Callable<T>() {

			public T call() throws Exception {
				return doOperation();
			}
		});

        T result = null;
        
        try {
            
            result = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

        executor.shutdownNow();
        
        return result;
	}
	
	
	public static void main(String[] args) {
		TimeoutOperation<String> t = new TimeoutOperation<String>()
		{

			@Override
			public String doOperation() {
				try {
					Thread.sleep(10000);
					
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
				
				return "hello";
			}
			
		};
		
		System.out.println(t.run(100));
	}
}
