'use strict';
import {Distribution, DistributionComponent, Service} from "app/models";
import {ModalsHandler, Dictionary} from "app/utils";
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";

interface IDistributionViewModel extends IWorkspaceViewModelScope {
    modalDistribution:ng.ui.bootstrap.IModalServiceInstance;
    service:Service;
    distributions:Array<Distribution>;
    showComponents(distribution:Distribution):void;
    markAsDeployed(distribution:Distribution):void;
    getStatusCount(distributionComponent:Array<DistributionComponent>):any;
    initDistributions():void;
    getUrlName(url:string):string;
    close():void;
    openDisributionStatusModal:Function;
}

export class DistributionViewModel {

    static '$inject' = [
        '$scope',
        'ModalsHandler'

    ];

    constructor(private $scope:IDistributionViewModel,
                private ModalsHandler:ModalsHandler) {
        this.initScope();
        this.$scope.setValidState(true);
        this.$scope.updateSelectedMenuItem();
    }

    private initScope = ():void => {
        this.$scope.service = <Service>this.$scope.component;


        // Open Distribution status modal
        this.$scope.openDisributionStatusModal = (distribution:Distribution, status:string):void => {
            this.ModalsHandler.openDistributionStatusModal(distribution, status, this.$scope.component).then(()=> {
                // OK
            }, ()=> {
                // ERROR
            });
        };


        this.$scope.showComponents = (distribution:Distribution):void => {
            let onError = (response) => {
                console.info('onError showComponents', response);
            };
            let onSuccess = (distributionComponents:Array<DistributionComponent>) => {
                distribution.distributionComponents = distributionComponents;
                distribution.statusCount = this.$scope.getStatusCount(distribution.distributionComponents);
                // distribution.components = this.aggregateDistributionComponent(distributionComponents);;
            };
            this.$scope.service.getDistributionsComponent(distribution.distributionID).then(onSuccess, onError);
        };

        this.$scope.getStatusCount = (distributionComponent:Array<DistributionComponent>):any => {
            return _.countBy(distributionComponent, 'status')
        };

        this.$scope.getUrlName = (url:string):string => {
            let urlName:string = _.last(url.split('/'));
            return urlName;
        };

        this.$scope.markAsDeployed = (distribution:Distribution):void => {
            let onError = (response) => {
                console.info('onError markAsDeployed', response);
            };
            let onSuccess = (result:any) => {
                distribution.deployementStatus = 'Deployed';
            };
            this.$scope.service.markAsDeployed(distribution.distributionID).then(onSuccess, onError);

        };

        this.$scope.initDistributions = ():void => {
            let onError = (response) => {
                console.info('onError initDistributions', response);
            };
            let onSuccess = (distributions:Array<Distribution>) => {
                this.$scope.distributions = distributions;
            };
            this.$scope.service.getDistributionsList().then(onSuccess, onError);
        };

        this.$scope.initDistributions();

    };


    private aggregateDistributionComponent = (distributionComponents:Array<DistributionComponent>):any => {
        let aggregateDistributions:Dictionary<string,Dictionary<string,Array<DistributionComponent>>> = new Dictionary<string,Dictionary<string,Array<DistributionComponent>>>();
        let tempAggregateDistributions:any = _.groupBy(distributionComponents, 'omfComponentID');
        let aa = new Dictionary<string,Array<DistributionComponent>>();

        let tempAggregate:any;
        _.forEach(tempAggregateDistributions, (distributionComponents:Array<DistributionComponent>, omfComponentID:string)=> {

            let urls:any = _.groupBy(distributionComponents, 'url');
            aggregateDistributions.setValue(omfComponentID, urls);
            // aggregateDistributions[omfComponentID] = ;

        });
        console.log(aggregateDistributions);
        return aggregateDistributions;
    };
}
