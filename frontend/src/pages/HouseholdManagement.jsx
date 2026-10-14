import React, { useState, useEffect } from 'react';
import api from '../services/api';

const HouseholdManagement = () => {
  const [apartments, setApartments] = useState([]);
  const [households, setHouseholds] = useState([]);
  const [apartmentId, setApartmentId] = useState('');
  const [flatNumber, setFlatNumber] = useState('');
  const [occupancy, setOccupancy] = useState(3);
  const [areaSqft, setAreaSqft] = useState(1200);
  const [metered, setMetered] = useState(true);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      const [apRes, hhRes] = await Promise.all([
        api.get('/apartments'),
        api.get('/households'),
      ]);
      setApartments(apRes.data || []);
      setHouseholds(hhRes.data || []);
      if (apRes.data && apRes.data.length > 0 && !apartmentId) {
        setApartmentId(apRes.data[0].id);
      }
    } catch (err) {
      console.error('API load failed in household management:', err);
      setMessage({ type: 'danger', text: 'Failed to load households from server.' });
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
      await api.post('/households', {
        apartmentId: parseInt(apartmentId),
        flatNumber,
        occupancy: parseInt(occupancy),
        areaSqft: parseFloat(areaSqft),
        metered,
      });
      setMessage({ type: 'success', text: `Household flat '${flatNumber}' registered successfully!` });
      setFlatNumber('');
      setOccupancy(3);
      setAreaSqft(1200);
      loadData();
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Failed to create household.';
      setMessage({ type: 'danger', text: errMsg });
    }
  };

  const handleDelete = async (id, flatNum) => {
    if (window.confirm(`Delete household flat '${flatNum}'?`)) {
      try {
        await api.delete(`/households/${id}`);
        setMessage({ type: 'success', text: `Household '${flatNum}' deleted.` });
        loadData();
      } catch (err) {
        const errMsg = err.response?.data?.message || 'Failed to delete household.';
        setMessage({ type: 'danger', text: errMsg });
      }
    }
  };

  return (
    <div>
      <h1 style={{ fontSize: '1.75rem', marginBottom: '0.5rem' }}>Household Management</h1>
      <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>Manage apartment flats, resident occupancy, and area metrics.</p>

      {message.text && <div className={`alert alert-${message.type}`}>{message.text}</div>}

      <div className="grid-cols-2">
        <div className="card">
          <h2 className="card-title">Register Household Unit</h2>
          {apartments.length === 0 ? (
            <p className="alert alert-warning">Please register at least one apartment community first.</p>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Select Apartment Community</label>
                <select className="form-control" value={apartmentId} onChange={(e) => setApartmentId(e.target.value)} required>
                  {apartments.map((ap) => (
                    <option key={ap.id} value={ap.id}>
                      {ap.name} ({ap.city})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Flat / Unit Number</label>
                <input
                  type="text"
                  className="form-control"
                  value={flatNumber}
                  onChange={(e) => setFlatNumber(e.target.value)}
                  placeholder="e.g. A-101, B-402"
                  required
                />
              </div>

              <div className="grid-cols-2" style={{ gap: '0.75rem' }}>
                <div className="form-group">
                  <label className="form-label">Occupancy Count</label>
                  <input
                    type="number"
                    min="1"
                    className="form-control"
                    value={occupancy}
                    onChange={(e) => setOccupancy(e.target.value)}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Area (Sq. Ft.)</label>
                  <input
                    type="number"
                    min="10"
                    step="0.01"
                    className="form-control"
                    value={areaSqft}
                    onChange={(e) => setAreaSqft(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.5rem' }}>
                <input
                  type="checkbox"
                  id="meteredCheck"
                  checked={metered}
                  onChange={(e) => setMetered(e.target.checked)}
                />
                <label htmlFor="meteredCheck" style={{ margin: 0, cursor: 'pointer', fontWeight: 600 }}>
                  Has Dedicated Water Meter
                </label>
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }}>
                Save Household
              </button>
            </form>
          )}
        </div>

        <div className="card">
          <h2 className="card-title">Registered Households ({households.length})</h2>
          {loading ? (
            <p>Loading households...</p>
          ) : households.length === 0 ? (
            <p style={{ color: 'var(--text-secondary)' }}>No households registered in PostgreSQL database yet. Use the form to add one.</p>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Flat #</th>
                    <th>Apartment</th>
                    <th>Occupancy</th>
                    <th>Area (sqft)</th>
                    <th>Metered</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {households.map((hh) => (
                    <tr key={hh.id}>
                      <td><strong>{hh.flatNumber}</strong></td>
                      <td>{hh.apartmentName}</td>
                      <td>{hh.occupancy} residents</td>
                      <td>{hh.areaSqft} sqft</td>
                      <td>
                        <span className={`badge ${hh.metered ? 'badge-success' : 'badge-warning'}`}>
                          {hh.metered ? 'METERED' : 'UNMETERED'}
                        </span>
                      </td>
                      <td>
                        <button onClick={() => handleDelete(hh.id, hh.flatNumber)} className="btn btn-danger" style={{ padding: '0.25rem 0.5rem', fontSize: '0.8rem' }}>
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

export default HouseholdManagement;
