import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const AdminRegister = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [adminSecret, setAdminSecret] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { register } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError('Passwords do not match. Please re-enter.');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }

    if (!adminSecret || adminSecret.trim() === '') {
      setError('Administrator secret key is required for authorized onboarding.');
      return;
    }

    setSubmitting(true);
    try {
      const user = await register(name, email, password, 'ADMIN', adminSecret);
      navigate('/admin/dashboard');
    } catch (err) {
      setError(
        err.response?.data?.message || 'Admin registration failed. Invalid secret key or email already registered.'
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
            Administrator Provisioning
          </span>
          <h1 style={{ marginTop: '0.75rem' }}>Register Administrator</h1>
          <p>
            Secure initial administrator account registration with backend server validation and secret verification.
          </p>

          <div className="auth-value-props">
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Backend server-validated admin secret key</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Full control over apartment communities & households</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Progressive tariff configuration & billing cycles</span>
            </div>
          </div>
        </div>

        <div className="auth-brand-footer">
          © 2026 Smart Water Usage Platform. All rights reserved.
        </div>
      </div>

      {/* Right Form Panel */}
      <div className="auth-form-panel">
        <div className="auth-form-card" style={{ maxWidth: '480px' }}>
          <div className="auth-form-header">
            <h2>Admin Account Setup</h2>
            <p>Register an administrator account with secret key authorization.</p>
          </div>

          {error && <div className="alert alert-danger">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <div className="form-control-wrapper">
                <span className="form-control-icon">👤</span>
                <input
                  type="text"
                  className="form-control has-icon"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Admin Manager"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Email Address</label>
              <div className="form-control-wrapper">
                <span className="form-control-icon">✉</span>
                <input
                  type="email"
                  className="form-control has-icon"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="admin@example.com"
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
                  placeholder="At least 6 characters"
                  autoComplete="new-password"
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

            <div className="form-group">
              <label className="form-label">Confirm Password</label>
              <div className="form-control-wrapper">
                <span className="form-control-icon">🔒</span>
                <input
                  type={showPassword ? 'text' : 'password'}
                  className="form-control has-icon"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Re-enter password"
                  autoComplete="new-password"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Admin Secret Key / Provisioning Key</label>
              <div className="form-control-wrapper">
                <span className="form-control-icon">🔑</span>
                <input
                  type="password"
                  className="form-control has-icon"
                  value={adminSecret}
                  onChange={(e) => setAdminSecret(e.target.value)}
                  placeholder="Enter administrator bootstrap secret key"
                  required
                />
              </div>
              <small style={{ color: 'var(--text-secondary)', fontSize: '0.75rem' }}>
                Required authorization key configured on the backend server.
              </small>
            </div>

            <button type="submit" className="btn btn-primary btn-full" disabled={submitting} style={{ marginTop: '1rem' }}>
              {submitting ? 'Registering Admin...' : 'Register Administrator Account'}
            </button>
          </form>

          <div className="auth-links">
            <p>
              Already registered? <Link to="/admin/login">Sign in here</Link>
            </p>
            <p style={{ marginTop: '0.25rem' }}>
              Resident Account? <Link to="/resident/register">Go to Resident Registration</Link>
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

export default AdminRegister;
