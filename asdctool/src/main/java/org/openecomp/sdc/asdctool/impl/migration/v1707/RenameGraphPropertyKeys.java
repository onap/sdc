package org.openecomp.sdc.asdctool.impl.migration.v1707;

import org.openecomp.sdc.asdctool.impl.migration.MigrationMsg;
import org.openecomp.sdc.asdctool.impl.migration.Migration;
import org.openecomp.sdc.asdctool.impl.migration.MigrationOperationUtils;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("renameGraphPropertyKeysMigration")
public class RenameGraphPropertyKeys implements Migration {

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
