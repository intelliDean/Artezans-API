import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { PublicLayout } from './layouts/PublicLayout';
import { Home } from './pages/Home';
import { CustomerDashboard } from './pages/CustomerDashboard';
import { ProviderWorkspace } from './pages/ProviderWorkspace';
import { BrowseListings } from './pages/BrowseListings';
import { AdminDashboard } from './pages/AdminDashboard';
import { BrowseTasks } from './pages/BrowseTasks';
import './App.css';

function AppRoutes() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        {/* Public Homepage Route */}
        <Route path="/" element={<Home />} />
        
        {/* Protected Dashboard Routes */}
        <Route path="/dashboard/customer" element={<CustomerDashboard />} />
        <Route path="/dashboard/provider" element={<ProviderWorkspace />} />
        <Route path="/dashboard/admin" element={<AdminDashboard />} />
        
        {/* Listings & Tasks Routes */}
        <Route path="/listings" element={<BrowseListings />} />
        <Route path="/tasks" element={<BrowseTasks />} />
      </Route>
      
      {/* 404 Route */}
      <Route path="*" element={
        <div style={{
          minHeight: '70vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          textAlign: 'center',
          padding: '4rem 1.5rem',
          gap: '1rem'
        }}>
          <div style={{ fontSize: '5rem', lineHeight: 1 }}>🔍</div>
          <h1 style={{ fontSize: '2rem', fontWeight: '800', margin: 0 }}>Page Not Found</h1>
          <p style={{
            color: 'var(--text-secondary)',
            maxWidth: '420px',
            lineHeight: '1.6',
            margin: 0
          }}>
            Looks like this page took the day off. Let's get you back to finding great services.
          </p>
          <a
            href="/"
            style={{
              marginTop: '0.5rem',
              padding: '0.75rem 2rem',
              backgroundColor: 'var(--accent)',
              color: 'white',
              borderRadius: 'var(--radius-sm)',
              fontWeight: '700',
              textDecoration: 'none',
              fontSize: '0.95rem',
              transition: 'opacity 0.2s',
            }}
            onMouseEnter={e => e.target.style.opacity = '0.85'}
            onMouseLeave={e => e.target.style.opacity = '1'}
          >
            Back to Home
          </a>
        </div>
      } />
    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
