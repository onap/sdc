package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComponentInstanceCapabiliteisPropertiesMergeTest {

    @InjectMocks
    private ComponentInstanceCapabiliteisPropertiesMerge testInstance;

    @Mock
    private ComponentCapabilitiesPropertiesMergeBL capabilitiesPropertiesMergeBL;

    @Mock
    private ComponentsUtils componentsUtils;

    private DataForMergeHolder mergeHolder;

    private Resource origInstanceNode;
    private List<CapabilityDefinition> origInstanceCapabilities;

    @Before
    public void setUp() throws Exception {
        origInstanceNode = new Resource();
        origInstanceCapabilities = Collections.emptyList();
        mergeHolder = new DataForMergeHolder();
        mergeHolder.setOrigInstanceNode(origInstanceNode);
        mergeHolder.setOrigInstanceCapabilities(origInstanceCapabilities);
    }

    @Test
    public void mergeDataAfterCreate() {
        Service currentComponent = new Service();
        when(capabilitiesPropertiesMergeBL.mergeComponentInstanceCapabilities(currentComponent, origInstanceNode, "instId", origInstanceCapabilities))
            .thenReturn(ActionStatus.OK);
        Either<Component, ResponseFormat> mergeResult = testInstance.mergeDataAfterCreate(new User(), mergeHolder, currentComponent, "instId");
        assertTrue(mergeResult.isLeft());
    }

    @Test
    public void mergeDataAfterCreate_error() {
        Service currentComponent = new Service();
        when(capabilitiesPropertiesMergeBL.mergeComponentInstanceCapabilities(currentComponent, origInstanceNode, "instId", origInstanceCapabilities))
                .thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(new ResponseFormat());
        Either<Component, ResponseFormat> mergeResult = testInstance.mergeDataAfterCreate(new User(), mergeHolder, currentComponent, "instId");
        assertTrue(mergeResult.isRight());
    }

}