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
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {PropertyModel} from 'app/models';
import {ModalsHandler} from 'app/utils';
import {SdcUiCommon, SdcUiComponents, SdcUiServices} from 'onap-ui-angular';
import {WorkspaceMode} from 'app/utils/constants';
import {TranslateService} from 'app/ng2/shared/translator/translate.service';
import {ComponentServiceNg2} from 'app/ng2/services/component-services/component.service';
import {ComponentGenericResponse} from 'app/ng2/services/responses/component-generic-response';
import {WorkspaceService} from '../workspace.service';

interface ITableHeader {
    title: string;
    property: string;
}

/**
 * Workspace "Properties" child tab. Migrated from the AngularJS PropertiesViewModel (Phase 6a) to an
 * OnPush Angular component rendered directly by the ui-router `workspace.properties` state — the last
 * remaining AngularJS workspace child tab.
 *
 * Migration pattern (mirrors GeneralTabComponent / the flow-editor tabs, Phase 3/5 "Mechanism B"):
 *  - read the working component from the shared WorkspaceService in ngOnInit (NOT via @Input — a
 *    downgraded component rendered by a ui-router state with propagateDigest:false never gets inputs);
 *  - OnPush change detection + detectChangesSafe() after every async / cross-framework mutation.
 *
 * The property EDIT modal stays AngularJS for now: it is opened imperatively via the upgraded
 * ModalsHandler ($uibModal + Sdc.ViewModels.PropertyFormViewModel), exactly as the five already-
 * migrated Angular callers (composition properties-tab, capabilities, inputs-table, group/policy,
 * hierarchy) do. It embeds the recursive type-map/type-list/fields-structure directives scheduled for
 * Phase 9, so migrating it here would be root-before-leaf. Phase 6b migrates it after Phase 9.
 *
 * IMPORTANT — OnPush + no digest: the AngularJS modal and the delete both mutate component.properties
 * OUTSIDE this component's zone-driven change detection. The old tab refreshed its table via the
 * global $digest; an OnPush Angular component gets none. We therefore recompute the displayed list and
 * call detectChanges() explicitly when the modal closes and after a delete — this is what keeps the
 * Selenium row-count assertions (ImportVFCAsset / ImportDCAE add/delete) green.
 */
@Component({
    selector: 'workspace-properties-tab',
    templateUrl: './properties-tab.component.html',
    styleUrls: ['./properties-tab.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkspacePropertiesTabComponent implements OnInit {

    component: any;
    isLoading: boolean = false;

    sortBy: string = 'name';
    reverse: boolean = false;
    filterTerms: string = '';

    // The displayed rows — recomputed by refresh(); never a template getter (a fresh array per CD
    // would churn OnPush and risk the "10 $digest" hazard the old AngularJS filter avoided).
    filteredProperties: PropertyModel[] = [];

    tableHeadersList: ITableHeader[] = [
        {title: 'Name', property: 'name'},
        {title: 'Type', property: 'type'},
        {title: 'Schema', property: 'schema.property.type'},
        {title: 'Description', property: 'description'}
    ];

    private mode: WorkspaceMode;

    constructor(private workspaceService: WorkspaceService,
                private componentService: ComponentServiceNg2,
                private modalsHandler: ModalsHandler,
                private modalService: SdcUiServices.ModalService,
                private translateService: TranslateService,
                private cdr: ChangeDetectorRef) {
    }

    ngOnInit(): void {
        this.component = this.workspaceService.component;
        if (!this.component) {
            return;
        }
        this.mode = this.workspaceService.getComponentMode(this.component);
        if (!this.component.properties) {
            this.isLoading = true;
            this.componentService.getComponentProperties(this.component).subscribe(
                (response: ComponentGenericResponse) => {
                    this.component.properties = response.properties;
                    this.refresh();
                    this.isLoading = false;
                    this.detectChangesSafe();
                },
                () => {
                    this.isLoading = false;
                    this.detectChangesSafe();
                }
            );
        } else {
            this.refresh();
        }
    }

    /**
     * Recompute the displayed list from component.properties applying the current sort + free-text
     * filter. Replaces the AngularJS template's `orderBy:sortBy:reverse | filter:{filterTerm}` — done
     * in TS (and only when something changes) so OnPush is not fed a new array every CD cycle.
     */
    private refresh(): void {
        const all: PropertyModel[] = (this.component && this.component.properties) || [];
        let rows = all.slice();
        const term = (this.filterTerms || '').toLowerCase();
        if (term) {
            rows = rows.filter((p) => (p.filterTerm || '').toLowerCase().indexOf(term) !== -1);
        }
        rows.sort((a, b) => {
            const av = this.readPath(a, this.sortBy);
            const bv = this.readPath(b, this.sortBy);
            const cmp = ('' + (av === undefined || av === null ? '' : av))
                .localeCompare('' + (bv === undefined || bv === null ? '' : bv));
            return this.reverse ? -cmp : cmp;
        });
        this.filteredProperties = rows;
        this.detectChangesSafe();
    }

    /**
     * Re-fetch the component's properties from the backend and rebuild the displayed list. Used after
     * the AngularJS edit modal closes, because that modal persists via the BE but does not update
     * component.properties in place (see addOrUpdateProperty). Authoritative and cheap.
     */
    private reloadProperties(): void {
        this.componentService.getComponentProperties(this.component).subscribe(
            (response: ComponentGenericResponse) => {
                this.component.properties = response.properties;
                this.refresh();
            },
            () => {
                // GET failed — keep the current list rather than blanking it
                this.detectChangesSafe();
            }
        );
    }

    /** Read a possibly-nested property path (e.g. 'schema.property.type') off a row for sorting. */
    private readPath(obj: any, path: string): any {
        return path.split('.').reduce((acc, key) => (acc === undefined || acc === null ? acc : acc[key]), obj);
    }

    onFilterChange(): void {
        this.refresh();
    }

    sort(sortBy: string): void {
        this.reverse = (this.sortBy === sortBy) ? !this.reverse : false;
        this.sortBy = sortBy;
        this.refresh();
    }

    /**
     * Open the (still-AngularJS) edit/create property modal, then reload the list on close.
     *
     * Signature mirrors the old PropertiesViewModel.openEditPropertyModal — including passing the
     * DISPLAY-ordered `filteredProperties` so the modal's prev/next navigation walks the list in the
     * same order the user sees.
     *
     * IMPORTANT (the by-reference trap): on Save the modal pushes the new property into the array it
     * was handed and calls topologyTemplateService.addProperty, which does NOT update
     * component.properties. The old AngularJS tab masked this because its $digest re-derived the table
     * straight off a shared component.properties reference; an OnPush Angular component gets no such
     * digest, and we intentionally hand the modal a derived copy. So a plain refresh() would miss the
     * new row. We therefore RELOAD component.properties from the backend on modal close (authoritative,
     * and covers add / update / in-modal delete uniformly). The modal closes (resolves) on both Save
     * and Cancel; a Cancel reload is a cheap, harmless no-op GET.
     */
    addOrUpdateProperty(property?: PropertyModel): void {
        const prop: PropertyModel = property ? property : new PropertyModel();
        prop.readonly = this.isViewMode();
        this.modalsHandler.openEditPropertyModal(
            prop, this.component, this.filteredProperties, false, 'component', this.component.uniqueId, this.isViewMode()
        ).then(() => {
            this.reloadProperties();
        }, () => {
            // modal dismissed — nothing changed
        });
    }

    delete(property: PropertyModel): void {
        const onOk: Function = (): void => {
            // component.deleteProperty (model method) DELETEs on the BE and removes the property from
            // component.properties on success. Refresh + detectChanges after the round-trip so the
            // table row disappears without a page reload (Selenium ImportDCAE/ImportVFCAsset contract).
            const result: any = this.component.deleteProperty(property.uniqueId);
            if (result && typeof result.then === 'function') {
                result.then(() => this.refresh(), () => this.refresh());
            } else {
                this.refresh();
            }
        };
        const title: string = this.translateService.translate('PROPERTY_VIEW_DELETE_MODAL_TITLE');
        const message: string = this.translateService.translate('PROPERTY_VIEW_DELETE_MODAL_TEXT', {name: property.name});
        const okButton = {
            testId: 'OK',
            text: 'OK',
            type: SdcUiCommon.ButtonType.info,
            callback: onOk,
            closeModal: true
        } as SdcUiComponents.ModalButtonComponent;
        this.modalService.openInfoModal(title, message, 'delete-modal', [okButton]);
    }

    // ---- Display helpers ----

    get propertiesCount(): number {
        return (this.component && this.component.properties && this.component.properties.length) || 0;
    }

    /** Strip the heat-datatype prefix for display, matching the old template's inline .replace(). */
    stripHeatPrefix(value: string): string {
        return (value || '').replace('org.openecomp.datatypes.heat.', '');
    }

    trackByUniqueId(index: number, property: PropertyModel): string {
        return property ? property.uniqueId : String(index);
    }

    // ---- Mode helpers (mirror the old workspace shim: both are mode === 'VIEW') ----

    isViewMode(): boolean {
        return this.mode === WorkspaceMode.VIEW;
    }

    isDisableMode(): boolean {
        return this.mode === WorkspaceMode.VIEW;
    }

    private detectChangesSafe(): void {
        if (!(this.cdr as any).destroyed) {
            this.cdr.detectChanges();
        }
    }
}
