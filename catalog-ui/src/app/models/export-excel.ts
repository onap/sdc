/**
 * Created by rcohen on 11/7/2016.
 */
'use strict';

export class ExportExcel {
    fileName:string;
    metaData:Array<string>;//array of text rows that display on the top of table
    dataObj:any;//array of JSONs - the table data
    tableHeaders:Array<string>;
    groupByField:string;//[optional] get field name in order to split data to some tables group by this field
}
