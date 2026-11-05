import {
    useCallback,
    useEffect,
    useState
} from "react";

import {
    useNavigate,
    useParams
} from "react-router-dom";

import toast from "react-hot-toast";

import {
    Download,
    Printer
} from "lucide-react";

import {
    apiErrorMessage
} from "../api/http";

import {
    agentApi
} from "../api/kioskApi";

import {
    useRealtimeInvalidation
} from "../realtime/useRealtimeInvalidation";

import {
    Button,
    Card,
    ErrorPanel,
    LoadingScreen,
    Money,
    PageHeader,
    StatusBadge,
    formatDateTime
} from "../components/ui";

const EXCEPTION_TYPES = [
    "CORRUPTED_FILE",
    "CONVERSION_FAILURE",
    "MISSING_INSTRUCTION",
    "PRINTER_INCOMPATIBILITY",
    "UNREADABLE_IMAGE",
    "PAYMENT_DISPUTE",
    "MISSING_FILE",
    "OTHER"
];

export default function AgentRequestPage() {
    const {
        requestId
    } = useParams();

    const navigate =
        useNavigate();

    const [request, setRequest] =
        useState(null);

    const [cashPin, setCashPin] =
        useState(null);

    const [error, setError] =
        useState("");

    const load =
        useCallback(
            async () => {
                try {
                    setRequest(
                        await agentApi.request(
                            requestId
                        )
                    );

                    setError("");

                } catch (error) {
                    setError(
                        apiErrorMessage(error)
                    );
                }
            },
            [requestId]
        );

    useEffect(
        () => {
            load();
        },
        [load]
    );

    useRealtimeInvalidation({
        privateChannel: true,

        topics: [
            "/topic/agent-queue"
        ],

        reload: load
    });

    async function perform(
        functionCall,
        successMessage
    ) {
        try {
            await functionCall();

            toast.success(
                successMessage
            );

            await load();

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );
        }
    }

    async function generatePin() {
        try {
            setCashPin(
                await agentApi.cashPin(
                    requestId
                )
            );

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );
        }
    }

    async function download(file) {
        try {
            const response =
                await agentApi.downloadFile(
                    requestId,
                    file.id
                );

            const url =
                URL.createObjectURL(
                    response.data
                );

            const link =
                document.createElement("a");

            link.href = url;

            link.download =
                file.originalFileName
                || `file-${file.id}`;

            document.body.appendChild(
                link
            );

            link.click();
            link.remove();

            URL.revokeObjectURL(url);

            toast.success(
                `${file.originalFileName} downloaded.`
            );

            await load();

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );
        }
    }

    function reasonPrompt(message) {
        const value =
            window.prompt(message);

        if (
            value === null
            || !value.trim()
        ) {
            return null;
        }

        return value.trim();
    }

    async function pause() {
        const reason =
            reasonPrompt(
                "Enter the reason for pausing this request:"
            );

        if (!reason) {
            return;
        }

        await perform(
            () =>
                agentApi.pause(
                    requestId,
                    reason
                ),
            "Request paused."
        );
    }

    async function release() {
        const reason =
            reasonPrompt(
                "Enter the reason for releasing this claim:"
            );

        if (!reason) {
            return;
        }

        await perform(
            () =>
                agentApi.release(
                    requestId,
                    reason
                ),
            "Claim released."
        );

        navigate(
            "/agent/queue"
        );
    }

    async function cancel() {
        const reason =
            reasonPrompt(
                "Enter the cancellation reason:"
            );

        if (!reason) {
            return;
        }

        if (
            !window.confirm(
                "Confirm cancellation of this request?"
            )
        ) {
            return;
        }

        await perform(
            () =>
                agentApi.cancel(
                    requestId,
                    reason
                ),
            "Request cancelled."
        );
    }

    async function reopen() {
        const reason =
            reasonPrompt(
                "Enter the reason for reopening this request:"
            );

        if (!reason) {
            return;
        }

        await perform(
            () =>
                agentApi.reopen(
                    requestId,
                    reason
                ),
            "Request reopened."
        );
    }

    async function reportProblem() {
        const selected =
            window.prompt(
                `Problem type:\n${EXCEPTION_TYPES.join("\n")}`,
                "OTHER"
            );

        if (
            !selected
            || !EXCEPTION_TYPES.includes(
                selected.trim().toUpperCase()
            )
        ) {
            toast.error(
                "Enter a valid problem type."
            );
            return;
        }

        const reason =
            reasonPrompt(
                "Describe the file or printing problem:"
            );

        if (!reason) {
            return;
        }

        await perform(
            () =>
                agentApi.reportProblem(
                    requestId,
                    selected
                        .trim()
                        .toUpperCase(),
                    reason
                ),
            "Problem reported."
        );
    }

    if (
        !request
        && !error
    ) {
        return (
            <LoadingScreen
                message="Loading request details..."
            />
        );
    }

    if (error) {
        return (
            <div className="page-container">
                <ErrorPanel
                    message={error}
                    onRetry={load}
                />
            </div>
        );
    }

    return (
        <div className="page-container">
            <PageHeader
                title="Print Request Details"
                description={request.trackingNumber}
                actions={
                    <Button
                        variant="outline"
                        onClick={
                            () =>
                                navigate(
                                    "/agent/queue"
                                )
                        }
                    >
                        Back to Queue
                    </Button>
                }
            />

            <Card>
                <div className="flex flex-wrap items-start justify-between gap-4">
                    <div>
                        <h2 className="text-xl font-bold">
                            {request.fullName}
                        </h2>

                        <p className="mt-1 text-sm text-slate-500">
                            {request.registrationNumber}
                            {" · "}
                            {request.userType}
                            {request.department
                                ? ` · ${request.department}`
                                : ""}
                        </p>
                    </div>

                    <div className="flex flex-wrap gap-2">
                        <StatusBadge
                            value={request.paymentStatus}
                        />

                        <StatusBadge
                            value={request.requestStatus}
                        />
                    </div>
                </div>

                <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                    <Info
                        label="Print color"
                        value={request.printColor}
                    />

                    <Info
                        label="Printing side"
                        value={request.printingSide}
                    />

                    <Info
                        label="Copies"
                        value={request.copies}
                    />

                    <Info
                        label="Document pages"
                        value={request.totalDocumentPages}
                    />

                    <Info
                        label="Physical sheets"
                        value={request.totalPhysicalSheets}
                    />

                    <Info
                        label="Applied rate"
                        value={
                            <Money
                                value={request.appliedRate}
                            />
                        }
                    />

                    <Info
                        label="Total amount"
                        value={
                            <Money
                                value={request.totalAmount}
                            />
                        }
                    />

                    <Info
                        label="Claimed"
                        value={formatDateTime(
                            request.claimedAt
                        )}
                    />
                </div>
            </Card>

            {request.paymentStatus !== "PAID" && (
                <Card className="mt-6 border-amber-200 bg-amber-50">
                    <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
                        <div>
                            <h2 className="font-bold">
                                Cash Payment PIN
                            </h2>

                            <p className="mt-1 text-sm text-slate-600">
                                Generate or display the active PIN and tell it to the customer.
                            </p>
                        </div>

                        <Button
                            onClick={generatePin}
                        >
                            Generate / Show PIN
                        </Button>
                    </div>

                    {cashPin && (
                        <div className="mt-5 rounded-2xl bg-white p-5 text-center">
                            <div className="font-mono text-4xl font-black tracking-[0.35em] text-blue-700">
                                {cashPin.pin}
                            </div>

                            <p className="mt-2 text-xs text-slate-500">
                                Expires:{" "}
                                {formatDateTime(
                                    cashPin.expiresAt
                                )}
                            </p>
                        </div>
                    )}
                </Card>
            )}

            <div className="mt-6">
                <h2 className="mb-4 text-xl font-bold">
                    Documents
                </h2>

                <div className="grid gap-4 lg:grid-cols-2">
                    {(request.files || [])
                        .map(
                            file => (
                                <Card key={file.id}>
                                    <div className="flex items-start justify-between gap-4">
                                        <div className="min-w-0">
                                            <h3 className="truncate font-semibold">
                                                {file.originalFileName}
                                            </h3>

                                            <p className="mt-1 text-xs text-slate-500">
                                                {file.fileType}
                                                {" · "}
                                                {file.pageCount} page(s)
                                                {" · "}
                                                {file.totalPhysicalSheets} sheet(s)
                                            </p>
                                        </div>

                                        <Money
                                            value={file.subtotal}
                                        />
                                    </div>

                                    <Button
                                        className="mt-4"
                                        variant="outline"
                                        disabled={
                                            !file.binaryAvailable
                                        }
                                        onClick={
                                            () =>
                                                download(file)
                                        }
                                    >
                                        <Download className="h-4 w-4"/>

                                        {file.binaryAvailable
                                            ? "Download"
                                            : "File Deleted"}
                                    </Button>
                                </Card>
                            )
                        )}
                </div>
            </div>

            <Card className="mt-6">
                <h2 className="font-bold">
                    Processing Actions
                </h2>

                <div className="mt-4 flex flex-wrap gap-2">
                    {[
                        "CLAIMED",
                        "DOWNLOADING"
                    ].includes(
                        request.requestStatus
                    ) && (
                        <Button
                            variant="success"
                            onClick={
                                () =>
                                    perform(
                                        () =>
                                            agentApi.startPrinting(
                                                requestId
                                            ),
                                        "Printing started."
                                    )
                            }
                        >
                            <Printer className="h-4 w-4"/>
                            Start Printing
                        </Button>
                    )}

                    {request.requestStatus
                        === "PRINTING" && (
                            <Button
                                variant="success"
                                onClick={
                                    async () => {
                                        if (
                                            !window.confirm(
                                                "Confirm that all documents have been printed successfully?"
                                            )
                                        ) {
                                            return;
                                        }

                                        await perform(
                                            () =>
                                                agentApi.complete(
                                                    requestId
                                                ),
                                            "Printing completed."
                                        );
                                    }
                                }
                            >
                                Done
                            </Button>
                        )}

                    {[
                        "CLAIMED",
                        "DOWNLOADING",
                        "PRINTING"
                    ].includes(
                        request.requestStatus
                    ) && (
                        <>
                            <Button
                                variant="warning"
                                onClick={pause}
                            >
                                Pause
                            </Button>

                            <Button
                                variant="danger"
                                onClick={reportProblem}
                            >
                                Report Problem
                            </Button>

                            {request.requestStatus
                                !== "PRINTING" && (
                                    <Button
                                        variant="outline"
                                        onClick={release}
                                    >
                                        Release Claim
                                    </Button>
                                )}

                            <Button
                                variant="danger"
                                onClick={cancel}
                            >
                                Cancel
                            </Button>
                        </>
                    )}

                    {[
                        "COMPLETED",
                        "FILE_ERROR",
                        "PAUSED",
                        "CANCELLED"
                    ].includes(
                        request.requestStatus
                    ) && (
                        <Button
                            variant="outline"
                            onClick={reopen}
                        >
                            Reopen
                        </Button>
                    )}
                </div>
            </Card>
        </div>
    );
}

function Info({
                  label,
                  value
              }) {
    return (
        <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                {label}
            </p>

            <div className="mt-1 text-sm font-semibold">
                {value ?? "—"}
            </div>
        </div>
    );
}