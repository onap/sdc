package org.openecomp.sdc.be.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;

public class RequirementDefinitionTest {

    @Test
    public void testRequirementDefinition() {
        final RequirementDefinition testSubject = new RequirementDefinition();
        assertThat(testSubject).isNotNull().isInstanceOf(RequirementDefinition.class);
    }

    @Test
    public void testRequirementDefinition_clone() {
        final RequirementDefinition testSubject = new RequirementDefinition(new RequirementDefinition());
        assertThat(testSubject).isNotNull().isInstanceOf(RequirementDefinition.class);
    }

    @Test
    public void testRequirementDefinition_fromRequirementDataDefinition() {
        final RequirementDefinition testSubject = new RequirementDefinition(new RequirementDataDefinition());
        assertThat(testSubject).isNotNull().isInstanceOf(RequirementDefinition.class);
    }
}