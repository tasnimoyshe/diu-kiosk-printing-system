import {
    Navigate,
    Route,
    Routes
} from "react-router-dom";

import {
    ProtectedRoute,
    PortalLayout,
    PublicLayout
} from "./components/layouts";

import PrintRequestPage
    from "./pages/PrintRequestPage";

import PublicQueuePage
    from "./pages/PublicQueuePage";

import {
    PaymentPage,
    PayLaterPage,
    TrackRequestPage
} from "./pages/PaymentTrackingPages";

import {
    AgentRegistrationPage,
    ChangePasswordPage,
    LoginPage,
    LoginSelectionPage
} from "./pages/AuthPages";

import {
    AdminDashboardPage,
    AdminQueuePage,
    PendingAgentsPage
} from "./pages/AdminPages";

import {
    AgentDashboardPage,
    AgentQueuePage
} from "./pages/AgentDashboardQueuePages";

import AgentRequestPage
    from "./pages/AgentRequestPage";

import PricingPage
    from "./pages/PricingPage";

import InventoryPage
    from "./pages/InventoryPage";

import {
    NotFoundPage,
    UnauthorizedPage
} from "./pages/SystemPages";

export default function App() {
    return (
        <Routes>

            <Route
                element={<PublicLayout/>}
            >
                <Route
                    index
                    element={<PrintRequestPage/>}
                />

                <Route
                    path="queue"
                    element={<PublicQueuePage/>}
                />

                <Route
                    path="payment/:trackingNumber"
                    element={<PaymentPage/>}
                />

                <Route
                    path="pay-later"
                    element={<PayLaterPage/>}
                />

                <Route
                    path="track"
                    element={<TrackRequestPage/>}
                />

                <Route
                    path="login"
                    element={<LoginSelectionPage/>}
                />

                <Route
                    path="login/admin"
                    element={
                        <LoginPage
                            expectedRole="ADMIN"
                        />
                    }
                />

                <Route
                    path="login/agent"
                    element={
                        <LoginPage
                            expectedRole="AGENT"
                        />
                    }
                />

                <Route
                    path="register-agent"
                    element={<AgentRegistrationPage/>}
                />

                <Route
                    path="unauthorized"
                    element={<UnauthorizedPage/>}
                />
            </Route>

            <Route
                element={
                    <ProtectedRoute
                        roles={["ADMIN"]}
                    />
                }
            >
                <Route
                    element={<PortalLayout/>}
                >
                    <Route
                        path="/admin"
                        element={
                            <Navigate
                                to="/admin/dashboard"
                                replace
                            />
                        }
                    />

                    <Route
                        path="/admin/dashboard"
                        element={<AdminDashboardPage/>}
                    />

                    <Route
                        path="/admin/agents/pending"
                        element={<PendingAgentsPage/>}
                    />

                    <Route
                        path="/admin/queue"
                        element={<AdminQueuePage/>}
                    />

                    <Route
                        path="/admin/profile"
                        element={<ChangePasswordPage/>}
                    />
                </Route>
            </Route>

            <Route
                element={
                    <ProtectedRoute
                        roles={["AGENT"]}
                    />
                }
            >
                <Route
                    element={<PortalLayout/>}
                >
                    <Route
                        path="/agent"
                        element={
                            <Navigate
                                to="/agent/dashboard"
                                replace
                            />
                        }
                    />

                    <Route
                        path="/agent/dashboard"
                        element={<AgentDashboardPage/>}
                    />

                    <Route
                        path="/agent/queue"
                        element={<AgentQueuePage/>}
                    />

                    <Route
                        path="/agent/requests/:requestId"
                        element={<AgentRequestPage/>}
                    />

                    <Route
                        path="/agent/pricing"
                        element={<PricingPage/>}
                    />

                    <Route
                        path="/agent/inventory"
                        element={<InventoryPage/>}
                    />

                    <Route
                        path="/agent/profile"
                        element={<ChangePasswordPage/>}
                    />
                </Route>
            </Route>

            <Route
                path="*"
                element={<NotFoundPage/>}
            />

        </Routes>
    );
}