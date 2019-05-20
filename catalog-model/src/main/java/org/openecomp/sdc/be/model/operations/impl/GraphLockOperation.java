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

package org.openecomp.sdc.be.model.operations.impl;

import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component("graph-lock-operation")
public class GraphLockOperation implements IGraphLockOperation {
    private static final Logger log = Logger.getLogger(GraphLockOperation.class.getName());

    @javax.annotation.Resource
    private JanusGraphGenericDao janusGraphGenericDao;

    public GraphLockOperation() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openecomp.sdc.be.model.operations.impl.IGraphLockOperation# lockResource(java.lang.String, org.openecomp.sdc.be.model.operations.api.IResourceOperation)
     */
    @Override
    public StorageOperationStatus lockComponent(String componentId, NodeTypeEnum nodeType) {
        log.info("lock resource with id {}", componentId);
        JanusGraphOperationStatus lockElementStatus = null;
        try {
            lockElementStatus = janusGraphGenericDao.lockElement(componentId, nodeType);
        } catch (Exception e) {
            lockElementStatus = JanusGraphOperationStatus.ALREADY_LOCKED;

        }

        return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(lockElementStatus);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.openecomp.sdc.be.model.operations.impl.IGraphLockOperation# unlockResource(java.lang.String, org.openecomp.sdc.be.model.operations.api.IResourceOperation)
     */
    @Override
    public StorageOperationStatus unlockComponent(String componentId, NodeTypeEnum nodeType) {
        JanusGraphOperationStatus lockElementStatus = janusGraphGenericDao
            .releaseElement(componentId, nodeType);
        return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(lockElementStatus);
    }

    @Override
    public StorageOperationStatus unlockComponentByName(String name, String componentId, NodeTypeEnum nodeType) {
        JanusGraphOperationStatus
            lockElementStatus = janusGraphGenericDao.releaseElement(name, nodeType);
        return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(lockElementStatus);
    }

    @Override
    public StorageOperationStatus lockComponentByName(String name, NodeTypeEnum nodeType) {
        log.info("lock resource with name {}", name);
        JanusGraphOperationStatus lockElementStatus = null;
        try {

            lockElementStatus = janusGraphGenericDao.lockElement(name, nodeType);

        } catch (Exception e) {
            lockElementStatus = JanusGraphOperationStatus.ALREADY_LOCKED;

        }

        return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(lockElementStatus);

    }
}
