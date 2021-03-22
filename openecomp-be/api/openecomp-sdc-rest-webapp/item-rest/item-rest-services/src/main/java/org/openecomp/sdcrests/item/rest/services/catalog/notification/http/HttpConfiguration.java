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
package org.openecomp.sdcrests.item.rest.services.catalog.notification.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents configuration for sending notifications to the Catalog side.
 *
 * @author evitaliy
 * @since 21 Nov 2018
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HttpConfiguration {

    private String catalogBeProtocol;
    private String catalogBeHttpPort;
    private String catalogBeSslPort;
    private String catalogBeFqdn;
    private String catalogNotificationUrl;
}
