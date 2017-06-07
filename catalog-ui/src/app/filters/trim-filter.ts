export class TrimFilter {

    constructor() {
        let filter = <TrimFilter>( (text:string) => {
            if (!angular.isString(text)) {
                return text;
            }

            return text.replace(/^\s+|\s+$/g, ''); // you could use .trim, but it's not going to work in IE<9
        });

        return filter;
    }
}

