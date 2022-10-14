/*
 *  Copyright © 2016-2017 European Support Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openecomp.sdc.common.session.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openecomp.sdc.common.session.SessionContext;
import org.openecomp.sdc.common.session.SessionContextProvider;
import org.openecomp.sdc.common.session.User;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCode;

public class AsdcSessionContextProvider implements SessionContextProvider {

    private static final ThreadLocal<String> threadUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> threadTenant = new ThreadLocal<>();

    @Override
    public void create(String userId, String tenant) {
        threadUserId.set(userId);
        threadTenant.set(tenant);
    }

    @Override
    public SessionContext get() {
        if (threadUserId.get() == null) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withMessage("UserId was not set " + "for this thread").build());
        }
        if (threadTenant.get() == null) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withMessage("Tenant was not set " + "for this thread").build());
        }
        return new AsdcSessionContext(new User(threadUserId.get()), threadTenant.get());
    }

    @Override
    public void close() {
        threadUserId.remove();
        threadTenant.remove();
    }

    @Getter
    @AllArgsConstructor
    private static class AsdcSessionContext implements SessionContext {

        private final User user;
        private final String tenant;
    }
}
