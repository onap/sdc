package org.openecomp.sdc.be.servlets;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;

public class JerseySpringBaseTest extends JerseyTest {

    protected static HttpServletRequest request;

    @BeforeClass
    public static void initBaseClass() {
        request = mock(HttpServletRequest.class);
    }

    protected ResourceConfig configure() {
        return configure(BaseTestConfig.class);
    }

    protected ResourceConfig configure(Class<?> springConfig) {
        ApplicationContext context = new AnnotationConfigApplicationContext(springConfig);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return new ResourceConfig()
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(request).to(HttpServletRequest.class);
                    }
                })
                .register(DefaultExceptionMapper.class)
                .register(ComponentExceptionMapper.class)
                .register(StorageExceptionMapper.class)
                .property("jersey.config.server.provider.classnames", "org.openecomp.sdc.be.view.MixinModelWriter")
                .property("contextConfig", context);
    }

}
