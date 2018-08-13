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

package org.openecomp.core.factory.impl;

import static org.openecomp.core.utilities.CommonMethods.newInstance;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public abstract class AbstractFactoryBase {

    /**
   * Temporary registry of default implementations. The map keeps class names rather then class
   * types to allow unloading of those classes from memory by garbage collector if factory is not
   * actually used.
   */
  private static final Map<String, String> REGISTRY = new ConcurrentHashMap<>();

  /**
   * Cached factory instances.
   */
  private static final Map<String, AbstractFactoryBase> FACTORY_MAP = new ConcurrentHashMap<>();
    public static final String E0001 = "E0001";

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
          new ErrorCode.ErrorCodeBuilder().withId(E0001).withMessage("Mandatory input factory.")
              .withCategory(ErrorCategory.SYSTEM).build());
    }

    if (impl == null) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withId(E0001).withMessage("Mandatory input impl.")
              .withCategory(ErrorCategory.SYSTEM).build());
    }
    if (FACTORY_MAP.containsKey(factory.getName())) {
      FACTORY_MAP.remove(factory.getName());
    }
    REGISTRY.put(factory.getName(), impl.getName());
  } // registerFactory

  // TODO: Remove
  protected static void registerFactory(String factoryName, String implName) {
    REGISTRY.put(factoryName, implName);
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
          new ErrorCode.ErrorCodeBuilder().withId(E0001).withMessage("Mandatory input factory.")
              .withCategory(ErrorCategory.SYSTEM).build());
    }

    FACTORY_MAP.remove(factory.getName());
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
          new ErrorCode.ErrorCodeBuilder().withId(E0001)
              .withMessage("Mandatory input factory type.").withCategory(ErrorCategory.SYSTEM)
              .build());

    }
    // Pick up factory instance from cache
    F factory = (F) FACTORY_MAP.get(factoryType.getName());
    // Check for the first time access
    if (factory == null) {
      // Synchronize factory instantiation
      synchronized (FACTORY_MAP) {
        // Re-check the factory instance
        factory = (F) FACTORY_MAP.get(factoryType.getName());
        if (factory == null) {
          // Get the implementation class name
          String implName = REGISTRY.get(factoryType.getName());

          if (StringUtils.isEmpty(implName)) {
            throw new CoreException(
                new ErrorCode.ErrorCodeBuilder().withId(E0001)
                    .withMessage("Mandatory input factory implementation.")
                    .withCategory(ErrorCategory.SYSTEM).build());
          }

          factory = newInstance(implName, factoryType);

          factory.init();

          // Cache the instantiated singleton
          FACTORY_MAP.put(factoryType.getName(), factory);
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
          new ErrorCode.ErrorCodeBuilder().withId(E0001)
              .withMessage("Mandatory input factory type.").withCategory(ErrorCategory.SYSTEM)
              .build());
    }
    // Pick up factory instance from cache
    F factory = (F) FACTORY_MAP.get(factoryType.getName());
    // Check for the first time access
    if (factory != null) {
      isFactoryRegistered = true;
    } else {
      // Get the implementation class name
      String implName = REGISTRY.get(factoryType.getName());
      if (StringUtils.isNotEmpty(implName)) {
        isFactoryRegistered = true;
      }
    }
    return isFactoryRegistered;
  }

  /**
   * Stop all.
   */
  public static void stopAll() {
    Collection<AbstractFactoryBase> factorylist = FACTORY_MAP.values();
    for (AbstractFactoryBase factory : factorylist) {
      factory.stop();
    }
  }

  protected void init() {
    // allows custom initialization
    // noop by default
  }

  protected void stop() {
    // allows custom shutdown
    // noop by default
  }

}
