package org.openecomp.core.converter.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

public class SubstitutionMappingsConverterErrorBuilder extends BaseErrorBuilder {
  private static final String SUB_MAPPINGS_CAPABILITY_REQUIREMENT_ENTRY_VALUE_ILLEGAL = "%s value" +
      " in substitution mappings is invalid, expected it to be %s";
  private static final String IMPORT_TOSCA = "IMPORT_TOSCA";


  public SubstitutionMappingsConverterErrorBuilder(String section,
                                                   String expectedType) {
    getErrorCodeBuilder()
        .withId(IMPORT_TOSCA)
        .withCategory(ErrorCategory.APPLICATION)
        .withMessage(String.format(SUB_MAPPINGS_CAPABILITY_REQUIREMENT_ENTRY_VALUE_ILLEGAL, section, expectedType));

  }
}
