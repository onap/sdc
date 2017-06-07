'use strict';

export interface IUrlToBase64Service {
    downloadUrl(url:string, callback:Function):void;
}

export class UrlToBase64Service implements IUrlToBase64Service {
    constructor() {
    }

    public downloadUrl = (url:string, callback:Function):void => {
        let xhr:any = new XMLHttpRequest();

        xhr.onload = ():void => {
            let reader = new FileReader();
            reader.onloadend = ():void => {
                if (xhr.status === 200) {
                    callback(reader.result);
                } else {
                    callback(null);
                }
            };
            reader.readAsDataURL(xhr.response);
        };
        xhr.open('GET', url);
        xhr.responseType = 'blob';
        xhr.send();
    }
}

