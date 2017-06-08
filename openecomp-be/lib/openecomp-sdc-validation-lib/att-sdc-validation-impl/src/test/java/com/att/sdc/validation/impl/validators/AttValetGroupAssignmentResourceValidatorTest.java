package com.att.sdc.validation.impl.validators;

import com.att.sdc.validation.datatypes.AttHeatResourceTypes;
import com.att.sdc.validation.datatypes.AttValetGroupTypeValues;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.core.validation.api.ValidationManager;
import org.openecomp.core.validation.factory.ValidationManagerFactory;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.validation.impl.validators.HeatResourceValidator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class AttValetGroupAssignmentResourceValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  AttValetGroupAssignmentResourceValidator resourceValidator = new
      AttValetGroupAssignmentResourceValidator();

  @Test
  public void testATTValetGroupType() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, AttHeatResourceTypes.ATT_VALET_GROUP_ASSIGNMENT.getType(),
        "/com/att/sdc/validation/impl/validators/att_heat_validator/att_valet_group_type/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, AttHeatResourceTypes.ATT_VALET_GROUP_ASSIGNMENT.getType(),
        "/com/att/sdc/validation/impl/validators/att_heat_validator/att_valet_group_type/negative");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: Unexpected group_type for ATT::Valet::GroupAssignment, Resource ID [valet_group_assignment_illegal_1]");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: Unexpected group_type for ATT::Valet::GroupAssignment, Resource ID [valet_group_assignment_illegal_2]");
  }

  @Test
  public void testValidationWithFullFlowFromValidationFactory() throws IOException {
    GlobalValidationContext globalContext = ValidationTestUtil.createGlobalContextFromPath(
        "/com/att/sdc/validation/impl/validators/att_heat_validator/att_valet_group_type/negative");
    ValidationManager validationManager = ValidationManagerFactory.getInstance().createInterface();
    validationManager.updateGlobalContext(globalContext);
    Map<String, List<ErrorMessage>> messages = validationManager.validate();

    Assert.assertNotNull(messages);
    ErrorMessage excpectedMessage = new ErrorMessage(ErrorLevel.WARNING,
        "WARNING: Unexpected group_type for ATT::Valet::GroupAssignment, Resource ID [valet_group_assignment_illegal_1]");
    Assert.assertTrue(messages.get("first.yaml").contains(excpectedMessage));
  }
}