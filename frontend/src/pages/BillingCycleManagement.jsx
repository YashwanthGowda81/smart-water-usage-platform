import React, { useState, useEffect } from 'react';
import api from '../services/api';

const BillingCycleManagement = () => {
  const [apartments, setApartments] = useState([]);
  const [selectedApartmentId, setSelectedApartmentId] = useState('');
  const [cycles, setCycles] = useState([]);
  const [selectedCycle, setSelectedCycle] = useState(null);
  const [bills, setBills] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const [formData, setFormData] = useState({
    apartmentId: '',
    name: 'March 2026 Water Billing Cycle',
    periodStart: '2026-03-01',
    periodEnd: '2026-03-31'
  });

  useEffect(() => {
    fetchApartments();
  }, []);

  useEffect(() => {
    if (selectedApartmentId) {
      fetchCycles(selectedApartmentId);
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

  const fetchCycles = async (apId) => {
    setLoading(true);
    try {
      const res = await api.get(`/billing-cycles/apartment/${apId}`);
      setCycles(res.data || []);
      if (res.data && res.data.length > 0) {
        setSelectedCycle(res.data[0]);
        fetchBills(res.data[0].id);
      } else {
        setSelectedCycle(null);
        setBills([]);
      }
    } catch (err) {
      console.error('Failed to load billing cycles:', err);
      setCycles([]);
      setSelectedCycle(null);
      setBills([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchBills = async (cycleId) => {
    try {
      const res = await api.get(`/bills/cycle/${cycleId}`);
      setBills(res.data || []);
    } catch (err) {
      console.warn('Could not fetch bills:', err);
      setBills([]);
    }
  };

  const handleCreateCycle = async (e) => {
    e.preventDefault();
    try {
      await api.post('/billing-cycles', formData);
      setMessage({ type: 'success', text: 'Billing cycle initialized successfully in PostgreSQL!' });
      setShowModal(false);
      fetchCycles(selectedApartmentId);
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to initialize cycle.' });
    }
  };

  const handleCalculateBills = async (cycleId) => {
    try {
      setLoading(true);
      await api.post(`/billing-cycles/${cycleId}/calculate`);
      setMessage({ type: 'success', text: 'Automated billing engine executed successfully! All household bills saved to PostgreSQL.' });
      fetchCycles(selectedApartmentId);
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Error executing bill calculation engine.' });
    } finally {
      setLoading(false);
    }
  };

  const handleFinalizeCycle = async (cycleId) => {
    try {
      await api.post(`/billing-cycles/${cycleId}/finalize`);
      setMessage({ type: 'success', text: 'Billing cycle finalized & notifications sent to residents!' });
      fetchCycles(selectedApartmentId);
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to finalize billing cycle.' });
    }
  };

  const handleDownloadPdf = async (billId, flatNumber) => {
    try {
      const res = await api.get(`/bills/${billId}/pdf`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `water_bill_flat_${flatNumber}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert('Failed to download PDF invoice.');
    }
  };

  return (
    <div style={{ maxWidth: '1240px', margin: '0 auto', padding: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--primary-navy)', marginBottom: '0.25rem' }}>🧾 Automated Billing Engine & Cycles</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Initialize billing periods, run progressive rate math & common cost distribution, and generate PDF invoices.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          ➕ Start New Billing Cycle
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

      {/* Cycles & Bills View */}
      {loading ? (
        <p>Loading billing cycles & calculations...</p>
      ) : cycles.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem 1.5rem' }}>
          <span style={{ fontSize: '3rem' }}>📅</span>
          <h3>No Active Billing Cycles</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Create a billing cycle for the current month to calculate household bills automatically.</p>
          <button className="btn btn-primary" style={{ marginTop: '1rem' }} onClick={() => setShowModal(true)}>
            Start Billing Cycle
          </button>
        </div>
      ) : (
        <div className="grid-cols-3" style={{ gridTemplateColumns: '1fr 2fr', gap: '1.5rem' }}>
          {/* Cycle Selector Sidebar */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <h3 style={{ color: 'var(--primary-navy)', fontSize: '1.1rem' }}>Billing Cycles</h3>
            {cycles.map(c => (
              <div
                key={c.id}
                className="card"
                onClick={() => { setSelectedCycle(c); fetchBills(c.id); }}
                style={{
                  cursor: 'pointer',
                  borderColor: selectedCycle?.id === c.id ? 'var(--primary-blue)' : 'var(--border-color)',
                  background: selectedCycle?.id === c.id ? '#F0F9FF' : '#FFF',
                  padding: '1rem'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <strong>{c.name}</strong>
                  <span className="badge-planned" style={{
                    background: c.status === 'FINALIZED' ? '#DCFCE7' : c.status === 'CALCULATING' ? '#FEF3C7' : '#E0F2FE',
                    color: c.status === 'FINALIZED' ? '#166534' : c.status === 'CALCULATING' ? '#92400E' : '#0369A1'
                  }}>
                    {c.status}
                  </span>
                </div>
                <small style={{ color: 'var(--text-secondary)', display: 'block', marginTop: '0.5rem' }}>
                  Period: {c.periodStart} to {c.periodEnd}
                </small>
              </div>
            ))}
          </div>

          {/* Selected Cycle Details & Bills Table */}
          {selectedCycle && (
            <div className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
                <div>
                  <h2 className="card-title" style={{ margin: 0 }}>{selectedCycle.name}</h2>
                  <small style={{ color: 'var(--text-secondary)' }}>Status: {selectedCycle.status} | Period: {selectedCycle.periodStart} - {selectedCycle.periodEnd}</small>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  {selectedCycle.status === 'OPEN' && (
                    <button className="btn btn-primary" onClick={() => handleCalculateBills(selectedCycle.id)}>
                      ⚡ Run Billing Engine
                    </button>
                  )}
                  {selectedCycle.status === 'CALCULATING' && (
                    <button className="btn btn-primary" style={{ background: 'var(--success)' }} onClick={() => handleFinalizeCycle(selectedCycle.id)}>
                      ✅ Finalize & Distribute Bills
                    </button>
                  )}
                </div>
              </div>

              <h4 style={{ marginBottom: '0.75rem', color: 'var(--primary-navy)' }}>Generated Household Bills ({bills.length})</h4>

              {bills.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '2rem', background: '#F8FAFC', borderRadius: '6px' }}>
                  <p style={{ color: 'var(--text-secondary)', marginBottom: '1rem' }}>Bills have not been calculated yet for this cycle.</p>
                  <button className="btn btn-primary" onClick={() => handleCalculateBills(selectedCycle.id)}>
                    ⚡ Calculate Household Bills Now
                  </button>
                </div>
              ) : (
                <div className="table-container">
                  <table>
                    <thead>
                      <tr>
                        <th>Flat #</th>
                        <th>Consumption (kL)</th>
                        <th>Metered Tariff ($)</th>
                        <th>Shared Cost ($)</th>
                        <th>Total Bill ($)</th>
                        <th>Status</th>
                        <th>Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {bills.map(b => (
                        <tr key={b.id}>
                          <td><strong>{b.flatNumber}</strong></td>
                          <td>{b.meteredConsumptionKl} kL</td>
                          <td>${parseFloat(b.meteredTariffCharge).toFixed(2)}</td>
                          <td>${parseFloat(b.sharedCostAllocation).toFixed(2)}</td>
                          <td style={{ color: 'var(--primary-blue)', fontWeight: 'bold' }}>${parseFloat(b.totalAmount).toFixed(2)}</td>
                          <td>
                            <span className="badge-planned" style={{
                              background: b.status === 'PAID' ? '#DCFCE7' : '#FEE2E2',
                              color: b.status === 'PAID' ? '#166534' : '#991B1B'
                            }}>
                              {b.status}
                            </span>
                          </td>
                          <td>
                            <button
                              className="btn btn-secondary"
                              style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem' }}
                              onClick={() => handleDownloadPdf(b.id, b.flatNumber)}
                            >
                              📄 Download PDF
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="modal-backdrop" style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card" style={{ width: '100%', maxWidth: '500px' }}>
            <h2 className="card-title">➕ Initialize Billing Cycle</h2>
            <form onSubmit={handleCreateCycle}>
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label className="form-label">Billing Cycle Name</label>
                <input
                  type="text"
                  className="form-control"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>

              <div className="grid-cols-2" style={{ gap: '1rem', marginBottom: '1.5rem' }}>
                <div className="form-group">
                  <label className="form-label">Period Start</label>
                  <input
                    type="date"
                    className="form-control"
                    value={formData.periodStart}
                    onChange={(e) => setFormData({ ...formData, periodStart: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Period End</label>
                  <input
                    type="date"
                    className="form-control"
                    value={formData.periodEnd}
                    onChange={(e) => setFormData({ ...formData, periodEnd: e.target.value })}
                    required
                  />
                </div>
              </div>

              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Create Cycle</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default BillingCycleManagement;
