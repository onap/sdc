import { getSdcConfig } from './sdc-config.config.factory';
import { getTranslationServiceConfig } from './translation.service.config.factory';
import { environment } from '../../environments/environment';

describe('build environment configuration', () => {
    afterEach(() => environment.production = false);

    it('loads the development API configuration without webpack globals', () => {
        expect(getSdcConfig().environment).toBe('dev');
        expect(getSdcConfig().api.root).toBe('/sdc1/feProxy/rest');
    });

    it('loads development translations from the application root', () => {
        expect(getTranslationServiceConfig().filePrefix).toBe('assets/languages/');
    });

    it('loads production translations from the SDC context', () => {
        environment.production = true;
        expect(getTranslationServiceConfig().filePrefix).toBe('sdc1/assets/languages/');
    });
});