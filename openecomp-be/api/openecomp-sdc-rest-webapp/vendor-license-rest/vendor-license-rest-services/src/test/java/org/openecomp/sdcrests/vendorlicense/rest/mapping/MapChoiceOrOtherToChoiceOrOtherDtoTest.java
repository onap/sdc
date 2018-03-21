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
import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdcrests.vendorlicense.types.ChoiceOrOtherDto;
import org.junit.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


public class MapChoiceOrOtherToChoiceOrOtherDtoTest {
	public enum TestEnum {
		Yes
	}

	@Test
	public void testOther() throws Exception {
		ChoiceOrOther source = new ChoiceOrOther();
		ChoiceOrOtherDto target = new ChoiceOrOtherDto();
		MapChoiceOrOtherToChoiceOrOtherDto mapper = new MapChoiceOrOtherToChoiceOrOtherDto();
		mapper.doMapping(source, target);
		assertEquals(target.getOther(), source.getOther());
		String param = "768f875f-af54-4cd2-aaa7-75ee7a176031";
		source.setOther(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getOther());
		assertNotNull(target.getOther());
		assertEquals(target.getOther(), param);
	}

	@Test
	public void testChoice() throws Exception {
		ChoiceOrOther source = new ChoiceOrOther();
		ChoiceOrOtherDto target = new ChoiceOrOtherDto();
		MapChoiceOrOtherToChoiceOrOtherDto mapper = new MapChoiceOrOtherToChoiceOrOtherDto();
		mapper.doMapping(source, target);
		assertEquals(target.getChoice(), source.getChoice());
		TestEnum param = TestEnum.Yes;
		source.setChoice(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getChoice());
		assertNotNull(target.getChoice());
		assertEquals(target.getChoice(), param);
	}
}
