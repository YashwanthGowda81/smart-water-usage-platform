import React from 'react';
import { Link } from 'react-router-dom';

const LandingPage = () => {
  return (
    <div className="landing-page">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-container">
          <div className="hero-text">
            <span className="hero-badge">Smart Water Platform</span>
            <h1 className="hero-title" style={{ fontSize: '2.4rem', lineHeight: '1.2' }}>
              Development of a Smart Water Usage Monitoring and Automated Billing Management Platform
            </h1>
            <p className="hero-subtitle">
              An enterprise full-stack web platform built for apartment communities to track household consumption, eliminate water wastage, allocate shared bulk costs fairly, and automate progressive tariff billing.
            </p>

            {/* Prominent Portal Action Buttons */}
            <div className="hero-actions" style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', marginTop: '1.5rem' }}>
              <Link to="/resident/login" className="btn btn-hero-primary" style={{ background: 'var(--primary-blue)', padding: '0.85rem 1.5rem', fontWeight: 600 }}>
                🏠 Resident Portal & Login
              </Link>
              <Link to="/admin/login" className="btn btn-hero-secondary" style={{ background: 'var(--primary-navy)', color: '#fff', border: '1px solid rgba(255,255,255,0.2)', padding: '0.85rem 1.5rem', fontWeight: 600 }}>
                🛡️ Apartment Owner / Admin Portal
              </Link>
            </div>
            <div style={{ marginTop: '0.75rem', display: 'flex', gap: '1.25rem', fontSize: '0.875rem' }}>
              <Link to="/resident/login" style={{ color: 'var(--primary-blue)', textDecoration: 'underline' }}>
                Resident Login →
              </Link>
              <Link to="/admin/login" style={{ color: 'var(--primary-blue)', textDecoration: 'underline' }}>
                Admin Login →
              </Link>
            </div>
          </div>

          {/* Hero Visual Mockup */}
          <div className="hero-visual">
            <div className="preview-card">
              <div className="preview-header">
                <span className="dot dot-red"></span>
                <span className="dot dot-yellow"></span>
                <span className="dot dot-green"></span>
                <span className="preview-title">PostgreSQL Live Data Engine</span>
              </div>
              <div className="preview-stats">
                <div className="p-stat">
                  <span className="p-label">Metered Volume</span>
                  <span className="p-value">Live kL</span>
                </div>
                <div className="p-stat">
                  <span className="p-label">Tiered Tariff</span>
                  <span className="p-value">Progressive</span>
                </div>
                <div className="p-stat">
                  <span className="p-label">Leak Detection</span>
                  <span className="p-value">Mean + 2σ</span>
                </div>
                <div className="p-stat">
                  <span className="p-label">Invoices</span>
                  <span className="p-value">iText PDF</span>
                </div>
              </div>
              <div className="preview-chart-mock">
                <div className="bar-mock" style={{ height: '55%' }}><span className="bar-label">Tier 1</span></div>
                <div className="bar-mock" style={{ height: '80%' }}><span className="bar-label">Tier 2</span></div>
                <div className="bar-mock" style={{ height: '40%' }}><span className="bar-label">Shared</span></div>
                <div className="bar-mock" style={{ height: '95%' }}><span className="bar-label">Invoice</span></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Problem & Solution Statement */}
      <section className="section overview-section" style={{ background: '#EEF4FA' }}>
        <div className="section-container">
          <div className="section-header">
            <h2>Problem Statement & Project Objective</h2>
            <p style={{ maxWidth: '800px', margin: '0 auto' }}>
              Urban apartment communities face severe challenges: unmonitored residential water wastage, lack of individual consumption visibility, and unfair flat-rate division of bulk water procurement costs.
            </p>
          </div>

          <div className="grid-cols-3">
            <div className="overview-card" style={{ background: '#fff' }}>
              <div className="card-icon">🚨</div>
              <h3>The Challenge</h3>
              <p>
                Flats without individual submeter tracking waste water without accountability, while fixed equal maintenance bills penalize low-consumption households.
              </p>
            </div>
            <div className="overview-card" style={{ background: '#fff' }}>
              <div className="card-icon">💡</div>
              <h3>The Proposed Solution</h3>
              <p>
                A software-only web platform integrating smart meter logs, progressive tiered tariffs, proportional shared tanker cost allocation, and automated PDF billing.
              </p>
            </div>
            <div className="overview-card" style={{ background: '#fff' }}>
              <div className="card-icon">🎯</div>
              <h3>Core Objective</h3>
              <p>
                Deliver transparency, incentivize water conservation, detect leaks statistically before structural damage occurs, and streamline billing operations.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Core Platform Capabilities */}
      <section className="section features-section">
        <div className="section-container">
          <div className="section-header">
            <h2>Comprehensive Platform Capabilities</h2>
            <p>Production features designed for both residents and apartment owners.</p>
          </div>

          <div className="grid-cols-3">
            <div className="feature-item">
              <h4>💧 Household Meter Monitoring</h4>
              <p>Track daily & monthly consumption per household derived from manual or submeter readings.</p>
            </div>
            <div className="feature-item">
              <h4>📊 Recharts Analytics & Visuals</h4>
              <p>Interactive bar and line charts depicting usage trends, peak spikes, and comparative metrics.</p>
            </div>
            <div className="feature-item">
              <h4>🏷️ Configurable Tiered Tariffs</h4>
              <p>Progressive rate slabs (e.g. 0-10 kL @ $20, 10-20 kL @ $30) to discourage excessive consumption.</p>
            </div>
            <div className="feature-item">
              <h4>🚛 Bulk Water Procurement</h4>
              <p>Track municipal and private supplier tanker deliveries, volume, and total procurement cost.</p>
            </div>
            <div className="feature-item">
              <h4>⚖️ Shared-Cost Allocation</h4>
              <p>Distribute common bulk water costs proportionally to metered consumption, using flat area fallback for unmetered units.</p>
            </div>
            <div className="feature-item">
              <h4>🧾 Automated PDF Invoicing</h4>
              <p>Generate itemized PDF invoices for every billing cycle with custom breakups and payment statuses.</p>
            </div>
            <div className="feature-item">
              <h4>⚠️ Statistical Leak Alerts</h4>
              <p>Automated anomaly scan detecting consumption spikes &gt; 2 standard deviations above 30-day baseline.</p>
            </div>
            <div className="feature-item">
              <h4>📂 Bulk CSV Upload & Validation</h4>
              <p>Upload batch meter readings via CSV with duplicate detection and error validation checks.</p>
            </div>
            <div className="feature-item">
              <h4>🌱 Conservation & Benchmarking</h4>
              <p>Compare household consumption against community averages and receive personalized eco-tips.</p>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="section workflow-section" style={{ background: '#F8FAFC' }}>
        <div className="section-container">
          <div className="section-header">
            <h2>How The Platform Works</h2>
            <p>End-to-end workflow from reading entry to invoice settlement.</p>
          </div>

          <div className="workflow-steps">
            <div className="step-card">
              <div className="step-number">01</div>
              <h3>1. Setup & Configure</h3>
              <p>Admins register apartments, household flats, and assigned meter serial numbers.</p>
            </div>
            <div className="step-card">
              <div className="step-number">02</div>
              <h3>2. Record Readings</h3>
              <p>Meter logs are entered manually or uploaded in batch via formatted CSV files.</p>
            </div>
            <div className="step-card">
              <div className="step-number">03</div>
              <h3>3. Calculate & Allocate</h3>
              <p>Progressive tariffs and shared tanker costs are calculated per household cycle.</p>
            </div>
            <div className="step-card">
              <div className="step-number">04</div>
              <h3>4. Finalize & Notify</h3>
              <p>Bills are finalized, PDF invoices rendered, and leak alerts dispatched to residents.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Technology Stack Section */}
      <section className="section tech-section" style={{ background: 'var(--primary-navy)', color: '#fff' }}>
        <div className="section-container">
          <div className="section-header" style={{ color: '#fff' }}>
            <h2 style={{ color: '#fff' }}>Production Technology Stack</h2>
            <p style={{ color: 'rgba(255,255,255,0.8)' }}>
              Built with industry-standard web frameworks and security protocols.
            </p>
          </div>

          <div className="grid-cols-4">
            <div className="tech-card" style={{ background: 'rgba(255,255,255,0.08)', padding: '1.25rem', borderRadius: '8px' }}>
              <h3 style={{ color: 'var(--primary-teal)' }}>Frontend</h3>
              <p style={{ fontSize: '0.9rem', margin: 0 }}>React 18, Vite, React Router DOM, Recharts & Axios</p>
            </div>
            <div className="tech-card" style={{ background: 'rgba(255,255,255,0.08)', padding: '1.25rem', borderRadius: '8px' }}>
              <h3 style={{ color: 'var(--primary-teal)' }}>Backend API</h3>
              <p style={{ fontSize: '0.9rem', margin: 0 }}>Java 21, Spring Boot 3.3.4, Spring Data JPA & iText PDF</p>
            </div>
            <div className="tech-card" style={{ background: 'rgba(255,255,255,0.08)', padding: '1.25rem', borderRadius: '8px' }}>
              <h3 style={{ color: 'var(--primary-teal)' }}>Database</h3>
              <p style={{ fontSize: '0.9rem', margin: 0 }}>PostgreSQL 17 & Flyway Versioned Migration Scripts</p>
            </div>
            <div className="tech-card" style={{ background: 'rgba(255,255,255,0.08)', padding: '1.25rem', borderRadius: '8px' }}>
              <h3 style={{ color: 'var(--primary-teal)' }}>Security</h3>
              <p style={{ fontSize: '0.9rem', margin: 0 }}>Spring Security 6, BCrypt Hashing, JWT Authentication & RBAC</p>
            </div>
          </div>
        </div>
      </section>

      {/* Call to Action with Separate Portals */}
      <section className="section cta-section">
        <div className="section-container">
          <h2>Select Your Portal to Enter</h2>
          <p style={{ maxWidth: '600px', margin: '0 auto 1.5rem auto' }}>
            Access your dedicated portal for household tracking or community administration.
          </p>
          <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
            <Link to="/resident/login" className="btn btn-cta" style={{ background: 'var(--primary-blue)' }}>
              🏠 Resident Portal & Login
            </Link>
            <Link to="/admin/login" className="btn btn-cta" style={{ background: 'var(--primary-navy)' }}>
              🛡️ Apartment Owner / Admin Portal
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
};

export default LandingPage;
