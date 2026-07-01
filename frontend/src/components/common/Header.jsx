import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

export const Header = () => {
  const { user, isAuthenticated, isLoading, openLoginModal, openPostTaskModal, logout } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const navigate = useNavigate();

  const toggleDropdown = () => setDropdownOpen(!dropdownOpen);

  const handlePostTaskClick = (e) => {
    e.preventDefault();
    if (isAuthenticated) {
      openPostTaskModal();
    } else {
      openLoginModal();
    }
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <a href="/" className="brand">
          <span>🛠️</span> Artezans
        </a>

        <div className="nav-links">
          <a href="/listings" className="nav-link">Browse Listings</a>
          <a href="/tasks" className="nav-link">Browse Tasks</a>
          <a href="#" className="nav-link" onClick={handlePostTaskClick} style={{ color: 'var(--success)', fontWeight: 'bold' }}>
            Post a Task
          </a>
          
          {isLoading ? (
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>Loading...</span>
          ) : isAuthenticated ? (
            /* User is Authenticated */
            <div className="profile-menu">
              <button 
                className="avatar-btn" 
                onClick={toggleDropdown}
                aria-expanded={dropdownOpen}
                aria-haspopup="true"
              >
                <span style={{ fontSize: '0.9rem' }}>Hi, {user.firstName || 'User'}</span>
                <div className="avatar" style={{
                  width: '32px',
                  height: '32px',
                  borderRadius: '50%',
                  backgroundColor: 'var(--accent)',
                  color: 'white',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '0.85rem',
                  fontWeight: 'bold'
                }}>
                  {(user.firstName?.[0] || 'U').toUpperCase()}
                </div>
              </button>

              {dropdownOpen && (
                <div className="dropdown-menu">
                  <span style={{ 
                    padding: '0.5rem 1rem', 
                    fontSize: '0.75rem', 
                    color: 'var(--text-secondary)',
                    borderBottom: '1px solid var(--border)' 
                  }}>
                    Role: {user.roles?.[0] || 'CUSTOMER'}
                  </span>
                  <button 
                    className="dropdown-item" 
                    onClick={() => { 
                      const isAdmin = user.roles?.includes('ADMIN') || user.roles?.includes('ROLE_ADMIN');
                      const isProvider = user.roles?.includes('SERVICE_PROVIDER') || user.roles?.includes('ROLE_SERVICE_PROVIDER');
                      if (isAdmin) {
                        navigate('/dashboard/admin');
                      } else if (isProvider) {
                        navigate('/dashboard/provider');
                      } else {
                        navigate('/dashboard/customer');
                      }
                      setDropdownOpen(false); 
                    }}
                  >
                    My Dashboard
                  </button>
                  <button 
                    className="dropdown-item" 
                    onClick={() => { logout(); setDropdownOpen(false); }}
                  >
                    Sign Out
                  </button>
                </div>
              )}
            </div>
          ) : (
            /* User is Guest */
            <button 
              className="btn-primary" 
              onClick={openLoginModal}
            >
              Sign In
            </button>
          )}
        </div>
      </div>
    </nav>
  );
};
