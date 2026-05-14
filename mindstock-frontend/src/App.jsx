import { useState, useEffect, useRef } from "react";
import {
  Search, Package, Camera, Bell, CheckCircle, Clock,
  ArrowRight, Eye, Cpu, Scan, Users, Plus, X, Box,
  Zap, AlertTriangle, ChevronDown, Undo2, Loader2,
  RefreshCw, Wifi, WifiOff
} from "lucide-react";
import { LogOut } from "lucide-react";
import { itemsAPI, usersAPI, loansAPI, categoriesAPI, visionAPI } from "./api";
import AuthScreen from "./AuthScreen";
import "./App.css";

const StatusBadge = ({ status }) => {
  const config = {
    DISPONIBLE: { label: "Disponible", className: "badge-green" },
    AGOTADO: { label: "Agotado", className: "badge-amber" },
    MANTENIMIENTO: { label: "Mantenimiento", className: "badge-red" },
  };
  const c = config[status] || config.DISPONIBLE;
  return <span className={`badge ${c.className}`}>{c.label}</span>;
};

const LoanStatusBadge = ({ status }) => {
  const config = {
    PENDIENTE: { label: "Pendiente", className: "badge-amber" },
    APROBADA: { label: "Aprobada", className: "badge-green" },
    RECHAZADA: { label: "Rechazada", className: "badge-red" },
    ACTIVO: { label: "Activo", className: "badge-amber" },
    DEVUELTO: { label: "Devuelto", className: "badge-green" },
    VENCIDO: { label: "Vencido", className: "badge-red" },
  };
  const c = config[status] || { label: status, className: "badge-amber" };
  return <span className={`badge ${c.className}`}>{c.label}</span>;
};

function ScanModal({ isOpen, onClose, onLoanCreated, currentUser }) {
  const [phase, setPhase] = useState("idle");
  const [detection, setDetection] = useState(null);
  const [error, setError] = useState(null);
  const [cameraReady, setCameraReady] = useState(false);

  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      setPhase("idle");
      setDetection(null);
      setError(null);
      startCamera();
    } else {
      stopCamera();
    }
    return () => stopCamera();
  }, [isOpen]);

  const startCamera = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { width: { ideal: 1280 }, height: { ideal: 720 }, facingMode: "environment" },
      });
      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        videoRef.current.onloadeddata = () => setCameraReady(true);
      }
    } catch (err) {
      setError("No se pudo acceder a la cámara. Verifica los permisos del navegador.");
    }
  };

  const stopCamera = () => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }
    setCameraReady(false);
  };

  const captureAndDetect = async () => {
  setPhase("scanning");
  setError(null);

  const video = videoRef.current;
  const canvas = canvasRef.current;
  canvas.width = video.videoWidth;
  canvas.height = video.videoHeight;
  canvas.getContext("2d").drawImage(video, 0, 0);
  const blob = await new Promise((resolve) => canvas.toBlob(resolve, "image/jpeg", 0.85));

  try {
    const result = await visionAPI.detectFrame(blob);
    if (result.best) {
      setDetection(result.best);
      setPhase("detected");
    }
  } catch (err) {
    // Diferenciar entre "no detectó" y "error de conexión"
    if (err.message.includes("No se detectó")) {
      setError("No se detectó ningún objeto. Acerca el objeto a la cámara e intenta de nuevo.");
      setPhase("idle");
    } else if (err.message.includes("incierta")) {
      setError("Detección incierta. Reposiciona el objeto e intenta de nuevo.");
      setPhase("idle");
    } else {
      // Solo en error real de conexión, simular
      try {
        const simResult = await visionAPI.simulateDetection();
        setDetection(simResult);
        setPhase("detected");
      } catch (simErr) {
        setError("No se pudo conectar con el módulo de visión.");
        setPhase("idle");
      }
    }
  }
};

  const confirmLoan = async () => {
    setPhase("confirming");
    setError(null);
    try {
      await visionAPI.confirmLoan({
        user_id: currentUser.id,
        item_id: detection.item_id,
        cantidad: 1,
        confidence_score: detection.confidence,
      });
      setPhase("done");
      setTimeout(() => { onLoanCreated(); onClose(); }, 1500);
    } catch (err) {
      setError(err.message);
      setPhase("detected");
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="scan-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-header-left">
            <div className="modal-icon"><Camera size={18} /></div>
            <div>
              <div className="modal-title">MindStock Vision</div>
              <div className="modal-subtitle">{cameraReady ? "Cámara activa" : "Iniciando cámara..."}</div>
            </div>
          </div>
          <button className="btn-icon" onClick={onClose}><X size={16} /></button>
        </div>

        <div className="scan-viewport">
          <video ref={videoRef} autoPlay playsInline muted style={{ position: "absolute", inset: 0, width: "100%", height: "100%", objectFit: "cover", borderRadius: "12px", opacity: cameraReady ? 1 : 0, transition: "opacity 0.5s" }} />
          <canvas ref={canvasRef} style={{ display: "none" }} />
          <div className="scan-grid" />
          {phase === "scanning" && <div className="scan-line" />}
          <div className="scan-target-wrapper">
            <div className={`scan-target ${phase === "detected" || phase === "done" ? "detected" : ""}`}>
              <div className="scan-corner tl" />
              <div className="scan-corner tr" />
              <div className="scan-corner bl" />
              <div className="scan-corner br" />
              {!cameraReady && <div className="scan-center-content"><Loader2 size={36} className="spin scan-scanning-icon" /></div>}
            </div>
          </div>
          <div className="scan-status">
            <div className={`scan-dot ${!cameraReady ? "red" : phase === "scanning" ? "red" : phase === "detected" || phase === "done" ? "green" : "gray"}`} />
            <span className="scan-status-text">
              {!cameraReady && "Iniciando cámara..."}
              {cameraReady && phase === "idle" && "Coloca el objeto frente a la cámara"}
              {phase === "scanning" && "Analizando con YOLOv8..."}
              {phase === "detected" && `${detection.item_name} — ${(detection.confidence * 100).toFixed(1)}%`}
              {phase === "confirming" && "Registrando préstamo..."}
              {phase === "done" && "Solicitud creada exitosamente"}
            </span>
          </div>
        </div>

        {detection && phase !== "idle" && phase !== "scanning" && (
          <div className={`scan-result ${phase === "done" ? "scan-result-done" : ""}`}>
            <div className="scan-result-header">
              <span className="scan-result-label">{phase === "done" ? "Solicitud creada" : "Objeto identificado"}</span>
              <span className="scan-confidence">{(detection.confidence * 100).toFixed(1)}%</span>
            </div>
            <div className="scan-result-name">{detection.item_name}</div>
            <div className="scan-result-meta">Detectado por AI Vision</div>
          </div>
        )}

        {error && <div className="scan-error"><AlertTriangle size={14} />{error}</div>}

        <div className="scan-actions">
          {phase === "idle" && (
            <button className="btn-scan-start" onClick={captureAndDetect} disabled={!cameraReady}>
              {cameraReady ? <><Scan size={18} /> Escanear objeto</> : <><Loader2 size={18} className="spin" /> Iniciando cámara...</>}
            </button>
          )}
          {phase === "scanning" && <button className="btn-scan-scanning" disabled><Loader2 size={16} className="spin" /> Analizando con YOLO...</button>}
          {phase === "detected" && (
            <div className="scan-actions-row">
              <button className="btn-rescan" onClick={captureAndDetect}><RefreshCw size={14} /> Re-escanear</button>
              <button className="btn-confirm-loan" onClick={confirmLoan}><CheckCircle size={14} /> Confirmar préstamo</button>
            </div>
          )}
          {phase === "confirming" && <button className="btn-scan-scanning" disabled><Loader2 size={16} className="spin" /> Registrando...</button>}
          {phase === "done" && <button className="btn-scan-done"><CheckCircle size={16} /> ¡Listo!</button>}
        </div>
      </div>
    </div>
  );
}

function NewRequestModal({ isOpen, onClose, onCreated }) {
  const [users, setUsers] = useState([]);
  const [items, setItems] = useState([]);
  const [form, setForm] = useState({ userId: "", itemId: "", cantidad: 1, detectedBy: "MANUAL", confidenceScore: null });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen) {
      usersAPI.getByRol("ALUMNO").then(setUsers);
      itemsAPI.getDisponibles().then(setItems);
    }
  }, [isOpen]);

  const handleSubmit = async () => {
    if (!form.userId || !form.itemId) { setError("Selecciona un alumno y un item"); return; }
    setLoading(true); setError(null);
    try {
      await loansAPI.createRequest({ ...form, userId: Number(form.userId), itemId: Number(form.itemId) });
      onCreated(); onClose();
      setForm({ userId: "", itemId: "", cantidad: 1, detectedBy: "MANUAL", confidenceScore: null });
    } catch (err) { setError(err.message); } finally { setLoading(false); }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-header-left">
            <div className="modal-icon modal-icon-secondary"><Plus size={18} /></div>
            <div><div className="modal-title">Solicitud manual</div><div className="modal-subtitle">Registro sin cámara</div></div>
          </div>
          <button className="btn-icon" onClick={onClose}><X size={16} /></button>
        </div>
        <div className="modal-body">
          {error && <div className="error-msg">{error}</div>}
          <label className="field-label">Alumno</label>
          <select className="field-input" value={form.userId} onChange={(e) => setForm({ ...form, userId: e.target.value })}>
            <option value="">Seleccionar alumno...</option>
            {users.map((u) => <option key={u.id} value={u.id}>{u.nombre} {u.apellido} ({u.numEstudiante})</option>)}
          </select>
          <label className="field-label">Item a prestar</label>
          <select className="field-input" value={form.itemId} onChange={(e) => setForm({ ...form, itemId: e.target.value })}>
            <option value="">Seleccionar item...</option>
            {items.map((i) => <option key={i.id} value={i.id}>{i.nombre} ({i.cantidadDisponible} disponibles)</option>)}
          </select>
          <label className="field-label">Cantidad</label>
          <input type="number" className="field-input" min="1" value={form.cantidad} onChange={(e) => setForm({ ...form, cantidad: Number(e.target.value) })} />
        </div>
        <div className="modal-footer">
          <button className="btn-secondary" onClick={onClose}>Cancelar</button>
          <button className="btn-primary" onClick={handleSubmit} disabled={loading}>
            {loading ? <><Loader2 size={14} className="spin" /> Creando...</> : "Crear solicitud"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [authChecked, setAuthChecked] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem("ms_user");
    if (saved) {
      try { setCurrentUser(JSON.parse(saved)); }
      catch { localStorage.clear(); }
    }
    setAuthChecked(true);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("ms_token");
    localStorage.removeItem("ms_user");
    setCurrentUser(null);
  };

  const [activeTab, setActiveTab] = useState("inventory");
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("Todos");
  const [showScan, setShowScan] = useState(false);
  const [showManual, setShowManual] = useState(false);
  const [visionOnline, setVisionOnline] = useState(false);

  const [items, setItems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [stats, setStats] = useState(null);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [activeLoans, setActiveLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);
  const [notification, setNotification] = useState(null);

  useEffect(() => {
    if (!currentUser) return;
    loadData();
    checkVision();
    const interval = setInterval(checkVision, 15000);
    return () => clearInterval(interval);
  }, [currentUser]);

  if (!authChecked) return null;
  if (!currentUser) {
    return <AuthScreen onAuthSuccess={setCurrentUser} />;
  }

  const isLab = currentUser.rol === "LABORATORISTA" || currentUser.rol === "ADMIN";

  async function checkVision() {
    try { await visionAPI.getStatus(); setVisionOnline(true); } catch { setVisionOnline(false); }
  }

  async function loadData() {
    setLoading(true);
    try {
      const [itemsData, catsData, statsData, requestsData, loansData] = await Promise.all([
        itemsAPI.getAll(), categoriesAPI.getAll(), itemsAPI.getStats(), loansAPI.getPendingRequests(), loansAPI.getActiveLoans(),
      ]);
      setItems(itemsData); setCategories(catsData); setStats(statsData); setPendingRequests(requestsData); setActiveLoans(loansData);
    } catch (err) { showNotification("Error cargando datos: " + err.message, "error"); } finally { setLoading(false); }
  }

  function showNotification(msg, type = "success") { setNotification({ msg, type }); setTimeout(() => setNotification(null), 3000); }

  const filteredItems = items.filter((item) => {
    const matchesSearch = item.nombre.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCat = selectedCategory === "Todos" || item.categoryNombre === selectedCategory;
    return matchesSearch && matchesCat;
  });

  async function handleApprove(requestId) {
    setActionLoading(requestId);
    try { await loansAPI.approveRequest(requestId, currentUser.id); showNotification("Solicitud aprobada correctamente"); loadData(); } catch (err) { showNotification(err.message, "error"); } finally { setActionLoading(null); }
  }
  async function handleReject(requestId) {
    setActionLoading(requestId);
    try { await loansAPI.rejectRequest(requestId); showNotification("Solicitud rechazada"); loadData(); } catch (err) { showNotification(err.message, "error"); } finally { setActionLoading(null); }
  }
  async function handleReturn(loanId) {
    setActionLoading(`loan-${loanId}`);
    try { await loansAPI.returnLoan(loanId); showNotification("Devolución registrada correctamente"); loadData(); } catch (err) { showNotification(err.message, "error"); } finally { setActionLoading(null); }
  }

  if (loading) {
    return (<div className="loading-screen"><div className="loading-logo"><Cpu size={32} /></div><div className="loading-text">Cargando MindStock...</div></div>);
  }

  return (
    <div className="app">
      {notification && (<div className={`notification ${notification.type}`}>{notification.type === "success" ? <CheckCircle size={16} /> : <AlertTriangle size={16} />}{notification.msg}</div>)}

      <nav className="navbar">
        <div className="navbar-inner">
          <div className="logo">
            <div className="logo-icon"><Cpu size={20} /></div>
            <div><span className="logo-text">mind<span className="logo-accent">stock</span></span><div className="logo-sub">IBERO CDMX</div></div>
          </div>
          <div className="nav-tabs">
            {[
              { key: "inventory", label: "Inventario", icon: Package },
              ...(isLab ? [{ key: "requests", label: "Solicitudes", icon: Bell, count: pendingRequests.length }] : []),
              { key: "loans", label: "Préstamos", icon: Clock, count: activeLoans.length },
            ].map((tab) => (
              <button key={tab.key} className={`nav-tab ${activeTab === tab.key ? "active" : ""}`} onClick={() => setActiveTab(tab.key)}>
                <tab.icon size={15} />{tab.label}{tab.count > 0 && <span className="nav-count">{tab.count}</span>}
              </button>
            ))}
          </div>
          <div className="nav-right">
            <div className={`vision-status ${visionOnline ? "online" : "offline"}`}>
              {visionOnline ? <Wifi size={13} /> : <WifiOff size={13} />}<span>Vision {visionOnline ? "ON" : "OFF"}</span>
            </div>
            <button className="btn-scan" onClick={() => setShowScan(true)}><Scan size={16} /> Escanear</button>
            <div className="user-chip">
              <span className="user-name">{currentUser.nombre}</span>
              <button className="user-logout" onClick={handleLogout} title="Cerrar sesión">
                <LogOut size={14} />
              </button>
            </div>
            {isLab && <button className="btn-manual" onClick={() => setShowManual(true)}><Plus size={16} /> Manual</button>}
          </div>
        </div>
      </nav>

      <main className="main">
        {stats && (
          <div className="stats-grid">
            {[
              { label: "Total artículos", value: stats.totalItems, icon: Box, color: "red", sub: "en inventario" },
              { label: "Disponibles", value: stats.itemsDisponibles, icon: CheckCircle, color: "green", sub: "listos para préstamo" },
              { label: "En préstamo", value: stats.itemsPrestados, icon: Users, color: "amber", sub: "con items prestados" },
              { label: "Solicitudes", value: stats.solicitudesPendientes, icon: Bell, color: "red", sub: "pendientes" },
            ].map((stat, i) => (
              <div key={i} className="stat-card" style={{ animationDelay: `${i * 0.08}s` }}>
                <div className="stat-header"><span className="stat-label">{stat.label}</span><div className={`stat-icon stat-icon-${stat.color}`}><stat.icon size={16} /></div></div>
                <div className="stat-value">{stat.value}</div><div className="stat-sub">{stat.sub}</div>
              </div>
            ))}
          </div>
        )}

        {activeTab === "inventory" && (
          <div className="fade-in">
            <div className="toolbar">
              <div className="search-box"><Search size={16} className="search-icon" /><input type="text" placeholder="Buscar en inventario..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} /></div>
              <div className="filter-pills">
                <button className={`pill ${selectedCategory === "Todos" ? "active" : ""}`} onClick={() => setSelectedCategory("Todos")}>Todos</button>
                {categories.map((cat) => (<button key={cat.id} className={`pill ${selectedCategory === cat.nombre ? "active" : ""}`} onClick={() => setSelectedCategory(cat.nombre)}>{cat.nombre}</button>))}
              </div>
            </div>
            <div className="items-grid">
              {filteredItems.map((item, i) => (
                <div key={item.id} className="item-card" style={{ animationDelay: `${i * 0.04}s` }}>
                  <div className="item-top"><div className="item-emoji">📦</div><StatusBadge status={item.status} /></div>
                  <div className="item-name">{item.nombre}</div>
                  <div className="item-meta"><span>{item.categoryNombre}</span><span className="dot" /><span>{item.labNombre}</span></div>
                  <div className="item-bottom"><div><span className="item-qty">{item.cantidadDisponible}</span><span className="item-qty-label">/ {item.cantidadTotal} disponibles</span></div></div>
                </div>
              ))}
              {filteredItems.length === 0 && <div className="empty-state"><Search size={40} /><p>No se encontraron items</p></div>}
            </div>
          </div>
        )}

        {activeTab === "requests" && (
          <div className="fade-in">
            <div className="section-card">
              <div className="section-header"><div className="section-title">Solicitudes pendientes</div><div className="section-live"><Zap size={12} className="live-dot" />{pendingRequests.length} pendiente{pendingRequests.length !== 1 ? "s" : ""}</div></div>
              {pendingRequests.length === 0 ? (<div className="empty-state-inline"><CheckCircle size={24} /><p>No hay solicitudes pendientes</p></div>) : (
                pendingRequests.map((req, i) => (
                  <div key={req.id} className="request-row" style={{ animationDelay: `${i * 0.06}s` }}>
                    <div className="request-left">
                      <div className="avatar">{req.userName.split(" ").map((n) => n[0]).join("")}</div>
                      <div>
                        <div className="request-user">{req.userName}</div>
                        <div className="request-detail">
                          Solicita: {req.itemName} (x{req.cantidad})
                          {req.detectedBy === "AI_VISION" && <span className="ai-tag"><Eye size={10} /> AI Vision {req.confidenceScore && `${(req.confidenceScore * 100).toFixed(1)}%`}</span>}
                        </div>
                      </div>
                    </div>
                    <div className="request-actions">
                      <button className="btn-reject" onClick={() => handleReject(req.id)} disabled={actionLoading === req.id}>Rechazar</button>
                      <button className="btn-approve" onClick={() => handleApprove(req.id)} disabled={actionLoading === req.id}>{actionLoading === req.id ? <Loader2 size={14} className="spin" /> : "Aprobar"}</button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {activeTab === "loans" && (
          <div className="fade-in">
            <div className="section-card">
              <div className="section-header"><div className="section-title">Préstamos activos</div><div className="section-live"><Clock size={12} />{activeLoans.length} activo{activeLoans.length !== 1 ? "s" : ""}</div></div>
              {activeLoans.length === 0 ? (<div className="empty-state-inline"><Package size={24} /><p>No hay préstamos activos</p></div>) : (
                activeLoans.map((loan, i) => (
                  <div key={loan.id} className="request-row" style={{ animationDelay: `${i * 0.06}s` }}>
                    <div className="request-left">
                      <div className="avatar avatar-amber">{loan.userName.split(" ").map((n) => n[0]).join("")}</div>
                      <div>
                        <div className="request-user">{loan.userName}</div>
                        <div className="request-detail">{loan.itemName} (x{loan.cantidad}) · Aprobado por {loan.approvedByName}</div>
                        <div className="request-dates">Prestado: {loan.fechaPrestamo} · Vence: {loan.fechaDevolucionEsperada}</div>
                      </div>
                    </div>
                    <div className="request-actions">
                      <LoanStatusBadge status={loan.status} />
                      <button className="btn-return" onClick={() => handleReturn(loan.id)} disabled={actionLoading === `loan-${loan.id}`}>
                        {actionLoading === `loan-${loan.id}` ? <Loader2 size={14} className="spin" /> : <><Undo2 size={14} /> Devolver</>}
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </main>

      <ScanModal isOpen={showScan} onClose={() => setShowScan(false)} onLoanCreated={() => { loadData(); showNotification("Solicitud creada por AI Vision"); }} currentUser={currentUser} />
      <NewRequestModal isOpen={showManual} onClose={() => setShowManual(false)} onCreated={() => { loadData(); showNotification("Solicitud manual creada"); }} />
    </div>
  );
}