import {WorkspaceService} from '../../workspace.service';

describe('WorkspaceService - isValidForm', () => {
    it('defaults to true', () => {
        const svc = new WorkspaceService({} as any);
        expect(svc.isValidForm).toBe(true);
    });

    it('can be set by a child tab', () => {
        const svc = new WorkspaceService({} as any);
        svc.isValidForm = false;
        expect(svc.isValidForm).toBe(false);
    });
});
