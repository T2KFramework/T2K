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
package de.dwslab.T2K.utils.timer;

import java.util.List;

/**
 * Used during Multi-Threading
 * @author Oliver
 *
 */
public class MultiTimer
	extends AggregatingTimer
{

	private AggregatingTimer _object;
	
	public MultiTimer(AggregatingTimer timer) {
		_object = timer;
		start();
		
		setCurrent(_object);
	}
	
	@Override
	public List<Timer> getChildren() {
		return _object.getChildren();
	}
	
	@Override
	protected void addChild(Timer child) {
		_object.addChild(child);
	}
	
	@Override
	protected boolean hasChild(Timer child) {
		return _object.hasChild(child);
	}
	
	@Override
	protected void start() {
        _start = System.currentTimeMillis();
	}
	
	@Override
	public void stop() {
	    synchronized (_object) {
	        setEnd();
	        _object.addDuration(super.getDuration());
	        
	        if(!_object.isSilent()) {
	            if(isVerbose()) {
	                System.out.println(_object.printEnd());
	            }
	        }
        }
	}
	
	@Override
	public String getName() {
		return _object.getName();
	}
	
	@Override
	protected StringBuilder print(String prefix) {
		return _object.print(prefix);
	}
	
	@Override
	protected void addDuration(long duration) {
		synchronized (_object) {
		    _object.addDuration(duration);
        }
	}
	
	@Override
	protected String formatValue() {
		 return _object.formatValue();
	}
	
	@Override
	public long getDuration() {
		return _object.getDuration();
	}
	
	@Override
	public boolean equals(Object obj) {
		return _object.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return _object.hashCode();
	}
	
	@Override
	public String toString() {
		return _object.toString();
	}
}
