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
import {IFileDownload, Component, ArtifactModel} from "app/models";
import {EventListenerService, CacheService} from "app/services";
import {EVENTS, FileUtils} from "app/utils";

export class DOWNLOAD_CSS_CLASSES {
    static DOWNLOAD_ICON = "table-download-btn tosca";
    static LOADER_ICON = "tlv-loader small loader";
}

export interface IDownloadArtifactScope extends ng.IScope {
    $window:any;
    artifact:ArtifactModel;
    component:Component;
    instance:boolean;
    download:Function;
    showLoader:boolean;
    downloadIconClass:string;
    updateDownloadIcon:Function;
}

export class DownloadArtifactDirective implements ng.IDirective {

    constructor(private $window:any, private cacheService:CacheService, private EventListenerService:EventListenerService, private fileUtils:FileUtils) {
    }

    scope = {
        artifact: '=',
        component: '=',
        instance: '=',
        showLoader: '=',
        downloadIconClass: '@'
    };
    restrict = 'EA';

    link = (scope:IDownloadArtifactScope, element:any) => {
        scope.$window = this.$window;

        element.on("click", function () {
            scope.download(scope.artifact);
        });


        let initDownloadLoader = ()=> {
            //if the artifact is in a middle of download progress register form callBack & change icon from download to loader
            if (scope.showLoader && this.cacheService.get(scope.artifact.uniqueId)) {
                this.EventListenerService.registerObserverCallback(EVENTS.DOWNLOAD_ARTIFACT_FINISH_EVENT + scope.artifact.uniqueId, scope.updateDownloadIcon);
                window.setTimeout(():void => {
                    if (this.cacheService.get(scope.artifact.uniqueId)) {
                        element[0].className = DOWNLOAD_CSS_CLASSES.LOADER_ICON;
                    }
                }, 1000);

            }
        };

        let setDownloadedFileLoader = ()=> {
            if (scope.showLoader) {
                //set in cache service thet the artifact is in download progress
                this.cacheService.set(scope.artifact.uniqueId, true);
                initDownloadLoader();
            }
        };

        let removeDownloadedFileLoader = ()=> {
            if (scope.showLoader) {
                this.cacheService.set(scope.artifact.uniqueId, false);
                this.EventListenerService.notifyObservers(EVENTS.DOWNLOAD_ARTIFACT_FINISH_EVENT + scope.artifact.uniqueId);
            }
        };


        //replace the loader to download icon
        scope.updateDownloadIcon = () => {
            element[0].className = scope.downloadIconClass || DOWNLOAD_CSS_CLASSES.DOWNLOAD_ICON;
        };


        initDownloadLoader();

        scope.download = (artifact:ArtifactModel):void => {

            let onFaild = (response):void => {
                console.info('onFaild', response);
                removeDownloadedFileLoader();
            };

            let onSuccess = (data:IFileDownload):void => {
                downloadFile(data);
                removeDownloadedFileLoader();
            };

            setDownloadedFileLoader();

            if (scope.instance) {
                scope.component.downloadInstanceArtifact(artifact.uniqueId).then(onSuccess, onFaild);
            } else {
                scope.component.downloadArtifact(artifact.uniqueId).then(onSuccess, onFaild);
            }
        };

        let downloadFile = (file:IFileDownload):void => {
            if (file) {
                let blob = this.fileUtils.base64toBlob(file.base64Contents, '');
                let fileName = file.artifactName;
                this.fileUtils.downloadFile(blob, fileName);
            }
        };

        element.on('$destroy', ()=> {
            //remove listener of download event
            if (scope.artifact && scope.artifact.uniqueId) {
                this.EventListenerService.unRegisterObserver(EVENTS.DOWNLOAD_ARTIFACT_FINISH_EVENT + scope.artifact.uniqueId);
            }
        });

    };

    public static factory = ($window:any, cacheService:CacheService, EventListenerService:EventListenerService, fileUtils:FileUtils)=> {
        return new DownloadArtifactDirective($window, cacheService, EventListenerService, fileUtils);
    };

}

DownloadArtifactDirective.factory.$inject = ['$window', 'Sdc.Services.CacheService', 'EventListenerService', 'FileUtils'];
