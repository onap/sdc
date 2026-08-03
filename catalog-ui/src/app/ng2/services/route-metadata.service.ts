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

import {DOCUMENT} from '@angular/common';
import {Inject, Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, NavigationEnd, Router} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';

const DEFAULT_BODY_CLASS = 'default-class';
const DEFAULT_TITLE = 'SDC';

/**
 * Replaces ui-router's `$stateChangeSuccess` handler (`app.ts:711-739`), which owned two things:
 * the `<body>` class (`$rootScope.bodyClass`, painted by `index.html`'s `<body data-ng-class>`) and
 * the WCAG 2.4.2 page title. Both die with the run block, and neither is visible to Jest, AOT or the
 * Selenium suite — `integration-tests/playwright-tests/tests/composition-geometry.spec.ts` is the
 * only gate that sees a lost body class, because `.composition` is the one value with live CSS
 * (`workspace.less:198`, `:276`).
 *
 * The CSAR-cache cleanup that shared that handler (`app.ts:714-723`) is NOT ported: the only reads
 * of `changeComponentCsarVersion` / `previousCsarComponent` anywhere in the tree are inside that
 * block itself, so it was clearing keys nobody consumed.
 */
@Injectable()
export class RouteMetadataService {

    private applied: string = null;
    private subscription: Subscription = null;

    constructor(private router: Router, @Inject(DOCUMENT) private document: any) {
    }

    public start(): void {
        if (this.subscription) { return; }
        this.subscription = this.router.events.subscribe((event: any) => {
            if (event instanceof NavigationEnd) {
                this.apply(this.router.routerState.snapshot.root);
            }
        });
    }

    public stop(): void {
        if (this.subscription) {
            this.subscription.unsubscribe();
            this.subscription = null;
        }
    }

    private apply(root: ActivatedRouteSnapshot): void {
        this.setBodyClass(this.deepestData(root, 'bodyClass') || DEFAULT_BODY_CLASS);
        this.document.title = this.deepestData(root, 'title') || DEFAULT_TITLE;
    }

    /**
     * Swaps the previously-applied class instead of assigning `className`. `data-ng-class` only ever
     * owned the classes it had added, so `modal-open` (modal.component.ts:71, which is what makes the
     * page behind a modal stop scrolling) and AngularJS's own `ng-scope` survived a state change —
     * assigning `className` would drop them.
     */
    private setBodyClass(bodyClass: string): void {
        const body = this.document.body;
        if (!body || this.applied === bodyClass) { return; }
        if (this.applied) {
            body.classList.remove(this.applied);
        }
        body.classList.add(bodyClass);
        this.applied = bodyClass;
    }

    /**
     * The DEEPEST route that declares the key wins, so a tab's own `bodyClass` overrides the
     * workspace shell's `title`. Walked explicitly rather than read off the deepest snapshot's
     * already-merged `data`: that merge only happens because `RouterModule.forRoot` sets
     * `paramsInheritanceStrategy: 'always'`, and this must not silently depend on it.
     */
    private deepestData(root: ActivatedRouteSnapshot, key: string): any {
        let value: any;
        let cursor: ActivatedRouteSnapshot = root;
        while (cursor) {
            if (cursor.data && cursor.data[key] !== undefined) {
                value = cursor.data[key];
            }
            cursor = cursor.firstChild;
        }
        return value;
    }
}
