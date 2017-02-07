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
package de.dwslab.T2K.utils.data;

public class Quad<T, U, V, W> {

	private T first;
	private U second;
	private V third;
	private W fourth;
	
	public Quad(T first, U second, V third, W fourth) {
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}
	
	public T getFirst() {
		return first;
	}
	
	public void setFirst(T first) {
		this.first = first;
	}
	
	public U getSecond() {
		return second;
	}
	
	public void setSecond(U second) {
		this.second = second;
	}
	
	public V getThird() {
		return third;
	}
	
	public void setThird(V third) {
		this.third = third;
	}
	
	public W getFourth() {
		return fourth;
	}
	
	public void setFourth(W fourth) {
		this.fourth = fourth;
	}
}
