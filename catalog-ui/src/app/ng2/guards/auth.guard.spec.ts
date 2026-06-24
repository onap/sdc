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

import {AuthGuard} from './auth.guard';

describe('AuthGuard', () => {
    let guard: AuthGuard;
    let mockAuthService: any;
    let mockNavigationService: any;

    beforeEach(() => {
        mockAuthService = {
            getLoggedinUser: jest.fn()
        };
        mockNavigationService = {
            navigate: jest.fn()
        };
        guard = new AuthGuard(mockAuthService, mockNavigationService);
    });

    it('should allow access when user is logged in and no permissions required', () => {
        mockAuthService.getLoggedinUser.mockReturnValue({role: 'DESIGNER', userId: 'cs0008'});
        const route = {data: {}} as any;
        expect(guard.canActivate(route, {} as any)).toBe(true);
    });

    it('should allow access when user has required role', () => {
        mockAuthService.getLoggedinUser.mockReturnValue({role: 'DESIGNER', userId: 'cs0008'});
        const route = {data: {permissions: ['DESIGNER', 'ADMIN']}} as any;
        expect(guard.canActivate(route, {} as any)).toBe(true);
    });

    it('should deny access and redirect to error-403 when user is not logged in', () => {
        mockAuthService.getLoggedinUser.mockReturnValue(null);
        const route = {data: {}} as any;
        expect(guard.canActivate(route, {} as any)).toBe(false);
        expect(mockNavigationService.navigate).toHaveBeenCalledWith('error-403');
    });

    it('should deny access when user does not have required role', () => {
        mockAuthService.getLoggedinUser.mockReturnValue({role: 'DESIGNER', userId: 'cs0008'});
        const route = {data: {permissions: ['ADMIN']}} as any;
        expect(guard.canActivate(route, {} as any)).toBe(false);
        expect(mockNavigationService.navigate).toHaveBeenCalledWith('error-403');
    });

    it('should allow access when permissions array is empty', () => {
        mockAuthService.getLoggedinUser.mockReturnValue({role: 'DESIGNER', userId: 'cs0008'});
        const route = {data: {permissions: []}} as any;
        expect(guard.canActivate(route, {} as any)).toBe(true);
    });
});
