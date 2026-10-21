export class HttpError extends Error {
    constructor(status, message) {
        super(message);
        this.status = status;
    }
}

export async function request(baseUrl, method, path, body, headers = {}) {
    const response = await fetch(baseUrl + path, {
        method,
        headers: body ? { "Content-Type": "application/json", ...headers } : headers,
        body: body ? JSON.stringify(body) : undefined
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new HttpError(response.status, data.error ?? `HTTP ${response.status}`);
    return data;
}
