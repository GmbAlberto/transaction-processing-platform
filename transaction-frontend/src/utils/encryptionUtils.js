const AES_SECRET_KEY = import.meta.env.VITE_AES_SECRET_KEY;

const IV_LENGTH = 12;

function base64ToUint8Array(base64) {
    const binaryString = window.atob(base64);

    return Uint8Array.from(
        binaryString,
        (character) => character.charCodeAt(0)
    );
}

function uint8ArrayToBase64(bytes) {
    let binaryString = "";

    bytes.forEach((byte) => {
        binaryString += String.fromCharCode(byte);
    });

    return window.btoa(binaryString);
}

async function importAesKey() {
    if (!AES_SECRET_KEY) {
        throw new Error(
            "No se configuró la variable VITE_AES_SECRET_KEY."
        );
    }

    const rawKey = base64ToUint8Array(AES_SECRET_KEY);

    if (rawKey.length !== 32) {
        throw new Error(
            "La llave AES debe contener exactamente 32 bytes para AES-256."
        );
    }

    return window.crypto.subtle.importKey(
        "raw",
        rawKey,
        {
            name: "AES-GCM",
        },
        false,
        ["encrypt"]
    );
}

export async function encryptSecret(secret) {
    if (!secret?.trim()) {
        throw new Error("El secreto no puede estar vacío.");
    }

    const key = await importAesKey();

    const iv = window.crypto.getRandomValues(
        new Uint8Array(IV_LENGTH)
    );

    const encodedSecret = new TextEncoder().encode(secret);

    const encryptedBuffer = await window.crypto.subtle.encrypt({
        name: "AES-GCM",
        iv,
        tagLength: 128,
    },
        key,
        encodedSecret
    );

    const encryptedBytes = new Uint8Array(encryptedBuffer);

    const result = new Uint8Array(iv.length + encryptedBytes.length);

    result.set(iv, 0);
    result.set(encryptedBytes, iv.length);

    return uint8ArrayToBase64(result);
}