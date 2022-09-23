/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
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

import {Component, Inject, OnInit} from '@angular/core';
import {MenuItem, MenuItemGroup} from "../../../utils/menu-handler";
import {CacheService} from "../../services/cache.service";
import {DataTypeModel} from "../../../models/data-types";
import {DataTypeService} from "../../services/data-type.service";

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
  currentMenu: MenuItem;

  constructor(private dataTypeService: DataTypeService, private cacheService: CacheService,
              @Inject('$state') private $state: ng.ui.IStateService,
              @Inject('$stateParams') private stateParams) { }

  ngOnInit(): void {
    this.sdcVersion = this.cacheService.get('version');
    this.typeMenuItemGroup = this.createTypeBreadcrumb();
    this.loadDataType();
  }

  private loadDataType(): void {
    if (this.stateParams.id) {
      this.dataTypeService.findById(this.stateParams.id).subscribe(dataType => {
        this.dataType = dataType;
        this.updateTypeBreadcrumb();
      }, error => {
        console.debug('Could not find data type %s', this.stateParams.id, error);
        this.goToBreadcrumbHome();
      });
    } else {
      this.dataType = new DataTypeModel();
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
    this.$state.go(homeMenuItemGroup.menuItems[homeMenuItemGroup.selectedIndex].state);
  }

  onMenuUpdate(menuItemGroup: MenuItemGroup): void {
    this.breadcrumbsModel.push(...[this.typeMenuItemGroup, menuItemGroup]);
  }

  onMenuClick(menuItem: MenuItem): void {
    this.currentMenu = menuItem;
  }

}
