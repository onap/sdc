/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.openecomp.sdc.be.wiring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.openecomp.sdc.be.components.impl.ComponentLocker;
import org.openecomp.sdc.be.components.impl.lock.ComponentLockAspect;
import org.openecomp.sdc.be.components.impl.lock.LockingTransactional;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.config.CatalogBeApplicationConfig;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Starts only the AOP part of the production context - its auto-proxy settings and lock advisor, plus the
 * lock aspect over a mocked {@link ComponentLocker} - to prove that {@code @LockingTransactional} methods are
 * really advised. {@code ComponentLockAspectTest} calls the advice directly and cannot notice a pointcut that no
 * longer applies.
 */
class ComponentLockAspectWiringTest {

    private final ComponentLocker componentLocker = mock(ComponentLocker.class);
    private GenericApplicationContext context;

    @BeforeEach
    void startAopInfrastructure() throws Exception {
        context = new GenericApplicationContext();
        EnableAspectJAutoProxy autoProxy = CatalogBeApplicationConfig.class.getAnnotation(EnableAspectJAutoProxy.class);
        AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(context);
        if (autoProxy.proxyTargetClass()) {
            AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(context);
        }
        context.getBeanFactory().registerSingleton("componentLockAspect", new ComponentLockAspect(componentLocker));
        context.getBeanFactory().registerSingleton("lockingTransactionAdvisor",
            new CatalogBeApplicationConfig().lockingTransactionAdvisor(context.getBeanFactory()));
        context.registerBeanDefinition("lockedOperations", new RootBeanDefinition(LockedOperations.class));
        context.refresh();
    }

    @AfterEach
    void stop() {
        context.close();
    }

    @Test
    void lockingTransactionalMethodRunsBetweenLockAndUnlock() {
        LockedOperations operations = context.getBean(LockedOperations.class);

        assertEquals("updated", operations.update("componentId", ComponentTypeEnum.SERVICE, "value"));

        InOrder order = inOrder(componentLocker);
        order.verify(componentLocker).lock("componentId", NodeTypeEnum.Service);
        order.verify(componentLocker).unlock("componentId", NodeTypeEnum.Service);
        order.verifyNoMoreInteractions();
    }

    @Test
    void methodWithoutTheAnnotationIsNotLocked() {
        context.getBean(LockedOperations.class).read("componentId", ComponentTypeEnum.SERVICE);

        verifyNoInteractions(componentLocker);
    }

    @Test
    void everyLockingTransactionalMethodInCatalogBeMatchesThePointcut() throws Exception {
        Map<String, PointcutAdvisor> advisors = context.getBeansOfType(PointcutAdvisor.class);
        assertEquals(1, advisors.size(), "advisors: " + advisors.keySet());
        PointcutAdvisor advisor = advisors.values().iterator().next();

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((reader, readerFactory) -> reader.getAnnotationMetadata().hasAnnotatedMethods(LockingTransactional.class.getName()));
        List<String> unmatched = new ArrayList<>();
        int annotated = 0;
        for (var candidate : scanner.findCandidateComponents("org.openecomp.sdc.be.components")) {
            Class<?> type = Class.forName(candidate.getBeanClassName());
            for (Method method : type.getDeclaredMethods()) {
                if (method.isAnnotationPresent(LockingTransactional.class)) {
                    annotated++;
                    if (!AopUtils.canApply(advisor, type) || !advisor.getPointcut().getMethodMatcher().matches(method, type)) {
                        unmatched.add(type.getSimpleName() + "#" + method.getName());
                    }
                }
            }
        }
        assertTrue(annotated > 0, "no @LockingTransactional method found; the scan is broken");
        assertTrue(unmatched.isEmpty(), "@LockingTransactional but not advised: " + unmatched);
        assertFalse(advisor.getPointcut().getMethodMatcher()
            .matches(LockedOperations.class.getMethod("read", String.class, ComponentTypeEnum.class), LockedOperations.class));
    }

    public static class LockedOperations {

        @LockingTransactional
        public String update(String componentId, ComponentTypeEnum componentType, String value) {
            return "updated";
        }

        public String read(String componentId, ComponentTypeEnum componentType) {
            return "read";
        }
    }
}
