package org.openecomp.sdc.tosca.datatypes;

import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.junit.Assert;
import org.junit.Test;

public class PropertyTypeTest {
  @Test
  public void shouldReturnNullWhenDisplayNameDoesNotExistForAnyProperty() {
    String s = "blabla";
    Assert.assertEquals(PropertyType.getPropertyTypeByDisplayName(s), null);
  }

  @Test
  public void shouldReturnApproppriatePropertyTypeWhenDisplayNameExist() {
    String s = "scalar-unit.size";
    Assert
        .assertEquals(PropertyType.getPropertyTypeByDisplayName(s), PropertyType.SCALAR_UNIT_SIZE);
  }
}