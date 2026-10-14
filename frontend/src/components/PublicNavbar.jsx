import React, { useState, useContext } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const PublicNavbar = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { user, logout } = useContext(AuthContext);
  const location = useLocation();

  const isActive = (path) => location.pathname === path;

  return (
    <header className="public-navbar">
      <div className="public-navbar-container">
        <Link to="/" className="public-brand">
          <span className="brand-logo">💧</span>
          <span className="brand-name">SMART WATER</span>
        </Link>

        {/* Mobile menu toggle */}
        <button
          className="mobile-toggle"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle navigation"
        >
          ☰
        </button>

        {/* Nav links */}
        <nav className={`public-nav-links ${mobileMenuOpen ? 'open' : ''}`}>
          <Link to="/" className={isActive('/') ? 'active' : ''} onClick={() => setMobileMenuOpen(false)}>
            Home
          </Link>
          <Link to="/features" className={isActive('/features') ? 'active' : ''} onClick={() => setMobileMenuOpen(false)}>
            Features
          </Link>
          <Link to="/how-it-works" className={isActive('/how-it-works') ? 'active' : ''} onClick={() => setMobileMenuOpen(false)}>
            How It Works
          </Link>
          <Link to="/about" className={isActive('/about') ? 'active' : ''} onClick={() => setMobileMenuOpen(false)}>
            About
          </Link>
          <Link to="/water-saving" className={isActive('/water-saving') ? 'active' : ''} onClick={() => setMobileMenuOpen(false)}>
            Water Saving
          </Link>

          <div className="nav-actions">
            {user ? (
              <>
                {user.role === 'ADMIN' ? (
                  <Link to="/admin/dashboard" className="btn-nav-register" onClick={() => setMobileMenuOpen(false)}>
                    Admin Console
                  </Link>
                ) : (
                  <Link to="/resident/dashboard" className="btn-nav-register" onClick={() => setMobileMenuOpen(false)}>
                    Resident Portal
                  </Link>
                )}
                <button
                  onClick={() => {
                    logout();
                    setMobileMenuOpen(false);
                  }}
                  className="btn-nav-login"
                  style={{ cursor: 'pointer' }}
                >
                  Sign Out
                </button>
              </>
            ) : (
              <>
                <Link to="/resident/login" className="btn-nav-login" onClick={() => setMobileMenuOpen(false)}>
                  Resident Login
                </Link>
                <Link to="/admin/login" className="btn-nav-login" style={{ background: 'var(--primary-navy)', color: '#fff', borderColor: 'var(--primary-navy)' }} onClick={() => setMobileMenuOpen(false)}>
                  Admin Login
                </Link>
                <Link to="/resident/register" className="btn-nav-register" onClick={() => setMobileMenuOpen(false)}>
                  Join Resident
                </Link>
              </>
            )}
          </div>
        </nav>
      </div>
    </header>
  );
};

export default PublicNavbar;
