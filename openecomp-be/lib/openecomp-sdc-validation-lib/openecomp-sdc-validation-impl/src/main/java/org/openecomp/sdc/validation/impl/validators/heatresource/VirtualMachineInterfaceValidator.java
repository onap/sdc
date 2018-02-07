/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.validation.impl.validators.heatresource;

import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.DefinedHeatParameterTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.impl.util.HeatValidationService;
import org.openecomp.sdc.validation.type.HeatResourceValidationContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class VirtualMachineInterfaceValidator implements ResourceValidator {
  private static final ErrorMessageCode ERROR_CODE_VLAN1 = new ErrorMessageCode("VLAN1");
  private static final ErrorMessageCode ERROR_CODE_VLAN2 = new ErrorMessageCode("VLAN2");


  @Override
  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {
    if (ToggleableFeature.VLAN_TAGGING.isActive()) {
      HeatResourceValidationContext heatResourceValidationContext =
          (HeatResourceValidationContext) validationContext;
      Optional<Object> tagPropertyValue = getVlanTagPropertyValue(resourceEntry.getValue());

      tagPropertyValue
          .ifPresent(o -> validateHasSingleParentPort(fileName, resourceEntry, globalContext,
              heatResourceValidationContext));
      validateHasTwoProperties(fileName, resourceEntry, globalContext);

    }
  }


  private void validateHasSingleParentPort(String fileName,
                                           Map.Entry<String, Resource> resourceEntry,
                                           GlobalValidationContext globalContext,
                                           HeatResourceValidationContext heatResourceValidationContext) {
    Object refsPropertyValue = resourceEntry.getValue().getProperties()
        .get(HeatConstants.VMI_REFS_PROPERTY_NAME);
    if (Objects.isNull(refsPropertyValue)) {
      return;
    }
    boolean hasSingleParentPort= HeatValidationService.hasSingleParentPort(fileName, globalContext,
        heatResourceValidationContext,
            refsPropertyValue);
    if (!hasSingleParentPort) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(ERROR_CODE_VLAN1,
              Messages.VLAN_SUBINTERFACE_MORE_THAN_ONE_PORT.getErrorMessage(),
              resourceEntry.getKey()));
    }


  }


  private void validateHasTwoProperties(String fileName, Map.Entry<String, Resource> resourceEntry,
                                        GlobalValidationContext globalContext) {

    Optional<Object> refsPropertyValue = getRefsPropertyValue(resourceEntry.getValue());
    Optional<Object> tagPropertyValue = getVlanTagPropertyValue(resourceEntry.getValue());


    if (refsPropertyValue.isPresent() && !tagPropertyValue.isPresent()) {
      globalContext
          .addMessage(fileName, ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(
                      ERROR_CODE_VLAN2,
                      Messages.VLAN_SUBINTERFACE_MISSING_TAG_PROPERTY.getErrorMessage(),
                      resourceEntry.getKey())
          );

    } else if (!refsPropertyValue.isPresent() && tagPropertyValue.isPresent()) {
      globalContext
          .addMessage(fileName, ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(
                      ERROR_CODE_VLAN2,
                      Messages.VLAN_SUBINTERFACE_MISSING_REFS_PROPERTY.getErrorMessage(),
                      resourceEntry.getKey()));

    }

  }


  private Optional<Object> getVlanTagPropertyValue(Resource resource) {
    Object vmiProperties = resource.getProperties()
        .get(HeatConstants.VMI_PROPERTIES_PROPERTY_NAME);
    if (Objects.nonNull(vmiProperties) && vmiProperties instanceof Map) {
      return Optional.ofNullable(((Map) vmiProperties)
          .get(HeatConstants.VMI_SUB_INTERFACE_VLAN_TAG_PROPERTY_NAME));
    }
    return Optional.empty();
  }

  private Optional<Object> getRefsPropertyValue(Resource resource) {
    Object refsProperty = resource.getProperties()
        .get(HeatConstants.VMI_REFS_PROPERTY_NAME);
    return Optional.ofNullable(refsProperty);

  }


  private enum Messages {
    VLAN_SUBINTERFACE_MORE_THAN_ONE_PORT(
        "More than one parent port found, there should be only one parent port for a VLAN sub-interface ID [%s]"),
    VLAN_SUBINTERFACE_MISSING_TAG_PROPERTY("VLAN Tag property " +
        "virtual_machine_interface_properties_sub_interface_vlan_tag is missing in VLAN Resource ID [%s]"),
    VLAN_SUBINTERFACE_MISSING_REFS_PROPERTY("Parent port property virtual_machine_interface_refs " +
        "is missing in VLAN Resource ID [%s]");

    String getErrorMessage() {
      return errorMessage;
    }

    private final String errorMessage;

    Messages(String errorMessage) {
      this.errorMessage = errorMessage;
    }

  }

}
