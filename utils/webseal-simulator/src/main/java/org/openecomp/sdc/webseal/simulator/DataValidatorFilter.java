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

package org.openecomp.sdc.webseal.simulator;

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
import org.openecomp.sdc.common.filters.DataValidatorFilterAbstract;
import org.openecomp.sdc.exception.NotAllowedSpecialCharsException;
import org.openecomp.sdc.webseal.simulator.conf.Conf;

/**
 * Implement DataValidatorFilter for webseal.
 * Extends {@link DataValidatorFilterAbstract}
 */
public class DataValidatorFilter extends DataValidatorFilterAbstract {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException, NotAllowedSpecialCharsException {
        try {
            super.doFilter(request, response, chain);
        } catch (final NotAllowedSpecialCharsException e) {
            // error handing to show 'Error: Special characters not allowed.'
            ((HttpServletResponse) response).sendError(400, ERROR_SPECIAL_CHARACTERS_NOT_ALLOWED);
        }
    }

    @Override
    protected List<String> getDataValidatorFilterExcludedUrls() {
        String dataValidatorFilterExcludedUrls = Conf.getInstance().getDataValidatorFilterExcludedUrls();
        if (StringUtils.isNotBlank(dataValidatorFilterExcludedUrls)) {
            return Arrays.asList(dataValidatorFilterExcludedUrls.split(","));
        }
        return new ArrayList<>();
    }
}
