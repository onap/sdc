/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import { Factory } from 'rosie';
import IdMixin from 'test-utils/factories/mixins/IdMixin.js';
import randomstring from 'randomstring';
import {relationTypes} from 'sdc-app/onboarding/softwareProduct/dependencies/SoftwareProductDependenciesConstants.js';

const SoftwareProductDependenciesBaseFactory = new Factory()
    .attrs({ sourceId: () => randomstring.generate(),
        targetId: () => randomstring.generate(),
        relationType: relationTypes.DEPENDS_ON
    }).extend(IdMixin);

export const SoftwareProductDependenciesResponseFactory = new Factory()
    .extend(SoftwareProductDependenciesBaseFactory).extend(IdMixin);

export const SoftwareProductDependenciesStoreFactory = new Factory()
.extend(SoftwareProductDependenciesBaseFactory)
.extend(IdMixin)
.attrs({ hasCycle: false });
