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
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelEntityDto;
import org.junit.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


public class MapVendorLicenseModelEntityToDtoTest {

	@Test
	public void testIconRef() throws Exception {
		VendorLicenseModelEntity source = new VendorLicenseModelEntity();
		VendorLicenseModelEntityDto target = new VendorLicenseModelEntityDto();
		MapVendorLicenseModelEntityToDto mapper = new MapVendorLicenseModelEntityToDto();
		mapper.doMapping(source, target);
		assertEquals(target.getIconRef(), source.getIconRef());
		String param = "3d3aed28-341a-490e-94b0-c7d4a520b64a";
		source.setIconRef(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getIconRef());
		assertNotNull(target.getIconRef());
		assertEquals(target.getIconRef(), param);
	}

	@Test
	public void testDescription() throws Exception {
		VendorLicenseModelEntity source = new VendorLicenseModelEntity();
		VendorLicenseModelEntityDto target = new VendorLicenseModelEntityDto();
		MapVendorLicenseModelEntityToDto mapper = new MapVendorLicenseModelEntityToDto();
		mapper.doMapping(source, target);
		assertEquals(target.getDescription(), source.getDescription());
		String param = "68dc3641-94ad-4109-8082-62037720739b";
		source.setDescription(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getDescription());
		assertNotNull(target.getDescription());
		assertEquals(target.getDescription(), param);
	}

	@Test
	public void testId() throws Exception {
		VendorLicenseModelEntity source = new VendorLicenseModelEntity();
		VendorLicenseModelEntityDto target = new VendorLicenseModelEntityDto();
		MapVendorLicenseModelEntityToDto mapper = new MapVendorLicenseModelEntityToDto();
		mapper.doMapping(source, target);
		assertEquals(target.getId(), source.getId());
		String param = "019b68df-92d9-40a6-a5b0-951d64838ce9";
		source.setId(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getId());
		assertNotNull(target.getId());
		assertEquals(target.getId(), param);
	}

	@Test
	public void testVendorName() throws Exception {
		VendorLicenseModelEntity source = new VendorLicenseModelEntity();
		VendorLicenseModelEntityDto target = new VendorLicenseModelEntityDto();
		MapVendorLicenseModelEntityToDto mapper = new MapVendorLicenseModelEntityToDto();
		mapper.doMapping(source, target);
		assertEquals(target.getVendorName(), source.getVendorName());
		String param = "99319b1a-5cea-459d-aff6-7a4be4f318c0";
		source.setVendorName(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getVendorName());
		assertNotNull(target.getVendorName());
		assertEquals(target.getVendorName(), param);
	}
}
