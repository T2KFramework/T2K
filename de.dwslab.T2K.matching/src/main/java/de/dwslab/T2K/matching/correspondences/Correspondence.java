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
package de.dwslab.T2K.matching.correspondences;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import au.com.bytecode.opencsv.CSVWriter;
import de.dwslab.T2K.matching.MatchingAdapter;

/**
 * A correspondence is the result of a Matching operation and represents a mapping from an instance to a candidate
 * @author Oliver
 *
 * @param <T>
 */
public class Correspondence<T> {

	private T first;
	private T second;
	private double similarity;
	private boolean correct=false;
	private Object correctValue;
	private Object source;
	
	public T getFirst() {
		return first;
	}
	public void setFirst(T first) {
		this.first = first;
	}
	public T getSecond() {
		return second;
	}
	public void setSecond(T second) {
		this.second = second;
	}
	public double getSimilarity() {
		return similarity;
	}
	protected void setSimilarity(double similarity) {
		this.similarity = similarity;
	}
	public boolean isCorrect() {
        return correct;
    }
	public void setCorrect(boolean correct) {
        this.correct = correct;
    }
	public Object getCorrectValue() {
        return correctValue;
    }
	public void setCorrectValue(Object correctValue) {
        this.correctValue = correctValue;
    }
	public Object getSource() {
        return source;
    }
	public void setSource(Object source) {
        this.source = source;
    }
	
	public Correspondence(T first, T second, double similarity)
	{
		setFirst(first);
		setSecond(second);
		setSimilarity(similarity);
	}
	
	public static <T> void writeCollection(Collection<Correspondence<T>> collection, String file, MatchingAdapter<T> firstAdapter, MatchingAdapter<T> secondAdapter) {
        try {
            CSVWriter w = new CSVWriter(new FileWriter(file));
            
            String[] header = new String[6];
            header[0] = "webtable column";
            header[1] = "dbpedia property uri";
            header[2] = "similarity";
            header[3] = "correct";
            header[4] = "correct value";
            header[5] = "source";
            w.writeNext(header);
            
            for(Correspondence<T> colC : collection) {
                
                String[] values = new String[6];
                values[0] = firstAdapter.getLabel(colC.getFirst()).toString();
                values[1] = secondAdapter.getLabel(colC.getSecond()).toString();
                values[2] = Double.toString(colC.getSimilarity());
                values[3] = Boolean.toString(colC.isCorrect());
                if(colC.getCorrectValue()!=null) {
                    values[4] = colC.getCorrectValue().toString();
                }
                values[5] = colC.getSource().toString();
                
                w.writeNext(values);
            }
            
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
