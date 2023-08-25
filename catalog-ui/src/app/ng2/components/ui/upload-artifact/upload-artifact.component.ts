/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import { Component, EventEmitter, Input, Output } from "@angular/core";
import { ArtifactModel } from "app/models";
import { TranslateService } from '../../../shared/translator/translate.service';
import { SdcUiServices } from 'onap-ui-angular';

@Component({
    selector: 'upload-artifact',
    template: `
	<svg-icon
        [mode]="'primary2'"
        [disabled]="disabled"
        [clickable]="!disabled"
        [name]="iconType"
        [testId]="testId" mode="info"
        (click)="fileUpload.click()"
        clickable="true"
        size="medium"
    ></svg-icon>
    <input
        type="file"
        style="display: none;"
        [disabled]="disabled"
        (change)="onFileSelect($event)"
        [accept]="extensionsWithDot"
        #fileUpload>
`
})
export class UploadArtifactComponent {

    @Input() extensions: string;
    @Input() artifact: ArtifactModel;
    @Input() isInstance: boolean;
    @Input() uploadIconClass: string;
    @Input() componentType: string;
    @Input() componentId: string;
    @Input() testId: string;
    @Input() disabled: boolean;
    @Output("onFileUpload") onFileUpload: EventEmitter<any> = new EventEmitter<any>();

    public extensionsWithDot: string;
    public iconType:string = "upload-o";

    constructor(
        private modalService: SdcUiServices.ModalService,
        private translateService: TranslateService) {

    }

    ngOnInit () {
        this.extensionsWithDot = this.getExtensionsWithDot(this.extensions);
    }

    public getExtensionsWithDot(extensions:string):string {
        extensions = extensions || '';
        return extensions.split(',')
            .map(ext => '.' + ext.toString())
            .join(',');
    }

    public onFileSelect(event) {
        const file = event.target.files[0];
        if (file && file.name) {
            const fileExtension: string = file.name.split('.').pop();
            if (this.extensionsWithDot.includes(fileExtension.toLowerCase())) {
                this.onFileUpload.emit(file);
            } else {
                const title: string = this.translateService.translate('NEW_SERVICE_RESOURCE_ERROR_VALID_TOSCA_EXTENSIONS_TITLE');
                const message: string = this.translateService.translate('NEW_SERVICE_RESOURCE_ERROR_VALID_TOSCA_EXTENSIONS', {extensions: this.extensionsWithDot});
                this.modalService.openWarningModal(title, message, 'error-invalid-tosca-ext');
            }
        }
    }
}
