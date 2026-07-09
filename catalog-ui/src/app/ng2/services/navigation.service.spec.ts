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

import {NavigationService} from './navigation.service';

describe('NavigationService', () => {
    let service: NavigationService;
    let mockState: any;
    let mockRouter: any;

    beforeEach(() => {
        mockState = {
            go: jest.fn().mockReturnValue({then: jest.fn()}),
            current: {name: 'workspace.general'},
            params: {id: 'abc123', type: 'SERVICE', previousState: 'catalog'},
            includes: jest.fn().mockReturnValue(false)
        };
        mockRouter = {
            navigate: jest.fn().mockReturnValue(Promise.resolve(true))
        };
        service = new NavigationService(mockState, mockRouter);
    });

    describe('navigate', () => {
        // dashboard and catalog are rendered by ui-router (downgraded <home-page>/<catalog-page>), NOT by
        // the Angular <router-outlet>, so they must navigate through $state.go — routing them through
        // router.navigate() left the view unchanged (dead top-nav CATALOG/HOME click). The ANGULAR_ROUTER_STATES
        // allow-list is therefore empty; these tests lock in that dashboard/catalog use ui-router.
        it('should use $state.go for dashboard state (ui-router-rendered)', () => {
            service.navigate('dashboard');
            expect(mockState.go).toHaveBeenCalledWith('dashboard', undefined, undefined);
            expect(mockRouter.navigate).not.toHaveBeenCalled();
        });

        it('should use $state.go for catalog state (ui-router-rendered)', () => {
            service.navigate('catalog');
            expect(mockState.go).toHaveBeenCalledWith('catalog', undefined, undefined);
            expect(mockRouter.navigate).not.toHaveBeenCalled();
        });

        it('should use $state.go for dashboard with query params', () => {
            service.navigate('dashboard', {show: 'recent', folder: 'DESIGNER'});
            expect(mockState.go).toHaveBeenCalledWith('dashboard', {show: 'recent', folder: 'DESIGNER'}, undefined);
            expect(mockRouter.navigate).not.toHaveBeenCalled();
        });

        it('should use $state.go for catalog with navigation options', () => {
            service.navigate('catalog', {filter: 'active'}, {location: 'replace'});
            expect(mockState.go).toHaveBeenCalledWith('catalog', {filter: 'active'}, {location: 'replace'});
            expect(mockRouter.navigate).not.toHaveBeenCalled();
        });

        it('should fall back to $state.go for workspace states', () => {
            service.navigate('workspace.general', {id: '123', type: 'VF'});
            expect(mockState.go).toHaveBeenCalledWith('workspace.general', {id: '123', type: 'VF'}, undefined);
            expect(mockRouter.navigate).not.toHaveBeenCalled();
        });

        it('should fall back to $state.go for unknown states', () => {
            service.navigate('adminDashboard');
            expect(mockState.go).toHaveBeenCalledWith('adminDashboard', undefined, undefined);
            expect(mockRouter.navigate).not.toHaveBeenCalled();
        });

        it('should fall back to $state.go when router is not available', () => {
            const serviceWithoutRouter = new NavigationService(mockState, null);
            serviceWithoutRouter.navigate('dashboard');
            expect(mockState.go).toHaveBeenCalledWith('dashboard', undefined, undefined);
        });

        it('should pass navigation options to $state.go for non-router states', () => {
            service.navigate('workspace.general', {id: '1'}, {reload: true});
            expect(mockState.go).toHaveBeenCalledWith('workspace.general', {id: '1'}, {reload: true});
        });
    });

    describe('getCurrentStateName', () => {
        it('should return current state name', () => {
            expect(service.getCurrentStateName()).toBe('workspace.general');
        });

        it('should reflect state changes', () => {
            mockState.current.name = 'dashboard';
            expect(service.getCurrentStateName()).toBe('dashboard');
        });
    });

    describe('getParams', () => {
        it('should return all state params', () => {
            expect(service.getParams()).toEqual({id: 'abc123', type: 'SERVICE', previousState: 'catalog'});
        });
    });

    describe('getParam', () => {
        it('should return a specific param value', () => {
            expect(service.getParam('id')).toBe('abc123');
        });

        it('should return undefined for non-existent param', () => {
            expect(service.getParam('nonexistent')).toBeUndefined();
        });
    });

    describe('includes', () => {
        it('should delegate to $state.includes', () => {
            mockState.includes.mockReturnValue(true);
            expect(service.includes('workspace')).toBe(true);
            expect(mockState.includes).toHaveBeenCalledWith('workspace');
        });

        it('should return false when state is not included', () => {
            mockState.includes.mockReturnValue(false);
            expect(service.includes('adminDashboard')).toBe(false);
        });
    });

    describe('updateUrlParams', () => {
        it('should navigate to current state with replace location and no notify', () => {
            service.updateUrlParams({filter: 'resources'});
            expect(mockState.go).toHaveBeenCalledWith('.', {filter: 'resources'}, {location: 'replace', notify: false});
        });
    });
});
