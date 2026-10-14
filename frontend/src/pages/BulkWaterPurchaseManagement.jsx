import React, { useState, useEffect } from 'react';
import api from '../services/api';

const BulkWaterPurchaseManagement = () => {
  const [apartments, setApartments] = useState([]);
  const [selectedApartmentId, setSelectedApartmentId] = useState('');
  const [purchases, setPurchases] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const [formData, setFormData] = useState({
    apartmentId: '',
    supplierName: 'AquaTanker Services Pvt Ltd',
    purchaseDate: new Date().toISOString().split('T')[0],
    volumeKl: 20.0,
    totalCost: 500.00,
    purchaseType: 'TANKER',
    notes: 'Emergency water tanker delivery during main supply maintenance.'
  });

  useEffect(() => {
    fetchApartments();
  }, []);

  useEffect(() => {
    if (selectedApartmentId) {
      fetchPurchases(selectedApartmentId);
    }
  }, [selectedApartmentId]);

  const fetchApartments = async () => {
    try {
      const res = await api.get('/apartments');
      setApartments(res.data || []);
      if (res.data && res.data.length > 0) {
        setSelectedApartmentId(res.data[0].id);
        setFormData(prev => ({ ...prev, apartmentId: res.data[0].id }));
      }
    } catch (err) {
      console.error('Failed to load apartments:', err);
    }
  };

  const fetchPurchases = async (apId) => {
    setLoading(true);
    try {
      const res = await api.get(`/purchases/apartment/${apId}`);
      setPurchases(res.data || []);
    } catch (err) {
      console.error('Failed to load purchases:', err);
      setPurchases([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/purchases', formData);
      setMessage({ type: 'success', text: 'Bulk water purchase recorded successfully!' });
      setShowModal(false);
      fetchPurchases(selectedApartmentId);
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to record purchase.' });
    }
  };

  const totalVolume = purchases.reduce((acc, p) => acc + (parseFloat(p.volumeKl) || 0), 0);
  const totalCost = purchases.reduce((acc, p) => acc + (parseFloat(p.totalCost) || 0), 0);

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--primary-navy)', marginBottom: '0.25rem' }}>🚛 Bulk Water Procurement</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Log municipal water supply and private tanker purchases for common area cost allocation.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          ➕ Record Bulk Purchase
        </button>
      </div>

      {message.text && (
        <div className={`alert ${message.type === 'error' ? 'alert-danger' : 'alert-success'}`} style={{ marginBottom: '1rem' }}>
          {message.text}
        </div>
      )}

      {/* Apartment Selector & Summary */}
      <div className="grid-cols-3" style={{ marginBottom: '1.5rem' }}>
        <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <label style={{ fontWeight: 'bold', marginBottom: '0.5rem' }}>Selected Community:</label>
          <select
            value={selectedApartmentId}
            onChange={(e) => {
              setSelectedApartmentId(e.target.value);
              setFormData(prev => ({ ...prev, apartmentId: e.target.value }));
            }}
            style={{ padding: '0.5rem 1rem', borderRadius: '6px', border: '1px solid var(--border-color)', width: '100%' }}
          >
            {apartments.map(ap => (
              <option key={ap.id} value={ap.id}>{ap.name} ({ap.city})</option>
            ))}
          </select>
        </div>

        <div className="stat-card">
          <span className="stat-label">Total Procured Volume</span>
          <span className="stat-value" style={{ color: 'var(--primary-blue)' }}>{totalVolume.toFixed(1)} kL</span>
        </div>

        <div className="stat-card">
          <span className="stat-label">Total Bulk Procurement Cost</span>
          <span className="stat-value" style={{ color: 'var(--success)' }}>${totalCost.toFixed(2)}</span>
        </div>
      </div>

      {/* Purchases List */}
      {loading ? (
        <p>Loading bulk water purchases...</p>
      ) : purchases.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem 1.5rem' }}>
          <span style={{ fontSize: '3rem' }}>💧</span>
          <h3>No Bulk Water Purchases Recorded</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Log incoming water tankers or municipal bill amounts in PostgreSQL to share costs fairly.</p>
          <button className="btn btn-primary" style={{ marginTop: '1rem' }} onClick={() => setShowModal(true)}>
            Record First Purchase
          </button>
        </div>
      ) : (
        <div className="card">
          <h3 className="card-title">📋 Procurement Records</h3>
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Supplier</th>
                  <th>Type</th>
                  <th>Volume (kL)</th>
                  <th>Total Cost ($)</th>
                  <th>Notes</th>
                </tr>
              </thead>
              <tbody>
                {purchases.map(p => (
                  <tr key={p.id}>
                    <td>{p.purchaseDate}</td>
                    <td><strong>{p.supplierName}</strong></td>
                    <td>
                      <span className="badge-planned" style={{
                        background: p.purchaseType === 'TANKER' ? '#FEF3C7' : '#E0F2FE',
                        color: p.purchaseType === 'TANKER' ? '#92400E' : '#0369A1'
                      }}>
                        {p.purchaseType}
                      </span>
                    </td>
                    <td>{parseFloat(p.volumeKl).toFixed(1)} kL</td>
                    <td style={{ color: 'var(--primary-blue)', fontWeight: 'bold' }}>${parseFloat(p.totalCost).toFixed(2)}</td>
                    <td style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{p.notes || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="modal-backdrop" style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card" style={{ width: '100%', maxWidth: '550px' }}>
            <h2 className="card-title">➕ Record Bulk Water Purchase</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label className="form-label">Supplier Name</label>
                <input
                  type="text"
                  className="form-control"
                  value={formData.supplierName}
                  onChange={(e) => setFormData({ ...formData, supplierName: e.target.value })}
                  required
                />
              </div>

              <div className="grid-cols-2" style={{ gap: '1rem', marginBottom: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Purchase Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={formData.purchaseDate}
                    onChange={(e) => setFormData({ ...formData, purchaseDate: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Procurement Type</label>
                  <select
                    className="form-control"
                    value={formData.purchaseType}
                    onChange={(e) => setFormData({ ...formData, purchaseType: e.target.value })}
                  >
                    <option value="TANKER">Private Tanker Delivery</option>
                    <option value="MUNICIPAL">Municipal Bulk Pipeline</option>
                    <option value="BOREWELL_ELECTRICITY">Borewell Electricity / Maintenance</option>
                  </select>
                </div>
              </div>

              <div className="grid-cols-2" style={{ gap: '1rem', marginBottom: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Volume (kL / Kiloliters)</label>
                  <input
                    type="number"
                    step="0.1"
                    className="form-control"
                    value={formData.volumeKl}
                    onChange={(e) => setFormData({ ...formData, volumeKl: parseFloat(e.target.value) })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Total Cost ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    className="form-control"
                    value={formData.totalCost}
                    onChange={(e) => setFormData({ ...formData, totalCost: parseFloat(e.target.value) })}
                    required
                  />
                </div>
              </div>

              <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                <label className="form-label">Notes / Remarks</label>
                <input
                  type="text"
                  className="form-control"
                  value={formData.notes}
                  onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                />
              </div>

              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Save Purchase Record</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default BulkWaterPurchaseManagement;
