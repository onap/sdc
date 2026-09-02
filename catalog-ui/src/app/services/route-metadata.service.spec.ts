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

import {NavigationEnd, NavigationStart} from '@angular/router';
import {Subject} from 'rxjs/Subject';
import {RouteMetadataService} from './route-metadata.service';

/**
 * These assertions ARE the gate for `bodyClass` and `document.title` at the unit level — the router
 * itself never sees either, and the only other gate is a Playwright measurement of the composition
 * layout. The chain under test replaces app.ts:711-739.
 */
describe('RouteMetadataService', () => {
    let service: RouteMetadataService;
    let events: Subject<any>;
    let router: any;
    let document: any;

    /** A snapshot chain, root first, each level's `data` as given. */
    function snapshot(...levels: any[]): any {
        const nodes = levels.map((data) => ({data, firstChild: null as any}));
        nodes.forEach((node, i) => node.firstChild = nodes[i + 1] || null);
        return nodes[0];
    }

    function navigateTo(...levels: any[]): void {
        router.routerState = {snapshot: {root: snapshot.apply(null, levels)}};
        events.next(new NavigationEnd(1, '/whatever', '/whatever'));
    }

    beforeEach(() => {
        events = new Subject<any>();
        router = {events, routerState: {snapshot: {root: snapshot({})}}};
        document = window.document.implementation.createHTMLDocument('test');
        service = new RouteMetadataService(router, document);
        service.start();
    });

    afterEach(() => service.stop());

    it('applies the default class and title for a route that declares neither', () => {
        navigateTo({});
        expect(document.body.className).toBe('default-class');
        expect(document.title).toBe('SDC');
    });

    it('applies a top-level route title', () => {
        navigateTo({title: 'SDC - Dashboard'});
        expect(document.title).toBe('SDC - Dashboard');
    });

    /**
     * The 11 routes that declare `bodyClass` are all workspace CHILDREN while the WCAG title is on
     * the workspace SHELL, so a single navigation has to read the two from different depths.
     */
    it('takes bodyClass from the deepest route and title from the shell', () => {
        navigateTo({}, {title: 'SDC - Workspace'}, {bodyClass: 'composition'});
        expect(document.body.className).toBe('composition');
        expect(document.title).toBe('SDC - Workspace');
    });

    it('falls back to the default class when only a parent declares a title', () => {
        navigateTo({}, {title: 'SDC - Workspace'}, {});
        expect(document.body.className).toBe('default-class');
    });

    it('swaps the class on a tab switch instead of accumulating', () => {
        navigateTo({}, {title: 'SDC - Workspace'}, {bodyClass: 'general'});
        navigateTo({}, {title: 'SDC - Workspace'}, {bodyClass: 'composition'});
        expect(document.body.className).toBe('composition');
    });

    /**
     * `modal-open` is added by ModalComponent.open() (modal.component.ts:71) and is what stops the
     * page behind a modal scrolling. `data-ng-class` only ever managed its own classes, so a state
     * change never touched it — assigning `className` would.
     */
    it('leaves a foreign body class alone', () => {
        navigateTo({}, {}, {bodyClass: 'general'});
        document.body.classList.add('modal-open');
        navigateTo({}, {}, {bodyClass: 'composition'});
        expect(document.body.classList.contains('modal-open')).toBe(true);
        expect(document.body.classList.contains('composition')).toBe(true);
        expect(document.body.classList.contains('general')).toBe(false);
    });

    it('ignores router events other than NavigationEnd', () => {
        router.routerState = {snapshot: {root: snapshot({bodyClass: 'composition'})}};
        events.next(new NavigationStart(1, '/whatever'));
        expect(document.body.className).toBe('');
    });

    it('stops applying once stopped', () => {
        navigateTo({bodyClass: 'general'});
        service.stop();
        navigateTo({bodyClass: 'composition'});
        expect(document.body.className).toBe('general');
    });

    it('does not subscribe twice when started twice', () => {
        const subscribe = jest.spyOn(events, 'subscribe');
        service.start();
        expect(subscribe).not.toHaveBeenCalled();
    });
});
