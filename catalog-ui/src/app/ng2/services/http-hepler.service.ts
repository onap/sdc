import { Injectable, Inject } from "@angular/core";
import { Dictionary } from "../../utils/dictionary/dictionary";
import { SharingService } from "../services/sharing.service";
import { SdcConfigToken, ISdcConfig } from "../config/sdc-config.config";


@Injectable()
export class HttpHelperService {
    constructor( private sharingService: SharingService,
        @Inject(SdcConfigToken) private sdcConfig: ISdcConfig){}
    
    public getUuidValue = (url: string): string => {
        let map: Dictionary<string, string> = this.sharingService.getUuidMap();
        if (map && url.indexOf(this.sdcConfig.api.root) > 0) {
            map.forEach((key: string) => {
                if (url.indexOf(key) !== -1) {
                    return this.sharingService.getUuidValue(key);
                }
            });
        }
        return '';
    }
    public static replaceUrlParams(url: string, urlParams: { [index: string]: any }): string {
        return url.replace(/:(\w+)/g, (m, p1): string => urlParams[p1] || '');
    }
    public static getHeaderMd5 = (object:any):string => {
        let componentString:string = JSON.stringify(object);
        let md5Result = md5(componentString).toLowerCase();
        return btoa(md5Result);
    };
}