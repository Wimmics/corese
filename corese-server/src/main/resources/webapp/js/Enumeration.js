/**
 * Usage:
 * e = new Enumeration("value1", "value2");
 * e.VALUE1; // === "value1"
 * e.VALUE2; // === "value2"
 */
export class Enumeration {
    constructor(obj) {
        if (Array.isArray(obj)) {
            for (let key of obj) {
                this.addEntry(key);
            }
        } else if (typeof obj === 'object') {
            for (const key in obj) {
                this.addEntry(key);
            }
        }
        return Object.freeze(this)
    }

    addEntry(key) {
        console.assert(this[key.toUpperCase()] === undefined && this[key] === undefined, `An entry conflict was detected for entry ${key}`)
        this[key.toUpperCase()] = key;
        this[key] = key;
    }

    has(key) {
        return this.hasOwnProperty(key.toUpperCase())
    }
}