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

export class FileUtils {

    static '$inject' = [
        '$window'
    ];

    constructor(private $window:any) {
    }

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

    public base64toBlob = (base64Data, contentType):any => {
        let byteCharacters = atob(base64Data);
        return this.byteCharactersToBlob(byteCharacters, contentType);
    };

    public downloadFile = (blob, fileName):void=> {
        let url = this.$window.URL.createObjectURL(blob);
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

    public getEntryDefinitionFileNameFromCsarBlob = (csarBlob:Blob):Promise<any> => {
        let JSZip = require("jszip");
        return JSZip.loadAsync(csarBlob).then(zip => {
            return zip.file("TOSCA-Metadata/TOSCA.meta").async("string");
        }).then((toscaMetaData: string) => {
            let fileEntities:Array<string> = toscaMetaData.replace("\r", "").split("\n");
            let entryDefinitionFilename:string = fileEntities.find(element => !element.search("Entry-Definitions"))
                .replace("Entry-Definitions:", "").trim();
            return entryDefinitionFilename;
        });
    }

    public getFileNameDataFromCsarBlob = (csarBlob:Blob, fileName:string):Promise<any> => {
        let JSZip = require("jszip");
        return JSZip.loadAsync(csarBlob).then(zip => {
            return zip.file(fileName).async("string");
        });
    }
}
