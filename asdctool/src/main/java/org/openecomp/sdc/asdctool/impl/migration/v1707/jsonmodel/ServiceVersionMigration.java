package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.IServiceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.List;

public class ServiceVersionMigration extends VersionMigration<Service> {

    @javax.annotation.Resource(name = "service-operation")
    private IServiceOperation serviceOperation;

    @Override
    NodeTypeEnum getNodeTypeEnum() {
        return NodeTypeEnum.Service;
    }

}
