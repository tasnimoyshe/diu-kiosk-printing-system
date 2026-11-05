import {
    useCallback,
    useEffect,
    useState
} from "react";

import toast from "react-hot-toast";

import {
    Pie,
    PieChart,
    ResponsiveContainer,
    Tooltip
} from "recharts";

import {
    apiErrorMessage
} from "../api/http";

import {
    adminApi,
    pageContent,
    publicApi
} from "../api/kioskApi";

import {
    useRealtimeInvalidation
} from "../realtime/useRealtimeInvalidation";

import QueueTable from "../components/QueueTable";

import {
    Button,
    Card,
    EmptyState,
    ErrorPanel,
    LoadingScreen,
    MetricCard,
    Money,
    PageHeader,
    StatusBadge,
    formatDateTime
} from "../components/ui";

export function AdminDashboardPage() {
    const [dashboard, setDashboard] =
        useState(null);

    const [error, setError] =
        useState("");

    const load =
        useCallback(
            async () => {
                try {
                    const result =
                        await adminApi.dashboard();

                    setDashboard(result);
                    setError("");

                } catch (error) {
                    setError(
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
            "/topic/admin-dashboard"
        ],

        reload: load
    });

    if (
        !dashboard
        && !error
    ) {
        return (
            <LoadingScreen
                message="Loading admin dashboard..."
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

    const printStats =
        dashboard.todayPrintStatistics
        || {};

    const fileChart = [
        {
            name: "PDF",
            value: printStats.pdfFiles || 0
        },
        {
            name: "DOC / DOCX",
            value: printStats.documentFiles || 0
        },
        {
            name: "Images",
            value: printStats.imageFiles || 0
        }
    ];

    return (
        <div className="page-container">
            <PageHeader
                title="Admin Dashboard"
                description="Printing, revenue and agent-performance overview."
            />

            <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                <MetricCard
                    label="In Queue"
                    value={dashboard.activeQueueCount}
                />

                <MetricCard
                    label="Completed Today"
                    value={dashboard.completedToday}
                />

                <MetricCard
                    label="Today's Printed Pages"
                    value={printStats.totalDocumentPages}
                    detail={`${printStats.totalPhysicalSheets || 0} physical sheets`}
                />

                <MetricCard
                    label="Today's Printing Revenue"
                    value={
                        <Money
                            value={dashboard.printingRevenue?.today}
                        />
                    }
                />
            </div>

            <div className="mt-6 grid gap-6 lg:grid-cols-2">
                <Card>
                    <h2 className="font-bold">
                        Today's File Types
                    </h2>

                    <div className="mt-4 h-72">
                        <ResponsiveContainer
                            width="100%"
                            height="100%"
                        >
                            <PieChart>
                                <Pie
                                    data={fileChart}
                                    dataKey="value"
                                    nameKey="name"
                                    cx="50%"
                                    cy="50%"
                                    outerRadius={95}
                                    fill="#2563eb"
                                    label
                                />

                                <Tooltip/>
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                </Card>

                <Card>
                    <h2 className="font-bold">
                        Revenue
                    </h2>

                    <div className="mt-5 space-y-5">
                        <RevenueRow
                            label="Printing · Today"
                            value={dashboard.printingRevenue?.today}
                        />

                        <RevenueRow
                            label="Printing · This Week"
                            value={dashboard.printingRevenue?.currentWeek}
                        />

                        <RevenueRow
                            label="Printing · This Month"
                            value={dashboard.printingRevenue?.currentMonth}
                        />

                        <div className="border-t border-slate-200"/>

                        <RevenueRow
                            label="Inventory · Today"
                            value={dashboard.inventoryRevenue?.today}
                        />

                        <RevenueRow
                            label="Inventory · This Week"
                            value={dashboard.inventoryRevenue?.currentWeek}
                        />

                        <RevenueRow
                            label="Inventory · This Month"
                            value={dashboard.inventoryRevenue?.currentMonth}
                        />
                    </div>
                </Card>
            </div>

            <Card className="mt-6">
                <h2 className="font-bold">
                    This Month's Agent Performance
                </h2>

                <div className="mt-4 overflow-x-auto">
                    <table className="min-w-full text-sm">
                        <thead>
                        <tr className="border-b border-slate-200 text-left text-xs uppercase text-slate-500">
                            <th className="py-3 pr-4">
                                Agent
                            </th>

                            <th className="py-3 pr-4">
                                Completed
                            </th>

                            <th className="py-3 pr-4">
                                Avg. Processing
                            </th>

                            <th className="py-3 pr-4">
                                Exceptions
                            </th>

                            <th className="py-3">
                                Inventory Actions
                            </th>
                        </tr>
                        </thead>

                        <tbody>
                        {(dashboard.currentMonthAgentPerformance || [])
                            .map(
                                agent => (
                                    <tr
                                        key={agent.agentAccountId}
                                        className="border-b border-slate-100"
                                    >
                                        <td className="py-3 pr-4">
                                            <strong>
                                                {agent.fullName}
                                            </strong>

                                            <div className="text-xs text-slate-500">
                                                {agent.agentId}
                                            </div>
                                        </td>

                                        <td className="py-3 pr-4">
                                            {agent.completedJobs}
                                        </td>

                                        <td className="py-3 pr-4">
                                            {agent.averageProcessingMinutes}
                                            {" min"}
                                        </td>

                                        <td className="py-3 pr-4">
                                            {agent.pausedOrFailedRequests}
                                        </td>

                                        <td className="py-3">
                                            {agent.inventoryActionCount}
                                        </td>
                                    </tr>
                                )
                            )}
                        </tbody>
                    </table>
                </div>
            </Card>
        </div>
    );
}

export function PendingAgentsPage() {
    const [agents, setAgents] =
        useState(null);

    const load =
        useCallback(
            async () => {
                try {
                    setAgents(
                        await adminApi.pendingAgents()
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
            "/topic/admin-agents"
        ],

        reload: load
    });

    async function action(
        agent,
        type
    ) {
        const accountId =
            agent.accountId
            ?? agent.id;

        if (
            !window.confirm(
                `${type === "approve" ? "Approve" : "Reject"} ${agent.fullName}?`
            )
        ) {
            return;
        }

        try {
            if (type === "approve") {
                await adminApi.approveAgent(
                    accountId
                );
            } else {
                await adminApi.rejectAgent(
                    accountId
                );
            }

            toast.success(
                type === "approve"
                    ? "Agent approved."
                    : "Agent rejected."
            );

            await load();

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );
        }
    }

    if (!agents) {
        return (
            <LoadingScreen
                message="Loading pending agents..."
            />
        );
    }

    return (
        <div className="page-container">
            <PageHeader
                title="Confirm Agents"
                description="Review pending registrations and approve or reject access."
            />

            {agents.length === 0 ? (
                <EmptyState
                    title="No pending agents"
                    description="All agent registrations have been reviewed."
                />
            ) : (
                <div className="grid gap-4 lg:grid-cols-2">
                    {agents.map(
                        agent => (
                            <Card
                                key={
                                    agent.accountId
                                    ?? agent.id
                                }
                            >
                                <div className="flex items-start justify-between gap-3">
                                    <div>
                                        <h2 className="font-bold">
                                            {agent.fullName}
                                        </h2>

                                        <p className="text-sm text-slate-500">
                                            {agent.agentId}
                                        </p>
                                    </div>

                                    <StatusBadge
                                        value={agent.agentStatus}
                                    />
                                </div>

                                <dl className="mt-5 space-y-2 text-sm">
                                    <Row
                                        label="Email"
                                        value={agent.email}
                                    />

                                    <Row
                                        label="Phone"
                                        value={agent.contactNumber}
                                    />

                                    <Row
                                        label="ID card"
                                        value={
                                            agent.idCardAvailable
                                                ? "Uploaded"
                                                : "Unavailable"
                                        }
                                    />

                                    <Row
                                        label="Registered"
                                        value={formatDateTime(
                                            agent.createdAt
                                        )}
                                    />
                                </dl>

                                <div className="mt-5 flex gap-2">
                                    <Button
                                        variant="success"
                                        onClick={
                                            () =>
                                                action(
                                                    agent,
                                                    "approve"
                                                )
                                        }
                                    >
                                        Approve
                                    </Button>

                                    <Button
                                        variant="danger"
                                        onClick={
                                            () =>
                                                action(
                                                    agent,
                                                    "reject"
                                                )
                                        }
                                    >
                                        Reject
                                    </Button>
                                </div>
                            </Card>
                        )
                    )}
                </div>
            )}
        </div>
    );
}

export function AdminQueuePage() {
    const [response, setResponse] =
        useState(null);

    const [search, setSearch] =
        useState("");

    const load =
        useCallback(
            async () => {
                try {
                    setResponse(
                        await publicApi.queue({
                            search,
                            page: 0,
                            size: 100
                        })
                    );
                } catch (error) {
                    toast.error(
                        apiErrorMessage(error)
                    );
                }
            },
            [search]
        );

    useEffect(
        () => {
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

    return (
        <div className="page-container">
            <PageHeader
                title="Admin Queue"
                description="Read-only operational view of the active printing queue."
            />

            <div className="mb-5 max-w-lg">
                <input
                    className="form-control"
                    placeholder="Search queue"
                    value={search}
                    onChange={
                        event =>
                            setSearch(
                                event.target.value
                            )
                    }
                />
            </div>

            {!response ? (
                <LoadingScreen/>
            ) : entries.length === 0 ? (
                <EmptyState
                    title="Queue is empty"
                />
            ) : (
                <QueueTable
                    entries={entries}
                />
            )}
        </div>
    );
}

function RevenueRow({
                        label,
                        value
                    }) {
    return (
        <div className="flex items-center justify-between gap-4">
        <span className="text-sm text-slate-500">
          {label}
        </span>

            <strong>
                <Money value={value}/>
            </strong>
        </div>
    );
}

function Row({
                 label,
                 value
             }) {
    return (
        <div className="flex justify-between gap-4">
            <dt className="text-slate-500">
                {label}
            </dt>

            <dd className="text-right font-medium">
                {value ?? "—"}
            </dd>
        </div>
    );
}