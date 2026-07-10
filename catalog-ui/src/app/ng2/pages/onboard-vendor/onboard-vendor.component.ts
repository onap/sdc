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
import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ElementRef,
    Inject,
    OnDestroy,
    OnInit
} from '@angular/core';
import * as _ from 'lodash';
import {CacheService} from 'app/services-ng2';
import {MenuItem, MenuItemGroup} from 'app/utils';
import {SdcConfigToken} from 'app/ng2/config/sdc-config.config';
import {IUserProperties} from 'app/models';
// Side-effect import: defines the global window.PunchOutRegistry used below. The old AngularJS
// <punch-out> directive carried the only require() of this bundle; it was removed with the
// directive layer, so this consumer must load it itself (the IIFE self-guards, so importing it
// from more than one site is idempotent).
import '../../../../third-party/PunchOutRegistry.js';

declare const window: any;

/** CSS class on the host <div> into which the React onboarding punch-out is mounted. */
const HOST_SELECTOR = '.onboard-vendor-punch-out-host';

/**
 * OnboardVendorPageComponent — pure-Angular replacement for the AngularJS
 * OnboardVendorViewModel + onboard-vendor-view.html (Phase 8 migration).
 *
 * Renders the React "onboarding/vendor" PunchOutRegistry component and drives
 * the SDC top-nav breadcrumb via handleBreadcrumbsUpdate, exactly as the old
 * AngularJS view-model did.
 *
 * The deferred-mount pattern (pendingMount flag / tryRender guarded by viewReady)
 * mirrors FlowEditorTabBase (Phase 5): PunchOutRegistry.loadOnBoarding may fire its
 * callback SYNCHRONOUSLY on a cached bundle, before the host <div> exists in the DOM.
 * Deferring the actual render to ngAfterViewInit prevents the null-host trap.
 */
@Component({
    selector: 'onboard-vendor-page',
    templateUrl: './onboard-vendor.component.html',
    styleUrls: ['./onboard-vendor.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class OnboardVendorPageComponent implements OnInit, AfterViewInit, OnDestroy {

    isLoading: boolean = true;
    vendorData: {breadcrumbs: {selectedKeys: string[]}};
    topNavMenuModel: Array<MenuItemGroup>;
    user: IUserProperties;
    version: string;

    // The first MenuItemGroup appended by the onboarding wizard; used as splice anchor.
    private firstControlledTopNavMenu: MenuItemGroup;
    // Track whether the root onboard menu callback has been wrapped (one-time setup).
    private topNavRootMenu: MenuItemGroup;

    private destroyed: boolean = false;
    private viewReady: boolean = false;
    private mounted: boolean = false;
    private pendingMount: boolean = false;

    constructor(
        private cacheService: CacheService,
        private cdr: ChangeDetectorRef,
        private el: ElementRef,
        @Inject(SdcConfigToken) private sdcConfig: any
    ) {}

    ngOnInit(): void {
        this.version = this.cacheService.get('version');
        this.user = this.cacheService.get('user');
        this.vendorData = {breadcrumbs: {selectedKeys: []}};
        this.topNavMenuModel = [];

        window['PunchOutRegistry'].loadOnBoarding(() => {
            if (this.destroyed) { return; }
            this.isLoading = false;
            this.pendingMount = true;
            this.tryRender();
            this.detectChangesSafe();
        });
    }

    ngAfterViewInit(): void {
        this.viewReady = true;
        // If loadOnBoarding already fired synchronously (cached bundle), render now
        // that the host <div> is in the DOM.
        this.tryRender();
    }

    ngOnDestroy(): void {
        this.destroyed = true;
        const host = this.getHost();
        if (host && this.mounted) {
            try {
                window['PunchOutRegistry'].unmount(host);
            } catch (e) {
                // no-op: nothing mounted yet or already unmounted
            }
        }
    }

    /** Called from the template's onEvent binding and from the render props closure. */
    handleVendorEvent(eventName: string, data: any): void {
        switch (eventName) {
            case 'breadcrumbsupdated':
                this.handleBreadcrumbsUpdate(data);
                break;
        }
    }

    // ── private helpers ────────────────────────────────────────────────────────

    private tryRender(): void {
        if (this.destroyed || this.mounted || !this.viewReady || !this.pendingMount) { return; }
        const host = this.getHost();
        if (!host) { return; }
        this.doRender(host);
    }

    private doRender(host: any): void {
        const props = this.buildProps();
        window['PunchOutRegistry'].render(props, host);
        this.mounted = true;
    }

    /**
     * Re-render the punch-out with updated props (mirrors the old AngularJS
     * `scope.$watch('data', render)` that re-rendered whenever vendorData changed).
     * Only called when mounted and viewReady.
     */
    private reRender(): void {
        if (this.destroyed || !this.mounted || !this.viewReady) { return; }
        const host = this.getHost();
        if (!host) { return; }
        this.doRender(host);
    }

    private buildProps(): any {
        return {
            name: 'onboarding/vendor',
            options: {
                data: this.vendorData,
                apiRoot: this.sdcConfig.api.root,
                apiHeaders: this.buildApiHeaders()
            },
            onEvent: (eventName: string, data: any) => this.handleVendorEvent(eventName, data)
        };
    }

    private buildApiHeaders(): any {
        const user = this.user || {};
        const cookie = this.sdcConfig.cookie;
        return {
            userId: {name: cookie.userIdSuffix, value: (user as any).userId},
            userFirstName: {name: cookie.userFirstName, value: (user as any).firstName},
            userLastName: {name: cookie.userLastName, value: (user as any).lastName},
            userEmail: {name: cookie.userEmail, value: (user as any).email}
        };
    }

    private getHost(): any {
        const root = this.el && this.el.nativeElement;
        return root ? root.querySelector(HOST_SELECTOR) : null;
    }

    /**
     * Ported verbatim from OnboardVendorViewModel.updateBreadcrumbsPath.
     * Splices the topNavMenuModel back to the firstControlledTopNavMenu anchor,
     * updates vendorData with the new selectedKeys, and re-renders the punch-out
     * so the React app receives the updated breadcrumb path.
     */
    private updateBreadcrumbsPath = (selectedKeys: Array<string>): Promise<boolean> => {
        const topNavMenuModel = this.topNavMenuModel;
        let startIndex = topNavMenuModel.indexOf(this.firstControlledTopNavMenu);
        if (startIndex === -1) {
            startIndex = topNavMenuModel.length;
        }
        topNavMenuModel.splice(startIndex + selectedKeys.length);
        this.vendorData = {breadcrumbs: {selectedKeys}};
        // Re-render the punch-out with updated vendorData (mirrors $watch('data', render))
        this.reRender();
        this.detectChangesSafe();
        return Promise.resolve(true);
    };

    /**
     * Ported verbatim from OnboardVendorViewModel.handleBreadcrumbsUpdate.
     * Builds an array of MenuItemGroup from the emitted breadcrumb menus and splices
     * them into topNavMenuModel at the firstControlledTopNavMenu anchor.
     */
    private handleBreadcrumbsUpdate(breadcrumbsMenus: Array<any>): void {
        let selectedKeys: string[] = [];
        const topNavMenus: MenuItemGroup[] = breadcrumbsMenus.map((breadcrumbMenu) => {
            const topNavMenu = new MenuItemGroup();
            topNavMenu.menuItems = breadcrumbMenu.menuItems.map((menuItem: any) =>
                new MenuItem(
                    menuItem.displayText,
                    this.updateBreadcrumbsPath as any,
                    null,
                    null,
                    [selectedKeys.concat([menuItem.key])]
                )
            );
            topNavMenu.selectedIndex = _.findIndex(
                breadcrumbMenu.menuItems,
                (item: any) => item.key === breadcrumbMenu.selectedKey
            );
            selectedKeys.push(breadcrumbMenu.selectedKey);
            return topNavMenu;
        });

        const topNavMenuModel = this.topNavMenuModel;
        const len = topNavMenuModel.length;
        let startIndex = topNavMenuModel.indexOf(this.firstControlledTopNavMenu);
        if (startIndex === -1) {
            startIndex = len;
        }
        topNavMenuModel.splice(startIndex, len - startIndex);
        topNavMenuModel.push.apply(topNavMenuModel, topNavMenus);
        this.firstControlledTopNavMenu = topNavMenus[0];

        if (startIndex === 1 && this.topNavRootMenu == null) {
            const topNavRootMenu = topNavMenuModel[0];
            const onboardItem = topNavRootMenu.menuItems[topNavRootMenu.selectedIndex];
            const originalCallback = onboardItem.callback;
            onboardItem.callback = (...args: any[]) => {
                const ret = this.updateBreadcrumbsPath([]);
                return (originalCallback && originalCallback.apply(undefined, args)) || ret;
            };
            this.topNavRootMenu = topNavRootMenu;
        }

        // updateBreadcrumbsPath already calls detectChangesSafe() internally, so no extra CD
        // call is needed here (the old view-model had no explicit CD at this point).
        this.updateBreadcrumbsPath(selectedKeys);
    }

    private detectChangesSafe(): void {
        if (!(this.cdr as any).destroyed) {
            this.cdr.detectChanges();
        }
    }
}
