/*
 * ============LICENSE_START=======================================================
 * GAB
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

package org.onap.sdc.gab;

import java.io.IOException;
import org.onap.sdc.gab.model.GABQuery;
import org.onap.sdc.gab.model.GABQuery.GABQueryType;
import org.onap.sdc.gab.model.GABResults;
import org.onap.sdc.gab.yaml.GABYamlParser;
import org.onap.sdc.gab.yaml.YamlParser;

public class GABServiceImpl implements GABService {

    public GABResults searchFor(GABQuery gabQuery) throws IOException {
        try (GABYamlParser gabYamlParser = new GABYamlParser(new YamlParser())) {
            return parse(gabQuery, gabYamlParser).filter(gabQuery.getFields()).collect();
        }
    }

    private GABYamlParser parse(GABQuery gabQuery, GABYamlParser gabYamlParser){
        return gabQuery.getType() == GABQueryType.PATH ?
            gabYamlParser.parseFile(gabQuery.getDocument()) : gabYamlParser.parseContent(gabQuery.getDocument());
    }
}
