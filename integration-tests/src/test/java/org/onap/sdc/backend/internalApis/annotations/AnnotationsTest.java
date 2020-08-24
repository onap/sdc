/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdc.backend.internalApis.annotations;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.ToscaAnnotationsTypesDefinition;
import org.onap.sdc.backend.ci.tests.utils.ToscaTypesDefinitionUtils;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.general.FileHandling;
import org.onap.sdc.backend.ci.tests.utils.rest.InputsRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.PropertyRestUtils;
import org.onap.sdc.backend.ci.tests.utils.validation.BaseValidationUtils;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.utils.ComponentUtilities;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.onap.sdc.backend.ci.tests.utils.rest.InputsRestUtils.deleteInputFromComponent;
import static org.springframework.util.CollectionUtils.isEmpty;

public class AnnotationsTest extends ComponentBaseTest {

    private static final String PCM_FLAVOR_NAME = "pcm_flavor_name";
    private static final String AVAILABILITY_ZONE = "availabilityzone_name";
    private static final String NET_NAME = "net_name";
    private static final String NF_NAMING_CODE = "nf_naming_code";
    private static final String [] PROPS_TO_DECLARE = new String [] {PCM_FLAVOR_NAME, AVAILABILITY_ZONE, NET_NAME, NF_NAMING_CODE};
    private static final String CSAR_WITH_ANNOTATIONS_V1 = "SIROV_annotations_VSP.csar";
    private static final String CSAR_WITH_ANNOTATIONS_V2 = "SIROV_annotations_VSP_V2.csar";
    private static final String SRIOV_PATH = FileHandling.getFilePath("SRIOV");

    @Rule
    public static TestName name = new TestName();

    public AnnotationsTest() {
        super();
    }

    @Test
    public void whenExportingToscaOfTopologyTemplate_annotationTypeYamlExist_sourceAnnotationExist() throws Exception {
        User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
        ResourceReqDetails resourceDetails = ElementFactory.getDefaultResourceByType("exportToscaAnnotationsYml", NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, sdncModifierDetails.getUserId(), ResourceTypeEnum.VF.toString());
        Resource createdVF = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
        ToscaAnnotationsTypesDefinition toscaAnnotations = ToscaTypesDefinitionUtils.getToscaAnnotationsFromCsar(createdVF, sdncModifierDetails);
        assertTrue(toscaAnnotations.getAnnotation_types().containsKey(ToscaAnnotationsTypesDefinition.SOURCE_ANNOTATION));
    }

    @Test
    public void whenDeclaringAnInputFromPropertyWhichOriginatedFromInputWithAnnotation_copyAnnotationsToNewInput() throws Exception {
        Resource vfWithAnnotationsV1 = importAnnotationsCsarAndCheckIn();
        Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
        ComponentInstance createdCmptInstance = AtomicOperationUtils.addComponentInstanceToComponentContainer(vfWithAnnotationsV1, service).left().value();
        Service fetchedService = AtomicOperationUtils.getServiceObject(service.getUniqueId());
        List<ComponentInstanceInput> declaredProps = declareProperties(fetchedService, createdCmptInstance, PROPS_TO_DECLARE);
        verifyAnnotationsOnDeclaredInputs(vfWithAnnotationsV1, fetchedService, declaredProps);
        Service serviceAfterPropertyDeclaration = AtomicOperationUtils.getServiceObject(service.getUniqueId());
        deleteDeclaredInputsAndVerifySuccess(serviceAfterPropertyDeclaration);
    }

    @Test
    public void onChangeVersion_copyAnnotationsFromNewVspToServiceInputs() throws Exception {
        Resource vfWithAnnotationsV1 = importAnnotationsCsarAndCheckIn();
        Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
        ComponentInstance createdCmptInstance = AtomicOperationUtils.addComponentInstanceToComponentContainer(vfWithAnnotationsV1, service).left().value();
        Service fetchedService = AtomicOperationUtils.getServiceObject(service.getUniqueId());
        declareProperties(fetchedService, createdCmptInstance, PROPS_TO_DECLARE);

        Resource vfWithAnnotationsV2 = updateAnnotationsCsarAndCheckIn(vfWithAnnotationsV1);
        Pair<Component, ComponentInstance> changeVersionRes = AtomicOperationUtils.changeComponentInstanceVersion(service, createdCmptInstance, vfWithAnnotationsV2, UserRoleEnum.DESIGNER, true).left().value();
        Component serviceAfterChangeVersion = changeVersionRes.getKey();
        ComponentInstance newInstance = changeVersionRes.getRight();
        List<ComponentInstanceInput> declaredProps = getInstanceProperties(serviceAfterChangeVersion, newInstance.getUniqueId(), PROPS_TO_DECLARE);
        verifyAnnotationsOnDeclaredInputs(vfWithAnnotationsV2, serviceAfterChangeVersion, declaredProps);

    }

    private void verifyAnnotationsOnDeclaredInputs(Resource vfWithAnnotations, Component fetchedService, List<ComponentInstanceInput> declaredProps) throws Exception {
        Map<String, InputDefinition> serviceLevelInputsByProperty = getCreatedInputsByProperty(fetchedService, declaredProps);
        Map<String, List<Annotation>> annotationsFromVfLevelInputs = getAnnotationsByInputName(vfWithAnnotations);
        verifyInputsAnnotation(serviceLevelInputsByProperty, declaredProps, annotationsFromVfLevelInputs);
    }

    private Map<String, List<Annotation>> getAnnotationsByInputName(Resource resource) {
        return resource.getInputs()
                .stream()
                .collect(toMap(InputDefinition::getName, input -> ComponentUtilities.getInputAnnotations(resource, input.getName())));
    }

    private void deleteDeclaredInputsAndVerifySuccess(Component service) throws Exception {
        for (InputDefinition declaredInput : service.getInputs()) {
            RestResponse deleteInputResponse = InputsRestUtils.deleteInputFromComponent(service, declaredInput.getUniqueId());
            BaseValidationUtils.checkSuccess(deleteInputResponse);
        }
        Service fetchedService = AtomicOperationUtils.getServiceObject(service.getUniqueId());
        assertThat(fetchedService.getInputs()).isNullOrEmpty();
    }

    private void verifyInputsAnnotation(Map<String, InputDefinition> inputsByProperty, List<ComponentInstanceInput> declaredProps, Map<String, List<Annotation>> expectedAnnotationsByInput) {
        Map<String, ComponentInstanceInput> propsByName = MapUtil.toMap(declaredProps, ComponentInstanceInput::getName);

        InputDefinition declaredFromPcmFlavourName = findInputDeclaredFromProperty(PCM_FLAVOR_NAME, inputsByProperty, propsByName);
        verifyInputAnnotations(declaredFromPcmFlavourName, expectedAnnotationsByInput.get(PCM_FLAVOR_NAME));

        InputDefinition declaredFromAvailabilityZone = findInputDeclaredFromProperty(AVAILABILITY_ZONE, inputsByProperty, propsByName);
        verifyInputAnnotations(declaredFromAvailabilityZone, expectedAnnotationsByInput.get(AVAILABILITY_ZONE));

        InputDefinition declaredFromNetName = findInputDeclaredFromProperty(NET_NAME, inputsByProperty, propsByName);
        verifyInputAnnotations(declaredFromNetName, expectedAnnotationsByInput.get(NET_NAME));

        InputDefinition declaredFromNFNamingCode = findInputDeclaredFromProperty(NF_NAMING_CODE, inputsByProperty, propsByName);
        verifyInputHasNoAnnotations(declaredFromNFNamingCode);
    }

    private void verifyInputHasNoAnnotations(InputDefinition inputWithoutAnnotation) {
        assertThat(inputWithoutAnnotation.getAnnotations()).isNullOrEmpty();
    }

    private InputDefinition findInputDeclaredFromProperty(String propName, Map<String, InputDefinition> inputsByProperty, Map<String, ComponentInstanceInput> propsByName) {
        String propId = propsByName.get(propName).getUniqueId();
        return inputsByProperty.get(propId);
    }

    private Map<String, InputDefinition> getCreatedInputsByProperty(Component service, List<ComponentInstanceInput> declaredProps) throws Exception {
        Service fetchedService = AtomicOperationUtils.getServiceObject(service.getUniqueId());
        List<InputDefinition> inputs = fetchedService.getInputs();
        return filterInputsCreatedByDeclaringFromProperties(declaredProps, inputs);
    }

    private Map<String, InputDefinition> filterInputsCreatedByDeclaringFromProperties(List<ComponentInstanceInput> declaredProps, List<InputDefinition> inputs) {
        List<String> declaredPropsIds = declaredProps.stream().map(ComponentInstanceInput::getUniqueId).collect(Collectors.toList());
        Map<String, InputDefinition> inputsByPropertyId = MapUtil.toMap(inputs, InputDefinition::getPropertyId);
        return Maps.filterKeys(inputsByPropertyId, declaredPropsIds::contains);
    }

    private Resource importAnnotationsCsarAndCheckIn() throws Exception {
        Resource annotationsVF = AtomicOperationUtils.importResourceFromCsar(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, CSAR_WITH_ANNOTATIONS_V1, SRIOV_PATH);
        AtomicOperationUtils.changeComponentState(annotationsVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
        return annotationsVF;
    }

    private Resource updateAnnotationsCsarAndCheckIn(Resource vfToUpdate) throws Exception {
        AtomicOperationUtils.changeComponentState(vfToUpdate, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true);
        Resource annotationsVfV2 = AtomicOperationUtils.updateResourceFromCsar(vfToUpdate, UserRoleEnum.DESIGNER, CSAR_WITH_ANNOTATIONS_V2, SRIOV_PATH);
        AtomicOperationUtils.changeComponentState(annotationsVfV2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
        return annotationsVfV2;
    }

    private List<ComponentInstanceInput> declareProperties(Service service, ComponentInstance instance, String ... propertiesToDeclareNames) throws Exception {
        Map<String, List<ComponentInstanceInput>> propertiesToDeclare = getServiceInstancesProps(service, instance.getUniqueId(), propertiesToDeclareNames);
        RestResponse restResponse = PropertyRestUtils.declareProporties(service, propertiesToDeclare, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
        BaseValidationUtils.checkSuccess(restResponse);
        return propertiesToDeclare.get(instance.getUniqueId());
    }

    private List<ComponentInstanceInput> getInstanceProperties(Component service, String ofInstance, String ... propsNames) {
        return getServiceInstancesProps(service, ofInstance, propsNames).get(ofInstance);
    }

    private void verifyInputAnnotations(InputDefinition input, List<Annotation> expectedAnnotations) {
        if (isEmpty(expectedAnnotations)) {
            assertThat(input.getAnnotations()).isNullOrEmpty();
            return;
        }
        assertThat(input.getAnnotations())
                .usingElementComparatorOnFields("type", "name", "properties")
                .isNotEmpty()
                .containsExactlyElementsOf(expectedAnnotations);
    }

    private Map<String, List<ComponentInstanceInput>> getServiceInstancesProps(Component fromService, String ofInstance, String ... propsToDeclareNames) {
        Set<String> propsToDeclare = ImmutableSet.<String>builder().add(propsToDeclareNames).build();
        List<ComponentInstanceInput> componentInstancesInputs = fromService.getComponentInstancesInputs().get(ofInstance);
        return componentInstancesInputs.stream()
                .filter(insInput -> propsToDeclare.contains(insInput.getName()))
                .collect(groupingBy((i) -> ofInstance));
    }

}
