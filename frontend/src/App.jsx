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
