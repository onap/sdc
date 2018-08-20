/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.maven.enforcer.rule;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class JarScopeRule implements EnforcerRule {

    private String testJars = null;
    private String thirdPartyJars = null;

    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        Log log = helper.getLog();
        try {
            // get the various expressions out of the helper.
            MavenProject project = (MavenProject) helper.evaluate("${project}");
            final List<Dependency> dependencies = project.getDependencies();
            log.info("Retrieved Project: " + project);

            List<String> testDependencies = getJarsList(testJars);
            List<String> thirdPartyDependencies = getJarsList(thirdPartyJars);

            for (Dependency dependency : dependencies) {
                validateScope(testDependencies, dependency, "test");
                validateScope(thirdPartyDependencies, dependency, "provided");

                if (dependency.getArtifactId().startsWith("openecomp") && dependency.getArtifactId().endsWith("-core")
                            && !"runtime".equals(dependency.getScope())) {
                    throw new EnforcerRuleException("Failing because core module " + dependency.getArtifactId()
                                                            + " scope is not runtime.");
                }
            }
        } catch (ExpressionEvaluationException e) {
            throw new EnforcerRuleException("Unable to lookup an expression " + e.getLocalizedMessage(), e);
        }
    }

    private void validateScope(List<String> jars, Dependency dependency,String scope) throws EnforcerRuleException {
        for (String jar : jars) {
            if (jar.equals(dependency.getArtifactId()) && !scope.equals(dependency.getScope())) {
                throw new EnforcerRuleException("Failing because " + jar + " scope is not test.");
            }
        }
    }

    private List<String> getJarsList(String jar) {
        List<String> jars = new ArrayList<>();
        if (jar != null) {
            if (jar.contains(",")) {
                final String[] split = jar.split(",");
                jars = Arrays.asList(split);
            } else {
                jars.add(jar);
            }
        }
        return jars;
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public boolean isResultValid(EnforcerRule enforcerRule) {
        return false;
    }

    @Override
    public String getCacheId() {
        return null;
    }
}
