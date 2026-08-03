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

import {SdcHashLocationStrategy} from './sdc-hash-location-strategy';

/**
 * The '#!' prefix is a compatibility contract, not a style choice: every bookmark users hold, every
 * `cy.visit`/`driver.get` URL in the Cypress + Selenium + Playwright suites, and every link mailed
 * around during design reviews carries it. Angular's HashLocationStrategy emits a bare '#', so
 * without this subclass the prefix silently disappears the moment ng1 stops owning the URL and every
 * one of those URLs 404s into /dashboard. Hence the round-trip assertions below.
 */
describe('SdcHashLocationStrategy', () => {
    let platformLocation: any;
    let strategy: SdcHashLocationStrategy;

    function build(baseHref: string = ''): SdcHashLocationStrategy {
        return new SdcHashLocationStrategy(platformLocation, baseHref);
    }

    beforeEach(() => {
        platformLocation = {
            hash: '',
            pathname: '/',
            search: '',
            onPopState: jest.fn(),
            onHashChange: jest.fn(),
            pushState: jest.fn(),
            replaceState: jest.fn(),
            back: jest.fn(),
            forward: jest.fn()
        };
        strategy = build();
    });

    describe('prepareExternalUrl', () => {
        it('writes the ng1 "#!" prefix on a flat url', () => {
            expect(strategy.prepareExternalUrl('/dashboard')).toBe('#!/dashboard');
        });

        it('writes the ng1 "#!" prefix on a nested url, query string and all', () => {
            expect(strategy.prepareExternalUrl('/catalog/workspace/abc/service/composition/details?tab=deployment'))
                .toBe('#!/catalog/workspace/abc/service/composition/details?tab=deployment');
        });

        it('leaves an empty url empty rather than emitting a bare "#!"', () => {
            expect(strategy.prepareExternalUrl('')).toBe('');
        });
    });

    describe('path', () => {
        it('strips the "!" so the router sees a plain path', () => {
            platformLocation.hash = '#!/catalog/workspace/abc/service/general';
            expect(strategy.path()).toBe('/catalog/workspace/abc/service/general');
        });

        /**
         * A '#/...' link is what an Angular-era HashLocationStrategy or a hand-edited URL produces.
         * ui-router's `hashPrefix('!')` rejected those outright; tolerating them here means old links
         * still resolve instead of falling through to the '**' route.
         */
        it('tolerates a legacy "#/" url with no bang', () => {
            platformLocation.hash = '#/dashboard';
            expect(strategy.path()).toBe('/dashboard');
        });

        it('returns the empty path for an absent or bare hash', () => {
            platformLocation.hash = '';
            expect(strategy.path()).toBe('');
            platformLocation.hash = '#';
            expect(strategy.path()).toBe('');
        });
    });

    /**
     * pushState/replaceState are what the router actually calls on every navigation, and they build
     * their url through prepareExternalUrl — so this is the assertion that the browser address bar
     * really keeps the prefix. The base class is not re-tested here beyond that.
     */
    it('keeps the prefix on the url it hands to the browser history', () => {
        strategy.pushState(null, 'SDC', '/catalog', '?a=1');
        expect(platformLocation.pushState).toHaveBeenCalledWith(null, 'SDC', '#!/catalog?a=1');

        strategy.replaceState(null, 'SDC', '/dashboard', '');
        expect(platformLocation.replaceState).toHaveBeenCalledWith(null, 'SDC', '#!/dashboard');
    });

    it('round-trips a url it wrote itself', () => {
        const url = '/dashboard/workspace/service/general';
        platformLocation.hash = strategy.prepareExternalUrl(url);
        expect(strategy.path()).toBe(url);
    });
});
