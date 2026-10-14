import React from 'react';
import { Link } from 'react-router-dom';

const WaterSavingPage = () => {
  return (
    <div className="public-page-container">
      <div className="public-page-header">
        <h1>Water Saving & Conservation</h1>
        <p>Simple, effective ways apartment communities can conserve water and reduce costs.</p>
      </div>

      <div className="grid-cols-2" style={{ marginBottom: '2rem' }}>
        <div className="card">
          <h2 className="card-title">🚿 Daily Household Habits</h2>
          <ul style={{ paddingLeft: '1.25rem', lineHeight: '1.8', color: '#475569' }}>
            <li>Turn off taps while brushing teeth or soaping dishes to save up to 6 liters per minute.</li>
            <li>Install low-flow aerators on kitchen and bathroom faucets.</li>
            <li>Run washing machines and dishwashers with full loads only.</li>
            <li>Opt for short showers instead of full baths.</li>
          </ul>
        </div>

        <div className="card">
          <h2 className="card-title">🔍 Detecting Household Leaks</h2>
          <ul style={{ paddingLeft: '1.25rem', lineHeight: '1.8', color: '#475569' }}>
            <li>Regularly check flush tanks and faucets for silent water trickles.</li>
            <li>Monitor meter reading trends for sudden unexplained increases.</li>
            <li>Inspect pipe joints and under-sink plumbing for moisture or corrosion.</li>
            <li>Report community water line leaks immediately to administrators.</li>
          </ul>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '2rem' }}>
        <h2 className="card-title">🏢 Apartment Community Best Practices</h2>
        <div className="grid-cols-3" style={{ gap: '1rem', marginTop: '1rem' }}>
          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '6px' }}>
            <h4>Common Area Monitoring</h4>
            <p style={{ fontSize: '0.9rem', color: '#64748b', marginTop: '0.35rem' }}>
              Submeter gardens, swimming pools, and clubhouse washrooms to prevent unnoticed wastage.
            </p>
          </div>
          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '6px' }}>
            <h4>Rainwater Harvesting</h4>
            <p style={{ fontSize: '0.9rem', color: '#64748b', marginTop: '0.35rem' }}>
              Utilize rooftop rainwater capture systems to recharge groundwater and supply non-potable needs.
            </p>
          </div>
          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '6px' }}>
            <h4>Transparent Submetering</h4>
            <p style={{ fontSize: '0.9rem', color: '#64748b', marginTop: '0.35rem' }}>
              Provide every household flat with individual meter data to incentivize individual conservation.
            </p>
          </div>
        </div>
      </div>

      <div style={{ textAlign: 'center', marginTop: '2.5rem', display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
        <Link to="/resident/login" className="btn btn-primary" style={{ padding: '0.75rem 2rem' }}>
          Resident Sign In & Usage Tracking
        </Link>
        <Link to="/admin/login" className="btn btn-secondary" style={{ padding: '0.75rem 2rem', background: 'var(--primary-navy)', color: '#fff' }}>
          Admin Console
        </Link>
      </div>
    </div>
  );
};

export default WaterSavingPage;
