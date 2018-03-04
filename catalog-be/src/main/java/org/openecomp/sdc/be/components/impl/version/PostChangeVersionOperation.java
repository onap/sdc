package org.openecomp.sdc.be.components.impl.version;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
@FunctionalInterface
public interface PostChangeVersionOperation {

    ActionStatus onChangeVersion(Component container, ComponentInstance prevVersion, ComponentInstance newVersion);

}
