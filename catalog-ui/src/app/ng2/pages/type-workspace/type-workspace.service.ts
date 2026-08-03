/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import {Injectable} from '@angular/core';
import {DataTypeModel} from '../../../models/data-types';
import {MenuItemGroup} from '../../../utils/menu-handler';

/**
 * The shared state the three type-workspace components used to exchange through the inherited
 * AngularJS workspace scope. `dataType`, `importFile` and `leftBarTabs` are the only members of
 * that scope any of them ever touched, so the whole bus shrinks to this service.
 */
@Injectable()
export class TypeWorkspaceService {

    public dataType: DataTypeModel;
    public importFile: any;
    public leftBarTabs: MenuItemGroup;
}
