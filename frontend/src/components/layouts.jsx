import {
    useState
} from "react";

import {
    Link,
    NavLink,
    Navigate,
    Outlet
} from "react-router-dom";

import {
    LogOut,
    Menu,
    Printer,
    X
} from "lucide-react";

import {
    useAuth
} from "../auth/AuthContext";

import {
    Button,
    LoadingScreen
} from "./ui";

function navClass({
                      isActive
                  }) {
    return `
    rounded-lg px-3 py-2 text-sm font-medium transition
    ${
        isActive
            ? "bg-blue-50 text-blue-700"
            : "text-slate-600 hover:bg-slate-100 hover:text-slate-950"
    }
  `;
}

function Brand() {
    return (
        <Link
            to="/"
            className="flex items-center gap-2 font-bold text-slate-950"
        >
        <span className="grid h-9 w-9 place-items-center rounded-xl bg-blue-600 text-white">
          <Printer className="h-5 w-5"/>
        </span>

            <span>
          DIU Kiosk
        </span>
        </Link>
    );
}

export function PublicLayout() {
    const [open, setOpen] =
        useState(false);

    const {
        user
    } = useAuth();

    const dashboardPath =
        user?.role === "ADMIN"
            ? "/admin/dashboard"
            : "/agent/dashboard";

    return (
        <div className="min-h-screen">
            <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/95 backdrop-blur">
                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    <Brand/>

                    <nav className="hidden items-center gap-1 md:flex">
                        <NavLink
                            to="/"
                            end
                            className={navClass}
                        >
                            Print Request
                        </NavLink>

                        <NavLink
                            to="/queue"
                            className={navClass}
                        >
                            Show Queue
                        </NavLink>

                        <NavLink
                            to="/track"
                            className={navClass}
                        >
                            Track Request
                        </NavLink>

                        <Link
                            to="/pay-later"
                        >
                            <Button
                                variant="outline"
                            >
                                Pay Later
                            </Button>
                        </Link>

                        {user ? (
                            <Link
                                to={dashboardPath}
                            >
                                <Button>
                                    Dashboard
                                </Button>
                            </Link>
                        ) : (
                            <Link to="/login">
                                <Button>
                                    Login
                                </Button>
                            </Link>
                        )}
                    </nav>

                    <button
                        className="rounded-lg p-2 md:hidden"
                        onClick={
                            () => setOpen(
                                value => !value
                            )
                        }
                        aria-label="Toggle menu"
                    >
                        {open
                            ? <X/>
                            : <Menu/>}
                    </button>
                </div>

                {open && (
                    <div className="border-t border-slate-200 bg-white px-4 py-3 md:hidden">
                        <div className="flex flex-col gap-1">
                            <NavLink
                                to="/"
                                end
                                className={navClass}
                                onClick={
                                    () =>
                                        setOpen(false)
                                }
                            >
                                Print Request
                            </NavLink>

                            <NavLink
                                to="/queue"
                                className={navClass}
                                onClick={
                                    () =>
                                        setOpen(false)
                                }
                            >
                                Show Queue
                            </NavLink>

                            <NavLink
                                to="/track"
                                className={navClass}
                                onClick={
                                    () =>
                                        setOpen(false)
                                }
                            >
                                Track Request
                            </NavLink>

                            <NavLink
                                to="/pay-later"
                                className={navClass}
                                onClick={
                                    () =>
                                        setOpen(false)
                                }
                            >
                                Pay Later
                            </NavLink>

                            <NavLink
                                to={
                                    user
                                        ? dashboardPath
                                        : "/login"
                                }
                                className={navClass}
                                onClick={
                                    () =>
                                        setOpen(false)
                                }
                            >
                                {user
                                    ? "Dashboard"
                                    : "Login"}
                            </NavLink>
                        </div>
                    </div>
                )}
            </header>

            <main>
                <Outlet/>
            </main>
        </div>
    );
}

export function ProtectedRoute({
                                   roles
                               }) {
    const {
        user,
        loading
    } = useAuth();

    if (loading) {
        return (
            <LoadingScreen
                message="Checking your session..."
            />
        );
    }

    if (!user) {
        return (
            <Navigate
                to="/login"
                replace
            />
        );
    }

    if (
        roles
        && !roles.includes(
            user.role
        )
    ) {
        return (
            <Navigate
                to="/unauthorized"
                replace
            />
        );
    }

    return <Outlet/>;
}

export function PortalLayout() {
    const [open, setOpen] =
        useState(false);

    const {
        user,
        logout
    } = useAuth();

    const isAdmin =
        user?.role === "ADMIN";

    const links =
        isAdmin
            ? [
                {
                    to: "/admin/dashboard",
                    label: "Dashboard"
                },
                {
                    to: "/admin/agents/pending",
                    label: "Confirm Agents"
                },
                {
                    to: "/admin/queue",
                    label: "Show Queue"
                },
                {
                    to: "/admin/profile",
                    label: "Profile"
                }
            ]
            : [
                {
                    to: "/agent/dashboard",
                    label: "Dashboard"
                },
                {
                    to: "/agent/queue",
                    label: "Show Queue"
                },
                {
                    to: "/agent/pricing",
                    label: "Pricing"
                },
                {
                    to: "/agent/inventory",
                    label: "Inventory"
                },
                {
                    to: "/agent/profile",
                    label: "Profile"
                }
            ];

    async function handleLogout() {
        await logout();
    }

    return (
        <div className="min-h-screen bg-slate-50">
            <header className="sticky top-0 z-40 border-b border-slate-200 bg-white">
                <div className="mx-auto flex min-h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    <Brand/>

                    <nav className="hidden items-center gap-1 lg:flex">
                        {links.map(
                            link => (
                                <NavLink
                                    key={link.to}
                                    to={link.to}
                                    className={navClass}
                                >
                                    {link.label}
                                </NavLink>
                            )
                        )}

                        <Button
                            variant="outline"
                            onClick={handleLogout}
                        >
                            <LogOut className="h-4 w-4"/>
                            Logout
                        </Button>
                    </nav>

                    <button
                        className="rounded-lg p-2 lg:hidden"
                        onClick={
                            () => setOpen(
                                value => !value
                            )
                        }
                        aria-label="Toggle portal menu"
                    >
                        {open
                            ? <X/>
                            : <Menu/>}
                    </button>
                </div>

                {open && (
                    <div className="border-t border-slate-200 bg-white p-4 lg:hidden">
                        <div className="flex flex-col gap-1">
                            {links.map(
                                link => (
                                    <NavLink
                                        key={link.to}
                                        to={link.to}
                                        className={navClass}
                                        onClick={
                                            () =>
                                                setOpen(false)
                                        }
                                    >
                                        {link.label}
                                    </NavLink>
                                )
                            )}

                            <Button
                                variant="outline"
                                className="mt-2"
                                onClick={handleLogout}
                            >
                                Logout
                            </Button>
                        </div>
                    </div>
                )}
            </header>

            <div className="border-b border-slate-200 bg-white">
                <div className="mx-auto max-w-7xl px-4 py-2 text-xs text-slate-500 sm:px-6 lg:px-8">
                    Signed in as{" "}
                    <strong className="text-slate-700">
                        {user?.fullName}
                    </strong>

                    {" · "}

                    {user?.role}

                    {user?.agentId
                        ? ` · ${user.agentId}`
                        : ""}
                </div>
            </div>

            <main>
                <Outlet/>
            </main>
        </div>
    );
}