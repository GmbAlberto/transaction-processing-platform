import { useEffect, useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router";
import { login } from "../services/authService";
import { hasValidToken } from "../utils/tokenUtils";

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const tokenIsValid = hasValidToken();

  useEffect(() => {
    setErrorMessage("");
  }, [username, password]);

  if (tokenIsValid) {
    return <Navigate to="/transactions" replace />;
  }

  async function handleSubmit(event) {
    event.preventDefault();

    const normalizedUsername = username.trim();

    if (!normalizedUsername || !password) {
      setErrorMessage("Ingresa el usuario y la contraseña");
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage("");

      await login(normalizedUsername, password);

      const destination = location.state?.from || "/transactions";

      navigate(destination, {
        replace: true,
      });
    } catch (error) {
      const serverMessage = error.response?.data?.message;

      if (!error.response) {
        setErrorMessage(
          "No fue posible comunicarse con el servidor",
        );
      } else if (error.response.status === 401) {
        setErrorMessage("Usuario o contraseña incorrectos");
      } else {
        setErrorMessage(
          serverMessage || "No fue posible iniciar sesión",
        );
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="page-container">
      <section className="login-card">
        <h1>Iniciar sesión</h1>

        <p className="page-description">
          Es necesario ingresar tus credenciales.
        </p>

        <form onSubmit={handleSubmit}>
          <div className="form-field">
            <label htmlFor="username">Usuario</label>

            <input
              id="username"
              name="username"
              type="text"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              maxLength={50}
              disabled={isSubmitting}
            />
          </div>

          <div className="form-field">
            <label htmlFor="password">Contraseña</label>

            <input
              id="password"
              name="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              maxLength={100}
              disabled={isSubmitting}
            />
          </div>

          {errorMessage && (
            <div className="error-message" role="alert">
              {errorMessage}
            </div>
          )}

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Iniciando sesión..." : "Ingresar"}
          </button>
        </form>
      </section>
    </main>
  );
}

export default LoginPage;