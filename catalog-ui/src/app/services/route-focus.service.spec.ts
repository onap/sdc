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
import {MAX_POLLS, POLL_INTERVAL_MS, RouteFocusService} from './route-focus.service';

describe('RouteFocusService', () => {
    let service: RouteFocusService;
    let events: Subject<any>;
    let router: any;
    let zone: any;

    const shell = {path: ':previousState/workspace/:id/:type'};
    const general = {path: 'general'};
    const composition = {path: 'composition'};
    const compositionPanel = {path: 'composition/:panelTab'};
    const dashboard = {path: 'dashboard'};
    const catalog = {path: 'catalog'};

    function snapshot(top: any, topParams: any = {}, child?: any, childParams: any = {}): any {
        const childNode = child ? {routeConfig: child, params: Object.assign({}, topParams, childParams), firstChild: null} : null;
        const topNode = {routeConfig: top, params: topParams, firstChild: childNode};
        return {routeConfig: null, params: {}, firstChild: topNode};
    }

    function navigate(root: any): void {
        events.next(new NavigationStart(1, '/x'));
        router.routerState = {snapshot: {root}};
        events.next(new NavigationEnd(1, '/x', '/x'));
    }

    function addMain(html: string): HTMLElement {
        const main = document.createElement('main');
        main.id = 'main-content';
        main.innerHTML = html;
        document.body.appendChild(main);
        return main;
    }

    const wsParams = {previousState: 'dashboard', id: 'uid-1', type: 'service'};

    beforeEach(() => {
        jest.useFakeTimers();
        events = new Subject<any>();
        router = {events, routerState: {snapshot: {root: snapshot(dashboard)}}};
        zone = {runOutsideAngular: jest.fn((fn: Function) => fn())};
        service = new RouteFocusService(router, zone, document);
        service.start();
    });

    afterEach(() => {
        service.stop();
        document.body.innerHTML = '';
        jest.useRealTimers();
    });

    it('leaves focus alone on the initial navigation', () => {
        addMain('<h1>Dashboard</h1>');
        navigate(snapshot(dashboard));
        jest.runAllTimers();
        expect(document.activeElement).toBe(document.body);
    });

    it('moves focus to the new page h1 and makes it programmatically focusable', () => {
        navigate(snapshot(dashboard));
        addMain('<h1>Catalog</h1>');
        navigate(snapshot(catalog));
        jest.advanceTimersByTime(POLL_INTERVAL_MS);
        const h1 = document.querySelector('h1');
        expect(document.activeElement).toBe(h1);
        expect(h1.getAttribute('tabindex')).toBe('-1');
        expect(zone.runOutsideAngular).toHaveBeenCalled();
    });

    it('waits for a heading that renders after the navigation', () => {
        navigate(snapshot(dashboard));
        const main = addMain('');
        navigate(snapshot(catalog));
        jest.advanceTimersByTime(POLL_INTERVAL_MS * 5);
        expect(document.activeElement).toBe(document.body);
        main.innerHTML = '<h1>Catalog</h1>';
        jest.advanceTimersByTime(POLL_INTERVAL_MS);
        expect(document.activeElement).toBe(main.querySelector('h1'));
    });

    it('focuses the tab heading, not the h1, when only the workspace tab changed', () => {
        navigate(snapshot(shell, wsParams, general));
        addMain('<h1>vFW</h1><h2 data-tab-heading>Composition</h2>');
        navigate(snapshot(shell, wsParams, composition));
        jest.advanceTimersByTime(POLL_INTERVAL_MS);
        expect(document.activeElement).toBe(document.querySelector('h2'));
    });

    it('falls back to the h1 when the new tab has no tab heading', () => {
        navigate(snapshot(shell, wsParams, general));
        addMain('<h1>vFW</h1>');
        navigate(snapshot(shell, wsParams, composition));
        jest.advanceTimersByTime(POLL_INTERVAL_MS);
        expect(document.activeElement).toBe(document.querySelector('h1'));
    });

    it('treats a different asset in the same shell as a new page', () => {
        navigate(snapshot(shell, wsParams, general));
        addMain('<h1>vFW</h1><h2 data-tab-heading>General</h2>');
        navigate(snapshot(shell, Object.assign({}, wsParams, {id: 'uid-2'}), general));
        jest.advanceTimersByTime(POLL_INTERVAL_MS);
        expect(document.activeElement).toBe(document.querySelector('h1'));
    });

    it('does nothing when only the query string changed', () => {
        navigate(snapshot(catalog));
        const main = addMain('<h1>Catalog</h1><input id="search">');
        const search = main.querySelector('input') as HTMLInputElement;
        search.focus();
        navigate(snapshot(catalog));
        jest.runAllTimers();
        expect(document.activeElement).toBe(search);
    });

    it('does nothing when only the composition panel tab changed', () => {
        navigate(snapshot(shell, wsParams, composition));
        const main = addMain('<h1>vFW</h1><button id="panel-tab">Info</button>');
        const panelTab = main.querySelector('button') as HTMLElement;
        panelTab.focus();
        navigate(snapshot(shell, wsParams, compositionPanel, {panelTab: 'details'}));
        jest.runAllTimers();
        expect(document.activeElement).toBe(panelTab);
    });

    it('moves focus off the control that triggered the navigation when that control survives it', () => {
        navigate(snapshot(shell, wsParams, general));
        const main = addMain('<h1>vFW</h1><button id="menu">Composition</button><h2 data-tab-heading>Composition</h2>');
        (main.querySelector('#menu') as HTMLElement).focus();
        navigate(snapshot(shell, wsParams, composition));
        jest.advanceTimersByTime(POLL_INTERVAL_MS);
        expect(document.activeElement).toBe(main.querySelector('h2'));
    });

    it('never takes focus from an element focused after the navigation started', () => {
        navigate(snapshot(dashboard));
        const main = addMain('<input id="field">');
        navigate(snapshot(catalog));
        (main.querySelector('#field') as HTMLElement).focus();
        main.insertAdjacentHTML('afterbegin', '<h1>Catalog</h1>');
        jest.runAllTimers();
        expect(document.activeElement).toBe(main.querySelector('#field'));
    });

    it('gives up after a bounded wait and focuses main when no heading ever rendered', () => {
        navigate(snapshot(dashboard));
        const main = addMain('');
        navigate(snapshot(catalog));
        jest.advanceTimersByTime(POLL_INTERVAL_MS * (MAX_POLLS - 1));
        expect(document.activeElement).toBe(document.body);
        jest.advanceTimersByTime(POLL_INTERVAL_MS);
        expect(document.activeElement).toBe(main);
        main.innerHTML = '<h1>late</h1>';
        jest.runAllTimers();
        expect(document.activeElement).toBe(main);
    });

    it('drops a pending wait when the next navigation starts a new one', () => {
        navigate(snapshot(dashboard));
        const main = addMain('');
        navigate(snapshot(catalog));
        const clear = jest.spyOn(window, 'clearTimeout');
        navigate(snapshot(shell, wsParams, general));
        expect(clear).toHaveBeenCalledTimes(1);
        main.innerHTML = '<h1>vFW</h1>';
        jest.advanceTimersByTime(POLL_INTERVAL_MS);
        expect(document.activeElement).toBe(main.querySelector('h1'));
        clear.mockRestore();
    });

    it('stops reacting once stopped', () => {
        navigate(snapshot(dashboard));
        addMain('<h1>Catalog</h1>');
        service.stop();
        navigate(snapshot(catalog));
        jest.runAllTimers();
        expect(document.activeElement).toBe(document.body);
    });
});
