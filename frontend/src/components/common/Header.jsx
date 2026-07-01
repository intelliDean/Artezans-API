import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import api from '../../services/api';

export const Header = () => {
  const { user, isAuthenticated, isLoading, openLoginModal, openPostTaskModal, logout } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);
  const navigate = useNavigate();
  const notifRef = useRef(null);
  const profileRef = useRef(null);

  const isProvider = user?.roles?.includes('SERVICE_PROVIDER') || user?.roles?.includes('ROLE_SERVICE_PROVIDER');

  // Fetch notifications only for authenticated providers (backend emits them for provider events)
  const { data: notifications = [] } = useQuery({
    queryKey: ['notifications', user?.id],
    queryFn: async () => {
      const res = await api.get('/service-provider/notifications');
      return Array.isArray(res.data) ? res.data : [];
    },
    enabled: isAuthenticated && isProvider,
    refetchInterval: 30000, // poll every 30s
  });

  const unreadCount = notifications.length;

  // Close dropdowns when clicking outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (notifRef.current && !notifRef.current.contains(e.target)) setNotifOpen(false);
      if (profileRef.current && !profileRef.current.contains(e.target)) setDropdownOpen(false);
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const toggleDropdown = () => setDropdownOpen(!dropdownOpen);
  const toggleNotif = () => setNotifOpen(!notifOpen);

  const handlePostTaskClick = (e) => {
    e.preventDefault();
    if (isAuthenticated) {
      openPostTaskModal();
    } else {
      openLoginModal();
    }
  };

  const goToDashboard = () => {
    const isAdmin = user?.roles?.includes('ADMIN') || user?.roles?.includes('ROLE_ADMIN');
    if (isAdmin) navigate('/dashboard/admin');
    else if (isProvider) navigate('/dashboard/provider');
    else navigate('/dashboard/customer');
    setDropdownOpen(false);
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
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>

              {/* ── Notification Bell (providers only) ── */}
              {isProvider && (
                <div ref={notifRef} style={{ position: 'relative' }}>
                  <button
                    onClick={toggleNotif}
                    aria-label={`Notifications${unreadCount > 0 ? `, ${unreadCount} unread` : ''}`}
                    style={{
                      position: 'relative',
                      background: 'none',
                      border: 'none',
                      cursor: 'pointer',
                      padding: '0.35rem',
                      borderRadius: '50%',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      transition: 'background 0.2s',
                    }}
                    onMouseEnter={e => e.currentTarget.style.background = 'var(--bg-tertiary)'}
                    onMouseLeave={e => e.currentTarget.style.background = 'none'}
                  >
                    <span style={{ fontSize: '1.25rem', lineHeight: 1 }}>🔔</span>
                    {unreadCount > 0 && (
                      <span style={{
                        position: 'absolute',
                        top: '0',
                        right: '0',
                        background: 'var(--error)',
                        color: 'white',
                        fontSize: '0.6rem',
                        fontWeight: '800',
                        borderRadius: '999px',
                        minWidth: '16px',
                        height: '16px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        padding: '0 3px',
                      }}>
                        {unreadCount > 9 ? '9+' : unreadCount}
                      </span>
                    )}
                  </button>

                  {notifOpen && (
                    <div style={{
                      position: 'absolute',
                      right: 0,
                      top: 'calc(100% + 8px)',
                      width: '320px',
                      background: 'var(--bg-secondary)',
                      border: '1px solid var(--border)',
                      borderRadius: 'var(--radius-md)',
                      boxShadow: 'var(--shadow-lg)',
                      zIndex: 1000,
                      overflow: 'hidden',
                    }}>
                      <div style={{
                        padding: '0.75rem 1rem',
                        borderBottom: '1px solid var(--border)',
                        fontWeight: '700',
                        fontSize: '0.9rem',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                      }}>
                        <span>🔔 Notifications</span>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontWeight: '500' }}>
                          {unreadCount} item{unreadCount !== 1 ? 's' : ''}
                        </span>
                      </div>
                      <div style={{ maxHeight: '320px', overflowY: 'auto' }}>
                        {notifications.length === 0 ? (
                          <div style={{
                            padding: '2rem 1rem',
                            textAlign: 'center',
                            color: 'var(--text-secondary)',
                            fontSize: '0.85rem',
                          }}>
                            <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>✅</div>
                            You're all caught up!
                          </div>
                        ) : (
                          notifications.map((notif, idx) => (
                            <div key={notif.id ?? idx} style={{
                              padding: '0.75rem 1rem',
                              borderBottom: idx < notifications.length - 1 ? '1px solid var(--border)' : 'none',
                              display: 'flex',
                              gap: '0.75rem',
                              alignItems: 'flex-start',
                              cursor: 'pointer',
                              transition: 'background 0.15s',
                            }}
                            onMouseEnter={e => e.currentTarget.style.background = 'var(--bg-tertiary)'}
                            onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
                            onClick={() => { goToDashboard(); setNotifOpen(false); }}
                            >
                              <span style={{ fontSize: '1.4rem', flexShrink: 0 }}>
                                {notif.notificationType === 'BOOKING' ? '📅'
                                  : notif.notificationType === 'PAYMENT' ? '💳'
                                  : notif.notificationType === 'TASK' ? '📋'
                                  : '🔔'}
                              </span>
                              <div style={{ flex: 1, minWidth: 0 }}>
                                <div style={{ fontWeight: '600', fontSize: '0.85rem', marginBottom: '0.2rem', lineHeight: 1.3 }}>
                                  {notif.title || notif.notificationType || 'New notification'}
                                </div>
                                <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', lineHeight: 1.4, overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
                                  {notif.message || notif.body || ''}
                                </div>
                              </div>
                            </div>
                          ))
                        )}
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* ── Profile / Avatar Dropdown ── */}
              <div ref={profileRef} className="profile-menu">
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
                      {user.emailAddress || user.email || ''}
                    </span>
                    <button className="dropdown-item" onClick={goToDashboard}>
                      My Dashboard
                    </button>
                    <button
                      className="dropdown-item"
                      onClick={() => { navigate('/?changePassword=1'); setDropdownOpen(false); }}
                    >
                      Change Password
                    </button>
                    <button
                      className="dropdown-item"
                      style={{ color: 'var(--error)' }}
                      onClick={() => { logout(); setDropdownOpen(false); }}
                    >
                      Sign Out
                    </button>
                  </div>
                )}
              </div>
            </div>
          ) : (
            /* User is Guest */
            <button className="btn-primary" onClick={openLoginModal}>
              Sign In
            </button>
          )}
        </div>
      </div>
    </nav>
  );
};
