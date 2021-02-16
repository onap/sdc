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

import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {InstanceFeDetails} from '../../../../models/instance-fe-details';
import {InstanceFeAttributesMap} from "../../../../models/attributes-outputs/attribute-fe-map";
import {AttributeFEModel} from "../../../../models/attributes-outputs/attribute-fe-model";
import {AttributesService} from "../../../services/attributes.service";
import {DerivedFEAttribute} from "../../../../models/attributes-outputs/derived-fe-attribute";

@Component({
  selector: 'attributes-table',
  templateUrl: './attributes-table.component.html',
  styleUrls: ['./attributes-table.component.less']
})
export class AttributesTableComponent implements OnChanges {

  @Input() feAttributesMap: InstanceFeAttributesMap;
  @Input() feInstanceNamesMap: Map<string, InstanceFeDetails>;
  @Input() selectedAttributeId: string;
  @Input() attributeNameSearchText: string;
  @Input() searchTerm: string;
  @Input() readonly: boolean;
  @Input() isLoading: boolean;
  @Input() hasDeclareOption: boolean;
  @Input() hideAttributeType: boolean;
  @Input() showDelete: boolean;

  @Output('attributeChanged') emitter: EventEmitter<AttributeFEModel> = new EventEmitter<AttributeFEModel>();
  @Output() selectAttributeRow: EventEmitter<AttributeRowSelectedEvent> = new EventEmitter<AttributeRowSelectedEvent>();
  @Output() updateCheckedAttributeCount: EventEmitter<boolean> = new EventEmitter<boolean>(); // only for hasDeclareOption
  @Output() updateCheckedChildAttributeCount: EventEmitter<boolean> = new EventEmitter<boolean>();//only for hasDeclareListOption
  @Output() deleteAttribute: EventEmitter<AttributeFEModel> = new EventEmitter<AttributeFEModel>();

  sortBy: string;
  reverse: boolean;
  direction: number;
  path: string[];

  readonly ascUpperLettersFirst = 1;
  readonly descLowerLettersFirst = -1;

  constructor(private attributesService: AttributesService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.fePropertiesMap) {
      this.sortBy = '';
      this.sort('name');
    }
  }

  sort(sortBy) {
    this.reverse = (this.sortBy === sortBy) ? !this.reverse : true;
    this.direction = this.reverse ? this.ascUpperLettersFirst : this.descLowerLettersFirst;
    this.sortBy = sortBy;
    this.path = sortBy.split('.');
  }

  onAttributeChanged = (attribute) => {
    this.emitter.emit(attribute);
  }

  // Click on main row (row of AttributeFEModel)
  onClickAttributeRow = (attribute: AttributeFEModel, instanceName: string) => {
    this.selectedAttributeId = attribute.name;
    const attributeRowSelectedEvent: AttributeRowSelectedEvent = new AttributeRowSelectedEvent(attribute, instanceName);
    this.selectAttributeRow.emit(attributeRowSelectedEvent);
  }

  // Click on inner row (row of DerivedFEAttribute)
  onClickAttributeInnerRow = (attribute: DerivedFEAttribute, instanceName: string) => {
    const attributeRowSelectedEvent: AttributeRowSelectedEvent = new AttributeRowSelectedEvent(attribute, instanceName);
    this.selectAttributeRow.emit(attributeRowSelectedEvent);
  }

  attributeChecked = (attrib: AttributeFEModel, childAttribName?: string) => {
    const isChecked: boolean = (!childAttribName) ? attrib.isSelected : attrib.flattenedChildren.find((attrib) => attrib.attributesName == childAttribName).isSelected;

    if (isChecked) {
      this.attributesService.disableRelatedAttributes(attrib, childAttribName);
    } else {
      this.attributesService.undoDisableRelatedAttributes(attrib, childAttribName);
    }
    this.updateCheckedAttributeCount.emit(isChecked);

  }

}

export class AttributeRowSelectedEvent {
  attributeModel: AttributeFEModel | DerivedFEAttribute;
  instanceName: string;

  constructor(attributeModel: AttributeFEModel | DerivedFEAttribute, instanceName: string) {
    this.attributeModel = attributeModel;
    this.instanceName = instanceName;
  }
}
