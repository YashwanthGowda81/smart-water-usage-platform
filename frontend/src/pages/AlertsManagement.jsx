import React, { useState, useEffect } from 'react';
import api from '../services/api';

const AlertsManagement = () => {
  const [households, setHouseholds] = useState([]);
  const [selectedHouseholdId, setSelectedHouseholdId] = useState('');
  const [anomalies, setAnomalies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  useEffect(() => {
    fetchHouseholds();
  }, []);

  useEffect(() => {
    if (selectedHouseholdId) {
      fetchAnomalies(selectedHouseholdId);
    }
  }, [selectedHouseholdId]);

  const fetchHouseholds = async () => {
    try {
      const res = await api.get('/households');
      setHouseholds(res.data || []);
      if (res.data && res.data.length > 0) {
        setSelectedHouseholdId(res.data[0].id);
      }
    } catch (err) {
      console.error('Failed to load households:', err);
    }
  };

  const fetchAnomalies = async (hhId) => {
    setLoading(true);
    try {
      const res = await api.get(`/anomalies/household/${hhId}`);
      setAnomalies(res.data || []);
    } catch (err) {
      console.error('Failed to load anomalies:', err);
      setAnomalies([]);
    } finally {
      setLoading(false);
    }
  };

  const handleRunScan = async () => {
    if (!selectedHouseholdId) return;
    try {
      setLoading(true);
      await api.post(`/anomalies/check/${selectedHouseholdId}`);
      setMessage({ type: 'success', text: 'Statistical leak detection scan completed (Mean + 2×StdDev formula applied).' });
      fetchAnomalies(selectedHouseholdId);
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to run anomaly scan.' });
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStatus = async (anomalyId, newStatus) => {
    try {
      await api.put(`/anomalies/${anomalyId}/status`, null, { params: { status: newStatus } });
      setMessage({ type: 'success', text: `Alert updated to ${newStatus}.` });
      fetchAnomalies(selectedHouseholdId);
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to update status.' });
    }
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--primary-navy)', marginBottom: '0.25rem' }}>⚠️ Statistical Leak & Anomaly Alerts</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Automated detection of continuous water leaks or abnormal consumption spikes exceeding baseline mean + 2× standard deviations.</p>
        </div>
        <button className="btn btn-primary" onClick={handleRunScan} disabled={!selectedHouseholdId}>
          🔍 Run Statistical Leak Check
        </button>
      </div>

      {message.text && (
        <div className={`alert ${message.type === 'error' ? 'alert-danger' : 'alert-success'}`} style={{ marginBottom: '1rem' }}>
          {message.text}
        </div>
      )}

      {/* Household Selector */}
      {households.length > 0 && (
        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <label style={{ fontWeight: 'bold', marginRight: '1rem' }}>Select Household Unit:</label>
          <select
            value={selectedHouseholdId}
            onChange={(e) => setSelectedHouseholdId(e.target.value)}
            style={{ padding: '0.5rem 1rem', borderRadius: '6px', border: '1px solid var(--border-color)', minWidth: '250px' }}
          >
            {households.map(hh => (
              <option key={hh.id} value={hh.id}>Flat {hh.flatNumber} - {hh.apartmentName || 'Apartment'}</option>
            ))}
          </select>
        </div>
      )}

      {/* Alerts Table */}
      {loading ? (
        <p>Running statistical anomaly algorithms...</p>
      ) : anomalies.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem 1.5rem' }}>
          <span style={{ fontSize: '3rem' }}>✅</span>
          <h3>No Water Leak Anomalies Detected</h3>
          <p style={{ color: 'var(--text-secondary)' }}>All recent water meter readings are within normal statistical ranges.</p>
        </div>
      ) : (
        <div className="card">
          <h3 className="card-title">🚨 Active & Historical Leak Alerts</h3>
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Detected On</th>
                  <th>Flat #</th>
                  <th>Observed Consumption</th>
                  <th>Baseline Mean</th>
                  <th>Threshold (Mean + 2σ)</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {anomalies.map(a => (
                  <tr key={a.id}>
                    <td>{a.readingDate}</td>
                    <td><strong>Flat {a.flatNumber}</strong></td>
                    <td style={{ color: '#DC2626', fontWeight: 'bold' }}>{a.observedValue} kL</td>
                    <td>{a.baselineMean} kL</td>
                    <td>{a.thresholdValue} kL</td>
                    <td>
                      <span className="badge-planned" style={{
                        background: a.status === 'OPEN' ? '#FEE2E2' : a.status === 'RESOLVED' ? '#DCFCE7' : '#FEF3C7',
                        color: a.status === 'OPEN' ? '#991B1B' : a.status === 'RESOLVED' ? '#166534' : '#92400E'
                      }}>
                        {a.status}
                      </span>
                    </td>
                    <td>
                      {a.status === 'OPEN' && (
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button
                            className="btn btn-primary"
                            style={{ padding: '0.25rem 0.5rem', fontSize: '0.8rem', background: 'var(--success)' }}
                            onClick={() => handleUpdateStatus(a.id, 'RESOLVED')}
                          >
                            Mark Resolved
                          </button>
                          <button
                            className="btn btn-secondary"
                            style={{ padding: '0.25rem 0.5rem', fontSize: '0.8rem' }}
                            onClick={() => handleUpdateStatus(a.id, 'FALSE_POSITIVE')}
                          >
                            False Alarm
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default AlertsManagement;
