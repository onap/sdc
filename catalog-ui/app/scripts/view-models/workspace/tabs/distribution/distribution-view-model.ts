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
/// <reference path="../../../../references"/>

module Sdc.ViewModels {
    'use strict';

    interface IDistributionViewModel extends IWorkspaceViewModelScope{
        modalDistribution:ng.ui.bootstrap.IModalServiceInstance;
        service: Models.Components.Service;
        distributions : Array<Models.Distribution>;
        showComponents(distribution:Models.Distribution): void;
        markAsDeployed(distribution:Models.Distribution): void;
        getStatusCount(distributionComponent:Array<Models.DistributionComponent>):any;
        initDistributions():void;
        getUrlName(url:string):string;
        close(): void;
        openDisributionStatusModal:Function;
    }

    export class DistributionViewModel{

        static '$inject' = [
            '$scope',
            'ModalsHandler'

        ];

        constructor(
            private $scope:IDistributionViewModel,
             private ModalsHandler: Sdc.Utils.ModalsHandler
        ){
            this.initScope();
            this.$scope.setValidState(true);
            this.$scope.updateSelectedMenuItem();
        }

        private initScope = (): void => {
            this.$scope.service = <Models.Components.Service>this.$scope.component;


            // Open Distribution status modal
            this.$scope.openDisributionStatusModal = (distribution: Models.Distribution,status:string):void => {
                this.ModalsHandler.openDistributionStatusModal(distribution,status).then(()=>{
                    // OK
                }, ()=>{
                    // ERROR
                });
            };


            this.$scope.showComponents = (distribution: Models.Distribution): void => {
                let onError = (response) => {
                    console.info('onError showComponents',response);
                };
                let onSuccess = (distributionComponents: Array<Models.DistributionComponent>) => {
                    distribution.distributionComponents = distributionComponents;
                    distribution.statusCount = this.$scope.getStatusCount(distribution.distributionComponents);
                   // distribution.components = this.aggregateDistributionComponent(distributionComponents);;
                };
                this.$scope.service.getDistributionsComponent(distribution.distributionID).then(onSuccess, onError);
            };

            this.$scope.getStatusCount = (distributionComponent:Array<Models.DistributionComponent>):any => {
                return _.countBy(distributionComponent, 'status')
            };

            this.$scope.getUrlName = (url:string):string =>{
                let urlName:string = _.last(url.split('/'));
                return urlName;
            };

            this.$scope.markAsDeployed = (distribution: Models.Distribution): void => {
                let onError = (response) => {
                    console.info('onError markAsDeployed',response);
                };
                let onSuccess = (result: any) => {
                    distribution.deployementStatus = 'Deployed';
                };
                this.$scope.service.markAsDeployed(distribution.distributionID).then(onSuccess, onError);

            };

            this.$scope.initDistributions = (): void => {
                let onError = (response) => {
                    console.info('onError initDistributions',response);
                };
                let onSuccess = (distributions: Array<Models.Distribution>) => {
                    this.$scope.distributions = distributions;
                };
                this.$scope.service.getDistributionsList().then(onSuccess, onError);
            };

            this.$scope.initDistributions();

        };


        private aggregateDistributionComponent = (distributionComponents:Array<Models.DistributionComponent>):any =>{
            let aggregateDistributions:Utils.Dictionary<string,Utils.Dictionary<string,Array<Models.DistributionComponent>>> = new Utils.Dictionary<string,Utils.Dictionary<string,Array<Models.DistributionComponent>>>();
            let tempAggregateDistributions:any= _.groupBy(distributionComponents,'omfComponentID');
            let aa = new Utils.Dictionary<string,Array<Models.DistributionComponent>>();

            let tempAggregate:any;
            _.forEach(tempAggregateDistributions,(distributionComponents:Array<Models.DistributionComponent>,omfComponentID:string)=>{

               let urls:any = _.groupBy(distributionComponents,'url');
                aggregateDistributions.setValue(omfComponentID,urls);
               // aggregateDistributions[omfComponentID] = ;

            });
            console.log(aggregateDistributions);
            return aggregateDistributions;
        };


    }
}
