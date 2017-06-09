package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.List;

public class ResourceVersionMigration extends VersionMigration<Resource> {

    @javax.annotation.Resource(name = "resource-operation")
    private IResourceOperation resourceOperation;

    @Override
    NodeTypeEnum getNodeTypeEnum() {
        return NodeTypeEnum.Resource;
    }

}
