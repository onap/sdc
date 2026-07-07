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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from 'rxjs/Subscription';
import {CacheService} from 'app/services-ng2';
import {SdcConfigToken, ISdcConfig} from 'app/ng2/config/sdc-config.config';
import {TranslateService} from 'app/ng2/shared/translator/translate.service';

/**
 * Admin Dashboard container component. Replaces the AngularJS AdminDashboardViewModel.
 * Owns the top-bar tab navigation and switches between UserManagementComponent and
 * CategoryManagementComponent via *ngIf.
 *
 * Migrated as part of Phase 7 (AngularJS → Angular migration).
 */
@Component({
    selector: 'admin-dashboard',
    templateUrl: './admin-dashboard.component.html',
    styleUrls: ['./admin-dashboard.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminDashboardComponent implements OnInit, OnDestroy {

    currentTab: string = 'USER_MANAGEMENT';
    version: string;
    monitorUrl: string;

    private languageChangedSubscription: Subscription;

    constructor(private cacheService: CacheService,
                @Inject(SdcConfigToken) private sdcConfig: ISdcConfig,
                private translateService: TranslateService,
                private cdr: ChangeDetectorRef) {}

    ngOnInit(): void {
        this.version = this.cacheService.get('version');
        this.monitorUrl = this.sdcConfig.api.kibana;
        this.currentTab = 'USER_MANAGEMENT';
        // The top-bar tab/Monitor labels use the impure `translate` pipe. On a cold page load
        // (this is the ADMIN landing state) the async language JSON has not resolved yet when
        // OnPush runs its single ngOnInit detectChanges(), so the pipe caches empty strings and
        // nothing fires a later CD (propagateDigest:false). Re-run CD when the language loads so
        // the pipe re-evaluates. languageChangedObservable is publishReplay(1) — it fires
        // immediately if already loaded, or on load otherwise.
        this.languageChangedSubscription = this.translateService.languageChangedObservable.subscribe(() => {
            this.detectChangesSafe();
        });
        this.detectChangesSafe();
    }

    ngOnDestroy(): void {
        if (this.languageChangedSubscription) {
            this.languageChangedSubscription.unsubscribe();
        }
    }

    isSelected(tab: string): boolean {
        return tab === this.currentTab;
    }

    moveToTab(tab: string): void {
        if (tab === this.currentTab) {
            return;
        }
        this.currentTab = tab;
        this.detectChangesSafe();
    }

    private detectChangesSafe(): void {
        if (!(this.cdr as any).destroyed) {
            this.cdr.detectChanges();
        }
    }
}
