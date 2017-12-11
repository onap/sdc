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

package org.openecomp.sdc.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * <p>The list of markers that can be used for special logging such as metrics, audit, etc.</p>
 *
 * <p>Although markers can be easily instantiated whenever needed, having constants for them helps eliminate mistakes -
 * misspelling, using a marker that is not handled, etc.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *
 *     Logger log = LogFactory.getLogger(this.getClass());
 *     log.info(Markers.AUDIT, "User '{}' logged out", user);
 *
 * </pre>
 *
 * @author EVITALIY
 * @since 13/09/2016.
 *
 * @see Marker
 */
public class Markers {

    public static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");
    public static final Marker METRICS = MarkerFactory.getMarker("METRICS");
}
