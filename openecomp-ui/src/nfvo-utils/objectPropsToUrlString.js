export default function objectPropsToUrlString(data) {
    let str = '';
    Object.keys(data).map(key => {
        if (typeof data[key] === 'object') {
            let obj = data[key];
            let arr = [];

            Object.keys(obj).map(prop => {
                if (obj[prop]) {
                    arr.push(prop);
                }
            });
            if (arr.length) {
                str += `&${key}=${arr.join(',')}`;
            }
        } else if (data[key]) {
            str += `&${key}=${data[key]}`;
        }
    });
    return str;
}
