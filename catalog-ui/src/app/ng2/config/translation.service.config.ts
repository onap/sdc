import { Provider } from "@angular/core";
import { TranslateServiceConfigToken } from "../shared/translator/translate.service.config";
import { getTranslationServiceConfig } from "./translation.service.config.factory";

export const TranslationServiceConfig:Provider = {
    provide: TranslateServiceConfigToken,
    useFactory: getTranslationServiceConfig
};
