import 'rxjs/add/operator/map';
import {of, throwError, Subject} from 'rxjs';
import {IUserProperties} from 'app/models/user';
import {UserManagementComponent} from '../user-management.component';

function makeUser(userId: string, overrides: any = {}): IUserProperties {
    return Object.assign({
        userId,
        firstName: 'First_' + userId,
        lastName: 'Last_' + userId,
        email: userId + '@example.com',
        role: 'DESIGNER',
        tempRole: '',
        lastLoginTime: '1609459200000',
        status: 'ACTIVE',
        isInEditMode: false,
        filterTerm: ''
    }, overrides) as IUserProperties;
}

function createComp() {
    const userService: any = {
        getAllUsers: jest.fn(() => of([makeUser('u1'), makeUser('u2')]))
    };
    // Emit on demand to simulate the async language JSON resolving after ngOnInit.
    const languageChanged = new Subject<string>();
    const translateService: any = {
        translate: jest.fn((key: string) => key),
        languageChangedObservable: languageChanged
    };
    const cdr: any = {detectChanges: jest.fn()};

    const comp = new UserManagementComponent(userService, translateService, cdr);
    return {comp, userService, translateService, languageChanged, cdr};
}

describe('UserManagementComponent', () => {

    it('populates usersList with index on ngOnInit and sets isLoading=false', () => {
        const {comp, userService} = createComp();
        expect(comp.isLoading).toBe(false);
        comp.ngOnInit();
        expect(userService.getAllUsers).toHaveBeenCalledTimes(1);
        expect(comp.usersList).toHaveLength(2);
        expect(comp.usersList[0].index).toBe(0);
        expect(comp.usersList[1].index).toBe(1);
        expect(comp.isLoading).toBe(false);
    });

    it('populates filteredUsers after ngOnInit', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        expect(comp.filteredUsers).toHaveLength(2);
    });

    it('sets isLoading=false on error', () => {
        const {comp, userService} = createComp();
        userService.getAllUsers.mockReturnValue(throwError('fail'));
        comp.ngOnInit();
        expect(comp.isLoading).toBe(false);
        expect(comp.usersList).toHaveLength(0);
    });

    it('builds tableHeadersList with 6 headers on ngOnInit', () => {
        const {comp, translateService} = createComp();
        comp.ngOnInit();
        expect(comp.tableHeadersList).toHaveLength(6);
        expect(comp.tableHeadersList[0]).toEqual({title: 'First Name', property: 'firstName'});
        expect(comp.tableHeadersList[1]).toEqual({title: 'Last Name', property: 'lastName'});
        // userId title comes from translate service
        expect(translateService.translate).toHaveBeenCalledWith('USER_MANAGEMENT_TABLE_HEADER_USER_ID');
        expect(comp.tableHeadersList[2].property).toBe('userId');
        expect(comp.tableHeadersList[3]).toEqual({title: 'Email', property: 'email'});
        expect(comp.tableHeadersList[4]).toEqual({title: 'Role', property: 'role'});
        expect(comp.tableHeadersList[5]).toEqual({title: 'Last Active', property: 'lastLoginTime'});
    });

    it('sort sets sortBy and toggles reverse only on repeated click of same column', () => {
        const {comp} = createComp();
        comp.ngOnInit();

        // click a new column → sets sortBy, reverse stays false
        comp.sort('firstName');
        expect(comp.sortBy).toBe('firstName');
        expect(comp.reverse).toBe(false);

        // click the same column again → toggles reverse
        comp.sort('firstName');
        expect(comp.sortBy).toBe('firstName');
        expect(comp.reverse).toBe(true);

        // click yet again → toggles back
        comp.sort('firstName');
        expect(comp.reverse).toBe(false);

        // switch to a different column → reverse resets to false
        comp.sort('lastName');
        expect(comp.sortBy).toBe('lastName');
        expect(comp.reverse).toBe(false);
    });

    it('getTitle converts role verbatim: GOVERNOR → governance Rep (capital R)', () => {
        const {comp} = createComp();
        expect(comp.getTitle('GOVERNOR')).toBe('governance Rep');
    });

    it('getTitle converts DESIGNER → designer', () => {
        const {comp} = createComp();
        expect(comp.getTitle('DESIGNER')).toBe('designer');
    });

    it('getTitle converts PRODUCT_STRATEGIST → product strategist (first _ replaced)', () => {
        const {comp} = createComp();
        expect(comp.getTitle('PRODUCT_STRATEGIST')).toBe('product strategist');
    });

    it('filteredUsers narrows by searchTerm via onSearchChange', () => {
        const {comp, userService} = createComp();
        // two users: u1 (firstName=First_u1) and u2 (firstName=First_u2)
        userService.getAllUsers.mockReturnValue(of([
            makeUser('u1', {firstName: 'Alice', lastName: 'Smith', email: 'alice@example.com', role: 'DESIGNER', lastLoginTime: '0'}),
            makeUser('u2', {firstName: 'Bob', lastName: 'Jones', email: 'bob@example.com', role: 'ADMIN', lastLoginTime: '0'})
        ]));
        comp.ngOnInit();
        expect(comp.filteredUsers).toHaveLength(2);

        comp.onSearchChange('alice');
        expect(comp.filteredUsers).toHaveLength(1);
        expect(comp.filteredUsers[0].userId).toBe('u1');

        comp.onSearchChange('');
        expect(comp.filteredUsers).toHaveLength(2);
    });

    it('filteredUsers narrows by searchTerm matching the role field (case-insensitive)', () => {
        const {comp, userService} = createComp();
        userService.getAllUsers.mockReturnValue(of([
            makeUser('u1', {firstName: 'Alice', lastName: 'Smith', email: 'alice@example.com', role: 'DESIGNER', lastLoginTime: '0'}),
            makeUser('u2', {firstName: 'Bob', lastName: 'Jones', email: 'bob@example.com', role: 'ADMIN', lastLoginTime: '0'})
        ]));
        comp.ngOnInit();
        expect(comp.filteredUsers).toHaveLength(2);

        // Search by role (lowercase term vs 'DESIGNER' role) exercises the role field in filterTerm
        comp.onSearchChange('designer');
        expect(comp.filteredUsers).toHaveLength(1);
        expect(comp.filteredUsers[0].userId).toBe('u1');
        expect(comp.filteredUsers[0].role).toBe('DESIGNER');
    });

    it('filteredUsers narrows by searchTerm when set then refresh() called directly', () => {
        const {comp, userService} = createComp();
        userService.getAllUsers.mockReturnValue(of([
            makeUser('u1', {firstName: 'Alice', lastName: 'Smith', email: 'alice@example.com', role: 'DESIGNER', lastLoginTime: '0'}),
            makeUser('u2', {firstName: 'Bob', lastName: 'Jones', email: 'bob@example.com', role: 'ADMIN', lastLoginTime: '0'})
        ]));
        comp.ngOnInit();
        comp.searchTerm = 'bob';
        (comp as any).refresh();
        expect(comp.filteredUsers).toHaveLength(1);
        expect(comp.filteredUsers[0].userId).toBe('u2');
    });

    it('sort recomputes filteredUsers order', () => {
        const {comp, userService} = createComp();
        userService.getAllUsers.mockReturnValue(of([
            makeUser('u2', {firstName: 'Bob'}),
            makeUser('u1', {firstName: 'Alice'}),
            makeUser('u3', {firstName: 'Charlie'})
        ]));
        comp.ngOnInit();
        comp.sort('firstName');
        expect(comp.filteredUsers.map((u) => u.firstName)).toEqual(['Alice', 'Bob', 'Charlie']);
        comp.sort('firstName');
        expect(comp.filteredUsers.map((u) => u.firstName)).toEqual(['Charlie', 'Bob', 'Alice']);
    });

    it('trackByUserId returns userId when present', () => {
        const {comp} = createComp();
        const user = makeUser('u1');
        expect(comp.trackByUserId(0, user)).toBe('u1');
    });

    it('trackByUserId returns index when userId is absent', () => {
        const {comp} = createComp();
        const user = makeUser('');
        user.userId = '';
        expect(comp.trackByUserId(5, user)).toBe(5);
    });

    it('detectChanges is called after ngOnInit success', () => {
        const {comp, cdr} = createComp();
        comp.ngOnInit();
        expect(cdr.detectChanges).toHaveBeenCalled();
    });

    it('lastLoginTime sorts numerically not lexicographically (default sortBy)', () => {
        // '9...' > '2...' lexicographically but 900000000000 < 2000000000000 numerically.
        // The old localeCompare implementation would put '2000000000000' before '900000000000'
        // because '2' < '9' as strings.  The fixed implementation must sort by numeric value.
        const {comp, userService} = createComp();
        userService.getAllUsers.mockReturnValue(of([
            makeUser('uA', {lastLoginTime: '900000000000'}),
            makeUser('uB', {lastLoginTime: '0'}),
            makeUser('uC', {lastLoginTime: '2000000000000'})
        ]));
        comp.ngOnInit();
        // Default sortBy is 'lastLoginTime', reverse=false → ascending numeric order
        expect(comp.filteredUsers.map((u) => u.lastLoginTime)).toEqual([
            '0', '900000000000', '2000000000000'
        ]);
    });

    it('lastLoginTime reverse sort also uses numeric order', () => {
        const {comp, userService} = createComp();
        userService.getAllUsers.mockReturnValue(of([
            makeUser('uA', {lastLoginTime: '900000000000'}),
            makeUser('uB', {lastLoginTime: '0'}),
            makeUser('uC', {lastLoginTime: '2000000000000'})
        ]));
        comp.ngOnInit();
        // Trigger reverse on lastLoginTime
        comp.sort('lastLoginTime'); // first click on same column → toggles reverse to true
        expect(comp.filteredUsers.map((u) => u.lastLoginTime)).toEqual([
            '2000000000000', '900000000000', '0'
        ]);
    });

    // --- cold-start label fix: rebuild the (translated) User ID header when the language loads ---

    it('rebuilds the User ID header title when the language JSON resolves after ngOnInit', () => {
        const {comp, translateService, languageChanged} = createComp();
        // Simulate a cold load: the language JSON is not yet loaded, so translate() returns ''.
        translateService.translate.mockReturnValue('');
        comp.ngOnInit();
        expect(comp.tableHeadersList[2].title).toBe(''); // User ID header blank on first render

        // Language JSON resolves → translate() now returns the real label.
        translateService.translate.mockReturnValue('User ID');
        languageChanged.next('en_US');

        expect(comp.tableHeadersList[2].title).toBe('User ID');
        expect(comp.tableHeadersList[2].property).toBe('userId');
    });

    it('re-runs detectChanges when the language JSON resolves after ngOnInit (cold load)', () => {
        const {comp, cdr, languageChanged} = createComp();
        comp.ngOnInit();
        cdr.detectChanges.mockClear();
        languageChanged.next('en_US');
        expect(cdr.detectChanges).toHaveBeenCalled();
    });

    it('ngOnDestroy unsubscribes from languageChangedObservable (no rebuild after destroy)', () => {
        const {comp, cdr, translateService, languageChanged} = createComp();
        comp.ngOnInit();
        comp.ngOnDestroy();
        translateService.translate.mockClear();
        cdr.detectChanges.mockClear();
        languageChanged.next('en_US');
        expect(cdr.detectChanges).not.toHaveBeenCalled();
        expect(translateService.translate).not.toHaveBeenCalled();
    });
});
