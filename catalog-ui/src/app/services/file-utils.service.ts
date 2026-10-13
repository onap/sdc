import { Injectable } from '@angular/core';
import { WindowRef } from './window.service';

@Injectable()
export class FileUtilsService {
    constructor(private windowRef: WindowRef) {}

        public byteCharactersToBlob = (byteCharacters, contentType): any => {
            contentType = contentType || '';
            const sliceSize = 1024;
            const bytesLength = byteCharacters.length;
            const slicesCount = Math.ceil(bytesLength / sliceSize);
            const byteArrays = new Array(slicesCount);

            for (let sliceIndex = 0; sliceIndex < slicesCount; ++sliceIndex) {
                const begin = sliceIndex * sliceSize;
                const end = Math.min(begin + sliceSize, bytesLength);

                const bytes = new Array(end - begin);
                for (let offset = begin, i = 0; offset < end; ++i, ++offset) {
                    bytes[i] = byteCharacters[offset].charCodeAt(0);
                }
                byteArrays[sliceIndex] = new Uint8Array(bytes);
            }
            return new Blob(byteArrays, {type: contentType});
        }

        public base64toBlob = (base64Data, contentType): any => {
            const byteCharacters = atob(base64Data);
            return this.byteCharactersToBlob(byteCharacters, contentType);
        }

        public downloadFile = (blob, fileName): void => {
            const url = this.windowRef.nativeWindow.URL.createObjectURL(blob);
            const downloadLink = document.createElement('a');

            downloadLink.setAttribute('href', url);
            downloadLink.setAttribute('download', fileName);
            document.body.appendChild(downloadLink);

            const clickEvent = new MouseEvent('click', {
                view: window,
                bubbles: true,
                cancelable: true
            });
            downloadLink.dispatchEvent(clickEvent);
        }
}
