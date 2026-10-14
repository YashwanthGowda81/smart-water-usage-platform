import React, { createContext, useState, useEffect } from 'react';
import api from '../services/api';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(localStorage.getItem('token') || '');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const verifySession = async () => {
      if (token) {
        try {
          const response = await api.get('/auth/me');
          setUser(response.data);
          localStorage.setItem('user', JSON.stringify(response.data));
        } catch (error) {
          console.warn('Session token invalid or expired:', error);
          logout();
        }
      } else {
        setUser(null);
      }
      setLoading(false);
    };

    verifySession();
  }, [token]);

  const login = async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    const { token: jwtToken, user: userData } = response.data;
    localStorage.setItem('token', jwtToken);
    localStorage.setItem('user', JSON.stringify(userData));
    setToken(jwtToken);
    setUser(userData);
    return userData;
  };

  const register = async (name, email, password, role, adminSecret) => {
    const response = await api.post('/auth/register', { name, email, password, role, adminSecret });
    const { token: jwtToken, user: userData } = response.data;
    localStorage.setItem('token', jwtToken);
    localStorage.setItem('user', JSON.stringify(userData));
    setToken(jwtToken);
    setUser(userData);
    return userData;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken('');
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        register,
        logout
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
