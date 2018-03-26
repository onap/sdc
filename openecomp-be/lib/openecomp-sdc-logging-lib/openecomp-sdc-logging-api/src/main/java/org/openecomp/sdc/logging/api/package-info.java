/*
 * Copyright © 2016-2018 European Support Limited
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

/**
 * <p>Client-visible API for logging, implemented according to
 * <a href="https://wiki.onap.org/download/attachments/1015849/ONAP%20application%20logging%20guidelines.pdf?api=v2">
 * ONAP application logging guidelines</a>. The actual implementation is delegated to a service provider bound through
 * the <a href="https://docs.oracle.com/javase/tutorial/ext/basics/spi.html">Java SPI</a> mechanism. The provider must
 * implement {@link org.openecomp.sdc.logging.spi.LoggingServiceProvider}.</p>
 * <p>The logging API collects the following types of data:</p>
 * <ol>
 *     <li>Context that must be propagated throughout the application, and available at any point for debug and error
 *     reporting.</li>
 *     <li>Audit data, reflecting the invocation of a local, usually REST, API.</li>
 *     <li>Metrics data, reflecting the invocation of a remote API by current system (component).</li>
 * </ol>
 * <p>The construction of all three types of data follows the same pattern for consistency. The builder pattern has
 * been chosen over an interface to enable to gracefully add new fields without affecting the client code. Also, the
 * builder can be implemented differently if needed, also without affecting client code. For instance, it may delegate
 * the instantiation and population of a data object to the service provider.</p>
 *
 * @author evitaliy
 * @since 26 Mar 2018
 */
package org.openecomp.sdc.logging.api;