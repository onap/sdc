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

package org.openecomp.sdc.be.ecomp;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.common.config.EcompClassification;
import org.openecomp.sdc.common.config.EcompErrorCode;
import org.openecomp.sdc.common.config.EcompErrorEnum;
import org.openecomp.sdc.common.config.generation.GenerateEcompErrorsCsv;

public class GenerateEcompErrorFileTest {

	@Test
	public void verifyNoDuplicatesInEcompErrorCodes() {

		EcompErrorEnum[] ecompErrorEnums = EcompErrorEnum.values();

		Map<EcompErrorCode, List<EcompClassification>> map = new HashMap<EcompErrorCode, List<EcompClassification>>();
		for (EcompErrorEnum ecompErrorEnum : ecompErrorEnums) {

			List<EcompClassification> list = map.get(ecompErrorEnum.getEcompErrorCode());
			if (list == null) {
				list = new ArrayList<>();

				list.add(ecompErrorEnum.getClassification());

				map.put(ecompErrorEnum.getEcompErrorCode(), list);
			} else {
				if (list.contains(ecompErrorEnum.getClassification())) {
					assertTrue(ecompErrorEnum.getEcompErrorCode() + " already defined with ecomp classification " + ecompErrorEnum.getClassification(), false);
				} else {
					list.add(ecompErrorEnum.getClassification());
				}

			}

		}

	}

	@Test
	public void generateEcompErrorFileInTarget() {

		GenerateEcompErrorsCsv ecompErrorsCsv = new GenerateEcompErrorsCsv();
		boolean result = ecompErrorsCsv.generateEcompErrorsCsvFile("target", false);
		assertTrue("check result from file generation", result);

	}

}
