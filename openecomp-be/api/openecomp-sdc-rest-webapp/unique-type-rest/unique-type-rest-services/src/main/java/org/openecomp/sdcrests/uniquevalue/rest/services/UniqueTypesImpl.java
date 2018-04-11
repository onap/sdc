/*
 * Copyright © 2018 European Support Limited
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
package org.openecomp.sdcrests.uniquevalue.rest.services;

import org.openecomp.core.dao.UniqueValueDaoFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdcrests.uniquevalue.rest.UniqueTypes;
import org.openecomp.sdcrests.uniquevalue.types.UniqueTypesProvider;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static com.google.common.collect.ObjectArrays.concat;

@Named
@Service("uniqueTypes")
@Scope(value = "prototype")
public class UniqueTypesImpl implements UniqueTypes {

  private static final String UNIQUE_TYPE_NOT_FOUND_ERR_ID = "UNIQUE_TYPE_NOT_FOUND";
  private static final String UNIQUE_TYPE_NOT_FOUND_MSG = "%s is not a supported unique type.";

  private static final Map<String, String> uniqueTypeToInternal = new HashMap<>();

  static {
    ServiceLoader.load(UniqueTypesProvider.class)
        .forEach(typesProvider -> uniqueTypeToInternal.putAll(typesProvider.listUniqueTypes()));
  }

  private final UniqueValueUtil uniqueValueUtil =
      new UniqueValueUtil(UniqueValueDaoFactory.getInstance().createInterface());

  @Override
  public Response listUniqueTypes(String user) {
    return Response.ok(
        new GenericCollectionWrapper<>(new ArrayList<>(uniqueTypeToInternal.keySet()),
            uniqueTypeToInternal.size()))
        .build();
  }

  @Override
  public Response getUniqueValue(String type, String value, String context, String user) {
    String internalType = uniqueTypeToInternal.get(type);

    if (internalType == null) {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder()
          .withCategory(ErrorCategory.APPLICATION)
          .withId(UNIQUE_TYPE_NOT_FOUND_ERR_ID)
          .withMessage(String.format(UNIQUE_TYPE_NOT_FOUND_MSG, type)).build());
    }
    
    return Response.ok(Collections
        .singletonMap("occupied", isUniqueValueOccupied(internalType, value, context))).build();
  }

  private boolean isUniqueValueOccupied(String type, String value, String context) {
    return context == null
        ? uniqueValueUtil.isUniqueValueOccupied(type, value)
        : uniqueValueUtil.isUniqueValueOccupied(type, composeUniqueCombination(context, value));
  }

  private static String[] composeUniqueCombination(String context, String value) {
    return concat(context.split("\\."), value);
  }
}
