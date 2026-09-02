import {TestBed} from '@angular/core/testing';
import {CacheService} from './cache.service';

describe('CacheService', () => {
    let service: CacheService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [CacheService]
        });
        service = TestBed.get(CacheService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('set and get', () => {
        it('should store and retrieve a value', () => {
            service.set('key1', 'value1');
            expect(service.get('key1')).toBe('value1');
        });

        it('should store objects', () => {
            const obj = {name: 'test', id: 123};
            service.set('obj', obj);
            expect(service.get('obj')).toBe(obj);
        });

        it('should return undefined for missing keys', () => {
            expect(service.get('nonexistent')).toBeUndefined();
        });

        it('should overwrite existing values', () => {
            service.set('key', 'first');
            service.set('key', 'second');
            expect(service.get('key')).toBe('second');
        });
    });

    describe('remove', () => {
        it('should remove an existing key', () => {
            service.set('key', 'value');
            service.remove('key');
            expect(service.get('key')).toBeUndefined();
        });

        it('should not throw when removing a nonexistent key', () => {
            expect(() => service.remove('missing')).not.toThrow();
        });
    });

    describe('contains', () => {
        it('should return true for existing key', () => {
            service.set('key', 'value');
            expect(service.contains('key')).toBe(true);
        });

        it('should return false for missing key', () => {
            expect(service.contains('missing')).toBe(false);
        });

        it('should return false after key is removed', () => {
            service.set('key', 'value');
            service.remove('key');
            expect(service.contains('key')).toBe(false);
        });
    });
});
