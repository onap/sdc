/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.impl.migration.v1707;

import org.openecomp.sdc.asdctool.impl.migration.*;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("renameGraphPropertyKeysMigration")
public class RenameGraphPropertyKeys implements Migration1707Task {

    private final static Map<String, String> KEY_PROPERTIES_TO_RENAME;

    @Autowired
    private MigrationOperationUtils migrationUtils;

    static {
        KEY_PROPERTIES_TO_RENAME = new HashMap<>();
        KEY_PROPERTIES_TO_RENAME.put("attuid", GraphPropertiesDictionary.USERID.getProperty());
        KEY_PROPERTIES_TO_RENAME.put("pmatt", GraphPropertiesDictionary.PROJECT_CODE.getProperty());
        KEY_PROPERTIES_TO_RENAME.put("attContact", GraphPropertiesDictionary.CONTACT_ID.getProperty());
        KEY_PROPERTIES_TO_RENAME.put("attCreator", GraphPropertiesDictionary.CREATOR_ID.getProperty());
    }

    @Override
    public boolean migrate() {
        return migrationUtils.renamePropertyKeys(KEY_PROPERTIES_TO_RENAME);
    }

    @Override
    public String description() {
        return MigrationMsg.RENMAE_KEY_PROPERTIES_1707.getMessage();
    }
}
