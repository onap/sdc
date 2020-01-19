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

package org.openecomp.sdc.be.model.utils;

import org.openecomp.sdc.be.datatypes.enums.PromoteVersionEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.common.api.Constants;
import org.springframework.util.StringUtils;

public class GroupUtils {

	public static boolean isVfModule(String type) {
		return type.equals(Constants.DEFAULT_GROUP_VF_MODULE);
	}
	
	   /**
     * The version of the group/poloces is an integer. In order to support BC, we might get a version in a float format.
     *
     * @param promoteVersion
     * @return
     */
	
	public static String updateVersion(PromoteVersionEnum promoteVersion, String currentVesion) {
	    if(StringUtils.isEmpty(currentVesion)){
	        return "0.0";
	    }
        String newVersion = currentVesion;
         switch (promoteVersion){
         case MINOR:
             newVersion = GroupUtils.increaseMainorVersion(currentVesion);
             break;
         case MAJOR:
             newVersion = GroupUtils.increaseMajorVersion(currentVesion);
             break;
         default:
             break;
         }     
         return newVersion;
    }
    
	private static String increaseMajorVersion(String version) {

       String[] versionParts = version.split(ToscaElementLifecycleOperation.VERSION_DELIMITER_REGEXP);
       Integer majorVersion = Integer.parseInt(versionParts[0]);
       
            
       Integer mainorVersion = versionParts.length > 1?Integer.parseInt(versionParts[1]):0;       
      
       if(mainorVersion > 0 || majorVersion == 0){
           majorVersion++;
       }
       return String.valueOf(majorVersion);

   }
   
	private static String increaseMainorVersion(String version) {

       String[] versionParts = version.split(ToscaElementLifecycleOperation.VERSION_DELIMITER_REGEXP);
      
       Integer mainorVersion = versionParts.length > 1?Integer.parseInt(versionParts[1]):0;

       mainorVersion++;

       return versionParts[0] + ToscaElementLifecycleOperation.VERSION_DELIMITER + String.valueOf(mainorVersion);

   }
}
