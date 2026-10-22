const KEY = "chess-preferences";
const DEFAULTS = { name: "", minutes: 10, muted: false, aiLevel: null, aiColor: "White" };

export class BrowserPreferences {
    #values;

    constructor() {
        try {
            this.#values = { ...DEFAULTS, ...JSON.parse(localStorage.getItem(KEY)) };
        } catch {
            this.#values = { ...DEFAULTS };
        }
    }

    get(key) {
        return this.#values[key];
    }

    set(key, value) {
        this.#values[key] = value;
        try {
            localStorage.setItem(KEY, JSON.stringify(this.#values));
        } catch {
        }
    }
}
