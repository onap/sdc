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

import {APP_BASE_HREF, Location} from '@angular/common';
import {Component} from '@angular/core';
import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {ActivatedRouteSnapshot, Router, RouterModule, RouterStateSnapshot, Routes} from '@angular/router';
import {routes} from './app.routes';

@Component({selector: 'app-routes-host', template: '<router-outlet></router-outlet>'})
class HostComponent {
}

@Component({selector: 'app-routes-stub', template: 'stub<router-outlet></router-outlet>'})
class StubComponent {
}

/**
 * Exercises the REAL `routes` array against the real router, with every `component` swapped for a
 * stub and every guard/resolver dropped. Matching, redirects and param inheritance are decided
 * purely by the path strings and the declaration order, so this keeps that surface under test
 * without dragging in ~30 page components and their DI.
 *
 * Route matching in this app is not a detail: the URLs are hard-coded in the Cypress, Selenium and
 * Playwright suites, and the differences from ui-router (a trailing slash is a real segment; a
 * double slash is unrepresentable; `redirectTo` must not carry a wildcard tail) are all invisible
 * until a URL silently lands on /dashboard.
 */
function stub(config: Routes): Routes {
    return config.map((route) => {
        const copy: any = {path: route.path};
        if (route.pathMatch) { copy.pathMatch = route.pathMatch; }
        if (route.redirectTo) { copy.redirectTo = route.redirectTo; }
        if (route.component) { copy.component = StubComponent; }
        if (route.children) { copy.children = stub(route.children); }
        return copy;
    });
}

describe('app.routes', () => {
    let router: Router;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [HostComponent, StubComponent],
            imports: [RouterModule.forRoot(stub(routes), {paramsInheritanceStrategy: 'always'})],
            providers: [{provide: APP_BASE_HREF, useValue: '/'}]
        });
        router = TestBed.get(Router);
        // A rendered outlet is mandatory, not cosmetic: PreActivation.setupRouteGuards dereferences
        // `context.outlet.component` for a reused node, so without one the second navigation in any
        // test throws "Cannot read properties of null (reading 'component')".
        TestBed.createComponent(HostComponent).detectChanges();
    });

    function go(url: string): string {
        let done = false;
        router.navigateByUrl(url).then(() => done = true);
        tick();
        expect(done).toBe(true);
        return router.url;
    }

    /**
     * `go()` above is the IN-APP path (`navigateByUrl` parses its argument verbatim). This is the
     * BROWSER path, and the two are not interchangeable: everything the router reads out of the
     * address bar — `initialNavigation()` for a deep link or reload, and the `hashchange`
     * subscription — arrives via `Location.path()`, which normalises through
     * `Location.stripTrailingSlash()`. Routing a trailing-slash URL only through `go()` is what let a
     * `path: 'composition/'` route pass this spec while every real navigation to it fell through to
     * '**' and landed on /dashboard.
     */
    function goFromBrowser(url: string): string {
        return go(Location.stripTrailingSlash(url));
    }

    it('sends the empty URL and any unknown URL to the dashboard', fakeAsync(() => {
        expect(go('/')).toBe('/dashboard');
        expect(go('/no/such/place')).toBe('/dashboard');
    }));

    it('matches composition, activity_log and deployment as the browser delivers them', fakeAsync(() => {
        expect(goFromBrowser('/catalog/workspace/abc/resource/composition/'))
            .toBe('/catalog/workspace/abc/resource/composition');
        expect(goFromBrowser('/catalog/workspace/abc/resource/activity_log/'))
            .toBe('/catalog/workspace/abc/resource/activity_log');
        expect(goFromBrowser('/catalog/workspace/abc/service/deployment/'))
            .toBe('/catalog/workspace/abc/service/deployment');
    }));

    // The form NavigationService emits and the form a Cypress spec deep-links to. Distinct configs
    // from bare 'composition' (see app.routes.ts), so this is not implied by the test above.
    it('matches a composition panel tab', fakeAsync(() => {
        expect(goFromBrowser('/catalog/workspace/abc/resource/composition/details'))
            .toBe('/catalog/workspace/abc/resource/composition/details');
    }));

    /**
     * The slash-less form is what a user typing the URL or an old bookmark produces. ui-router would
     * not have matched it either, but here the '' child redirect means it silently lands on the
     * General tab rather than on /dashboard.
     */
    it('redirects a tab-less workspace URL to the general tab', fakeAsync(() => {
        expect(go('/catalog/workspace/abc/resource')).toBe('/catalog/workspace/abc/resource/general');
    }));

    /**
     * CREATE mode. ui-router's URL had a DOUBLE slash where the id belongs
     * ('/dashboard/workspace//service/general'); DefaultUrlSerializer.parse() drops an empty segment
     * and everything after it, so that URL cannot be expressed and the single-slash form replaces
     * it. Pinned because NavigationService.gotoWorkspaceTab and the Playwright create-flow test both
     * have to agree on which form it is.
     */
    it('matches the id-less create URL on the single-slash form', fakeAsync(() => {
        expect(go('/dashboard/workspace/service/general')).toBe('/dashboard/workspace/service/general');
    }));

    /**
     * The 'workspace-old' state's replacement. The route path carries no wildcard tail even though
     * the old url did ('/workspace/:id/:type/*workspaceInnerPath') — measured: this form already
     * preserves the whole tail across the redirect, while adding '/**' makes every such URL fall
     * through to '**' and land on /dashboard. Both halves are pinned here.
     */
    it('rewrites a bare /workspace URL under /catalog, tail and all', fakeAsync(() => {
        expect(go('/workspace/abc/resource/general')).toBe('/catalog/workspace/abc/resource/general');
        expect(go('/workspace/abc/resource/composition/details'))
            .toBe('/catalog/workspace/abc/resource/composition/details');
    }));

    it('matches the type-workspace URL and tolerates an empty :subPage', fakeAsync(() => {
        expect(go('/catalog/type-workspace/datatype/abc/general')).toBe('/catalog/type-workspace/datatype/abc/general');
        expect(go('/catalog/type-workspace/datatype/abc/')).toBe('/catalog/type-workspace/datatype/abc/');
    }));

    /**
     * `paramsInheritanceStrategy: 'always'` is what makes the shell's :id/:type/:previousState
     * readable from the deepest child — NavigationService.getParams() and every workspace tab depend
     * on it. Without it a leaf under a component-less-free hierarchy sees only its own params.
     */
    it('inherits the shell params down to the deepest leaf', fakeAsync(() => {
        go('/dashboard/workspace/abc/resource/composition/details');
        let leaf: ActivatedRouteSnapshot = (router.routerState.snapshot as RouterStateSnapshot).root;
        while (leaf.firstChild) { leaf = leaf.firstChild; }
        expect(leaf.params).toEqual({previousState: 'dashboard', id: 'abc', type: 'resource', panelTab: 'details'});
    }));

    it('declares no duplicate top-level paths', () => {
        const paths = routes.map((r) => r.path);
        expect(paths.length).toBe(new Set(paths).size);
    });
});
