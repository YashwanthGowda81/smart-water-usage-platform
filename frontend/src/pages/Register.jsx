import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const Register = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [role, setRole] = useState('RESIDENT');
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

    setSubmitting(true);
    try {
      const user = await register(name, email, password, role, adminSecret);
      if (user.role === 'ADMIN') {
        navigate('/admin/dashboard');
      } else {
        navigate('/resident/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Email might already be registered.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-split-container">
      {/* Left Brand Panel */}
      <div className="auth-brand-panel">
        <div className="auth-brand-header">
          <span className="auth-brand-logo">💧</span>
          <span className="auth-brand-title">SMART WATER</span>
        </div>

        <div className="auth-brand-body">
          <h1>Create your account</h1>
          <p>
            Join your apartment community on Smart Water to track consumption, view transparent water billing, and optimize water efficiency.
          </p>

          <div className="auth-value-props">
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Individual meter tracking & transparent metrics</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Fair common-area cost distribution</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Instant abnormal usage & leakage notifications</span>
            </div>
          </div>
        </div>

        <div className="auth-brand-footer">
          © 2026 Smart Water Usage Platform. All rights reserved.
        </div>
      </div>

      {/* Right Register Form Panel */}
      <div className="auth-form-panel">
        <div className="auth-form-card" style={{ maxWidth: '480px' }}>
          <div className="auth-form-header">
            <h2>Get Started Today</h2>
            <p>Fill out the details below to register your account.</p>
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
                  placeholder="e.g. Ananya Sharma"
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
                  placeholder="name@example.com"
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
              <label className="form-label">Account Type / Role</label>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginTop: '0.25rem' }}>
                <div
                  onClick={() => setRole('RESIDENT')}
                  style={{
                    padding: '0.75rem 1rem',
                    borderRadius: 'var(--radius-md)',
                    border: role === 'RESIDENT' ? '2px solid var(--primary-blue)' : '1px solid var(--border-color)',
                    background: role === 'RESIDENT' ? 'var(--primary-light)' : 'var(--bg-surface)',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease'
                  }}
                >
                  <div style={{ fontWeight: 600, color: 'var(--text-main)', fontSize: '0.9rem' }}>
                    🏠 Resident
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                    View usage & bills
                  </div>
                </div>

                <div
                  onClick={() => setRole('ADMIN')}
                  style={{
                    padding: '0.75rem 1rem',
                    borderRadius: 'var(--radius-md)',
                    border: role === 'ADMIN' ? '2px solid var(--primary-blue)' : '1px solid var(--border-color)',
                    background: role === 'ADMIN' ? 'var(--primary-light)' : 'var(--bg-surface)',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease'
                  }}
                >
                  <div style={{ fontWeight: 600, color: 'var(--text-main)', fontSize: '0.9rem' }}>
                    🛡️ Administrator
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                    Manage community & meters
                  </div>
                </div>
              </div>
            </div>

            {role === 'ADMIN' && (
              <div className="form-group" style={{ marginTop: '0.75rem' }}>
                <label className="form-label">Admin Provisioning Secret Key</label>
                <div className="form-control-wrapper">
                  <span className="form-control-icon">🔑</span>
                  <input
                    type="password"
                    className="form-control has-icon"
                    value={adminSecret}
                    onChange={(e) => setAdminSecret(e.target.value)}
                    placeholder="Enter admin key (e.g. ADMIN-SECRET-2026)"
                  />
                </div>
                <small style={{ color: 'var(--text-secondary)', fontSize: '0.75rem' }}>
                  Required to register an Administrator account (Unless initializing first platform user).
                </small>
              </div>
            )}

            <button type="submit" className="btn btn-primary btn-full" disabled={submitting} style={{ marginTop: '1rem' }}>
              {submitting ? 'Creating Account...' : 'Create Account'}
            </button>
          </form>

          <div className="auth-links">
            <p>
              Already have an account? <Link to="/login">Sign in here</Link>
            </p>
            <Link to="/" className="link-public-home">
              ← Back to public website
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
