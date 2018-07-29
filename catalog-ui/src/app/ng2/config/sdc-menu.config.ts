import {Provider, OpaqueToken} from "@angular/core";
import {getSdcMenu} from "./sdc-menu.config.factory";
import {IAppMenu} from "app/models";

export { IAppMenu };

export const SdcMenuToken = new OpaqueToken('SdcMenuToken');

export const SdcMenu:Provider = {
    provide: SdcMenuToken,
    useFactory: getSdcMenu
};
