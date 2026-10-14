import React, { useContext } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { isPreviewMode } from '../utils/previewMode';

const PreviewBanner = () => {
  const { previewRole, setPreviewRole, user } = useContext(AuthContext);
  const navigate = useNavigate();
  const location = useLocation();

  if (!isPreviewMode()) {
    return null;
  }

  const handleRoleSwitch = (newRole) => {
    setPreviewRole(newRole);
    if (newRole === 'ADMIN' && location.pathname.startsWith('/resident')) {
      navigate('/admin/dashboard');
    } else if (newRole === 'RESIDENT' && location.pathname.startsWith('/admin')) {
      navigate('/resident/dashboard');
    }
  };

  return (
    <div
      style={{
        background: 'linear-gradient(90deg, #12304A 0%, #1769AA 100%)',
        color: '#FFFFFF',
        padding: '0.4rem 1rem',
        fontSize: '0.8rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '0.5rem',
        borderBottom: '2px solid #25B8B0',
        zIndex: 9999,
        position: 'relative'
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
        <span
          style={{
            background: '#25B8B0',
            color: '#12304A',
            fontWeight: 800,
            padding: '0.1rem 0.5rem',
            borderRadius: '4px',
            fontSize: '0.7rem',
            letterSpacing: '0.5px'
          }}
        >
          DEV PREVIEW MODE
        </span>
        <span>
          Authentication Bypassed for UI Inspection {user && user.isPreview ? `(${user.name})` : ''}
        </span>
      </div>

      {/* Quick Route Navigator */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
        <span style={{ color: '#94A3B8', fontWeight: 600 }}>Quick Jump:</span>

        <div style={{ display: 'flex', gap: '0.35rem' }}>
          <Link to="/" style={{ color: '#E2E8F0', padding: '0.1rem 0.4rem', background: 'rgba(255,255,255,0.1)', borderRadius: '3px' }}>Home</Link>
          <Link to="/login" style={{ color: '#E2E8F0', padding: '0.1rem 0.4rem', background: 'rgba(255,255,255,0.1)', borderRadius: '3px' }}>Login</Link>
          <Link to="/register" style={{ color: '#E2E8F0', padding: '0.1rem 0.4rem', background: 'rgba(255,255,255,0.1)', borderRadius: '3px' }}>Register</Link>
        </div>

        <span style={{ color: 'rgba(255,255,255,0.2)' }}>|</span>

        {/* Admin Links */}
        <div style={{ display: 'flex', gap: '0.35rem' }}>
          <Link to="/admin/dashboard" style={{ color: '#7DD3FC', padding: '0.1rem 0.4rem', background: 'rgba(255,255,255,0.1)', borderRadius: '3px', fontWeight: 600 }}>Admin Dash</Link>
          <Link to="/admin/apartments" style={{ color: '#7DD3FC', padding: '0.1rem 0.4rem', background: 'rgba(255,255,255,0.1)', borderRadius: '3px' }}>Apartments</Link>
          <Link to="/admin/households" style={{ color: '#7DD3FC', padding: '0.1rem 0.4rem', background: 'rgba(255,255,255,0.1)', borderRadius: '3px' }}>Households</Link>
          <Link to="/admin/meters" style={{ color: '#7DD3FC', padding: '0.1rem 0.4rem', background: 'rgba(255,255,255,0.1)', borderRadius: '3px' }}>Meters</Link>
          <Link to="/admin/usage" style={{ color: '#7DD3FC', padding: '0.1rem 0.4rem', background: 'rgba(255,255,255,0.1)', borderRadius: '3px' }}>Usage & CSV</Link>
        </div>

        <span style={{ color: 'rgba(255,255,255,0.2)' }}>|</span>

        {/* Resident Links */}
        <div style={{ display: 'flex', gap: '0.35rem' }}>
          <Link to="/resident/dashboard" style={{ color: '#86EFAC', padding: '0.1rem 0.4rem', background: 'rgba(255,255,255,0.1)', borderRadius: '3px', fontWeight: 600 }}>Resident Dash</Link>
          <Link to="/resident/usage" style={{ color: '#86EFAC', padding: '0.1rem 0.4rem', background: 'rgba(255,255,255,0.1)', borderRadius: '3px' }}>Resident Usage</Link>
        </div>

        {/* Role Toggle */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', marginLeft: '0.5rem' }}>
          <span style={{ fontSize: '0.75rem', color: '#CBD5E1' }}>Role:</span>
          <button
            type="button"
            onClick={() => handleRoleSwitch('ADMIN')}
            style={{
              padding: '0.15rem 0.4rem',
              fontSize: '0.7rem',
              fontWeight: 700,
              background: previewRole === 'ADMIN' ? '#1769AA' : 'transparent',
              color: '#FFFFFF',
              border: '1px solid #1769AA',
              borderRadius: '3px',
              cursor: 'pointer'
            }}
          >
            ADMIN
          </button>
          <button
            type="button"
            onClick={() => handleRoleSwitch('RESIDENT')}
            style={{
              padding: '0.15rem 0.4rem',
              fontSize: '0.7rem',
              fontWeight: 700,
              background: previewRole === 'RESIDENT' ? '#15803D' : 'transparent',
              color: '#FFFFFF',
              border: '1px solid #15803D',
              borderRadius: '3px',
              cursor: 'pointer'
            }}
          >
            RESIDENT
          </button>
        </div>
      </div>
    </div>
  );
};

export default PreviewBanner;
