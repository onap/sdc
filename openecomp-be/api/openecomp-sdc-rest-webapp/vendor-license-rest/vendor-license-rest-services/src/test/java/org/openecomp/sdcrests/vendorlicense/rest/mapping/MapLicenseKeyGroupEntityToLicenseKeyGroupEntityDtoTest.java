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
import org.openecomp.sdc.vendorlicense.dao.types.*;
import org.openecomp.sdcrests.vendorlicense.types.LicenseKeyGroupEntityDto;
import org.junit.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


public class MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDtoTest {

	@Test
	public void testReferencingFeatureGroups() throws Exception {
		LicenseKeyGroupEntity source = new LicenseKeyGroupEntity();
		LicenseKeyGroupEntityDto target = new LicenseKeyGroupEntityDto();
		MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto mapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getReferencingFeatureGroups(), source.getReferencingFeatureGroups());
		Set<String> param = new HashSet<>(Arrays.asList("c7797917-7548-44c2-93b9-cf72f27bca3f"));
		source.setReferencingFeatureGroups(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getReferencingFeatureGroups());
		assertNotNull(target.getReferencingFeatureGroups());
		assertEquals(target.getReferencingFeatureGroups(), param);
	}

	@Test
	public void testDescription() throws Exception {
		LicenseKeyGroupEntity source = new LicenseKeyGroupEntity();
		LicenseKeyGroupEntityDto target = new LicenseKeyGroupEntityDto();
		MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto mapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getDescription(), source.getDescription());
		String param = "f026c265-d91f-4c47-8f3b-2600be5023dc";
		source.setDescription(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getDescription());
		assertNotNull(target.getDescription());
		assertEquals(target.getDescription(), param);
	}

	@Test
	public void testType() throws Exception {
		LicenseKeyGroupEntity source = new LicenseKeyGroupEntity();
		LicenseKeyGroupEntityDto target = new LicenseKeyGroupEntityDto();
		MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto mapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getType(), source.getType());
		LicenseKeyType licenseKeyType = LicenseKeyType.Universal;
		source.setType(licenseKeyType);
		mapper.doMapping(source, target);
		assertNotNull(source.getType());
		assertNotNull(target.getType());
		assertEquals(target.getType(), licenseKeyType);
	}

	@Test
	public void testIncrements() throws Exception {
		LicenseKeyGroupEntity source = new LicenseKeyGroupEntity();
		LicenseKeyGroupEntityDto target = new LicenseKeyGroupEntityDto();
		MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto mapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getIncrements(), source.getIncrements());
		String param = "813639e7-45d6-4443-8a7c-a955d34a931a";
		source.setIncrements(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getIncrements());
		assertNotNull(target.getIncrements());
		assertEquals(target.getIncrements(), param);
	}

	@Test
	public void testExpiryDate() throws Exception {
		LicenseKeyGroupEntity source = new LicenseKeyGroupEntity();
		LicenseKeyGroupEntityDto target = new LicenseKeyGroupEntityDto();
		MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto mapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getExpiryDate(), source.getExpiryDate());
		String param = "d7925c8e-239f-44a3-9b24-67ddbd9467c2";
		source.setExpiryDate(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getExpiryDate());
		assertNotNull(target.getExpiryDate());
		assertEquals(target.getExpiryDate(), param);
	}

	@Test
	public void testId() throws Exception {
		LicenseKeyGroupEntity source = new LicenseKeyGroupEntity();
		LicenseKeyGroupEntityDto target = new LicenseKeyGroupEntityDto();
		MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto mapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getId(), source.getId());
		String param = "b2f35aa5-0240-46b7-bade-35890d46e9f9";
		source.setId(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getId());
		assertNotNull(target.getId());
		assertEquals(target.getId(), param);
	}

	@Test
	public void testThresholdUnits() throws Exception {
		LicenseKeyGroupEntity source = new LicenseKeyGroupEntity();
		LicenseKeyGroupEntityDto target = new LicenseKeyGroupEntityDto();
		MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto mapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
		ThresholdUnit thresholdUnit = ThresholdUnit.Absolute;
		source.setThresholdUnits(thresholdUnit);
		mapper.doMapping(source, target);
		assertNotNull(source.getThresholdUnits());
		assertNotNull(target.getThresholdUnits());
		assertEquals(target.getThresholdUnits(), thresholdUnit);
	}

	@Test
	public void testThresholdValue() throws Exception {
		LicenseKeyGroupEntity source = new LicenseKeyGroupEntity();
		LicenseKeyGroupEntityDto target = new LicenseKeyGroupEntityDto();
		MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto mapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getThresholdValue(), source.getThresholdValue());
		Integer param = 1144092791;
		source.setThresholdValue(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getThresholdValue());
		assertNotNull(target.getThresholdValue());
		assertEquals(target.getThresholdValue(), param);
	}

	@Test
	public void testName() throws Exception {
		LicenseKeyGroupEntity source = new LicenseKeyGroupEntity();
		LicenseKeyGroupEntityDto target = new LicenseKeyGroupEntityDto();
		MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto mapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getName(), source.getName());
		String param = "8fb07080-e7ea-4543-812c-1ecdbb9e82c9";
		source.setName(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getName());
		assertNotNull(target.getName());
		assertEquals(target.getName(), param);
	}

	@Test
	public void testOperationalScope() throws Exception {
		LicenseKeyGroupEntity source = new LicenseKeyGroupEntity();
		LicenseKeyGroupEntityDto target = new LicenseKeyGroupEntityDto();
		MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto mapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
		MultiChoiceOrOther<OperationalScope> operationalScopeMultiChoiceOrOther = new MultiChoiceOrOther<>();
		source.setOperationalScope(operationalScopeMultiChoiceOrOther);
		mapper.doMapping(source, target);
		assertNotNull(source.getOperationalScope());
		assertNotNull(target.getOperationalScope());
		assertEquals(target.getOperationalScope().getChoices(), operationalScopeMultiChoiceOrOther.getChoices());
	}

	@Test
	public void testStartDate() throws Exception {
		LicenseKeyGroupEntity source = new LicenseKeyGroupEntity();
		LicenseKeyGroupEntityDto target = new LicenseKeyGroupEntityDto();
		MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto mapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
		mapper.doMapping(source, target);
		assertEquals(target.getStartDate(), source.getStartDate());
		String param = "75f2ae76-b32f-4ac1-92f6-a9d92cb41160";
		source.setStartDate(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getStartDate());
		assertNotNull(target.getStartDate());
		assertEquals(target.getStartDate(), param);
	}
}
