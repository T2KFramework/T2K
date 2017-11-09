/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.tableprocessor.model.Table;

/**
 *
 * @author domi
 */
public class DBpediaClassAdapter extends EvaluationAdapter<Table> {

    @Override
    public Object getUniqueIdentifier(Table instance) {
        return instance.getHeader().split("\\.")[0].toLowerCase();
    }
    
}
