package org.openecomp.sdc.ci.tests.servicefilter;

import java.util.Collections;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.PropertyReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceFilterDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.PropertyRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceFilterUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ServiceFilterTests extends ComponentBaseTest {
    @Rule
    public static TestName name = new TestName();

    private static ServiceReqDetails externalService;
    private static ComponentInstanceReqDetails componentInstanceReqDetails;
    private static User user = null;

    public ServiceFilterTests() {
        super(name, ServiceFilterTests.class.getName());
    }

    @BeforeTest
    public void init() throws Exception {
        user = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

        ServiceReqDetails internalService;
        //Create External Service
        externalService = ElementFactory.getDefaultService();
        externalService.setName("ExternalService" + Math.random());
        ServiceRestUtils.createService(externalService, user);

        //Create Internal Service
        internalService = ElementFactory.getDefaultService();
        internalService.setName("InternalService" + Math.random());
        ServiceRestUtils.createService(internalService, user);

        //Add property services
        //#PropertyOne
        PropertyReqDetails propertyReqDetails = ElementFactory.getDefaultStringProperty();
        propertyReqDetails.setName("StringProp1");
        String body = propertyReqDetails.propertyToJsonString();
        PropertyRestUtils.createServiceProperty(externalService.getUniqueId(), body, user);
        PropertyRestUtils.createServiceProperty(internalService.getUniqueId(), body, user);
        //#PropertyTwo
        propertyReqDetails.setName("StringProp2");
        body = propertyReqDetails.propertyToJsonString();
        RestResponse response = PropertyRestUtils.createServiceProperty(externalService.getUniqueId(), body, user);
        response = PropertyRestUtils.createServiceProperty(internalService.getUniqueId(), body, user);

        //CheckIn internal Service
        response = LifecycleRestUtils.changeServiceState(internalService, user, "0.1",
                LifeCycleStatesEnum.CHECKIN,
                "{\"userRemarks\":\"CheckIn\"}");
        BaseRestUtils.checkSuccess(response);
        if (response.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS) {
            internalService.setUniqueId(ResponseParser.getUniqueIdFromResponse(response));
        }
        //Make internal service as component instance
        componentInstanceReqDetails =
                ElementFactory.getDefaultComponentInstance(internalService.getUniqueId(), "ServiceProxy");
        response = ComponentInstanceRestUtils.createComponentInstance(componentInstanceReqDetails,
                user, externalService.getUniqueId(), ComponentTypeEnum.SERVICE);
        BaseRestUtils.checkCreateResponse(response);
        if (response.getErrorCode() == BaseRestUtils.STATUS_CODE_CREATED) {
            componentInstanceReqDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(response));
            componentInstanceReqDetails.setName(ResponseParser.getNameFromResponse(response));
        }
        //Mark as dependent
        componentInstanceReqDetails.setDirectives(Collections.singletonList("selectable"));
        response = ComponentInstanceRestUtils.updateComponentInstance(componentInstanceReqDetails,
                user, externalService.getUniqueId(), ComponentTypeEnum.SERVICE);
        BaseRestUtils.checkSuccess(response);
    }

    @Test
    public void createServiceFilter() throws Exception {
        //Add Service Filter
        ServiceFilterDetails serviceFilterDetails = ElementFactory.getDefaultEqualOperatorFilter("StringProp1", "value");
        RestResponse restResponse = ServiceFilterUtils.createServiceFilter(externalService.getUniqueId(),
                componentInstanceReqDetails.getUniqueId(), serviceFilterDetails, user);
        logger.info("CreateServiceFilter Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "createServiceFilter")
    public void updateServiceFilter() throws Exception {
        //Update Service Filter
        ServiceFilterDetails serviceFilterDetails =
                ElementFactory.getDefaultEqualOperatorFilter("StringProp1", "updated");
        RestResponse restResponse = ServiceFilterUtils.updateServiceFilter(externalService.getUniqueId(),
                componentInstanceReqDetails.getUniqueId(), Collections.singletonList(serviceFilterDetails),  user);
        logger.info("UpdateServiceFilter Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    //    @Test(dependsOnMethods = "updateServiceFilter")
    public void deleteServiceFilter() throws Exception {
        //Delete Service Filter
        RestResponse restResponse = ServiceFilterUtils.deleteServiceFilter(externalService.getUniqueId(),
                componentInstanceReqDetails.getUniqueId(), 0, user);
        logger.info("DeleteServiceFilter Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }
}