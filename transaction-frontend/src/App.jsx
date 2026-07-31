import { Navigate, Route, Routes } from "react-router";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import TransactionsPage from "./pages/TransactionsPage";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route
          path="/transactions"
          element={<TransactionsPage />}
        />
      </Route>

      <Route
        path="/"
        element={<Navigate to="/transactions" replace />}
      />

      <Route
        path="*"
        element={<Navigate to="/transactions" replace />}
      />
    </Routes>
  );
}

export default App;