import React, { useState, useEffect } from 'react';
import api from '../services/api';

const TariffManagement = () => {
  const [apartments, setApartments] = useState([]);
  const [selectedApartmentId, setSelectedApartmentId] = useState('');
  const [tariffs, setTariffs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const [formData, setFormData] = useState({
    name: 'Standard Tiered Tariff',
    apartmentId: '',
    effectiveFrom: new Date().toISOString().split('T')[0],
    tiers: [
      { minVolumeKl: 0, maxVolumeKl: 10, ratePerKl: 2.50 },
      { minVolumeKl: 10, maxVolumeKl: 25, ratePerKl: 5.00 },
      { minVolumeKl: 25, maxVolumeKl: null, ratePerKl: 9.00 }
    ]
  });

  useEffect(() => {
    fetchApartments();
  }, []);

  useEffect(() => {
    if (selectedApartmentId) {
      fetchTariffs(selectedApartmentId);
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

  const fetchTariffs = async (apId) => {
    setLoading(true);
    try {
      const res = await api.get(`/tariffs/apartment/${apId}`);
      setTariffs(res.data || []);
    } catch (err) {
      console.error('Failed to load tariffs:', err);
      setTariffs([]);
    } finally {
      setLoading(false);
    }
  };

  const handleTierChange = (index, field, value) => {
    const newTiers = [...formData.tiers];
    newTiers[index][field] = value === '' ? null : parseFloat(value);
    setFormData({ ...formData, tiers: newTiers });
  };

  const addTier = () => {
    const lastTier = formData.tiers[formData.tiers.length - 1];
    const minVol = lastTier ? lastTier.maxVolumeKl || 30 : 0;
    setFormData({
      ...formData,
      tiers: [
        ...formData.tiers,
        { minVolumeKl: minVol, maxVolumeKl: minVol + 15, ratePerKl: 12.00 }
      ]
    });
  };

  const removeTier = (index) => {
    if (formData.tiers.length <= 1) return;
    const newTiers = formData.tiers.filter((_, i) => i !== index);
    setFormData({ ...formData, tiers: newTiers });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/tariffs', formData);
      setMessage({ type: 'success', text: 'Tiered tariff plan created successfully!' });
      setShowModal(false);
      fetchTariffs(selectedApartmentId);
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to create tariff plan.' });
    }
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--primary-navy)', marginBottom: '0.25rem' }}>📊 Tiered Water Tariff Management</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Configure progressive consumption rate tiers per apartment community.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          ➕ Create New Tariff Plan
        </button>
      </div>

      {message.text && (
        <div className={`alert ${message.type === 'error' ? 'alert-danger' : 'alert-success'}`} style={{ marginBottom: '1rem' }}>
          {message.text}
        </div>
      )}

      {/* Apartment Selector */}
      {apartments.length > 0 && (
        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <label style={{ fontWeight: 'bold', marginRight: '1rem' }}>Select Apartment Community:</label>
          <select
            value={selectedApartmentId}
            onChange={(e) => {
              setSelectedApartmentId(e.target.value);
              setFormData(prev => ({ ...prev, apartmentId: e.target.value }));
            }}
            style={{ padding: '0.5rem 1rem', borderRadius: '6px', border: '1px solid var(--border-color)', minWidth: '250px' }}
          >
            {apartments.map(ap => (
              <option key={ap.id} value={ap.id}>{ap.name} ({ap.city})</option>
            ))}
          </select>
        </div>
      )}

      {/* Tariff List */}
      {loading ? (
        <p>Loading tariff configurations...</p>
      ) : tariffs.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem 1.5rem' }}>
          <span style={{ fontSize: '3rem' }}>🏷️</span>
          <h3>No Tariff Plans Configured</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Set up a progressive water tariff plan to automatically charge households based on consumption tiers.</p>
          <button className="btn btn-primary" style={{ marginTop: '1rem' }} onClick={() => setShowModal(true)}>
            Create Tariff Plan
          </button>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {tariffs.map(t => (
            <div key={t.id} className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
                <div>
                  <h3 style={{ margin: 0, color: 'var(--primary-navy)' }}>{t.name}</h3>
                  <small style={{ color: 'var(--text-secondary)' }}>Effective From: {t.effectiveFrom} | Status: <strong style={{ color: t.active ? 'var(--success)' : 'var(--text-secondary)' }}>{t.active ? 'ACTIVE' : 'INACTIVE'}</strong></small>
                </div>
                <span className="badge-planned" style={{ background: '#E0F2FE', color: '#0369A1' }}>Progressive Tiers</span>
              </div>

              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>Tier #</th>
                      <th>Volume Range (kL)</th>
                      <th>Rate ($ / kL)</th>
                    </tr>
                  </thead>
                  <tbody>
                    {t.tiers?.map((tier, idx) => (
                      <tr key={idx}>
                        <td><strong>Tier {tier.tierNumber || idx + 1}</strong></td>
                        <td>{tier.minVolumeKl} kL {tier.maxVolumeKl ? `to ${tier.maxVolumeKl} kL` : '+ (Unlimited)'}</td>
                        <td style={{ color: 'var(--primary-blue)', fontWeight: 'bold' }}>${parseFloat(tier.ratePerKl).toFixed(2)} / kL</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal for creating tariff plan */}
      {showModal && (
        <div className="modal-backdrop" style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card" style={{ width: '100%', maxWidth: '650px', maxHeight: '90vh', overflowY: 'auto' }}>
            <h2 className="card-title">➕ Create Progressive Tariff Plan</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label className="form-label">Plan Name</label>
                <input
                  type="text"
                  className="form-control"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>

              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label className="form-label">Effective From Date</label>
                <input
                  type="date"
                  className="form-control"
                  value={formData.effectiveFrom}
                  onChange={(e) => setFormData({ ...formData, effectiveFrom: e.target.value })}
                  required
                />
              </div>

              <h4 style={{ marginTop: '1.5rem', marginBottom: '0.75rem', color: 'var(--primary-navy)' }}>Consumption Tiers</h4>

              {formData.tiers.map((tier, idx) => (
                <div key={idx} style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.75rem', background: '#F8FAFC', padding: '0.75rem', borderRadius: '6px' }}>
                  <div style={{ flex: 1 }}>
                    <small style={{ fontWeight: 'bold' }}>Min (kL)</small>
                    <input
                      type="number"
                      step="0.1"
                      className="form-control"
                      value={tier.minVolumeKl}
                      onChange={(e) => handleTierChange(idx, 'minVolumeKl', e.target.value)}
                      required
                    />
                  </div>
                  <div style={{ flex: 1 }}>
                    <small style={{ fontWeight: 'bold' }}>Max (kL)</small>
                    <input
                      type="number"
                      step="0.1"
                      placeholder="Infinity"
                      className="form-control"
                      value={tier.maxVolumeKl !== null ? tier.maxVolumeKl : ''}
                      onChange={(e) => handleTierChange(idx, 'maxVolumeKl', e.target.value)}
                    />
                  </div>
                  <div style={{ flex: 1 }}>
                    <small style={{ fontWeight: 'bold' }}>Rate ($/kL)</small>
                    <input
                      type="number"
                      step="0.01"
                      className="form-control"
                      value={tier.ratePerKl}
                      onChange={(e) => handleTierChange(idx, 'ratePerKl', e.target.value)}
                      required
                    />
                  </div>
                  {formData.tiers.length > 1 && (
                    <button type="button" onClick={() => removeTier(idx)} className="btn btn-secondary" style={{ marginTop: '1.25rem', padding: '0.4rem 0.6rem', background: '#FEE2E2', color: '#DC2626', border: 'none' }}>
                      ❌
                    </button>
                  )}
                </div>
              ))}

              <button type="button" onClick={addTier} className="btn btn-secondary" style={{ width: '100%', marginBottom: '1.5rem' }}>
                ➕ Add Another Rate Tier
              </button>

              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Save Tariff Plan</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default TariffManagement;
