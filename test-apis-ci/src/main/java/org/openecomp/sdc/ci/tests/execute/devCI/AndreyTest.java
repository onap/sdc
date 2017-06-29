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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.openecomp.sdc.ci.tests.datatypes.GroupHeatMetaDefinition;
import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.openecomp.sdc.ci.tests.datatypes.TypeHeatMetaDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.utils.CsarParserUtils;
import org.openecomp.sdc.ci.tests.utils.ToscaParserUtils;
import org.openecomp.sdc.tosca.parser.api.ISdcCsarHelper;
import org.openecomp.sdc.tosca.parser.exceptions.SdcToscaParserException;
import org.openecomp.sdc.tosca.parser.impl.SdcToscaParserFactory;
//import org.openecomp.sdc.toscaparser.api.Metadata;
import org.openecomp.sdc.toscaparser.api.Capability;
import org.openecomp.sdc.toscaparser.api.NodeTemplate;
import org.openecomp.sdc.toscaparser.api.common.JToscaException;
import org.testng.annotations.Test;

public class AndreyTest {

	public static void main(String[] args) throws Exception {
		ToscaDefinition toscaDefinition;
		System.out.println("start " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
//        File path = new File("C:/Data/D2.0/TOSCA_Ex/Definitions/tosca_definition_version.yaml");
//        File path = new File("C:/Data/D2.0/TOSCA_Ex/resource-Vl11Vl10-template.yml");
        File path = new File("C:/Data/D2.0/TOSCA_Ex/service-Servicepxtc-template US822998.yml");
        File csarPath = new File("C:/Data/D2.0/TOSCA_Ex/Nested.csar");

        toscaDefinition = ToscaParserUtils.parseToscaYamlToJavaObject(path);
        System.out.println("listTypeHeatMetaDefinition start " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        List<TypeHeatMetaDefinition> listTypeHeatMetaDefinition = CsarParserUtils.getListTypeHeatMetaDefinition(csarPath);
        System.out.println("get service start " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        System.out.println(listTypeHeatMetaDefinition);
        for(TypeHeatMetaDefinition typeHeatMetaDefinition : listTypeHeatMetaDefinition){
        	for(GroupHeatMetaDefinition groupHeatMetaDefinition : typeHeatMetaDefinition.getGroupHeatMetaDefinition()){
        		List<HeatMetaFirstLevelDefinition> artifactList = groupHeatMetaDefinition.getArtifactList();
        		boolean isBase = groupHeatMetaDefinition.getPropertyHeatMetaDefinition().getValue();
        	}
        	
        }
        System.out.println("Finished");
        System.out.println("get service start " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
	}
	
	
	@Test
	public void distributionTest() throws SdcToscaParserException, JToscaException, IOException {
		//String serviceName = import and create().getName();
		//getServiceObject();
		//parseServiceObject();
		
		
		
		SdcToscaParserFactory factory = SdcToscaParserFactory.getInstance();	
        long startTime = System.currentTimeMillis();
        long estimatedTime = System.currentTimeMillis() - startTime; 
        System.out.println("Time to init factory " + estimatedTime);
        String fileStr1 = "C:\\Users\\ys9693\\NewDownload\\2016-006_vvm_vvm_30_1607_e2e.zip.csar";//ToscaParserStubsTest.class.getClassLoader().getResource("csars/service-ServiceFdnt-csar-0904-2.csar").getFile();
        File file1 = new File(fileStr1);
        String name = file1.getName();
        String absolutePath = file1.getAbsolutePath();
        ISdcCsarHelper fdntCsarHelper = factory.getSdcCsarHelper(file1.getAbsolutePath());
        
        List<NodeTemplate> serviceVfList = fdntCsarHelper.getServiceVfList();
        System.out.println("serviceVfList----->" + serviceVfList);
        
        List<NodeTemplate> serviceNodeTemplatesByTypeVF = fdntCsarHelper.getServiceNodeTemplatesByType("VF");
        System.out.println("serviceNodeTemplatesByTypeVF----->" + serviceNodeTemplatesByTypeVF);
        List<NodeTemplate> serviceNodeTemplatesByTypeVFC = fdntCsarHelper.getServiceNodeTemplatesByType("VFC");
        System.out.println("serviceNodeTemplatesByTypeVFC -------->" + serviceNodeTemplatesByTypeVFC);
        List<NodeTemplate> serviceNodeTemplatesByTypeVL = fdntCsarHelper.getServiceNodeTemplatesByType("VL");
        System.out.println("serviceNodeTemplatesByTypeVL------>" +serviceNodeTemplatesByTypeVL);
        List<NodeTemplate> serviceNodeTemplatesByTypeCP = fdntCsarHelper.getServiceNodeTemplatesByType("CP");
        System.out.println("serviceNodeTemplatesByTypeCP ---------->" + serviceNodeTemplatesByTypeCP);        
        
        System.out.println("getMetaData"+ serviceVfList.get(0).getMetaData());
        System.out.println("UUID"+ serviceVfList.get(0).getMetaData().getValue("UUID"));
        System.out.println("customizationUUID"+ serviceVfList.get(0).getMetaData().getValue("customizationUUID"));
        System.out.println("serviceVfList.size()------->"+  serviceVfList.size());
        System.out.println("getCpListByVf--------->" + fdntCsarHelper.getCpListByVf(serviceVfList.get(0).getMetaData().getValue("customizationUUID")));
        List<NodeTemplate> vfcListByVf = fdntCsarHelper.getVfcListByVf(serviceVfList.get(0).getMetaData().getValue("customizationUUID"));
        System.out.println("getVfcListByVf--------->" + vfcListByVf);
        for (NodeTemplate nodeTemplate : vfcListByVf) {
			
        	System.out.println("----> getCpPropertiesFromVfc--------->" + fdntCsarHelper.getCpPropertiesFromVfc(nodeTemplate));
		}
        
        for (NodeTemplate nodeTemplate : serviceVfList) {
        	System.out.println("NodeName---->"+nodeTemplate.getName());
        	System.out.println("getTypeOfNodeTemplate--------->" + fdntCsarHelper.getTypeOfNodeTemplate(nodeTemplate));
        	System.out.println("getServiceInputLeafValueOfDefault--------->" + fdntCsarHelper.getServiceInputLeafValueOfDefault(serviceVfList.get(0).getMetaData().getValue("customizationUUID")));
        	System.out.println("getVfModulesByVf--------->" + fdntCsarHelper.getVfModulesByVf(nodeTemplate.getMetaData().getValue("customizationUUID")));
        	ArrayList<Object> requirements = nodeTemplate.getRequirements();
        	requirements.size();
        	LinkedHashMap<String, Capability> capabilities = nodeTemplate.getCapabilities();
        	ArrayList<Object> requirements2 = nodeTemplate.getRequirements();
		}
        
	}
	
}
