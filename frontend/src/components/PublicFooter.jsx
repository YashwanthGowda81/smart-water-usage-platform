import React from 'react';
import { Link } from 'react-router-dom';

const PublicFooter = () => {
  return (
    <footer className="public-footer">
      <div className="footer-container">
        <div className="footer-brand">
          <h3>💧 SMART WATER</h3>
          <p>
            Smart water usage monitoring and management platform for modern apartment communities. Building a transparent foundation for billing and conservation.
          </p>
        </div>

        <div className="footer-links-group">
          <h4>Navigation</h4>
          <Link to="/">Home</Link>
          <Link to="/features">Features</Link>
          <Link to="/how-it-works">How It Works</Link>
          <Link to="/about">About Us</Link>
          <Link to="/water-saving">Water Saving</Link>
        </div>

        <div className="footer-links-group">
          <h4>Resident Access</h4>
          <Link to="/resident/login">Resident Login</Link>
          <Link to="/resident/register">Resident Registration</Link>
          <Link to="/resident/dashboard">Household Portal</Link>
        </div>

        <div className="footer-links-group">
          <h4>Admin Access</h4>
          <Link to="/admin/login">Admin Console Login</Link>
          <Link to="/admin/register">Admin Secure Onboarding</Link>
          <Link to="/admin/dashboard">Management Console</Link>
        </div>
      </div>

      <div className="footer-bottom">
        <p>© 2026 Smart Water Usage Platform. All rights reserved.</p>
      </div>
    </footer>
  );
};

export default PublicFooter;
