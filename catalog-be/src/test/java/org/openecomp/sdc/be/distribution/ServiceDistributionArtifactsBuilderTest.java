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

package org.openecomp.sdc.be.distribution;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.openecomp.sdc.be.components.BaseConfDependentTest;
import org.openecomp.sdc.be.components.distribution.engine.ArtifactInfoImpl;
import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public class ServiceDistributionArtifactsBuilderTest extends BaseConfDependentTest {

	@InjectMocks
	ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder = new ServiceDistributionArtifactsBuilder();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testConvertServiceArtifactsToArtifactInfo() {

		Service service = new Service();
		String serviceName = "myService";
		String serviceVersion = "1.0";
		String serviceId = "serviceId";
		service.setName(serviceName);
		service.setVersion(serviceVersion);
		service.setUniqueId(serviceId);
		
		
		String artifactName = "service-Myservice-template.yml";
		String artifactLabel = "assettoscatemplate";
		String esArtifactId = "123123dfgdfgd0";
		byte[] payload = "some payload".getBytes();
		
		ArtifactDefinition toscaTemplateArtifact = new ArtifactDefinition();
		toscaTemplateArtifact.setArtifactName(artifactName);
		toscaTemplateArtifact.setArtifactType(ArtifactTypeEnum.TOSCA_TEMPLATE.getType());
		toscaTemplateArtifact.setArtifactLabel(artifactLabel);
		toscaTemplateArtifact.setEsId(esArtifactId);
		toscaTemplateArtifact.setUniqueId(esArtifactId);
		toscaTemplateArtifact.setPayload(payload);
		
		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		toscaArtifacts.put(artifactLabel, toscaTemplateArtifact);
		service.setToscaArtifacts(toscaArtifacts);
		
		ArtifactDefinition deploymentArtifact = new ArtifactDefinition();
		deploymentArtifact.setArtifactName("deployment.yaml");
		deploymentArtifact.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		deploymentArtifact.setArtifactType(ArtifactTypeEnum.OTHER.getType());
		deploymentArtifact.setArtifactLabel("deployment");
		deploymentArtifact.setEsId("deployment007");
		deploymentArtifact.setUniqueId("deployment007");
		deploymentArtifact.setPayload(payload);
		Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
		deploymentArtifacts.put("deployment", deploymentArtifact);
		service.setDeploymentArtifacts(deploymentArtifacts);
		
		Class<ServiceDistributionArtifactsBuilder> targetClass = ServiceDistributionArtifactsBuilder.class;
		String methodName = "convertServiceArtifactsToArtifactInfo";
		Object[] argObjects = {service};
		Class[] argClasses = {Service.class};
	    try {
	    	Method method = targetClass.getDeclaredMethod(methodName, argClasses);
	    	method.setAccessible(true);
	    	List<ArtifactInfoImpl> convertServiceArtifactsToArtifactInfoRes =
	    			(List<ArtifactInfoImpl>) method.invoke(serviceDistributionArtifactsBuilder, argObjects);
	    	assertTrue(convertServiceArtifactsToArtifactInfoRes != null);
	    	assertTrue(convertServiceArtifactsToArtifactInfoRes.size() == 2);
	    	List<String> artifactsNames = convertServiceArtifactsToArtifactInfoRes.stream().map(a->a.getArtifactName()).collect(Collectors.toList());
	    	assertTrue(artifactsNames.contains(artifactName) && artifactsNames.contains("deployment.yaml"));
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
}
