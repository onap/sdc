import {GeneralFormService, ValidationPatterns} from '../general-form.service';

const PATTERNS: ValidationPatterns = {
    name: /^(?=.*[^. ])[\s\w\&_.:-]{1,1024}$/,
    contactId: /^[\s\w-]{1,50}$/,
    tag: /^[\s\w_.-]{1,50}$/,
    vendorName: /^[\x20-\x21\x23-\x29\x2B-\x2E\x30-\x39\x3B\x3D\x40-\x5B\x5D-\x7B\x7D-\xFF]{1,60}$/,
    vendorRelease: /^[\x20-\x21\x23-\x29\x2B-\x2E\x30-\x39\x3B\x3D\x40-\x5B\x5D-\x7B\x7D-\xFF]{1,25}$/,
    vendorModelNumber: /^[\x20-\x21\x23-\x29\x2B-\x2E\x30-\x39\x3B\x3D\x40-\x5B\x5D-\x7B\x7D-\xFF]{1,65}$/,
    comment: /^[ -¿]*$/
};

describe('GeneralFormService', () => {
    let service: GeneralFormService;
    beforeEach(() => { service = new GeneralFormService(); });

    it('builds a FormGroup with the expected controls', () => {
        const form = service.buildForm(PATTERNS);
        ['name', 'description', 'vendorName', 'vendorRelease', 'resourceVendorModelNumber',
         'contactId', 'tags', 'category', 'model'].forEach(c => {
            expect(form.get(c)).toBeTruthy();
        });
    });

    it('marks name required and invalid when empty', () => {
        const form = service.buildForm(PATTERNS);
        form.get('name').setValue('');
        expect(form.get('name').valid).toBe(false);
    });

    it('rejects a name that is only dots/spaces (DE250513)', () => {
        const form = service.buildForm(PATTERNS);
        form.get('name').setValue('. .');
        expect(form.get('name').valid).toBe(false);
    });

    it('accepts a valid name', () => {
        const form = service.buildForm(PATTERNS);
        form.get('name').setValue('MyVF_1');
        expect(form.get('name').valid).toBe(true);
    });

    it('patternValidator passes on empty value', () => {
        const validator = service.patternValidator(PATTERNS.vendorName);
        expect(validator({value: ''} as any)).toBeNull();
    });

    it('patternValidator fails on non-matching value', () => {
        const validator = service.patternValidator(/^[0-9]+$/);
        expect(validator({value: 'abc'} as any)).toEqual({pattern: true});
    });
});
