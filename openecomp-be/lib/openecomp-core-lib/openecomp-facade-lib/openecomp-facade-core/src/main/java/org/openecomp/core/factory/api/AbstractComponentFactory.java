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

package org.openecomp.core.factory.api;

import org.openecomp.core.factory.FactoryConfig;
import org.openecomp.core.factory.impl.AbstractFactoryBase;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

import java.util.Map;

public abstract class AbstractComponentFactory<I> extends AbstractFactory<I> {

  static {
    Registry registry = new RegistryImpl();
    InitializationHelper.registerFactoryMapping(registry);
  }

  interface Registry {
    void register(String factory, String impl);
  }

  private static class RegistryImpl implements Registry {
    @Override
    public void register(String factory, String impl) {
      AbstractFactoryBase.registerFactory(factory, impl);
    }
  }

  static class InitializationHelper {


    private static boolean isRegistered = false;

    private InitializationHelper() {
    }

    static synchronized boolean registerFactoryMapping(Registry registry) {

      boolean done = !isRegistered;

      if (!isRegistered) {
        registerFactoryMappingImpl(registry);
        isRegistered = true;
      }

      return done;
    }

    private static void registerFactoryMappingImpl(Registry registry) {
      Map<String, String> factoryMap = FactoryConfig.getFactoriesMap();

      try {
        for (Map.Entry<String, String> entry : factoryMap.entrySet()) {
          String abstractClassName = entry.getKey();
          String concreteTypeName = entry.getValue();

          if (CommonMethods.isEmpty(concreteTypeName)) {
            throw new CoreException(
                new ErrorCode.ErrorCodeBuilder().withId("E0003")
                    .withMessage("Missing configuration value:" + concreteTypeName + ".")
                    .withCategory(ErrorCategory.SYSTEM).build());

          }

          registry.register(abstractClassName, concreteTypeName);
        }
      } catch (RuntimeException exception) {
        throw exception;
      } catch (Exception exception) {
        throw new RuntimeException(exception);
      }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> unsecureCast(Class<?> cls) {
      return (Class<T>) cls;
    }

    private static String nameOf(Class<?> clazz) {
      return (clazz != null) ? clazz.getName() : "null";
    }
  }

}
