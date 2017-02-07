/*
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
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
package de.dwslab.T2K.utils.diagnostics;

public class MemoryUtils {

    /**
     * Calls System.gc() several times, until freeMemory() returns stable values
     * @return
     */
    public static long waitGetFreeMemory() {
        // waits for free memory measurement to stabilize
      long init = Runtime.getRuntime().freeMemory(), init2;
      int count = 0;
      do {
          //System.out.println("waiting..." + init);
          System.gc();
          try { Thread.sleep(250); } catch (Exception x) { }
          init2 = init;
          init = Runtime.getRuntime().freeMemory();
          if (init == init2) ++ count; else count = 0;
      } while (count < 5);
      //System.out.println("ok..." + init);
      return init;
    }
    
}
