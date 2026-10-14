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
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { register } = useContext(AuthContext);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');

    if (!name || name.trim() === '') {
      setError('Full name is required.');
      return;
    }

    if (!email || email.trim() === '') {
      setError('Email address is required.');
      return;
    }

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
      const response = await register(name.trim(), email.trim(), password, role);
      setSuccessMessage(
        response?.message || 'Account registered successfully! Please proceed to login.'
      );
      setPassword('');
      setConfirmPassword('');
    } catch (err) {
      const serverMessage = err.response?.data?.message;
      const validationErrors = err.response?.data?.details
        ? Object.values(err.response.data.details).join(', ')
        : null;
      setError(
        validationErrors || serverMessage || (err.message ? `Registration failed: ${err.message}` : 'Registration failed. Please check your network connection and try again.')
      );
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

          {error && <div className="alert alert-danger" style={{ marginBottom: '1.25rem' }}>{error}</div>}

          {successMessage ? (
            <div style={{ textAlign: 'center', padding: '1.5rem 0' }}>
              <div style={{ fontSize: '3rem', marginBottom: '0.75rem' }}>✅</div>
              <h3 style={{ color: 'var(--primary-navy)', marginBottom: '0.5rem' }}>Registration Successful</h3>
              <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem', lineHeight: '1.6' }}>
                {successMessage}
              </p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {role === 'ADMIN' ? (
                  <Link to="/admin/login" className="btn btn-primary btn-full" style={{ padding: '0.75rem', fontSize: '1rem', fontWeight: 600 }}>
                    Proceed to Owner Login →
                  </Link>
                ) : (
                  <Link to="/resident/login" className="btn btn-primary btn-full" style={{ padding: '0.75rem', fontSize: '1rem', fontWeight: 600 }}>
                    Proceed to Resident Login →
                  </Link>
                )}
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
                    placeholder="e.g. John Doe"
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
                    placeholder="name@apartment.com"
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
                <label className="form-label">Select Role</label>
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
                      Household member
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
                      🛡️ Apartment Owner
                    </div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                      Community setup
                    </div>
                  </div>
                </div>
              </div>

              <button type="submit" className="btn btn-primary btn-full" disabled={submitting} style={{ marginTop: '1rem' }}>
                {submitting ? 'Creating Account...' : 'Create Account'}
              </button>
            </form>
          )}

          <div className="auth-links" style={{ marginTop: '1.5rem' }}>
            <p>
              Already have an account? <Link to="/login">Sign in here</Link>
            </p>
            <div style={{ margin: '0.85rem 0', padding: '0.75rem', background: '#F8FAFC', borderRadius: '6px', fontSize: '0.825rem', border: '1px solid #E2E8F0' }}>
              <div style={{ fontWeight: 600, color: 'var(--primary-navy)', marginBottom: '0.25rem' }}>Direct Registration Options:</div>
              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
                <Link to="/resident/register" style={{ color: 'var(--primary-blue)', fontWeight: 600 }}>🏠 Resident Sign Up</Link>
                <span style={{ color: '#CBD5E1' }}>|</span>
                <Link to="/admin/register" style={{ color: 'var(--primary-navy)', fontWeight: 600 }}>🛡️ Owner Registration</Link>
              </div>
            </div>
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
