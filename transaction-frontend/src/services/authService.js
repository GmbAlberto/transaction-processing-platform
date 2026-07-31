import api from "./api";
import { getToken, getTokenPayload, removeToken, saveToken } from "../utils/tokenUtils";

export async function login(username, password) {
    const response = await api.post("/auth/login", {
        username,
        password,
    });

    const authenticationData = response.data?.data;
    const token = authenticationData?.token;

    if (!token) {
        throw new Error("El servidor no devolvió un token de acceso");
    }

    saveToken(token);

    return authenticationData;
}

export function logout() {
    removeToken();
}

export function getAuthenticatedUsername() {
    const payload = getTokenPayload(getToken());

    return payload?.sub ?? null;
}