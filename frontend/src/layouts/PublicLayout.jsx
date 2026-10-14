import React from 'react';
import { Outlet } from 'react-router-dom';
import PublicNavbar from '../components/PublicNavbar';
import PublicFooter from '../components/PublicFooter';

const PublicLayout = () => {
  return (
    <div className="public-layout">
      <PublicNavbar />
      <main className="public-content">
        <Outlet />
      </main>
      <PublicFooter />
    </div>
  );
};

export default PublicLayout;
