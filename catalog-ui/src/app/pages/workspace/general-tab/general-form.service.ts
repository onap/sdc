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
import {Injectable} from '@angular/core';
import {AbstractControl, FormControl, FormGroup, ValidatorFn, Validators} from '@angular/forms';

export interface ValidationPatterns {
    name: RegExp;
    contactId: RegExp;
    tag: RegExp;
    vendorName: RegExp;
    vendorRelease: RegExp;
    vendorModelNumber: RegExp;
    comment: RegExp;
}

@Injectable()
export class GeneralFormService {

    // Mirrors the AngularJS ng-pattern behaviour: empty value is valid (required handles
    // emptiness separately), otherwise the value must match the regex.
    public patternValidator(re: RegExp): ValidatorFn {
        return (control: AbstractControl) => {
            const value = control.value;
            if (value === null || value === undefined || value === '') {
                return null;
            }
            return re.test(value) ? null : {pattern: true};
        };
    }

    public buildForm(patterns: ValidationPatterns, isResource: boolean = true): FormGroup {
        // vendorName / vendorRelease are Resource-only fields. The old template wrapped them in
        // ng-if="component.isResource()", and an unrendered AngularJS ng-model field never
        // registered in editForm.$valid — so for a Service they did not exist and did not affect
        // validity. With a reactive form the controls always exist, so requiring them
        // unconditionally leaves a Service permanently invalid (the fields are hidden via *ngIf and
        // can never be filled). That blocks the lifecycle-save/certify gate
        // (registerLifecycleSaveHandler aborts on !form.valid -> certify modal never opens ->
        // Selenium times out on the checkindialog textarea). Mirror the old type-gating: require
        // these only for Resources.
        const vendorNameValidators = isResource
            ? [Validators.required, this.patternValidator(patterns.vendorName)]
            : [this.patternValidator(patterns.vendorName)];
        const vendorReleaseValidators = isResource
            ? [Validators.required, this.patternValidator(patterns.vendorRelease)]
            : [this.patternValidator(patterns.vendorRelease)];
        return new FormGroup({
            name: new FormControl('', [Validators.required, this.patternValidator(patterns.name)]),
            description: new FormControl('', [Validators.required, this.patternValidator(patterns.comment)]),
            vendorName: new FormControl('', vendorNameValidators),
            vendorRelease: new FormControl('', vendorReleaseValidators),
            resourceVendorModelNumber: new FormControl('', [this.patternValidator(patterns.vendorModelNumber)]),
            contactId: new FormControl('', [Validators.required, this.patternValidator(patterns.contactId)]),
            tags: new FormControl([]),
            category: new FormControl('', [Validators.required]),
            model: new FormControl(''),
            instantiationType: new FormControl(''),
            namingPolicy: new FormControl(''),
            ecompGeneratedNaming: new FormControl(true),
            environmentContext: new FormControl(''),
            serviceType: new FormControl(''),
            serviceFunction: new FormControl(''),
            serviceRole: new FormControl('')
        });
    }
}
