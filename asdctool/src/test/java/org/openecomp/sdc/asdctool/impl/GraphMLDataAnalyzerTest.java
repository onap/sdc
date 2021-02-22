/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.asdctool.impl;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openecomp.sdc.asdctool.impl.GraphMLDataAnalyzer.EXCEL_EXTENSION;
import static org.openecomp.sdc.asdctool.impl.GraphMLDataAnalyzer.GRAPH_ML_EXTENSION;

public class GraphMLDataAnalyzerTest {

    public static final String FILE_NAME = "export";

    @Test
    public void testAnalyzeGraphMLDataNoFile() {
        String[] args = new String[]{"noExistFile"};

        // default test
        GraphMLDataAnalyzer graph = new GraphMLDataAnalyzer();
        String result = graph.analyzeGraphMLData(args);

        assertNull(result);
    }

    @Test
    public void testAnalyzeGraphMLData() {
        String path = getClass().getClassLoader().getResource(FILE_NAME + GRAPH_ML_EXTENSION).getPath();
        String[] args = new String[]{path};

        // default test
        GraphMLDataAnalyzer graph = new GraphMLDataAnalyzer();
        String result = graph.analyzeGraphMLData(args);

        assertNotNull(result);
        assertTrue(result.endsWith(EXCEL_EXTENSION));
    }
}
