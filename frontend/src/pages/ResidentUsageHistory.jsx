import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

const ResidentUsageHistory = () => {
  const [usageLogs, setUsageLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUsage = async () => {
      try {
        const res = await api.get('/usage');
        setUsageLogs(res.data || []);
      } catch (err) {
        console.error('API error fetching usage logs:', err);
        setUsageLogs([]);
      } finally {
        setLoading(false);
      }
    };

    fetchUsage();
  }, []);

  return (
    <div style={{ maxWidth: '1240px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', marginBottom: '0.25rem', color: 'var(--primary-navy)' }}>Water Consumption History</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Detailed log of daily water meter readings and calculated usage volume.</p>
        </div>
        <Link to="/resident/dashboard" className="btn btn-secondary">
          ← Resident Portal
        </Link>
      </div>

      <div className="card">
        {loading ? (
          <p>Loading usage history...</p>
        ) : usageLogs.length === 0 ? (
          <p style={{ color: 'var(--text-secondary)' }}>No water usage readings logged in PostgreSQL database yet.</p>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Log ID</th>
                  <th>Reading Date</th>
                  <th>Meter Number</th>
                  <th>Flat / Unit</th>
                  <th>Previous Reading</th>
                  <th>Current Reading</th>
                  <th>Consumption</th>
                  <th>Reading Source</th>
                </tr>
              </thead>
              <tbody>
                {usageLogs.map((log) => (
                  <tr key={log.id}>
                    <td>#{log.id}</td>
                    <td>{log.readingDate}</td>
                    <td><strong>{log.meterNumber}</strong></td>
                    <td>Flat {log.flatNumber}</td>
                    <td>{log.previousReading} kL</td>
                    <td>{log.currentReading} kL</td>
                    <td style={{ color: 'var(--primary-blue)', fontWeight: 'bold' }}>{log.consumption} kL</td>
                    <td>
                      <span className={`badge ${log.source === 'CSV' ? 'badge-warning' : 'badge-success'}`}>
                        {log.source}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default ResidentUsageHistory;
