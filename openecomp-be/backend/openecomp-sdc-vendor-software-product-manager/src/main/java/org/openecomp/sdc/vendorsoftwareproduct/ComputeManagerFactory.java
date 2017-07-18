package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

public abstract class ComputeManagerFactory extends AbstractComponentFactory<ComputeManager> {

    public static ComputeManagerFactory getInstance() {
      return AbstractFactory.getInstance(ComputeManagerFactory.class);
    }
}
