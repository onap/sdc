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
import {Inject, Injectable, NgZone} from '@angular/core';
import {ActivatedRouteSnapshot, NavigationEnd, NavigationStart, Router} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';

export const PAGE_HEADING_SELECTOR = '#main-content h1';
export const TAB_HEADING_SELECTOR = '[data-tab-heading]';
export const POLL_INTERVAL_MS = 100;
export const MAX_POLLS = 100;

/**
 * WCAG 2.4.3: after an in-app navigation, focus goes to the new page's heading — the h1, or the
 * workspace tab's heading when only the tab changed — instead of being dropped on <body>.
 *
 * It only ever takes focus that nobody else holds: the poll gives up as soon as focus is on
 * anything other than <body> or the element that had it at NavigationStart, so a field that a
 * user, a dialog or a Selenium/Playwright step focused in the meantime keeps it. Navigations that
 * leave the page and tab unchanged (the dashboard and catalog filters rewrite the query string,
 * the composition panel rewrites :panelTab) do nothing.
 *
 * The poll runs outside the Angular zone: a pending in-zone timer keeps Testability unstable, and
 * the Selenium suite's waitForAngular would sit out the whole bounded wait on every navigation.
 */
@Injectable()
export class RouteFocusService {

    private subscription: Subscription = null;
    private focusAtStart: Element = null;
    private previousKey: {page: string, tab: string} = null;
    private timer: any = null;

    constructor(private router: Router, private zone: NgZone, @Inject(DOCUMENT) private document: any) {
    }

    public start(): void {
        if (this.subscription) { return; }
        this.subscription = this.router.events.subscribe((event: any) => {
            if (event instanceof NavigationStart) {
                this.focusAtStart = this.document.activeElement;
            } else if (event instanceof NavigationEnd) {
                this.onNavigationEnd(this.router.routerState.snapshot.root);
            }
        });
    }

    public stop(): void {
        this.cancel();
        if (this.subscription) {
            this.subscription.unsubscribe();
            this.subscription = null;
        }
    }

    private onNavigationEnd(root: ActivatedRouteSnapshot): void {
        const key = this.keyOf(root);
        const previous = this.previousKey;
        this.previousKey = key;
        if (!previous || previous.page === key.page && previous.tab === key.tab) {
            return;
        }
        this.cancel();
        const tabOnly = previous.page === key.page;
        this.zone.runOutsideAngular(() => this.poll(tabOnly, 0));
    }

    private poll(tabOnly: boolean, attempt: number): void {
        this.timer = setTimeout(() => {
            this.timer = null;
            if (this.focusTaken()) { return; }
            const target = this.findHeading(tabOnly);
            if (target) {
                this.focus(target);
            } else if (attempt + 1 < MAX_POLLS) {
                this.poll(tabOnly, attempt + 1);
            } else {
                const main = this.document.getElementById('main-content');
                if (main) { this.focus(main); }
            }
        }, POLL_INTERVAL_MS);
    }

    private focusTaken(): boolean {
        const active = this.document.activeElement;
        return !!active && active !== this.document.body && active !== this.document.documentElement
            && active !== this.focusAtStart;
    }

    private findHeading(tabOnly: boolean): HTMLElement {
        const tabHeading = tabOnly ? this.document.querySelector(TAB_HEADING_SELECTOR) : null;
        return tabHeading || this.document.querySelector(PAGE_HEADING_SELECTOR);
    }

    private focus(target: HTMLElement): void {
        if (!target.hasAttribute('tabindex')) {
            target.setAttribute('tabindex', '-1');
        }
        // TypeScript 2.4's lib.dom declares focus() without FocusOptions.
        (target as any).focus({preventScroll: true});
    }

    private cancel(): void {
        if (this.timer) {
            clearTimeout(this.timer);
            this.timer = null;
        }
    }

    /**
     * `page` is the top-level route and its own path params; `tab` is the first path segment of
     * its child route plus the child's params, minus the composition panel's `:panelTab`. Query
     * params are deliberately left out.
     */
    private keyOf(root: ActivatedRouteSnapshot): {page: string, tab: string} {
        const top = root && root.firstChild;
        if (!top) { return {page: '', tab: ''}; }
        const page = this.pathOf(top) + JSON.stringify(top.params || {});
        const child = top.firstChild;
        if (!child) { return {page, tab: ''}; }
        const params = Object.assign({}, child.params);
        Object.keys(top.params || {}).forEach((name) => delete params[name]);
        delete params.panelTab;
        const childPath = this.pathOf(child).split('/')[0];
        return {page, tab: childPath + JSON.stringify(params)};
    }

    private pathOf(snapshot: ActivatedRouteSnapshot): string {
        return snapshot.routeConfig && snapshot.routeConfig.path || '';
    }
}
