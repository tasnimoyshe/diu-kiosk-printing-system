import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState
} from "react";

import {
    authApi
} from "../api/kioskApi";

import {
    tryRestoreSession
} from "../api/http";

const AuthContext =
    createContext(null);

export function AuthProvider({
                                 children
                             }) {
    const [user, setUser] =
        useState(null);

    const [loading, setLoading] =
        useState(true);

    const refreshUser =
        useCallback(
            async () => {
                try {
                    /*
                     * First check whether the current access-token cookie
                     * is already valid.
                     *
                     * skipAuthRefresh prevents the Axios interceptor from
                     * independently starting another refresh attempt.
                     */
                    const current =
                        await authApi.me({
                            skipAuthRefresh: true
                        });

                    setUser(current);

                    return current;

                } catch (error) {
                    const status =
                        error?.response?.status;

                    if (status !== 401) {
                        setUser(null);

                        return null;
                    }

                    /*
                     * A 401 may simply mean the short-lived access token
                     * expired. Try the persistent refresh-token cookie once.
                     *
                     * Missing/expired refresh cookies are intentionally silent
                     * because that is the normal logged-out state.
                     */
                    try {
                        const restored =
                            await tryRestoreSession();

                        if (!restored) {
                            setUser(null);

                            return null;
                        }

                        const current =
                            await authApi.me({
                                skipAuthRefresh: true
                            });

                        setUser(current);

                        return current;

                    } catch {
                        setUser(null);

                        return null;
                    }

                } finally {
                    setLoading(false);
                }
            },
            []
        );

    useEffect(
        () => {
            refreshUser();
        },
        [refreshUser]
    );

    const login =
        useCallback(
            async credentials => {
                await authApi.login(
                    credentials
                );

                return refreshUser();
            },
            [refreshUser]
        );

    const logout =
        useCallback(
            async () => {
                try {
                    await authApi.logout();

                } finally {
                    setUser(null);
                }
            },
            []
        );

    const value =
        useMemo(
            () => ({
                user,
                loading,

                authenticated:
                    Boolean(user),

                login,
                logout,
                refreshUser
            }),
            [
                user,
                loading,
                login,
                logout,
                refreshUser
            ]
        );

    return (
        <AuthContext.Provider
            value={value}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context =
        useContext(
            AuthContext
        );

    if (!context) {
        throw new Error(
            "useAuth must be used inside AuthProvider"
        );
    }

    return context;
}