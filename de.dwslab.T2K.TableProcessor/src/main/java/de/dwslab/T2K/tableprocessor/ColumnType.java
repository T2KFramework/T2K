/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.tableprocessor;

import de.dwslab.T2K.tableprocessor.model.TableColumn.ColumnDataType;
import de.dwslab.T2K.units.Unit;

/**
 *
 * @author domi
 */
public class ColumnType {
    
    private ColumnDataType type;
    private Unit unit;
    
    public ColumnType(ColumnDataType type, Unit unit) {
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
    public Unit getUnit() {
        return unit;
    }
    
}
