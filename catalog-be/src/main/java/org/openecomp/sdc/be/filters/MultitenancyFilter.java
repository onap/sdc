package org.openecomp.sdc.be.filters;


import org.keycloak.adapters.*;
import org.keycloak.adapters.servlet.FilterRequestAuthenticator;
import org.keycloak.adapters.servlet.OIDCFilterSessionStore;
import org.keycloak.adapters.servlet.OIDCServletHttpFacade;
import org.keycloak.adapters.spi.*;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.common.util.Multitenancy;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class MultitenancyFilter implements Filter {

    private final static Logger log = Logger.getLogger("" + MultitenancyFilter.class);
    public static final org.openecomp.sdc.logging.api.Logger LOGGER = LoggerFactory.getLogger(MultitenancyFilter.class);

    public static final String SKIP_PATTERN_PARAM = "keycloak.config.skipPattern";

    public static final String ID_MAPPER_PARAM = "keycloak.config.idMapper";

    public static final String CONFIG_RESOLVER_PARAM = "keycloak.config.resolver";

    public static final String CONFIG_FILE_PARAM = "keycloak.config.file";

    public static final String CONFIG_PATH_PARAM = "keycloak.config.path";

    protected AdapterDeploymentContext deploymentContext;

    protected SessionIdMapper idMapper = new InMemorySessionIdMapper();

    protected NodesRegistrationManagement nodesRegistrationManagement;

    protected Pattern skipPattern;

    private final KeycloakConfigResolver definedconfigResolver;

    boolean keycloak;

    /**
     * Constructor that can be used to define a {@code KeycloakConfigResolver} that will be used at initialization to
     * provide the {@code KeycloakDeployment}.
     * @param definedconfigResolver the resolver
     */
    public MultitenancyFilter(KeycloakConfigResolver definedconfigResolver) {
        this.definedconfigResolver = definedconfigResolver;
    }

    public MultitenancyFilter() {
        this(null);
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        String skipPatternDefinition = filterConfig.getInitParameter(SKIP_PATTERN_PARAM);
        if (skipPatternDefinition != null) {
            skipPattern = Pattern.compile(skipPatternDefinition, Pattern.DOTALL);
        }

        String idMapperClassName = filterConfig.getInitParameter(ID_MAPPER_PARAM);
        if (idMapperClassName != null) {
            try {
                final Class<?> idMapperClass = getClass().getClassLoader().loadClass(idMapperClassName);
                final Constructor<?> idMapperConstructor = idMapperClass.getDeclaredConstructor();
                Object idMapperInstance = null;
                // for KEYCLOAK-13745 test
                if (idMapperConstructor.getModifiers() == Modifier.PRIVATE) {
                    idMapperInstance = idMapperClass.getMethod("getInstance").invoke(null);
                } else {
                    idMapperInstance = idMapperConstructor.newInstance();
                }
                if(idMapperInstance instanceof SessionIdMapper) {
                    this.idMapper = (SessionIdMapper) idMapperInstance;
                } else {
                    log.log(Level.WARNING, "SessionIdMapper class {0} is not instance of org.keycloak.adapters.spi.SessionIdMapper", idMapperClassName);
                }
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.log(Level.WARNING, "SessionIdMapper class could not be instanced", e);
            }
        }

        if (definedconfigResolver != null) {
            deploymentContext = new AdapterDeploymentContext(definedconfigResolver);
            log.log(Level.INFO, "Using {0} to resolve Keycloak configuration on a per-request basis.", definedconfigResolver.getClass());
        } else {
            String configResolverClass = filterConfig.getInitParameter(CONFIG_RESOLVER_PARAM);
            if (configResolverClass != null) {
                try {
                    KeycloakConfigResolver configResolver = (KeycloakConfigResolver) getClass().getClassLoader().loadClass(configResolverClass).newInstance();
                    deploymentContext = new AdapterDeploymentContext(configResolver);
                    log.log(Level.INFO, "Using {0} to resolve Keycloak configuration on a per-request basis.", configResolverClass);
                } catch (Exception ex) {
                    log.log(Level.FINE, "The specified resolver {0} could NOT be loaded. Keycloak is unconfigured and will deny all requests. Reason: {1}", new Object[]{configResolverClass, ex.getMessage()});
                    deploymentContext = new AdapterDeploymentContext(new KeycloakDeployment());
                }
            } else {
                String fp = filterConfig.getInitParameter(CONFIG_FILE_PARAM);
                InputStream is = null;
                if (fp != null) {
                    try {
                        is = new FileInputStream(fp);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String path = "/WEB-INF/keycloak.json";
                    String pathParam = filterConfig.getInitParameter(CONFIG_PATH_PARAM);
                    if (pathParam != null) path = pathParam;
                    is = filterConfig.getServletContext().getResourceAsStream(path);
                }
                KeycloakDeployment kd = createKeycloakDeploymentFrom(is);
                deploymentContext = new AdapterDeploymentContext(kd);
                log.fine("Keycloak is using a per-deployment configuration.");
            }
        }
        filterConfig.getServletContext().setAttribute(AdapterDeploymentContext.class.getName(), deploymentContext);
        nodesRegistrationManagement = new NodesRegistrationManagement();
    }

    private KeycloakDeployment createKeycloakDeploymentFrom(InputStream is) {
        if (is == null) {
            log.fine("No adapter configuration. Keycloak is unconfigured and will deny all requests.");
            return new KeycloakDeployment();
        }
        return KeycloakDeploymentBuilder.build(is);
    }


    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        log.fine("Keycloak OIDC Filter");
        Multitenancy keyaccess= new Multitenancy();
        keycloak= keyaccess.multitenancycheck();

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (keycloak == false) {
            chain.doFilter(req, res);
            return;
        }

        if (shouldSkip(request)) {
            chain.doFilter(req, res);
            return;
        }

        OIDCServletHttpFacade facade = new OIDCServletHttpFacade(request, response);
        KeycloakDeployment deployment = deploymentContext.resolveDeployment(facade);
        if (deployment == null || !deployment.isConfigured()) {
            response.sendError(403);
            log.fine("deployment not configured");
            return;
        }

        PreAuthActionsHandler preActions = new PreAuthActionsHandler(new org.openecomp.sdc.be.filters.MultitenancyFilter.IdMapperUserSessionManagement(), deploymentContext, facade);

        if (preActions.handleRequest()) {
            //System.err.println("**************** preActions.handleRequest happened!");
            return;
        }


        nodesRegistrationManagement.tryRegister(deployment);
        OIDCFilterSessionStore tokenStore = new OIDCFilterSessionStore(request, facade, 100000, deployment, idMapper);
        tokenStore.checkCurrentToken();


        FilterRequestAuthenticator authenticator = new FilterRequestAuthenticator(deployment, tokenStore, facade, request, 8443);
        AuthOutcome outcome = authenticator.authenticate();
        if (outcome == AuthOutcome.AUTHENTICATED) {
            log.fine("AUTHENTICATED");
            if (facade.isEnded()) {
                return;
            }
            AuthenticatedActionsHandler actions = new AuthenticatedActionsHandler(deployment, facade);
            if (actions.handledRequest()) {
                return;
            } else {
                HttpServletRequestWrapper wrapper = tokenStore.buildWrapper();
                chain.doFilter(wrapper, res);
                return;
            }
        }
        AuthChallenge challenge = authenticator.getChallenge();
        if (challenge != null) {
            log.fine("challenge");
            challenge.challenge(facade);
            return;
        }
        response.sendError(403);

    }

    /**
     * Decides whether this {@link Filter} should skip the given {@link HttpServletRequest} based on the configured {@link org.keycloak.adapters.servlet.KeycloakOIDCFilter#skipPattern}.
     * Patterns are matched against the {@link HttpServletRequest#getRequestURI() requestURI} of a request without the context-path.
     * A request for {@code /myapp/index.html} would be tested with {@code /index.html} against the skip pattern.
     * Skipped requests will not be processed further by {@link org.keycloak.adapters.servlet.KeycloakOIDCFilter} and immediately delegated to the {@link FilterChain}.
     *
     * @param request the request to check
     * @return {@code true} if the request should not be handled,
     * {@code false} otherwise.
     */
    private boolean shouldSkip(HttpServletRequest request) {

        if (skipPattern == null) {
            return false;
        }

        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        return skipPattern.matcher(requestPath).matches();
    }

    @Override
    public void destroy() {

    }

    private class IdMapperUserSessionManagement implements UserSessionManagement {
        @Override
        public void logoutAll() {
            if (idMapper != null) {
                idMapper.clear();
            }
        }

        @Override
        public void logoutHttpSessions(List<String> ids) {
            log.fine("**************** logoutHttpSessions");
            //System.err.println("**************** logoutHttpSessions");
            for (String id : ids) {
                log.finest("removed idMapper: " + id);
                idMapper.removeSession(id);
            }

        }
    }
}
