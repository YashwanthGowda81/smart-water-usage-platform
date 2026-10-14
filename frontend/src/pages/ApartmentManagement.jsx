import React, { useState, useEffect } from 'react';
import api from '../services/api';

const ApartmentManagement = () => {
  const [apartments, setApartments] = useState([]);
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [city, setCity] = useState('');
  const [state, setState] = useState('');
  const [pincode, setPincode] = useState('');
  const [totalUnits, setTotalUnits] = useState(10);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(true);

  const fetchApartments = async () => {
    try {
      const res = await api.get('/apartments');
      setApartments(res.data || []);
    } catch (err) {
      console.error('API error fetching apartments:', err);
      setMessage({ type: 'danger', text: 'Failed to load apartments from server.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchApartments();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage({ type: '', text: '' });
    try {
      await api.post('/apartments', {
        name,
        address,
        city,
        state,
        pincode,
        totalUnits: parseInt(totalUnits),
      });
      setMessage({ type: 'success', text: `Apartment '${name}' registered successfully!` });
      setName('');
      setAddress('');
      setCity('');
      setState('');
      setPincode('');
      setTotalUnits(10);
      fetchApartments();
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Failed to create apartment.';
      setMessage({ type: 'danger', text: errMsg });
    }
  };

  const handleDelete = async (id, apName) => {
    if (window.confirm(`Are you sure you want to delete apartment '${apName}'?`)) {
      try {
        await api.delete(`/apartments/${id}`);
        setMessage({ type: 'success', text: `Apartment '${apName}' deleted successfully.` });
        fetchApartments();
      } catch (err) {
        const errMsg = err.response?.data?.message || 'Failed to delete apartment.';
        setMessage({ type: 'danger', text: errMsg });
      }
    }
  };

  return (
    <div>
      <h1 style={{ fontSize: '1.75rem', marginBottom: '0.5rem' }}>Apartment Management</h1>
      <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>Configure apartment complexes and communities.</p>

      {message.text && <div className={`alert alert-${message.type}`}>{message.text}</div>}

      <div className="grid-cols-2">
        <div className="card">
          <h2 className="card-title">Register New Apartment</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Apartment Name</label>
              <input
                type="text"
                className="form-control"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Skyline Towers"
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Address</label>
              <input
                type="text"
                className="form-control"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                placeholder="123 Main Street, Sector 4"
                required
              />
            </div>
            <div className="grid-cols-2" style={{ gap: '0.75rem' }}>
              <div className="form-group">
                <label className="form-label">City</label>
                <input
                  type="text"
                  className="form-control"
                  value={city}
                  onChange={(e) => setCity(e.target.value)}
                  placeholder="Bengaluru"
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">State</label>
                <input
                  type="text"
                  className="form-control"
                  value={state}
                  onChange={(e) => setState(e.target.value)}
                  placeholder="Karnataka"
                  required
                />
              </div>
            </div>
            <div className="grid-cols-2" style={{ gap: '0.75rem' }}>
              <div className="form-group">
                <label className="form-label">Pincode</label>
                <input
                  type="text"
                  className="form-control"
                  value={pincode}
                  onChange={(e) => setPincode(e.target.value)}
                  placeholder="560001"
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Total Units / Flats</label>
                <input
                  type="number"
                  min="1"
                  className="form-control"
                  value={totalUnits}
                  onChange={(e) => setTotalUnits(e.target.value)}
                  required
                />
              </div>
            </div>
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
              Save Apartment
            </button>
          </form>
        </div>

        <div className="card">
          <h2 className="card-title">Registered Apartments ({apartments.length})</h2>
          {loading ? (
            <p>Loading apartments...</p>
          ) : apartments.length === 0 ? (
            <p style={{ color: 'var(--text-secondary)' }}>No apartments registered in PostgreSQL database yet. Use the form to add one.</p>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Location</th>
                    <th>Units</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {apartments.map((ap) => (
                    <tr key={ap.id}>
                      <td>#{ap.id}</td>
                      <td><strong>{ap.name}</strong></td>
                      <td>{ap.city}, {ap.state}</td>
                      <td>{ap.totalUnits}</td>
                      <td>
                        <button onClick={() => handleDelete(ap.id, ap.name)} className="btn btn-danger" style={{ padding: '0.25rem 0.5rem', fontSize: '0.8rem' }}>
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

export default ApartmentManagement;
