import axios from "axios";

const baseURL =
    import.meta.env.VITE_API_BASE_URL || "";

export const http = axios.create({
    baseURL,
    withCredentials: true
});

/*
 * This Axios instance intentionally has no response interceptor.
 * It is used for CSRF acquisition and silent refresh operations so
 * an authentication refresh cannot recursively trigger another refresh.
 */
const authTransport = axios.create({
    baseURL,
    withCredentials: true
});

let refreshPromise = null;

function readCookie(name) {
    const prefix = `${name}=`;

    const entry = document.cookie
        .split("; ")
        .find(
            item =>
                item.startsWith(prefix)
        );

    if (!entry) {
        return "";
    }

    return decodeURIComponent(
        entry.substring(prefix.length)
    );
}

export async function ensureCsrf() {
    await authTransport.get(
        "/api/auth/csrf"
    );

    return readCookie(
        "XSRF-TOKEN"
    );
}

/*
 * Attempts to restore authentication without exposing an expected
 * "refresh token cookie is missing" response to the UI.
 *
 * Returns:
 * true  -> refresh succeeded
 * false -> there is no usable refresh session
 *
 * Unexpected network/server errors are thrown normally.
 */
export async function tryRestoreSession() {
    if (refreshPromise) {
        return refreshPromise;
    }

    refreshPromise = (async () => {
        try {
            await authTransport.get(
                "/api/auth/csrf"
            );

            const csrfToken =
                readCookie(
                    "XSRF-TOKEN"
                );

            if (!csrfToken) {
                return false;
            }

            await authTransport.post(
                "/api/auth/refresh",
                null,
                {
                    headers: {
                        "X-XSRF-TOKEN":
                        csrfToken
                    }
                }
            );

            return true;

        } catch (error) {
            const status =
                error?.response?.status;

            /*
             * These are normal when the browser has no refresh cookie,
             * the refresh token expired, or it was revoked.
             */
            if (
                status === 400
                || status === 401
                || status === 403
            ) {
                return false;
            }

            throw error;
        }
    })();

    try {
        return await refreshPromise;

    } finally {
        refreshPromise = null;
    }
}

export async function mutate(
    method,
    url,
    data,
    config = {}
) {
    const csrfToken =
        await ensureCsrf();

    return http.request({
        ...config,

        method,
        url,
        data,

        headers: {
            ...(config.headers || {}),

            "X-XSRF-TOKEN":
            csrfToken
        }
    });
}

http.interceptors.response.use(
    response => response,

    async error => {
        const originalRequest =
            error.config || {};

        const url =
            String(
                originalRequest.url || ""
            );

        const status =
            error?.response?.status;

        const authenticationEndpoint =
            url.includes(
                "/api/auth/login"
            )
            || url.includes(
                "/api/auth/refresh"
            )
            || url.includes(
                "/api/auth/csrf"
            );

        const shouldAttemptRefresh =
            status === 401
            && !originalRequest._retried
            && !originalRequest.skipAuthRefresh
            && !authenticationEndpoint;

        if (!shouldAttemptRefresh) {
            return Promise.reject(error);
        }

        originalRequest._retried = true;

        try {
            const restored =
                await tryRestoreSession();

            if (restored) {
                return http(
                    originalRequest
                );
            }

        } catch {
            /*
             * The original 401 is more useful to the caller than exposing
             * a background refresh failure.
             */
        }

        return Promise.reject(error);
    }
);

export function apiErrorMessage(
    error,
    fallback =
    "Something went wrong. Please try again."
) {
    const response =
        error?.response?.data;

    if (
        response?.fieldErrors
        && typeof response.fieldErrors === "object"
    ) {
        const firstFieldError =
            Object.values(
                response.fieldErrors
            )[0];

        if (firstFieldError) {
            return String(
                firstFieldError
            );
        }
    }

    return (
        response?.message
        || error?.message
        || fallback
    );
}