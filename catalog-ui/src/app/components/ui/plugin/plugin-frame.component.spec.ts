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

import * as _ from 'lodash';
import {of, Subject} from 'rxjs';
import {PluginFrameComponent, pluginDisplayName} from './plugin-frame.component';

(window as any)._ = _;

describe('plugin frame URL and lifecycle', () => {
    let frame: PluginFrameComponent;
    let status: Subject<boolean>;
    let listener: Function;
    let unregister: jest.Mock;
    let eventBus: any;
    let navigation: any;

    beforeEach(() => {
        status = new Subject<boolean>();
        unregister = jest.fn();
        eventBus = {on: jest.fn(), off: jest.fn(), notify: jest.fn(() => of(undefined)), unregister: jest.fn(), NoWindowOutEvents: [], lastEventNotified: 'READY'};
        navigation = {onNavigationStart: jest.fn(callback => {listener = callback; return unregister;}), navigate: jest.fn()};
        frame = new PluginFrameComponent(eventBus, {isPluginOnline: () => status} as any, navigation);
        frame.plugin = {pluginId: 'workflow', pluginSourceUrl: 'https://plugin.test/frame'} as any;
    });

    it('preserves reserved punctuation, spaces, repeated inputs and empty query values in the emitted URL', () => {
        frame.queryParams = {context: 'a b+c&d=e', reserved: '@:$,;?/', repeated: ['one', 'two'], empty: '', absent: null, missing: undefined};
        frame.ngOnInit();
        expect(frame.pluginUrl).toBeUndefined();
        status.next(true);
        expect(frame.pluginUrl).toBe('https://plugin.test/frame?context=a%20b+c%26d=e&reserved=@:$,;?/&repeated=one,two&empty=');
    });

    it('waits for WINDOW_OUT before navigating and removes the listener on destruction', () => {
        frame.ngOnInit();
        status.next(true);
        const event = {fromState: 'plugins', toState: 'home', fromParams: {}, toParams: {}, preventDefault: jest.fn()};
        listener(event);
        expect(event.preventDefault).toHaveBeenCalled();
        expect(eventBus.notify).toHaveBeenCalledWith('WINDOW_OUT');
        expect(eventBus.unregister).toHaveBeenCalledWith('workflow');
        expect(navigation.navigate).toHaveBeenCalledWith('home', {});
        frame.ngOnDestroy();
        expect(unregister).toHaveBeenCalledTimes(1);
    });

    it('finishes loading without initializing an offline plugin', () => {
        const done = jest.fn();
        frame.onLoadingDone.subscribe(done);
        frame.ngOnInit();
        status.next(false);
        expect(done).toHaveBeenCalledTimes(1);
        expect(frame.pluginUrl).toBeUndefined();
        expect(navigation.onNavigationStart).not.toHaveBeenCalled();
    });
});

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
