import {
    Link
} from "react-router-dom";

import {
    Button,
    Card
} from "../components/ui";

export function UnauthorizedPage() {
    return (
        <div className="page-container max-w-lg py-20">
            <Card className="text-center">
                <div className="text-6xl">
                    403
                </div>

                <h1 className="mt-4 text-2xl font-bold">
                    Access denied
                </h1>

                <p className="mt-2 text-sm text-slate-500">
                    Your account does not have permission to access this page.
                </p>

                <Link to="/">
                    <Button className="mt-6">
                        Go Home
                    </Button>
                </Link>
            </Card>
        </div>
    );
}

export function NotFoundPage() {
    return (
        <div className="page-container max-w-lg py-20">
            <Card className="text-center">
                <div className="text-6xl">
                    404
                </div>

                <h1 className="mt-4 text-2xl font-bold">
                    Page not found
                </h1>

                <p className="mt-2 text-sm text-slate-500">
                    The page you requested does not exist.
                </p>

                <Link to="/">
                    <Button className="mt-6">
                        Go Home
                    </Button>
                </Link>
            </Card>
        </div>
    );
}