/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2025-2026 Deutsche Telekom AG. All rights reserved.
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

import {Injectable} from '@angular/core';
import {ActivatedRoute, ActivatedRouteSnapshot, NavigationEnd, NavigationStart, Router} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';
import {WorkspaceService} from '../pages/workspace/workspace.service';

export interface NavigationOptions {
    location?: boolean | 'replace';
    notify?: boolean;
    reload?: boolean;
}

export interface NavigationStartEvent {
    toState: string;
    toParams: any;
    fromState: string;
    fromParams: any;
    preventDefault: () => void;
}

export type NavigationStartCallback = (event: NavigationStartEvent) => void;

/**
 * The ui-router state name -> URL template table, transcribed from the 36 `$stateProvider.state()`
 * calls app.ts used to declare (`app.ts:77-565`). Templates are relative to the workspace shell for
 * the dotted `workspace.*` entries and absolute for the rest.
 *
 * Three of the ui-router urls carried a TRAILING SLASH ('composition/', 'activity_log/',
 * 'deployment/') and these values deliberately drop it, matching the routes in `app.routes.ts` — see
 * the `workspaceChildren` comment there for why a 'composition/' route cannot match. Emitting the
 * slash here would break navigation the other way round: `navigateByUrl` parses its argument
 * directly, without `Location`'s normalisation, so the trailing empty segment would survive and miss
 * the slash-free route.
 */
const WORKSPACE_TAB_PATHS: {[state: string]: string} = {
    'general': 'general',
    'information_artifacts': 'information_artifacts',
    'tosca_artifacts': 'tosca_artifacts',
    'deployment_artifacts': 'deployment_artifacts',
    'properties': 'properties',
    'properties_assignment': 'properties_assignment',
    'attributes': 'attributes',
    'attributes_outputs': 'attributes_outputs',
    'reqAndCap': 'req_and_capabilities',
    'reqAndCapEditable': 'req_and_capabilities_editable',
    'management_workflow': 'management_workflow',
    'network_call_flow': 'network_call_flow',
    'composition': 'composition',
    'activity_log': 'activity_log',
    'distribution': 'distribution',
    'deployment': 'deployment',
    'interface_operation': 'interface_operation',
    'interface-definition': 'interfaceDefinition',
    'plugins': 'plugins/:path'
};

const TOP_LEVEL_PATHS: {[state: string]: string} = {
    'dashboard': '/dashboard',
    'catalog': '/catalog',
    'adminDashboard': '/adminDashboard',
    'onboardVendor': '/onboardVendor',
    'error-403': '/error-403',
    'plugins': '/plugins/:path',
    'type-workspace': '/:previousState/type-workspace/:type/:id/:subPage'
};

/**
 * `workspace.composition.*` were third-level ui-router states; the Angular route flattens them onto
 * `composition/:panelTab`. Listed explicitly rather than derived, because the mapping has to work in
 * BOTH directions and `getCurrentStateName()` must not invent a state name for an arbitrary segment.
 */
const COMPOSITION_PANEL_TABS: string[] =
    ['details', 'properties', 'artifacts', 'relations', 'structure', 'lifecycle', 'api', 'deployment'];

/**
 * Params that had no URL representation under ui-router either — a `File`, a parsed CSAR, an
 * `isViewOnly` boolean handed straight to the next controller. ui-router kept them in
 * `$stateParams` because it never round-tripped params through the URL. The Angular router only has
 * `NavigationExtras.state` for this and that landed in 7.2, so they go into a transient store that
 * `getParam()` reads and the next navigation clears.
 *
 * `components` (a `Component[]`, passed by workspace-container.component.ts:390/:585) and `state`
 * (a state name, attached to every left-bar menu item at :670) belong here for the same reason:
 * ui-router carried the first as a `resolve` and the second only in `$stateParams`, so neither ever
 * reached the URL. Serialised they become '?components=%5Bobject%20Object%5D&state=workspace.general'
 * — `encodeURIComponent` stringifies the array to '[object Object]'. Nothing reads either from the
 * URL (the breadcrumb list comes from `cacheService.get('breadcrumbsComponents')`).
 */
const TRANSIENT_PARAMS: string[] = ['componentCsar', 'importedFile', 'resourceType', 'queryParams', 'components', 'state'];

@Injectable()
export class NavigationService {

    private transientParams: any = {};

    constructor(private router: Router,
                private route: ActivatedRoute,
                private workspaceService: WorkspaceService) {
    }

    /**
     * `navigateByUrl` with a STRING, never `navigate()` with a command array: `createUrlTree`
     * percent-encodes a '/' inside a command, so the three trailing-slash tabs serialise to
     * '…/composition%2F' and stop matching. Measured on router 5.2.11.
     */
    public navigate(state: string, params?: any, options?: NavigationOptions): Promise<boolean> {
        const effectiveParams = this.withPreviousStateDefault(state, this.inheritPathParams(state, params));
        this.transientParams = this.extractTransient(effectiveParams);
        const url = this.stateToPath(state, effectiveParams);
        const extras: any = {};
        if (options && options.location === 'replace') {
            extras.replaceUrl = true;
        }
        return this.router.navigateByUrl(url, extras);
    }

    /**
     * The ui-router state name for the current URL. 12 call sites compare the result against the
     * DOTTED names in `States` — `=== States.WORKSPACE_GENERAL`,
     * `.indexOf(States.WORKSPACE_COMPOSITION) > -1`, `.split('.', 2)` — so the URL is mapped back to
     * a state name rather than exposed raw. Returns '' for a URL with no state (e.g. '/').
     */
    public getCurrentStateName(): string {
        const segments = this.pathSegments();
        if (!segments.length) { return ''; }

        const workspaceIdx = segments.indexOf('workspace');
        if (workspaceIdx > 0) {
            return 'workspace.' + this.tabStateName(segments.slice(workspaceIdx + 1));
        }
        if (segments.indexOf('type-workspace') > 0) { return 'type-workspace'; }

        const first = segments[0];
        return TOP_LEVEL_PATHS[first] ? first : '';
    }

    public getUnsavedChanges(): boolean {
        return !!this.workspaceService.unsavedChanges;
    }

    public setUnsavedChanges(value: boolean): void {
        this.workspaceService.unsavedChanges = value;
    }

    /**
     * ui-router's `{reload: true}` has no `NavigationExtras` equivalent. Re-navigating the same URL
     * re-runs the resolvers because `AppRoutingModule` sets `onSameUrlNavigation: 'reload'`; when
     * `params` change the URL, the ordinary navigation does it. Rebuilding from the current state
     * name (rather than replaying `router.url`) is what lets the two callers at
     * workspace-container.component.ts:544/:546 swap in a freshly created component's id.
     */
    public reload(params?: any): Promise<boolean> {
        const url = this.router.url;
        const merged: any = this.paramsOf(url);
        if (params) {
            Object.keys(params).forEach((k) => merged[k] = params[k]);
        }
        return this.navigate(this.stateNameOf(url), merged);
    }

    public onNavigationStart(cb: NavigationStartCallback): () => void {
        return this.subscribeTo(NavigationStart, cb);
    }

    public onNavigationSuccess(cb: (event: NavigationStartEvent) => void): () => void {
        return this.subscribeTo(NavigationEnd, cb);
    }

    /**
     * Path params from the whole `ActivatedRoute` chain merged with the root's query params, so a
     * workspace tab sees the shell's `:id`/`:type` (which `paramsInheritanceStrategy: 'always'`
     * copies down) alongside `?filter.term=…`. Path params win on collision, and both win over the
     * transient store so a stale object cannot shadow a real route param.
     */
    public getParams(): any {
        const merged: any = {};
        Object.keys(this.transientParams).forEach((k) => merged[k] = this.transientParams[k]);

        let cursor: ActivatedRouteSnapshot = this.route.snapshot;
        const pathParams: any = {};
        while (cursor) {
            Object.keys(cursor.queryParams || {}).forEach((k) => merged[k] = cursor.queryParams[k]);
            Object.keys(cursor.params || {}).forEach((k) => pathParams[k] = cursor.params[k]);
            cursor = cursor.firstChild;
        }
        Object.keys(pathParams).forEach((k) => merged[k] = pathParams[k]);
        return merged;
    }

    public getParam(key: string): any {
        return this.getParams()[key];
    }

    /**
     * ui-router's `$state.includes()` tested state-name ancestry; here it is a path-SEGMENT test, so
     * 'workspace' cannot match a '/…/type-workspace/…' url — top-nav.component.ts:92 asks for the
     * two separately and picks a different breadcrumb for each.
     */
    public includes(stateName: string): boolean {
        return this.pathSegments().indexOf(stateName) !== -1;
    }

    private stateToPath(state: string, params: any): string {
        const p = params || {};

        if (state === '.') {
            return this.pathOnly() + this.queryString(p, []);
        }

        if (state.indexOf('workspace.') === 0) {
            return this.workspacePath(state.substring('workspace.'.length), p);
        }

        const template = TOP_LEVEL_PATHS[state];
        if (!template) {
            // A state name no longer in the table: navigate literally so the '**' route reports it
            // rather than silently doing nothing.
            return '/' + state;
        }
        if (state === 'type-workspace') {
            // ui-router's url had all four params required; `subPage` is the only one callers omit
            // (2 of 3 call sites), and without a value the 5-segment route cannot match.
            const withSubPage: any = {};
            Object.keys(p).forEach((k) => withSubPage[k] = p[k]);
            if (!withSubPage.subPage) { withSubPage.subPage = 'general'; }
            return this.fillTemplate(template, withSubPage);
        }
        return this.fillTemplate(template, p);
    }

    /**
     * `<previousState>/workspace[/<id>]/<type>/<tab>`. The id-less CREATE form emits a SINGLE slash:
     * ui-router produced a double one ('/dashboard/workspace//service/general') but
     * `DefaultUrlSerializer` discards an empty segment AND everything after it, so that URL is
     * unrepresentable and `app.routes.ts` declares a dedicated id-less route for the 4-segment form.
     */
    private workspacePath(tabState: string, params: any): string {
        const consumed = ['previousState', 'id', 'type'];
        const prefix = '/' + params.previousState + '/workspace' +
            (params.id ? '/' + params.id : '') + '/' + params.type;

        const dotIdx = tabState.indexOf('.');
        if (dotIdx !== -1 && tabState.substring(0, dotIdx) === 'composition') {
            return prefix + '/composition/' + tabState.substring(dotIdx + 1) + this.queryString(params, consumed);
        }

        const tabPath = WORKSPACE_TAB_PATHS[tabState] || tabState;
        const consumedByTab: string[] = [];
        const tabSegments = this.substituteParams(tabPath, params, consumedByTab);
        return prefix + '/' + tabSegments + this.queryString(params, consumed.concat(consumedByTab));
    }

    /** The inverse of `workspacePath`'s tab half — segments AFTER `<previousState>/workspace`. */
    private tabStateName(afterWorkspace: string[]): string {
        // Drop the id (when present) and the type. The id-less create URL has one fewer segment, and
        // the two are told apart by the count: [id, type, tab…] vs [type, tab…].
        const tail = afterWorkspace.slice(afterWorkspace.length > 2 ? 2 : 1);
        if (!tail.length) { return ''; }

        if (tail[0] === 'plugins') { return 'plugins'; }
        if (tail[0] === 'composition') {
            // A bookmarked '…/composition/' still yields a trailing '' segment here, which is not a
            // panel tab; 'composition/details' yields a real one.
            return (tail[1] && COMPOSITION_PANEL_TABS.indexOf(tail[1]) !== -1)
                ? 'composition.' + tail[1] : 'composition';
        }
        const match = Object.keys(WORKSPACE_TAB_PATHS)
            .filter((state) => WORKSPACE_TAB_PATHS[state] === tail[0])[0];
        return match || tail[0];
    }

    private fillTemplate(template: string, params: any): string {
        const consumed: string[] = [];
        return this.substituteParams(template, params, consumed) + this.queryString(params, consumed);
    }

    /** Substitutes ':name' segments, recording each name it consumed so it stays out of the query. */
    private substituteParams(template: string, params: any, consumed: string[]): string {
        return template.split('/').map((segment) => {
            if (segment.charAt(0) !== ':') { return segment; }
            const key = segment.substring(1);
            consumed.push(key);
            return params[key];
        }).join('/');
    }

    /**
     * Every param the path did not consume becomes a query param, which is how ui-router's declared
     * `?show&folder&filter.term…` query params behaved. `null`/`undefined` are dropped rather than
     * serialised — `changeFilterParams` (catalog.component.ts:619-640) sets a cleared filter to
     * `null` and expects it to disappear from the URL.
     */
    private queryString(params: any, consumed: string[]): string {
        const pairs = Object.keys(params)
            .filter((k) => consumed.indexOf(k) === -1)
            .filter((k) => TRANSIENT_PARAMS.indexOf(k) === -1)
            .filter((k) => params[k] !== null && params[k] !== undefined)
            .map((k) => encodeURIComponent(k) + '=' + encodeURIComponent(params[k]));
        return pairs.length ? '?' + pairs.join('&') : '';
    }

    private extractTransient(params: any): any {
        const store: any = {};
        if (!params) { return store; }
        TRANSIENT_PARAMS.filter((k) => params[k] !== undefined && params[k] !== null)
            .forEach((k) => store[k] = params[k]);
        return store;
    }

    /**
     * `$state.go` defaulted to `{inherit: true}` (angular-ui-router.js:3176), so a transition within
     * one asset only had to name the params it CHANGED and ui-router copied the rest off the current
     * state. Five call sites rely on that and pass no `id`/`type` at all — interface-definition:651
     * and interface-operation:398 pass only `{path}`, workspace-container:401 passes nothing, and
     * :616 forwards a menu item's `{state}`. Without this they would build '/…/workspace/undefined/…'.
     *
     * Only the target route's own PATH params are inherited, deliberately narrower than ui-router,
     * which inherited every param of the common ancestors — for the flat catalog/dashboard states
     * that would drag stale `filter.*` query params into an unrelated URL.
     */
    private inheritPathParams(state: string, params: any): any {
        const inheritable = this.pathParamsOf(state);
        if (!inheritable.length) { return params; }

        // Read the CURRENT URL rather than the ActivatedRoute snapshot: the snapshot lags a
        // navigation that is still settling, and a stale :id there would be silently baked into the
        // next URL. The URL is authoritative and always current.
        const merged: any = {};
        const current = this.paramsOf(this.router.url);
        inheritable.forEach((key) => {
            if (current[key] !== undefined) { merged[key] = current[key]; }
        });
        Object.keys(params || {}).forEach((k) => merged[k] = params[k]);
        return merged;
    }

    /** The path-param names the target state's URL template consumes. */
    private pathParamsOf(state: string): string[] {
        if (state === 'type-workspace') { return ['previousState', 'type', 'id', 'subPage']; }
        if (state.indexOf('workspace.') !== 0) { return []; }
        const tab = state.substring('workspace.'.length);
        const base = ['previousState', 'id', 'type'];
        return tab === 'plugins' ? base.concat(['path']) : base;
    }

    /**
     * ui-router filled `previousState` in globally for any state whose name contained 'workspace'
     * (`app.ts:700-707`), so 13 of the 15 workspace `navigate()` call sites still omit it. Deriving
     * it here keeps them working; the alternative would be alias routes for the missing segment.
     */
    private withPreviousStateDefault(state: string, params: any): any {
        if (state.indexOf('workspace') === -1 || (params && params.previousState)) { return params; }

        const segments = this.pathSegments();
        const inWorkspace = segments.indexOf('workspace') > 0 || segments.indexOf('type-workspace') > 0;
        const candidate = inWorkspace ? segments[0] : segments[segments.length - 1];
        const previousState = (candidate === 'catalog') ? 'catalog' : 'dashboard';

        const merged: any = {previousState};
        Object.keys(params || {}).forEach((k) => merged[k] = params[k]);
        merged.previousState = previousState;
        return merged;
    }

    private subscribeTo(eventType: any, cb: (event: NavigationStartEvent) => void): () => void {
        const fromUrl = this.router.url;
        const subscription: Subscription = this.router.events.subscribe((event: any) => {
            if (!(event instanceof eventType)) { return; }
            cb({
                toState: this.stateNameOf(event.url),
                toParams: this.paramsOf(event.url),
                fromState: this.stateNameOf(fromUrl),
                fromParams: this.paramsOf(fromUrl),
                // ui-router cancelled an in-flight transition from a $stateChangeStart listener. The
                // Angular router has no equivalent — only a guard can cancel — so this cannot work
                // and says so instead of failing silently. Every real consumer is a guard now.
                preventDefault: () => console.warn(
                    'NavigationService: preventDefault() cannot cancel an Angular Router navigation; ' +
                    'use a CanDeactivate guard instead.')
            });
        });
        return () => subscription.unsubscribe();
    }

    private pathSegments(url?: string): string[] {
        const path = (url === undefined ? this.router.url : url) || '';
        return path.split('?')[0].split('/').filter((s) => !!s);
    }

    private pathOnly(): string {
        return (this.router.url || '').split('?')[0];
    }

    /**
     * `getCurrentStateName()` / `getParams()` for an ARBITRARY url rather than the live one, so a
     * navigation event can report on the URL it carries. The event fires before `ActivatedRoute` has
     * caught up, so the params are parsed out of the URL instead of read off the snapshot.
     */
    private stateNameOf(url: string): string {
        const segments = this.pathSegments(url);
        if (!segments.length) { return ''; }
        const workspaceIdx = segments.indexOf('workspace');
        if (workspaceIdx > 0) { return 'workspace.' + this.tabStateName(segments.slice(workspaceIdx + 1)); }
        if (segments.indexOf('type-workspace') > 0) { return 'type-workspace'; }
        return TOP_LEVEL_PATHS[segments[0]] ? segments[0] : '';
    }

    private paramsOf(url: string): any {
        const segments = this.pathSegments(url);
        const params: any = {};
        const workspaceIdx = segments.indexOf('workspace');
        const typeWorkspaceIdx = segments.indexOf('type-workspace');
        if (workspaceIdx > 0) {
            params.previousState = segments[0];
            const tail = segments.slice(workspaceIdx + 1);
            // [id, type, tab…] for a saved asset, [type, tab…] for the id-less create form; the
            // segment count is the only discriminator, exactly as in `tabStateName`.
            if (tail.length > 2) {
                params.id = tail[0];
                params.type = tail[1];
            } else {
                params.type = tail[0];
            }
            const tab = tail.slice(tail.length > 2 ? 2 : 1);
            if (tab[0] === 'plugins' && tab[1]) { params.path = tab[1]; }
            if (tab[0] === 'composition' && tab[1]) { params.panelTab = tab[1]; }
        } else if (typeWorkspaceIdx > 0) {
            params.previousState = segments[0];
            params.type = segments[typeWorkspaceIdx + 1];
            params.id = segments[typeWorkspaceIdx + 2];
            params.subPage = segments[typeWorkspaceIdx + 3];
        }
        const query = (url || '').split('?')[1];
        if (query) {
            query.split('&').forEach((pair) => {
                const [k, v] = pair.split('=');
                params[decodeURIComponent(k)] = v === undefined ? '' : decodeURIComponent(v);
            });
        }
        return params;
    }
}
