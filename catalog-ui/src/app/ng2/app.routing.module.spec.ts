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

import {SdcUrlHandlingStrategy} from './app.routing.module';

describe('SdcUrlHandlingStrategy', () => {
    let strategy: SdcUrlHandlingStrategy;

    beforeEach(() => {
        strategy = new SdcUrlHandlingStrategy();
    });

    describe('shouldProcessUrl', () => {
        it('should process root URL', () => {
            expect(strategy.shouldProcessUrl({toString: () => '/'} as any)).toBe(true);
        });

        it('should process /dashboard URL', () => {
            expect(strategy.shouldProcessUrl({toString: () => '/dashboard'} as any)).toBe(true);
        });

        it('should process /dashboard with query params', () => {
            expect(strategy.shouldProcessUrl({toString: () => '/dashboard?show=recent'} as any)).toBe(true);
        });

        it('should process /catalog URL', () => {
            expect(strategy.shouldProcessUrl({toString: () => '/catalog'} as any)).toBe(true);
        });

        it('should process /catalog with query params', () => {
            expect(strategy.shouldProcessUrl({toString: () => '/catalog?filter.active=true'} as any)).toBe(true);
        });

        it('should process type-workspace URLs', () => {
            expect(strategy.shouldProcessUrl({toString: () => '/catalog/type-workspace/datatype/abc123/general'} as any)).toBe(true);
        });

        it('should process type-workspace from dashboard', () => {
            expect(strategy.shouldProcessUrl({toString: () => '/dashboard/type-workspace/datatype/def456/properties'} as any)).toBe(true);
        });

        it('should NOT process workspace URLs', () => {
            expect(strategy.shouldProcessUrl({toString: () => '/catalog/workspace/abc123/SERVICE/'} as any)).toBe(false);
        });

        it('should NOT process adminDashboard URL', () => {
            expect(strategy.shouldProcessUrl({toString: () => '/adminDashboard'} as any)).toBe(false);
        });

        it('should NOT process onboardVendor URL', () => {
            expect(strategy.shouldProcessUrl({toString: () => '/onboardVendor'} as any)).toBe(false);
        });

        it('should NOT process error-403 URL', () => {
            expect(strategy.shouldProcessUrl({toString: () => '/error-403'} as any)).toBe(false);
        });

        it('should NOT process plugins URL', () => {
            expect(strategy.shouldProcessUrl({toString: () => '/plugins/some-plugin'} as any)).toBe(false);
        });
    });

    describe('extract', () => {
        it('should return the URL unchanged', () => {
            const url = {toString: () => '/dashboard'} as any;
            expect(strategy.extract(url)).toBe(url);
        });
    });

    describe('merge', () => {
        it('should return the new URL part', () => {
            const newUrl = {toString: () => '/catalog'} as any;
            const rawUrl = {toString: () => '/dashboard'} as any;
            expect(strategy.merge(newUrl, rawUrl)).toBe(newUrl);
        });
    });
});
