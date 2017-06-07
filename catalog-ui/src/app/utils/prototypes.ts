interface String {
    format(variables:Array<string>):string
}

interface Array<T> {
    clean(o:T):Array<T>;
}


/**
 * This function will replace the %<number> with strings (from array).
 * Example: "Requested '%1' resource was not found.".format(["MyResource"]);
 * Note: in case the array contains empty string the function will also remove the '' or the "".
 */
if (!String.hasOwnProperty("format")) {
    String.prototype["format"] = function (variables:Array<string>):string {

        if (variables === null || variables === undefined || variables.length === 0) {
            variables = [''];
        }

        for (let i = 0; i < variables.length; i++) {
            if (variables[i] === '' || variables[i] === null) {
                variables[i] = '--DELETE--';
            }
        }

        let res = this.replace(/%(\d+)/g, function (_, m) {
            return variables[--m];
        });

        res = res.replace(" '--DELETE--' ", " ");
        res = res.replace(" \"--DELETE--\" ", " ");
        res = res.replace("'--DELETE--'", "");
        res = res.replace("\"--DELETE--\"", "");
        res = res.replace("--DELETE--", "");

        return res;
    };
}

if (!String.hasOwnProperty("capitalizeFirstLetter")) {
    String.prototype["capitalizeFirstLetter"] = function () {
        return this.charAt(0).toUpperCase() + this.slice(1);
    };
}

if (!String.hasOwnProperty("replaceAll")) {
    String.prototype["replaceAll"] = function (find:string, replace:string):string {
        return this.replace(new RegExp(find, 'g'), replace);
    };
}

if (!Array.hasOwnProperty("clean")) {
    Array.prototype.clean = function (deleteValue) {
        for (let i = 0; i < this.length; i++) {
            if (this[i] == deleteValue) {
                this.splice(i, 1);
                i--;
            }
        }
        return this;
    };
}
