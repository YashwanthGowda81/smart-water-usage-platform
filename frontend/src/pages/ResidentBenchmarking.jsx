import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

const ResidentBenchmarking = () => {
  const [households, setHouseholds] = useState([]);
  const [selectedHouseholdId, setSelectedHouseholdId] = useState('');
  const [benchmark, setBenchmark] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchHouseholds();
  }, []);

  useEffect(() => {
    if (selectedHouseholdId) {
      fetchBenchmarking(selectedHouseholdId);
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

  const fetchBenchmarking = async (hhId) => {
    setLoading(true);
    try {
      const res = await api.get(`/benchmarking/household/${hhId}`);
      setBenchmark(res.data);
    } catch (err) {
      console.error('Failed to fetch benchmark:', err);
      setBenchmark(null);
    } finally {
      setLoading(false);
    }
  };

  const getRatingBadgeStyle = (rating) => {
    switch (rating) {
      case 'EXCELLENT':
        return { background: '#DCFCE7', color: '#166534' };
      case 'GOOD':
        return { background: '#E0F2FE', color: '#0369A1' };
      case 'AVERAGE':
        return { background: '#FEF3C7', color: '#92400E' };
      case 'HIGH_CONSUMPTION':
        return { background: '#FEE2E2', color: '#991B1B' };
      default:
        return { background: '#F1F5F9', color: '#475569' };
    }
  };

  return (
    <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--primary-navy)', marginBottom: '0.25rem' }}>🌱 Water Conservation & Community Benchmarking</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Compare your household water consumption against apartment community baselines and receive actionable water saving insights.</p>
        </div>
        <Link to="/resident/dashboard" className="btn btn-secondary">
          ← Back to Resident Dashboard
        </Link>
      </div>

      {/* Household Selector */}
      {households.length > 1 && (
        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <label style={{ fontWeight: 'bold', marginRight: '1rem' }}>Select Household Unit:</label>
          <select
            value={selectedHouseholdId}
            onChange={(e) => setSelectedHouseholdId(e.target.value)}
            style={{ padding: '0.5rem 1rem', borderRadius: '6px', border: '1px solid var(--border-color)' }}
          >
            {households.map(hh => (
              <option key={hh.id} value={hh.id}>Flat {hh.flatNumber} - {hh.apartmentName || 'Apartment'}</option>
            ))}
          </select>
        </div>
      )}

      {loading ? (
        <p>Calculating community water benchmarks...</p>
      ) : benchmark ? (
        <>
          {/* Main Stat Scorecards */}
          <div className="grid-cols-3" style={{ marginBottom: '2rem' }}>
            <div className="stat-card">
              <span className="stat-label">Your Household (30 Days)</span>
              <span className="stat-value" style={{ color: 'var(--primary-blue)' }}>
                {parseFloat(benchmark.householdUsage30DaysKl).toFixed(1)} kL
              </span>
            </div>

            <div className="stat-card">
              <span className="stat-label">Community Average (30 Days)</span>
              <span className="stat-value">
                {parseFloat(benchmark.communityAvg30DaysKl).toFixed(1)} kL
              </span>
            </div>

            <div className="stat-card">
              <span className="stat-label">Efficiency Rating</span>
              <span style={{
                fontSize: '1.2rem',
                fontWeight: 'bold',
                padding: '0.4rem 0.8rem',
                borderRadius: '20px',
                marginTop: '0.5rem',
                display: 'inline-block',
                textAlign: 'center',
                ...getRatingBadgeStyle(benchmark.efficiencyRating)
              }}>
                {benchmark.efficiencyRating}
              </span>
            </div>
          </div>

          {/* Benchmark Recommendation & Comparison Card */}
          <div className="card" style={{ marginBottom: '2rem', borderLeft: '5px solid var(--primary-teal)' }}>
            <h2 className="card-title">💡 Conservation Assessment</h2>
            <p style={{ fontSize: '1.1rem', lineHeight: '1.6', color: 'var(--primary-navy)' }}>
              {benchmark.recommendation}
            </p>
            <div style={{ marginTop: '1rem', background: '#F8FAFC', padding: '1rem', borderRadius: '6px', color: 'var(--text-secondary)' }}>
              <strong>Statistical Variance:</strong> Your consumption is{' '}
              <strong style={{ color: benchmark.percentageDifference <= 0 ? 'var(--success)' : '#DC2626' }}>
                {Math.abs(parseFloat(benchmark.percentageDifference)).toFixed(1)}% {benchmark.percentageDifference <= 0 ? 'LOWER' : 'HIGHER'}
              </strong>{' '}
              than peer households in your building.
            </div>
          </div>

          {/* Water Saving Tips */}
          <div className="card">
            <h2 className="card-title">🌊 Recommended Action Steps</h2>
            <div className="grid-cols-2" style={{ gap: '1rem', marginTop: '1rem' }}>
              <div style={{ background: '#F0F0FF', padding: '1rem', borderRadius: '8px' }}>
                <h4 style={{ margin: 0, color: 'var(--primary-blue)' }}>🚰 Aerator Faucet Fittings</h4>
                <p style={{ fontSize: '0.9rem', marginTop: '0.5rem', color: 'var(--text-secondary)' }}>
                  Installing low-flow aerators on kitchen and bathroom taps can reduce water usage by up to 30% without affecting pressure.
                </p>
              </div>
              <div style={{ background: '#F0F0FF', padding: '1rem', borderRadius: '8px' }}>
                <h4 style={{ margin: 0, color: 'var(--primary-blue)' }}>🚽 Dual-Flush Toilet Valves</h4>
                <p style={{ fontSize: '0.9rem', marginTop: '0.5rem', color: 'var(--text-secondary)' }}>
                  Check toilet flappers for silent leaks. Dual-flush mechanisms save up to 15,000 liters per year per household.
                </p>
              </div>
            </div>
          </div>
        </>
      ) : (
        <div className="card" style={{ textAlign: 'center', padding: '3rem 1.5rem' }}>
          <span style={{ fontSize: '3rem' }}>🌱</span>
          <h3>No Benchmarking Data Available</h3>
          <p style={{ color: 'var(--text-secondary)' }}>Log water meter readings for your household to calculate community conservation benchmarks.</p>
        </div>
      )}
    </div>
  );
};

export default ResidentBenchmarking;
