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

import {Component, Input, ViewEncapsulation} from "@angular/core";
import {GabService, ServerResponse} from "../../../services/gab.service";
import {PathsAndNamesDefinition} from "../../../../models/paths-and-names";

const COLUMN_PREFIX: string = 'col';

@Component({
    selector: 'gab',
    templateUrl: './generic-artifact-browser.component.html',
    styleUrls:['./generic-artifact-browser.component.less'],
    encapsulation: ViewEncapsulation.None
})
export class GenericArtifactBrowserComponent {
    @Input()
    pathsandnames: PathsAndNamesDefinition[];
    @Input()
    artifactid: string;
    @Input()
    resourceid: string;

    columns: ColumnDefinition[];
    rows: any[];
    originRows: any[];
    selectedRows: any[];
    isLoading: boolean;
    ready: boolean;
    columnsFilters: Map<string, string>;

    constructor(private gabService: GabService) {
    }

    ngOnInit() {
        this.ready = false;
        this.isLoading = true;
        this.columns = [];
        this.columnsFilters = new Map<string,string>();
        let paths = this.pathsandnames.map(c => {
            return c.path;
        });
        this.gabService.getArtifact(this.artifactid, this.resourceid, paths)
        .subscribe(response => {
            let typedServerResponse:ServerResponse = <ServerResponse>response.json();
            this.normalizeDataForNgxDatatable(typedServerResponse.data);
            this.isLoading = false;
            this.ready = true;
        });
    }

  updateColumnFilter(event, column) {
    const val = event.target.value.toLowerCase();
    this.columnsFilters.set(column, val);
    let temp_rows = [...this.originRows];

    this.columnsFilters.forEach((value: string, key: string) => {
      temp_rows = this.updateSingleColumnFilter(value, key, temp_rows);
    });

    // update the rows
    this.rows = temp_rows
  }

  private updateSingleColumnFilter(value, column, rows) {
    return rows.filter(function(obj) {
      const row = obj[column];
      return row !== undefined && row.toLowerCase().indexOf(value) !== -1 || !value;
    });
  }

    private normalizeDataForNgxDatatable(data: [{ [key: string]: string }]) {
        let result: NormalizationResult = this.getNormalizationResult(data, this.pathsandnames);
        this.rows = result.rows;
        this.originRows = result.rows;
        this.columns = result.columns;
    }

    private getNormalizationResult(data: [{ [key: string]: string }],
                                   pathsAndNames: PathsAndNamesDefinition[]): NormalizationResult {
        //Prepare column names and column data property names
        let mappingsPathToProp = new Map<string,string>();
        let columnsDefinitions = this.normalizeColumns(pathsAndNames, mappingsPathToProp);

        //Convert rows from { "string": "string" } to { prop : "string" } format
        //This is required by NgxDatatable component
        let arrayOfRows = this.normalizeRows(data, mappingsPathToProp);

        return new NormalizationResult(arrayOfRows, columnsDefinitions);
    }

    private normalizeColumns(pathsAndNames: PathsAndNamesDefinition[], mappingsPathToProp: Map<string,string>) {
        let columnsDefinitions: ColumnDefinition[] = [];
        let index: number = 1;

        pathsAndNames.forEach(function (col) {
            let columnDataPropertyName: string = COLUMN_PREFIX + index;
            mappingsPathToProp.set(col.path, columnDataPropertyName);
            let cell: ColumnDefinition = new ColumnDefinition(col.friendlyName, columnDataPropertyName);
            columnsDefinitions.push(cell);
            index += 1;
        });
        return columnsDefinitions;
    }

    private normalizeRows(data: [{ [key: string]: string }], mappingsPathToProp: Map<string,string>) {
        let arrayOfRows = [];
        data.forEach(function (col) {
            let row = {};
            for (let key in col) {
                if (col.hasOwnProperty(key)) {
                    let columnNameAsProp = mappingsPathToProp.get(key);
                    row[columnNameAsProp] = col[key];
                }
            }
            arrayOfRows.push(row);
        });

        return arrayOfRows;
    }
}

class NormalizationResult {
    constructor(public rows: any[], public columns: ColumnDefinition[]) {}
}

export class ColumnDefinition {
    constructor(public name: string, public prop: string) {}
}

