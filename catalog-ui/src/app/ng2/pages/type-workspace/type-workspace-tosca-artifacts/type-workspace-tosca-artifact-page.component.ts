import {Component, Input, OnInit, ViewChild} from "@angular/core";
import {ArtifactModel, DataTypeModel, IFileDownload} from "../../../../models";
import {Store} from "@ngxs/store";
import {DataTypesService} from "../../../../services/data-types-service";

@Component({
    selector: 'app-type-workspace-tosca-artifact',
    templateUrl: './type-workspace-tosca-artifact-page.component.html',
    styleUrls: ['./type-workspace-tosca-artifact-page.component.less', '../../../../../assets/styles/table-style.less']
})
export class TypeWorkspaceToscaArtifactPageComponent implements OnInit {

    @Input() dataType: DataTypeModel = new DataTypeModel();
    @ViewChild('toscaArtifactsTable') table: any;
    public toscaArtifacts: Array<ArtifactModel> = [];
    public componentId: string;
    public componentType: string;
    public disabled: boolean;
    public iconType: string;
    public testId: string;

    private DOWNLOAD_CSS_CLASSES = {
        DOWNLOAD_ICON: "download-o",
        LOADER_ICON: "spinner"
    }

    private DATATYPE_ARTIFACT = {
        ARTIFACT_NAME : "Tosca Template",
        ARTIFACT_TYPE : "TOSCA_TEMPLATE",
        ARTIFACT_VERSION : "1"
    }

    constructor(private store: Store, private dataTypesService: DataTypesService) {
    }

    ngOnInit(): void {
        this.iconType = this.DOWNLOAD_CSS_CLASSES.DOWNLOAD_ICON;
        this.componentId = this.dataType.uniqueId;
        this.componentType = 'datatype';

        const artifactTemplateForDataType: ArtifactModel = new ArtifactModel();
        artifactTemplateForDataType.artifactDisplayName = this.DATATYPE_ARTIFACT.ARTIFACT_NAME;
        artifactTemplateForDataType.artifactType = this.DATATYPE_ARTIFACT.ARTIFACT_NAME;
        artifactTemplateForDataType.artifactVersion = this.DATATYPE_ARTIFACT.ARTIFACT_VERSION;
        this.toscaArtifacts.push(artifactTemplateForDataType);
    }

    onActivate(event) {
        if (event.type === 'click') {
            this.table.rowDetail.toggleExpandRow(event.row);
        }
    }

    public download = (event) => {
        event.stopPropagation();
        this.dataTypesService.downloadDataType(this.componentId).then(
            (file) => {
                console.log("file", file.data);
                if (file.data) {
                    let blob = this.base64toBlob(file.data.base64Contents, '');
                    let fileName = file.data.artifactName;
                    this.triggerFileDownload(blob, fileName);
                }
            }
        );
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