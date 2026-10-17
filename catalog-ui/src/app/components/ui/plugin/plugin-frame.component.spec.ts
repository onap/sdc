/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import {pluginDisplayName} from './plugin-frame.component';

describe('plugin frame title', () => {
    const plugin: any = {
        pluginId: 'WORKFLOW',
        pluginDisplayOptions: {tab: {displayName: 'WORKFLOW'}, context: {displayName: 'Workflow Designer'}}
    };

    it('uses the display name of the current display type', () => {
        expect(pluginDisplayName(plugin, 'context')).toBe('Workflow Designer');
        expect(pluginDisplayName(plugin, 'tab')).toBe('WORKFLOW');
    });

    it('falls back to any display name, then to the plugin id', () => {
        expect(pluginDisplayName({pluginId: 'P', pluginDisplayOptions: {context: {displayName: 'Ctx'}}} as any, 'tab')).toBe('Ctx');
        expect(pluginDisplayName({pluginId: 'P'} as any, 'tab')).toBe('P');
        expect(pluginDisplayName(undefined)).toBe('');
    });

    it('titles the iframe', () => {
        // tslint:disable-next-line:no-var-requires
        const template: string = require('./plugin-frame.component.html');
        const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');
        expect(doc.querySelector('iframe').getAttribute('[title]')).toBe('frameTitle');
    });
});
