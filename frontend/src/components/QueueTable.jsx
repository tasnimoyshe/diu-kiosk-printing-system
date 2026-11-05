import {
    Clock3,
    Hash
} from "lucide-react";

import {
    Money,
    StatusBadge,
    formatDateTime
} from "./ui";

export default function QueueTable({
                                       entries = [],
                                       actions,
                                       trackingAction
                                   }) {
    return (
        <>
            <div className="professional-scrollbar hidden overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm md:block">
                <table className="min-w-full divide-y divide-slate-200">
                    <thead className="bg-slate-50/90">
                    <tr>
                        <TableHeading>
                            Queue
                        </TableHeading>

                        <TableHeading>
                            User
                        </TableHeading>

                        <TableHeading>
                            Tracking
                        </TableHeading>

                        <TableHeading>
                            Payment
                        </TableHeading>

                        <TableHeading>
                            Request
                        </TableHeading>

                        <TableHeading>
                            Amount
                        </TableHeading>

                        <TableHeading>
                            Submitted
                        </TableHeading>

                        {actions && (
                            <TableHeading
                                align="right"
                            >
                                Action
                            </TableHeading>
                        )}
                    </tr>
                    </thead>

                    <tbody className="divide-y divide-slate-100">
                    {entries.map(
                        entry => (
                            <tr
                                key={
                                    entry.requestId
                                    ?? entry.trackingNumber
                                }
                                className="transition hover:bg-slate-50/80"
                            >
                                <td className="whitespace-nowrap px-4 py-4">
                                    <div className="inline-flex items-center gap-1 rounded-lg bg-blue-50 px-2.5 py-1.5 text-sm font-extrabold text-blue-700">
                                        <Hash className="h-3.5 w-3.5"/>

                                        {entry.queuePosition}
                                    </div>
                                </td>

                                <td className="px-4 py-4">
                                    <div className="font-semibold text-slate-900">
                                        {entry.fullName}
                                    </div>

                                    <div className="mt-1 text-xs text-slate-500">
                                        {entry.maskedRegistrationNumber}
                                        {" · "}
                                        {pretty(
                                            entry.userType
                                        )}
                                    </div>
                                </td>

                                <td className="px-4 py-4">
                                    <div className="flex min-w-[170px] flex-col items-start gap-2">
                          <span className="font-mono text-xs font-semibold text-slate-700">
                            {entry.trackingNumber}
                          </span>

                                        {trackingAction && (
                                            <div>
                                                {trackingAction(
                                                    entry
                                                )}
                                            </div>
                                        )}
                                    </div>
                                </td>

                                <td className="px-4 py-4">
                                    <StatusBadge
                                        value={entry.paymentStatus}
                                    />
                                </td>

                                <td className="px-4 py-4">
                                    <StatusBadge
                                        value={entry.requestStatus}
                                    />
                                </td>

                                <td className="whitespace-nowrap px-4 py-4 text-sm font-bold text-slate-800">
                                    <Money
                                        value={entry.totalAmount}
                                    />
                                </td>

                                <td className="whitespace-nowrap px-4 py-4 text-xs text-slate-500">
                                    <div className="flex items-center gap-1.5">
                                        <Clock3 className="h-3.5 w-3.5"/>

                                        {formatDateTime(
                                            entry.submittedAt
                                        )}
                                    </div>
                                </td>

                                {actions && (
                                    <td className="px-4 py-4 text-right">
                                        {actions(entry)}
                                    </td>
                                )}
                            </tr>
                        )
                    )}
                    </tbody>
                </table>
            </div>

            <div className="space-y-3 md:hidden">
                {entries.map(
                    entry => (
                        <div
                            key={
                                entry.requestId
                                ?? entry.trackingNumber
                            }
                            className="surface-elevated overflow-hidden p-0"
                        >
                            <div className="flex items-start justify-between gap-3 border-b border-slate-100 bg-slate-50/70 px-4 py-3">
                                <div>
                                    <div className="inline-flex items-center gap-1 text-sm font-extrabold text-blue-700">
                                        <Hash className="h-3.5 w-3.5"/>
                                        Queue {entry.queuePosition}
                                    </div>

                                    <p className="mt-1 font-bold text-slate-950">
                                        {entry.fullName}
                                    </p>

                                    <p className="mt-0.5 text-xs text-slate-500">
                                        {entry.maskedRegistrationNumber}
                                        {" · "}
                                        {pretty(
                                            entry.userType
                                        )}
                                    </p>
                                </div>

                                <StatusBadge
                                    value={entry.paymentStatus}
                                />
                            </div>

                            <div className="space-y-4 p-4">
                                <div>
                                    <p className="muted-label">
                                        Tracking number
                                    </p>

                                    <div className="mt-1 flex flex-wrap items-center gap-2">
                          <span className="break-all font-mono text-xs font-semibold text-slate-700">
                            {entry.trackingNumber}
                          </span>

                                        {trackingAction
                                            ? trackingAction(
                                                entry
                                            )
                                            : null}
                                    </div>
                                </div>

                                <div className="grid grid-cols-2 gap-3">
                                    <div>
                                        <p className="muted-label">
                                            Status
                                        </p>

                                        <div className="mt-1.5">
                                            <StatusBadge
                                                value={entry.requestStatus}
                                            />
                                        </div>
                                    </div>

                                    <div>
                                        <p className="muted-label">
                                            Amount
                                        </p>

                                        <p className="mt-1.5 text-sm font-bold">
                                            <Money
                                                value={entry.totalAmount}
                                            />
                                        </p>
                                    </div>
                                </div>

                                <div className="flex items-center gap-1.5 text-xs text-slate-500">
                                    <Clock3 className="h-3.5 w-3.5"/>

                                    {formatDateTime(
                                        entry.submittedAt
                                    )}
                                </div>

                                {actions && (
                                    <div className="border-t border-slate-100 pt-3">
                                        {actions(entry)}
                                    </div>
                                )}
                            </div>
                        </div>
                    )
                )}
            </div>
        </>
    );
}

function TableHeading({
                          children,
                          align = "left"
                      }) {
    return (
        <th
            className={`
            whitespace-nowrap px-4 py-3
            text-xs font-bold uppercase tracking-[0.08em]
            text-slate-500
            ${
                align === "right"
                    ? "text-right"
                    : "text-left"
            }
          `}
        >
            {children}
        </th>
    );
}

function pretty(value) {
    return String(value || "—")
        .replaceAll(
            "_",
            " "
        );
}