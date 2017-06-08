package org.openecomp.sdc.vendorsoftwareproduct.errors;


import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes
    .CYCLIC_DEPENDENCY_IN_COMPONENTS;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes
    .INVALID_COMPONENT_RELATION_TYPE;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes
    .NO_SOURCE_COMPONENT;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes
    .SAME_SOURCE_TARGET_COMPONENT;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class ComponentDependencyModelErrorBuilder {

  private static final String CYCLIC_DEPENDENCY_COMPONENT_MSG = "Cyclic dependency exists between"
      + " components.";

  private static final String INVALID_REALTION_TYPE_MSG = "Invalid relation type for components.";

  private static final String NO_SOURCE_COMPONENT_MSG = "Source component is mandatory.";

  private static final String SOURCE_TARGET_COMPONENT_EQUAL_MSG = "Source and target components "
      + "are same.";



  public static ErrorCode getcyclicDependencyComponentErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(CYCLIC_DEPENDENCY_IN_COMPONENTS);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(CYCLIC_DEPENDENCY_COMPONENT_MSG));
    return builder.build();
  }

  public static ErrorCode getInvalidRelationTypeErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(INVALID_COMPONENT_RELATION_TYPE);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(INVALID_REALTION_TYPE_MSG));
    return builder.build();
  }

  public static ErrorCode getNoSourceComponentErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(NO_SOURCE_COMPONENT);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(NO_SOURCE_COMPONENT_MSG));
    return builder.build();
  }

  public static ErrorCode getSourceTargetComponentEqualErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(SAME_SOURCE_TARGET_COMPONENT);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(SOURCE_TARGET_COMPONENT_EQUAL_MSG));
    return builder.build();
  }
}
