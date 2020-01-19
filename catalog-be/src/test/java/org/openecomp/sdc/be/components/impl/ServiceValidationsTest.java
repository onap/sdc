package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ServiceValidationsTest extends ServiceBussinessLogicBaseTestSetup {

    @Test
    public void testInvalidEnvironmentContext() {
        Service newService = createServiceObject(false);
        newService.setEnvironmentContext("not valid");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_ENVIRONMENT_CONTEXT, "not valid");
            return;
        }
        fail();
    }

    @Test
    public void testValidEnvironmentContext() {
        Service newService = createServiceObject(false);
        newService.setEnvironmentContext("Critical_Revenue-Bearing");
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getEnvironmentContext()).isEqualTo("Critical_Revenue-Bearing");
    }

    @Test
    public void testCreateServiceWithNoEnvironmentContext() {
        Service newService = createServiceObject(false);
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getEnvironmentContext()).isEqualTo("General_Revenue-Bearing");
    }

    @Test
    public void testInvalidInstantiationType() {
        Service newService = createServiceObject(false);
        newService.setInstantiationType("not valid");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_INSTANTIATION_TYPE, "not valid");
            return;
        }
        fail();
    }

    @Test
    public void testEmptyInstantiationType() {
        Service newService = createServiceObject(false);
        newService.setInstantiationType(null);
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getInstantiationType()).isEqualTo("A-la-carte");
    }

    @Test
    public void testValidInstantiationType() {
        Service newService = createServiceObject(false);
        newService.setInstantiationType("Macro");
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getInstantiationType()).isEqualTo("Macro");
    }

    @Test
    public void testInvalidServiceRole() {
        Service newService = createServiceObject(false);
        newService.setServiceRole("gsg*");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_PROPERY, SERVICE_ROLE);
            return;
        }
        fail();
    }

    @Test
    public void testTooLongServiceRole() {
        Service newService = createServiceObject(false);
        newService.setServiceRole("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.PROPERTY_EXCEEDS_LIMIT, SERVICE_ROLE);
            return;
        }
        fail();
    }

    @Test
    public void testValidServiceRole() {
        Service newService = createServiceObject(false);
        newService.setServiceRole("role");
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getServiceRole()).isEqualTo("role");
    }

    @Test
    public void testInvalidServiceType() {
        Service newService = createServiceObject(false);
        newService.setServiceType("gsg*");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_PROPERY, SERVICE_TYPE);
            return;
        }
        fail();
    }

    @Test
    public void testValidServiceType() {
        Service newService = createServiceObject(false);
        newService.setServiceType("type");
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getServiceType()).isEqualTo("type");
    }

    @Test
    public void testTooLongServiceType() {
        Service newService = createServiceObject(false);
        newService.setServiceType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.PROPERTY_EXCEEDS_LIMIT, SERVICE_TYPE);
            return;
        }
        fail();
    }

    @Test
    public void testEcompGeneratedNamingIsMissing() {
        Service newService = createServiceObject(false);
        newService.setEcompGeneratedNaming(null);
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.MISSING_ECOMP_GENERATED_NAMING);
            return;
        }
        fail();

    }

    @Test
    public void testNamingPolicyWIthEcompNamingFalse() {
        Service newService = createServiceObject(false);
        newService.setEcompGeneratedNaming(false);
        newService.setNamingPolicy("policy");
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getNamingPolicy()).isEqualTo("");
    }

    @Test
    public void testInvalidNamingPolicy() {
        Service newService = createServiceObject(false);
        newService.setNamingPolicy("פוליסי");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_NAMING_POLICY);
            return;
        }
        fail();
    }

    @Test
    public void testTooLongNamingPolicy() {
        Service newService = createServiceObject(false);
        newService.setNamingPolicy("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.NAMING_POLICY_EXCEEDS_LIMIT, "100");
            return;
        }
        fail();
    }
}
