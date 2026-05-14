import { useState, useEffect, useRef } from "react";
import { Cpu, Mail, Lock, User, Hash, Radio, Loader2, AlertTriangle, ArrowRight, UserPlus, CreditCard } from "lucide-react";
import { authAPI } from "./api";

export default function AuthScreen({ onAuthSuccess }) {
  const [mode, setMode] = useState("login"); // "login" | "register" | "rfid"
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [rfidBuffer, setRfidBuffer] = useState("");
  const rfidTimeout = useRef(null);
  const rfidInputRef = useRef(null);

  // Form states
  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [registerForm, setRegisterForm] = useState({
    nombre: "", apellido: "", email: "", numEstudiante: "", password: "", rfidUid: "",
  });

  // RFID listener (en modo RFID y registro)
  useEffect(() => {
    if (mode === "rfid" && rfidInputRef.current) {
      rfidInputRef.current.focus();
    }
  }, [mode]);

  // Captura global del teclado para detectar lectura de RFID rápida
  // (los lectores RFID USB emiten UID + Enter en milisegundos)
  useEffect(() => {
    if (mode !== "rfid") return;

    let buffer = "";
    let lastKeyTime = Date.now();

    const handler = (e) => {
      const now = Date.now();
      // Si pasaron más de 100ms entre teclas, es input humano - reset
      if (now - lastKeyTime > 100) buffer = "";
      lastKeyTime = now;

      if (e.key === "Enter") {
        if (buffer.length >= 4) {
          handleRfidLogin(buffer);
          buffer = "";
        }
      } else if (e.key.length === 1) {
        buffer += e.key;
      }
    };

    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [mode]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true); setError(null);
    try {
      const result = await authAPI.login(loginForm.email, loginForm.password);
      saveSession(result);
    } catch (err) { setError(err.message); }
    finally { setLoading(false); }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true); setError(null);
    try {
      const result = await authAPI.register(registerForm);
      saveSession(result);
    } catch (err) { setError(err.message); }
    finally { setLoading(false); }
  };

  const handleRfidLogin = async (uid) => {
    setLoading(true); setError(null);
    try {
      const result = await authAPI.loginRfid(uid);
      saveSession(result);
    } catch (err) { setError("Tarjeta no reconocida. ¿Ya está registrada?"); }
    finally { setLoading(false); }
  };

  const saveSession = (result) => {
    localStorage.setItem("ms_token", result.token);
    localStorage.setItem("ms_user", JSON.stringify(result.user));
    onAuthSuccess(result.user);
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-logo">
          <div className="logo-icon"><Cpu size={28} /></div>
          <div>
            <span className="logo-text">mind<span className="logo-accent">stock</span></span>
            <div className="logo-sub">IBERO CDMX</div>
          </div>
        </div>

        <div className="auth-tabs">
          <button className={`auth-tab ${mode === "login" ? "active" : ""}`}
            onClick={() => { setMode("login"); setError(null); }}>
            <Mail size={14} /> Email
          </button>
          <button className={`auth-tab ${mode === "rfid" ? "active" : ""}`}
            onClick={() => { setMode("rfid"); setError(null); }}>
            <CreditCard size={14} /> RFID
          </button>
          <button className={`auth-tab ${mode === "register" ? "active" : ""}`}
            onClick={() => { setMode("register"); setError(null); }}>
            <UserPlus size={14} /> Registro
          </button>
        </div>

        {error && (
          <div className="auth-error">
            <AlertTriangle size={14} /> {error}
          </div>
        )}

        {/* LOGIN */}
        {mode === "login" && (
          <form onSubmit={handleLogin} className="auth-form">
            <div className="auth-field">
              <Mail size={16} className="auth-field-icon" />
              <input type="email" placeholder="Correo electrónico" required
                value={loginForm.email}
                onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })} />
            </div>
            <div className="auth-field">
              <Lock size={16} className="auth-field-icon" />
              <input type="password" placeholder="Contraseña" required
                value={loginForm.password}
                onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })} />
            </div>
            <button className="auth-submit" disabled={loading}>
              {loading ? <Loader2 size={16} className="spin" /> : <>Ingresar <ArrowRight size={16} /></>}
            </button>
            <div className="auth-hint">
              💡 Demo: <code>carlos.mendez@ibero.mx</code> / <code>mindstock2026</code>
            </div>
          </form>
        )}

        {/* RFID */}
        {mode === "rfid" && (
          <div className="auth-rfid">
            <div className="rfid-pulse">
              <Radio size={48} />
            </div>
            <div className="rfid-title">Acerca tu tarjeta al lector</div>
            <div className="rfid-subtitle">
              {loading ? "Verificando..." : "Esperando lectura..."}
            </div>
            <input ref={rfidInputRef} type="text" className="rfid-hidden-input" />
          </div>
        )}

        {/* REGISTRO */}
        {mode === "register" && (
          <form onSubmit={handleRegister} className="auth-form">
            <div className="auth-row">
              <div className="auth-field">
                <User size={16} className="auth-field-icon" />
                <input type="text" placeholder="Nombre" required
                  value={registerForm.nombre}
                  onChange={(e) => setRegisterForm({ ...registerForm, nombre: e.target.value })} />
              </div>
              <div className="auth-field">
                <input type="text" placeholder="Apellido" required
                  value={registerForm.apellido}
                  onChange={(e) => setRegisterForm({ ...registerForm, apellido: e.target.value })} />
              </div>
            </div>
            <div className="auth-field">
              <Mail size={16} className="auth-field-icon" />
              <input type="email" placeholder="Correo Ibero" required
                value={registerForm.email}
                onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })} />
            </div>
            <div className="auth-field">
              <Hash size={16} className="auth-field-icon" />
              <input type="text" placeholder="Número de estudiante" required
                value={registerForm.numEstudiante}
                onChange={(e) => setRegisterForm({ ...registerForm, numEstudiante: e.target.value })} />
            </div>
            <div className="auth-field">
              <Lock size={16} className="auth-field-icon" />
              <input type="password" placeholder="Contraseña (mín. 6 caracteres)" required minLength={6}
                value={registerForm.password}
                onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })} />
            </div>
            <div className="auth-field">
              <CreditCard size={16} className="auth-field-icon" />
              <input type="text" placeholder="UID de tarjeta RFID (opcional)"
                value={registerForm.rfidUid}
                onChange={(e) => setRegisterForm({ ...registerForm, rfidUid: e.target.value })} />
            </div>
            <button className="auth-submit" disabled={loading}>
              {loading ? <Loader2 size={16} className="spin" /> : <>Crear cuenta <ArrowRight size={16} /></>}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}