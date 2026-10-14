import React, { useState, useContext } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const AdminLayout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const { user, logout } = useContext(AuthContext);
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <div className="admin-layout">
      {/* Sidebar */}
      <aside className={`admin-sidebar ${sidebarOpen ? 'open' : 'closed'}`}>
        <div className="sidebar-brand">
          <Link to="/admin/dashboard" className="brand-link">
            <span>💧</span>
            {sidebarOpen && <strong>SMART WATER</strong>}
          </Link>
          <button className="toggle-btn" onClick={() => setSidebarOpen(!sidebarOpen)}>
            {sidebarOpen ? '◀' : '▶'}
          </button>
        </div>

        <div className="sidebar-menu">
          <span className="menu-header">{sidebarOpen && 'CORE MANAGEMENT'}</span>
          <Link to="/admin/dashboard" className={`menu-item ${isActive('/admin/dashboard') ? 'active' : ''}`}>
            <span className="icon">📊</span>
            {sidebarOpen && <span>Dashboard</span>}
          </Link>
          <Link to="/admin/apartments" className={`menu-item ${isActive('/admin/apartments') ? 'active' : ''}`}>
            <span className="icon">🏢</span>
            {sidebarOpen && <span>Apartments</span>}
          </Link>
          <Link to="/admin/households" className={`menu-item ${isActive('/admin/households') ? 'active' : ''}`}>
            <span className="icon">🏠</span>
            {sidebarOpen && <span>Households</span>}
          </Link>
          <Link to="/admin/residents" className={`menu-item ${isActive('/admin/residents') ? 'active' : ''}`}>
            <span className="icon">👥</span>
            {sidebarOpen && <span>Residents</span>}
          </Link>
          <Link to="/admin/meters" className={`menu-item ${isActive('/admin/meters') ? 'active' : ''}`}>
            <span className="icon">📟</span>
            {sidebarOpen && <span>Meters</span>}
          </Link>
          <Link to="/admin/usage" className={`menu-item ${isActive('/admin/usage') ? 'active' : ''}`}>
            <span className="icon">💧</span>
            {sidebarOpen && <span>Water Usage & CSV</span>}
          </Link>

          <span className="menu-header">{sidebarOpen && 'BILLING & ALERTS'}</span>
          <Link to="/admin/tariffs" className={`menu-item ${isActive('/admin/tariffs') ? 'active' : ''}`}>
            <span className="icon">🏷️</span>
            {sidebarOpen && <span>Tiered Tariffs</span>}
          </Link>
          <Link to="/admin/purchases" className={`menu-item ${isActive('/admin/purchases') ? 'active' : ''}`}>
            <span className="icon">🚛</span>
            {sidebarOpen && <span>Bulk Water Purchases</span>}
          </Link>
          <Link to="/admin/billing" className={`menu-item ${isActive('/admin/billing') ? 'active' : ''}`}>
            <span className="icon">🧾</span>
            {sidebarOpen && <span>Billing Cycles & Invoices</span>}
          </Link>
          <Link to="/admin/alerts" className={`menu-item ${isActive('/admin/alerts') ? 'active' : ''}`}>
            <span className="icon">⚠️</span>
            {sidebarOpen && <span>Leak & Anomaly Alerts</span>}
          </Link>
          <Link to="/admin/notifications" className={`menu-item ${isActive('/admin/notifications') ? 'active' : ''}`}>
            <span className="icon">🔔</span>
            {sidebarOpen && <span>Notification Center</span>}
          </Link>
        </div>

        <div className="sidebar-footer">
          {sidebarOpen && (
            <div className="user-info">
              <strong>{user?.name || 'Smart Water Admin'}</strong>
              <small>ADMINISTRATOR</small>
            </div>
          )}
          <button onClick={handleLogout} className="btn-sidebar-logout" title="Logout">
            🚪 {sidebarOpen && 'Sign Out'}
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="admin-main">
        <header className="admin-header">
          <div className="header-left">
            <Link to="/" className="btn-back-home">
              ← Public Site
            </Link>
          </div>
          <div className="header-right">
            <span className="status-indicator">
              <span className="pulse"></span> PostgreSQL Live
            </span>
            <span className="admin-badge">{user?.email || 'admin@smartwater.com'}</span>
          </div>
        </header>

        <main className="admin-body">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;
