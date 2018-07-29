package org.openecomp.sdc.be.components.impl.instance;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
@FunctionalInterface
public interface OnChangeVersionOperation {

    /**
     * A side effect operation to execute when a component instance version was changed from {@code prevVersion} to {@code newVersion}
     * @param container the container which contains the instance which is version was changed
     * @param prevVersion the previous version of the component instance.
     * @param newVersion the new version of the component instance.
     * @return the status of the operation
     */
    ActionStatus onChangeVersion(Component container, ComponentInstance prevVersion, ComponentInstance newVersion);

}
