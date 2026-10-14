import React, { useContext } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) return null;

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="navbar">
      <Link to={user.role === 'ADMIN' ? '/admin/dashboard' : '/resident/dashboard'} className="navbar-brand">
        💧 SmartWater SaaS
      </Link>
      <ul className="navbar-links">
        {user.role === 'ADMIN' ? (
          <>
            <li>
              <Link to="/admin/dashboard" className={isActive('/admin/dashboard') ? 'active' : ''}>
                Dashboard
              </Link>
            </li>
            <li>
              <Link to="/admin/apartments" className={isActive('/admin/apartments') ? 'active' : ''}>
                Apartments
              </Link>
            </li>
            <li>
              <Link to="/admin/households" className={isActive('/admin/households') ? 'active' : ''}>
                Households
              </Link>
            </li>
            <li>
              <Link to="/admin/meters" className={isActive('/admin/meters') ? 'active' : ''}>
                Meters
              </Link>
            </li>
            <li>
              <Link to="/admin/usage" className={isActive('/admin/usage') ? 'active' : ''}>
                Water Readings & CSV
              </Link>
            </li>
          </>
        ) : (
          <>
            <li>
              <Link to="/resident/dashboard" className={isActive('/resident/dashboard') ? 'active' : ''}>
                My Household
              </Link>
            </li>
            <li>
              <Link to="/resident/usage" className={isActive('/resident/usage') ? 'active' : ''}>
                Usage History
              </Link>
            </li>
          </>
        )}
      </ul>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <span className="user-badge">
          {user.name} ({user.role})
        </span>
        <button onClick={handleLogout} className="btn-logout">
          Logout
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
