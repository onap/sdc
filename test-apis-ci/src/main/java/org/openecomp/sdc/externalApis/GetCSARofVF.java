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

package org.openecomp.sdc.externalApis;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedExternalAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.cassandra.CassandraUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.AssetRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertTrue;

import com.google.gson.Gson;

import fj.data.Either;

public class GetCSARofVF extends ComponentBaseTest {

//	protected User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
//	protected User sdncAdminUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
//	protected ConsumerDataDefinition consumerDataDefinition;

	@Rule
	public static TestName name = new TestName();

	public GetCSARofVF() {
		super(name, GetCSARofVF.class.getName());
	}

//	Gson gson = new Gson();

	@BeforeMethod
	public void setup() throws Exception {

//		AtomicOperationUtils.createDefaultConsumer(true);
//		CassandraUtils.truncateAllKeyspaces();
		

	}
	
	@Test
	public void getResourceToscaModelCheckOutState() throws Exception {


		Resource resource = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		
//		HttpResponse componentToscaModel = AssetRestUtils.getComponentToscaModel(AssetTypeEnum.RESOURCES, resource.getUUID());
		File toscaModelCsarFile = AssetRestUtils.getToscaModelCsarFile(AssetTypeEnum.RESOURCES, resource.getUUID());
		
		// validate tosca structure  
		validateCsarContent(resource, toscaModelCsarFile);
	
		// Validate audit message
		validateAudit(resource);
		
		
		
	}


	
	@Test
	public void getResourceToscaModelCheckInState() throws Exception {
		
		Resource resource = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		
		AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		
//		HttpResponse componentToscaModel = AssetRestUtils.getComponentToscaModel(AssetTypeEnum.RESOURCES, resource.getUUID());
		File toscaModelCsarFile = AssetRestUtils.getToscaModelCsarFile(AssetTypeEnum.RESOURCES, resource.getUUID());
		
		// validate tosca structure  
		validateCsarContent(resource, toscaModelCsarFile);
	
		// Validate audit message
		validateAudit(resource);
		
		
		
	}
	
	@Test
	public void getRsourceToscaModelCertifyState() throws Exception {
		
		Resource resource = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		
		AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		
//		HttpResponse componentToscaModel = AssetRestUtils.getComponentToscaModel(AssetTypeEnum.RESOURCES, resource.getUUID());
		File toscaModelCsarFile = AssetRestUtils.getToscaModelCsarFile(AssetTypeEnum.RESOURCES, resource.getUUID());
		
		// validate tosca structure  
		validateCsarContent(resource, toscaModelCsarFile);
	
		// Validate audit message
		validateAudit(resource);
		
		
	}
	
	
	@Test
	public void getServiceToscaModelCheckOutState() throws Exception {

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		
//		HttpResponse componentToscaModel = AssetRestUtils.getComponentToscaModel(AssetTypeEnum.RESOURCES, resource.getUUID());
		File toscaModelCsarFile = AssetRestUtils.getToscaModelCsarFile(AssetTypeEnum.SERVICES, service.getUUID());
		
		// validate tosca structure  
		validateCsarContent(service, toscaModelCsarFile);
	
		validateAudit(service);
		
		
		
	}
	
	@Test
	public void getServiceToscaModelCheckInState() throws Exception {
		
		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		
		AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		
//		HttpResponse componentToscaModel = AssetRestUtils.getComponentToscaModel(AssetTypeEnum.RESOURCES, resource.getUUID());
		File toscaModelCsarFile = AssetRestUtils.getToscaModelCsarFile(AssetTypeEnum.SERVICES, service.getUUID());
		
		// validate tosca structure  
		validateCsarContent(service, toscaModelCsarFile);
	
		validateAudit(service);
		
		
	}
	
	@Test
	public void getServiceToscaModelCertifyState() throws Exception {
		
		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, service, UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		
//		HttpResponse componentToscaModel = AssetRestUtils.getComponentToscaModel(AssetTypeEnum.RESOURCES, resource.getUUID());
		File toscaModelCsarFile = AssetRestUtils.getToscaModelCsarFile(AssetTypeEnum.SERVICES, service.getUUID());
		
		// validate tosca structure  
		validateCsarContent(service, toscaModelCsarFile);
	
		validateAudit(service);
		
	}
	
	

	
	

	public void validateCsarContent(Component resource, File toscaModelCsarFile) throws ZipException, IOException {
		ZipFile zipFile = new ZipFile(toscaModelCsarFile);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while(entries.hasMoreElements()){
			ZipEntry nextElement = entries.nextElement();
			if (!(nextElement.getName().contains("Artifacts")||nextElement.getName().contains("csar.meta"))){
				assertTrue("missing file in csar template", (nextElement.getName().equals("TOSCA-Metadata/TOSCA.meta") || 
						nextElement.getName().equals("Definitions/"+ resource.getComponentType().getValue().toLowerCase()+"-"+ resource.getSystemName()+"-template.yml")) ||
						nextElement.getName().equals("Definitions/"+ resource.getComponentType().getValue().toLowerCase()+"-"+ resource.getSystemName()+"-template-interface.yml"));
				}
			}
	}
	
	public void validateAudit(Component resource) throws Exception {
		ExpectedExternalAudit expectedAudit = null;
		if (resource.getComponentType().equals(ComponentTypeEnum.RESOURCE)){
		expectedAudit = ElementFactory.getDefaultExternalAuditObject(AssetTypeEnum.RESOURCES, AuditingActionEnum.GET_TOSCA_MODEL, ("/" + resource.getUUID() + "/toscaModel"));
		}
		else expectedAudit = ElementFactory.getDefaultExternalAuditObject(AssetTypeEnum.SERVICES, AuditingActionEnum.GET_TOSCA_MODEL, ("/" + resource.getUUID() + "/toscaModel"));
		expectedAudit.setRESOURCE_NAME(resource.getName());
		expectedAudit.setSERVICE_INSTANCE_ID(resource.getUUID());
		expectedAudit.setRESOURCE_TYPE(resource.getComponentType().getValue());
//		AuditValidationUtils.validateExternalAudit(expectedAudit, AuditingActionEnum.GET_TOSCA_MODEL.getName(),	null);

		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, expectedAudit.getRESOURCE_NAME());
        AuditValidationUtils.validateExternalAudit(expectedAudit, AuditingActionEnum.GET_TOSCA_MODEL.getName(), body);

	}
	
}
