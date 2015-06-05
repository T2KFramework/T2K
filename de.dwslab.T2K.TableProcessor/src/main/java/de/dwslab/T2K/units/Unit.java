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
package de.dwslab.T2K.units;

import java.util.List;

/**
 * @author petar
 *
 */
public class Unit
	implements java.io.Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 72130226382025936L;

	private String name;

	private List<SubUnit> subunits;

	private SubUnit mainUnit;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SubUnit> getSubunits() {
		return subunits;
	}

	public void setSubunits(List<SubUnit> subunits) {
		this.subunits = subunits;
	}

	public SubUnit getMainUnit() {
		return mainUnit;
	}

	public void setMainUnit(SubUnit mainUnit) {
		this.mainUnit = mainUnit;
	}

	public Unit() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
