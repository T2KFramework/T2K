/**
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of
							Mannheim (t2k@dwslab.de)
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
package de.mannheim.uni.matching;

import java.util.Collection;

public class TestInstance implements Comparable<TestInstance> {
	private int id;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	private String label;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	private Object type;
	public Object getType() {
		return type;
	}
	public void setType(Object type) {
		this.type = type;
	}
	public TestInstance(int id) {
		this.id = id;
	}
	public TestInstance(int id, String label) {
		this.id = id;
		this.label = label;
	}
	public TestInstance(int id, String label, Object type) {
		this.id = id;
		this.label = label;
		this.type = type;
	}
    public int compareTo(TestInstance arg0) {
        return Integer.compare(id, arg0.getId());
    }
    
    private Collection<String> list;
    public Collection<String> getList() {
        return list;
    }
    public void setList(Collection<String> list) {
        this.list = list;
    }
}
