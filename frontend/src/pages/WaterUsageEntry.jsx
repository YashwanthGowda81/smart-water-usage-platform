import React, { useState, useEffect } from 'react';
import api from '../services/api';

const WaterUsageEntry = () => {
  const [meters, setMeters] = useState([]);
  const [usageLogs, setUsageLogs] = useState([]);
  const [meterNumber, setMeterNumber] = useState('');
  const [readingDate, setReadingDate] = useState(new Date().toISOString().split('T')[0]);
  const [currentReading, setCurrentReading] = useState('');
  const [csvFile, setCsvFile] = useState(null);
  const [uploadSummary, setUploadSummary] = useState(null);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);

  const loadData = async () => {
    try {
      const [mtRes, usRes] = await Promise.all([
        api.get('/meters'),
        api.get('/usage'),
      ]);
      setMeters(mtRes.data || []);
      setUsageLogs(usRes.data || []);
      if (mtRes.data && mtRes.data.length > 0 && !meterNumber) {
        setMeterNumber(mtRes.data[0].meterNumber);
      }
    } catch (err) {
      console.error('API error loading usage entry data:', err);
      setMessage({ type: 'danger', text: 'Failed to load meter reading logs from server.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleManualSubmit = async (e) => {
    e.preventDefault();
    setMessage({ type: '', text: '' });
    try {
      const res = await api.post('/usage', {
        meterNumber,
        readingDate,
        currentReading: parseFloat(currentReading),
        source: 'MANUAL',
      });
      setMessage({
        type: 'success',
        text: `Reading recorded! Consumption calculated: ${res.data.consumption} kL (Previous: ${res.data.previousReading} kL)`,
      });
      setCurrentReading('');
      loadData();
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Failed to record water usage.';
      setMessage({ type: 'danger', text: errMsg });
    }
  };

  const handleCsvUpload = async (e) => {
    e.preventDefault();
    if (!csvFile) {
      setMessage({ type: 'danger', text: 'Please select a CSV file to upload.' });
      return;
    }
    setUploading(true);
    setMessage({ type: '', text: '' });
    setUploadSummary(null);

    const formData = new FormData();
    formData.append('file', csvFile);

    try {
      const res = await api.post('/usage/upload-csv', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      setUploadSummary(res.data);
      setMessage({
        type: 'success',
        text: `CSV processing complete! ${res.data.successfulRows} of ${res.data.totalRows} rows uploaded successfully.`,
      });
      loadData();
    } catch (err) {
      const errMsg = err.response?.data?.message || 'CSV upload failed.';
      setMessage({ type: 'danger', text: errMsg });
    } finally {
      setUploading(false);
    }
  };

  const handleDownloadTemplate = async () => {
    try {
      const res = await api.get('/usage/template', { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'text/csv' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'water_meter_readings_template.csv');
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert('Failed to download CSV template.');
    }
  };

  return (
    <div>
      <h1 style={{ fontSize: '1.75rem', marginBottom: '0.5rem' }}>Water Meter Readings & CSV Upload</h1>
      <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>Record daily water usage manually or upload bulk meter CSV files.</p>

      {message.text && <div className={`alert alert-${message.type}`}>{message.text}</div>}

      <div className="grid-cols-2">
        {/* Manual Reading Entry */}
        <div className="card">
          <h2 className="card-title">📝 Manual Reading Entry</h2>
          {meters.length === 0 ? (
            <p className="alert alert-warning">Please configure at least one meter first.</p>
          ) : (
            <form onSubmit={handleManualSubmit}>
              <div className="form-group">
                <label className="form-label">Select Meter</label>
                <select className="form-control" value={meterNumber} onChange={(e) => setMeterNumber(e.target.value)} required>
                  {meters.map((mt) => (
                    <option key={mt.id} value={mt.meterNumber}>
                      {mt.meterNumber} (Flat {mt.flatNumber} - {mt.apartmentName})
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid-cols-2" style={{ gap: '0.75rem' }}>
                <div className="form-group">
                  <label className="form-label">Reading Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={readingDate}
                    onChange={(e) => setReadingDate(e.target.value)}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Current Meter Reading (kL)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    className="form-control"
                    value={currentReading}
                    onChange={(e) => setCurrentReading(e.target.value)}
                    placeholder="e.g. 1212.50"
                    required
                  />
                </div>
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '0.5rem' }}>
                Save Meter Reading
              </button>
            </form>
          )}
        </div>

        {/* CSV Bulk Upload */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <h2 className="card-title" style={{ margin: 0 }}>📂 Bulk CSV Upload</h2>
            <button type="button" onClick={handleDownloadTemplate} className="btn btn-secondary" style={{ fontSize: '0.8rem', padding: '0.3rem 0.6rem' }}>
              📥 Download CSV Template
            </button>
          </div>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
            Expected CSV Headers: <code>meterNumber,readingDate,currentReading</code>
          </p>

          <form onSubmit={handleCsvUpload}>
            <div className="form-group">
              <label className="form-label">Select CSV File</label>
              <input
                type="file"
                accept=".csv"
                className="form-control"
                onChange={(e) => setCsvFile(e.target.files[0])}
                required
              />
            </div>
            <button type="submit" className="btn btn-secondary" style={{ width: '100%' }} disabled={uploading}>
              {uploading ? 'Processing CSV...' : 'Upload & Process CSV'}
            </button>
          </form>

          <div style={{ marginTop: '1rem', background: '#F8FAFC', padding: '0.75rem', borderRadius: 'var(--radius-sm)', fontSize: '0.85rem' }}>
            <strong>Sample CSV Format:</strong>
            <pre style={{ margin: '0.35rem 0 0 0', background: '#E2E8F0', padding: '0.5rem', borderRadius: '4px' }}>
{`meterNumber,readingDate,currentReading
MTR-1001,2026-10-01,1212.50
MTR-1002,2026-10-01,1534.20`}
            </pre>
          </div>
        </div>
      </div>

      {/* CSV Summary Report if available */}
      {uploadSummary && (
        <div className="card" style={{ borderColor: 'var(--primary-blue)' }}>
          <h2 className="card-title">📊 CSV Upload Report Summary</h2>
          <div className="grid-cols-3" style={{ marginBottom: '1rem' }}>
            <div className="stat-card">
              <span className="stat-label">Total CSV Rows</span>
              <span className="stat-value">{uploadSummary.totalRows}</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Successful Rows</span>
              <span className="stat-value" style={{ color: 'var(--success)' }}>{uploadSummary.successfulRows}</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Failed / Errors</span>
              <span className="stat-value" style={{ color: 'var(--error)' }}>{uploadSummary.failedRows}</span>
            </div>
          </div>

          {uploadSummary.errors && uploadSummary.errors.length > 0 && (
            <div>
              <h3 style={{ fontSize: '1rem', color: 'var(--error)', marginBottom: '0.5rem' }}>Row Error Breakdown</h3>
              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>Row #</th>
                      <th>Meter Number</th>
                      <th>Date</th>
                      <th>Raw Reading</th>
                      <th>Error Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    {uploadSummary.errors.map((err, idx) => (
                      <tr key={idx}>
                        <td>Line {err.rowNumber}</td>
                        <td>{err.meterNumber || 'N/A'}</td>
                        <td>{err.readingDate || 'N/A'}</td>
                        <td>{err.rawCurrentReading || 'N/A'}</td>
                        <td style={{ color: 'var(--error)' }}>{err.errorMessage}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Recent Usage Logs Table */}
      <div className="card">
        <h2 className="card-title">💧 Recorded Water Usage Logs ({usageLogs.length})</h2>
        {loading ? (
          <p>Loading readings...</p>
        ) : usageLogs.length === 0 ? (
          <p style={{ color: 'var(--text-secondary)' }}>No water usage readings logged in PostgreSQL database yet.</p>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Meter #</th>
                  <th>Flat / Apartment</th>
                  <th>Reading Date</th>
                  <th>Prev Reading</th>
                  <th>Curr Reading</th>
                  <th>Consumption (kL)</th>
                  <th>Source</th>
                </tr>
              </thead>
              <tbody>
                {usageLogs.map((log) => (
                  <tr key={log.id}>
                    <td><strong>{log.meterNumber}</strong></td>
                    <td>Flat {log.flatNumber} ({log.apartmentName})</td>
                    <td>{log.readingDate}</td>
                    <td>{log.previousReading} kL</td>
                    <td>{log.currentReading} kL</td>
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
    </div>
  );
};

export default WaterUsageEntry;
