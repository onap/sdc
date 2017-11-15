package org.openecomp.sdc.vendorsoftwareproduct.impl.mock;

import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;

public class NoSqlDbFactoryMock extends NoSqlDbFactory {


    @Override
    public NoSqlDb createInterface() {
        return null;
    }
}
