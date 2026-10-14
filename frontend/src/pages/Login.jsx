import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      const user = await login(email, password);
      if (user.role === 'ADMIN') {
        navigate('/admin/dashboard');
      } else {
        navigate('/resident/dashboard');
      }
    } catch (err) {
      setError(
        err.response?.data?.message || 'Invalid credentials or authentication error. Please try again.'
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-split-container">
      {/* Left Brand & Value Proposition Panel */}
      <div className="auth-brand-panel">
        <div className="auth-brand-header">
          <span className="auth-brand-logo">💧</span>
          <span className="auth-brand-title">SMART WATER</span>
        </div>

        <div className="auth-brand-body">
          <h1>Welcome back</h1>
          <p>
            Sign in to manage water usage, monitor household consumption, and access your apartment community dashboard.
          </p>

          <div className="auth-value-props">
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Transparent water usage monitoring</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Real-time consumption insights</span>
            </div>
            <div className="prop-item">
              <span className="prop-icon">✓</span>
              <span>Smarter apartment management</span>
            </div>
          </div>
        </div>

        <div className="auth-brand-footer">
          © 2026 Smart Water Usage Platform. All rights reserved.
        </div>
      </div>

      {/* Right Login Form Panel */}
      <div className="auth-form-panel">
        <div className="auth-form-card">
          <div className="auth-form-header">
            <h2>Sign in to Smart Water</h2>
            <p>Enter your registered account credentials to continue.</p>
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
              {submitting ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          <div className="auth-links">
            <p>
              Don't have an account? <Link to="/register">Create one</Link>
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

export default Login;
