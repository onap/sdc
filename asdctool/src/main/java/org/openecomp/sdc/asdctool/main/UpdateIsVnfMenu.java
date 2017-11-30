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

package org.openecomp.sdc.asdctool.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.asdctool.impl.UpdatePropertyOnVertex;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateIsVnfMenu {

	private static Logger log = LoggerFactory.getLogger(UpdateIsVnfMenu.class.getName());

	private static void usageAndExit() {
		updateIsVnfTrueUsage();
		System.exit(1);
	}

	private static void updateIsVnfTrueUsage() {
		System.out.println(
				"Usage: updateIsVnfTrue <titan.properties> <systemServiceName1,systemServiceName2,...,systemServiceNameN>");
	}

	public static void main(String[] args) throws Exception {

		if (args == null || args.length < 1) {
			usageAndExit();
		}

		UpdatePropertyOnVertex updatePropertyOnVertex = new UpdatePropertyOnVertex();
		String operation = args[0];

		switch (operation.toLowerCase()) {

		case "updateisvnftrue":
			boolean isValid = verifyParamsLength(args, 3);
			if (false == isValid) {
				updateIsVnfTrueUsage();
				System.exit(1);
			}

			Map<String, Object> keyValueToSet = new HashMap<>();
			keyValueToSet.put(GraphPropertiesDictionary.IS_VNF.getProperty(), true);

			List<Map<String, Object>> orCriteria = buildCriteriaFromSystemServiceNames(args[2]);
			Integer updatePropertyOnServiceAtLeastCertified = updatePropertyOnVertex
					.updatePropertyOnServiceAtLeastCertified(args[1], keyValueToSet, orCriteria);

			if (updatePropertyOnServiceAtLeastCertified == null) {
				System.exit(2);
			} else if (updatePropertyOnServiceAtLeastCertified.intValue() >= 0) {
				log.debug("Number of updated services is {}",updatePropertyOnServiceAtLeastCertified.intValue());
				System.exit(0);
			}

			break;
		default:
			usageAndExit();
		}

	}

	private static List<Map<String, Object>> buildCriteriaFromSystemServiceNames(String systemList) {

		List<Map<String, Object>> systemNames = new ArrayList<>();

		String[] split = systemList.split(",");
		if (split != null) {
			for (String systemName : split) {
				systemName = systemName.trim();

				Map<String, Object> map = new HashMap();
				map.put(GraphPropertiesDictionary.SYSTEM_NAME.getProperty(), systemName);
				map.put(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Service.getName());

				systemNames.add(map);
			}
		}

		return systemNames;
	}

	private static boolean verifyParamsLength(String[] args, int i) {
		if (args == null) {
			if (i > 0) {
				return false;
			}
			return true;
		}

		if (args.length >= i) {
			return true;
		}
		return false;
	}

}
