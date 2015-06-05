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

package de.dwslab.T2K.index;

import java.util.Collection;
import java.util.List;

public interface IIndexSearcher<TDocument> {

	/**
	 * Searches for a single term
	 * @param query
	 * @return
	 */
	List<TDocument> search(String query);
	
	/**
	 * Searches for multiple terms
	 * @param query
	 * @return
	 */
	List<TDocument> search(Collection<String> query);
	
	/**
	 * Looks up a specific term (no normalisation is done)
	 * @param query
	 * @return
	 */
	List<TDocument> lookup(String query);
}
