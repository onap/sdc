import {ComponentMetadataService} from '../component-metadata.service';

describe('ComponentMetadataService', () => {
    let service: ComponentMetadataService;
    beforeEach(() => { service = new ComponentMetadataService(); });

    it('calculateUnique returns mainCategory when no subCategory', () => {
        expect(service.calculateUnique('Network L2-3', '')).toBe('Network L2-3');
    });

    it('calculateUnique joins main and sub with _#_', () => {
        expect(service.calculateUnique('Network L2-3', 'Gateway')).toBe('Network L2-3_#_Gateway');
    });

    it('splitUniqueId splits on _#_', () => {
        expect(service.splitUniqueId('Network L2-3_#_Gateway')).toEqual({main: 'Network L2-3', sub: 'Gateway'});
    });

    it('splitUniqueId handles a main-only id', () => {
        expect(service.splitUniqueId('Network L2-3')).toEqual({main: 'Network L2-3', sub: ''});
    });
});
