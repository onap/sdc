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

/**
 * Characterization test for `CookieService` (SDC-4829 Phase 10-12 safety net).
 *
 * Reads user identity from cookies. Two behaviours matter for the migration:
 *  1. When the WebSeal junction cookie is present, the service builds a prefix
 *     `<prefix><junction>!` and reads the user cookies UNDER that prefix.
 *  2. When the junction cookie is absent, the prefix is empty and user cookies are read by
 *     their bare suffix names.
 * The cookie store is injected as `$document` (an array-like whose [0].cookie is the cookie
 * string), matching AngularJS's `$document`.
 */
import {CookieService} from './cookie-service';

describe('CookieService (characterization)', () => {
    const cookieConfig = {
        junctionName: 'junction',
        prefix: 'AMWEBJCT!',
        userIdSuffix: 'USER_ID',
        userFirstName: 'FIRST',
        userLastName: 'LAST',
        userEmail: 'EMAIL',
    };

    const makeService = (cookieString: string) => {
        const sdcConfig = {cookie: cookieConfig} as any;
        const $document = [{cookie: cookieString}];
        return new CookieService(sdcConfig, $document);
    };

    it('reads bare-suffix user cookies when no junction cookie is set', () => {
        const svc = makeService('USER_ID=cs0008; FIRST=Carlos; LAST=Santana; EMAIL=cs@example.com');
        expect(svc.getUserId()).toBe('cs0008');
        expect(svc.getFirstName()).toBe('Carlos');
        expect(svc.getLastName()).toBe('Santana');
        expect(svc.getEmail()).toBe('cs@example.com');
    });

    it('applies the WebSeal junction prefix to user cookie names', () => {
        // junction=proxy -> prefix "AMWEBJCT!proxy!" is prepended to each user cookie name.
        const svc = makeService('junction=proxy; AMWEBJCT!proxy!USER_ID=jh0003');
        expect(svc.getUserId()).toBe('jh0003');
    });

    it('returns empty string for a missing cookie', () => {
        const svc = makeService('SOMETHING_ELSE=1');
        expect(svc.getUserId()).toBe('');
    });

    it('exposes the configured userId suffix', () => {
        const svc = makeService('');
        expect(svc.getUserIdSuffix()).toBe('USER_ID');
    });

    it('tolerates leading spaces after semicolons', () => {
        const svc = makeService('FIRST=Ann;   USER_ID=ab1234');
        expect(svc.getUserId()).toBe('ab1234');
    });
});
