package org.openecomp.sdc.be.components.merge.instance;

import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

/**
 * Created by chaya on 9/20/2017.
 */
public interface ComponentInstanceMergeInterface {

    void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent, ComponentInstance currentResourceInstance, Component originComponent);

    Either<Component, ResponseFormat> mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId);
}
