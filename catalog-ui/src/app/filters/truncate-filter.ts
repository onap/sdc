export class TruncateFilter {
    constructor() {
        let filter = <TruncateFilter>(str:string, length:number) => {
            if (str.length <= length) {
                return str;
            }

            //if(str[length - 1] === ' '){
            //    return str.substring(0, length - 1) + '...';
            //}

            let char;
            let index = length;
            while (char !== ' ' && index !== 0) {
                index--;
                char = str[index];
            }
            if (index === 0) {
                return (index === 0) ? str : str.substring(0, length - 3) + '...';
            }
            return (index === 0) ? str : str.substring(0, index) + '...';
        };
        return filter;
    }

}
