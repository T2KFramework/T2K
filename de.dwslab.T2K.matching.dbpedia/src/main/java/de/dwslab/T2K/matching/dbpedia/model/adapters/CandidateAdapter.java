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
package de.dwslab.T2K.matching.dbpedia.model.adapters;

import java.util.List;

import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.utils.data.string.StringUtils;

/**
 *
 * @author domi
 */
public class CandidateAdapter extends EvaluationAdapter<TableRow> {

        @Override
        public Object getUniqueIdentifier(TableRow instance) {
            if(instance!=null) {
                return Integer.toString(instance.getRowIndexInFile());
//                
//                if(instance.getKey() instanceof List) {
//                    return "{" + StringUtils.join((List)instance.getKey(), "|") + "}";
//                } else {
//                    return instance.getKey().toString().replaceAll("\\s", " ");
//                }
            } else {
                return null;
            }
        }
    }
