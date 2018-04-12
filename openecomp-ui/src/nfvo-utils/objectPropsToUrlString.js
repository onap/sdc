export default function objectPropsToUrlString(data) {
    let str = '';
    Object.keys(data).map(key => {
        if (typeof data[key] === 'object') {
            let obj = data[key];
            let arr = [];

            Object.keys(obj).map(prop => {
                if (obj[prop]) {
                    arr.push(encodeURIComponent(prop));
                }
            });
            if (arr.length) {
                str += `&${encodeURIComponent(key)}=${arr.join(',')}`;
            }
        } else if (data[key]) {
            str += `&${encodeURIComponent(key)}=${encodeURIComponent(
                data[key]
            )}`;
        }
    });
    return str;
}
