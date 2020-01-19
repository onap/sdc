import { Component, Input } from "@angular/core";
import {IFileDownload, Component as TopologyTemplate, ArtifactModel, FullComponentInstance} from "app/models";
import {EventListenerService} from "app/services";
import {CacheService} from "app/services-ng2";
import {EVENTS} from "app/utils";
import { TopologyTemplateService } from "app/ng2/services/component-services/topology-template.service";
import { WorkspaceService } from "app/ng2/pages/workspace/workspace.service";
import { ComponentInstanceServiceNg2 } from "app/ng2/services/component-instance-services/component-instance.service";

@Component({
    selector: 'download-artifact',
    template: `
	<svg-icon [mode]="'primary2'" [disabled]="disabled" [clickable]="!disabled" [name]="iconType" [testId]="testId" mode="info" clickable="true" size="medium" (click)="download($event)"></svg-icon>
`
})
export class DownloadArtifactComponent {
    
    @Input() showLoader:boolean;
    @Input() artifact:ArtifactModel;
    @Input() isInstance: boolean;
    @Input() downloadIconClass: string;
    @Input() componentType: string;
    @Input() componentId: string;
    @Input() testId: string;
    @Input() disabled: boolean;

    public iconType:string;

    private DOWNLOAD_CSS_CLASSES = {
        DOWNLOAD_ICON: "download-o",
        LOADER_ICON: "spinner"
    }
    constructor(private cacheService:CacheService, private EventListenerService:EventListenerService, private topologyTemplateService:TopologyTemplateService,
                private componentInstanceService: ComponentInstanceServiceNg2, private workspaceService:WorkspaceService) {
        
    }

    ngOnInit () {
        this.iconType = this.DOWNLOAD_CSS_CLASSES.DOWNLOAD_ICON;
        this.initDownloadLoader();

    }

    private initDownloadLoader = ()=> {
        //if the artifact is in a middle of download progress register form callBack & change icon from download to loader
        if (this.showLoader && this.cacheService.get(this.artifact.uniqueId)) {
            this.EventListenerService.registerObserverCallback(EVENTS.DOWNLOAD_ARTIFACT_FINISH_EVENT + this.artifact.uniqueId, this.updateDownloadIcon);
            window.setTimeout(():void => {
                if (this.cacheService.get(this.artifact.uniqueId)) {
                    this.iconType = this.DOWNLOAD_CSS_CLASSES.LOADER_ICON;
                }
            }, 1000);
        }
    };

    private updateDownloadIcon = () => {
        this.iconType = this.downloadIconClass || this.DOWNLOAD_CSS_CLASSES.DOWNLOAD_ICON;
    };

    public download = (event) => {
        event.stopPropagation();
        let onFaild = (response):void => {
            console.info('onFaild', response);
            this.removeDownloadedFileLoader();
        };

        let onSuccess = (data:IFileDownload):void => {
            this.downloadFile(data);
            this.removeDownloadedFileLoader();
        };

        this.setDownloadedFileLoader();

        if (this.isInstance) {
            this.componentInstanceService.downloadInstanceArtifact(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, this.componentId, this.artifact.uniqueId).subscribe(onSuccess, onFaild);
        } else {
            this.topologyTemplateService.downloadArtifact(this.componentType, this.componentId, this.artifact.uniqueId).subscribe(onSuccess, onFaild);
        }
    };

    private setDownloadedFileLoader = ()=> {
        if (this.showLoader) {
            //set in cache service thet the artifact is in download progress
            this.cacheService.set(this.artifact.uniqueId, true);
            this.initDownloadLoader();
        }
    };

    private removeDownloadedFileLoader = ()=> {
        if (this.showLoader) {
            this.cacheService.set(this.artifact.uniqueId, false);
            this.EventListenerService.notifyObservers(EVENTS.DOWNLOAD_ARTIFACT_FINISH_EVENT + this.artifact.uniqueId);
        }
    };

    private downloadFile = (file:IFileDownload):void => {
        if (file) {
            let blob = this.base64toBlob(file.base64Contents, '');
            let fileName = file.artifactName;
            this.triggerFileDownload(blob, fileName);
        }
    };

    public base64toBlob = (base64Data, contentType):any => {
        let byteCharacters = atob(base64Data);
        return this.byteCharactersToBlob(byteCharacters, contentType);
    };

    public byteCharactersToBlob = (byteCharacters, contentType):any => {
        contentType = contentType || '';
        let sliceSize = 1024;
        let bytesLength = byteCharacters.length;
        let slicesCount = Math.ceil(bytesLength / sliceSize);
        let byteArrays = new Array(slicesCount);

        for (let sliceIndex = 0; sliceIndex < slicesCount; ++sliceIndex) {
            let begin = sliceIndex * sliceSize;
            let end = Math.min(begin + sliceSize, bytesLength);

            let bytes = new Array(end - begin);
            for (let offset = begin, i = 0; offset < end; ++i, ++offset) {
                bytes[i] = byteCharacters[offset].charCodeAt(0);
            }
            byteArrays[sliceIndex] = new Uint8Array(bytes);
        }
        return new Blob(byteArrays, {type: contentType});
    };

    public triggerFileDownload = (blob, fileName):void=> {
        let url = window.URL.createObjectURL(blob);
        let downloadLink = document.createElement("a");

        downloadLink.setAttribute('href', url);
        downloadLink.setAttribute('download', fileName);
        document.body.appendChild(downloadLink);

        var clickEvent = new MouseEvent("click", {
            "view": window,
            "bubbles": true,
            "cancelable": true
        });
        downloadLink.dispatchEvent(clickEvent);

    }
}
