/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.model.tosca.constraints;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to validate constraints types.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstraintUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConstraintUtil.class);

    /**
     * Validates that the {@link ToscaType} specified is a {@link ToscaType#STRING}.
     *
     * @param propertyType The property tosca type.
     * @throws ConstraintValueDoNotMatchPropertyTypeException In case the type is not {@link ToscaType#STRING}.
     */
    public static void checkStringType(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        if (ToscaType.STRING != propertyType) {
            throw new ConstraintValueDoNotMatchPropertyTypeException("Invalid property type <" + propertyType.toString() + ">");
        }
    }
    
    public static boolean isComparableType(final ToscaType propertyType)  {
        final ToscaType toscaType = ToscaType.getToscaType(propertyType.getType());
        switch (toscaType) {
            case FLOAT:
            case DOUBLE:
            case INTEGER:
            case LONG:
            case TIMESTAMP:
            case VERSION:
            case STRING:
            case SCALAR_UNIT_SIZE:
            case SCALAR_UNIT_TIME:
            case SCALAR_UNIT_BITRATE:
            case SCALAR_UNIT_FREQUENCY:
                return true;
            default:
                return false;
        }
    }

    /**
     * Verify that the given tosca type is supported for comparison
     *
     * @param propertyType the tosca type to check
     * @throws ConstraintValueDoNotMatchPropertyTypeException if the property type cannot be compared
     */
    public static void checkComparableType(final ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        if (!isComparableType(propertyType) && !ToscaType.BOOLEAN.equals(propertyType)) {
            throw new ConstraintValueDoNotMatchPropertyTypeException("Constraint is invalid for property type <" + propertyType.getType() + ">");
        }
    }

    /**
     * Convert a string value following its type throw exception if it cannot be converted to a comparable
     *
     * @param propertyType the type of the property
     * @param value        the value to convert
     * @return the converted comparable
     * @throws ConstraintValueDoNotMatchPropertyTypeException if the converted value is not a comparable
     */
    @SuppressWarnings("rawtypes")
    public static Comparable convertToComparable(ToscaType propertyType, String value) {
        final Object comparableObj = propertyType.convert(value);
        if (!(comparableObj instanceof Comparable)) {
            throw new IllegalArgumentException("Try to convert a value of a type which is not comparable [" + propertyType + "] to Comparable");
        } else {
            return (Comparable) comparableObj;
        }
    }

    public static ConstraintInformation getConstraintInformation(Object constraint) throws IntrospectionException {
        PropertyDescriptor firstDescriptor = null;
        for (final PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(constraint.getClass()).getPropertyDescriptors()) {
            if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null) {
                firstDescriptor = propertyDescriptor;
                break;
            }
        }
        if (firstDescriptor == null) {
            throw new IntrospectionException("Cannot find constraint name");
        }
        try {
            return new ConstraintInformation(firstDescriptor.getName(), firstDescriptor.getReadMethod().invoke(constraint), null, null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IntrospectionException("Cannot retrieve constraint reference " + e.getMessage());
        }
    }

    public static <T> T parseToCollection(String value, TypeReference<T> typeReference) throws ConstraintValueDoNotMatchPropertyTypeException {
        T objectMap;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMap = objectMapper.readValue(value, typeReference);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ConstraintValueDoNotMatchPropertyTypeException("The value [" + value + "] is not valid");
        }
        return objectMap;
    }

    @AllArgsConstructor
    public static class ConstraintInformation {

        private final String name;
        private final Object reference;
        private final String value;
        private final String type;
    }
}
