/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.ci.tests.execute.devCI;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.utils.ToscaParserUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.CsarValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class ToscaGroupInsideVF extends ComponentBaseTest {
	private static Logger logger = LoggerFactory.getLogger(ToscaGroupInsideVF.class.getName());

	@Rule
	public static TestName name = new TestName();

	public ToscaGroupInsideVF() {
		super(name, ToscaGroupInsideVF.class.getName());
	}

	@Test
	public void createResourceFromCsarArts() throws Exception {

		// String csar = getCsar();
		// parseCsar(csar);
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("VF_RI2_G6_withArtifacts");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		Component resourceObject = AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		Resource vfManual = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true)
				.left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, vfManual, UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.changeComponentState(vfManual, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true)
				.getLeft();

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils
				.addComponentInstanceToComponentContainer(resourceObject, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(vfManual, service, UserRoleEnum.DESIGNER, true)
				.left().value();
		AtomicOperationUtils.getServiceObject(service, UserRoleEnum.DESIGNER);
		AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true)
				.getLeft();
		AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CANCELCERTIFICATION, true)
				.getLeft();
		AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true)
				.getLeft();

	}

	@Test
	public void soferTest() throws Exception {

		// String csar = getCsar();
		// parseCsar(csar);
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("sofer");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

	}

	@Test
	public void createVFwith2VLs() throws Exception {

		// String csar = getCsar();
		// parseCsar(csar);
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("VSPPackage");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		Component resourceObject = AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		Resource vfManual = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true)
				.left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, vfManual, UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.changeComponentState(vfManual, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true)
				.getLeft();

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils
				.addComponentInstanceToComponentContainer(resourceObject, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(vfManual, service, UserRoleEnum.DESIGNER, true)
				.left().value();
		AtomicOperationUtils.getServiceObject(service, UserRoleEnum.DESIGNER);
		AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true)
				.getLeft();
		AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CANCELCERTIFICATION, true)
				.getLeft();
		AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true)
				.getLeft();

	}

	@Test // (enabled = false)
	public void createResourceFromCsarHappy() throws Exception {
		// String csarUUID = "VF_RI2_G2_withArtifacts";
		String csarUUID = "VF_RI2_G1_Invalid";
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(csarUUID);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		CsarValidationUtils.validateCsarVfArtifact(csarUUID, resource);
	}

	@Test // (enabled = false)
	public void createResourceFromCsarWithProperty() throws Exception {
		String csarUUID = "VF_RI2_G4_withArtifacts";
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(csarUUID);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		CsarValidationUtils.validateCsarVfArtifact(csarUUID, resource);
	}

	@Test // (enabled = false)
	public void UpdateCsarWithNonExistingResourceInstanceFail() throws Exception {

		// String csarUUID = "VF_RI2_G1-RI_NotExist";
		// String csarUUID = "nested3";

		// String csarUUID = "VF_RI2_G1_Invalid_WithArtifacts";
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		RestResponse copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar",
				"VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);
		String csarUUID = "VF_RI2_G4_withArtifacts.csar";

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(csarUUID);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifactsRI_FAIL.csar",
				"VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);

		resourceDetails.setName(resource.getName());
		// resourceDetails.setVendorName("Govnuk");
		// resourceDetails.setDescription("Other");
		RestResponse createResource2 = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource2);
		Resource resource2 = ResponseParser.parseToObjectUsingMapper(createResource2.getResponse(), Resource.class);

		CsarValidationUtils.validateCsarVfArtifact(csarUUID, resource);
		ToscaDefinition toscaDefinition = ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID);
		CsarValidationUtils.validateToscaDefinitonObjectVsResource(toscaDefinition, resource);

		// CsarValidationUtils.validateCsarVfArtifact(csarUUID2, resource2);
		// ToscaDefinition toscaDefinition2 =
		// ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID2);
		// CsarValidationUtils.validateToscaDefinitonObjectVsResource(toscaDefinition2,
		// resource2);

		// Csar csar = parserTocsarObject(csarUUID);
		// validateCsarVsResourceObj(csar, resource);
		// csar.node_types();

	}

	@Test // (enabled = false)
	public void UpdateCsarWithSameCsarDifferentMetadata() throws Exception {

		// User sdncModifierDetails =
		// ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		// RestResponse copyRes =
		// copyCsarRest(sdncModifierDetails,"VF_RI2_G4_withArtifacts_a.csar","VF_RI2_G4_withArtifacts.csar");
		// BaseRestUtils.checkSuccess(copyRes);
		String csarUUID = "VF_RI2_G4_withArtifacts.csar";

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(csarUUID);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		resourceDetails.setName(resource.getName());
		resourceDetails.setVendorName("Govnuk");
		resourceDetails.setDescription("Other");
		RestResponse createResource2 = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource2);
		Resource resource2 = ResponseParser.parseToObjectUsingMapper(createResource2.getResponse(), Resource.class);

		CsarValidationUtils.validateCsarVfArtifact(csarUUID, resource);
		ToscaDefinition toscaDefinition = ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID);
		CsarValidationUtils.validateToscaDefinitonObjectVsResource(toscaDefinition, resource);

		// CsarValidationUtils.validateCsarVfArtifact(csarUUID2, resource2);
		// ToscaDefinition toscaDefinition2 =
		// ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID2);
		// CsarValidationUtils.validateToscaDefinitonObjectVsResource(toscaDefinition2,
		// resource2);

		// Csar csar = parserTocsarObject(csarUUID);
		// validateCsarVsResourceObj(csar, resource);
		// csar.node_types();

	}

	@Test // (enabled = false)
	public void UpdateCsarWithSameCsar() throws Exception {

		// User sdncModifierDetails =
		// ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		// RestResponse copyRes =
		// copyCsarRest(sdncModifierDetails,"VF_RI2_G4_withArtifacts_a.csar","VF_RI2_G4_withArtifacts.csar");
		// BaseRestUtils.checkSuccess(copyRes);
		String csarUUID = "VF_RI2_G4_withArtifacts.csar";

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(csarUUID);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		resourceDetails.setName(resource.getName());
		RestResponse createResource2 = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource2);
		Resource resource2 = ResponseParser.parseToObjectUsingMapper(createResource2.getResponse(), Resource.class);

		CsarValidationUtils.validateCsarVfArtifact(csarUUID, resource);
		ToscaDefinition toscaDefinition = ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID);
		CsarValidationUtils.validateToscaDefinitonObjectVsResource(toscaDefinition, resource);

		// CsarValidationUtils.validateCsarVfArtifact(csarUUID2, resource2);
		// ToscaDefinition toscaDefinition2 =
		// ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID2);
		// CsarValidationUtils.validateToscaDefinitonObjectVsResource(toscaDefinition2,
		// resource2);

		// Csar csar = parserTocsarObject(csarUUID);
		// validateCsarVsResourceObj(csar, resource);
		// csar.node_types();

	}

	@Test // (enabled = false)
	public void UpdateCsarCertifiedVfWithSameCsar() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar",
				"VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);
		String csarUUID = "VF_RI2_G4_withArtifacts.csar";

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(csarUUID);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		Pair<Component, RestResponse> changeComponentState = AtomicOperationUtils.changeComponentState(resource,
				UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		resource = (Resource) changeComponentState.getLeft();

		resourceDetails.setName(resource.getName());
		RestResponse createResource2 = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource2);
		Resource resource2 = ResponseParser.parseToObjectUsingMapper(createResource2.getResponse(), Resource.class);

		CsarValidationUtils.validateCsarVfArtifact(csarUUID, resource);
		ToscaDefinition toscaDefinition = ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID);
		CsarValidationUtils.validateToscaDefinitonObjectVsResource(toscaDefinition, resource);

		// CsarValidationUtils.validateCsarVfArtifact(csarUUID2, resource2);
		// ToscaDefinition toscaDefinition2 =
		// ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID2);
		// CsarValidationUtils.validateToscaDefinitonObjectVsResource(toscaDefinition2,
		// resource2);

		// Csar csar = parserTocsarObject(csarUUID);
		// validateCsarVsResourceObj(csar, resource);
		// csar.node_types();

	}

	@Test // (enabled = false)
	public void UpdateCsarDifferentTosca() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar",
				"VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);
		String csarUUID = "VF_RI2_G4_withArtifacts.csar";

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(csarUUID);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifactsUpdated.csar",
				"VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);

		resourceDetails.setName(resource.getName());
		RestResponse createResource2 = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource2);
		Resource resource2 = ResponseParser.parseToObjectUsingMapper(createResource2.getResponse(), Resource.class);

		CsarValidationUtils.validateCsarVfArtifact(csarUUID, resource);
		ToscaDefinition toscaDefinition = ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID);
		CsarValidationUtils.validateToscaDefinitonObjectVsResource(toscaDefinition, resource);

		// CsarValidationUtils.validateCsarVfArtifact(csarUUID2, resource2);
		// ToscaDefinition toscaDefinition2 =
		// ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID2);
		// CsarValidationUtils.validateToscaDefinitonObjectVsResource(toscaDefinition2,
		// resource2);

		// Csar csar = parserTocsarObject(csarUUID);
		// validateCsarVsResourceObj(csar, resource);
		// csar.node_types();

	}

	@Test // (enabled = false)
	public void UpdateCsarDifferentToscaAndArtifacts() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar",
				"VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);
		String csarUUID = "VF_RI2_G4_withArtifacts.csar";

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(csarUUID);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_UpdateToscaAndArtifacts.csar",
				"VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);

		resourceDetails.setName(resource.getName());
		RestResponse createResource2 = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource2);
		Resource resource2 = ResponseParser.parseToObjectUsingMapper(createResource2.getResponse(), Resource.class);

		CsarValidationUtils.validateCsarVfArtifact(csarUUID, resource);
		ToscaDefinition toscaDefinition = ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID);
		CsarValidationUtils.validateToscaDefinitonObjectVsResource(toscaDefinition, resource);

		// CsarValidationUtils.validateCsarVfArtifact(csarUUID2, resource2);
		// ToscaDefinition toscaDefinition2 =
		// ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID2);
		// CsarValidationUtils.validateToscaDefinitonObjectVsResource(toscaDefinition2,
		// resource2);

		// Csar csar = parserTocsarObject(csarUUID);
		// validateCsarVsResourceObj(csar, resource);
		// csar.node_types();

	}

	@Test // (enabled = false)
	public void migration() throws Exception {
		String csarUUID = "VF_RI2_G4_withArtifacts";
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(csarUUID);
		resourceDetails.setName("Resource1");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);

		resourceDetails.setName("Resource2");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();

		resourceDetails.setName("Resource3");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		resourceDetails.setName("Resource4");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

		resourceDetails.setName("Resource5");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();

		resourceDetails.setName("Resource6");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, true)
				.getLeft();

		resourceDetails.setName("Resource7");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true)
				.getLeft();

		logger.debug("7 VF resources were created");

	}

	public static RestResponse copyCsarRest(User sdncModifierDetails, String sourceCsarUuid, String targetCsarUuid)
			throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.COPY_CSAR_USING_SIMULATOR, config.getCatalogBeHost(), config.getCatalogBePort(),
				sourceCsarUuid, targetCsarUuid);
		String userId = sdncModifierDetails.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);
		HttpRequest http = new HttpRequest();

		RestResponse copyCsarResponse = http.httpSendPost(url, "dummy", headersMap);
		if (copyCsarResponse.getErrorCode() != 200) {
			return null;
		}
		return copyCsarResponse;

	}

	private static Map<String, String> prepareHeadersMap(String userId) {
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		if (userId != null) {
			headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
		}
		return headersMap;
	}

	public static void main(String[] args) throws Exception {
		// String csarUUID = "VF_RI2_G4_withArtifacts";
		String csarUUID = "node_types";
		ToscaParserUtils.getToscaDefinitionObjectByCsarUuid(csarUUID);
	}
}
