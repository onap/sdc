/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.common.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.CommonConfigurationManager;
import org.openecomp.sdc.common.errors.DefaultExceptionMapper;
import org.openecomp.sdc.exception.NotAllowedSpecialCharsException;

/**
 * Implements DataValidatorFilter for onboarding.
 * Extends {@link DataValidatorFilterAbstract}
 */
public class DataValidatorFilter extends DataValidatorFilterAbstract {

    private final DefaultExceptionMapper defaultExceptionMapper;

    public DataValidatorFilter() {
        defaultExceptionMapper = new DefaultExceptionMapper();
    }

    @Override
    public void doFilter(final ServletRequest request, ServletResponse response, final FilterChain chain)
        throws IOException, ServletException, NotAllowedSpecialCharsException {
        try {
            super.doFilter(request, response, chain);
        } catch (final NotAllowedSpecialCharsException e) {
            defaultExceptionMapper.writeToResponse(e, (HttpServletResponse) response);
        }
    }

    @Override
    protected List<String> getDataValidatorFilterExcludedUrls() {
        final CommonConfigurationManager commonConfigurationManager = CommonConfigurationManager.getInstance();
        if (commonConfigurationManager != null) {
            final String dataValidatorFilterExcludedUrls = commonConfigurationManager.getConfigValue(DATA_VALIDATOR_FILTER_EXCLUDED_URLS, "");
            if (StringUtils.isNotBlank(dataValidatorFilterExcludedUrls)) {
                return Arrays.asList(dataValidatorFilterExcludedUrls.split(","));
            }
        }
        return new ArrayList<>();
    }

}
