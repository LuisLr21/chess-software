const KEY = "chess-session";

export class BrowserSessionStore {
    save(session) {
        try {
            sessionStorage.setItem(KEY, JSON.stringify(session));
        } catch {
        }
    }

    load() {
        try {
            return JSON.parse(sessionStorage.getItem(KEY));
        } catch {
            return null;
        }
    }

    clear() {
        try {
            sessionStorage.removeItem(KEY);
        } catch {
        }
    }
}
