package org.openecomp.sdc.be.components.merge.instance;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.model.*;

import java.util.HashMap;
import java.util.Map;

import static groovy.util.GroovyTestCase.assertEquals;


public class ComponentInstanceArtifactsMergeTest {

    @InjectMocks
    private ComponentInstanceArtifactsMerge testInstance;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDeploymentArtifactSaveData() throws Exception {

        Component containerComponent = new Resource();
        Component originComponent = buildOriginalComponentWithOneArtifact();
        ComponentInstance componentInstance = buildComponentInstanceWithTwoArtifacts();

        DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
        testInstance.saveDataBeforeMerge(dataForMergeHolder, containerComponent, componentInstance, originComponent);
        Map<String, ArtifactDefinition> originalComponentDeploymentArtifactsCreatedOnTheInstance = dataForMergeHolder.getOrigComponentDeploymentArtifactsCreatedOnTheInstance();

        assertEquals(originalComponentDeploymentArtifactsCreatedOnTheInstance.size() , 1);
        assert(originalComponentDeploymentArtifactsCreatedOnTheInstance.containsKey("artifactTwo"));
    }

    @Test
    public void testInformationalArtifactSaveData() throws Exception {

        Component containerComponent = new Resource();
        Component originComponent = buildOriginalComponentWithOneArtifact();
        ComponentInstance componentInstance = buildComponentInstanceWithTwoArtifacts();

        DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
        testInstance.saveDataBeforeMerge(dataForMergeHolder, containerComponent, componentInstance, originComponent);
        Map<String, ArtifactDefinition> originalComponentInformationalArtifactsCreatedOnTheInstance = dataForMergeHolder.getOrigComponentInformationalArtifactsCreatedOnTheInstance();

        assertEquals(originalComponentInformationalArtifactsCreatedOnTheInstance.size() , 1);
        assert(originalComponentInformationalArtifactsCreatedOnTheInstance.containsKey("artifactTwo"));
    }

    private ComponentInstance buildComponentInstanceWithTwoArtifacts(){
        ArtifactDefinition artifactFromTheOriginalResource = new ArtifactDefinition();
        artifactFromTheOriginalResource.setArtifactLabel("artifactOne");
        ArtifactDefinition artifactCreatedOnTheInstance = new ArtifactDefinition();
        artifactCreatedOnTheInstance.setArtifactLabel("artifactTwo");

        Map<String, ArtifactDefinition> componentInstanceArtifacts = new HashMap<>();
        componentInstanceArtifacts.put(artifactFromTheOriginalResource.getArtifactLabel(), artifactFromTheOriginalResource);
        componentInstanceArtifacts.put(artifactCreatedOnTheInstance.getArtifactLabel(), artifactCreatedOnTheInstance);

        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setDeploymentArtifacts(componentInstanceArtifacts);
        componentInstance.setArtifacts(componentInstanceArtifacts);
        return componentInstance;
    }

    private Component buildOriginalComponentWithOneArtifact() {
        ArtifactDefinition artifactFromTheOriginalResource = new ArtifactDefinition();
        artifactFromTheOriginalResource.setArtifactLabel("artifactOne");

        Map<String, ArtifactDefinition> originComponentArtifacts = new HashMap<>();
        originComponentArtifacts.put(artifactFromTheOriginalResource.getArtifactLabel(), artifactFromTheOriginalResource);
        Component originComponent = new Resource();
        originComponent.setDeploymentArtifacts(originComponentArtifacts);
        originComponent.setArtifacts(originComponentArtifacts);
        return originComponent;
    }



}