import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

const ResidentBills = () => {
  const [bills, setBills] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchResidentBills();
  }, []);

  const fetchResidentBills = async () => {
    try {
      const res = await api.get('/bills/my-bills');
      setBills(res.data || []);
    } catch (err) {
      console.error('Failed to fetch resident bills:', err);
      setBills([]);
    } finally {
      setLoading(false);
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
    <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--primary-navy)', marginBottom: '0.25rem' }}>🧾 Household Water Bills</h1>
          <p style={{ color: 'var(--text-secondary)' }}>View itemized breakdown of metered consumption, progressive rates, common area allocation, and download official PDF statements.</p>
        </div>
        <Link to="/resident/dashboard" className="btn btn-secondary">
          ← Back to Resident Dashboard
        </Link>
      </div>

      {loading ? (
        <p>Loading household statements...</p>
      ) : bills.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem 1.5rem' }}>
          <span style={{ fontSize: '3rem' }}>📄</span>
          <h3>No Billing Statements Available</h3>
          <p style={{ color: 'var(--text-secondary)' }}>No water bills have been generated for your household in PostgreSQL yet.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {bills.map(b => (
            <div key={b.id} className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
                <div>
                  <h3 style={{ margin: 0, color: 'var(--primary-navy)' }}>{b.billingCycleName || 'Monthly Water Bill'}</h3>
                  <small style={{ color: 'var(--text-secondary)' }}>Flat {b.flatNumber} ({b.apartmentName}) | Due Date: {b.dueDate}</small>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <span className="badge-planned" style={{
                    background: b.status === 'PAID' ? '#DCFCE7' : '#FEE2E2',
                    color: b.status === 'PAID' ? '#166534' : '#991B1B',
                    fontSize: '0.9rem',
                    padding: '0.4rem 0.8rem'
                  }}>
                    {b.status}
                  </span>
                  <button className="btn btn-primary" onClick={() => handleDownloadPdf(b.id, b.flatNumber)}>
                    📄 Download PDF Invoice
                  </button>
                </div>
              </div>

              {/* Itemized Table */}
              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>Line Item Description</th>
                      <th>Quantity / Basis</th>
                      <th>Amount ($)</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td><strong>Individual Metered Water Consumption</strong></td>
                      <td>{b.meteredConsumptionKl} kL (Tiered Tariff Rate Math)</td>
                      <td>${(parseFloat(b.progressiveTariffCharge ?? b.meteredTariffCharge ?? 0) || 0).toFixed(2)}</td>
                    </tr>
                    <tr>
                      <td><strong>Shared Common Area Water Allocation</strong></td>
                      <td>Proportional / Area-based Distribution</td>
                      <td>${(parseFloat(b.sharedCostAllocation ?? 0) || 0).toFixed(2)}</td>
                    </tr>
                    {parseFloat(b.penaltyAmount || b.adjustments || 0) > 0 && (
                      <tr>
                        <td style={{ color: '#DC2626' }}><strong>Overdue Penalty Charge</strong></td>
                        <td>Late Payment Fee / Adjustment</td>
                        <td style={{ color: '#DC2626' }}>${(parseFloat(b.penaltyAmount || b.adjustments || 0) || 0).toFixed(2)}</td>
                      </tr>
                    )}
                    <tr style={{ background: '#F8FAFC', fontWeight: 'bold' }}>
                      <td colSpan="2">TOTAL MONTHLY WATER BILL AMOUNT</td>
                      <td style={{ color: 'var(--primary-blue)', fontSize: '1.1rem' }}>${(parseFloat(b.totalPayable ?? b.totalAmount ?? 0) || 0).toFixed(2)}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ResidentBills;
