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

  const login = async (email, password, role = null) => {
    const payload = { 
      email: email ? email.trim().toLowerCase() : '', 
      password 
    };
    if (role) {
      payload.role = role;
    }
    const response = await api.post('/auth/login', payload);
    const { token: jwtToken, user: userData } = response.data;
    localStorage.setItem('token', jwtToken);
    localStorage.setItem('user', JSON.stringify(userData));
    setToken(jwtToken);
    setUser(userData);
    return userData;
  };

  const register = async (name, email, password, role = 'RESIDENT') => {
    const normalizedEmail = email ? email.trim().toLowerCase() : '';
    const payload = { name: name.trim(), email: normalizedEmail, password, role };
    const endpoint = role === 'ADMIN' ? '/auth/register/owner' : '/auth/register/resident';
    const response = await api.post(endpoint, payload);
    // Registration must not automatically log in or set tokens in session
    return response.data;
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
