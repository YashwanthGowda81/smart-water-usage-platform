import React from 'react';
import { Link as RouterLink } from 'react-router-dom';

const HowItWorksPage = () => {
  return (
    <div className="public-page-container">
      <div className="public-page-header">
        <h1>How Smart Water Works</h1>
        <p>A step-by-step walkthrough of apartment setup, meter readings, and data insights.</p>
      </div>

      <div className="card" style={{ marginBottom: '2.5rem' }}>
        <h2 className="card-title">Core Active Workflow</h2>
        <div className="workflow-diagram">
          <div className="diag-box">
            <strong>1. Apartment Setup</strong>
            <span>Register complex & flats</span>
          </div>
          <div className="diag-arrow">➔</div>
          <div className="diag-box">
            <strong>2. Household Reg</strong>
            <span>Set flat & occupancy</span>
          </div>
          <div className="diag-arrow">➔</div>
          <div className="diag-box">
            <strong>3. Meter Assign</strong>
            <span>Link serial number</span>
          </div>
          <div className="diag-arrow">➔</div>
          <div className="diag-box">
            <strong>4. Water Reading</strong>
            <span>Manual / CSV Upload</span>
          </div>
          <div className="diag-arrow">➔</div>
          <div className="diag-box">
            <strong>5. Consumption</strong>
            <span>`current - previous`</span>
          </div>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '2.5rem' }}>
        <h2 className="card-title">Planned Future Billing Workflow</h2>
        <div className="workflow-diagram">
          <div className="diag-box planned">
            <strong>Consumption Data</strong>
            <span>Calculated kL usage</span>
          </div>
          <div className="diag-arrow">➔</div>
          <div className="diag-box planned">
            <strong>Tariff Engine</strong>
            <span>Configurable rate tiers</span>
          </div>
          <div className="diag-arrow">➔</div>
          <div className="diag-box planned">
            <strong>Shared Cost</strong>
            <span>Common area distribution</span>
          </div>
          <div className="diag-arrow">➔</div>
          <div className="diag-box planned">
            <strong>Invoice Gen</strong>
            <span>PDF Invoices</span>
          </div>
          <div className="diag-arrow">➔</div>
          <div className="diag-box planned">
            <strong>Alerts</strong>
            <span>Leakage warnings</span>
          </div>
        </div>
      </div>

      <div className="grid-cols-2">
        <div className="card">
          <h3>👨‍💼 For Apartment Administrators</h3>
          <ol style={{ paddingLeft: '1.25rem', lineHeight: '2', color: '#475569', marginTop: '0.5rem' }}>
            <li>Create an Apartment record (e.g. Green Valley Residency).</li>
            <li>Register households (A-101, A-102...) with square footage.</li>
            <li>Assign water meters to each household flat.</li>
            <li>Record daily/monthly readings or upload CSV callsets.</li>
          </ol>
        </div>

        <div className="card">
          <h3>🏠 For Apartment Residents</h3>
          <ol style={{ paddingLeft: '1.25rem', lineHeight: '2', color: '#475569', marginTop: '0.5rem' }}>
            <li>Sign in using assigned resident credentials.</li>
            <li>View household flat specifications and assigned meters.</li>
            <li>Track weekly/monthly water consumption trends.</li>
            <li>Follow conservation tips to reduce water usage.</li>
          </ol>
        </div>
      </div>

      <div style={{ textAlign: 'center', marginTop: '2.5rem' }}>
        <RouterLink to="/login" className="btn btn-primary" style={{ padding: '0.75rem 2rem' }}>
          Test Workflow Live
        </RouterLink>
      </div>
    </div>
  );
};

export default HowItWorksPage;
