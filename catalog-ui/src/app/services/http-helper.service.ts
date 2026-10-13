import { Inject, Injectable } from '@angular/core';
import { ISdcConfig, SdcConfigToken } from '../config/sdc-config.config';
import { Dictionary } from '../utils/dictionary';
import { SharingService } from './sharing.service';

@Injectable()
export class HttpHelperService {
    constructor( private sharingService: SharingService,
                 @Inject(SdcConfigToken) private sdcConfig: ISdcConfig) {}
    public static replaceUrlParams(url: string, urlParams: { [index: string]: any }): string {
        return url.replace(/:(\w+)/g, (m, p1): string => urlParams[p1] || '');
    }
    public static getHeaderMd5 = (object: any): string => {
        const componentString: string = JSON.stringify(object);
        const md5Result = md5(componentString).toLowerCase();
        return btoa(md5Result);
    }

    public getUuidValue = (url: string): string => {
        const map: Dictionary<string, string> = this.sharingService.getUuidMap();
        if (map && url.indexOf(this.sdcConfig.api.root) > 0) {
            map.forEach((key: string) => {
                if (url.indexOf(key) !== -1) {
                    return this.sharingService.getUuidValue(key);
                }
            });
        }
        return '';
    }
}
