package org.openecomp.server.configuration;

import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration exists because @SpringBootTest does not load the
 * spring boot app's main class. In the main class, the UserStartupListener is registered
 * that invokes the SessionContextProviderFactory before the application context
 * is being bootstrapped.
 */
@Configuration
public class SessionContextConfiguration {

    public static final String SESSION_CONTEXT_PROVIDER_INIT_BEAN = "onboarding-session-context-provider-init";

    @Bean(SESSION_CONTEXT_PROVIDER_INIT_BEAN)
    void sessionContextConfig() {
        SessionContextProviderFactory.getInstance().createInterface().create("onboarding", "dox");
    }
}
