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
}
