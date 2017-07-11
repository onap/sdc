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

'use strict';
import {Distribution, DistributionComponent, ExportExcel} from "app/models";

interface IDistributionStatusModalViewModelScope {
    distribution:Distribution;
    status:string;
    getStatusCount(distributionComponent:Array<DistributionComponent>):any;
    getUrlName(url:string):string;
    modalDitributionStatus:ng.ui.bootstrap.IModalServiceInstance;
    footerButtons:Array<any>;
    //exportExcelData:ExportExcel;
    close():void;
    initDataForExportExcel():ExportExcel;
}

export class DistributionStatusModalViewModel {

    static '$inject' = ['$scope', '$uibModalInstance', 'data', '$filter'];

    constructor(private $scope:IDistributionStatusModalViewModelScope,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private data:any,
                private $filter:ng.IFilterService) {
        this.initScope();
    }

    private generateMetaDataForExportExcel = ():Array<string>=> {
        let metaData = [];
        metaData[0] = 'Name:' + this.data.component.name + '| UUID:' + this.data.component.uuid + '|  Invariant UUID:' + this.data.component.invariantUUID;
        metaData[1] = 'Distribution ID:' + this.$scope.distribution.distributionID +
            '| USER ID:' + this.$scope.distribution.userId +
            '| Time[UTC]:' + this.$filter('date')(this.$scope.distribution.timestamp, 'MM/dd/yyyy h:mma', 'UTC') +
            '| Status:' + this.$scope.distribution.deployementStatus;
        return metaData;
    };

    private generateDataObjectForExportExcel = ():any=> {
        let correctFormatDataObj = [];
        _.each(this.$scope.distribution.distributionComponents, (dComponent:DistributionComponent) => {
            if (dComponent.status == this.$scope.status) {
                correctFormatDataObj.push({
                    'omfComponentID': dComponent.omfComponentID,
                    'artiFactName': this.$scope.getUrlName(dComponent.url),
                    'url': dComponent.url,
                    'timestamp': this.$filter('date')(dComponent.timestamp, 'MM/dd/yyyy h:mma', 'UTC'),
                    'status': dComponent.status
                });
            }
        });
        return correctFormatDataObj;
    };

    private initScope = ():void => {
        this.$scope.distribution = this.data.distribution;
        this.$scope.status = this.data.status;
        this.$scope.modalDitributionStatus = this.$uibModalInstance;


        this.$scope.getUrlName = (url:string):string => {
            let urlName:string = _.last(url.split('/'));
            return urlName;
        };

        this.$scope.initDataForExportExcel = ():ExportExcel => {
            let exportExcelData = new ExportExcel();
            exportExcelData.fileName = this.$scope.status;
            exportExcelData.groupByField = "omfComponentID";
            exportExcelData.tableHeaders = ["Component ID", "Artifact Name", "URL", "Time(UTC)", "Status"];
            exportExcelData.metaData = this.generateMetaDataForExportExcel();
            exportExcelData.dataObj = this.generateDataObjectForExportExcel();
            return exportExcelData;
        };

        this.$scope.close = ():void => {
            this.$uibModalInstance.close();
        };

        this.$scope.footerButtons = [
            {'name': 'Close', 'css': 'blue', 'callback': this.$scope.close}
        ];

    };
}
