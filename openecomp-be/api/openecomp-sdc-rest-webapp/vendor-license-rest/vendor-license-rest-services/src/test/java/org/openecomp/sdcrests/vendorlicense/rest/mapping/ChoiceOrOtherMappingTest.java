package org.openecomp.sdcrests.vendorlicense.rest.mapping;

import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdcrests.vendorlicense.types.ChoiceOrOtherDto;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Any change to ChoiceOrOther easily break reconstruction of objects of this type.
 * This test protects from such accidental changes.
 *
 * @author EVITALIY
 * @since 28 Dec 17
 */
public class ChoiceOrOtherMappingTest {

    private static final String UNKNOWN = "Unknown";

    public enum TestEnum {
        Yes
    }

    @Test
    public void testApplyMappingFromDto() {

        ChoiceOrOtherDto<TestEnum> source = new ChoiceOrOtherDto<>();
        source.setChoice(TestEnum.Yes);
        source.setOther(UNKNOWN);

        ChoiceOrOther<TestEnum> expected = new ChoiceOrOther<>(TestEnum.Yes, UNKNOWN);

        ChoiceOrOther result = new MapChoiceOrOtherDtoToChoiceOrOther().applyMapping(source, ChoiceOrOther.class);
        assertEquals(result, expected);
    }

    @Test
    public void testApplyMappingToDto() {

        ChoiceOrOther<TestEnum> source = new ChoiceOrOther<>(TestEnum.Yes, UNKNOWN);

        ChoiceOrOtherDto<TestEnum> expected = new ChoiceOrOtherDto<>();
        expected.setChoice(TestEnum.Yes);
        expected.setOther(UNKNOWN);

        ChoiceOrOtherDto result = new MapChoiceOrOtherToChoiceOrOtherDto().applyMapping(source, ChoiceOrOtherDto.class);
        assertEquals(result, expected);
    }
}