export class Enumeration {
    constructor(obj) {
        if (Array.isArray(obj)) {
            for (let key of obj) {
                this[key] = key;
            }
        } else if (typeof obj === 'object') {
            for (const key in obj) {
                this[key] = obj[key]
            }
        }
        return Object.freeze(this)
    }
    has = (key) => {
        return this.hasOwnProperty(key)
    }
}