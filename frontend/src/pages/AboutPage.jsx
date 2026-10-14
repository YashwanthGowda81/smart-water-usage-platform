import React from 'react';
import { Link } from 'react-router-dom';

const AboutPage = () => {
  return (
    <div className="public-page-container">
      <div className="public-page-header">
        <h1>About Smart Water</h1>
        <p>Software-only full-stack platform for apartment water monitoring and management.</p>
      </div>

      <div className="card" style={{ marginBottom: '2rem' }}>
        <h2 className="card-title">Project Purpose</h2>
        <p style={{ color: '#475569', lineHeight: '1.7', marginBottom: '1rem' }}>
          The <strong>Smart Water Usage Monitoring and Automated Billing Management Platform</strong> is designed to help apartment communities transition from unmetered, arbitrary water division to precise, data-driven consumption tracking.
        </p>
        <p style={{ color: '#475569', lineHeight: '1.7' }}>
          By providing manual entry tools, bulk CSV uploads, structured PostgreSQL storage, and real-time dashboard analytics, Smart Water establishes transparency for apartment administrators and residents alike.
        </p>
      </div>

      <div className="grid-cols-2" style={{ marginBottom: '2rem' }}>
        <div className="card">
          <h2 className="card-title">👥 Who Uses It?</h2>
          <ul style={{ paddingLeft: '1.25rem', color: '#475569', lineHeight: '1.8' }}>
            <li><strong>Apartment Administrators</strong>: Register complexes, set up households, configure meter serial numbers, enter/upload readings, and view community consumption.</li>
            <li><strong>Residents</strong>: Log in to view personal household occupancy, assigned submeters, historical water usage logs, and conservation recommendations.</li>
          </ul>
        </div>

        <div className="card">
          <h2 className="card-title">💻 Technology Stack</h2>
          <ul style={{ paddingLeft: '1.25rem', color: '#475569', lineHeight: '1.8' }}>
            <li><strong>Backend</strong>: Java 21 LTS, Spring Boot 3.3.4, Spring Data JPA, Spring Security 6, Maven</li>
            <li><strong>Database</strong>: PostgreSQL 17 managed via Flyway migrations</li>
            <li><strong>Frontend</strong>: React 18, Vite, React Router DOM v6, Axios, Recharts</li>
          </ul>
        </div>
      </div>

      <div className="card">
        <h2 className="card-title">🚀 Platform Capabilities (Phases 1–4 Complete)</h2>
        <p style={{ color: '#475569', marginBottom: '1rem' }}>
          All four architectural phases have been fully implemented, tested, and integrated:
        </p>
        <div className="grid-cols-2">
          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '6px' }}>
            <h4 style={{ color: '#0284c7' }}>Automated Billing Engine</h4>
            <p style={{ fontSize: '0.9rem', color: '#64748b' }}>Configurable progressive tiered tariffs, billing cycles (calculate/finalize/archive), shared bulk cost distribution, and itemized PDF invoice generation via Apache PDFBox.</p>
          </div>
          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '6px' }}>
            <h4 style={{ color: '#0284c7' }}>Anomaly Detection & Security</h4>
            <p style={{ fontSize: '0.9rem', color: '#64748b' }}>Statistical mean + 2 std-dev outlier alerts with duplicate prevention, BCrypt hashing, JWT authorization, and strict resident household data isolation.</p>
          </div>
        </div>
      </div>

      <div style={{ textAlign: 'center', marginTop: '2.5rem', display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
        <Link to="/resident/login" className="btn btn-primary" style={{ padding: '0.75rem 2rem' }}>
          Resident Sign In
        </Link>
        <Link to="/admin/login" className="btn btn-secondary" style={{ padding: '0.75rem 2rem', background: 'var(--primary-navy)', color: '#fff' }}>
          Admin Sign In
        </Link>
      </div>
    </div>
  );
};

export default AboutPage;
