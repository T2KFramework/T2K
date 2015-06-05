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
package de.dwslab.T2K.utils.data;

public class ValueAggregator {
	double min, max, sum;
	long cnt;
	
	public ValueAggregator()
	{
		reset();
	}
	
	public void reset()
	{
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		sum = 0;
		cnt = 0;
	}
	
	public void AddValue(double d)
	{
	    if(d==Double.NaN) {
	        return;
	    }
	    
		if(d<min)
			min = d;
		
		if(d>max)
			max = d;
		
		sum+=d;
		cnt++;
	}
	
	public double getMin()
	{
		if(cnt==0)
			return 0;
		else
			return min;
	}
	
	public double getMax()
	{
		if(cnt==0)
			return 0;
		else
			return max;
	}
	
	public double getAvg()
	{
		if(cnt==0)
			return 0;
		else
			return sum / (double)cnt;
	}
	
	public double getSum()
	{
		return sum;
	}
	
	public long getCount()
	{
		return cnt;
	}
}
