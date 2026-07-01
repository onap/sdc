import {AdminDashboardComponent} from './admin-dashboard.component';
import {SdcConfigToken} from 'app/ng2/config/sdc-config.config';

// ---------------------------------------------------------------------------
// Factory
// ---------------------------------------------------------------------------

function createComp() {
    const cacheService: any = {
        get: jest.fn((key: string) => {
            if (key === 'version') { return '1.17'; }
            return undefined;
        })
    };

    const sdcConfig: any = {
        api: {
            kibana: 'http://kibana'
        }
    };

    const cdr: any = {detectChanges: jest.fn(), destroyed: false};

    const comp = new AdminDashboardComponent(cacheService, sdcConfig, cdr);

    return {comp, cacheService, sdcConfig, cdr};
}

// ---------------------------------------------------------------------------
// Specs
// ---------------------------------------------------------------------------

describe('AdminDashboardComponent', () => {

    // --- ngOnInit ---

    it('ngOnInit sets currentTab to USER_MANAGEMENT', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        expect(comp.currentTab).toBe('USER_MANAGEMENT');
    });

    it('ngOnInit sets version from cacheService.get("version")', () => {
        const {comp, cacheService} = createComp();
        comp.ngOnInit();
        expect(cacheService.get).toHaveBeenCalledWith('version');
        expect(comp.version).toBe('1.17');
    });

    it('ngOnInit sets monitorUrl from sdcConfig.api.kibana', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        expect(comp.monitorUrl).toBe('http://kibana');
    });

    it('ngOnInit calls detectChanges', () => {
        const {comp, cdr} = createComp();
        comp.ngOnInit();
        expect(cdr.detectChanges).toHaveBeenCalled();
    });

    // --- isSelected ---

    it('isSelected("USER_MANAGEMENT") returns true after ngOnInit', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        expect(comp.isSelected('USER_MANAGEMENT')).toBe(true);
    });

    it('isSelected("CATEGORY_MANAGEMENT") returns false after ngOnInit', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        expect(comp.isSelected('CATEGORY_MANAGEMENT')).toBe(false);
    });

    // --- moveToTab ---

    it('moveToTab("CATEGORY_MANAGEMENT") switches currentTab', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        comp.moveToTab('CATEGORY_MANAGEMENT');
        expect(comp.currentTab).toBe('CATEGORY_MANAGEMENT');
        expect(comp.isSelected('CATEGORY_MANAGEMENT')).toBe(true);
        expect(comp.isSelected('USER_MANAGEMENT')).toBe(false);
    });

    it('moveToTab("CATEGORY_MANAGEMENT") calls detectChanges', () => {
        const {comp, cdr} = createComp();
        comp.ngOnInit();
        cdr.detectChanges.mockClear();
        comp.moveToTab('CATEGORY_MANAGEMENT');
        expect(cdr.detectChanges).toHaveBeenCalled();
    });

    it('moveToTab to current tab is a no-op (does not call detectChanges)', () => {
        const {comp, cdr} = createComp();
        comp.ngOnInit();
        cdr.detectChanges.mockClear();
        comp.moveToTab('USER_MANAGEMENT');  // already current
        expect(cdr.detectChanges).not.toHaveBeenCalled();
        expect(comp.currentTab).toBe('USER_MANAGEMENT');
    });

    it('moveToTab back to USER_MANAGEMENT from CATEGORY_MANAGEMENT switches back', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        comp.moveToTab('CATEGORY_MANAGEMENT');
        comp.moveToTab('USER_MANAGEMENT');
        expect(comp.currentTab).toBe('USER_MANAGEMENT');
        expect(comp.isSelected('USER_MANAGEMENT')).toBe(true);
    });
});
