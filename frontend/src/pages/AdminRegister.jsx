import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const AdminRegister = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { register } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');

    if (!name || name.trim() === '') {
      setError('Owner full name is required.');
      return;
    }

    if (!email || email.trim() === '') {
      setError('Owner email address is required.');
      return;
    }

    // Basic email format check
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email.trim())) {
      setError('Please provide a valid email address.');
      return;
    }

    if (!password) {
      setError('Password is required.');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match. Please re-enter.');
      return;
    }

    setSubmitting(true);
    try {
      // Register with role ADMIN and NO secret key requirement
      const response = await register(name.trim(), email.trim(), password, 'ADMIN');
      setSuccessMessage(
        response?.message || 'Owner account registered successfully! You can now proceed to login.'
      );
      // Clear sensitive fields
      setPassword('');
      setConfirmPassword('');
    } catch (err) {
      const serverMessage = err.response?.data?.message;
      const validationErrors = err.response?.data?.details
        ? Object.values(err.response.data.details).join(', ')
        : null;
      setError(
        validationErrors || serverMessage || (err.message ? `Owner registration failed: ${err.message}` : 'Owner registration failed. Please check your network connection and try again.')
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
          <h1 style={{ marginTop: '0.75rem' }}>Register Apartment Owner</h1>
          <p>
            Create your platform administrator account to manage apartment communities, meters, progressive tariffs, and billing lifecycles.
          </p>

          <div className="auth-value-props">
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Centralized apartment community management</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Submetering and bulk CSV meter reading entry</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Progressive tariff configuration & billing cycles</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Secure server-enforced role authorization</span>
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
            <h2>Apartment Owner Registration</h2>
            <p>Set up your community administrator account.</p>
          </div>

          {error && <div className="alert alert-danger" style={{ marginBottom: '1.25rem' }}>{error}</div>}

          {successMessage ? (
            <div style={{ textAlign: 'center', padding: '1.5rem 0' }}>
              <div style={{ fontSize: '3rem', marginBottom: '0.75rem' }}>✅</div>
              <h3 style={{ color: 'var(--primary-navy)', marginBottom: '0.5rem' }}>Registration Successful</h3>
              <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem', lineHeight: '1.6' }}>
                {successMessage}
              </p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <Link to="/admin/login" className="btn btn-primary btn-full" style={{ padding: '0.75rem', fontSize: '1rem', fontWeight: 600 }}>
                  Proceed to Owner Login →
                </Link>
                <Link to="/" className="btn btn-secondary btn-full">
                  Back to Public Website
                </Link>
              </div>
            </div>
          ) : (
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
                    placeholder="e.g. Community Owner / Admin"
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
                    placeholder="owner@apartment.com"
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

              <button type="submit" className="btn btn-primary btn-full" disabled={submitting} style={{ marginTop: '1rem' }}>
                {submitting ? 'Registering Owner...' : 'Register Owner Account'}
              </button>
            </form>
          )}

          <div className="auth-links" style={{ marginTop: '1.5rem' }}>
            <p>
              Already registered? <Link to="/admin/login">Owner Login here</Link>
            </p>
            <p style={{ marginTop: '0.25rem' }}>
              Resident Account? <Link to="/resident/register">Resident Registration</Link>
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
