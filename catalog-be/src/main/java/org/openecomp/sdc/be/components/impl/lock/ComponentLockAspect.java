package org.openecomp.sdc.be.components.impl.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.openecomp.sdc.be.components.impl.ComponentLocker;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class ComponentLockAspect {

    private static final Logger log = Logger.getLogger(ComponentLockAspect.class);
    private final ComponentLocker componentLocker;

    public ComponentLockAspect(ComponentLocker componentLocker) {
        this.componentLocker = componentLocker;
    }

    public Object lock(ProceedingJoinPoint proceedingJoinPoint, String componentId, ComponentTypeEnum componentType) throws Throwable {
        return surroundWithLockUnlock(proceedingJoinPoint, componentId, componentType.getNodeType());
    }

    private Object surroundWithLockUnlock(ProceedingJoinPoint proceedingJoinPoint, String componentId, NodeTypeEnum componentType) throws Throwable {
        String methodName = proceedingJoinPoint.getSignature().toShortString();
        try {
            log.trace("#{} - before locking component {} of type {}", methodName, componentId, componentType);
            componentLocker.lock(componentId, componentType);
            log.trace("#{} - after locking component {} of type {}", methodName, componentId, componentType);
            return proceedingJoinPoint.proceed();
        }
        finally {
            log.trace("#{} - before unlocking component {} of type {}", methodName, componentId, componentType);
            componentLocker.unlock(componentId, componentType);
            log.trace("#{} - after unlocking component {} of type {}", methodName, componentId, componentType);
        }
    }


}
