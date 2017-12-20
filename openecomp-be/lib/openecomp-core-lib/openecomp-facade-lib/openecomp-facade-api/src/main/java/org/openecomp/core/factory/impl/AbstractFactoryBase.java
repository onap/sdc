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

package org.openecomp.core.factory.impl;


import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.openecomp.core.utilities.CommonMethods.isEmpty;
import static org.openecomp.core.utilities.CommonMethods.newInstance;

public abstract class AbstractFactoryBase {

  /**
   * Temporary registry of default implementations. The map keeps class names rather then class
   * types to allow unloading of those classes from memory by garbage collector if factory is not
   * actually used.
   */
  private static Map<String, String> registry = new ConcurrentHashMap<String, String>();

  /**
   * Cached factory instances.
   */
  private static Map<String, AbstractFactoryBase> factoryMap =
      new ConcurrentHashMap<String, AbstractFactoryBase>();

  /**
   * Registers implementor for an abstract factory. The method accepts Java classes rather then
   * class names to ensure type safety at compilation time.
   *
   * @param <I>     Java interface type instantiated by abstract factory
   * @param <F>     Type specific abstract factory for concrete Java interface
   * @param factory Java class of a type specific abstract factory
   * @param impl    Java class of type specific factory implementor
   */
  public static <I, F extends AbstractFactoryBase> void registerFactory(Class<F> factory,
                                                                        Class<? extends F> impl) {
    if (factory == null) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withId("E0001").withMessage("Mandatory input factory.")
              .withCategory(ErrorCategory.SYSTEM).build());
    }

    if (impl == null) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withId("E0001").withMessage("Mandatory input impl.")
              .withCategory(ErrorCategory.SYSTEM).build());
    }
    if (factoryMap != null && factoryMap.containsKey(factory.getName())) {
      factoryMap.remove(factory.getName());
    }
    registry.put(factory.getName(), impl.getName());
  } // registerFactory

  // TODO: Remove
  protected static void registerFactory(String factoryName, String implName) {
    registry.put(factoryName, implName);
  } // registerFactory

  /**
   * Unregister factory.
   *
   * @param <F>     the type parameter
   * @param factory the factory
   */
  public static <F extends AbstractFactoryBase> void unregisterFactory(Class<F> factory) {
    if (factory == null) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withId("E0001").withMessage("Mandatory input factory.")
              .withCategory(ErrorCategory.SYSTEM).build());
    }
    if (factoryMap != null) {
      factoryMap.remove(factory.getName());
    }
  }

  /**
   * Instantiates the configured implementation of an abstract factory.
   *
   * @param <I>         Java interface type instantiated by abstract factory
   * @param <F>         Type specific abstract factory for concrete Java interface
   * @param factoryType Java class of type specific abstract factory
   * @return Instance of implementation class
   */
  @SuppressWarnings("unchecked")
  public static <I, F extends AbstractFactoryBase> F getInstance(Class<F> factoryType) {
    if (factoryType == null) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withId("E0001")
              .withMessage("Mandatory input factory type.").withCategory(ErrorCategory.SYSTEM)
              .build());

    }
    // Pick up factory instance from cache
    F factory = (F) factoryMap.get(factoryType.getName());
    // Check for the first time access
    if (factory == null) {
      // Synchronize factory instantiation
      synchronized (factoryType) {
        // Re-check the factory instance
        factory = (F) factoryMap.get(factoryType.getName());
        if (factory == null) {
          // Get the implementation class name
          String implName = registry.get(factoryType.getName());

          if (isEmpty(implName)) {
            throw new CoreException(
                new ErrorCode.ErrorCodeBuilder().withId("E0001")
                    .withMessage("Mandatory input factory implementation.")
                    .withCategory(ErrorCategory.SYSTEM).build());
          }

          factory = newInstance(implName, factoryType);

          factory.init();

          // Cache the instantiated singleton
          factoryMap.put(factoryType.getName(), factory);
        }
      }
    }

    return factory;

  } // getInstance


  /**
   * Is factory registered boolean.
   *
   * @param <F>         the type parameter
   * @param factoryType the factory type
   * @return the boolean
   */
  public static <F extends AbstractFactoryBase> boolean isFactoryRegistered(Class<F> factoryType) {
    boolean isFactoryRegistered = false;
    if (factoryType == null) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withId("E0001")
              .withMessage("Mandatory input factory type.").withCategory(ErrorCategory.SYSTEM)
              .build());
    }
    // Pick up factory instance from cache
    F factory = (F) factoryMap.get(factoryType.getName());
    // Check for the first time access
    if (factory != null) {
      isFactoryRegistered = true;
    } else {
      // Get the implementation class name
      String implName = registry.get(factoryType.getName());
      if (!isEmpty(implName)) {
        isFactoryRegistered = true;
      }
    }
    return isFactoryRegistered;
  }

  /**
   * Stop all.
   */
  public static void stopAll() {
    Collection<AbstractFactoryBase> factorylist = factoryMap.values();
    for (AbstractFactoryBase factory : factorylist) {
      factory.stop();
    }
  }

  protected void init() {
  }

  protected void stop() {
  }

}
