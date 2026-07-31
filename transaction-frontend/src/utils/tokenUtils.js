const TOKEN_KEY = "transaction_access_token";

export function saveToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

export function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

export function removeToken() {
    localStorage.removeItem(TOKEN_KEY);
}

export function getTokenPayload(token) {
    if (!token) {
        return null;
    }

    try {
        const parts = token.split(".");

        if (parts.length !== 3) {
            return null;
        }

        const base64Url = parts[1];
        const base64 = base64Url
            .replace(/-/g, "+")
            .replace(/_/g, "/")
            .padEnd(Math.ceil(base64Url.length / 4) * 4, "=");

        return JSON.parse(atob(base64));
    } catch {
        return null;
    }
}

export function isTokenExpired(token) {
    const payload = getTokenPayload(token);

    if (!payload?.exp) {
        return true;
    }

    return Date.now() >= payload.exp * 1000;
}

export function hasValidToken() {
    const token = getToken();

    if (!token) {
        return false;
    }

    if (isTokenExpired(token)) {
        removeToken();
        return false;
    }

    return true;
}