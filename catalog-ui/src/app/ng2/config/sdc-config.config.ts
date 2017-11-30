import {Provider, OpaqueToken} from "@angular/core";
import {getSdcConfig, ISdcConfig} from "./sdc-config.config.factory";

export { ISdcConfig };

export const SdcConfigToken = new OpaqueToken('SdcConfigToken');

export const SdcConfig:Provider = {
    provide: SdcConfigToken,
    useFactory: getSdcConfig
};
