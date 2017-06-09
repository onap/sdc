export class ClearWhiteSpacesFilter {

    constructor() {
        let filter = <ClearWhiteSpacesFilter>( (text:string) => {
            if (!angular.isString(text)) {
                return text;
            }

            return text.replace(/ /g, ''); // remove also whitespaces inside
        });

        return filter;
    }
}


