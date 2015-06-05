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
package de.mannheim.uni.matching.blocking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.dwslab.T2K.matching.similarity.signatures.NGramsSignatureGenerator;
import de.dwslab.T2K.matching.similarity.signatures.PrefixFiltering;
import junit.framework.TestCase;

public class SignatureBlockingTest extends TestCase {

    public void testBlocking() {
//        SignatureBlocking<String> blocking = new SignatureBlocking<>();
//        
//        blocking.setSignatureGenerator(new NGramsSignatureGenerator(2));
//        blocking.setSignatureFilter(new PrefixFiltering<String>(2.4));
//        
//        // examples from http://dbgroup.cs.tsinghua.edu.cn/wangjn/papers/icde2011-fastjoin.pdf
//        
//        List<String> candidates = new ArrayList<>();
//        candidates.add("kobe and trancy");
//        //candidates.add("trcy macgrady mvp");
//        candidates.add("kobe bryant age");
//        candidates.add("mvp tracy mcgrady");
//        
//        blocking.generateCandidateSignatures(candidates);
//        Collection<String> blocked = blocking.getCandidates("trcy macgrady mvp", candidates);
//        
//        assertEquals(1, blocked.size());
//        assertEquals("mvp tracy mcgrady", blocked.iterator().next());
        
    }
    
}
