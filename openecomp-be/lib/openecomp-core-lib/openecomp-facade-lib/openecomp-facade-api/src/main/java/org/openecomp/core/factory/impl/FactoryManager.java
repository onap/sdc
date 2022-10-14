/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.core.factory.impl;

import com.amdocs.zusammen.utils.facade.impl.FactoryConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class FactoryManager {

    private static final FactoryManager instance = new FactoryManager();
    private static final String ERROR_CODE_E0001 = "E0001";
    /**
     * Temporary registry of default implementations. The map keeps class names rather then class types to allow unloading of those classes from
     * memory by garbage collector if factory is not actually used.
     */
    private final Map<String, String> factoryRegistry = new ConcurrentHashMap<>();
    /**
     * Cached factory instances.
     */
    private final Map<String, AbstractFactoryBase> factoryInstanceMap = new ConcurrentHashMap<>();

    private FactoryManager() {
        initializeFactoryRegistry();
    }

    public static synchronized FactoryManager getInstance() {
        return instance;
    }

    private void initializeFactoryRegistry() {
        final Map<String, String> factoryMap = FactoryConfig.getFactoriesMap();
        for (final Map.Entry<String, String> entry : factoryMap.entrySet()) {
            final String abstractClassName = entry.getKey();
            final String concreteTypeName = entry.getValue();
            if (StringUtils.isEmpty(concreteTypeName)) {
                throw new CoreException(
                    new ErrorCode.ErrorCodeBuilder().withId("E0003").withMessage("Missing configuration value:" + concreteTypeName + ".")
                        .withCategory(ErrorCategory.SYSTEM).build());
            }
            registerFactory(abstractClassName, concreteTypeName);
        }
    }

    /**
     * Instantiates the configured implementation of an abstract factory.
     *
     * @param <F>         Type specific abstract factory for concrete Java interface
     * @param factoryType Java class of type specific abstract factory
     * @return Instance of implementation class
     */
    @SuppressWarnings("unchecked")
    public <F extends AbstractFactoryBase> F getFactoryInstance(Class<F> factoryType) {
        if (factoryType == null) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withId(ERROR_CODE_E0001).withMessage("Mandatory input factory type.")
                .withCategory(ErrorCategory.SYSTEM).build());
        }
        final String factoryTypeName = factoryType.getName();
        // Check if the factory is already cached
        if (factoryInstanceMap.get(factoryTypeName) == null) {
            //if not, create a new one and cache it

            // Get the implementation class name
            final String implName = factoryRegistry.get(factoryTypeName);
            if (StringUtils.isEmpty(implName)) {
                throw new CoreException(
                    new ErrorCode.ErrorCodeBuilder().withId(ERROR_CODE_E0001).withMessage("Mandatory input factory implementation.")
                        .withCategory(ErrorCategory.SYSTEM).build());
            }
            F factory = CommonMethods.newInstance(implName, factoryType);
            factory.init();
            // Cache the instantiated singleton
            factoryInstanceMap.putIfAbsent(factoryTypeName, factory);
        }
        return (F) factoryInstanceMap.get(factoryTypeName);
    }

    public void registerFactory(final String factoryName, final String implName) {
        factoryRegistry.put(factoryName, implName);
    }

    /**
     * Unregister factory and removes the cached instance if any.
     *
     * @param factoryName the factory name to unregister
     */
    public void unregisterFactory(final String factoryName) {
        final String factoryClass = factoryRegistry.get(factoryName);
        if (StringUtils.isNotEmpty(factoryClass)) {
            factoryInstanceMap.remove(factoryClass);
            factoryRegistry.remove(factoryName);
        }
    }

    /**
     * Removes the cached factory instance if any.
     *
     * @param <F>     the type parameter
     * @param factory the factory
     */
    public <F extends AbstractFactoryBase> void removeFactoryInstance(Class<F> factory) {
        if (factory == null) {
            throw new CoreException(
                new ErrorCode.ErrorCodeBuilder().withId(ERROR_CODE_E0001).withMessage("Mandatory input factory.").withCategory(ErrorCategory.SYSTEM)
                    .build());
        }
        factoryInstanceMap.remove(factory.getName());
    }

    /**
     * Stop all.
     */
    public void stopAll() {
        factoryInstanceMap.values().forEach(AbstractFactoryBase::stop);
    }
}
