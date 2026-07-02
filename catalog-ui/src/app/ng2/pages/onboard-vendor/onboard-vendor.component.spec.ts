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
import {OnboardVendorPageComponent} from './onboard-vendor.component';
import {MenuItemGroup} from 'app/utils';

function makeRegistry() {
    return {
        loadOnBoarding: jest.fn((cb: () => void) => cb()),   // synchronous (cached-bundle path)
        render: jest.fn(),
        unmount: jest.fn()
    };
}

function createComp() {
    const hostEl: any = {tagName: 'DIV'};
    const cacheService: any = {
        get: jest.fn((k: string) => {
            if (k === 'user') { return {userId: 'cs0008', firstName: 'C', lastName: 'S', email: 'cs@x'}; }
            if (k === 'version') { return '1.16.3'; }
            return null;
        })
    };
    const cdr: any = {detectChanges: jest.fn()};
    const el: any = {nativeElement: {querySelector: jest.fn(() => hostEl)}};
    const sdcConfig: any = {
        api: {root: '/sdc2/rest/v1/'},
        cookie: {userIdSuffix: 'USER_ID', userFirstName: 'FN', userLastName: 'LN', userEmail: 'EM'}
    };

    const comp = new OnboardVendorPageComponent(cacheService, cdr, el, sdcConfig);
    return {comp, cacheService, cdr, el, hostEl};
}

describe('OnboardVendorPageComponent', () => {
    let registry: any;

    beforeEach(() => {
        registry = makeRegistry();
        (window as any).PunchOutRegistry = registry;
    });

    afterEach(() => {
        delete (window as any).PunchOutRegistry;
    });

    // Test 1: ngOnInit initializes state and calls loadOnBoarding
    it('ngOnInit initializes vendorData with empty selectedKeys, reads version/user from CacheService, and calls PunchOutRegistry.loadOnBoarding', () => {
        const {comp, cacheService} = createComp();

        comp.ngOnInit();

        expect(comp.vendorData).toEqual({breadcrumbs: {selectedKeys: []}});
        expect(comp.version).toBe('1.16.3');
        expect(comp.user).toEqual({userId: 'cs0008', firstName: 'C', lastName: 'S', email: 'cs@x'});
        expect(comp.topNavMenuModel).toEqual([]);
        expect(registry.loadOnBoarding).toHaveBeenCalledTimes(1);
        expect(cacheService.get).toHaveBeenCalledWith('version');
        expect(cacheService.get).toHaveBeenCalledWith('user');
    });

    // Test 2: handleVendorEvent with 'breadcrumbsupdated' builds topNavMenuModel correctly
    it("handleVendorEvent('breadcrumbsupdated') builds a MenuItemGroup with correct selectedIndex and menuItems count", () => {
        const {comp, cdr} = createComp();
        comp.ngOnInit();
        comp.ngAfterViewInit();

        const breadcrumbsMenus = [
            {
                selectedKey: 'key2',
                menuItems: [
                    {key: 'key1', displayText: 'Item One'},
                    {key: 'key2', displayText: 'Item Two'}
                ]
            }
        ];

        // Precondition: the punch-out was rendered once at mount (ngOnInit + ngAfterViewInit).
        expect(registry.render).toHaveBeenCalledTimes(1);
        const renderCallsBefore = registry.render.mock.calls.length;

        jest.spyOn(cdr, 'detectChanges');
        comp.handleVendorEvent('breadcrumbsupdated', breadcrumbsMenus);

        expect(comp.topNavMenuModel).toHaveLength(1);
        const group: MenuItemGroup = comp.topNavMenuModel[0];
        expect(group).toBeInstanceOf(MenuItemGroup);
        expect(group.menuItems).toHaveLength(2);
        expect(group.selectedIndex).toBe(1);  // 'key2' is at index 1
        expect(cdr.detectChanges).toHaveBeenCalled();
        // Key behavioral commitment: vendorData changed (breadcrumb path updated), so the
        // punch-out MUST be re-rendered — mirrors the old AngularJS $watch('data', render).
        expect(registry.render.mock.calls.length).toBeGreaterThan(renderCallsBefore);
    });

    // Test 3a: ngOnDestroy after a mount calls unmount
    it('ngOnDestroy calls PunchOutRegistry.unmount after the punch-out was mounted', () => {
        const {comp, hostEl} = createComp();
        comp.ngOnInit();
        comp.ngAfterViewInit();

        comp.ngOnDestroy();

        expect(registry.unmount).toHaveBeenCalledWith(hostEl);
    });

    // Test 3b: ngOnDestroy without a prior mount does not throw
    it('ngOnDestroy does not throw if the punch-out was never mounted', () => {
        // When loadOnBoarding fires synchronously BUT the host <div> does not exist
        // (querySelector returns null), nothing is mounted → unmount must not be called.
        const hostEl: any = {tagName: 'DIV'};
        const cacheService: any = {
            get: jest.fn((k: string) => k === 'user' ? {userId: 'cs0008', firstName: 'C', lastName: 'S', email: 'cs@x'} : '1.0')
        };
        const cdr: any = {detectChanges: jest.fn()};
        const el: any = {nativeElement: {querySelector: jest.fn(() => null)}};  // host never found
        const sdcConfig: any = {
            api: {root: '/sdc2/rest/v1/'},
            cookie: {userIdSuffix: 'USER_ID', userFirstName: 'FN', userLastName: 'LN', userEmail: 'EM'}
        };

        const comp = new OnboardVendorPageComponent(cacheService, cdr, el, sdcConfig);
        comp.ngOnInit();
        comp.ngAfterViewInit();

        expect(() => comp.ngOnDestroy()).not.toThrow();
        expect(registry.unmount).not.toHaveBeenCalled();
    });
});
