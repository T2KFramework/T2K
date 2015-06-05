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
import java.util.LinkedList;

public class TestEntity extends TestInstance {

	public TestEntity(int id) {
		super(id);
		parts = new LinkedList<TestInstance>();
	}
	public TestEntity(int id, String label) {
		super(id, label);
		parts = new LinkedList<TestInstance>();
	}
	public TestEntity(int id, String label, Object type) {
		super(id, label, type);
		parts = new LinkedList<TestInstance>();
	}

	private Collection<TestInstance> parts;
	public Collection<TestInstance> getParts() {
		return parts;
	}
	public void setParts(Collection<TestInstance> parts) {
		this.parts = parts;
	}
	
}
