import {
    useEffect
} from "react";

import {
    AlertCircle,
    LoaderCircle,
    X
} from "lucide-react";

export function Button({
                           children,
                           variant = "primary",
                           className = "",
                           type = "button",
                           disabled,
                           ...props
                       }) {
    const variants = {
        primary:
            "bg-blue-600 text-white shadow-sm hover:bg-blue-700 hover:shadow-md active:bg-blue-800",

        secondary:
            "bg-slate-100 text-slate-800 hover:bg-slate-200",

        success:
            "bg-emerald-600 text-white shadow-sm hover:bg-emerald-700",

        danger:
            "bg-red-600 text-white shadow-sm hover:bg-red-700",

        warning:
            "bg-amber-500 text-white shadow-sm hover:bg-amber-600",

        outline:
            "border border-slate-300 bg-white text-slate-700 shadow-sm hover:border-slate-400 hover:bg-slate-50",

        ghost:
            "text-slate-600 hover:bg-slate-100 hover:text-slate-950"
    };

    return (
        <button
            type={type}
            disabled={disabled}
            className={`
            inline-flex min-h-10 items-center justify-center gap-2
            rounded-xl px-4 py-2 text-sm font-semibold
            transition duration-150
            disabled:cursor-not-allowed disabled:opacity-50
            ${variants[variant] || variants.primary}
            ${className}
          `}
            {...props}
        >
            {children}
        </button>
    );
}

export function Card({
                         children,
                         className = "",
                         ...props
                     }) {
    return (
        <div
            className={`
            surface-elevated
            p-5
            ${className}
          `}
            {...props}
        >
            {children}
        </div>
    );
}

export function Input({
                          label,
                          error,
                          helperText,
                          ...props
                      }) {
    return (
        <label className="block">
            {label && (
                <span className="form-label">
              {label}
            </span>
            )}

            <input
                className={`
              form-control
              ${
                    error
                        ? "border-red-400 focus:border-red-500 focus:ring-red-100"
                        : ""
                }
            `}
                {...props}
            />

            {error ? (
                <span className="mt-1.5 flex items-center gap-1 text-xs font-medium text-red-600">
              <AlertCircle className="h-3.5 w-3.5"/>
                    {error}
            </span>
            ) : helperText ? (
                <span className="mt-1.5 block text-xs text-slate-500">
              {helperText}
            </span>
            ) : null}
        </label>
    );
}

export function Select({
                           label,
                           children,
                           error,
                           helperText,
                           ...props
                       }) {
    return (
        <label className="block">
            {label && (
                <span className="form-label">
              {label}
            </span>
            )}

            <select
                className={`
              form-control
              ${
                    error
                        ? "border-red-400 focus:border-red-500 focus:ring-red-100"
                        : ""
                }
            `}
                {...props}
            >
                {children}
            </select>

            {error ? (
                <span className="mt-1.5 flex items-center gap-1 text-xs font-medium text-red-600">
              <AlertCircle className="h-3.5 w-3.5"/>
                    {error}
            </span>
            ) : helperText ? (
                <span className="mt-1.5 block text-xs text-slate-500">
              {helperText}
            </span>
            ) : null}
        </label>
    );
}

export function Textarea({
                             label,
                             error,
                             helperText,
                             ...props
                         }) {
    return (
        <label className="block">
            {label && (
                <span className="form-label">
              {label}
            </span>
            )}

            <textarea
                className={`
              form-control min-h-24 resize-y
              ${
                    error
                        ? "border-red-400 focus:border-red-500 focus:ring-red-100"
                        : ""
                }
            `}
                {...props}
            />

            {error ? (
                <span className="mt-1.5 flex items-center gap-1 text-xs font-medium text-red-600">
              <AlertCircle className="h-3.5 w-3.5"/>
                    {error}
            </span>
            ) : helperText ? (
                <span className="mt-1.5 block text-xs text-slate-500">
              {helperText}
            </span>
            ) : null}
        </label>
    );
}

export function PageHeader({
                               title,
                               description,
                               actions
                           }) {
    return (
        <div className="mb-6 flex flex-col justify-between gap-4 sm:mb-8 sm:flex-row sm:items-start">
            <div className="min-w-0">
                <h1 className="text-2xl font-extrabold tracking-tight text-slate-950 sm:text-3xl">
                    {title}
                </h1>

                {description && (
                    <p className="mt-1.5 max-w-3xl text-sm leading-6 text-slate-600">
                        {description}
                    </p>
                )}
            </div>

            {actions && (
                <div className="flex shrink-0 flex-wrap gap-2">
                    {actions}
                </div>
            )}
        </div>
    );
}

export function SectionTitle({
                                 title,
                                 description,
                                 actions
                             }) {
    return (
        <div className="mb-4 flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
            <div>
                <h2 className="text-lg font-bold text-slate-950">
                    {title}
                </h2>

                {description && (
                    <p className="mt-1 text-sm text-slate-500">
                        {description}
                    </p>
                )}
            </div>

            {actions && (
                <div className="flex flex-wrap gap-2">
                    {actions}
                </div>
            )}
        </div>
    );
}

export function LoadingScreen({
                                  message = "Loading..."
                              }) {
    return (
        <div className="flex min-h-64 flex-col items-center justify-center gap-3 text-slate-500">
            <div className="grid h-14 w-14 place-items-center rounded-2xl border border-slate-200 bg-white shadow-sm">
                <LoaderCircle
                    className="h-7 w-7 animate-spin text-blue-600"
                />
            </div>

            <span className="text-sm font-medium">
          {message}
        </span>
        </div>
    );
}

export function ErrorPanel({
                               message,
                               onRetry
                           }) {
    return (
        <div className="rounded-2xl border border-red-200 bg-red-50 p-5 shadow-sm">
            <div className="flex gap-3">
                <div className="grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-red-100 text-red-700">
                    <AlertCircle className="h-5 w-5"/>
                </div>

                <div className="min-w-0 flex-1">
                    <p className="font-semibold text-red-900">
                        Something went wrong
                    </p>

                    <p className="mt-1 text-sm leading-6 text-red-700">
                        {message}
                    </p>

                    {onRetry && (
                        <Button
                            variant="outline"
                            className="mt-4"
                            onClick={onRetry}
                        >
                            Try Again
                        </Button>
                    )}
                </div>
            </div>
        </div>
    );
}

export function EmptyState({
                               title = "Nothing here yet",
                               description,
                               action
                           }) {
    return (
        <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-12 text-center shadow-sm">
            <div className="mx-auto grid h-12 w-12 place-items-center rounded-2xl bg-slate-100 text-slate-400">
                <span className="text-xl">—</span>
            </div>

            <h3 className="mt-4 font-bold text-slate-800">
                {title}
            </h3>

            {description && (
                <p className="mx-auto mt-1 max-w-lg text-sm leading-6 text-slate-500">
                    {description}
                </p>
            )}

            {action && (
                <div className="mt-5">
                    {action}
                </div>
            )}
        </div>
    );
}

export function StatusBadge({
                                value
                            }) {
    const normalized =
        String(value || "UNKNOWN")
            .toUpperCase();

    let classes =
        "border-slate-200 bg-slate-100 text-slate-700";

    if (
        normalized.includes("PAID")
        || normalized.includes("APPROVED")
        || normalized.includes("COMPLETED")
        || normalized === "ACTIVE"
        || normalized === "SALE"
    ) {
        classes =
            "border-emerald-200 bg-emerald-50 text-emerald-700";
    }

    if (
        normalized.includes("PENDING")
        || normalized.includes("AWAITING")
        || normalized.includes("CLAIMED")
        || normalized.includes("DOWNLOADING")
    ) {
        classes =
            "border-amber-200 bg-amber-50 text-amber-700";
    }

    if (
        normalized.includes("PRINTING")
    ) {
        classes =
            "border-blue-200 bg-blue-50 text-blue-700";
    }

    if (
        normalized === "ARCHIVED"
    ) {
        classes =
            "border-orange-200 bg-orange-50 text-orange-700";
    }

    if (
        normalized === "RETURN"
    ) {
        classes =
            "border-rose-200 bg-rose-50 text-rose-700";
    }

    if (
        normalized === "MANUAL_CORRECTION"
    ) {
        classes =
            "border-violet-200 bg-violet-50 text-violet-700";
    }

    if (
        normalized.includes("REJECTED")
        || normalized.includes("SUSPENDED")
        || normalized.includes("CANCELLED")
        || normalized.includes("EXPIRED")
        || normalized.includes("FAILED")
        || normalized.includes("ERROR")
        || normalized.includes("DELETED")
    ) {
        classes =
            "border-red-200 bg-red-50 text-red-700";
    }

    return (
        <span
            className={`
            inline-flex items-center rounded-full border
            px-2.5 py-1 text-[11px] font-bold tracking-wide
            ${classes}
          `}
        >
        {normalized.replaceAll("_", " ")}
      </span>
    );
}

export function MetricCard({
                               label,
                               value,
                               detail
                           }) {
    return (
        <div className="surface-elevated p-5 transition duration-200 hover:-translate-y-0.5 hover:shadow-lg">
            <p className="text-sm font-semibold text-slate-500">
                {label}
            </p>

            <div className="mt-2 text-3xl font-extrabold tracking-tight text-slate-950">
                {value ?? 0}
            </div>

            {detail && (
                <p className="mt-1.5 text-xs leading-5 text-slate-500">
                    {detail}
                </p>
            )}
        </div>
    );
}

export function Money({
                          value
                      }) {
    const number =
        Number(value || 0);

    return (
        <>
            {Number.isFinite(number)
                ? number.toFixed(2)
                : "0.00"}{" "}
            BDT
        </>
    );
}

export function Modal({
                          open,
                          title,
                          description,
                          children,
                          footer,
                          onClose,
                          size = "md"
                      }) {
    const sizes = {
        sm: "max-w-md",
        md: "max-w-lg",
        lg: "max-w-2xl",
        xl: "max-w-4xl"
    };

    useEffect(
        () => {
            if (!open) {
                return undefined;
            }

            const previousOverflow =
                document.body.style.overflow;

            document.body.style.overflow =
                "hidden";

            function handleKey(event) {
                if (event.key === "Escape") {
                    onClose?.();
                }
            }

            window.addEventListener(
                "keydown",
                handleKey
            );

            return () => {
                document.body.style.overflow =
                    previousOverflow;

                window.removeEventListener(
                    "keydown",
                    handleKey
                );
            };
        },
        [
            open,
            onClose
        ]
    );

    if (!open) {
        return null;
    }

    return (
        <div
            className="fixed inset-0 z-[100] flex items-end justify-center bg-slate-950/45 p-0 backdrop-blur-sm sm:items-center sm:p-4"
            role="dialog"
            aria-modal="true"
            onMouseDown={
                event => {
                    if (
                        event.target ===
                        event.currentTarget
                    ) {
                        onClose?.();
                    }
                }
            }
        >
            <div
                className={`
              professional-scrollbar
              max-h-[92vh] w-full overflow-y-auto
              rounded-t-3xl border border-slate-200
              bg-white shadow-2xl
              sm:rounded-3xl
              ${sizes[size] || sizes.md}
            `}
            >
                <div className="flex items-start justify-between gap-4 border-b border-slate-100 px-5 py-5 sm:px-6">
                    <div className="min-w-0">
                        <h2 className="text-lg font-extrabold text-slate-950">
                            {title}
                        </h2>

                        {description && (
                            <p className="mt-1 text-sm leading-6 text-slate-500">
                                {description}
                            </p>
                        )}
                    </div>

                    <button
                        type="button"
                        onClick={onClose}
                        className="grid h-10 w-10 shrink-0 place-items-center rounded-xl text-slate-500 transition hover:bg-slate-100 hover:text-slate-950"
                        aria-label="Close dialog"
                    >
                        <X className="h-5 w-5"/>
                    </button>
                </div>

                <div className="px-5 py-5 sm:px-6">
                    {children}
                </div>

                {footer && (
                    <div className="flex flex-wrap justify-end gap-2 border-t border-slate-100 bg-slate-50/70 px-5 py-4 sm:px-6">
                        {footer}
                    </div>
                )}
            </div>
        </div>
    );
}

export function formatDateTime(
    value
) {
    if (!value) {
        return "—";
    }

    const date =
        new Date(value);

    if (Number.isNaN(date.getTime())) {
        return String(value);
    }

    return date.toLocaleString(
        "en-BD",
        {
            dateStyle: "medium",
            timeStyle: "short"
        }
    );
}