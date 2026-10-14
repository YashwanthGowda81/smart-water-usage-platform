import React, { useState, useEffect } from 'react';
import api from '../services/api';

const MeterManagement = () => {
  const [households, setHouseholds] = useState([]);
  const [meters, setMeters] = useState([]);
  const [householdId, setHouseholdId] = useState('');
  const [meterNumber, setMeterNumber] = useState('');
  const [meterType, setMeterType] = useState('DIGITAL');
  const [installationDate, setInstallationDate] = useState(new Date().toISOString().split('T')[0]);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      const [hhRes, mtRes] = await Promise.all([
        api.get('/households'),
        api.get('/meters'),
      ]);
      setHouseholds(hhRes.data || []);
      setMeters(mtRes.data || []);
      if (hhRes.data && hhRes.data.length > 0 && !householdId) {
        setHouseholdId(hhRes.data[0].id);
      }
    } catch (err) {
      console.error('API error loading meters:', err);
      setMessage({ type: 'danger', text: 'Failed to load meters from server.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage({ type: '', text: '' });
    try {
      await api.post('/meters', {
        householdId: parseInt(householdId),
        meterNumber,
        meterType,
        installationDate,
      });
      setMessage({ type: 'success', text: `Meter '${meterNumber}' configured successfully!` });
      setMeterNumber('');
      loadData();
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Failed to configure meter.';
      setMessage({ type: 'danger', text: errMsg });
    }
  };

  const handleDelete = async (id, mtrNum) => {
    if (window.confirm(`Delete meter configuration '${mtrNum}'?`)) {
      try {
        await api.delete(`/meters/${id}`);
        setMessage({ type: 'success', text: `Meter '${mtrNum}' deleted.` });
        loadData();
      } catch (err) {
        const errMsg = err.response?.data?.message || 'Failed to delete meter.';
        setMessage({ type: 'danger', text: errMsg });
      }
    }
  };

  return (
    <div>
      <h1 style={{ fontSize: '1.75rem', marginBottom: '0.5rem' }}>Meter Configuration</h1>
      <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>Assign smart digital or submeter devices to apartment households.</p>

      {message.text && <div className={`alert alert-${message.type}`}>{message.text}</div>}

      <div className="grid-cols-2">
        <div className="card">
          <h2 className="card-title">Configure & Assign Meter</h2>
          {households.length === 0 ? (
            <p className="alert alert-warning">Please register at least one household flat first.</p>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Select Household Flat</label>
                <select className="form-control" value={householdId} onChange={(e) => setHouseholdId(e.target.value)} required>
                  {households.map((hh) => (
                    <option key={hh.id} value={hh.id}>
                      Flat {hh.flatNumber} - {hh.apartmentName}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Meter Serial Number</label>
                <input
                  type="text"
                  className="form-control"
                  value={meterNumber}
                  onChange={(e) => setMeterNumber(e.target.value)}
                  placeholder="e.g. MTR-1001"
                  required
                />
              </div>

              <div className="grid-cols-2" style={{ gap: '0.75rem' }}>
                <div className="form-group">
                  <label className="form-label">Meter Type</label>
                  <select className="form-control" value={meterType} onChange={(e) => setMeterType(e.target.value)}>
                    <option value="DIGITAL">DIGITAL</option>
                    <option value="ANALOG">ANALOG</option>
                    <option value="SUBMETER">SUBMETER</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Installation Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={installationDate}
                    onChange={(e) => setInstallationDate(e.target.value)}
                    required
                  />
                </div>
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '0.5rem' }}>
                Save Meter Configuration
              </button>
            </form>
          )}
        </div>

        <div className="card">
          <h2 className="card-title">Active Meter Hardware ({meters.length})</h2>
          {loading ? (
            <p>Loading meters...</p>
          ) : meters.length === 0 ? (
            <p style={{ color: 'var(--text-secondary)' }}>No meters configured in PostgreSQL database yet. Use the form to assign one.</p>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Meter #</th>
                    <th>Assigned Flat</th>
                    <th>Type</th>
                    <th>Installed</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {meters.map((mt) => (
                    <tr key={mt.id}>
                      <td><strong>{mt.meterNumber}</strong></td>
                      <td>Flat {mt.flatNumber} ({mt.apartmentName})</td>
                      <td>{mt.meterType}</td>
                      <td>{mt.installationDate}</td>
                      <td>
                        <span className="badge badge-success">{mt.status}</span>
                      </td>
                      <td>
                        <button onClick={() => handleDelete(mt.id, mt.meterNumber)} className="btn btn-danger" style={{ padding: '0.25rem 0.5rem', fontSize: '0.8rem' }}>
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MeterManagement;
