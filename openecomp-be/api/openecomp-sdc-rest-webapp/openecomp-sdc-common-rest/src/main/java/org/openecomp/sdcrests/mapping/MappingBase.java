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

package org.openecomp.sdcrests.mapping;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerServiceName;

/**
 * Base class for all mapping classes. Mapping classes will perform data mapping from source object
 *  to target object Base class provides following<br>  <ol>  <li>provides life cycle of
 * mapping class , first mapSimpleProperties is called and then  mapComplexProperties is
 * called.</li>  <li>methods mapSimpleProperties and mapComplexProperties with default
 * implementation, these should  be overridden by concrete mapping classes for writing mapping
 * logic.</li>  </ol>
 *
 *
 */

public abstract class MappingBase<S, T> {

  /**
   * Method is called for starting mapping from source object to target object method sets context
   *  in the thread locale and than calls mapSimpleProperties and mapComplexProperties
   * respectively.
   *
   * @param source : source object for mapping
   * @param clazz  : target <code>Class</code> for mapping
   * @return <code>T</code> - instance of type <code>T</code>
   */

  public final T applyMapping(final S source, Class<T> clazz) {
    T target = (T) instantiateTarget(clazz);
    if (source == null || target == null) {
      //TODO: what what?
    } else {
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
  }

  /**
   * The actual method that does the mapping between the <code>source</code> to <code>target</code>
   * objects.  This method is being called automatically as part of the mapper class.  This
   * method must be override (it is abstract) by the mapper class.
   *
   * @param source - the source object.
   * @param target - the target object.
   */

  public abstract void doMapping(final S source, T target);

  /**
   * This method is called after the <code>doMapping</code> method.
   */

  public void postMapping(final S source, T target) {
  }

  /**
   * Creates the instance of the input class.
   *
   * @return <code>Object</code>
   */

  private Object instantiateTarget(final Class<?> clazz) {
    Object object = null;
    try {
      object = clazz.newInstance();

    } catch (InstantiationException | IllegalAccessException exception ) {
      //TODO: what what?
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(
          LoggerConstants.TARGET_ENTITY,
          LoggerServiceName.Create_LIMIT.toString(), ErrorLevel.ERROR.name(),
          exception.getMessage(), exception.getMessage());

      throw new CoreException((new ErrorCode.ErrorCodeBuilder())
          .withMessage(exception.getMessage())
          .withId(exception.getMessage())
          .withCategory(ErrorCategory.APPLICATION).build());


    }
    return object;

  }

}

