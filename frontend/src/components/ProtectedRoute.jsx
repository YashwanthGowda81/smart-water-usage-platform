import React, { useContext } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const ProtectedRoute = ({ allowedRoles }) => {
  const { user, loading, setPreviewRole } = useContext(AuthContext);

  if (loading) {
    return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading application...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    if (user.isPreview) {
      return (
        <div style={{ padding: '3rem 1.5rem', textAlign: 'center', maxWidth: '600px', margin: '2rem auto' }} className="card">
          <h2 style={{ color: 'var(--primary-navy)', marginBottom: '0.5rem' }}>⚡ Preview Role Switch Notice</h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
            This page requires the <strong>{allowedRoles.join(' or ')}</strong> role. You are currently viewing as <strong>{user.role}</strong> in Preview Mode.
          </p>
          <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem' }}>
            <button
              onClick={() => setPreviewRole(allowedRoles[0])}
              className="btn btn-primary"
            >
              Switch Preview Role to {allowedRoles[0]}
            </button>
          </div>
        </div>
      );
    }

    return (
      <div style={{ padding: '3rem 1.5rem', textAlign: 'center', maxWidth: '500px', margin: '3rem auto' }} className="card">
        <h2 style={{ color: 'var(--error)', marginBottom: '0.5rem' }}>Access Denied</h2>
        <p style={{ color: 'var(--text-secondary)' }}>You do not have permission to view this page with your account role ({user.role}).</p>
      </div>
    );
  }

  return <Outlet />;
};

export default ProtectedRoute;
