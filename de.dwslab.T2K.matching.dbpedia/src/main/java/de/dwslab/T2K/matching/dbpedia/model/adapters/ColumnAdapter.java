/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.tableprocessor.model.TableColumn;

/**
 *
 * @author domi
 */
public class ColumnAdapter extends EvaluationAdapter<TableColumn>{

    @Override
    public Object getUniqueIdentifier(TableColumn instance) {        
        int index = -1;
        for(int i=0; i< instance.getTable().getColumns().size();i++) {
            if(instance.equals(instance.getTable().getColumns().get(i))) {
                index = i;
                break;
            }
        }
        return index;
        //return instance.getHeader().toLowerCase().replace(".", "");
    }
    
}
