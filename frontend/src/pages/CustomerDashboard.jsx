import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../services/api';

export const CustomerDashboard = () => {
  const { user, isAuthenticated, isLoading, openLoginModal, logout } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [activeTab, setActiveTab] = useState('tasks');
  const [selectedTaskForBids, setSelectedTaskForBids] = useState(null);

  // Settings form states
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [addressLine, setAddressLine] = useState('');
  const [state, setState] = useState('');
  const [postalCode, setPostalCode] = useState('');
  
  const [settingsSuccess, setSettingsSuccess] = useState('');
  const [settingsError, setSettingsError] = useState('');
  const [isUpdatingSettings, setIsUpdatingSettings] = useState(false);

  // Populate settings form when user details load
  useEffect(() => {
    if (user) {
      setFirstName(user.firstName || '');
      setLastName(user.lastName || '');
      setPhoneNumber(user.phoneNumber || '');
      if (user.address) {
        setAddressLine(user.address.addressLine || '');
        setState(user.address.state || '');
        setPostalCode(user.address.postalCode || '');
      }
    }
  }, [user]);

  // Auth Guard
  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      navigate('/');
      openLoginModal();
    }
  }, [isLoading, isAuthenticated, navigate, openLoginModal]);

  // Query: Fetch customer's own tasks
  const { data: myTasks, isLoading: isTasksLoading, isError: isTasksError } = useQuery({
    queryKey: ['my-tasks'],
    queryFn: async () => {
      const response = await api.get('/task/my-tasks');
      return response.data;
    },
    enabled: isAuthenticated,
  });

  // Mutation: Deactivate/Delete task
  const deleteMutation = useMutation({
    mutationFn: async (taskId) => {
      const response = await api.post(`/task/delete-task/${taskId}`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-tasks'] });
      queryClient.invalidateQueries({ queryKey: ['active-tasks'] });
      alert('Task cancelled and deactivated successfully.');
    },
    onError: (err) => {
      alert(`Deactivation failed: ${err.response?.data?.message || err.message}`);
    }
  });

  // Handler: Update profile settings
  const handleUpdateSettings = async (e) => {
    e.preventDefault();
    setSettingsSuccess('');
    setSettingsError('');
    setIsUpdatingSettings(true);

    try {
      const payload = {
        firstName,
        lastName,
        phoneNumber,
        addressRequest: {
          addressLine,
          state,
          postalCode,
          country: 'Australia'
        }
      };

      await api.post('/customer/update-info', payload);
      setSettingsSuccess('Profile details updated successfully!');
      // Trigger token refresh or manual context update if needed
    } catch (err) {
      setSettingsError(err.response?.data?.message || 'Failed to update details. Check format.');
    } finally {
      setIsUpdatingSettings(false);
    }
  };

  const handleDeactivate = (taskId) => {
    if (window.confirm('Are you sure you want to cancel and delete this task?')) {
      deleteMutation.mutate(taskId);
    }
  };

  if (isLoading || !user) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <p>Verifying authentication session...</p>
      </div>
    );
  }

  // Pre-seed mock bids on any task for user-friendly flow
  const generateMockBids = (task) => {
    if (!task) return [];
    // Consistent bids based on task ID
    const seed = task.id || 1;
    return [
      {
        id: 101 * seed,
        providerName: 'Alex Carpenter',
        providerRating: 4.9,
        providerJobs: 42,
        avatar: '👨‍🔧',
        bidAmount: Math.round(task.customerBudget * 0.95),
        message: `Hi ${user.firstName}, I can complete this for you tomorrow morning. I have my own commercial grade tools and over 5 years of local experience.`,
        duration: '3 hours'
      },
      {
        id: 102 * seed,
        providerName: 'Jessica Clean',
        providerRating: 4.8,
        providerJobs: 118,
        avatar: '👩‍🎨',
        bidAmount: Math.round(task.customerBudget * 1.05),
        message: 'Hello! I specialize in this type of work and have high quality references. Let me know if you would like me to help out.',
        duration: '4 hours'
      }
    ];
  };

  return (
    <div className="container" style={{ padding: '2rem 1.5rem', position: 'relative' }}>
      
      {/* Dashboard Header */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '2rem',
        paddingBottom: '1rem',
        borderBottom: '1px solid var(--border)'
      }}>
        <div>
          <h2 style={{ fontSize: '1.75rem', fontWeight: '800' }}>My Dashboard</h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
            Welcome back, <strong>{user.firstName} {user.lastName}</strong> ({user.emailAddress})
          </p>
        </div>
        <span style={{
          fontSize: '0.75rem',
          fontWeight: '700',
          backgroundColor: 'var(--accent-bg)',
          color: 'var(--accent)',
          padding: '0.3rem 0.75rem',
          borderRadius: '12px',
          textTransform: 'uppercase'
        }}>
          Customer Profile
        </span>
      </div>

      {/* Tabs Header */}
      <div style={{
        display: 'flex',
        gap: '0.5rem',
        borderBottom: '2px solid var(--border)',
        marginBottom: '2rem'
      }}>
        <button
          onClick={() => { setActiveTab('tasks'); setSelectedTaskForBids(null); }}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'tasks' ? '3px solid var(--accent)' : '3px solid transparent',
            color: activeTab === 'tasks' ? 'var(--text-primary)' : 'var(--text-secondary)',
            fontWeight: '700',
            cursor: 'pointer',
            fontSize: '0.95rem',
            transition: 'all 0.15s ease'
          }}
        >
          📋 My Posted Tasks
        </button>
        <button
          onClick={() => { setActiveTab('bookings'); setSelectedTaskForBids(null); }}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'bookings' ? '3px solid var(--accent)' : '3px solid transparent',
            color: activeTab === 'bookings' ? 'var(--text-primary)' : 'var(--text-secondary)',
            fontWeight: '700',
            cursor: 'pointer',
            fontSize: '0.95rem',
            transition: 'all 0.15s ease'
          }}
        >
          🤝 Bookings & Payments
        </button>
        <button
          onClick={() => { setActiveTab('settings'); setSelectedTaskForBids(null); }}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'settings' ? '3px solid var(--accent)' : '3px solid transparent',
            color: activeTab === 'settings' ? 'var(--text-primary)' : 'var(--text-secondary)',
            fontWeight: '700',
            cursor: 'pointer',
            fontSize: '0.95rem',
            transition: 'all 0.15s ease'
          }}
        >
          ⚙️ Account Settings
        </button>
      </div>

      {/* Tab Content */}
      <div style={{ minHeight: '400px' }}>
        
        {/* TAB 1: POSTED TASKS */}
        {activeTab === 'tasks' && (
          <div>
            {isTasksLoading ? (
              <p>Loading your posted tasks...</p>
            ) : isTasksError ? (
              <p style={{ color: 'var(--error)' }}>Failed to load your tasks.</p>
            ) : (!myTasks || myTasks.length === 0) ? (
              <div style={{
                textAlign: 'center',
                padding: '4rem 2rem',
                border: '2px dashed var(--border)',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--bg-secondary)',
                color: 'var(--text-secondary)'
              }}>
                <span style={{ fontSize: '2.5rem', display: 'block', marginBottom: '0.5rem' }}>📋</span>
                <strong>You haven't posted any custom tasks yet</strong>
                <p style={{ fontSize: '0.85rem', marginTop: '0.25rem', marginBottom: '1.5rem' }}>
                  Need a customized service? Post a job and local providers will send offers.
                </p>
                <button
                  className="btn-primary"
                  onClick={() => navigate('/')}
                  style={{ padding: '0.6rem 1.5rem' }}
                >
                  Go Post a Task
                </button>
              </div>
            ) : (
              <div style={{ display: 'grid', gap: '1.25rem' }}>
                {myTasks.map((task) => (
                  <div
                    key={task.id}
                    style={{
                      backgroundColor: 'var(--bg-secondary)',
                      border: '1px solid var(--border)',
                      borderRadius: 'var(--radius-md)',
                      padding: '1.25rem 1.5rem',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '1rem',
                      opacity: task.active ? 1 : 0.6
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div>
                        <h4 style={{ fontSize: '1.1rem', fontWeight: '700', margin: '0 0 0.25rem 0' }}>
                          {task.taskServiceName}
                        </h4>
                        <div style={{ display: 'flex', gap: '1rem', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                          <span>📍 {task.userAddress || 'My Home'}</span>
                          <span>💰 Budget: <strong>${task.customerBudget}</strong></span>
                        </div>
                      </div>
                      <span style={{
                        fontSize: '0.65rem',
                        fontWeight: '700',
                        backgroundColor: task.active ? 'var(--success-bg)' : 'var(--border)',
                        color: task.active ? 'var(--success)' : 'var(--text-secondary)',
                        padding: '0.25rem 0.5rem',
                        borderRadius: '4px',
                        textTransform: 'uppercase'
                      }}>
                        {task.active ? 'Active' : 'Cancelled'}
                      </span>
                    </div>

                    <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: 0 }}>
                      {task.taskDescription}
                    </p>

                    {task.active && (
                      <div style={{ display: 'flex', gap: '0.75rem', marginTop: '0.5rem' }}>
                        <button
                          onClick={() => setSelectedTaskForBids(task)}
                          className="btn-primary"
                          style={{
                            padding: '0.5rem 1.25rem',
                            fontSize: '0.8rem',
                            backgroundColor: 'var(--accent)'
                          }}
                        >
                          💬 View Bids ({generateMockBids(task).length})
                        </button>
                        <button
                          onClick={() => handleDeactivate(task.id)}
                          style={{
                            padding: '0.5rem 1.25rem',
                            fontSize: '0.8rem',
                            backgroundColor: 'transparent',
                            color: 'var(--error)',
                            border: '1px solid var(--error)',
                            borderRadius: 'var(--radius-sm)',
                            cursor: 'pointer'
                          }}
                          onMouseOver={(e) => e.currentTarget.style.backgroundColor = 'var(--error-bg)'}
                          onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                        >
                          Cancel Task
                        </button>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* TAB 2: BOOKINGS & PAYMENTS */}
        {activeTab === 'bookings' && (
          <div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '1rem' }}>Active Bookings & Checkout</h3>
            
            {/* Seeded Mock Booking ready for Checkout */}
            <div style={{
              backgroundColor: 'var(--bg-secondary)',
              border: '1px solid var(--border)',
              borderRadius: 'var(--radius-md)',
              padding: '1.5rem',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              boxShadow: 'var(--shadow-sm)'
            }}>
              <div>
                <span style={{
                  fontSize: '0.65rem',
                  fontWeight: '700',
                  backgroundColor: '#ff9500',
                  color: '#ffffff',
                  padding: '0.2rem 0.5rem',
                  borderRadius: '4px',
                  textTransform: 'uppercase',
                  display: 'inline-block',
                  marginBottom: '0.5rem'
                }}>
                  Awaiting Payment
                </span>
                <h4 style={{ fontSize: '1.05rem', fontWeight: '700', margin: '0 0 0.25rem 0' }}>
                  Professional Lawn Mowing & Cleanup
                </h4>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '0 0 0.5rem 0' }}>
                  Provider: <strong>John D. (G-Force Gardening)</strong>
                </p>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                  📅 Scheduled: <strong>Tomorrow, 10:00 AM</strong>
                </div>
              </div>

              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: '1.4rem', fontWeight: '800', color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
                  $80.00
                </div>
                <button
                  className="btn-primary"
                  onClick={() => alert('Inline Stripe & PayPal checkout widgets will be mounted here in Step 4!')}
                  style={{
                    padding: '0.5rem 1.25rem',
                    fontSize: '0.8rem',
                    backgroundColor: 'var(--success)',
                    border: 'none'
                  }}
                >
                  💳 Secure Checkout
                </button>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: ACCOUNT SETTINGS */}
        {activeTab === 'settings' && (
          <div style={{ maxWidth: '600px', backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '2rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '1.25rem' }}>Update Personal Details</h3>

            {settingsSuccess && (
              <div style={{ backgroundColor: 'var(--success-bg)', color: 'var(--success)', padding: '0.75rem 1rem', borderRadius: 'var(--radius-sm)', fontSize: '0.85rem', marginBottom: '1.25rem' }}>
                {settingsSuccess}
              </div>
            )}

            {settingsError && (
              <div style={{ backgroundColor: 'var(--error-bg)', color: 'var(--error)', padding: '0.75rem 1rem', borderRadius: 'var(--radius-sm)', fontSize: '0.85rem', marginBottom: '1.25rem' }}>
                {settingsError}
              </div>
            )}

            <form onSubmit={handleUpdateSettings}>
              <div style={{ display: 'flex', gap: '1rem' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label" htmlFor="first-name">First Name</label>
                  <input
                    id="first-name"
                    type="text"
                    className="form-control"
                    required
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                  />
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label" htmlFor="last-name">Last Name</label>
                  <input
                    id="last-name"
                    type="text"
                    className="form-control"
                    required
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="phone-number">Phone Number</label>
                <input
                  id="phone-number"
                  type="tel"
                  className="form-control"
                  required
                  placeholder="e.g. +61412345678"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="address-line">Address Line</label>
                <input
                  id="address-line"
                  type="text"
                  className="form-control"
                  placeholder="Street address"
                  value={addressLine}
                  onChange={(e) => setAddressLine(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', gap: '1rem' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label" htmlFor="state">State</label>
                  <input
                    id="state"
                    type="text"
                    className="form-control"
                    placeholder="e.g. NSW"
                    value={state}
                    onChange={(e) => setState(e.target.value)}
                  />
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label" htmlFor="postal-code">Postal Code</label>
                  <input
                    id="postal-code"
                    type="text"
                    className="form-control"
                    placeholder="e.g. 2000"
                    value={postalCode}
                    onChange={(e) => setPostalCode(e.target.value)}
                  />
                </div>
              </div>

              <button
                type="submit"
                className="submit-btn"
                disabled={isUpdatingSettings}
                style={{ marginTop: '1rem' }}
              >
                {isUpdatingSettings ? 'Saving Changes...' : 'Save Profile Changes'}
              </button>
            </form>

            <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '2rem 0' }} />

            <button
              onClick={() => { logout(); navigate('/'); }}
              style={{
                width: '100%',
                padding: '0.75rem',
                backgroundColor: 'var(--error-bg)',
                color: 'var(--error)',
                border: '1px solid var(--error)',
                borderRadius: 'var(--radius-md)',
                cursor: 'pointer',
                fontWeight: '700'
              }}
              onMouseOver={(e) => e.currentTarget.style.backgroundColor = 'rgba(255, 69, 58, 0.15)'}
              onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'var(--error-bg)'}
            >
              Sign Out Session
            </button>
          </div>
        )}

      </div>

      {/* Bids Drawer (Phase 11 Overlay) */}
      {selectedTaskForBids && (
        <div style={{
          position: 'absolute',
          top: 0,
          right: 0,
          bottom: 0,
          width: '100%',
          maxWidth: '450px',
          backgroundColor: 'var(--bg-secondary)',
          borderLeft: '1px solid var(--border)',
          boxShadow: 'var(--shadow-lg)',
          padding: '2rem 1.5rem',
          zIndex: 10,
          display: 'flex',
          flexDirection: 'column',
          boxSizing: 'border-box'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <h3 style={{ fontSize: '1.2rem', fontWeight: '800', margin: 0 }}>Offers received</h3>
            <button
              onClick={() => setSelectedTaskForBids(null)}
              style={{ background: 'none', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: 'var(--text-secondary)' }}
            >
              &times;
            </button>
          </div>

          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
            Review proposals submitted for <strong>{selectedTaskForBids.taskServiceName}</strong> (Budget: ${selectedTaskForBids.customerBudget})
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', overflowY: 'auto', flex: 1 }}>
            {generateMockBids(selectedTaskForBids).map((bid) => (
              <div key={bid.id} style={{
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-md)',
                padding: '1.25rem',
                backgroundColor: 'var(--bg-primary)',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.75rem'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span style={{ fontSize: '1.5rem' }}>{bid.avatar}</span>
                    <div>
                      <strong style={{ fontSize: '0.9rem', display: 'block' }}>{bid.providerName}</strong>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                        ★ {bid.providerRating} ({bid.providerJobs} jobs)
                      </span>
                    </div>
                  </div>
                  <strong style={{ fontSize: '1.15rem', color: 'var(--success)' }}>
                    ${bid.bidAmount}
                  </strong>
                </div>

                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: 0, lineHeight: '1.4' }}>
                  {bid.message}
                </p>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.75rem', color: 'var(--text-secondary)', borderTop: '1px solid var(--border)', paddingTop: '0.5rem' }}>
                  <span>⏳ Takes {bid.duration}</span>
                  <button
                    onClick={() => {
                      alert(`Bid accepted! In Step 4, this will trigger com.api.artezans.booking.service endpoints and prompt for checkout!`);
                      setSelectedTaskForBids(null);
                      setActiveTab('bookings');
                    }}
                    style={{
                      backgroundColor: 'var(--success)',
                      color: '#ffffff',
                      border: 'none',
                      padding: '0.35rem 0.8rem',
                      borderRadius: '4px',
                      fontWeight: '700',
                      cursor: 'pointer'
                    }}
                  >
                    Accept Offer
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

    </div>
  );
};
