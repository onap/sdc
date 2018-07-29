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

import {Component, Input, ComponentRef} from '@angular/core';
import {ModalService} from 'app/ng2/services/modal.service';
import {ModalModel, ButtonModel} from 'app/models';
import {ServicePathCreatorComponent} from 'app/ng2/pages/service-path-creator/service-path-creator.component';
import {ModalComponent} from 'app/ng2/components/ui/modal/modal.component';
import ServicePathsListComponent from "app/ng2/pages/service-paths-list/service-paths-list.component";
import {Service} from "app/models/components/service";

@Component({
    selector: 'service-path',
    templateUrl: './service-path.component.html',
    styleUrls: ['service-path.component.less'],
    providers: [ModalService]
})

export class ServicePathComponent {
	showServicePathMenu: boolean = false;
	modalInstance: ComponentRef<ModalComponent>;
	@Input() service: Service;
	@Input() onCreate: Function;
	@Input() onSave: Function;
	@Input() isViewOnly:boolean;

	constructor(private ModalServiceNg2: ModalService) {}

	onCreateServicePath = ():void => {
		this.showServicePathMenu = false;
		let cancelButton: ButtonModel = new ButtonModel('Cancel', 'outline white', this.ModalServiceNg2.closeCurrentModal);
		let saveButton: ButtonModel = new ButtonModel('Create', 'blue', this.createPath, this.getDisabled );
		let modalModel: ModalModel = new ModalModel('l', 'Create Service Flow', '', [saveButton, cancelButton], 'standard', true);
		this.modalInstance = this.ModalServiceNg2.createCustomModal(modalModel);
		this.ModalServiceNg2.addDynamicContentToModal(this.modalInstance, ServicePathCreatorComponent, {service: this.service});
		this.modalInstance.instance.open();
	};

	onListServicePath = ():void => {
		this.showServicePathMenu = false;
		let cancelButton: ButtonModel = new ButtonModel('Close', 'outline white', this.ModalServiceNg2.closeCurrentModal);
		let modalModel: ModalModel = new ModalModel('md', 'Service Flows List','', [cancelButton], 'standard', true);
		this.modalInstance = this.ModalServiceNg2.createCustomModal(modalModel);
		this.ModalServiceNg2.addDynamicContentToModal(this.modalInstance, ServicePathsListComponent, {service: this.service,
			onCreateServicePath: this.onCreateServicePath, onEditServicePath: this.onEditServicePath, isViewOnly: this.isViewOnly});
		this.modalInstance.instance.open();
	};

	createPath  = ():void => {
		this.onCreate(this.modalInstance.instance.dynamicContent.instance.createServicePathData());
		this.ModalServiceNg2.closeCurrentModal();
	};

	onEditServicePath = (id:string):void =>   {
		let cancelButton: ButtonModel = new ButtonModel('Cancel', 'outline white', this.ModalServiceNg2.closeCurrentModal);
		let saveButton: ButtonModel = new ButtonModel('Save', 'blue', this.createPath, this.getDisabled );
		let modalModel: ModalModel = new ModalModel('l', 'Edit Path', '', [saveButton, cancelButton], 'standard', true);
		this.modalInstance = this.ModalServiceNg2.createCustomModal(modalModel);
		this.ModalServiceNg2.addDynamicContentToModal(this.modalInstance, ServicePathCreatorComponent, {service: this.service, pathId: id});
		this.modalInstance.instance.open();
	};

	getDisabled = ():boolean =>  {
		return this.isViewOnly || !this.modalInstance.instance.dynamicContent.instance.checkFormValidForSubmit();
	};
}

