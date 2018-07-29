package org.openecomp.sdc.be.components.impl;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;


@org.springframework.stereotype.Component
public class ComponentLocker  {

    private static final Logger log = Logger.getLogger(ComponentLocker.class.getName());
    private final GraphLockOperation graphLockOperation;

    public ComponentLocker(GraphLockOperation graphLockOperation) {
        this.graphLockOperation = graphLockOperation;
    }

    public void lock(String id, ComponentTypeEnum componentType) {
        lock(id, componentType.getNodeType());
    }

    public void lock(Component component) {
        doLockComponent(component.getUniqueId(), component.getComponentType().getNodeType());
    }

    public void lock(String id, NodeTypeEnum nodeTypeEnum) {
        doLockComponent(id, nodeTypeEnum);
    }

    private void doLockComponent(String id, NodeTypeEnum nodeTypeEnum) {
        log.debug("#doLockComponent - locking component {} of type {}", id, nodeTypeEnum);
        StorageOperationStatus storageOperationStatus = graphLockOperation.lockComponent(id, nodeTypeEnum);
        if (storageOperationStatus != StorageOperationStatus.OK) {
     		log.debug("#doLockComponent - failed to lock component {} with status {}", id, storageOperationStatus);
            throw new StorageException(storageOperationStatus);
        }
    }

    public void unlock(String id, ComponentTypeEnum componentType) {
        unlock(id, componentType.getNodeType());
    }

    public void unlock(String id, NodeTypeEnum nodeTypeEnum) {
        log.debug("#unlock - unlocking component {} of type {}", id, nodeTypeEnum);
        StorageOperationStatus storageOperationStatus = graphLockOperation.unlockComponent(id, nodeTypeEnum);
        if (storageOperationStatus != StorageOperationStatus.OK) {
            log.debug("#unlock - failed to unlock component {} with status {}", id, storageOperationStatus);
            throw new StorageException(storageOperationStatus, id);
        }
    }
}
