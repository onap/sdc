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
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.DefinedHeatParameterTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.type.HeatResourceValidationContext;

import java.util.Iterator;
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

      if (tagPropertyValue.isPresent()){
        validateHasSingleParentPort(fileName, resourceEntry, globalContext,
            heatResourceValidationContext);
      }
      validateHasTwoProperties(fileName, resourceEntry, globalContext);

    }
  }


  private void validateHasSingleParentPort(String fileName,
                                           Map.Entry<String, Resource> resourceEntry,
                                           GlobalValidationContext globalContext,
                                           HeatResourceValidationContext heatResourceValidationContext) {
    Object refsPropertyValue = resourceEntry.getValue().getProperties()
        .get(HeatConstants.VMI_REFS_PROPERTY_NAME);
    boolean hasSingleParentPort = true;
    if (refsPropertyValue != null && refsPropertyValue instanceof List) {
      if (((List) refsPropertyValue).size() == 1) {
        final Object element = ((List) refsPropertyValue).get(0);
        if (isSingleEntryMap(element)) {
          final Set<String> values =
              HeatStructureUtil.getReferencedValuesByFunctionName(fileName, "get_param", element,
                  globalContext);
          if (values.size() != 1) {
            hasSingleParentPort = false;
          } else {
            Iterator<String> ir = values.iterator();
            String param = "";
            while (ir.hasNext()) {
              param = ir.next();
            }

            final boolean isString = heatResourceValidationContext.getHeatOrchestrationTemplate
                ().getParameters().get(param).getType().equals(DefinedHeatParameterTypes.STRING
                .getType());
            if (!isString) {
              hasSingleParentPort = false;
            }
          }
        }
      } else {
        hasSingleParentPort = false;
      }
    }

    if (!hasSingleParentPort) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(ERROR_CODE_VLAN1,
                  Messages.VLAN_SUBINTERFACE_MORE_THAN_ONE_PORT.getErrorMessage(),
                  resourceEntry.getKey()));
    }
  }

  private boolean isSingleEntryMap(Object element) {
    return (element instanceof Map) && ((Map) element).size() == 1;
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


}
