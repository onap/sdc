'use strict';
import {FileUtils} from "app/utils";

export interface IJsonExportExcelScope extends ng.IScope {
    initExportExcelData:Function;
    export:()=>void;
}

export class JsonExportExcelDirective implements ng.IDirective {

    constructor(private fileUtils:FileUtils) {
    }

    scope = {
        initExportExcelData: '&'//get function that init and returns Models.ExportExcel
    };

    public restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./export-json-to-excel.html');
    };

    private jsonToTableText = (headers:string, dataObj:Array<Object>):string=> {
        let tableText = headers + "\n";
        _.each(dataObj, (rowData:any) => {
            tableText += _.values(rowData).join() + "\n";
        });
        return tableText;
    };

    link = (scope:IJsonExportExcelScope, $elem:any) => {
        scope.export = ():void => {
            let exportExcelData = scope.initExportExcelData();
            exportExcelData.fileName = !!exportExcelData.fileName ? exportExcelData.fileName : 'export-excel';

            let headers = exportExcelData.tableHeaders.join(",");
            let tableData = "";
            if (exportExcelData.groupByField) {
                _.each(_.groupBy(exportExcelData.dataObj, exportExcelData.groupByField), (groupData:Array<Object>) => {
                    tableData += this.jsonToTableText(headers, groupData);
                    tableData += '\n';
                });
            } else {
                tableData = this.jsonToTableText(headers, exportExcelData.dataObj);
            }

            let blob = new Blob([exportExcelData.metaData.join('\n\n') + '\n\n' + tableData], {type: "text/csv;charset=utf-8"});

            return this.fileUtils.downloadFile(blob, exportExcelData.fileName + '.csv');
        };

    };

    public static factory = (fileUtils:FileUtils)=> {
        return new JsonExportExcelDirective( fileUtils);
    };

}

JsonExportExcelDirective.factory.$inject = ['FileUtils'];
