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
package org.openecomp.sdcrests.mapping;

import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCode;

/**
 * Base class for all mapping classes. Mapping classes will perform data mapping from source object to target object Base class provides
 * following<br>
 *  <ol>  <li>provides life cycle of mapping class , first mapSimpleProperties is called and then  mapComplexProperties is called.</li>  <li>methods
 * mapSimpleProperties and mapComplexProperties with default implementation, these should  be overridden by concrete mapping classes for writing
 * mapping logic.</li>  </ol>
 */
public abstract class MappingBase<S, T> {

    /**
     * Method is called for starting mapping from source object to target object method sets context in the thread locale and than calls
     * mapSimpleProperties and mapComplexProperties respectively.
     *
     * @param source : source object for mapping
     * @param clazz  : target <code>Class</code> for mapping
     * @return <code>T</code> - instance of type <code>T</code>
     */
    public final T applyMapping(final S source, Class<T> clazz) {
        T target = (T) instantiateTarget(clazz);
        if (source != null) {
            preMapping(source, target);
            doMapping(source, target);
            postMapping(source, target);
        }
        return target;
    }

    /**
     * This method is called before the <code>doMapping</code> method.
     */
    public void preMapping(final S source, T target) {
        // extension point
    }

    /**
     * The actual method that does the mapping between the <code>source</code> to <code>target</code> objects.  This method is being called
     * automatically as part of the mapper class.  This method must be override (it is abstract) by the mapper class.
     *
     * @param source - the source object.
     * @param target - the target object.
     */
    public abstract void doMapping(final S source, T target);

    /**
     * This method is called after the <code>doMapping</code> method.
     */
    public void postMapping(final S source, T target) {
        // extension point
    }

    /**
     * Creates the instance of the input class.
     *
     * @return <code>Object</code>
     */
    private Object instantiateTarget(final Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException exception) {
            throw new CoreException((new ErrorCode.ErrorCodeBuilder()).withMessage(exception.getMessage()).build(), exception);
        }
    }
}
