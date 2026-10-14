import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const ResidentRegister = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
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
      // Force RESIDENT role for resident registration
      const user = await register(name, email, password, 'RESIDENT', '');
      navigate('/resident/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Resident registration failed. Email may already be registered.');
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
            Resident Account Registration
          </span>
          <h1 style={{ marginTop: '0.75rem' }}>Join Your Water Community</h1>
          <p>
            Create your resident profile to access meter consumption data, view itemized bills, and track water conservation.
          </p>

          <div className="auth-value-props">
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Dedicated resident portal & dashboard</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Daily/monthly Recharts water consumption graphs</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Fair progressive tariff billing transparency</span>
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
            <h2>Resident Registration</h2>
            <p>Register a new resident account to get started.</p>
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
                  placeholder="e.g. Priya Sharma"
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
              <label className="form-label">Account Role</label>
              <input type="text" className="form-control" value="RESIDENT (Standard Account)" disabled />
            </div>

            <button type="submit" className="btn btn-primary btn-full" disabled={submitting} style={{ marginTop: '1rem' }}>
              {submitting ? 'Registering...' : 'Register Resident Account'}
            </button>
          </form>

          <div className="auth-links">
            <p>
              Already registered? <Link to="/resident/login">Sign in here</Link>
            </p>
            <p style={{ marginTop: '0.25rem' }}>
              Apartment Owner / Admin? <Link to="/admin/register">Go to Admin Registration</Link>
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

export default ResidentRegister;
