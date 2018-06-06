package org.openecomp.sdc.be.model.tosca.validators;

import static org.junit.Assert.assertFalse;

import java.util.Collections;

import org.junit.Test;

public class MapValidatorTest {

    @Test
    public void isValid_nonMapString() {
        assertFalse(MapValidator.getInstance().isValid("abc", "string", Collections.emptyMap()));
        assertFalse(MapValidator.getInstance().isValid("1", "string", Collections.emptyMap()));
    }
}