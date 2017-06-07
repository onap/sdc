export class StringToDateFilter {

    constructor() {
        let filter = <StringToDateFilter>( (date:string) => {
            if (date) {
                return new Date(date.replace(" UTC", '').replace(" ", 'T') + '+00:00');
            }
        });
        return filter;
    }
}

