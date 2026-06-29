import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { PublicLayout } from './layouts/PublicLayout';
import { Home } from './pages/Home';
import { CustomerDashboard } from './pages/CustomerDashboard';
import './App.css';

function AppRoutes() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        {/* Public Homepage Route */}
        <Route path="/" element={<Home />} />
        
        {/* Customer Dashboard Protected Route */}
        <Route path="/dashboard/customer" element={<CustomerDashboard />} />
        
        {/* Placeholder routes for listings and tasks */}
        <Route path="/listings" element={
          <div className="container" style={{ padding: '3rem 1.5rem' }}>
            <h2>Service Listings</h2>
            <p style={{ color: 'var(--text-secondary)' }}>Standard packages directory is coming soon in Step 3!</p>
          </div>
        } />
        <Route path="/tasks" element={
          <div className="container" style={{ padding: '3rem 1.5rem' }}>
            <h2>Custom Tasks Board</h2>
            <p style={{ color: 'var(--text-secondary)' }}>Full search-enabled task listings are coming soon in Step 3!</p>
          </div>
        } />
      </Route>
      
      {/* 404 Route */}
      <Route path="*" element={
        <div style={{ padding: '5rem 1.5rem', textAlign: 'center' }}>
          <h2>Page Not Found</h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>The page you are looking for does not exist.</p>
          <a href="/" style={{ fontWeight: 'bold' }}>Back to Home</a>
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
