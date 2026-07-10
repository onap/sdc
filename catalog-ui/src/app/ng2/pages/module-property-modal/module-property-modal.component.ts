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
import {ChangeDetectionStrategy, Component as NgComponent, OnInit} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import {Component, DisplayModule, PropertyBEModel, PropertyModel} from 'app/models';
import {PROPERTY_TYPES, UNIQUE_GROUP_PROPERTIES_NAME, ValidationUtils} from 'app/utils';
import {ModulePropertyModalService} from './module-property-modal.service';

/**
 * Angular replacement for the AngularJS ModulePropertyView + PropertyFormBaseView
 * (view-models/forms/property-forms/module-property-modal, base-property-form). Edits the value of ONE existing
 * group/module property (name/type read-only); the VF-module readonly rules and min/max/initial-count validation
 * are ported from ModulePropertyView. Mounted as dynamic content by ModalsHandler.openEditModulePropertyModal;
 * the outer ng2 ModalService supplies the Save/Cancel buttons and Save calls {@link #save} (gated on {@link #isValid}).
 */
@NgComponent({
    selector: 'module-property-modal',
    templateUrl: './module-property-modal.component.html',
    styleUrls: ['./module-property-modal.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ModulePropertyModalComponent implements OnInit {

    public input: {
        property: PropertyModel;
        component: Component;
        selectedModule: DisplayModule;
        filteredProperties: PropertyModel[];
    };

    public property: PropertyModel;
    // Active validation error keys (see runUniqueValidation / value-pattern check); the template shows the message.
    public errors: { [key: string]: boolean } = {};

    constructor(private modulePropertyModalService: ModulePropertyModalService,
                private validationUtils: ValidationUtils) {
    }

    ngOnInit(): void {
        // Work on a copy so a Cancel does not mutate the shared list property (old PropertyFormBaseView.initScope:136).
        this.property = new PropertyModel(this.input.property);
        this.initValidation();
    }

    private get component(): Component {
        return this.input.component;
    }

    // Old PropertyFormBaseView.isPropertyValueOwner (property-form-base-model.ts:90-92).
    private isPropertyValueOwner(): boolean {
        return this.component.isService() || !!this.component.selectedInstance;
    }

    public isValueDisabled(): boolean {
        return this.property.readonly && !this.isPropertyValueOwner();
    }

    public isBooleanValue(): boolean {
        return this.valueType() === PROPERTY_TYPES.BOOLEAN;
    }

    private valueType(): string {
        return this.property.simpleType || this.property.type;
    }

    // Ported from ModulePropertyView.initValidation (module-property-model.ts:118-140): the readonly-by-name rules.
    private initValidation(): void {
        switch (this.property.name) {
            case UNIQUE_GROUP_PROPERTIES_NAME.IS_BASE:
            case UNIQUE_GROUP_PROPERTIES_NAME.VF_MODULE_TYPE:
            case UNIQUE_GROUP_PROPERTIES_NAME.VOLUME_GROUP:
            case UNIQUE_GROUP_PROPERTIES_NAME.VF_MODULE_LABEL:
                this.property.readonly = true;
                break;
            case UNIQUE_GROUP_PROPERTIES_NAME.VF_MODULE_DESCRIPTION:
                this.property.readonly = this.component.isService();
                break;
        }
    }

    // Ported from ModulePropertyView.onValueChange (module-property-model.ts:161-172): reset-to-default when the
    // value is cleared, then run the value-format check plus the unique-property numeric validation (Task 7).
    public onValueChange(): void {
        if (!this.property.value) {
            if (this.isPropertyValueOwner()) {
                this.property.value = this.component.isService() ? this.property.parentValue : this.property.defaultValue;
            }
        }
        this.runUniqueValidation();
        this.runValueValidation();
    }

    // Client-side value-format check, reproducing the old template's ng-pattern="getValidationPattern(property.type)"
    // plus the per-type validateJson / validateIntRange $setValidity('pattern', ...) that
    // module-property-view.html applied (see PROPERTY_EDIT_PATTERN). An empty value stays valid (the reset-to-default
    // above already handled the owner case); a malformed value sets errors['pattern'] so isValid() fails, Save is
    // disabled and the value never reaches the BE. Boolean is edited via a <select>, so it needs no pattern check.
    private runValueValidation(): void {
        this.errors['pattern'] = false;
        const raw = this.property.value;
        if (raw == null || raw === '' || this.isBooleanValue()) {
            return;
        }
        const type: string = this.valueType();
        const strValue: string = String(raw);
        const pattern: RegExp = this.validationUtils.getValidationPattern(type);
        if (pattern && !pattern.test(strValue)) {
            this.errors['pattern'] = true;
            return;
        }
        if (type === PROPERTY_TYPES.JSON && !this.validationUtils.validateJson(strValue)) {
            this.errors['pattern'] = true;
            return;
        }
        if (type === PROPERTY_TYPES.INTEGER && !this.validationUtils.validateIntRange(strValue)) {
            this.errors['pattern'] = true;
        }
    }

    // Sibling property numeric value from the module's property list, by unique-group name.
    private findValue(name: string): number {
        const p = (this.input.filteredProperties || []).find((x) => x.name === name);
        return p ? parseInt(p.value, 10) : NaN;
    }

    // Old ModulePropertyView.isUniqueProperty (module-property-model.ts:142-146).
    private isUniqueProperty(): boolean {
        return this.property.name === UNIQUE_GROUP_PROPERTIES_NAME.MIN_VF_MODULE_INSTANCES
            || this.property.name === UNIQUE_GROUP_PROPERTIES_NAME.MAX_VF_MODULE_INSTANCES
            || this.property.name === UNIQUE_GROUP_PROPERTIES_NAME.INITIAL_COUNT;
    }

    // Ported from ModulePropertyView.onValueChange unique-property block (module-property-model.ts:173-235).
    // The old code used $setValidity('KEY', isValid) (valid=true); here errors['KEY'] = !isValid (error=true).
    protected runUniqueValidation(): void {
        this.errors = {};
        if (!this.isUniqueProperty()) {
            return;
        }
        const max = this.findValue(UNIQUE_GROUP_PROPERTIES_NAME.MAX_VF_MODULE_INSTANCES);
        const min = this.findValue(UNIQUE_GROUP_PROPERTIES_NAME.MIN_VF_MODULE_INSTANCES);
        const initial = this.findValue(UNIQUE_GROUP_PROPERTIES_NAME.INITIAL_COUNT);
        const value = parseInt(this.property.value, 10);
        const parent = parseInt(this.property.parentValue, 10);
        const isService = this.component.isService();

        switch (this.property.name) {
            case UNIQUE_GROUP_PROPERTIES_NAME.MIN_VF_MODULE_INSTANCES: {
                const valid = isNaN(max)
                    ? value <= initial
                    : !!value && value <= max && value <= initial;
                this.errors['maxValidation'] = !valid;
                if (isService && !isNaN(parent)) {
                    this.errors['minValidationVfLevel'] = !(value >= parent);
                }
                break;
            }
            case UNIQUE_GROUP_PROPERTIES_NAME.MAX_VF_MODULE_INSTANCES: {
                const valid = isNaN(min)
                    ? value >= initial
                    : isNaN(value) || (value >= min && value >= initial);
                this.errors['minValidation'] = !valid;
                if (isService && !isNaN(parent)) {
                    this.errors['maxValidationVfLevel'] = !(value <= parent);
                }
                break;
            }
            case UNIQUE_GROUP_PROPERTIES_NAME.INITIAL_COUNT: {
                let valid: boolean;
                if (isNaN(min) && isNaN(max)) {
                    valid = true;
                } else if (isNaN(min)) {
                    valid = value <= max;
                } else if (isNaN(max)) {
                    valid = value >= min;
                } else {
                    valid = min <= value && value <= max;
                }
                this.errors['minOrMaxValidation'] = !valid;
                break;
            }
        }
    }

    public isValid(): boolean {
        return Object.keys(this.errors).every((k) => !this.errors[k]);
    }

    public save(): Observable<Array<PropertyBEModel>> {
        // Old ModulePropertyView.save() (module-property-model.ts:112-148) only issued the PUT when the property was
        // editable AND its value actually changed; otherwise it just closed the modal. Preserve that guard so a Save
        // on a read-only or unchanged property does not fire a redundant BE write.
        if (this.property.readonly || this.property.value === this.input.property.value) {
            return Observable.of([]);
        }
        return this.modulePropertyModalService.save(this.component, this.input.selectedModule, this.property);
    }
}
