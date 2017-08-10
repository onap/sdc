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

package org.openecomp.sdc.ci.tests.execute.sanity;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ToscaValidation{
	
	
	@BeforeClass
	public void precondition(){
		
		Map<String, Map<String, Object>> expected = new HashMap<String, Map<String, Object>>();
		
		//import amdocs VNF and download csar
		
		//parse downloaded csar and add it to expected object
		
		//create VF base on VNF imported from previous step - declare all inputs
		
		//certify VF  and download csar
		
		//parse downloaded csar and add it to expected object
		
		//create service add VF  - declare all inputs
		
		//certify service and download csar
		
		//parse downloaded csar and add it to expected object
		
				
	}
	
	@Test
	public void validateMetaData(){
		
		
		
		
	}
	
	@Test
	public void validatePropertiesInputs(){
		
		
		
	}
	
	
	
	

	

}
