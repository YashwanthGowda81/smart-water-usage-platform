import React, { useState, useEffect } from 'react';
import api from '../services/api';

const NotificationCenter = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchNotifications();
  }, []);

  const fetchNotifications = async () => {
    try {
      const res = await api.get('/notifications/my-notifications');
      setNotifications(res.data || []);
    } catch (err) {
      console.error('Failed to fetch notifications:', err);
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  };

  const markAsRead = async (id) => {
    try {
      await api.post(`/notifications/${id}/read`);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
    } catch (err) {
      console.error('Failed to mark notification read:', err);
    }
  };

  const markAllRead = async () => {
    try {
      await api.post('/notifications/read-all');
      setNotifications(prev => prev.map(n => ({ ...n, read: true })));
    } catch (err) {
      console.error('Failed to mark all read:', err);
    }
  };

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', padding: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', color: 'var(--primary-navy)', marginBottom: '0.25rem' }}>🔔 Notification & Alert Center</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Real-time notifications for leak alerts, bill statements, and community updates.</p>
        </div>
        <button className="btn btn-secondary" onClick={markAllRead} disabled={notifications.length === 0}>
          Check All as Read
        </button>
      </div>

      {loading ? (
        <p>Loading notifications...</p>
      ) : notifications.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem 1.5rem' }}>
          <span style={{ fontSize: '3rem' }}>🔔</span>
          <h3>No Notifications</h3>
          <p style={{ color: 'var(--text-secondary)' }}>You have no unread or historical alerts in PostgreSQL.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {notifications.map(n => (
            <div
              key={n.id}
              className="card"
              style={{
                borderLeft: n.read ? '4px solid #CBD5E1' : '4px solid var(--primary-blue)',
                background: n.read ? '#FFF' : '#F0F9FF'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <h3 style={{ margin: 0, fontSize: '1.1rem', color: 'var(--primary-navy)' }}>{n.title}</h3>
                  <p style={{ marginTop: '0.5rem', color: 'var(--text-main)', fontSize: '0.95rem' }}>{n.message}</p>
                  <small style={{ color: 'var(--text-secondary)' }}>Received: {new Date(n.createdAt).toLocaleString()}</small>
                </div>
                {!n.read && (
                  <button
                    className="btn btn-secondary"
                    style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem' }}
                    onClick={() => markAsRead(n.id)}
                  >
                    Mark Read
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default NotificationCenter;
