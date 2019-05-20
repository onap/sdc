package org.openecomp.sdc.be.components.path;


import com.google.common.collect.Sets;
import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ForwardingPathValidatorTest implements ForwardingPathTestUtils {

    ResponseFormatManager mock;

    private Service  service = (Service) getToscaFullElement().left().value();


    @Mock
    ToscaOperationFacade toscaOperationFacade;
    @InjectMocks
    ForwardingPathValidationUtilTest test = new ForwardingPathValidationUtilTest();

    private static final String SERVICE_ID = "serviceid1";




    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mock = Mockito.mock(ResponseFormatManager.class);
        when(toscaOperationFacade.getToscaElement(any(), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(mock.getResponseFormat(any())).thenReturn(new ResponseFormat());
        when(mock.getResponseFormat(any(), any())).thenReturn(new ResponseFormat());
        when(mock.getResponseFormat(any(), any(), any())).thenReturn(new ResponseFormat());

    }

    @Test
    public void testValidForwardingPathName(){

        Collection<ForwardingPathDataDefinition> paths = createData("pathName", "http", "8285", "pathName");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = test.validateForwardingPaths(paths, SERVICE_ID, false);
        assertTrue(booleanResponseFormatEither.isLeft());
    }

    @Test
    public void testEmptyForwardingPathName(){
        Collection<ForwardingPathDataDefinition> paths = createData("", "protocol", "8285", "name1");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither =   test
                .validateForwardingPaths(paths, SERVICE_ID, false);
        assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void testLongForwardingPathName(){
        String pathName = "Failed to execute goal on project catalog-be: Could not resolve dependencies for project \n" +
                "org.openecomp.sdc:catalog-be:war:1.1.0-SNAPSHOT: Failed to collect dependencies at \n" +
                "org.openecomp.sdc.common:openecomp-sdc-artifact-generator-api:jar:1802.0.1.167: ";
        Collection<ForwardingPathDataDefinition> paths = createData(pathName,
                "http", "port", "name1");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither =   test
                .validateForwardingPaths(paths, SERVICE_ID, false);
        assertTrue(booleanResponseFormatEither.isRight());

    }

    @Test
    public void testUniqueForwardingPathNameUpdateName(){

        Collection<ForwardingPathDataDefinition> paths = createData("pathName4", "httpfd", "82df85", "name1");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = test.validateForwardingPaths(paths, SERVICE_ID, true);
        assertTrue(booleanResponseFormatEither.isLeft());

    }

    @Test
    public void testUniqueForwardingPathNameUpdatePort(){

        Collection<ForwardingPathDataDefinition> paths = createData("pathName3", "httpfd", "82df85", "name1");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = test.validateForwardingPaths(paths, SERVICE_ID, true);
        assertTrue(booleanResponseFormatEither.isLeft());

    }

    @Test
    public void testLongForwardingPathPortNumber(){
        String port = "Failed to execute goal on project catalog-be: Could not resolve dependencies for project \n" +
                "org.openecomp.sdc:catalog-be:war:1.1.0-SNAPSHOT: Failed to collect dependencies at \n" +
                "org.openecomp.sdc.common:openecomp-sdc-artifact-generator-api:jar:1802.0.1.167: ";
        Collection<ForwardingPathDataDefinition> paths = createData("pathName",
                "http", port, "name1");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = test.validateForwardingPaths(paths, SERVICE_ID, false);
        assertTrue(booleanResponseFormatEither.isRight());

    }

    @Test
    public void testLongForwardingPathProtocol(){
        String protocol = "Failed to execute goal on project catalog-be: Could not resolve dependencies for project \n" +
                "org.openecomp.sdc:catalog-be:war:1.1.0-SNAPSHOT: Failed to collect dependencies at \n" +
                "org.openecomp.sdc.common:openecomp-sdc-artifact-generator-api:jar:1802.0.1.167: ";
        Collection<ForwardingPathDataDefinition> paths = createData("pathName",
                protocol, "port", "name1");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither =  test.validateForwardingPaths(paths, SERVICE_ID, false);
        assertTrue(booleanResponseFormatEither.isRight());

    }

    private Set<ForwardingPathDataDefinition> createData(String pathName, String protocol, String ports, String uniqueId) {

        return Sets.newHashSet(createPath(pathName, protocol, ports, uniqueId));
    }


    private  <T extends Component> Either<T, StorageOperationStatus> getToscaFullElement() {

        return Either.left((T) setUpServiceMcok());
    }

    private Service setUpServiceMcok(){
    Service service = new Service();
    service.addForwardingPath(createPath("pathName3", "http", "8285", "name1"));
    return  service;
    }

    private class ForwardingPathValidationUtilTest extends ForwardingPathValidator {

        protected ResponseFormatManager getResponseFormatManager() {
            return mock;
        }
    }

}
