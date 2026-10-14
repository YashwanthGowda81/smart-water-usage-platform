import React from 'react';
import { Link } from 'react-router-dom';

const FeaturesPage = () => {
  return (
    <div className="public-page-container">
      <div className="public-page-header">
        <h1>Platform Features</h1>
        <p>Explore currently active capabilities and planned architectural modules.</p>
      </div>

      <h2 style={{ fontSize: '1.4rem', marginBottom: '1rem', color: '#1e293b' }}>
        Current Active Features (Milestone 1)
      </h2>
      <div className="grid-cols-2" style={{ marginBottom: '2.5rem' }}>
        <div className="card">
          <h3 className="card-title">💧 Water Usage Logging</h3>
          <p style={{ color: '#475569' }}>
            Record daily or weekly water readings per meter. The system validates reading continuity (current &ge; previous), prevents negative inputs, and calculates consumption in kiloliters (kL).
          </p>
        </div>

        <div className="card">
          <h3 className="card-title">📂 Bulk CSV Upload</h3>
          <p style={{ color: '#475569' }}>
            Upload multi-row CSV files containing meter numbers, reading dates, and meter values. Features row-by-row validation, duplicate row detection, and structured summary reporting.
          </p>
        </div>

        <div className="card">
          <h3 className="card-title">🏢 Apartment & Household Management</h3>
          <p style={{ color: '#475569' }}>
            Register apartment societies, total units, flat numbers, resident occupancy counts, square footage, and dedicated submetering flags.
          </p>
        </div>

        <div className="card">
          <h3 className="card-title">📟 Meter Configuration</h3>
          <p style={{ color: '#475569' }}>
            Configure digital, analog, or submeter hardware serial numbers, installation dates, and status assignments to household flats.
          </p>
        </div>

        <div className="card">
          <h3 className="card-title">📊 Admin & Resident Dashboards</h3>
          <p style={{ color: '#475569' }}>
            Role-tailored interfaces showing real-time statistics, monthly consumption graphs, recent logs, and household management controls.
          </p>
        </div>

        <div className="card">
          <h3 className="card-title">🛡️ Security & Role-Based Control</h3>
          <p style={{ color: '#475569' }}>
            Spring Security 6 with BCrypt password hashing, JWT authentication, and role authorization enforcing Admin vs. Resident access rules.
          </p>
        </div>
      </div>

      <h2 style={{ fontSize: '1.4rem', marginBottom: '1rem', color: '#1e293b' }}>
        Automated Billing & Analytics Modules (Phase 2)
      </h2>
      <div className="grid-cols-2" style={{ marginBottom: '2rem' }}>
        <div className="card">
          <h3 className="card-title">🧾 Tiered Tariff & Billing Engine</h3>
          <p style={{ color: '#475569' }}>
            Configurable rate tiers (e.g., 0-10 kL @ ₹20, &gt;10 kL @ ₹30), billing cycle state machine, bulk water procurement tracking (tanker & municipal), fair consumption-proportional shared cost allocation, and Apache PDFBox invoice generation.
          </p>
        </div>

        <div className="card">
          <h3 className="card-title">⚠️ Anomaly & Leak Alerts</h3>
          <p style={{ color: '#475569' }}>
            Statistical outlier algorithms for automated leak indicators, persistent anomaly event logging with duplicate alert defense, and in-app resident notifications.
          </p>
        </div>
      </div>

      <div style={{ textAlign: 'center', marginTop: '2rem', display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
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

export default FeaturesPage;
