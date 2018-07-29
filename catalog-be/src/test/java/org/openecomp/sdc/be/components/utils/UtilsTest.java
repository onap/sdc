package org.openecomp.sdc.be.components.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {

    @Test
    public void testThatEmptyListReturns0() {
        List<String> existingResourceNames = new ArrayList<>();
        int counter = Utils.getNextCounter(existingResourceNames);
        assertThat(counter).isZero();
    }

    @Test
    public void testListWithValidValue() {
        List<String> existingResourceNames = Arrays.asList("d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..0");
        int counter = Utils.getNextCounter(existingResourceNames);
        assertThat(counter).isEqualTo(1);
    }

    @Test
    public void testListWithInvalidSingleValue() {
        List<String> existingResourceNames = Arrays.asList("d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection");
        int counter = Utils.getNextCounter(existingResourceNames);
        assertThat(counter).isEqualTo(1);
    }

    @Test
    public void testListWithValidValues() {
        List<String> existingResourceNames = Arrays.asList("d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..0",
                "d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..10",
                "d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..15",
                "d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..2");
        int counter = Utils.getNextCounter(existingResourceNames);
        assertThat(counter).isEqualTo(16);
    }

    @Test
    public void testListWithInvalidValue() {
        List<String> existingResourceNames = Arrays.asList("d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..0",
                "d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..10",
                "d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection..15",
                "d7f886ce-7e32-4b1f-bfd8-f664b03fee09.ruti..NetworkCollection");
        int counter = Utils.getNextCounter(existingResourceNames);
        assertThat(counter).isEqualTo(16);
    }
}
