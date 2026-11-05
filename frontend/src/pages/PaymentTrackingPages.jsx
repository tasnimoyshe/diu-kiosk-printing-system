import {
    useCallback,
    useEffect,
    useState
} from "react";

import {
    Link,
    useLocation,
    useNavigate,
    useParams
} from "react-router-dom";

import toast from "react-hot-toast";

import {
    CreditCard
} from "lucide-react";

import {
    apiErrorMessage
} from "../api/http";

import {
    publicApi
} from "../api/kioskApi";

import {
    useRealtimeInvalidation
} from "../realtime/useRealtimeInvalidation";

import {
    Button,
    Card,
    ErrorPanel,
    Input,
    LoadingScreen,
    Money,
    PageHeader,
    StatusBadge,
    Textarea,
    formatDateTime
} from "../components/ui";

export function PaymentPage() {
    const {
        trackingNumber
    } = useParams();

    const location =
        useLocation();

    const navigate =
        useNavigate();

    const [request, setRequest] =
        useState(null);

    const [options, setOptions] =
        useState([]);

    const [registrationNumber, setRegistrationNumber] =
        useState(
            location.state?.registrationNumber
            || ""
        );

    const [pin, setPin] =
        useState("");

    const [loading, setLoading] =
        useState(true);

    const [paying, setPaying] =
        useState(false);

    const [error, setError] =
        useState("");

    const load =
        useCallback(
            async () => {
                try {
                    setError("");

                    const [
                        currentRequest,
                        paymentOptions
                    ] = await Promise.all([
                        publicApi.getRequest(
                            trackingNumber
                        ),

                        publicApi.paymentOptions()
                    ]);

                    setRequest(
                        currentRequest
                    );

                    setOptions(
                        paymentOptions
                    );

                } catch (error) {
                    setError(
                        apiErrorMessage(error)
                    );

                } finally {
                    setLoading(false);
                }
            },
            [trackingNumber]
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

    const alreadyPaid =
        request?.paymentStatus === "PAID"
        || request?.status === "PAID";

    async function payCash(event) {
        event.preventDefault();

        if (
            !registrationNumber.trim()
        ) {
            toast.error(
                "Registration number is required."
            );
            return;
        }

        if (
            !/^\d{4}$/.test(pin)
        ) {
            toast.error(
                "Enter the four-digit cash PIN."
            );
            return;
        }

        setPaying(true);

        try {
            await publicApi.cashPayment(
                trackingNumber,
                {
                    registrationNumber:
                        registrationNumber.trim(),

                    pin
                }
            );

            toast.success(
                "Cash payment verified successfully."
            );

            setPin("");

            await load();

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );

        } finally {
            setPaying(false);
        }
    }

    function payLater() {
        navigate(
            "/pay-later",
            {
                state: {
                    trackingNumber
                }
            }
        );

        toast.success(
            "Keep your tracking number. You can return and complete payment later."
        );
    }

    if (loading) {
        return (
            <LoadingScreen
                message="Loading payment information..."
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
        <div className="page-container max-w-4xl">
            <PageHeader
                title="Payment"
                description={`Tracking number: ${trackingNumber}`}
                actions={
                    !alreadyPaid ? (
                        <Button
                            variant="outline"
                            onClick={payLater}
                        >
                            Pay Later
                        </Button>
                    ) : null
                }
            />

            {!alreadyPaid && (
                <Card className="mb-6 border-blue-200 bg-blue-50">
                    <h2 className="font-bold text-blue-950">
                        Save your tracking number
                    </h2>

                    <p className="mt-2 text-sm text-blue-800">
                        You do not have to pay immediately.
                        If you leave this page, use the
                        Pay Later option in the main menu
                        and enter this tracking number:
                    </p>

                    <div className="mt-4 rounded-xl bg-white px-4 py-3 font-mono text-lg font-bold text-blue-700">
                        {trackingNumber}
                    </div>
                </Card>
            )}

            <Card>
                <div className="grid gap-4 sm:grid-cols-3">
                    <Summary
                        label="Total amount"
                    >
                        <Money
                            value={request?.totalAmount}
                        />
                    </Summary>

                    <Summary
                        label="Payment"
                    >
                        <StatusBadge
                            value={
                                request?.paymentStatus
                                || (
                                    request?.status
                                    === "PAID"
                                        ? "PAID"
                                        : "PENDING"
                                )
                            }
                        />
                    </Summary>

                    <Summary
                        label="Request status"
                    >
                        <StatusBadge
                            value={request?.status}
                        />
                    </Summary>
                </div>
            </Card>

            <div className="mt-6 grid gap-4 md:grid-cols-3">
                {options.map(
                    option => (
                        <Card
                            key={option.method}
                            className={
                                option.enabled
                                    ? "border-blue-200"
                                    : "opacity-70"
                            }
                        >
                            <h3 className="font-bold">
                                {option.method
                                    .replaceAll(
                                        "_",
                                        " "
                                    )}
                            </h3>

                            <p className="mt-2 text-sm text-slate-500">
                                {option.label}
                            </p>

                            {!option.enabled && (
                                <p className="mt-4 font-semibold text-amber-700">
                                    Coming soon
                                </p>
                            )}
                        </Card>
                    )
                )}
            </div>

            <Card className="mt-6">
                <h2 className="text-lg font-bold">
                    Cash Payment
                </h2>

                <p className="mt-1 text-sm text-slate-500">
                    Ask the kiosk agent for the active four-digit PIN,
                    then enter it below.
                </p>

                <form
                    onSubmit={payCash}
                    className="mt-5 grid gap-4 sm:grid-cols-2"
                >
                    <Input
                        label="DIU ID / Registration number"
                        value={registrationNumber}
                        onChange={
                            event =>
                                setRegistrationNumber(
                                    event.target.value
                                )
                        }
                        placeholder="242-15-125"
                    />

                    <Input
                        label="Four-digit cash PIN"
                        value={pin}
                        onChange={
                            event =>
                                setPin(
                                    event.target.value
                                        .replace(
                                            /\D/g,
                                            ""
                                        )
                                        .slice(
                                            0,
                                            4
                                        )
                                )
                        }
                        inputMode="numeric"
                        maxLength={4}
                        placeholder="0000"
                    />

                    <div className="flex flex-wrap gap-3 sm:col-span-2">
                        <Button
                            type="submit"
                            disabled={
                                paying
                                || alreadyPaid
                            }
                        >
                            {alreadyPaid
                                ? "Already Paid"
                                : paying
                                    ? "Verifying..."
                                    : "Confirm Cash Payment"}
                        </Button>

                        {!alreadyPaid && (
                            <Button
                                variant="outline"
                                onClick={payLater}
                            >
                                Pay Later
                            </Button>
                        )}
                    </div>
                </form>
            </Card>
        </div>
    );
}

export function PayLaterPage() {
    const location =
        useLocation();

    const navigate =
        useNavigate();

    const [trackingNumber, setTrackingNumber] =
        useState(
            location.state?.trackingNumber
            || ""
        );

    const [loading, setLoading] =
        useState(false);

    async function continuePayment(
        event
    ) {
        event.preventDefault();

        const normalized =
            trackingNumber.trim();

        if (!normalized) {
            toast.error(
                "Enter your tracking number."
            );
            return;
        }

        setLoading(true);

        try {
            /*
             * Verify that the tracking number exists before
             * navigating to the payment page.
             */
            await publicApi.getRequest(
                normalized
            );

            navigate(
                `/payment/${encodeURIComponent(
                    normalized
                )}`
            );

        } catch (error) {
            toast.error(
                apiErrorMessage(
                    error,
                    "Print request not found."
                )
            );

        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="page-container max-w-xl">
            <PageHeader
                title="Pay Later"
                description="Already submitted a print request? Enter your tracking number to return to its payment page."
            />

            <Card>
                <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-xl bg-blue-100 text-blue-700">
                    <CreditCard className="h-6 w-6"/>
                </div>

                <form
                    onSubmit={continuePayment}
                    className="space-y-4"
                >
                    <Input
                        label="Tracking number"
                        value={trackingNumber}
                        onChange={
                            event =>
                                setTrackingNumber(
                                    event.target.value
                                )
                        }
                        placeholder="Enter your print request tracking number"
                        autoFocus
                    />

                    <Button
                        type="submit"
                        className="w-full"
                        disabled={loading}
                    >
                        {loading
                            ? "Finding Request..."
                            : "Continue to Payment"}
                    </Button>
                </form>

                <p className="mt-5 text-center text-sm text-slate-500">
                    Want to see the current request status instead?{" "}
                    <Link
                        to="/track"
                        className="font-semibold text-blue-700 hover:underline"
                    >
                        Track Request
                    </Link>
                </p>
            </Card>
        </div>
    );
}

export function TrackRequestPage() {
    const navigate =
        useNavigate();

    const [trackingNumber, setTrackingNumber] =
        useState("");

    const [request, setRequest] =
        useState(null);

    const [searchedTracking, setSearchedTracking] =
        useState("");

    const [error, setError] =
        useState("");

    const [loading, setLoading] =
        useState(false);

    const [registrationNumber, setRegistrationNumber] =
        useState("");

    const [reason, setReason] =
        useState("");

    const load =
        useCallback(
            async () => {
                if (!searchedTracking) {
                    return;
                }

                try {
                    const response =
                        await publicApi.getRequest(
                            searchedTracking
                        );

                    setRequest(response);
                    setError("");

                } catch (error) {
                    setRequest(null);

                    setError(
                        apiErrorMessage(error)
                    );
                }
            },
            [searchedTracking]
        );

    useRealtimeInvalidation({
        enabled:
            Boolean(
                searchedTracking
            ),

        topics: [
            "/topic/public-queue"
        ],

        reload: load
    });

    async function search(event) {
        event.preventDefault();

        const normalized =
            trackingNumber.trim();

        if (!normalized) {
            toast.error(
                "Enter your tracking number."
            );
            return;
        }

        setLoading(true);

        setSearchedTracking(
            normalized
        );

        try {
            const response =
                await publicApi
                    .getRequest(
                        normalized
                    );

            setRequest(response);
            setError("");

        } catch (error) {
            setRequest(null);

            setError(
                apiErrorMessage(error)
            );

        } finally {
            setLoading(false);
        }
    }

    async function cancel() {
        if (
            !window.confirm(
                "Are you sure you want to cancel this print request?"
            )
        ) {
            return;
        }

        if (
            !registrationNumber.trim()
            || !reason.trim()
        ) {
            toast.error(
                "Registration number and cancellation reason are required."
            );
            return;
        }

        try {
            await publicApi.cancelRequest(
                searchedTracking,
                {
                    registrationNumber:
                        registrationNumber.trim(),

                    reason:
                        reason.trim()
                }
            );

            toast.success(
                "Print request cancelled."
            );

            await load();

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );
        }
    }

    const paid =
        request?.paymentStatus === "PAID"
        || request?.status === "PAID";

    const terminal =
        [
            "COMPLETED",
            "CANCELLED",
            "EXPIRED"
        ].includes(
            request?.status
        );

    const canContinuePayment =
        request
        && !paid
        && !terminal;

    return (
        <div className="page-container max-w-4xl">
            <PageHeader
                title="Track Print Request"
                description="Enter the tracking number issued when the request was submitted."
            />

            <Card>
                <form
                    onSubmit={search}
                    className="flex flex-col gap-3 sm:flex-row"
                >
                    <div className="flex-1">
                        <Input
                            value={trackingNumber}
                            onChange={
                                event =>
                                    setTrackingNumber(
                                        event.target.value
                                    )
                            }
                            placeholder="Enter tracking number"
                        />
                    </div>

                    <Button
                        type="submit"
                        disabled={loading}
                    >
                        {loading
                            ? "Searching..."
                            : "Track"}
                    </Button>
                </form>
            </Card>

            {error && (
                <div className="mt-6">
                    <ErrorPanel
                        message={error}
                    />
                </div>
            )}

            {request && (
                <Card className="mt-6">
                    <div className="flex flex-wrap items-start justify-between gap-4">
                        <div>
                            <h2 className="text-xl font-bold">
                                {request.fullName}
                            </h2>

                            <p className="mt-1 font-mono text-xs text-slate-500">
                                {request.trackingNumber}
                            </p>
                        </div>

                        <StatusBadge
                            value={request.status}
                        />
                    </div>

                    <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                        <Summary
                            label="User type"
                        >
                            {request.userType}
                        </Summary>

                        <Summary
                            label="Print color"
                        >
                            {request.printColor}
                        </Summary>

                        <Summary
                            label="Printing side"
                        >
                            {request.printingSide}
                        </Summary>

                        <Summary
                            label="Document pages"
                        >
                            {request.totalDocumentPages}
                        </Summary>

                        <Summary
                            label="Physical sheets"
                        >
                            {request.totalPhysicalSheets}
                        </Summary>

                        <Summary
                            label="Total"
                        >
                            <Money
                                value={request.totalAmount}
                            />
                        </Summary>

                        <Summary
                            label="Submitted"
                        >
                            {formatDateTime(
                                request.createdAt
                            )}
                        </Summary>

                        <Summary
                            label="Expires"
                        >
                            {formatDateTime(
                                request.expiresAt
                            )}
                        </Summary>
                    </div>

                    {canContinuePayment && (
                        <div className="mt-6 border-t border-slate-200 pt-6">
                            <Button
                                onClick={
                                    () =>
                                        navigate(
                                            `/payment/${encodeURIComponent(
                                                request.trackingNumber
                                            )}`
                                        )
                                }
                            >
                                <CreditCard className="h-4 w-4"/>
                                Continue Payment
                            </Button>
                        </div>
                    )}

                    {[
                        "AWAITING_PAYMENT",
                        "PAYMENT_FAILED"
                    ].includes(
                        request.status
                    ) && (
                        <div className="mt-8 border-t border-slate-200 pt-6">
                            <h3 className="font-bold">
                                Cancel Request
                            </h3>

                            <div className="mt-4 grid gap-4 sm:grid-cols-2">
                                <Input
                                    label="DIU ID / Registration number"
                                    value={registrationNumber}
                                    onChange={
                                        event =>
                                            setRegistrationNumber(
                                                event.target.value
                                            )
                                    }
                                />

                                <div className="sm:col-span-2">
                                    <Textarea
                                        label="Cancellation reason"
                                        value={reason}
                                        onChange={
                                            event =>
                                                setReason(
                                                    event.target.value
                                                )
                                        }
                                    />
                                </div>

                                <div className="sm:col-span-2">
                                    <Button
                                        variant="danger"
                                        onClick={cancel}
                                    >
                                        Cancel Request
                                    </Button>
                                </div>
                            </div>
                        </div>
                    )}
                </Card>
            )}
        </div>
    );
}

function Summary({
                     label,
                     children
                 }) {
    return (
        <div>
            <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                {label}
            </p>

            <div className="mt-1 font-semibold">
                {children ?? "—"}
            </div>
        </div>
    );
}