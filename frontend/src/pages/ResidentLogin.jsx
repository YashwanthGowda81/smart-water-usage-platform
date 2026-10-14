import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const ResidentLogin = () => {
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
      setError('Please provide both resident email and password.');
      return;
    }

    setSubmitting(true);

    try {
      const user = await login(email.trim(), password, 'RESIDENT');
      if (user.role === 'RESIDENT') {
        navigate('/resident/dashboard');
      } else {
        logout();
        setError('Access denied: This portal is for Residents only. Apartment Owners/Administrators must use the Owner Login page.');
      }
    } catch (err) {
      setError(
        err.response?.data?.message || 'Invalid resident credentials. Please check your email and password.'
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-split-container">
      {/* Left Brand Panel */}
      <div className="auth-brand-panel" style={{ background: 'linear-gradient(135deg, #12304A 0%, #1769AA 100%)' }}>
        <div className="auth-brand-header">
          <span className="auth-brand-logo">💧</span>
          <span className="auth-brand-title">SMART WATER</span>
        </div>

        <div className="auth-brand-body">
          <span className="badge badge-info" style={{ background: 'rgba(255,255,255,0.2)', color: '#fff', fontSize: '0.85rem' }}>
            Resident Portal
          </span>
          <h1 style={{ marginTop: '0.75rem' }}>Welcome Back, Resident!</h1>
          <p>
            Sign in to track your household water consumption, view itemized monthly bills, download PDF invoices, and check leak alerts.
          </p>

          <div className="auth-value-props">
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Real-time household meter analytics</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Transparent itemized billing & PDF downloads</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Automated leak anomaly alerts</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Peer benchmarking & conservation score</span>
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
            <h2>Resident Sign In</h2>
            <p>Access your household water account and bill statements.</p>
          </div>

          {error && <div className="alert alert-danger">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <div className="form-control-wrapper">
                <span className="form-control-icon">✉</span>
                <input
                  type="email"
                  className="form-control has-icon"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="resident@apartment.com"
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
              {submitting ? 'Signing in...' : 'Sign In to Resident Portal'}
            </button>
          </form>

          <div className="auth-links">
            <p>
              New Resident? <Link to="/resident/register">Create a Resident Account</Link>
            </p>
            <p style={{ marginTop: '0.25rem' }}>
              Apartment Owner / Admin? <Link to="/admin/login">Go to Admin Login</Link>
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

export default ResidentLogin;
