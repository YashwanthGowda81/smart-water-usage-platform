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
        Planned Future Modules
      </h2>
      <div className="grid-cols-2" style={{ marginBottom: '2rem' }}>
        <div className="card" style={{ borderColor: '#cbd5e1', background: '#f8fafc' }}>
          <span className="badge-planned">Module Under Development</span>
          <h3 className="card-title" style={{ marginTop: '0.5rem' }}>🧾 Tiered Tariff & Billing Engine</h3>
          <p style={{ color: '#64748b' }}>
            Configurable rate tiers (e.g., 0-10 kL @ ₹20, 10-20 kL @ ₹30), billing cycles, bulk water procurement tracking, shared cost distribution, and PDF invoice generation.
          </p>
        </div>

        <div className="card" style={{ borderColor: '#cbd5e1', background: '#f8fafc' }}>
          <span className="badge-planned">Module Under Development</span>
          <h3 className="card-title" style={{ marginTop: '0.5rem' }}>⚠️ Anomaly & Leak Alerts</h3>
          <p style={{ color: '#64748b' }}>
            Statistical mean + 2 std-dev algorithms for automated leak indicators, scheduled background checks, and in-app/email notifications.
          </p>
        </div>
      </div>

      <div style={{ textAlign: 'center', marginTop: '2rem' }}>
        <Link to="/login" className="btn btn-primary" style={{ padding: '0.75rem 2rem' }}>
          Get Started Now
        </Link>
      </div>
    </div>
  );
};

export default FeaturesPage;
