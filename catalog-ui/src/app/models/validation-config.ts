/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

class PropertyValue {
    min: number;
    max: number;
}

class validationPatterns {
    vendorRelease: RegExp;
    stringOrEmpty: string;
    vendorName: RegExp;
    vendorModelNumber: RegExp;
    tag: RegExp;
    contactId: RegExp;
    componentName: RegExp;
    string: RegExp;
    comment:RegExp;
    integer: RegExp;
}

export class Validations {
    propertyValue: PropertyValue;
    validationPatterns: validationPatterns;
}

export class ValidationConfiguration {
    static validation: Validations;

}

export class Validation {
    componentNameValidationPattern:RegExp;
    contactIdValidationPattern:RegExp;
    tagValidationPattern:RegExp;
    VendorReleaseValidationPattern:RegExp;
    VendorNameValidationPattern:RegExp;
    VendorModelNumberValidationPattern:RegExp;
    commentValidationPattern:RegExp;

    constructor(validationData?:Validations) {
        if(validationData) {
            this.commentValidationPattern = validationData.validationPatterns.comment;
            this.componentNameValidationPattern = validationData.validationPatterns.componentName;
            this.contactIdValidationPattern = validationData.validationPatterns.contactId;
            this.tagValidationPattern = validationData.validationPatterns.tag;
            this.VendorModelNumberValidationPattern = validationData.validationPatterns.vendorModelNumber;
            this.VendorNameValidationPattern = validationData.validationPatterns.vendorName;
            this.VendorReleaseValidationPattern = validationData.validationPatterns.vendorRelease;
        }
    }
}
