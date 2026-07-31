import { Navigate, Outlet, useLocation } from "react-router";
import { hasValidToken } from "../utils/tokenUtils";

function ProtectedRoute() {
    const location = useLocation();

    if (!hasValidToken()) {
        return (
            <Navigate
                to="/login"
                replace
                state={{ from: location.pathname }}
            />
        );
    }

    return <Outlet />;
}

export default ProtectedRoute;