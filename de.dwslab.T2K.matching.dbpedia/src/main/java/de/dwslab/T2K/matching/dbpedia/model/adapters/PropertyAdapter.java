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
public class PropertyAdapter extends EvaluationAdapter<TableColumn>  {
    @Override
    public Object getUniqueIdentifier(TableColumn instance) {
        return instance.getHeader();
    }
}
