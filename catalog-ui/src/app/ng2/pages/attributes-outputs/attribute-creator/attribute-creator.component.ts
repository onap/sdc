/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 Nordix Foundation. All rights reserved.
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

import {Component} from '@angular/core';
import {DataTypesMap} from 'app/models';
import {DropdownValue} from 'app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component';
import {DataTypeService} from 'app/ng2/services/data-type.service';
import {PROPERTY_DATA} from 'app/utils';
import * as _ from 'lodash';
import {PROPERTY_TYPES} from '../../../../utils';
import {AttributeBEModel} from "../../../../models/attributes-outputs/attribute-be-model";
import {Validation} from "../../../../view-models/workspace/tabs/general/general-view-model";

@Component({
  selector: 'attribute-creator',
  templateUrl: './attribute-creator.component.html',
  styleUrls: ['./attribute-creator.component.less'],
})

export class AttributeCreatorComponent {

  validation:Validation;
  typesAttributes: DropdownValue[];
  typesSchemaAttributes: DropdownValue[];
  attributeModel: AttributeBEModel;
  dataTypes: DataTypesMap;
  isLoading: boolean;

  constructor(protected dataTypeService: DataTypeService) {
  }

  ngOnInit() {
    this.attributeModel = new AttributeBEModel();
    this.attributeModel.type = '';
    this.attributeModel.schema.property.type = '';
    const types: string[] = PROPERTY_DATA.TYPES; // All types - simple type + map + list
    this.dataTypes = this.dataTypeService.getAllDataTypes(); // Get all data types in service
    const nonPrimitiveTypes: string[] = _.filter(Object.keys(this.dataTypes), (type: string) => {
      return types.indexOf(type) === -1;
    });

    this.typesAttributes = _.map(PROPERTY_DATA.TYPES,
        (type: string) => new DropdownValue(type, type)
    );
    const typesSimpleProperties = _.map(PROPERTY_DATA.SIMPLE_TYPES,
        (type: string) => new DropdownValue(type, type)
    );
    const nonPrimitiveTypesValues = _.map(nonPrimitiveTypes,
        (type: string) => new DropdownValue(type,
            type.replace('org.openecomp.datatypes.heat.', ''))
    )
    .sort((a, b) => a.label.localeCompare(b.label));
    this.typesAttributes = _.concat(this.typesAttributes, nonPrimitiveTypesValues);
    this.typesSchemaAttributes = _.concat(typesSimpleProperties, nonPrimitiveTypesValues);
    this.typesAttributes.unshift(new DropdownValue('', 'Select Type...'));
    this.typesSchemaAttributes.unshift(new DropdownValue('', 'Select Schema Type...'));

  }

  checkFormValidForSubmit() {
    const showSchema: boolean = this.showSchema();
    const isSchemaValid: boolean = (!(showSchema && !this.attributeModel.schema.property.type));
    if (!showSchema) {
      this.attributeModel.schema.property.type = '';
    }
    return this.attributeModel.name && this.attributeModel.type && isSchemaValid;
  }

  showSchema(): boolean {
    return [PROPERTY_TYPES.LIST, PROPERTY_TYPES.MAP].indexOf(this.attributeModel.type) > -1;
  }

  onSchemaTypeChange(): void {
    if (this.attributeModel.type === PROPERTY_TYPES.MAP) {
      this.attributeModel.value = JSON.stringify({'': null});
    } else if (this.attributeModel.type === PROPERTY_TYPES.LIST) {
      this.attributeModel.value = JSON.stringify([]);
    }
  }

}
