/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  Modifications Copyright (C) 2026 Deutsche Telekom AG.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import {Component, Inject, Injector, OnInit} from '@angular/core';
import {SdcMenuToken, IAppMenu} from "../../config/sdc-menu.config";
import {MenuItem, MenuItemGroup} from "../../utils/menu-handler";
import {CacheService} from "../../services/cache.service";
import {DataTypeModel} from "../../models/data-types";
import {DataTypeService} from "../../services/data-type.service";
import {TypeWorkspaceService} from "./type-workspace.service";
import {TranslateService} from "../../shared/translator/translate.service";
import {NavigationService} from "../../services/navigation.service";
import {HttpErrorResponse} from "@angular/common/http";
import {ServerErrorResponse} from "../../models/server-error-response";
import {Observable} from "rxjs/Observable";
import {SdcUiCommon, SdcUiComponents, SdcUiServices} from "onap-ui-angular/dist";
import {NotificationSettings} from "onap-ui-angular/dist/notifications/utilities/notification.config";

@Component({
  selector: 'app-type-workspace',
  templateUrl: './type-workspace.component.html',
  styleUrls: ['./type-workspace.component.less']
})
export class TypeWorkspaceComponent implements OnInit {

  private typeMenuItemGroup: MenuItemGroup;
  isLoading: boolean;
  disabled: boolean;
  isViewOnly: boolean = true;
  sdcVersion: string;
  breadcrumbsModel: Array<MenuItemGroup> = [];
  dataType: DataTypeModel = new DataTypeModel();
  importedDataType: DataTypeModel = new DataTypeModel();
  currentMenu: MenuItem;

  constructor(private typeWorkspaceService: TypeWorkspaceService,
              private dataTypeService: DataTypeService, private cacheService: CacheService,
              private notificationsService: SdcUiServices.NotificationsService,
              private translateService: TranslateService,
              private navigationService: NavigationService,
              private injector: Injector,
              private modalServiceSdcUI: SdcUiServices.ModalService,
              @Inject(SdcMenuToken) public sdcMenu: IAppMenu) { }

  ngOnInit(): void {
    this.sdcVersion = this.cacheService.get('version');
    this.typeMenuItemGroup = this.createTypeBreadcrumb();

    this.loadDataType();
  }

  private loadDataType(): void {
    if (this.navigationService.getParams().id && this.navigationService.getParams().id != "import") {
      this.dataTypeService.findById(this.navigationService.getParams().id).subscribe(dataType => {
        this.dataType = dataType;
        this.updateTypeBreadcrumb();
      }, error => {
        console.debug('Could not find data type %s', this.navigationService.getParams().id, error);
        this.goToBreadcrumbHome();
      });
        this.isViewOnly = true;
    } else {

      this.isViewOnly = false;
      this.dataType = new DataTypeModel();
    }
  }

  onImportedType(dataType) {
    this.typeMenuItemGroup.updateSelectedMenuItemText(`Data Type: ${dataType.name}`);
  }

  private createImportType() {
      if (this.typeWorkspaceService.dataType.derivedFromName != undefined && this.typeWorkspaceService.dataType.model != undefined) {
          this.dataTypeService.createImportedType(this.typeWorkspaceService.dataType.model.name, this.typeWorkspaceService.importFile)
              .subscribe(response => {
              this.importedDataType = new DataTypeModel(response);
              this.notificationsService.push(new NotificationSettings(
                  'success',
                  this.typeWorkspaceService.dataType.name + ' ' + this.translateService.translate('IMPORT_DATA_TYPE_SUCCESS_MESSAGE_TEXT'),
                  this.translateService.translate('IMPORT_DATA_TYPE_TITLE_TEXT'),
                  5000));
              this.navigationService.navigate(this.navigationService.getCurrentStateName(), {importedFile: null, id: this.typeWorkspaceService.dataType.uniqueId, isViewOnly: true}, {reload: true});
          }, error => {//because overriding http interceptor
                  if (error instanceof HttpErrorResponse) {
                      const errorResponse: ServerErrorResponse = new ServerErrorResponse(error);
                      const modalService = this.injector.get(SdcUiServices.ModalService);
                      const errorDetails = {
                          'Error Code': errorResponse.status != 409 ? errorResponse.messageId : "Data Type already exists",
                          'Status Code': errorResponse.status
                      };
                      modalService.openErrorDetailModal('Error', errorResponse.status != 409 ? errorResponse.message : "Data Type already exists", 'error-modal', errorDetails);
                      return Observable.throwError(error);
                  }
              });
      }
      else {
          this.notificationsService.push(new NotificationSettings(
              'error',
              this.typeWorkspaceService.dataType.name + ' ' + "Derived from is invalid in file",
              this.translateService.translate('IMPORT_DATA_TYPE_TITLE_TEXT'),
              5000));
      }
  }

  private deleteDataType() {
      const modalTitle: string = this.translateService.translate('DELETE_DATA_TYPE_TITLE_CONFIRMATION_TEXT');
      const modalMessage: string = this.translateService.translate('DELETE_DATA_TYPE_MESSAGE_CONFIRMATION_TEXT');;
      const modalButton = {
          testId: 'ok-button',
          text: this.sdcMenu.alertMessages.okButton,
          type: SdcUiCommon.ButtonType.warning,
          callback: this.handleDeleteDataType(),
          closeModal: true
      } as SdcUiComponents.ModalButtonComponent;
      this.modalServiceSdcUI.openWarningModal(modalTitle, modalMessage, 'alert-modal', [modalButton]);
  }

  private handleDeleteDataType():Function {
    return () => {
      this.isLoading = true;
      this.dataTypeService.deleteDataType(this.dataType.uniqueId).subscribe(()=> {
        this.notificationsService.push(new NotificationSettings(
            'success',
            this.dataType.model + ' ' + this.dataType.name + ' ' + this.translateService.translate('DELETE_SUCCESS_MESSAGE_TEXT'),
            this.translateService.translate("DELETE_SUCCESS_MESSAGE_TITLE"),
            5000));
        if (this.navigationService.getParams().previousState) {
            switch (this.navigationService.getParams().previousState) {
                case 'catalog':
                case 'dashboard':
                    this.navigationService.navigate(this.navigationService.getParams().previousState);
                    break;
                default:
                    this.navigationService.navigate('dashboard');
                    break;
            }
        }
    }, (error) => {
        this.isLoading = false;
        this.notificationsService.push(new NotificationSettings(
            'error',
            this.dataType.model + ' ' + this.dataType.name + ' ' + this.translateService.translate('DELETE_FAILURE_MESSAGE_TEXT'),
            this.translateService.translate('DELETE_FAILURE_MESSAGE_TITLE'),
            5000));
        if (error instanceof HttpErrorResponse) {
            const errorResponse: ServerErrorResponse = new ServerErrorResponse(error);
            const modalService = this.injector.get(SdcUiServices.ModalService);
            const errorDetails = {
                'Error Code': errorResponse.messageId,
                'Status Code': errorResponse.status
            };
            modalService.openErrorDetailModal('Error', errorResponse.message, 'error-modal', errorDetails);
        }
    });
    }
  }

  private updateTypeBreadcrumb(): void {
    this.typeMenuItemGroup.updateSelectedMenuItemText(`Data Type: ${this.dataType.name}`);
  }

  private createTypeBreadcrumb(): MenuItemGroup {
    const menuItem = new MenuItem(`Data Type: ${this.dataType ? this.dataType.name : ''}`, undefined, undefined, undefined, [], [], false);
    return new MenuItemGroup(0, [menuItem]);
  }

  goToBreadcrumbHome(): void {
    const homeMenuItemGroup: MenuItemGroup = this.breadcrumbsModel[0];
    this.navigationService.navigate(homeMenuItemGroup.menuItems[homeMenuItemGroup.selectedIndex].state);
  }

  onMenuUpdate(menuItemGroup: MenuItemGroup): void {
    this.breadcrumbsModel.push(...[this.typeMenuItemGroup, menuItemGroup]);
    if (!this.isViewOnly) {
        // menu.js gives the DataType group BARE states ('general'), unlike the 7 component groups'
        // dotted ones ('workspace.general'), so comparing against the bare name is correct here.
        this.typeWorkspaceService.leftBarTabs.menuItems.forEach((item: MenuItem) => {
            item.isDisabled = ('general' !== item.state);
            item.disabledCategory = ('general' !== item.state);
        });
    }
  }

  onMenuClick(menuItem: MenuItem): void {
    this.currentMenu = menuItem;
  }
}
