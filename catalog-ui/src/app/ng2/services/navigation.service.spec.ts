/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2025 Deutsche Telekom AG. All rights reserved.
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
import {NavigationService} from './navigation.service';

/**
 * A route snapshot chain, root-first, as `ActivatedRoute.snapshot` + `.firstChild` expose it.
 * `getParams()` walks this and merges, because `paramsInheritanceStrategy: 'always'` puts the
 * shell's :id/:type on every node but query params only on the root.
 */
function routeTree(nodes: Array<{params?: any, queryParams?: any}>): any {
    const snapshots = nodes.map((n) => ({
        params: n.params || {},
        queryParams: n.queryParams || {},
        firstChild: null
    }));
    snapshots.forEach((s, i) => s.firstChild = snapshots[i + 1] || null);
    return {snapshot: snapshots[0]};
}

describe('NavigationService', () => {
    let service: NavigationService;
    let router: any;
    let route: any;
    let events: Subject<any>;
    let workspaceService: any;

    beforeEach(() => {
        events = new Subject<any>();
        router = {
            url: '/dashboard',
            events,
            navigateByUrl: jest.fn().mockReturnValue(Promise.resolve(true))
        };
        route = routeTree([{params: {}, queryParams: {}}]);
        workspaceService = {unsavedChanges: false};
        service = new NavigationService(router, route, workspaceService);
    });

    /** The URL the service actually asked the router for. */
    function navigatedTo(): string {
        expect(router.navigateByUrl).toHaveBeenCalled();
        return router.navigateByUrl.mock.calls[router.navigateByUrl.mock.calls.length - 1][0];
    }

    describe('navigate — top-level states', () => {
        it('maps the flat states to their absolute paths', () => {
            service.navigate('dashboard');
            expect(navigatedTo()).toBe('/dashboard');
            service.navigate('catalog');
            expect(navigatedTo()).toBe('/catalog');
            service.navigate('adminDashboard');
            expect(navigatedTo()).toBe('/adminDashboard');
            service.navigate('onboardVendor');
            expect(navigatedTo()).toBe('/onboardVendor');
            service.navigate('error-403');
            expect(navigatedTo()).toBe('/error-403');
        });

        it('appends unconsumed params as query params', () => {
            service.navigate('dashboard', {show: 'recent', folder: 'DESIGNER'});
            expect(navigatedTo()).toBe('/dashboard?show=recent&folder=DESIGNER');
        });

        it('drops null and undefined params instead of serialising them', () => {
            service.navigate('catalog', {'filter.term': 'vfw', 'filter.categories': null, 'filter.order': undefined});
            expect(navigatedTo()).toBe('/catalog?filter.term=vfw');
        });

        it('url-encodes param values', () => {
            service.navigate('catalog', {'filter.term': 'a b&c'});
            expect(navigatedTo()).toBe('/catalog?filter.term=a%20b%26c');
        });

        /**
         * The BARE 'plugins' state (app.ts:535, url '/plugins/*path') is the top-nav tab, distinct
         * from the dotted 'workspace.plugins' child (app.ts:505, url 'plugins/*path'). Both take a
         * :path, and top-nav.component.ts:75 compares the current state name against bare 'plugins'.
         */
        it('distinguishes the top-level plugins tab from the workspace plugins child', () => {
            service.navigate('plugins', {path: 'workflowDesigner'});
            expect(navigatedTo()).toBe('/plugins/workflowDesigner');

            router.url = '/catalog/workspace/abc/service/general';
            service.navigate('workspace.plugins', {path: 'workflowDesigner'});
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/plugins/workflowDesigner');
        });
    });

    describe('navigate — workspace tabs', () => {
        it('maps a dotted workspace state to an absolute path', () => {
            service.navigate('workspace.general', {id: 'abc', type: 'service', previousState: 'catalog'});
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/general');
        });

        /**
         * D2: ui-router produced a DOUBLE slash here ('/dashboard/workspace//service/general');
         * DefaultUrlSerializer discards an empty segment and everything after it, so the id-less
         * CREATE url is a single slash and matches the dedicated ':previousState/workspace/:type'
         * route. Pinned in app.routes.spec.ts from the routing side.
         */
        it('emits a SINGLE slash for the id-less create url', () => {
            service.navigate('workspace.general', {type: 'service'});
            expect(navigatedTo()).toBe('/dashboard/workspace/service/general');
        });

        /**
         * These three carried a trailing slash under ui-router and must NOT emit one now: the routes
         * dropped it (see app.routes.ts), and `navigateByUrl` parses this string verbatim — the
         * trailing empty segment would survive and miss the route. Related: navigate() has to use
         * `navigateByUrl` rather than `createUrlTree` because a slash inside a command array is
         * percent-encoded ('composition%2F') and stops matching either way.
         */
        it('emits no trailing slash on composition, activity_log and deployment', () => {
            const params = {id: 'abc', type: 'service', previousState: 'catalog'};
            service.navigate('workspace.composition', params);
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/composition');
            service.navigate('workspace.activity_log', params);
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/activity_log');
            service.navigate('workspace.deployment', params);
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/deployment');
        });

        /**
         * The composition panel tabs were third-level ui-router states; the Angular route flattens
         * them to 'composition/:panelTab'. menu.js:260 navigates to 'workspace.composition.details'
         * by name, so the dotted third level has to keep resolving.
         */
        it('flattens the composition panel tabs onto :panelTab', () => {
            const params = {id: 'abc', type: 'service', previousState: 'catalog'};
            service.navigate('workspace.composition.details', params);
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/composition/details');
            service.navigate('workspace.composition.api', params);
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/composition/api');
        });

        it('maps the two reqAndCap aliases and the hyphenated interface state', () => {
            const params = {id: 'abc', type: 'service', previousState: 'catalog'};
            service.navigate('workspace.reqAndCap', params);
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/req_and_capabilities');
            service.navigate('workspace.reqAndCapEditable', params);
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/req_and_capabilities_editable');
            // menu.js:231 spells this one with a hyphen while every other tab uses an underscore.
            service.navigate('workspace.interface-definition', params);
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/interfaceDefinition');
        });

        it('carries extra params through as query params alongside the path params', () => {
            service.navigate('workspace.general', {id: 'abc', type: 'service', previousState: 'catalog', isViewOnly: true});
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/general?isViewOnly=true');
        });
    });

    describe('previousState defaulting', () => {
        /**
         * ui-router filled `previousState` in globally for any state whose name contained
         * 'workspace' (app.ts:700-707) — 13 of the 15 workspace navigate() call sites still omit it,
         * so the default has to live here or every one of them produces '/undefined/workspace/…'.
         */
        it('defaults previousState to dashboard', () => {
            service.navigate('workspace.general', {id: 'abc', type: 'service'});
            expect(navigatedTo()).toBe('/dashboard/workspace/abc/service/general');
        });

        it('derives previousState from the current catalog url', () => {
            router.url = '/catalog';
            service.navigate('workspace.general', {id: 'abc', type: 'service'});
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/general');
        });

        it('carries the previousState of the workspace it is already in', () => {
            router.url = '/catalog/workspace/abc/service/general';
            service.navigate('workspace.properties', {id: 'abc', type: 'service'});
            expect(navigatedTo()).toBe('/catalog/workspace/abc/service/properties');
        });

        it('falls back to dashboard from an unrelated url', () => {
            router.url = '/adminDashboard';
            service.navigate('workspace.general', {id: 'abc', type: 'service'});
            expect(navigatedTo()).toBe('/dashboard/workspace/abc/service/general');
        });

        it('never overrides an explicit previousState', () => {
            router.url = '/catalog';
            service.navigate('workspace.general', {id: 'abc', type: 'service', previousState: 'dashboard'});
            expect(navigatedTo()).toBe('/dashboard/workspace/abc/service/general');
        });
    });

    describe('navigate — type-workspace', () => {
        it('defaults the subPage segment so the 5-segment route still matches', () => {
            service.navigate('type-workspace', {type: 'datatype', id: 'abc'});
            expect(navigatedTo()).toBe('/dashboard/type-workspace/datatype/abc/general');
        });

        it('honours an explicit subPage and previousState', () => {
            service.navigate('type-workspace', {type: 'datatype', id: 'abc', previousState: 'catalog', subPage: 'properties'});
            expect(navigatedTo()).toBe('/catalog/type-workspace/datatype/abc/properties');
        });
    });

    describe('navigate — the "." current-state form', () => {
        /**
         * home.component.ts:234 and catalog.component.ts:645 use `navigate('.', filterParams,
         * {location: 'replace', notify: false})` to write the filter state into the URL without
         * leaving the page. Only the query string may change.
         */
        it('replaces the query string of the current url and keeps the path', () => {
            router.url = '/catalog?filter.term=old';
            service.navigate('.', {'filter.term': 'new', 'filter.active': true});
            expect(navigatedTo()).toBe('/catalog?filter.term=new&filter.active=true');
        });

        it('clears the query string when every param is null', () => {
            router.url = '/catalog?filter.term=old';
            service.navigate('.', {'filter.term': null});
            expect(navigatedTo()).toBe('/catalog');
        });

        it('maps location:replace onto replaceUrl', () => {
            service.navigate('.', {'filter.term': 'x'}, {location: 'replace', notify: false});
            const extras = router.navigateByUrl.mock.calls[0][1];
            expect(extras).toEqual(expect.objectContaining({replaceUrl: true}));
        });

        it('returns the router promise so callers can chain .then()', () => {
            const result = service.navigate('dashboard');
            expect(typeof result.then).toBe('function');
            return result.then((ok) => expect(ok).toBe(true));
        });
    });

    describe('getCurrentStateName', () => {
        /**
         * Callers compare against DOTTED ui-router names — `=== States.WORKSPACE_GENERAL`,
         * `.indexOf(States.WORKSPACE_COMPOSITION)`, `.split('.', 2)` — so the URL has to be mapped
         * back to a state name rather than exposed raw. 12 call sites depend on this.
         */
        it('maps a workspace url back to its dotted state name', () => {
            router.url = '/catalog/workspace/abc/service/general';
            expect(service.getCurrentStateName()).toBe('workspace.general');
            router.url = '/catalog/workspace/abc/service/properties_assignment';
            expect(service.getCurrentStateName()).toBe('workspace.properties_assignment');
        });

        it('maps composition, activity_log and deployment back to their state names', () => {
            router.url = '/catalog/workspace/abc/service/composition';
            expect(service.getCurrentStateName()).toBe('workspace.composition');
            router.url = '/catalog/workspace/abc/service/activity_log';
            expect(service.getCurrentStateName()).toBe('workspace.activity_log');
            router.url = '/catalog/workspace/abc/service/deployment';
            expect(service.getCurrentStateName()).toBe('workspace.deployment');
        });

        // The routes no longer carry the ui-router trailing slash, but an old bookmark still can, and
        // `router.url` re-serialises it as a trailing empty segment. That must not be read as a panel
        // tab or the 12 dotted-name callers would see 'workspace.composition.' and stop matching.
        it('still maps the legacy trailing-slash urls back to the same state names', () => {
            router.url = '/catalog/workspace/abc/service/composition/';
            expect(service.getCurrentStateName()).toBe('workspace.composition');
            router.url = '/catalog/workspace/abc/service/deployment/';
            expect(service.getCurrentStateName()).toBe('workspace.deployment');
        });

        /**
         * workspace-container.component.ts:155 tests
         * `getCurrentStateName().indexOf(States.WORKSPACE_COMPOSITION) > -1`, so a panel tab must
         * report a name that still CONTAINS 'workspace.composition'.
         */
        it('reports the composition panel tabs as third-level state names', () => {
            router.url = '/catalog/workspace/abc/service/composition/details';
            expect(service.getCurrentStateName()).toBe('workspace.composition.details');
            expect(service.getCurrentStateName().indexOf('workspace.composition')).toBeGreaterThan(-1);
        });

        it('maps the id-less create url back to workspace.general', () => {
            router.url = '/dashboard/workspace/service/general';
            expect(service.getCurrentStateName()).toBe('workspace.general');
        });

        it('maps the flat urls back to their state names', () => {
            router.url = '/dashboard';
            expect(service.getCurrentStateName()).toBe('dashboard');
            router.url = '/catalog?filter.term=x';
            expect(service.getCurrentStateName()).toBe('catalog');
            router.url = '/plugins/workflowDesigner';
            expect(service.getCurrentStateName()).toBe('plugins');
            router.url = '/catalog/type-workspace/datatype/abc/general';
            expect(service.getCurrentStateName()).toBe('type-workspace');
        });

        it('returns the empty string for an unrecognised url', () => {
            router.url = '/';
            expect(service.getCurrentStateName()).toBe('');
        });
    });

    describe('getParams / getParam', () => {
        it('merges path params down the route tree with the root query params', () => {
            route = routeTree([
                {queryParams: {'filter.term': 'vfw'}},
                {params: {previousState: 'catalog', id: 'abc', type: 'service'}},
                {params: {panelTab: 'details'}}
            ]);
            service = new NavigationService(router, route, workspaceService);
            expect(service.getParams()).toEqual({
                'filter.term': 'vfw', previousState: 'catalog', id: 'abc', type: 'service', panelTab: 'details'
            });
            expect(service.getParam('id')).toBe('abc');
            expect(service.getParam('filter.term')).toBe('vfw');
            expect(service.getParam('nope')).toBeUndefined();
        });

        it('lets a path param win over a query param of the same name', () => {
            route = routeTree([{queryParams: {id: 'from-query'}}, {params: {id: 'from-path'}}]);
            service = new NavigationService(router, route, workspaceService);
            expect(service.getParam('id')).toBe('from-path');
        });

        /**
         * ui-router carried arbitrary objects in `$stateParams` — a File, a parsed CSAR — which have
         * no URL representation. NavigationExtras.state does not exist before Angular 7.2, so these
         * are held in a transient store that survives exactly one navigation, and getParam() has to
         * read it as if it were a route param. WorkspaceComponentResolver:52/:75-76 and
         * type-workspace-general.component.ts:141 depend on this.
         */
        it('surfaces non-serialisable params from the transient store', () => {
            const file = {name: 'x.csar'};
            service.navigate('workspace.general', {type: 'service', componentCsar: file, resourceType: 'VF'});
            expect(service.getParam('componentCsar')).toBe(file);
            expect(service.getParam('resourceType')).toBe('VF');
            // and they must NOT have leaked into the URL
            expect(navigatedTo()).toBe('/dashboard/workspace/service/general');
        });

        it('lets a real route param shadow a stale transient value', () => {
            service.navigate('workspace.general', {type: 'service', id: 'transient'});
            route = routeTree([{}, {params: {id: 'real'}}]);
            service = new NavigationService(router, route, workspaceService);
            expect(service.getParam('id')).toBe('real');
        });

        it('clears the transient store on the next navigation', () => {
            service.navigate('workspace.general', {type: 'service', componentCsar: {name: 'x.csar'}});
            expect(service.getParam('componentCsar')).toBeDefined();
            service.navigate('dashboard');
            expect(service.getParam('componentCsar')).toBeUndefined();
        });
    });

    describe('includes', () => {
        it('matches a url segment', () => {
            router.url = '/dashboard/workspace/abc/service/general';
            expect(service.includes('workspace')).toBe(true);
            expect(service.includes('catalog')).toBe(false);
        });

        /**
         * top-nav.component.ts:92 asks for 'workspace' and 'type-workspace' separately and picks a
         * different breadcrumb for each, so 'workspace' must not swallow a type-workspace url.
         */
        it('does not report a type-workspace url as a workspace url', () => {
            router.url = '/catalog/type-workspace/datatype/abc/general';
            expect(service.includes('type-workspace')).toBe(true);
            expect(service.includes('workspace')).toBe(false);
        });

        it('ignores the query string', () => {
            router.url = '/catalog?filter.term=workspace';
            expect(service.includes('workspace')).toBe(false);
            expect(service.includes('catalog')).toBe(true);
        });
    });

    describe('unsavedChanges', () => {
        /**
         * The flag lived on `$state.current.data` under ui-router. The Angular router deep-freezes a
         * Route's `data` (Recognizer.inheritParamsAndData), so writing it there throws in strict mode
         * and is silently dropped otherwise — hence WorkspaceService owns it.
         */
        it('delegates to WorkspaceService', () => {
            expect(service.getUnsavedChanges()).toBe(false);
            service.setUnsavedChanges(true);
            expect(workspaceService.unsavedChanges).toBe(true);
            expect(service.getUnsavedChanges()).toBe(true);
            service.setUnsavedChanges(false);
            expect(service.getUnsavedChanges()).toBe(false);
        });
    });

    describe('reload', () => {
        it('re-navigates the current url', () => {
            router.url = '/dashboard/workspace/abc/service/general';
            service.reload();
            expect(navigatedTo()).toBe('/dashboard/workspace/abc/service/general');
        });

        /**
         * workspace-container.component.ts:544/:546 reload with a NEW id after a create or a CSAR
         * update, so reload(params) has to rebuild the url rather than repeat it verbatim.
         */
        it('rebuilds the url with overridden params', () => {
            router.url = '/dashboard/workspace/old-id/service/general';
            service.reload({id: 'new-id'});
            expect(navigatedTo()).toBe('/dashboard/workspace/new-id/service/general');
        });

        it('keeps a null-valued param out of the url', () => {
            router.url = '/dashboard/workspace/old-id/service/general';
            service.reload({id: 'new-id', componentCsar: null});
            expect(navigatedTo()).toBe('/dashboard/workspace/new-id/service/general');
        });
    });

    describe('navigation events', () => {
        it('adapts NavigationStart to the NavigationStartEvent shape', () => {
            const cb = jest.fn();
            const off = service.onNavigationStart(cb);
            router.url = '/dashboard';
            events.next(new NavigationStart(1, '/catalog'));
            expect(cb).toHaveBeenCalledWith(expect.objectContaining({
                toState: 'catalog', fromState: 'dashboard'
            }));
            expect(typeof off).toBe('function');
        });

        it('adapts NavigationEnd and reports the url params', () => {
            const cb = jest.fn();
            service.onNavigationSuccess(cb);
            events.next(new NavigationEnd(1, '/catalog/workspace/abc/service/general', '/catalog/workspace/abc/service/general'));
            expect(cb).toHaveBeenCalledWith(expect.objectContaining({
                toState: 'workspace.general',
                toParams: expect.objectContaining({previousState: 'catalog', id: 'abc', type: 'service'})
            }));
        });

        it('does not deliver NavigationEnd to a start listener, or the reverse', () => {
            const start = jest.fn();
            const success = jest.fn();
            service.onNavigationStart(start);
            service.onNavigationSuccess(success);
            events.next(new NavigationStart(1, '/catalog'));
            expect(start).toHaveBeenCalledTimes(1);
            expect(success).not.toHaveBeenCalled();
            events.next(new NavigationEnd(1, '/catalog', '/catalog'));
            expect(start).toHaveBeenCalledTimes(1);
            expect(success).toHaveBeenCalledTimes(1);
        });

        it('stops delivering after the returned thunk is called', () => {
            const cb = jest.fn();
            const off = service.onNavigationStart(cb);
            off();
            events.next(new NavigationStart(1, '/catalog'));
            expect(cb).not.toHaveBeenCalled();
        });

        /**
         * ui-router's `event.preventDefault()` cancelled an in-flight transition; the Angular router
         * has no equivalent — a navigation is cancelled by a guard, not by an event subscriber. It
         * stays on the event object so the existing callers compile, but it cannot work, so it warns
         * rather than failing silently. Every real consumer is converted to a guard.
         */
        it('warns instead of pretending preventDefault() can cancel a navigation', () => {
            const warn = jest.spyOn(console, 'warn').mockImplementation(() => undefined);
            const cb = jest.fn();
            service.onNavigationStart(cb);
            events.next(new NavigationStart(1, '/catalog'));
            cb.mock.calls[0][0].preventDefault();
            expect(warn).toHaveBeenCalled();
            warn.mockRestore();
        });
    });
});
