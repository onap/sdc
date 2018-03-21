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
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdcrests.vendorlicense.types.MultiChoiceOrOtherDto;
import org.junit.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


public class MapMultiChoiceOrOtherToMultiChoiceOrOtherDtoTest {

	@Test
	public void testOther() throws Exception {
		MultiChoiceOrOther source = new MultiChoiceOrOther();
		MultiChoiceOrOtherDto target = new MultiChoiceOrOtherDto();
		MapMultiChoiceOrOtherToMultiChoiceOrOtherDto mapper = new MapMultiChoiceOrOtherToMultiChoiceOrOtherDto();
		mapper.doMapping(source, target);
		assertEquals(target.getOther(), source.getOther());
		String param = "32a675f1-c1ab-4d15-a6f4-9c1a07d6ce60";
		source.setOther(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getOther());
		assertNotNull(target.getOther());
		assertEquals(target.getOther(), param);
	}

	@Test
	public void testChoices() throws Exception {
		MultiChoiceOrOther source = new MultiChoiceOrOther();
		MultiChoiceOrOtherDto target = new MultiChoiceOrOtherDto();
		MapMultiChoiceOrOtherToMultiChoiceOrOtherDto mapper = new MapMultiChoiceOrOtherToMultiChoiceOrOtherDto();
		mapper.doMapping(source, target);
		assertEquals(target.getChoices(), source.getChoices());
		Set<String> param = new HashSet<>(Arrays.asList("ed6f5011-d9ef-4f27-8b10-ab8142296a9b"));
		source.setChoices(param);
		mapper.doMapping(source, target);
		assertNotNull(source.getChoices());
		assertNotNull(target.getChoices());
		assertEquals(target.getChoices(), param);
	}
}
