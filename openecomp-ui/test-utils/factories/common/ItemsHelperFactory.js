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

import { Factory } from 'rosie';
import randomstring from 'randomstring';
import IdMixin from 'test-utils/factories/mixins/IdMixin.js';

export const itemFactory = new Factory()
    .extend(IdMixin)
    .option('isArchived', false)
    .attrs({
        description: () => randomstring.generate(),
        name: () => randomstring.generate(),
        owner: () => randomstring.generate()
    })
    .attr('status', ['isArchived'], isArchived => {
        return isArchived ? 'ARCHIVE' : 'ACTIVE';
    });

export const vspFactory = new Factory().extend(itemFactory).attrs({
    type: 'vsp',
    properties: {
        onboardingMethod: 'NetworkPackage',
        vendorId: randomstring.generate(33),
        vendorName: randomstring.generate()
    }
});

export const vlmFactory = new Factory().extend(itemFactory).attrs({
    type: 'vlm',
    properties: {}
});
