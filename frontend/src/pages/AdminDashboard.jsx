import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  LineChart, Line, Legend
} from 'recharts';
import api from '../services/api';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    apartmentsCount: 0,
    householdsCount: 0,
    metersCount: 0,
    usageLogsCount: 0,
    totalVolume: 0,
  });
  const [householdsData, setHouseholdsData] = useState([]);
  const [trendData, setTrendData] = useState([]);
  const [recentLogs, setRecentLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [apRes, hhRes, mtRes, usRes] = await Promise.all([
          api.get('/apartments'),
          api.get('/households'),
          api.get('/meters'),
          api.get('/usage'),
        ]);

        const logs = usRes.data || [];
        processDashboardData(apRes.data || [], hhRes.data || [], mtRes.data || [], logs);
      } catch (err) {
        console.error('Failed to load dashboard metrics from PostgreSQL:', err);
        setError('Could not load real-time analytics from PostgreSQL database.');
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  const processDashboardData = (apartments, households, meters, logs) => {
    const totalVol = logs.reduce((acc, item) => acc + (parseFloat(item.consumption) || 0), 0);

    setStats({
      apartmentsCount: apartments.length,
      householdsCount: households.length,
      metersCount: meters.length,
      usageLogsCount: logs.length,
      totalVolume: totalVol,
    });

    setRecentLogs(logs.slice(0, 5));

    // Group consumption by Flat Number for Bar Chart
    const flatMap = {};
    logs.forEach((log) => {
      const flat = log.flatNumber || 'Unknown';
      flatMap[flat] = (flatMap[flat] || 0) + (parseFloat(log.consumption) || 0);
    });

    const chartByFlat = Object.keys(flatMap).map((flat) => ({
      flatNumber: `Flat ${flat}`,
      consumption: parseFloat(flatMap[flat].toFixed(2)),
    }));
    setHouseholdsData(chartByFlat);

    // Group consumption by Reading Date for Line Chart Trend
    const dateMap = {};
    logs.forEach((log) => {
      const date = log.readingDate;
      dateMap[date] = (dateMap[date] || 0) + (parseFloat(log.consumption) || 0);
    });

    const chartByDate = Object.keys(dateMap)
      .sort()
      .map((date) => ({
        date: date,
        consumption: parseFloat(dateMap[date].toFixed(2)),
      }));
    setTrendData(chartByDate);
  };

  return (
    <div>
      <div className="dashboard-header" style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.75rem', marginBottom: '0.25rem' }}>Administrator Console</h1>
        <p style={{ color: 'var(--text-secondary)' }}>
          Real-time metrics, PostgreSQL meter logs, and consumption analytics.
        </p>

        {error && (
          <div className="alert alert-danger" style={{ marginTop: '0.75rem' }}>
            ⚠️ {error}
          </div>
        )}
      </div>

      {loading ? (
        <p>Loading dashboard analytics from PostgreSQL...</p>
      ) : (
        <>
          {/* Stat Cards Grid */}
          <div className="grid-cols-4" style={{ marginBottom: '2rem' }}>
            <div className="stat-card">
              <span className="stat-label">Apartment Communities</span>
              <span className="stat-value">{stats.apartmentsCount}</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Registered Households</span>
              <span className="stat-value">{stats.householdsCount}</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Active Water Meters</span>
              <span className="stat-value">{stats.metersCount}</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Total Water Logged</span>
              <span className="stat-value" style={{ color: 'var(--primary-blue)' }}>
                {stats.totalVolume.toFixed(1)} kL
              </span>
            </div>
          </div>

          {/* Recharts Graphs */}
          <div className="grid-cols-2" style={{ marginBottom: '2rem' }}>
            {/* Household Usage Bar Chart */}
            <div className="card">
              <h2 className="card-title">📊 Consumption by Household (kL)</h2>
              <div style={{ width: '100%', height: 260 }}>
                {householdsData.length === 0 ? (
                  <p style={{ color: 'var(--text-secondary)', padding: '2rem 0', textAlign: 'center' }}>No household usage recorded in PostgreSQL yet.</p>
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={householdsData} margin={{ top: 10, right: 20, left: 0, bottom: 20 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} />
                      <XAxis dataKey="flatNumber" />
                      <YAxis unit=" kL" />
                      <Tooltip formatter={(value) => [`${value} kL`, 'Water Usage']} />
                      <Bar dataKey="consumption" fill="var(--primary-blue)" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </div>
            </div>

            {/* Consumption Trend Line Chart */}
            <div className="card">
              <h2 className="card-title">📈 Weekly Consumption Trend (kL)</h2>
              <div style={{ width: '100%', height: 260 }}>
                {trendData.length === 0 ? (
                  <p style={{ color: 'var(--text-secondary)', padding: '2rem 0', textAlign: 'center' }}>No historical trend readings logged yet.</p>
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={trendData} margin={{ top: 10, right: 20, left: 0, bottom: 20 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} />
                      <XAxis dataKey="date" />
                      <YAxis unit=" kL" />
                      <Tooltip formatter={(value) => [`${value} kL`, 'Total Usage']} />
                      <Legend />
                      <Line type="monotone" dataKey="consumption" stroke="var(--aqua-accent)" strokeWidth={3} dot={{ r: 5 }} />
                    </LineChart>
                  </ResponsiveContainer>
                )}
              </div>
            </div>
          </div>

          {/* Recent Meter Readings & Action Grid */}
          <div className="grid-cols-2">
            <div className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h2 className="card-title" style={{ margin: 0 }}>💧 Recent Meter Readings</h2>
                <Link to="/admin/usage" className="btn btn-secondary" style={{ fontSize: '0.8rem', padding: '0.35rem 0.75rem' }}>
                  Log Readings / CSV
                </Link>
              </div>

              {recentLogs.length === 0 ? (
                <p style={{ color: 'var(--text-secondary)' }}>No water readings logged in PostgreSQL yet.</p>
              ) : (
                <div className="table-container">
                  <table>
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Meter #</th>
                        <th>Flat</th>
                        <th>Consumption</th>
                        <th>Source</th>
                      </tr>
                    </thead>
                    <tbody>
                      {recentLogs.map((log) => (
                        <tr key={log.id}>
                          <td>{log.readingDate}</td>
                          <td><strong>{log.meterNumber}</strong></td>
                          <td>Flat {log.flatNumber}</td>
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

            <div className="card">
              <h2 className="card-title">⚡ Quick Management Actions</h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                <Link to="/admin/apartments" className="btn btn-primary" style={{ textAlign: 'left' }}>
                  🏢 Manage Apartment Communities
                </Link>
                <Link to="/admin/households" className="btn btn-primary" style={{ textAlign: 'left' }}>
                  🏠 Register Household Units & Occupants
                </Link>
                <Link to="/admin/meters" className="btn btn-primary" style={{ textAlign: 'left' }}>
                  📟 Configure Smart Water Meters
                </Link>
                <Link to="/admin/usage" className="btn btn-primary" style={{ textAlign: 'left' }}>
                  📂 Record Meter Readings & Upload CSV
                </Link>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default AdminDashboard;
