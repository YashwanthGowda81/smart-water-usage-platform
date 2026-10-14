import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import PreviewBanner from './components/PreviewBanner';

import PublicLayout from './layouts/PublicLayout';
import AdminLayout from './layouts/AdminLayout';

import LandingPage from './pages/LandingPage';
import AboutPage from './pages/AboutPage';
import FeaturesPage from './pages/FeaturesPage';
import HowItWorksPage from './pages/HowItWorksPage';
import WaterSavingPage from './pages/WaterSavingPage';

import Login from './pages/Login';
import Register from './pages/Register';
import ResidentLogin from './pages/ResidentLogin';
import ResidentRegister from './pages/ResidentRegister';
import AdminLogin from './pages/AdminLogin';
import AdminRegister from './pages/AdminRegister';

import AdminDashboard from './pages/AdminDashboard';
import ApartmentManagement from './pages/ApartmentManagement';
import HouseholdManagement from './pages/HouseholdManagement';
import ResidentManagement from './pages/ResidentManagement';
import MeterManagement from './pages/MeterManagement';
import WaterUsageEntry from './pages/WaterUsageEntry';
import TariffManagement from './pages/TariffManagement';
import BulkWaterPurchaseManagement from './pages/BulkWaterPurchaseManagement';
import BillingCycleManagement from './pages/BillingCycleManagement';
import AlertsManagement from './pages/AlertsManagement';
import NotificationCenter from './pages/NotificationCenter';

import ResidentDashboard from './pages/ResidentDashboard';
import ResidentUsageHistory from './pages/ResidentUsageHistory';
import ResidentBills from './pages/ResidentBills';
import ResidentBenchmarking from './pages/ResidentBenchmarking';

function App() {
  return (
    <AuthProvider>
      <Router>
        <PreviewBanner />
        <Routes>
          {/* Public Website Routes */}
          <Route element={<PublicLayout />}>
            <Route path="/" element={<LandingPage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/features" element={<FeaturesPage />} />
            <Route path="/how-it-works" element={<HowItWorksPage />} />
            <Route path="/water-saving" element={<WaterSavingPage />} />
          </Route>

          {/* Authentication Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/resident/login" element={<ResidentLogin />} />
          <Route path="/resident/register" element={<ResidentRegister />} />
          <Route path="/admin/login" element={<AdminLogin />} />
          <Route path="/admin/register" element={<AdminRegister />} />

          {/* Admin Application Routes */}
          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route element={<AdminLayout />}>
              <Route path="/admin/dashboard" element={<AdminDashboard />} />
              <Route path="/admin/apartments" element={<ApartmentManagement />} />
              <Route path="/admin/households" element={<HouseholdManagement />} />
              <Route path="/admin/residents" element={<ResidentManagement />} />
              <Route path="/admin/meters" element={<MeterManagement />} />
              <Route path="/admin/usage" element={<WaterUsageEntry />} />
              <Route path="/admin/tariffs" element={<TariffManagement />} />
              <Route path="/admin/purchases" element={<BulkWaterPurchaseManagement />} />
              <Route path="/admin/billing" element={<BillingCycleManagement />} />
              <Route path="/admin/alerts" element={<AlertsManagement />} />
              <Route path="/admin/notifications" element={<NotificationCenter />} />
            </Route>
          </Route>

          {/* Resident Application Routes */}
          <Route element={<ProtectedRoute allowedRoles={['RESIDENT', 'ADMIN']} />}>
            <Route path="/resident/dashboard" element={<ResidentDashboard />} />
            <Route path="/resident/usage" element={<ResidentUsageHistory />} />
            <Route path="/resident/bills" element={<ResidentBills />} />
            <Route path="/resident/benchmarking" element={<ResidentBenchmarking />} />
            <Route path="/resident/notifications" element={<NotificationCenter />} />
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
