import { useCallback, useEffect, useState } from "react";
import { transactionService } from "../services/transactionService";
import { encryptSecret } from "../utils/encryptionUtils";
import "./TransactionsPage.css";
import { getAuthenticatedUsername, logout } from "../services/authService";

const INITIAL_FORM = {
    operation: "",
    amount: "",
    customer: "",
    secret: "",
};

const SORTABLE_COLUMNS = [
    {
        label: "ID",
        field: "id",
    },
    {
        label: "Operación",
        field: "operation",
    },
    {
        label: "Referencia",
        field: "reference",
    },
    {
        label: "Estatus",
        field: "status",
    },
];

function TransactionsPage() {
    const [transactions, setTransactions] = useState([]);
    const [form, setForm] = useState(INITIAL_FORM);

    const [page, setPage] = useState(0);
    const [size, setSize] = useState(5);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const [sortBy, setSortBy] = useState("createdAt");
    const [direction, setDirection] = useState("desc");

    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [cancellingId, setCancellingId] = useState(null);

    const [successMessage, setSuccessMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    const username = getAuthenticatedUsername() || "usuario";

    const loadTransactions = useCallback(async () => {
        try {
            setLoading(true);
            setErrorMessage("");

            const response = await transactionService.getTransactions({
                page,
                size,
                sortBy,
                direction,
            });

            const pageData = response.data;

            setTransactions(pageData.content ?? []);
            setTotalPages(pageData.totalPages ?? 0);
            setTotalElements(pageData.totalElements ?? 0);
        } catch (error) {
            setErrorMessage(
                error.response?.data?.message ||
                "No fue posible obtener las transacciones."
            );
        } finally {
            setLoading(false);
        }
    }, [page, size, sortBy, direction]);

    useEffect(() => {
        loadTransactions();
    }, [loadTransactions]);

    const handleInputChange = (event) => {
        const { name, value } = event.target;

        setForm((currentForm) => ({
            ...currentForm,
            [name]: value,
        }));
    };

    const handleSort = (field) => {
        if (sortBy === field) {
            setDirection((currentDirection) =>
                currentDirection === "asc" ? "desc" : "asc"
            );
        } else {
            setSortBy(field);
            setDirection("asc");
        }

        setPage(0);
    };

    const getSortIcon = (field) => {
        if (sortBy !== field) {
            return "↕";
        }

        return direction === "asc" ? "↑" : "↓";
    };

    const validateForm = () => {
        if (!form.operation.trim()) {
            return "La operación es obligatoria.";
        }

        if (!form.amount) {
            return "El importe es obligatorio.";
        }

        if (Number(form.amount) <= 0) {
            return "El importe debe ser mayor que cero.";
        }

        if (!form.customer.trim()) {
            return "El cliente es obligatorio.";
        }

        if (!form.secret.trim()) {
            return "El secreto es obligatorio.";
        }

        return null;
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        const validationMessage = validateForm();

        if (validationMessage) {
            setErrorMessage(validationMessage);
            setSuccessMessage("");
            return;
        }

        try {
            setSaving(true);
            setErrorMessage("");
            setSuccessMessage("");

            const encryptedSecret = await encryptSecret(form.secret);

            const request = {
                operacion: form.operation.trim(),
                importe: Number(form.amount),
                cliente: form.customer.trim(),
                secreto: encryptedSecret,
            };

            const response =
                await transactionService.createTransaction(request);

            setSuccessMessage(
                response.message ||
                "Transacción registrada correctamente."
            );

            setForm(INITIAL_FORM);

            /*
             * Regresamos a la primera página para que sea más probable
             * que la transacción recién creada aparezca de inmediato.
             */
            if (page !== 0) {
                setPage(0);
            } else {
                await loadTransactions();
            }
        } catch (error) {
            setErrorMessage(
                error.response?.data?.message ||
                "No fue posible registrar la transacción."
            );
        } finally {
            setSaving(false);
        }
    };

    const handleCancelTransaction = async (transaction) => {
        const confirmed = window.confirm(
            `¿Deseas cancelar la transacción con referencia ${transaction.referencia}?`
        );

        if (!confirmed) {
            return;
        }

        try {
            setCancellingId(transaction.id);
            setErrorMessage("");
            setSuccessMessage("");

            const response =
                await transactionService.cancelTransaction({
                    id: transaction.id,
                    reference: transaction.referencia,
                });

            setSuccessMessage(
                response.message ||
                "Transacción cancelada correctamente."
            );

            await loadTransactions();
        } catch (error) {
            setErrorMessage(
                error.response?.data?.message ||
                "No fue posible cancelar la transacción."
            );
        } finally {
            setCancellingId(null);
        }
    };

    const handlePreviousPage = () => {
        setPage((currentPage) =>
            Math.max(currentPage - 1, 0)
        );
    };

    const handleNextPage = () => {
        setPage((currentPage) =>
            Math.min(currentPage + 1, totalPages - 1)
        );
    };

    const handleLogout = () => {
        logout();
        window.location.replace("/login");
    };

    const handleSizeChange = (event) => {
        const selectedSize = event.target.value;

        setPage(0);

        if (selectedSize === "all") {
            setSize(Math.max(totalElements, 1));
        } else {
            setSize(Number(selectedSize));
        }
    };

    const isCancelled = (status) =>
        status?.toLowerCase() === "cancelada";

    return (
        <main className="transactions-page">
            <header className="transactions-header">
                <div>
                    <h1>Transacciones</h1>
                    <p>
                        Sesión iniciada como{" "}
                        <strong>{username}</strong>
                    </p>
                </div>

                <button
                    type="button"
                    className="logout-button"
                    onClick={handleLogout}
                >
                    Cerrar sesión
                </button>
            </header>

            <section className="transactions-card">
                <div className="section-heading">
                    <div>
                        <h2>Transacciones registradas</h2>
                    </div>

                    <button
                        type="button"
                        className="refresh-button"
                        onClick={loadTransactions}
                        disabled={loading}
                    >
                        {loading ? "Actualizando..." : "Actualizar"}
                    </button>
                </div>

                {successMessage && (
                    <div className="alert alert-success">
                        {successMessage}
                    </div>
                )}

                {errorMessage && (
                    <div className="alert alert-error">
                        {errorMessage}
                    </div>
                )}

                <div className="table-container">
                    <table className="transactions-table">
                        <thead>
                            <tr>
                                {SORTABLE_COLUMNS.map((column) => (
                                    <th key={column.field}>
                                        <button
                                            type="button"
                                            className="sort-button"
                                            onClick={() =>
                                                handleSort(column.field)
                                            }
                                        >
                                            {column.label}
                                            <span>
                                                {getSortIcon(column.field)}
                                            </span>
                                        </button>
                                    </th>
                                ))}

                                <th>Acciones</th>
                            </tr>
                        </thead>

                        <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan="5" className="table-message">
                                        Cargando transacciones...
                                    </td>
                                </tr>
                            ) : transactions.length === 0 ? (
                                <tr>
                                    <td colSpan="5" className="table-message">
                                        No existen transacciones registradas.
                                    </td>
                                </tr>
                            ) : (
                                transactions.map((transaction) => (
                                    <tr key={transaction.id}>
                                        <td>{transaction.id}</td>
                                        <td>{transaction.operacion}</td>
                                        <td>{transaction.referencia}</td>
                                        <td>
                                            <span
                                                className={`status-badge ${isCancelled(transaction.estatus)
                                                    ? "status-cancelled"
                                                    : "status-approved"
                                                    }`}
                                            >
                                                {transaction.estatus}
                                            </span>
                                        </td>
                                        <td>
                                            <button
                                                type="button"
                                                className="cancel-button"
                                                disabled={
                                                    isCancelled(transaction.estatus) ||
                                                    cancellingId === transaction.id
                                                }
                                                onClick={() =>
                                                    handleCancelTransaction(transaction)
                                                }
                                            >
                                                {cancellingId === transaction.id
                                                    ? "Cancelando..."
                                                    : isCancelled(transaction.estatus)
                                                        ? "Cancelada"
                                                        : "Cancelar"}
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                <div className="pagination-container">
                    <div className="page-size-selector">
                        <label htmlFor="page-size">
                            Elementos por página:
                        </label>

                        <select
                            id="page-size"
                            value={
                                size >= totalElements && totalElements > 15
                                    ? "all"
                                    : size
                            }
                            onChange={handleSizeChange}
                            disabled={loading}
                        >
                            <option value={5}>5</option>
                            <option value={10}>10</option>
                            <option value={15}>15</option>
                            <option value="all">Todos</option>
                        </select>
                    </div>

                    <div className="pagination">
                        <button
                            type="button"
                            onClick={() => setPage(0)}
                            disabled={page === 0 || loading}
                            aria-label="Primera página"
                        >
                            «
                        </button>

                        <button
                            type="button"
                            onClick={handlePreviousPage}
                            disabled={page === 0 || loading}
                        >
                            Anterior
                        </button>

                        <span>
                            Página {totalPages === 0 ? 0 : page + 1} de{" "}
                            {totalPages}
                        </span>

                        <button
                            type="button"
                            onClick={handleNextPage}
                            disabled={
                                totalPages === 0 ||
                                page >= totalPages - 1 ||
                                loading
                            }
                        >
                            Siguiente
                        </button>

                        <button
                            type="button"
                            onClick={() => setPage(Math.max(totalPages - 1, 0))}
                            disabled={
                                totalPages === 0 ||
                                page >= totalPages - 1 ||
                                loading
                            }
                            aria-label="Última página"
                        >
                            »
                        </button>
                    </div>

                    <div className="pagination-summary">
                        {totalElements}{" "}
                        {totalElements === 1
                            ? "transacción"
                            : "transacciones"}
                    </div>
                </div>
            </section>

            <section className="transactions-card">
                <h2>Registrar transacción</h2>

                <form
                    className="transaction-form"
                    onSubmit={handleSubmit}
                >
                    <div className="form-group">
                        <label htmlFor="operation">
                            Operación
                        </label>

                        <input
                            id="operation"
                            name="operation"
                            type="text"
                            value={form.operation}
                            onChange={handleInputChange}
                            placeholder="Ej. venta"
                            maxLength={20}
                            disabled={saving}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="amount">
                            Importe
                        </label>

                        <input
                            id="amount"
                            name="amount"
                            type="number"
                            value={form.amount}
                            onChange={handleInputChange}
                            placeholder="100.00"
                            min="0.01"
                            step="0.01"
                            disabled={saving}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="customer">
                            Cliente
                        </label>

                        <input
                            id="customer"
                            name="customer"
                            type="text"
                            value={form.customer}
                            onChange={handleInputChange}
                            placeholder="Ej. Angel"
                            maxLength={60}
                            disabled={saving}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="secret">
                            Secreto
                        </label>

                        <input
                            id="secret"
                            name="secret"
                            type="password"
                            value={form.secret}
                            onChange={handleInputChange}
                            placeholder="Ingresa el secreto"
                            maxLength={255}
                            autoComplete="off"
                            disabled={saving}
                        />
                    </div>

                    <div className="form-actions">
                        <button
                            type="submit"
                            className="save-button"
                            disabled={saving}
                        >
                            {saving
                                ? "Guardando..."
                                : "Guardar transacción"}
                        </button>
                    </div>
                </form>
            </section>
        </main>
    );
}

export default TransactionsPage;