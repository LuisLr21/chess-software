export async function request(baseUrl, method, path, body, headers = {}) {
    const response = await fetch(baseUrl + path, {
        method,
        headers: body ? { "Content-Type": "application/json", ...headers } : headers,
        body: body ? JSON.stringify(body) : undefined
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error);
    return data;
}
