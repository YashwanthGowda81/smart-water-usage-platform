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
          <h4>Platform Access</h4>
          <Link to="/login">Sign In</Link>
          <Link to="/register">Create Account</Link>
          <Link to="/admin/dashboard">Admin Console</Link>
          <Link to="/resident/dashboard">Resident Portal</Link>
        </div>
      </div>

      <div className="footer-bottom">
        <p>© 2026 Smart Water Usage Platform. All rights reserved.</p>
      </div>
    </footer>
  );
};

export default PublicFooter;
