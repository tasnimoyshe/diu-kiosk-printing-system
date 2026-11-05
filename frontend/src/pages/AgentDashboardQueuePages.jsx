import {
    useCallback,
    useEffect,
    useMemo,
    useState
} from "react";

import {
    Link
} from "react-router-dom";

import toast from "react-hot-toast";

import {
    Clock3,
    Copy,
    ExternalLink,
    KeyRound,
    ListChecks,
    RefreshCw,
    Search,
    Settings2,
    ShieldCheck
} from "lucide-react";

import {
    apiErrorMessage
} from "../api/http";

import {
    agentApi,
    pageContent
} from "../api/kioskApi";

import {
    useAuth
} from "../auth/AuthContext";

import {
    useRealtimeInvalidation
} from "../realtime/useRealtimeInvalidation";

import QueueTable from "../components/QueueTable";

import {
    Button,
    Card,
    EmptyState,
    LoadingScreen,
    MetricCard,
    Modal,
    Money,
    PageHeader,
    Select,
    StatusBadge,
    formatDateTime
} from "../components/ui";

export function AgentDashboardPage() {
    const [dashboard, setDashboard] =
        useState(null);

    const load =
        useCallback(
            async () => {
                try {
                    setDashboard(
                        await agentApi.dashboard()
                    );

                } catch (error) {
                    toast.error(
                        apiErrorMessage(error)
                    );
                }
            },
            []
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
            "/topic/agent-queue",
            "/topic/inventory"
        ],

        reload: load
    });

    if (!dashboard) {
        return (
            <LoadingScreen
                message="Loading agent dashboard..."
            />
        );
    }

    return (
        <div className="page-container">
            <PageHeader
                title="Agent Dashboard"
                description="Live queue information and your own operational performance."
            />

            <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                <MetricCard
                    label="Active Queue"
                    value={
                        dashboard.sharedActiveQueueCount
                    }
                    detail="Current shop queue"
                />

                <MetricCard
                    label="Completed Today"
                    value={
                        dashboard.sharedCompletedToday
                    }
                    detail="Shop-wide operational count"
                />

                <MetricCard
                    label="My Completed Today"
                    value={
                        dashboard.ownCompletedToday
                    }
                />

                <MetricCard
                    label="My Completed This Month"
                    value={
                        dashboard.ownCompletedCurrentMonth
                    }
                />
            </div>

            <div className="mt-4 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                <MetricCard
                    label="My Avg. Processing"
                    value={`${dashboard.ownAverageProcessingMinutesCurrentMonth || 0} min`}
                />

                <MetricCard
                    label="My Exceptions"
                    value={
                        dashboard.ownPausedOrFailedCurrentMonth
                    }
                />

                <MetricCard
                    label="Inventory Actions"
                    value={
                        dashboard.ownInventoryActionsCurrentMonth
                    }
                    detail="Current month"
                />

                <MetricCard
                    label="Inventory Net"
                    value={
                        <Money
                            value={
                                dashboard.ownInventoryNetAmountCurrentMonth
                            }
                        />
                    }
                    detail="Your current-month activity"
                />
            </div>
        </div>
    );
}

export function AgentQueuePage() {
    const {
        user
    } = useAuth();

    const [response, setResponse] =
        useState(null);

    const [exceptions, setExceptions] =
        useState([]);

    const [search, setSearch] =
        useState("");

    const [paymentStatus, setPaymentStatus] =
        useState("");

    const [requestStatus, setRequestStatus] =
        useState("");

    const [claiming, setClaiming] =
        useState(false);

    const [
        loadingPinRequestId,
        setLoadingPinRequestId
    ] = useState(null);

    const [
        pinDialog,
        setPinDialog
    ] = useState(null);

    const load =
        useCallback(
            async () => {
                try {
                    const [
                        queueResult,
                        exceptionResult
                    ] = await Promise.all([
                        agentApi.queue({
                            search,
                            paymentStatus,
                            requestStatus,
                            page: 0,
                            size: 100
                        }),

                        agentApi.exceptions()
                    ]);

                    setResponse(
                        queueResult
                    );

                    setExceptions(
                        Array.isArray(
                            exceptionResult
                        )
                            ? exceptionResult
                            : pageContent(
                                exceptionResult
                            )
                    );

                } catch (error) {
                    toast.error(
                        apiErrorMessage(error)
                    );
                }
            },
            [
                search,
                paymentStatus,
                requestStatus
            ]
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

    async function claimNext() {
        if (
            !window.confirm(
                "Start processing the next eligible paid request?"
            )
        ) {
            return;
        }

        setClaiming(true);

        try {
            const claimed =
                await agentApi.claimNext();

            toast.success(
                `Request ${claimed.trackingNumber} is now assigned to you.`
            );

            await load();

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );

        } finally {
            setClaiming(false);
        }
    }

    async function showPin(entry) {
        if (
            entry.paymentStatus === "PAID"
        ) {
            toast(
                "This request is already paid."
            );

            return;
        }

        setLoadingPinRequestId(
            entry.requestId
        );

        try {
            const pin =
                await agentApi.cashPin(
                    entry.requestId
                );

            setPinDialog({
                requestId:
                entry.requestId,

                fullName:
                entry.fullName,

                trackingNumber:
                entry.trackingNumber,

                pin:
                pin.pin,

                expiresAt:
                pin.expiresAt
            });

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );

        } finally {
            setLoadingPinRequestId(
                null
            );
        }
    }

    async function regeneratePin() {
        if (!pinDialog) {
            return;
        }

        const temporaryEntry = {
            requestId:
            pinDialog.requestId,

            fullName:
            pinDialog.fullName,

            trackingNumber:
            pinDialog.trackingNumber,

            paymentStatus:
                "PENDING"
        };

        await showPin(
            temporaryEntry
        );
    }

    async function copyPin() {
        if (!pinDialog?.pin) {
            return;
        }

        try {
            await navigator.clipboard
                .writeText(
                    pinDialog.pin
                );

            toast.success(
                "PIN copied."
            );

        } catch {
            toast.error(
                "Unable to copy the PIN."
            );
        }
    }

    const entries =
        pageContent(response);

    return (
        <div className="page-container">
            <PageHeader
                title="Printing Queue"
                description="Paid requests are processed first. PINs are available only inside the authenticated agent portal."
                actions={
                    <>
                        <Link
                            to="/agent/pricing"
                        >
                            <Button
                                variant="outline"
                            >
                                <Settings2 className="h-4 w-4"/>
                                Pricing
                            </Button>
                        </Link>

                        <Button
                            onClick={claimNext}
                            disabled={claiming}
                        >
                            <ListChecks className="h-4 w-4"/>

                            {claiming
                                ? "Claiming..."
                                : "Start Processing"}
                        </Button>
                    </>
                }
            />

            <div className="mb-6 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
                <div className="grid gap-3 md:grid-cols-[minmax(240px,1fr)_220px_220px]">
                    <label className="relative">
              <span className="sr-only">
                Search queue
              </span>

                        <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"/>

                        <input
                            className="form-control pl-10"
                            placeholder="Search name, ID or tracking number"
                            value={search}
                            onChange={
                                event =>
                                    setSearch(
                                        event.target.value
                                    )
                            }
                        />
                    </label>

                    <Select
                        value={paymentStatus}
                        onChange={
                            event =>
                                setPaymentStatus(
                                    event.target.value
                                )
                        }
                    >
                        <option value="">
                            All payment states
                        </option>

                        <option value="PENDING">
                            Not Paid
                        </option>

                        <option value="PAID">
                            Paid
                        </option>
                    </Select>

                    <Select
                        value={requestStatus}
                        onChange={
                            event =>
                                setRequestStatus(
                                    event.target.value
                                )
                        }
                    >
                        <option value="">
                            All request states
                        </option>

                        <option value="AWAITING_PAYMENT">
                            Awaiting Payment
                        </option>

                        <option value="PAID">
                            Paid
                        </option>

                        <option value="CLAIMED">
                            Claimed
                        </option>

                        <option value="DOWNLOADING">
                            Downloading
                        </option>

                        <option value="PRINTING">
                            Printing
                        </option>

                        <option value="FILE_ERROR">
                            File Error
                        </option>
                    </Select>
                </div>
            </div>

            {!response ? (
                <LoadingScreen
                    message="Loading current queue..."
                />
            ) : entries.length === 0 ? (
                <EmptyState
                    title="No matching requests"
                    description="Try changing the search term or filters."
                />
            ) : (
                <QueueTable
                    entries={entries}

                    trackingAction={
                        entry =>
                            entry.paymentStatus
                            === "PAID" ? (
                                <span className="inline-flex items-center gap-1 text-xs font-semibold text-emerald-700">
                            <ShieldCheck className="h-3.5 w-3.5"/>
                            Payment complete
                          </span>
                            ) : (
                                <Button
                                    variant="outline"
                                    className="min-h-8 px-2.5 py-1 text-xs"
                                    disabled={
                                        loadingPinRequestId
                                        === entry.requestId
                                    }
                                    onClick={
                                        () =>
                                            showPin(
                                                entry
                                            )
                                    }
                                >
                                    {loadingPinRequestId
                                    === entry.requestId ? (
                                        <RefreshCw className="h-3.5 w-3.5 animate-spin"/>
                                    ) : (
                                        <KeyRound className="h-3.5 w-3.5"/>
                                    )}

                                    {loadingPinRequestId
                                    === entry.requestId
                                        ? "Loading..."
                                        : "Show PIN"}
                                </Button>
                            )
                    }

                    actions={
                        entry => {
                            const assignedToMe =
                                String(
                                    entry.assignedAgentAccountId
                                    ?? ""
                                )
                                ===
                                String(
                                    user?.accountId
                                    ?? ""
                                );

                            if (assignedToMe) {
                                return (
                                    <Link
                                        to={`/agent/requests/${entry.requestId}`}
                                    >
                                        <Button
                                            variant="outline"
                                            className="w-full md:w-auto"
                                        >
                                            Open Request
                                            <ExternalLink className="h-3.5 w-3.5"/>
                                        </Button>
                                    </Link>
                                );
                            }

                            if (
                                entry.assignedAgentId
                            ) {
                                return (
                                    <span className="text-xs font-medium text-slate-500">
                            Claimed by{" "}
                                        {entry.assignedAgentId}
                          </span>
                                );
                            }

                            return (
                                <span className="text-xs font-medium text-slate-500">
                          {entry.paymentStatus
                          === "PAID"
                              ? "Ready to process"
                              : "Waiting for payment"}
                        </span>
                            );
                        }
                    }
                />
            )}

            <div className="mt-10">
                <PageHeader
                    title="Exception Queue"
                    description="Requests temporarily removed from normal processing because of a documented problem."
                />

                {exceptions.length === 0 ? (
                    <EmptyState
                        title="No current exceptions"
                        description="Requests with file or processing problems will appear here."
                    />
                ) : (
                    <div className="grid gap-4 lg:grid-cols-2">
                        {exceptions.map(
                            item => (
                                <Card
                                    key={
                                        item.exceptionId
                                    }
                                >
                                    <div className="flex items-start justify-between gap-4">
                                        <div>
                                            <h3 className="font-bold text-slate-950">
                                                {item.fullName}
                                            </h3>

                                            <p className="mt-1 font-mono text-xs text-slate-500">
                                                {item.trackingNumber}
                                            </p>
                                        </div>

                                        <StatusBadge
                                            value={
                                                item.exceptionType
                                            }
                                        />
                                    </div>

                                    <p className="mt-4 text-sm leading-6 text-slate-600">
                                        {item.reason}
                                    </p>

                                    {item.agentId && (
                                        <p className="mt-4 text-xs font-medium text-slate-500">
                                            Responsible agent:{" "}
                                            {item.agentId}
                                        </p>
                                    )}
                                </Card>
                            )
                        )}
                    </div>
                )}
            </div>

            <CashPinDialog
                data={pinDialog}
                onClose={
                    () =>
                        setPinDialog(null)
                }
                onCopy={copyPin}
                onRegenerate={
                    regeneratePin
                }
            />
        </div>
    );
}

function CashPinDialog({
                           data,
                           onClose,
                           onCopy,
                           onRegenerate
                       }) {
    const [
        remainingSeconds,
        setRemainingSeconds
    ] = useState(0);

    useEffect(
        () => {
            if (
                !data?.expiresAt
            ) {
                setRemainingSeconds(0);
                return undefined;
            }

            function update() {
                const expiry =
                    new Date(
                        data.expiresAt
                    ).getTime();

                const remaining =
                    Math.max(
                        0,
                        Math.ceil(
                            (
                                expiry
                                - Date.now()
                            )
                            / 1000
                        )
                    );

                setRemainingSeconds(
                    remaining
                );
            }

            update();

            const timer =
                window.setInterval(
                    update,
                    1000
                );

            return () =>
                window.clearInterval(
                    timer
                );
        },
        [
            data?.expiresAt
        ]
    );

    const expired =
        Boolean(data)
        && remainingSeconds <= 0;

    const countdown =
        useMemo(
            () => {
                const minutes =
                    Math.floor(
                        remainingSeconds
                        / 60
                    );

                const seconds =
                    remainingSeconds % 60;

                return `${minutes}:${String(
                    seconds
                ).padStart(2, "0")}`;
            },
            [
                remainingSeconds
            ]
        );

    return (
        <Modal
            open={Boolean(data)}
            title="Cash Payment PIN"
            description={
                data
                    ? `Request for ${data.fullName}`
                    : ""
            }
            onClose={onClose}
            footer={
                <>
                    <Button
                        variant="outline"
                        onClick={onClose}
                    >
                        Close
                    </Button>

                    <Button
                        onClick={
                            expired
                                ? onRegenerate
                                : onCopy
                        }
                    >
                        {expired ? (
                            <>
                                <RefreshCw className="h-4 w-4"/>
                                Generate New PIN
                            </>
                        ) : (
                            <>
                                <Copy className="h-4 w-4"/>
                                Copy PIN
                            </>
                        )}
                    </Button>
                </>
            }
        >
            {data && (
                <>
                    <div className="rounded-2xl border border-blue-200 bg-blue-50 px-5 py-6 text-center">
                        <p className="text-xs font-bold uppercase tracking-[0.16em] text-blue-600">
                            Tell this PIN to the customer
                        </p>

                        <div className={`
                  mt-3 font-mono text-5xl font-black tracking-[0.32em]
                  ${
                            expired
                                ? "text-slate-400"
                                : "text-blue-700"
                        }
                `}>
                            {data.pin}
                        </div>

                        <div className="mt-5 flex flex-wrap items-center justify-center gap-4 text-xs font-semibold">
                  <span className={`
                    inline-flex items-center gap-1.5
                    ${
                      expired
                          ? "text-red-700"
                          : "text-slate-600"
                  }
                  `}>
                    <Clock3 className="h-4 w-4"/>

                      {expired
                          ? "PIN expired"
                          : `Expires in ${countdown}`}
                  </span>
                        </div>
                    </div>

                    <div className="mt-5 rounded-xl border border-slate-200 bg-slate-50 p-4">
                        <p className="muted-label">
                            Tracking number
                        </p>

                        <p className="mt-1 break-all font-mono text-sm font-bold text-slate-800">
                            {data.trackingNumber}
                        </p>

                        <p className="mt-3 text-xs leading-5 text-slate-500">
                            Clicking “Show PIN” again creates a fresh
                            five-minute PIN and the previous PIN becomes invalid.
                            The customer should use the most recently displayed PIN.
                        </p>
                    </div>
                </>
            )}
        </Modal>
    );
}