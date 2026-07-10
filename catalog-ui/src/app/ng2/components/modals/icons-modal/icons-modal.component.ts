/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications copyright (c) 2026 Deutsche Telekom AG
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
import {ChangeDetectionStrategy, Component as NgComponent, Inject, OnInit} from '@angular/core';
import * as _ from 'lodash';
import {AvailableIconsService} from 'app/services';
import {Component, IMainCategory, ISubCategory} from 'app/models';
import {ResourceType} from 'app/utils/constants';

/**
 * Angular replacement for the AngularJS IconsModalViewModel (view-models/modals/icons-modal/icons-modal-view.ts).
 * The component is mounted as dynamic content by ModalsHandler.openUpdateIconModal via the ng2 ModalService,
 * which supplies the OK/Cancel buttons; OK reads {@link #updateIcon} (isDirty) and resolves the caller's deferred.
 * The old controller's $state / ComponentFactory dependencies are dropped — the caller (general-tab) owns the
 * dirty -> unsavedChanges reaction.
 */
@NgComponent({
    selector: 'icons-modal',
    templateUrl: './icons-modal.component.html',
    styleUrls: ['./icons-modal.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class IconsModalComponent implements OnInit {

    public component: Component;
    public icons: string[] = [];
    public iconSprite: string;
    public selectedIcon: string;

    constructor(@Inject(AvailableIconsService) private availableIconsService: AvailableIconsService) {
    }

    ngOnInit(): void {
        this.initIcons();
        this.iconSprite = this.component.iconSprite;
        this.selectedIcon = this.component.icon;
        if (this.component.isResource()) {
            this.initVendor();
        }
    }

    public changeIcon(icon: string): void {
        this.selectedIcon = icon;
    }

    /** Mutates component.icon to the selection and returns whether it changed (isDirty).
     *  The ModalsHandler OK button resolves the caller's deferred with this value. */
    public updateIcon(): boolean {
        const isDirty: boolean = this.component.icon !== this.selectedIcon;
        this.component.icon = this.selectedIcon;
        return isDirty;
    }

    // Ported verbatim from IconsModalViewModel._initIcons (icons-modal-view.ts:70-105).
    private initIcons(): void {
        this.icons = [];
        if (this.component.categories && this.component.categories.length > 0) {
            _.forEach(this.component.categories, (category: IMainCategory): void => {
                if (category.icons) {
                    this.icons = this.icons.concat(category.icons);
                }
                if (category.subcategories) {
                    _.forEach(category.subcategories, (subcategory: ISubCategory): void => {
                        if (subcategory.icons) {
                            this.icons = this.icons.concat(subcategory.icons);
                        }
                    });
                }
            });
        }
        if (this.component.isResource()) {
            const resourceType: string = this.component.getComponentSubType();
            if (resourceType === ResourceType.VL) {
                this.icons = ['vl'];
            }
            if (resourceType === ResourceType.CP) {
                this.icons = ['cp'];
            }
        }
        if (this.icons.length === 0) {
            this.icons = this.availableIconsService.getIcons(this.component.componentType);
        }
        // we always add the default icon to the list
        this.icons.push('defaulticon');
    }

    // Ported verbatim from IconsModalViewModel.initVendor (icons-modal-view.ts:107-123).
    private initVendor(): void {
        const vendors: string[] = this.availableIconsService.getIcons(this.component.componentType).slice(5, 19);
        let vendorName = this.component.vendorName.toLowerCase();
        if ('at&t' === vendorName) {
            vendorName = 'att';
        }
        if ('nokia' === vendorName) {
            vendorName = 'nokiasiemens';
        }
        const vendor: string = _.find(vendors, (v: string) => v.replace(/[_]/g, '').toLowerCase() === vendorName);
        if (vendor && this.icons.indexOf(vendor) === -1) {
            this.icons.push(vendor);
        }
    }
}
