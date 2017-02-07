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
package de.dwslab.T2K.utils.timer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.time.DurationFormatUtils;

public class AggregatingTimer
	extends Timer
{

	private AtomicLong _total = new AtomicLong();
	private AtomicInteger _count = new AtomicInteger();
	
	public AggregatingTimer(String name) {
		super(name);
	}
	
	public AggregatingTimer(String name, Timer parent) {
		super(name, parent);
	}

	protected AggregatingTimer()
	{
		
	}
	
	protected void addDuration(long duration)
	{
		//_total += duration;
	    _total.addAndGet(duration);
		//_count++;
	    _count.incrementAndGet();
	}
	
	@Override
	public void stop() {
		super.stop();
		
		addDuration(getDuration());
	}
	
	@Override
	protected String formatValue() {
		if(_total.get()==0)
		{
			return super.formatValue();
		}
		else
		{
			String value="", valueAvg="";
			
			value = DurationFormatUtils.formatDuration(_total.get(), "HH:mm:ss.S");
			valueAvg = DurationFormatUtils.formatDuration(_total.get()/(long)_count.get(), "HH:mm:ss.S");
			
			return getName() + ": " + value + "(" + _count + " times; " + valueAvg + " on avg.)";
		}
	}
}
