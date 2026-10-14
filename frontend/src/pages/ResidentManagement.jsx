import React, { useState, useEffect } from 'react';
import api from '../services/api';

const ResidentManagement = () => {
  const [users, setUsers] = useState([]);
  const [apartments, setApartments] = useState([]);
  const [households, setHouseholds] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [apartmentId, setApartmentId] = useState('');
  const [householdId, setHouseholdId] = useState('');
  const [status, setStatus] = useState('ACTIVE');
  const [message, setMessage] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      const [uRes, aRes, hRes] = await Promise.all([
        api.get('/users'),
        api.get('/apartments'),
        api.get('/households'),
      ]);
      setUsers(uRes.data || []);
      setApartments(aRes.data || []);
      setHouseholds(hRes.data || []);
    } catch (err) {
      console.error('API error loading users and units:', err);
      setMessage({ type: 'danger', text: 'Failed to load user management data from server.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSelectUser = (u) => {
    setSelectedUser(u);
    setApartmentId(u.apartmentId || '');
    setHouseholdId(u.householdId || '');
    setStatus(u.status || 'ACTIVE');
    setMessage({ type: '', text: '' });
  };

  const handleAssignSubmit = async (e) => {
    e.preventDefault();
    if (!selectedUser) return;

    setMessage({ type: '', text: '' });
    try {
      await api.put(`/users/${selectedUser.id}/assign`, {
        apartmentId: apartmentId ? parseInt(apartmentId) : null,
        householdId: householdId ? parseInt(householdId) : null,
        status,
      });
      setMessage({ type: 'success', text: `Resident '${selectedUser.name}' updated successfully!` });
      setSelectedUser(null);
      loadData();
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Failed to assign resident.';
      setMessage({ type: 'danger', text: errMsg });
    }
  };

  const handleDeleteUser = async (id, name) => {
    if (window.confirm(`Delete resident user '${name}'?`)) {
      try {
        await api.delete(`/users/${id}`);
        setMessage({ type: 'success', text: `User '${name}' deleted.` });
        if (selectedUser?.id === id) setSelectedUser(null);
        loadData();
      } catch (err) {
        const errMsg = err.response?.data?.message || 'Failed to delete user.';
        setMessage({ type: 'danger', text: errMsg });
      }
    }
  };

  // Filter households by selected apartmentId for assignment dropdown
  const filteredHouseholds = apartmentId
    ? households.filter((h) => h.apartmentId === parseInt(apartmentId))
    : households;

  return (
    <div>
      <h1 style={{ fontSize: '1.75rem', marginBottom: '0.5rem' }}>Resident Account Management</h1>
      <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
        Manage registered resident accounts, assign residents to flat numbers and communities.
      </p>

      {message.text && <div className={`alert alert-${message.type}`}>{message.text}</div>}

      <div className="grid-cols-2">
        {/* User Assignment Form */}
        <div className="card">
          <h2 className="card-title">
            {selectedUser ? `Assign Flat for: ${selectedUser.name}` : 'Select a Resident to Assign'}
          </h2>

          {!selectedUser ? (
            <p style={{ color: 'var(--text-secondary)' }}>
              Click "Assign Unit" on any resident account in the table to assign them to an apartment community and household flat.
            </p>
          ) : (
            <form onSubmit={handleAssignSubmit}>
              <div className="form-group">
                <label className="form-label">Resident Account</label>

                <input type="text" className="form-control" value={`${selectedUser.name} (${selectedUser.email})`} disabled />
              </div>

              <div className="form-group">
                <label className="form-label">Apartment Community</label>
                <select
                  className="form-control"
                  value={apartmentId}
                  onChange={(e) => {
                    setApartmentId(e.target.value);
                    setHouseholdId('');
                  }}
                >
                  <option value="">-- Unassigned --</option>
                  {apartments.map((ap) => (
                    <option key={ap.id} value={ap.id}>
                      {ap.name} ({ap.city})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Household Flat / Unit</label>
                <select
                  className="form-control"
                  value={householdId}
                  onChange={(e) => setHouseholdId(e.target.value)}
                >
                  <option value="">-- Unassigned --</option>
                  {filteredHouseholds.map((hh) => (
                    <option key={hh.id} value={hh.id}>
                      Flat {hh.flatNumber} ({hh.apartmentName})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Account Status</label>
                <select className="form-control" value={status} onChange={(e) => setStatus(e.target.value)}>
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="INACTIVE">INACTIVE</option>
                  <option value="SUSPENDED">SUSPENDED</option>
                </select>
              </div>

              <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1rem' }}>
                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>
                  Save Assignment
                </button>
                <button type="button" className="btn btn-secondary" onClick={() => setSelectedUser(null)}>
                  Cancel
                </button>
              </div>
            </form>
          )}
        </div>

        {/* Registered Users Table */}
        <div className="card">
          <h2 className="card-title">Registered Accounts ({users.length})</h2>
          {loading ? (
            <p>Loading user accounts...</p>
          ) : users.length === 0 ? (
            <p style={{ color: 'var(--text-secondary)' }}>No registered users found in PostgreSQL database.</p>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Name / Email</th>
                    <th>Role</th>
                    <th>Assigned Flat</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => {
                    const assignedHh = households.find((h) => h.id === u.householdId);
                    return (
                      <tr key={u.id}>
                        <td>
                          <strong>{u.name}</strong>
                          <br />
                          <small style={{ color: 'var(--text-secondary)' }}>{u.email}</small>
                        </td>
                        <td>
                          <span className={`badge ${u.role === 'ADMIN' ? 'badge-primary' : 'badge-info'}`}>
                            {u.role}
                          </span>
                        </td>
                        <td>
                          {assignedHh ? (
                            <span>Flat {assignedHh.flatNumber}</span>
                          ) : (
                            <span style={{ color: 'var(--text-secondary)', fontStyle: 'italic' }}>Unassigned</span>
                          )}
                        </td>
                        <td>
                          <span className={`badge ${u.status === 'ACTIVE' ? 'badge-success' : 'badge-warning'}`}>
                            {u.status}
                          </span>
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: '0.35rem' }}>
                            <button
                              onClick={() => handleSelectUser(u)}
                              className="btn btn-primary"
                              style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                            >
                              Assign
                            </button>
                            <button
                              onClick={() => handleDeleteUser(u.id, u.name)}
                              className="btn btn-danger"
                              style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ResidentManagement;
