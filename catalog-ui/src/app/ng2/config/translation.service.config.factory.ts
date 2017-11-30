import { ITranslateServiceConfig } from "../shared/translator/translate.service.config";

declare const __ENV__:string;

export function getTranslationServiceConfig() : ITranslateServiceConfig {
    const pathPrefix = (__ENV__ === 'prod') ? 'sdc1/' : '';
    return {
        filePrefix: pathPrefix + 'assets/languages/',
        fileSuffix: '.json',
        allowedLanguages: ['en_US'],
        defaultLanguage: 'en_US'
    };
}
