import {
    useEffect,
    useRef
} from "react";

import {
    Client
} from "@stomp/stompjs";

function websocketUrl(path) {
    const protocol =
        window.location.protocol === "https:"
            ? "wss:"
            : "ws:";

    return `${protocol}//${window.location.host}${path}`;
}

export function useRealtimeInvalidation({
                                            enabled = true,
                                            privateChannel = false,
                                            topics = [],
                                            reload
                                        }) {
    const reloadRef =
        useRef(reload);

    reloadRef.current =
        reload;

    const topicKey =
        topics.join("|");

    useEffect(
        () => {
            if (
                !enabled
                || topics.length === 0
            ) {
                return undefined;
            }

            let pollingId = null;

            const interval =
                Number(
                    import.meta.env
                        .VITE_POLL_INTERVAL_MS
                    || 12000
                );

            const startPolling =
                () => {
                    if (pollingId) {
                        return;
                    }

                    pollingId =
                        window.setInterval(
                            () => {
                                reloadRef.current?.();
                            },
                            interval
                        );
                };

            const stopPolling =
                () => {
                    if (!pollingId) {
                        return;
                    }

                    window.clearInterval(
                        pollingId
                    );

                    pollingId = null;
                };

            startPolling();

            const path =
                privateChannel
                    ? (
                        import.meta.env
                            .VITE_WS_PRIVATE_PATH
                        || "/ws/private"
                    )
                    : (
                        import.meta.env
                            .VITE_WS_PUBLIC_PATH
                        || "/ws/public"
                    );

            const client =
                new Client({
                    brokerURL:
                        websocketUrl(path),

                    reconnectDelay:
                        5000,

                    heartbeatIncoming:
                        10000,

                    heartbeatOutgoing:
                        10000,

                    onConnect: () => {
                        stopPolling();

                        topics.forEach(
                            topic => {
                                client.subscribe(
                                    topic,
                                    () => {
                                        reloadRef.current?.();
                                    }
                                );
                            }
                        );
                    },

                    onWebSocketClose:
                    startPolling,

                    onWebSocketError:
                    startPolling,

                    onStompError:
                    startPolling
                });

            client.activate();

            return () => {
                stopPolling();

                client.deactivate();
            };
        },
        [
            enabled,
            privateChannel,
            topicKey
        ]
    );
}