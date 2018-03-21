/*
* Copyright © 2016-2018 European Support Limited
*
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
*/

package org.openecomp.sdcrests.vendorlicense.rest.mapping;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolEntityDto;
import org.junit.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


public class MapEntitlementPoolEntityToEntitlementPoolEntityDtoTest {

	@Test
	public void testReferencingFeatureGroups() throws Exception {
		EntitlementPoolEntity source = new EntitlementPoolEntity();
		EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
		MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper = new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getReferencingFeatureGroups(), source.getReferencingFeatureGroups());
		Set<String> param = new HashSet<>(Arrays.asList("a3ad0c7a-9f4c-44d2-8c0c-b4b47cd6d264"));
		source.setReferencingFeatureGroups(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getReferencingFeatureGroups());
		assertNotNull(target.getReferencingFeatureGroups());
		assertEquals(target.getReferencingFeatureGroups(), param);
	}

	@Test
	public void testDescription() throws Exception {
		EntitlementPoolEntity source = new EntitlementPoolEntity();
		EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
		MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper = new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getDescription(), source.getDescription());
		String param = "58191782-5de2-4d42-b8ca-119707d38150";
		source.setDescription(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getDescription());
		assertNotNull(target.getDescription());
		assertEquals(target.getDescription(), param);
	}

	@Test
	public void testIncrements() throws Exception {
		EntitlementPoolEntity source = new EntitlementPoolEntity();
		EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
		MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper = new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getIncrements(), source.getIncrements());
		String param = "ea6b2eab-f959-41c2-9a09-2898eb5401be";
		source.setIncrements(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getIncrements());
		assertNotNull(target.getIncrements());
		assertEquals(target.getIncrements(), param);
	}

	@Test
	public void testExpiryDate() throws Exception {
		EntitlementPoolEntity source = new EntitlementPoolEntity();
		EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
		MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper = new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getExpiryDate(), source.getExpiryDate());
		String param = "7e27d6a4-78be-44df-b099-dd41317586ba";
		source.setExpiryDate(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getExpiryDate());
		assertNotNull(target.getExpiryDate());
		assertEquals(target.getExpiryDate(), param);
	}

	@Test
	public void testId() throws Exception {
		EntitlementPoolEntity source = new EntitlementPoolEntity();
		EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
		MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper = new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getId(), source.getId());
		String param = "e2163b3f-971e-4523-a5bc-0e6f7dd44e37";
		source.setId(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getId());
		assertNotNull(target.getId());
		assertEquals(target.getId(), param);
	}

	@Test
	public void testThresholdValue() throws Exception {
		EntitlementPoolEntity source = new EntitlementPoolEntity();
		EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
		MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper = new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getThresholdValue(), source.getThresholdValue());
		Integer param = 2146099790;
		source.setThresholdValue(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getThresholdValue());
		assertNotNull(target.getThresholdValue());
		assertEquals(target.getThresholdValue(), param);
	}

	@Test
	public void testName() throws Exception {
		EntitlementPoolEntity source = new EntitlementPoolEntity();
		EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
		MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper = new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getName(), source.getName());
		String param = "f8faa28a-435d-4cea-98ee-de7a46b52ec5";
		source.setName(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getName());
		assertNotNull(target.getName());
		assertEquals(target.getName(), param);
	}

	@Test
	public void testOperationalScope() throws Exception {
		EntitlementPoolEntity source = new EntitlementPoolEntity();
		EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
		MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper = new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
		MultiChoiceOrOther<OperationalScope> param = new MultiChoiceOrOther<>();
		param.setChoices(new HashSet(Arrays.asList("a", "b")));
		source.setOperationalScope(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getOperationalScope());
		assertNotNull(target.getOperationalScope());
		assertEquals(target.getOperationalScope().getChoices(), param.getChoices());
	}

	@Test
	public void testStartDate() throws Exception {
		EntitlementPoolEntity source = new EntitlementPoolEntity();
		EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
		MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper = new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getStartDate(), source.getStartDate());
		String param = "afeadea1-9fb7-4d2d-bcc3-3ef298ed1802";
		source.setStartDate(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getStartDate());
		assertNotNull(target.getStartDate());
		assertEquals(target.getStartDate(), param);
	}
}
