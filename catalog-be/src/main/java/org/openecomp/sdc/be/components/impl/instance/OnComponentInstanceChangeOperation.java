package org.openecomp.sdc.be.components.impl.instance;

import org.openecomp.sdc.be.components.impl.OnDeleteEntityOperation;

/**
 * An interface which groups all operations to be executed when an action on a component instance has occurred
 */
public interface OnComponentInstanceChangeOperation extends OnChangeVersionOperation, OnDeleteEntityOperation {
}
