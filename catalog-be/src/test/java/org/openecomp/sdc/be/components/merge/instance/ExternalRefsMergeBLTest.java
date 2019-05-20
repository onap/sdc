package org.openecomp.sdc.be.components.merge.instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ExternalReferencesOperation;

import javax.annotation.Resource;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExternalRefsMergeBLTest {

    private static final String NEW_INSTANCE_ID = "NEW_INSTANCE_ID";
    @Resource
    private ExternalRefsMergeBL externalRefsMergeBL;
    @Mock
    private ExternalReferencesOperation externalReferencesOperation;

    private final Map<String, List<String>> externalRefs = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        externalRefsMergeBL = new ExternalRefsMergeBL(externalReferencesOperation);
        externalRefs.put("a", Arrays.asList("1", "2"));
    }

    @Test
    public void testExternalArtifactsSaveData_noArtifactsExist() {
        Component containerComponent = new org.openecomp.sdc.be.model.Resource();
        ComponentInstance componentInstance = new ComponentInstance();
        when(externalReferencesOperation.getAllExternalReferences(any(), any()))
                .thenReturn(new HashMap<>());

        DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
        externalRefsMergeBL.saveDataBeforeMerge(dataForMergeHolder, containerComponent, componentInstance, containerComponent);
        Map<String, List<String>> originalComponentDeploymentArtifactsCreatedOnTheInstance = dataForMergeHolder.getOrigCompInstExternalRefs();

        assertThat(originalComponentDeploymentArtifactsCreatedOnTheInstance.size()).isZero();
    }

    @Test
    public void testExternalArtifactsSaveData_artifactsExist() {
        Component containerComponent = new org.openecomp.sdc.be.model.Resource();
        ComponentInstance componentInstance = new ComponentInstance();
        when(externalReferencesOperation.getAllExternalReferences(any(), any()))
                .thenReturn(externalRefs);

        DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
        externalRefsMergeBL.saveDataBeforeMerge(dataForMergeHolder, containerComponent, componentInstance, containerComponent);
        Map<String, List<String>> origCompInstExternalRefs = dataForMergeHolder.getOrigCompInstExternalRefs();

        assertThat(origCompInstExternalRefs.size()).isEqualTo(1);
        assertThat(origCompInstExternalRefs.containsKey("a")).isTrue();
    }

    @Test
    public void testExternalArtifactsRestoreData_noArtifacts() {
        Component containerComponent = new org.openecomp.sdc.be.model.Resource();
        ComponentInstance ci = new ComponentInstance();
        ci.setUniqueId(NEW_INSTANCE_ID);
        containerComponent.setComponentInstances(Collections.singletonList(ci));
        DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
        externalRefsMergeBL.mergeDataAfterCreate(new User(), dataForMergeHolder, containerComponent, NEW_INSTANCE_ID);
        verifyZeroInteractions(externalReferencesOperation);
    }

    @Test
    public void testExternalArtifactsRestoreData_hasArtifacts() {
        Component containerComponent = new org.openecomp.sdc.be.model.Resource();
        ComponentInstance ci = new ComponentInstance();
        ci.setUniqueId(NEW_INSTANCE_ID);
        containerComponent.setComponentInstances(Collections.singletonList(ci));
        DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
        dataForMergeHolder.setOrigComponentInstanceExternalRefs(externalRefs);
        externalRefsMergeBL.mergeDataAfterCreate(new User(), dataForMergeHolder, containerComponent, NEW_INSTANCE_ID);
        verify(externalReferencesOperation, times(1)).addAllExternalReferences(any(), eq(NEW_INSTANCE_ID), eq(externalRefs));
    }

    @Test(expected=ComponentException.class)
    public void testExternalArtifactsRestoreData_noCI() {
        Component containerComponent = new org.openecomp.sdc.be.model.Resource();
        DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
        externalRefsMergeBL.mergeDataAfterCreate(new User(), dataForMergeHolder, containerComponent, NEW_INSTANCE_ID);
    }
}