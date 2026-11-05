import {
    useState
} from "react";

import {
    Link,
    useNavigate
} from "react-router-dom";

import toast from "react-hot-toast";

import {
    ShieldCheck,
    UserRound
} from "lucide-react";

import {
    apiErrorMessage
} from "../api/http";

import {
    authApi
} from "../api/kioskApi";

import {
    useAuth
} from "../auth/AuthContext";

import {
    Button,
    Card,
    Input,
    PageHeader
} from "../components/ui";

export function LoginSelectionPage() {
    return (
        <div className="page-container max-w-4xl">
            <PageHeader
                title="Login"
                description="Select the correct portal."
            />

            <div className="grid gap-5 sm:grid-cols-2">
                <Link to="/login/admin">
                    <Card className="h-full transition hover:-translate-y-1 hover:border-blue-300">
                        <ShieldCheck className="h-10 w-10 text-blue-600"/>

                        <h2 className="mt-4 text-xl font-bold">
                            Admin Login
                        </h2>

                        <p className="mt-2 text-sm text-slate-500">
                            Access dashboard, agent approvals, queue and administrative reports.
                        </p>
                    </Card>
                </Link>

                <Link to="/login/agent">
                    <Card className="h-full transition hover:-translate-y-1 hover:border-emerald-300">
                        <UserRound className="h-10 w-10 text-emerald-600"/>

                        <h2 className="mt-4 text-xl font-bold">
                            Agent Login
                        </h2>

                        <p className="mt-2 text-sm text-slate-500">
                            Access the printing queue, processing, pricing and inventory.
                        </p>
                    </Card>
                </Link>
            </div>
        </div>
    );
}

export function LoginPage({
                              expectedRole
                          }) {
    const navigate =
        useNavigate();

    const {
        login,
        logout
    } = useAuth();

    const [loginId, setLoginId] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [loading, setLoading] =
        useState(false);

    async function submit(event) {
        event.preventDefault();

        if (
            !loginId.trim()
            || !password
        ) {
            toast.error(
                "Login ID and password are required."
            );
            return;
        }

        setLoading(true);

        try {
            const current =
                await login({
                    loginId:
                        loginId.trim(),

                    password
                });

            if (
                !current
                || current.role !== expectedRole
            ) {
                await logout();

                toast.error(
                    expectedRole === "ADMIN"
                        ? "This account is not an administrator account."
                        : "This account is not an agent account."
                );

                return;
            }

            toast.success(
                "Login successful."
            );

            navigate(
                expectedRole === "ADMIN"
                    ? "/admin/dashboard"
                    : "/agent/dashboard",
                {
                    replace: true
                }
            );

        } catch (error) {
            toast.error(
                apiErrorMessage(
                    error,
                    "Login failed."
                )
            );

        } finally {
            setLoading(false);
        }
    }

    const admin =
        expectedRole === "ADMIN";

    return (
        <div className="page-container max-w-lg">
            <PageHeader
                title={
                    admin
                        ? "Admin Login"
                        : "Agent Login"
                }
                description={
                    admin
                        ? "Administrator access only."
                        : "Only approved agents can sign in."
                }
            />

            <Card>
                <form
                    onSubmit={submit}
                    className="space-y-4"
                >
                    <Input
                        label={
                            admin
                                ? "Admin email"
                                : "Agent ID or email"
                        }
                        value={loginId}
                        onChange={
                            event =>
                                setLoginId(
                                    event.target.value
                                )
                        }
                        autoComplete="username"
                    />

                    <Input
                        label="Password"
                        type="password"
                        value={password}
                        onChange={
                            event =>
                                setPassword(
                                    event.target.value
                                )
                        }
                        autoComplete="current-password"
                    />

                    <Button
                        type="submit"
                        className="w-full"
                        disabled={loading}
                    >
                        {loading
                            ? "Signing in..."
                            : "Login"}
                    </Button>
                </form>

                {!admin && (
                    <p className="mt-5 text-center text-sm text-slate-500">
                        Need an agent account?{" "}
                        <Link
                            to="/register-agent"
                            className="font-semibold text-blue-700 hover:underline"
                        >
                            Register here
                        </Link>
                    </p>
                )}
            </Card>
        </div>
    );
}

export function AgentRegistrationPage() {
    const navigate =
        useNavigate();

    const [form, setForm] =
        useState({
            fullName: "",
            agentId: "",
            email: "",
            contactNumber: "",
            password: "",
            confirmPassword: ""
        });

    const [idCard, setIdCard] =
        useState(null);

    const [loading, setLoading] =
        useState(false);

    function change(event) {
        setForm(
            previous => ({
                ...previous,
                [event.target.name]:
                event.target.value
            })
        );
    }

    function validate() {
        if (
            !form.fullName.trim()
            || !form.agentId.trim()
            || !form.email.trim()
            || !form.contactNumber.trim()
        ) {
            return "Complete all required fields.";
        }

        if (
            form.password
            !== form.confirmPassword
        ) {
            return "Password and confirmation do not match.";
        }

        if (
            !/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,72}$/
                .test(form.password)
        ) {
            return "Password needs at least 8 characters with uppercase, lowercase and a digit.";
        }

        if (!idCard) {
            return "ID card upload is required.";
        }

        return null;
    }

    async function submit(event) {
        event.preventDefault();

        const validationError =
            validate();

        if (validationError) {
            toast.error(
                validationError
            );
            return;
        }

        setLoading(true);

        try {
            await authApi.registerAgent(
                {
                    ...form,
                    fullName:
                        form.fullName.trim(),

                    agentId:
                        form.agentId.trim(),

                    email:
                        form.email.trim(),

                    contactNumber:
                        form.contactNumber.trim()
                },
                idCard
            );

            toast.success(
                "Registration submitted. Wait for administrator approval."
            );

            navigate(
                "/login/agent"
            );

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );

        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="page-container max-w-2xl">
            <PageHeader
                title="Agent Registration"
                description="Your account remains pending until approved by the administrator."
            />

            <Card>
                <form
                    onSubmit={submit}
                    className="grid gap-4 sm:grid-cols-2"
                >
                    <Input
                        label="Full name"
                        name="fullName"
                        maxLength={40}
                        value={form.fullName}
                        onChange={change}
                    />

                    <Input
                        label="Agent ID"
                        name="agentId"
                        value={form.agentId}
                        onChange={change}
                    />

                    <Input
                        label="Email"
                        name="email"
                        type="email"
                        value={form.email}
                        onChange={change}
                    />

                    <Input
                        label="Bangladesh contact number"
                        name="contactNumber"
                        value={form.contactNumber}
                        onChange={change}
                        placeholder="01712345678"
                    />

                    <Input
                        label="Password"
                        name="password"
                        type="password"
                        value={form.password}
                        onChange={change}
                    />

                    <Input
                        label="Confirm password"
                        name="confirmPassword"
                        type="password"
                        value={form.confirmPassword}
                        onChange={change}
                    />

                    <label className="block sm:col-span-2">
              <span className="form-label">
                ID Card
              </span>

                        <input
                            type="file"
                            className="form-control"
                            accept=".pdf,.jpg,.jpeg,.png,.heic,.heif"
                            onChange={
                                event =>
                                    setIdCard(
                                        event.target.files?.[0]
                                        || null
                                    )
                            }
                        />
                    </label>

                    <div className="sm:col-span-2">
                        <Button
                            type="submit"
                            disabled={loading}
                        >
                            {loading
                                ? "Submitting..."
                                : "Register Agent"}
                        </Button>
                    </div>
                </form>
            </Card>
        </div>
    );
}

export function ChangePasswordPage() {
    const {
        refreshUser
    } = useAuth();

    const [currentPassword, setCurrentPassword] =
        useState("");

    const [newPassword, setNewPassword] =
        useState("");

    const [confirmPassword, setConfirmPassword] =
        useState("");

    const [loading, setLoading] =
        useState(false);

    async function submit(event) {
        event.preventDefault();

        if (
            newPassword
            !== confirmPassword
        ) {
            toast.error(
                "New password and confirmation do not match."
            );
            return;
        }

        if (
            !/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,72}$/
                .test(newPassword)
        ) {
            toast.error(
                "New password needs uppercase, lowercase and a digit and must contain at least 8 characters."
            );
            return;
        }

        setLoading(true);

        try {
            await authApi.changePassword({
                currentPassword,
                newPassword
            });

            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");

            await refreshUser();

            toast.success(
                "Password changed successfully."
            );

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );

        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="page-container max-w-xl">
            <PageHeader
                title="Profile & Password"
                description="Change your account password securely."
            />

            <Card>
                <form
                    onSubmit={submit}
                    className="space-y-4"
                >
                    <Input
                        label="Current password"
                        type="password"
                        value={currentPassword}
                        onChange={
                            event =>
                                setCurrentPassword(
                                    event.target.value
                                )
                        }
                    />

                    <Input
                        label="New password"
                        type="password"
                        value={newPassword}
                        onChange={
                            event =>
                                setNewPassword(
                                    event.target.value
                                )
                        }
                    />

                    <Input
                        label="Confirm new password"
                        type="password"
                        value={confirmPassword}
                        onChange={
                            event =>
                                setConfirmPassword(
                                    event.target.value
                                )
                        }
                    />

                    <Button
                        type="submit"
                        disabled={loading}
                    >
                        {loading
                            ? "Updating..."
                            : "Change Password"}
                    </Button>
                </form>
            </Card>
        </div>
    );
}