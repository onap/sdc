/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Telstra Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Assert;
import org.junit.Test;

public class ExternalCategoryTypeEnumTest {

	private static final String PARTNER_DOMAIN_SERVICE_UPPER = "Partner Domain Service";
	private static final String PARTNER_DOMAIN_SERVICE_LOWER = "partner domain service";
	private ExternalCategoryTypeEnum createTestSubject() {
		return ExternalCategoryTypeEnum.PARTNER_DOMAIN_SERVICE;
	}

	@Test
	public void testGetValue()  {
		ExternalCategoryTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
		Assert.assertSame(result, ExternalCategoryTypeEnum.PARTNER_DOMAIN_SERVICE.getValue());
	}

	@Test
	public void testIsAtomicType() {
		ExternalCategoryTypeEnum testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isAtomicType();
		Assert.assertSame(result,true);
	}

	@Test
	public void testGetInvalidType() {
		String type = "Invalid Service";
		ExternalCategoryTypeEnum result;

		// default test
		result = ExternalCategoryTypeEnum.getType(type);
		Assert.assertSame(result, null);
		Assert.assertNull(result);
	}

	@Test
	public void testGetValidType()  {
		String type = "PARTNER_DOMAIN_SERVICE";
		ExternalCategoryTypeEnum result;

		// default test
		result = ExternalCategoryTypeEnum.getType(type);
		Assert.assertSame(result, ExternalCategoryTypeEnum.PARTNER_DOMAIN_SERVICE);

	}

	@Test
	public void testGetTypeByName() {
		String type = "";
		ExternalCategoryTypeEnum result;

		// default test
		result = ExternalCategoryTypeEnum.getType(type);
		Assert.assertSame(result, null);
		result = ExternalCategoryTypeEnum.getTypeByName(ExternalCategoryTypeEnumTest.PARTNER_DOMAIN_SERVICE_UPPER);
		Assert.assertSame(result, ExternalCategoryTypeEnum.PARTNER_DOMAIN_SERVICE);

	}

	@Test
	public void testGetTypeIgnoreCase() {
		String type = "";
		ExternalCategoryTypeEnum result;

		// default test
		result = ExternalCategoryTypeEnum.getTypeIgnoreCase(type);
		Assert.assertSame(result, null);
		result = ExternalCategoryTypeEnum.getTypeIgnoreCase(ExternalCategoryTypeEnumTest.PARTNER_DOMAIN_SERVICE_LOWER);
		Assert.assertSame(result,ExternalCategoryTypeEnum.PARTNER_DOMAIN_SERVICE );
	}

	@Test
	public void testContainsName() {
		String type = "";
		boolean result;

		// default test
		result = ExternalCategoryTypeEnum.containsName(type);
		Assert.assertSame(result,false);
		result = ExternalCategoryTypeEnum.containsName(ExternalCategoryTypeEnumTest.PARTNER_DOMAIN_SERVICE_LOWER);
		Assert.assertSame(result,false);
	}

	@Test
	public void testContainsIgnoreCase() {
		String type = "";
		boolean result;

		// default test
		result = ExternalCategoryTypeEnum.containsIgnoreCase(type);
		Assert.assertSame(result,false);
		result = ExternalCategoryTypeEnum.containsIgnoreCase(ExternalCategoryTypeEnumTest.PARTNER_DOMAIN_SERVICE_LOWER);
		Assert.assertSame(result,true);
		result = ExternalCategoryTypeEnum.containsIgnoreCase(ExternalCategoryTypeEnumTest.PARTNER_DOMAIN_SERVICE_UPPER);
		Assert.assertSame(result,true);
	}
}

