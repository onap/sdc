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
 * ================================================================================
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdcrests.uniquevalue.rest.services;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.openecomp.core.dao.UniqueValueDaoFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.common.errors.ErrorCodeAndMessage;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdcrests.uniquevalue.rest.UniqueTypes;
import org.openecomp.sdcrests.uniquevalue.types.UniqueTypesProvider;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Named
@Service("uniqueTypes")
@Scope(value = "prototype")
public class UniqueTypesImpl implements UniqueTypes {

    private static final String UNIQUE_TYPE_NOT_FOUND_ERR_ID = "UNIQUE_TYPE_NOT_FOUND";
    private static final String UNIQUE_TYPE_NOT_FOUND_MSG = "%s is not a supported unique type.";
    private static final Map<String, String> UNIQUE_TYPE_TO_INTERNAL;

    static {
        Map<String, String> uniqueTypes = new HashMap<>();
        ServiceLoader.load(UniqueTypesProvider.class).forEach(typesProvider -> uniqueTypes.putAll(typesProvider.listUniqueTypes()));
        UNIQUE_TYPE_TO_INTERNAL = Collections.unmodifiableMap(uniqueTypes);
    }

    private UniqueValueUtil uniqueValueUtil;

    @Override
    public Response listUniqueTypes(String user) {
        return Response.ok(new GenericCollectionWrapper<>(new ArrayList<>(UNIQUE_TYPE_TO_INTERNAL.keySet()))).build();
    }

    @Override
    public Response getUniqueValue(String type, String value, String user) {
        String internalType = UNIQUE_TYPE_TO_INTERNAL.get(type);
        if (internalType == null) {
            ErrorCode error = new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION).withId(UNIQUE_TYPE_NOT_FOUND_ERR_ID)
                .withMessage(String.format(UNIQUE_TYPE_NOT_FOUND_MSG, type)).build();
            return Response.status(NOT_FOUND).entity(new ErrorCodeAndMessage(NOT_FOUND, error)).build();
        }
        return Response.ok(Collections.singletonMap("occupied", getUniqueValueUtil().isUniqueValueOccupied(internalType, value))).build();
    }

    private UniqueValueUtil getUniqueValueUtil() {
        if (uniqueValueUtil == null) {
            uniqueValueUtil = new UniqueValueUtil(UniqueValueDaoFactory.getInstance().createInterface());
        }
        return uniqueValueUtil;
    }

    @VisibleForTesting
    void setUniqueValueUtil(UniqueValueUtil uniqueValueUtil) {
        this.uniqueValueUtil = uniqueValueUtil;
    }
}
