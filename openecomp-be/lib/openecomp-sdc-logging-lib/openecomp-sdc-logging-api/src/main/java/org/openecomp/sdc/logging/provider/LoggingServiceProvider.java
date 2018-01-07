/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.sdc.logging.provider;

/**
 * <p>From the application code (consumer) perspective, logger creation (factory) and logging context are independent
 * services. From the service provider perspective, however, these services are related and must be implemented together
 * using the same underlying mechanism. Therefore, the service provider-facing interface combines the two services
 * &mdash; to eliminate the chance that their implementations don't work well together.</p>
 *
 * @author EVITALIY
 * @since 07 Jan 18
 */
public interface LoggingServiceProvider extends LoggerCreationService, LoggingContextService {
    // single provider must implement two separate consumer services
}
