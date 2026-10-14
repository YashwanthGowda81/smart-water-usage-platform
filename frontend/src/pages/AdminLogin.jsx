import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const AdminLogin = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  
  const { login, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Please provide both administrator email and password.');
      return;
    }

    setSubmitting(true);

    try {
      const user = await login(email.trim(), password);
      if (user.role === 'ADMIN') {
        navigate('/admin/dashboard');
      } else {
        // Safe denial: Logout resident credentials to prevent residual session on admin console
        logout();
        setError('Access denied: This account is registered as a Resident. Please sign in via the Resident Portal.');
      }
    } catch (err) {
      setError(
        err.response?.data?.message || 'Invalid administrator credentials. Please check your email and password.'
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-split-container">
      {/* Left Brand Panel */}
      <div className="auth-brand-panel" style={{ background: 'linear-gradient(135deg, #0A192F 0%, #12304A 100%)' }}>
        <div className="auth-brand-header">
          <span className="auth-brand-logo">💧</span>
          <span className="auth-brand-title">SMART WATER</span>
        </div>

        <div className="auth-brand-body">
          <span className="badge badge-primary" style={{ background: 'var(--primary-blue)', color: '#fff', fontSize: '0.85rem' }}>
            Apartment Owner / Admin Console
          </span>
          <h1 style={{ marginTop: '0.75rem' }}>Administrator Sign In</h1>
          <p>
            Access full community management, household metering, tiered tariff configuration, bulk water purchase tracking, and bill finalization.
          </p>

          <div className="auth-value-props">
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Apartment, flat & smart meter management</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Bulk CSV meter upload & duplicate validation</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Progressive tariff & shared cost allocation</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Billing cycle state machine & PDF invoices</span>
            </div>
          </div>
        </div>

        <div className="auth-brand-footer">
          © 2026 Smart Water Usage Platform. All rights reserved.
        </div>
      </div>

      {/* Right Form Panel */}
      <div className="auth-form-panel">
        <div className="auth-form-card">
          <div className="auth-form-header">
            <h2>Apartment Owner / Admin Login</h2>
            <p>Enter your administrative credentials to continue.</p>
          </div>

          {error && <div className="alert alert-danger">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Administrator Email</label>
              <div className="form-control-wrapper">
                <span className="form-control-icon">✉</span>
                <input
                  type="email"
                  className="form-control has-icon"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="admin@apartment.com"
                  autoComplete="email"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <div className="form-control-wrapper">
                <span className="form-control-icon">🔒</span>
                <input
                  type={showPassword ? 'text' : 'password'}
                  className="form-control has-icon"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  autoComplete="current-password"
                  required
                />
                <button
                  type="button"
                  className="toggle-password-btn"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-full" disabled={submitting}>
              {submitting ? 'Authenticating Admin...' : 'Sign In as Administrator'}
            </button>
          </form>

          <div className="auth-links">
            <p>
              Need an Administrator account? <Link to="/admin/register">Register Admin Account</Link>
            </p>
            <p style={{ marginTop: '0.25rem' }}>
              Resident User? <Link to="/resident/login">Go to Resident Login</Link>
            </p>
            <Link to="/" className="link-public-home" style={{ marginTop: '0.5rem', display: 'inline-block' }}>
              ← Back to public website
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminLogin;
