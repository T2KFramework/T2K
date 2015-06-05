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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.tableprocessor;

import de.dwslab.T2K.tableprocessor.model.TableColumn.ColumnDataType;
import de.dwslab.T2K.units.Unit_domi;

/**
 *
 * @author domi
 */
public class ColumnType {
    
    private ColumnDataType type;
    private Unit_domi unit;
    
    public ColumnType(ColumnDataType type, Unit_domi unit) {
        this.type = type;
        this.unit = unit;
    }

    /**
     * @return the type
     */
    public ColumnDataType getType() {
        return type;
    }

    /**
     * @return the unit
     */
    public Unit_domi getUnit() {
        return unit;
    }
    
}
