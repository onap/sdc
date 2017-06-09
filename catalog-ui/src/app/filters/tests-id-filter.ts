export class TestsIdFilter {

    constructor() {
        let filter = <TestsIdFilter>( (testId:string) => {
            return testId.replace(/\s/g, '_').toLowerCase();
        });

        return filter;
    }
}

