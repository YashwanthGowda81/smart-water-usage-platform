import React, { useContext, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import api from '../services/api';

const ResidentDashboard = () => {
  const { user } = useContext(AuthContext);
  const [households, setHouseholds] = useState([]);
  const [meters, setMeters] = useState([]);
  const [usageLogs, setUsageLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [hhRes, mtRes, usRes] = await Promise.all([
          api.get('/households'),
          api.get('/meters'),
          api.get('/usage'),
        ]);
        setHouseholds(hhRes.data || []);
        setMeters(mtRes.data || []);
        setUsageLogs(usRes.data || []);
      } catch (err) {
        console.warn('API error loading resident dashboard data:', err);
        setHouseholds([]);
        setMeters([]);
        setUsageLogs([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // Calculate statistics
  const totalConsumption = usageLogs.reduce((acc, log) => acc + (parseFloat(log.consumption) || 0), 0);
  const latestLog = usageLogs.length > 0 ? usageLogs[0] : null;

  return (
    <div style={{ maxWidth: '1240px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      {/* Resident Navigation Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', marginBottom: '0.25rem', color: 'var(--primary-navy)' }}>Resident Portal</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Welcome back, <strong>{user?.name || 'Resident'}</strong>! Track consumption, view itemized water bills, and monitor leak alerts.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <Link to="/" className="btn btn-secondary">
            ← Public Site
          </Link>
          <Link to="/resident/bills" className="btn btn-primary">
            🧾 My Water Bills
          </Link>
          <Link to="/resident/benchmarking" className="btn btn-primary" style={{ background: 'var(--primary-teal)' }}>
            🌱 Conservation Score
          </Link>
          <Link to="/resident/notifications" className="btn btn-secondary">
            🔔 Notifications
          </Link>
        </div>
      </div>

      {loading ? (
        <p>Loading resident details...</p>
      ) : (
        <>
          <div className="grid-cols-3" style={{ marginBottom: '2rem' }}>
            <div className="stat-card">
              <span className="stat-label">Total Water Consumed</span>
              <span className="stat-value">{totalConsumption.toFixed(2)} kL</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Latest Logged Consumption</span>
              <span className="stat-value" style={{ color: 'var(--success)' }}>
                {latestLog ? `${latestLog.consumption} kL` : '0 kL'}
              </span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Active Household Meters</span>
              <span className="stat-value">{meters.length}</span>
            </div>
          </div>

          <div className="grid-cols-2" style={{ marginBottom: '1.5rem' }}>
            <div className="card">
              <h2 className="card-title">🏠 Household Details</h2>
              {households.length === 0 ? (
                <p style={{ color: 'var(--text-secondary)' }}>No household unit assigned in PostgreSQL yet. Contact your administrator.</p>
              ) : (
                <ul style={{ listStyle: 'none', lineHeight: '2' }}>
                  {households.map((hh) => (
                    <li key={hh.id} style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
                      <strong>Flat {hh.flatNumber}</strong> - {hh.apartmentName || 'Apartment'}
                      <br />
                      <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                        Occupancy: {hh.occupancy} residents | Area: {hh.areaSqft} sqft | Metered: {hh.metered ? 'Yes' : 'No'}
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <div className="card">
              <h2 className="card-title">📟 Assigned Meters</h2>
              {meters.length === 0 ? (
                <p style={{ color: 'var(--text-secondary)' }}>No water meters configured for your unit.</p>
              ) : (
                <ul style={{ listStyle: 'none', lineHeight: '2' }}>
                  {meters.map((mt) => (
                    <li key={mt.id} style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
                      <strong>Meter #{mt.meterNumber}</strong> ({mt.meterType})
                      <br />
                      <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                        Installed on {mt.installationDate} | Status: {mt.status}
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>

          {/* Resident Action Feature Cards */}
          <div className="grid-cols-3" style={{ marginBottom: '1.5rem' }}>
            <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <h3 className="card-title" style={{ color: 'var(--primary-navy)' }}>🧾 Monthly Water Invoices</h3>
                <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                  View itemized breakdown of metered usage, progressive rates, and shared common area costs. Download PDF invoices.
                </p>
              </div>
              <Link to="/resident/bills" className="btn btn-primary" style={{ marginTop: '1rem', textAlign: 'center' }}>
                View Invoices & PDFs
              </Link>
            </div>

            <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <h3 className="card-title" style={{ color: 'var(--primary-navy)' }}>🌱 Community Benchmarking</h3>
                <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                  Compare your household 30-day water consumption against peer average and view custom conservation tips.
                </p>
              </div>
              <Link to="/resident/benchmarking" className="btn btn-primary" style={{ marginTop: '1rem', textAlign: 'center', background: 'var(--primary-teal)' }}>
                View Benchmarks
              </Link>
            </div>

            <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <h3 className="card-title" style={{ color: 'var(--primary-navy)' }}>⚠️ Leak Alerts & Notices</h3>
                <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                  Instant statistical anomaly alerts when consumption spikes are detected by automated leak scanning.
                </p>
              </div>
              <Link to="/resident/notifications" className="btn btn-secondary" style={{ marginTop: '1rem', textAlign: 'center' }}>
                Open Alert Center
              </Link>
            </div>
          </div>

          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h2 className="card-title" style={{ margin: 0 }}>💧 Recent Consumption Logs</h2>
              <Link to="/resident/usage" className="btn btn-primary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}>
                View Full History
              </Link>
            </div>

            {usageLogs.length === 0 ? (
              <p style={{ color: 'var(--text-secondary)' }}>No water usage readings logged in PostgreSQL database yet.</p>
            ) : (
              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>Date</th>
                      <th>Meter #</th>
                      <th>Previous Reading</th>
                      <th>Current Reading</th>
                      <th>Consumption (kL)</th>
                    </tr>
                  </thead>
                  <tbody>
                    {usageLogs.slice(0, 5).map((log) => (
                      <tr key={log.id}>
                        <td>{log.readingDate}</td>
                        <td>{log.meterNumber}</td>
                        <td>{log.previousReading} kL</td>
                        <td>{log.currentReading} kL</td>
                        <td style={{ color: 'var(--primary-blue)', fontWeight: 'bold' }}>{log.consumption} kL</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
};

export default ResidentDashboard;
