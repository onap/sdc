package org.openecomp.sdcrests.vendorlicense.rest.mapping;

import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdcrests.vendorlicense.types.ChoiceOrOtherDto;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author EVITALIY
 * @since 28 Dec 17
 */
public class MapChoiceOrOtherDtoToChoiceOrOtherTest {

    private static final String UNKNOWN = "Unknown";

    public enum TestEnum {
        Yes
    }

    @Test
    public void testApplyMapping() {

        // Any change to ChoiceOrOther easily break reconstruction of objects of this type.
        // This test protects from such accidental changes.

        ChoiceOrOtherDto<TestEnum> source = new ChoiceOrOtherDto<>();
        source.setChoice(TestEnum.Yes);
        source.setOther(UNKNOWN);

        ChoiceOrOther<TestEnum> expected = new ChoiceOrOther<>(TestEnum.Yes, UNKNOWN);

        ChoiceOrOther result = new MapChoiceOrOtherDtoToChoiceOrOther().applyMapping(source, ChoiceOrOther.class);
        assertEquals(result, expected);
    }
}