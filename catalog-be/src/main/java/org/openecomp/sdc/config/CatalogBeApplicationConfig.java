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

package org.openecomp.sdc.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.openecomp.sdc.be.components.distribution.engine.config.DistributionEngineSpringConfig;
import org.openecomp.sdc.be.components.impl.lock.ComponentLockAspect;
import org.openecomp.sdc.be.config.CatalogModelSpringConfig;
import org.openecomp.sdc.be.dao.config.DAOSpringConfig;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJPointcutAdvisor;
import org.springframework.aop.config.SimpleBeanFactoryAwareAspectInstanceFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

/**
 * Root application context of the catalog-be web application, loaded by the {@code ContextLoaderListener}
 * in {@code web.xml}.
 */
// @formatter:off
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan({
    "org.openecomp.sdc.be.components.health",
    "org.openecomp.sdc.be.servlets",
    "org.openecomp.sdc.be.externalapi.servlet",
    "org.openecomp.sdc.be.components.scheduledtasks",
    "org.openecomp.sdc.be.components.kafka",
    "org.openecomp.sdc.be.components.impl"})
@Import({
    DAOSpringConfig.class,
    CatalogModelSpringConfig.class,
    DistributionEngineSpringConfig.class,
    CatalogBESpringConfig.class,
    MicrometerSpringConfig.class})
// @formatter:on
public class CatalogBeApplicationConfig {

    private static final String LOCKING_TRANSACTION =
        "@annotation(org.openecomp.sdc.be.components.impl.lock.LockingTransactional) and args(componentId, componentType,..)";

    /**
     * Applies {@link ComponentLockAspect#lock} around every {@code @LockingTransactional} method. Built by hand
     * rather than by annotating the aspect with {@code @Aspect}: the jcabi {@code ajc} step in this module's build
     * would then also weave it into the classes at compile time, as an AspectJ singleton it cannot instantiate.
     */
    @Bean
    public AspectJPointcutAdvisor lockingTransactionAdvisor(BeanFactory beanFactory) throws NoSuchMethodException {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(LOCKING_TRANSACTION);
        SimpleBeanFactoryAwareAspectInstanceFactory aspect = new SimpleBeanFactoryAwareAspectInstanceFactory();
        aspect.setAspectBeanName("componentLockAspect");
        aspect.setBeanFactory(beanFactory);
        AspectJAroundAdvice advice = new AspectJAroundAdvice(
            ComponentLockAspect.class.getMethod("lock", ProceedingJoinPoint.class, String.class, ComponentTypeEnum.class), pointcut, aspect);
        advice.setAspectName("componentLockAspect");
        advice.setArgumentNames("proceedingJoinPoint,componentId,componentType");
        return new AspectJPointcutAdvisor(advice);
    }
}
