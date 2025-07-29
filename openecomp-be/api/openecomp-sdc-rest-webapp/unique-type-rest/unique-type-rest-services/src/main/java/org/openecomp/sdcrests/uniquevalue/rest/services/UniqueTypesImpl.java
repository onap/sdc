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

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import javax.inject.Named;
import org.openecomp.core.dao.UniqueValueDaoFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdcrests.errors.ErrorCodeAndMessage;
import org.openecomp.sdcrests.uniquevalue.rest.UniqueTypes;
import org.openecomp.sdcrests.uniquevalue.types.UniqueTypesProvider;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.context.annotation.ScopedProxyMode;
@Named
@Service("uniqueTypes")
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
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
    public ResponseEntity<GenericCollectionWrapper<String>> listUniqueTypes(String user) {
        GenericCollectionWrapper<String> wrapper = new GenericCollectionWrapper<>(
                new ArrayList<>(UNIQUE_TYPE_TO_INTERNAL.keySet())
        );
        return ResponseEntity.ok(wrapper);
       // return Response.ok(new GenericCollectionWrapper<>(new ArrayList<>(UNIQUE_TYPE_TO_INTERNAL.keySet()))).build();
    }

    @Override
    public ResponseEntity getUniqueValue(String type, String value, String user) {
        String internalType = UNIQUE_TYPE_TO_INTERNAL.get(type);
        if (internalType == null) {
            ErrorCode error = new ErrorCode.ErrorCodeBuilder()
                    .withCategory(ErrorCategory.APPLICATION)
                    .withId(UNIQUE_TYPE_NOT_FOUND_ERR_ID)
                    .withMessage(String.format(UNIQUE_TYPE_NOT_FOUND_MSG, type))
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorCodeAndMessage(HttpStatus.NOT_FOUND,error));
        }
        return ResponseEntity.ok(Collections.singletonMap("occupied", getUniqueValueUtil().isUniqueValueOccupied(internalType, value)));
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
