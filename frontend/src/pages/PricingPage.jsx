import {
    useCallback,
    useEffect,
    useState
} from "react";

import toast from "react-hot-toast";

import {
    apiErrorMessage
} from "../api/http";

import {
    agentApi,
    pageContent
} from "../api/kioskApi";

import {
    useRealtimeInvalidation
} from "../realtime/useRealtimeInvalidation";

import {
    Button,
    Card,
    Input,
    LoadingScreen,
    Money,
    PageHeader,
    formatDateTime
} from "../components/ui";

export default function PricingPage() {
    const [pricing, setPricing] =
        useState(null);

    const [history, setHistory] =
        useState([]);

    const [form, setForm] =
        useState(null);

    const [saving, setSaving] =
        useState(false);

    const load =
        useCallback(
            async () => {
                try {
                    const [
                        current,
                        historyResponse
                    ] = await Promise.all([
                        agentApi.pricing(),

                        agentApi.pricingHistory({
                            page: 0,
                            size: 50
                        })
                    ]);

                    setPricing(current);

                    setForm({
                        colorSingleSided:
                        current.colorSingleSided,

                        blackWhiteSingleSided:
                        current.blackWhiteSingleSided,

                        colorDoubleSided:
                        current.colorDoubleSided,

                        blackWhiteDoubleSided:
                        current.blackWhiteDoubleSided
                    });

                    setHistory(
                        pageContent(
                            historyResponse
                        )
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
        topics: [
            "/topic/pricing"
        ],
        reload: load
    });

    function change(event) {
        setForm(
            previous => ({
                ...previous,
                [event.target.name]:
                event.target.value
            })
        );
    }

    async function save(event) {
        event.preventDefault();

        setSaving(true);

        try {
            await agentApi.updatePricing({
                revision:
                pricing.revision,

                colorSingleSided:
                    Number(
                        form.colorSingleSided
                    ),

                blackWhiteSingleSided:
                    Number(
                        form.blackWhiteSingleSided
                    ),

                colorDoubleSided:
                    Number(
                        form.colorDoubleSided
                    ),

                blackWhiteDoubleSided:
                    Number(
                        form.blackWhiteDoubleSided
                    )
            });

            toast.success(
                "Global printing prices updated."
            );

            await load();

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );

        } finally {
            setSaving(false);
        }
    }

    if (
        !pricing
        || !form
    ) {
        return (
            <LoadingScreen
                message="Loading pricing..."
            />
        );
    }

    return (
        <div className="page-container">
            <PageHeader
                title="Printing Pricing"
                description="Changes affect future requests only. Existing requests retain their original price snapshot."
            />

            <Card>
                <form
                    onSubmit={save}
                    className="grid gap-4 sm:grid-cols-2"
                >
                    <Input
                        label="Color · One side"
                        name="colorSingleSided"
                        type="number"
                        step="0.01"
                        min="0"
                        value={form.colorSingleSided}
                        onChange={change}
                    />

                    <Input
                        label="Black & White · One side"
                        name="blackWhiteSingleSided"
                        type="number"
                        step="0.01"
                        min="0"
                        value={form.blackWhiteSingleSided}
                        onChange={change}
                    />

                    <Input
                        label="Color · Both sides"
                        name="colorDoubleSided"
                        type="number"
                        step="0.01"
                        min="0"
                        value={form.colorDoubleSided}
                        onChange={change}
                    />

                    <Input
                        label="Black & White · Both sides"
                        name="blackWhiteDoubleSided"
                        type="number"
                        step="0.01"
                        min="0"
                        value={form.blackWhiteDoubleSided}
                        onChange={change}
                    />

                    <div className="sm:col-span-2">
                        <Button
                            type="submit"
                            disabled={saving}
                        >
                            {saving
                                ? "Saving..."
                                : "Update Global Prices"}
                        </Button>

                        <span className="ml-3 text-xs text-slate-500">
                Revision {pricing.revision}
              </span>
                    </div>
                </form>
            </Card>

            <Card className="mt-6">
                <h2 className="font-bold">
                    Price History
                </h2>

                <div className="mt-4 overflow-x-auto">
                    <table className="min-w-full text-sm">
                        <thead>
                        <tr className="border-b border-slate-200 text-left text-xs uppercase text-slate-500">
                            <th className="py-3 pr-4">
                                Type
                            </th>

                            <th className="py-3 pr-4">
                                Previous
                            </th>

                            <th className="py-3 pr-4">
                                New
                            </th>

                            <th className="py-3 pr-4">
                                Agent
                            </th>

                            <th className="py-3">
                                Changed
                            </th>
                        </tr>
                        </thead>

                        <tbody>
                        {history.map(
                            entry => (
                                <tr
                                    key={entry.id}
                                    className="border-b border-slate-100"
                                >
                                    <td className="py-3 pr-4">
                                        {entry.priceType
                                            ?.replaceAll("_", " ")}
                                    </td>

                                    <td className="py-3 pr-4">
                                        <Money
                                            value={entry.previousPrice}
                                        />
                                    </td>

                                    <td className="py-3 pr-4">
                                        <Money
                                            value={entry.newPrice}
                                        />
                                    </td>

                                    <td className="py-3 pr-4">
                                        {entry.changedByAgentId
                                            || entry.changedByAccountId
                                            || "—"}
                                    </td>

                                    <td className="py-3">
                                        {formatDateTime(
                                            entry.changedAt
                                        )}
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