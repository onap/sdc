/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

import {Component, Input, ViewChild, ViewEncapsulation} from "@angular/core";
import {GabService, ServerResponse} from "../../../services/gab.service";
import {DatatableComponent} from "@swimlane/ngx-datatable";

const COLUMN_PREFIX: string = 'col';
const REFRESH_TIMEOUT: number = 100;

export class ColumnDefinition {

    constructor(public name: string, public prop: string) {}
}

@Component({
    selector: 'gab',
    templateUrl: './generic-artifact-browser.component.html',
    styleUrls:['./generic-artifact-browser.component.less'],
    encapsulation: ViewEncapsulation.None
})
export class GenericArtifactBrowserComponent {
    @Input()
    columnz: string[];
    @Input()
    artifactid: string;
    @Input()
    resourceid: string;

    @ViewChild(DatatableComponent) table: DatatableComponent;

    columns: ColumnDefinition[];
    rows: any[];
    selectedRows: any[];
    isLoading: boolean;

    constructor(private gabService: GabService) {
    }

    ngOnInit() {
        this.isLoading = true;
        this.columns = [];
        this.gabService.getArtifact(this.artifactid, this.resourceid, this.columnz)
        .subscribe(response => {
            let typedServerResponse:ServerResponse = <ServerResponse>response.json();
            this.normalizeDataForNgxDatatable(typedServerResponse.data);
            this.adjustTableHeight();
            this.isLoading = false;
        });
    }

    private normalizeDataForNgxDatatable(data) {
        //Prepare column names and column data property names
        let mappings = {};
        let index: number = 1;
        let columnsDefinitions: ColumnDefinition[] = [];

        this.columnz.forEach(function(columnFriendlyName) {
            let columnPropertyName: string = COLUMN_PREFIX + index;
            mappings[columnFriendlyName] = columnPropertyName;
            let cell: ColumnDefinition = new ColumnDefinition(columnFriendlyName, mappings[columnFriendlyName]);
            columnsDefinitions.push(cell);
            index += 1;
        });

        //Convert rows from { "string": "string" } to { prop : "string" } format
        //This is required by NgxDatatable component
        let arrayOfRows = [];
        data.forEach(function(col) {
            let row = {};
            for(let key in col) {
                if(col.hasOwnProperty(key)) {
                    let columnNameAsProp = mappings[key];
                    row[columnNameAsProp] = col[key];
                }
            }
            arrayOfRows.push(row);
        });

        this.rows = arrayOfRows;
        this.columns = columnsDefinitions;
    }

    private adjustTableHeight() {
        let newHeight: number = this.table.rowHeight * this.rows.length;
        let newHeightAsString: string = newHeight + 'px';
        setTimeout(() => {
            $('.datatable-body').height(newHeightAsString);
        }, (REFRESH_TIMEOUT));
    }
}
