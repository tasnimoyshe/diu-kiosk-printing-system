import {
    useCallback,
    useEffect,
    useState
} from "react";

import {
    Search
} from "lucide-react";

import {
    publicApi,
    pageContent
} from "../api/kioskApi";

import {
    apiErrorMessage
} from "../api/http";

import {
    useRealtimeInvalidation
} from "../realtime/useRealtimeInvalidation";

import QueueTable from "../components/QueueTable";

import {
    Button,
    EmptyState,
    ErrorPanel,
    Input,
    LoadingScreen,
    PageHeader
} from "../components/ui";

export default function PublicQueuePage() {
    const [search, setSearch] =
        useState("");

    const [queryText, setQueryText] =
        useState("");

    const [page, setPage] =
        useState(0);

    const [response, setResponse] =
        useState(null);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");

    const load =
        useCallback(
            async () => {
                try {
                    setError("");

                    const result =
                        await publicApi.queue({
                            search: queryText,
                            page,
                            size: 20
                        });

                    setResponse(result);

                } catch (error) {
                    setError(
                        apiErrorMessage(error)
                    );

                } finally {
                    setLoading(false);
                }
            },
            [
                queryText,
                page
            ]
        );

    useEffect(
        () => {
            setLoading(true);
            load();
        },
        [load]
    );

    useRealtimeInvalidation({
        topics: [
            "/topic/public-queue"
        ],
        reload: load
    });

    const entries =
        pageContent(response);

    function submitSearch(event) {
        event.preventDefault();

        setPage(0);
        setQueryText(
            search.trim()
        );
    }

    if (
        loading
        && !response
    ) {
        return (
            <LoadingScreen
                message="Loading current queue..."
            />
        );
    }

    return (
        <div className="page-container">
            <PageHeader
                title="Current Printing Queue"
                description="Paid requests receive priority while submission time remains the ordering rule within each payment group."
            />

            <form
                onSubmit={submitSearch}
                className="mb-6 flex flex-col gap-2 sm:flex-row"
            >
                <div className="flex-1">
                    <Input
                        value={search}
                        onChange={
                            event =>
                                setSearch(
                                    event.target.value
                                )
                        }
                        placeholder="Search by tracking number or ID"
                        aria-label="Search queue"
                    />
                </div>

                <Button type="submit">
                    <Search className="h-4 w-4"/>
                    Search
                </Button>
            </form>

            {error ? (
                <ErrorPanel
                    message={error}
                    onRetry={load}
                />
            ) : entries.length === 0 ? (
                <EmptyState
                    title="No active requests"
                    description="There are currently no matching requests in the printing queue."
                />
            ) : (
                <>
                    <QueueTable
                        entries={entries}
                    />

                    <div className="mt-6 flex justify-between">
                        <Button
                            variant="outline"
                            disabled={
                                page === 0
                            }
                            onClick={
                                () =>
                                    setPage(
                                        current =>
                                            Math.max(
                                                0,
                                                current - 1
                                            )
                                    )
                            }
                        >
                            Previous
                        </Button>

                        <span className="self-center text-sm text-slate-500">
                  Page {page + 1}
                </span>

                        <Button
                            variant="outline"
                            disabled={
                                response?.last === true
                                || entries.length < 20
                            }
                            onClick={
                                () =>
                                    setPage(
                                        current =>
                                            current + 1
                                    )
                            }
                        >
                            Next
                        </Button>
                    </div>
                </>
            )}
        </div>
    );
}