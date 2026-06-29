import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../services/api';

export const ProviderWorkspace = () => {
  const { user, isAuthenticated, isLoading, openLoginModal, logout } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [activeTab, setActiveTab] = useState('peculiar');
  const [selectedTaskForBid, setSelectedTaskForBid] = useState(null);
  
  // Bid Form state
  const [bidAmount, setBidAmount] = useState('');
  const [bidMessage, setBidMessage] = useState('');
  const [bidSuccess, setBidSuccess] = useState(false);

  // New Listing Form state
  const [isCreateListingOpen, setIsCreateListingOpen] = useState(false);
  const [businessName, setBusinessName] = useState('');
  const [serviceCategory, setServiceCategory] = useState('Cleaning');
  const [serviceName, setServiceName] = useState('');
  const [serviceDescription, setServiceDescription] = useState('');
  const [pricing, setPricing] = useState('');
  const [availableDays, setAvailableDays] = useState(['MONDAY', 'WEDNESDAY', 'FRIDAY']);
  const [availableFrom, setAvailableFrom] = useState('09:00');
  const [availableTo, setAvailableTo] = useState('17:00');
  const [listingImages, setListingImages] = useState(null);

  const [listingSuccess, setListingSuccess] = useState('');
  const [listingError, setListingError] = useState('');
  const [isSubmittingListing, setIsSubmittingListing] = useState(false);

  // Auth & Role Guard
  useEffect(() => {
    if (!isLoading) {
      if (!isAuthenticated) {
        navigate('/');
        openLoginModal();
      } else {
        // Check if user has SERVICE_PROVIDER role
        const hasProviderRole = user.roles?.includes('SERVICE_PROVIDER') || user.roles?.includes('ROLE_SERVICE_PROVIDER');
        if (!hasProviderRole) {
          alert('Access denied: You must be logged in as a Service Provider.');
          navigate('/dashboard/customer');
        }
      }
    }
  }, [isLoading, isAuthenticated, user, navigate, openLoginModal]);

  // Query: Fetch peculiar tasks (matching provider category/skills)
  const { data: peculiarTasks, isLoading: isTasksLoading } = useQuery({
    queryKey: ['peculiar-tasks'],
    queryFn: async () => {
      const response = await api.get('/service_provider/peculiar-tasks');
      return response.data;
    },
    enabled: isAuthenticated,
  });

  // Query: Fetch provider's own bookings
  const { data: providerBookings, isLoading: isBookingsLoading } = useQuery({
    queryKey: ['provider-bookings'],
    queryFn: async () => {
      const response = await api.get('/booking/provider-bookings');
      return response.data;
    },
    enabled: isAuthenticated,
  });

  // Query: Fetch provider's listings (page 1)
  const { data: listingsPage, isLoading: isListingsLoading } = useQuery({
    queryKey: ['provider-listings'],
    queryFn: async () => {
      // Endpoint is GET /api/v1/listing/listings/{pageNumber}
      const response = await api.get('/listing/listings/1');
      return response.data;
    },
    enabled: isAuthenticated,
  });

  const providerListings = listingsPage?.content || [];

  // Mutation: Accept booking proposal
  const acceptProposalMutation = useMutation({
    mutationFn: async (bookingId) => {
      const response = await api.post(`/booking/accept-proposal?bookingId=${bookingId}`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['provider-bookings'] });
      alert('Booking proposal accepted! Awaiting customer payment.');
    },
    onError: (err) => {
      alert(`Accept failed: ${err.response?.data?.message || err.message}`);
    }
  });

  // Mutation: Complete job
  const completeJobMutation = useMutation({
    mutationFn: async (bookingId) => {
      const response = await api.post(`/booking/complete-task?bookingId=${bookingId}`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['provider-bookings'] });
      alert('Job marked as completed. Awaiting customer confirmation to release funds.');
    },
    onError: (err) => {
      alert(`Complete failed: ${err.response?.data?.message || err.message}`);
    }
  });

  // Handler: Create listing
  const handleCreateListing = async (e) => {
    e.preventDefault();
    setListingSuccess('');
    setListingError('');

    if (!listingImages) {
      setListingError('Please upload at least one business picture (required).');
      return;
    }

    setIsSubmittingListing(true);

    const formData = new FormData();
    formData.append('businessName', businessName);
    formData.append('serviceCategory', serviceCategory);
    formData.append('serviceName', serviceName);
    formData.append('serviceDescription', serviceDescription);
    formData.append('pricing', pricing);
    formData.append('availableDays', availableDays.join(','));
    formData.append('availableFrom', availableFrom);
    formData.append('availableTo', availableTo);
    formData.append('available', 'true');
    formData.append('businessPicture', listingImages); // Spring binding

    try {
      await api.post('/listing/create-listing', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      setListingSuccess('Service listing created successfully!');
      queryClient.invalidateQueries({ queryKey: ['provider-listings'] });
      
      // Reset form
      setTimeout(() => {
        setIsCreateListingOpen(false);
        setBusinessName('');
        setServiceName('');
        setServiceDescription('');
        setPricing('');
        setListingImages(null);
      }, 1500);
    } catch (err) {
      setListingError(err.response?.data?.message || 'Failed to create listing. Please try again.');
    } finally {
      setIsSubmittingListing(false);
    }
  };

  const handleSendBid = (e) => {
    e.preventDefault();
    setBidSuccess(true);
    setTimeout(() => {
      setBidSuccess(false);
      setSelectedTaskForBid(null);
      setBidAmount('');
      setBidMessage('');
    }, 1500);
  };

  if (isLoading || !user) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <p>Verifying authentication session...</p>
      </div>
    );
  }

  const getStageLabel = (stage) => {
    switch (stage) {
      case 'PROPOSED': return 'Proposal Received';
      case 'ACCEPTED': return 'Awaiting Payment';
      case 'PAID': return 'Scheduled (Paid)';
      case 'COMPLETED': return 'Completed';
      default: return stage;
    }
  };

  return (
    <div className="container" style={{ padding: '2rem 1.5rem', position: 'relative' }}>
      
      {/* Workspace Header */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '2rem',
        paddingBottom: '1rem',
        borderBottom: '1px solid var(--border)'
      }}>
        <div>
          <h2 style={{ fontSize: '1.75rem', fontWeight: '800' }}>Provider Workspace</h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
            Welcome back, <strong>{user.firstName}</strong> | Contractor Profile ({user.emailAddress})
          </p>
        </div>
        <span style={{
          fontSize: '0.75rem',
          fontWeight: '700',
          backgroundColor: '#30b0c7',
          color: '#ffffff',
          padding: '0.3rem 0.75rem',
          borderRadius: '12px',
          textTransform: 'uppercase'
        }}>
          Service Provider Mode
        </span>
      </div>

      {/* Tabs */}
      <div style={{
        display: 'flex',
        gap: '0.5rem',
        borderBottom: '2px solid var(--border)',
        marginBottom: '2rem'
      }}>
        <button
          onClick={() => { setActiveTab('peculiar'); setSelectedTaskForBid(null); }}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'peculiar' ? '3px solid var(--accent)' : '3px solid transparent',
            color: activeTab === 'peculiar' ? 'var(--text-primary)' : 'var(--text-secondary)',
            fontWeight: '700',
            cursor: 'pointer',
            fontSize: '0.95rem'
          }}
        >
          🔍 Matching Tasks
        </button>
        <button
          onClick={() => { setActiveTab('bookings'); setSelectedTaskForBid(null); }}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'bookings' ? '3px solid var(--accent)' : '3px solid transparent',
            color: activeTab === 'bookings' ? 'var(--text-primary)' : 'var(--text-secondary)',
            fontWeight: '700',
            cursor: 'pointer',
            fontSize: '0.95rem'
          }}
        >
          📋 Active Bookings
        </button>
        <button
          onClick={() => { setActiveTab('listings'); setSelectedTaskForBid(null); }}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'listings' ? '3px solid var(--accent)' : '3px solid transparent',
            color: activeTab === 'listings' ? 'var(--text-primary)' : 'var(--text-secondary)',
            fontWeight: '700',
            cursor: 'pointer',
            fontSize: '0.95rem'
          }}
        >
          🛍️ My listings
        </button>
        <button
          onClick={() => { logout(); navigate('/'); }}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            color: 'var(--error)',
            fontWeight: '700',
            cursor: 'pointer',
            fontSize: '0.95rem',
            marginLeft: 'auto'
          }}
        >
          Sign Out
        </button>
      </div>

      {/* Tab Panel */}
      <div style={{ minHeight: '400px' }}>
        
        {/* TAB 1: PECULIAR TASKS */}
        {activeTab === 'peculiar' && (
          <div>
            <div style={{ marginBottom: '1.5rem' }}>
              <h3 style={{ fontSize: '1.15rem', fontWeight: '700', margin: '0 0 0.25rem 0' }}>Job Requests Matching Your Category</h3>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Based on your services, here are custom tasks posted by local customers.</p>
            </div>

            {isTasksLoading ? (
              <p>Loading matching tasks...</p>
            ) : (!peculiarTasks || peculiarTasks.length === 0) ? (
              <div style={{
                textAlign: 'center',
                padding: '4rem 2rem',
                border: '2px dashed var(--border)',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--bg-secondary)',
                color: 'var(--text-secondary)'
              }}>
                <span style={{ fontSize: '2.5rem', display: 'block', marginBottom: '0.5rem' }}>📋</span>
                <strong>No matching custom tasks found</strong>
                <p style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}>
                  Create more service listings under different categories to see matching task board requests!
                </p>
              </div>
            ) : (
              <div style={{ display: 'grid', gap: '1.25rem' }}>
                {peculiarTasks.map((task) => (
                  <div
                    key={task.id}
                    style={{
                      backgroundColor: 'var(--bg-secondary)',
                      border: '1px solid var(--border)',
                      borderRadius: 'var(--radius-md)',
                      padding: '1.25rem 1.5rem',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}
                  >
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <h4 style={{ fontSize: '1.05rem', fontWeight: '700', margin: '0 0 0.25rem 0' }}>
                        {task.taskServiceName}
                      </h4>
                      <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>
                        {task.taskDescription}
                      </p>
                      <div style={{ display: 'flex', gap: '1rem', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                        <span>📍 Location: {task.userAddress || 'Australia'}</span>
                        <span>📅 Preferred Date: {task.taskDates?.join(', ') || 'Flexible'}</span>
                      </div>
                    </div>

                    <div style={{ textAlign: 'right', marginLeft: '1.5rem' }}>
                      <div style={{ fontSize: '1.3rem', fontWeight: '800', color: 'var(--accent)', marginBottom: '0.5rem' }}>
                        ${task.customerBudget}
                      </div>
                      <button
                        className="btn-primary"
                        onClick={() => setSelectedTaskForBid(task)}
                        style={{ padding: '0.45rem 1.25rem', fontSize: '0.8rem', backgroundColor: '#34c759', border: 'none' }}
                      >
                        ⚡ Submit Bid
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* TAB 2: ACTIVE BOOKINGS */}
        {activeTab === 'bookings' && (
          <div>
            <div style={{ marginBottom: '1.5rem' }}>
              <h3 style={{ fontSize: '1.15rem', fontWeight: '700', margin: '0 0 0.25rem 0' }}>Your Booking Catalog</h3>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Jobs proposed by customers or confirmed through accepted custom bids.</p>
            </div>

            {isBookingsLoading ? (
              <p>Loading bookings...</p>
            ) : (!providerBookings || providerBookings.length === 0) ? (
              <div style={{
                textAlign: 'center',
                padding: '4rem 2rem',
                border: '2px dashed var(--border)',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--bg-secondary)',
                color: 'var(--text-secondary)'
              }}>
                <span style={{ fontSize: '2.5rem', display: 'block', marginBottom: '0.5rem' }}>🤝</span>
                <strong>No active client bookings found</strong>
                <p style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}>
                  Your bookings will appear here once clients purchase your listings or accept your bids.
                </p>
              </div>
            ) : (
              <div style={{ display: 'grid', gap: '1.25rem' }}>
                {providerBookings.map((booking) => (
                  <div
                    key={booking.id}
                    style={{
                      backgroundColor: 'var(--bg-secondary)',
                      border: '1px solid var(--border)',
                      borderRadius: 'var(--radius-md)',
                      padding: '1.25rem 1.5rem',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}
                  >
                    <div>
                      <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.5rem' }}>
                        <span style={{
                          fontSize: '0.65rem',
                          fontWeight: '700',
                          padding: '0.15rem 0.4rem',
                          borderRadius: '4px',
                          color: '#ffffff',
                          backgroundColor: booking.bookingStage === 'PROPOSED' ? '#ff9500' :
                                           booking.bookingStage === 'ACCEPTED' ? '#0071e3' :
                                           booking.bookingStage === 'PAID' ? '#34c759' : '#5856d6'
                        }}>
                          {getStageLabel(booking.bookingStage)}
                        </span>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                          Booking #{booking.id}
                        </span>
                      </div>

                      <h4 style={{ fontSize: '1.05rem', fontWeight: '700', margin: '0 0 0.25rem 0' }}>
                        {booking.listing?.serviceName || 'Service Request'}
                      </h4>
                      <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '0 0 0.4rem 0' }}>
                        Scheduled: <strong>{booking.bookDates?.join(', ') || 'Flexible'}</strong> | Time: {booking.bookFrom} - {booking.bookTo}
                      </p>
                    </div>

                    <div style={{ textAlign: 'right', marginLeft: '1.5rem' }}>
                      <div style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
                        ${booking.totalCost?.toFixed(2)}
                      </div>

                      {booking.bookingStage === 'PROPOSED' && (
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button
                            onClick={() => acceptProposalMutation.mutate(booking.id)}
                            style={{
                              backgroundColor: 'var(--success)',
                              color: 'white',
                              border: 'none',
                              padding: '0.45rem 1rem',
                              borderRadius: 'var(--radius-sm)',
                              fontWeight: '700',
                              fontSize: '0.75rem',
                              cursor: 'pointer'
                            }}
                          >
                            Accept
                          </button>
                        </div>
                      )}

                      {booking.bookingStage === 'ACCEPTED' && (
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                          Awaiting customer payment
                        </span>
                      )}

                      {booking.bookingStage === 'PAID' && (
                        <button
                          onClick={() => completeJobMutation.mutate(booking.id)}
                          style={{
                            backgroundColor: 'var(--accent)',
                            color: 'white',
                            border: 'none',
                            padding: '0.45rem 1rem',
                            borderRadius: 'var(--radius-sm)',
                            fontWeight: '700',
                            fontSize: '0.75rem',
                            cursor: 'pointer'
                          }}
                        >
                          Mark Job Done
                        </button>
                      )}

                      {booking.bookingStage === 'COMPLETED' && (
                        <span style={{ fontSize: '0.75rem', color: 'var(--success)', fontWeight: 'bold' }}>
                          ✓ Job Completed
                        </span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* TAB 3: MY LISTINGS */}
        {activeTab === 'listings' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <div>
                <h3 style={{ fontSize: '1.15rem', fontWeight: '700', margin: '0 0 0.25rem 0' }}>Your Service Packages</h3>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Predefined services that customers can book directly.</p>
              </div>
              <button
                className="btn-primary"
                onClick={() => setIsCreateListingOpen(true)}
                style={{ padding: '0.5rem 1.25rem', fontSize: '0.8rem' }}
              >
                + Create New Package
              </button>
            </div>

            {isListingsLoading ? (
              <p>Loading service packages...</p>
            ) : (providerListings.length === 0) ? (
              <div style={{
                textAlign: 'center',
                padding: '4rem 2rem',
                border: '2px dashed var(--border)',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--bg-secondary)',
                color: 'var(--text-secondary)'
              }}>
                <span style={{ fontSize: '2.5rem', display: 'block', marginBottom: '0.5rem' }}>🛍️</span>
                <strong>You haven't listed any services yet</strong>
                <p style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}>
                  Create standard packages so customers can book you directly from the homepage!
                </p>
              </div>
            ) : (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.25rem' }}>
                {providerListings.map((listing) => (
                  <div
                    key={listing.id}
                    style={{
                      backgroundColor: 'var(--bg-secondary)',
                      border: '1px solid var(--border)',
                      borderRadius: 'var(--radius-md)',
                      padding: '1.5rem',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '0.75rem',
                      position: 'relative'
                    }}
                  >
                    <span style={{
                      position: 'absolute',
                      top: '1rem',
                      right: '1rem',
                      fontSize: '0.65rem',
                      fontWeight: '700',
                      backgroundColor: 'var(--accent-bg)',
                      color: 'var(--accent)',
                      padding: '0.25rem 0.5rem',
                      borderRadius: '4px'
                    }}>
                      {listing.serviceCategory}
                    </span>

                    <h4 style={{ fontSize: '1.05rem', fontWeight: '800', margin: 0, paddingRight: '4.5rem' }}>
                      {listing.serviceName}
                    </h4>
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', margin: 0 }}>
                      Business: <strong>{listing.businessName}</strong>
                    </p>
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: 0, flexGrow: 1 }}>
                      {listing.serviceDescription || 'No description provided.'}
                    </p>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid var(--border)', paddingTop: '0.75rem', marginTop: '0.25rem' }}>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                        🕒 {listing.availableFrom} - {listing.availableTo}
                      </span>
                      <strong style={{ fontSize: '1.2rem', color: 'var(--accent)' }}>
                        ${listing.pricing?.toFixed(2)}/hr
                      </strong>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

      </div>

      {/* Slide Drawer: Submit Bid Form */}
      {selectedTaskForBid && (
        <div style={{
          position: 'absolute',
          top: 0,
          right: 0,
          bottom: 0,
          width: '100%',
          maxWidth: '420px',
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
            <h3 style={{ fontSize: '1.2rem', fontWeight: '800', margin: 0 }}>Send Offer Proposal</h3>
            <button
              onClick={() => setSelectedTaskForBid(null)}
              style={{ background: 'none', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: 'var(--text-secondary)' }}
            >
              &times;
            </button>
          </div>

          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
            Submit your price and cover letter to bid on <strong>{selectedTaskForBid.taskServiceName}</strong> (Client Budget: ${selectedTaskForBid.customerBudget})
          </p>

          {bidSuccess ? (
            <div style={{ backgroundColor: 'var(--success-bg)', color: 'var(--success)', padding: '1rem', borderRadius: 'var(--radius-md)', fontSize: '0.85rem', textAlign: 'center' }}>
              ✓ Offer proposal sent successfully!
            </div>
          ) : (
            <form onSubmit={handleSendBid} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div className="form-group">
                <label className="form-label" htmlFor="bid-amount">Your Bid Amount (AUD)</label>
                <input
                  id="bid-amount"
                  type="number"
                  className="form-control"
                  required
                  min={10}
                  placeholder="e.g. 140"
                  value={bidAmount}
                  onChange={(e) => setBidAmount(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="bid-msg">Cover Letter / Message</label>
                <textarea
                  id="bid-msg"
                  className="form-control"
                  required
                  rows={4}
                  placeholder="Explain why you are the best fit for this job..."
                  value={bidMessage}
                  onChange={(e) => setBidMessage(e.target.value)}
                />
              </div>

              <button type="submit" className="submit-btn">
                Submit Bid Offer
              </button>
            </form>
          )}
        </div>
      )}

      {/* Modal Overlay: Create New Service Package */}
      {isCreateListingOpen && (
        <div style={{
          position: 'fixed',
          top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 100
        }}>
          <div style={{
            backgroundColor: 'var(--bg-secondary)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius-lg)',
            padding: '2rem',
            width: '100%',
            maxWidth: '550px',
            maxHeight: '90vh',
            overflowY: 'auto'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h3 style={{ fontSize: '1.2rem', fontWeight: '800', margin: 0 }}>Create Service Listing</h3>
              <button
                onClick={() => setIsCreateListingOpen(false)}
                style={{ background: 'none', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: 'var(--text-secondary)' }}
              >
                &times;
              </button>
            </div>

            {listingSuccess && (
              <div style={{ backgroundColor: 'var(--success-bg)', color: 'var(--success)', padding: '0.75rem 1rem', borderRadius: 'var(--radius-md)', fontSize: '0.85rem', marginBottom: '1.25rem' }}>
                {listingSuccess}
              </div>
            )}

            {listingError && (
              <div style={{ backgroundColor: 'var(--error-bg)', color: 'var(--error)', padding: '0.75rem 1rem', borderRadius: 'var(--radius-md)', fontSize: '0.85rem', marginBottom: '1.25rem' }}>
                {listingError}
              </div>
            )}

            <form onSubmit={handleCreateListing}>
              <div className="form-group">
                <label className="form-label" htmlFor="listing-business">Business Name</label>
                <input
                  id="listing-business"
                  type="text"
                  className="form-control"
                  required
                  placeholder="e.g. G-Force Gardening Solutions"
                  value={businessName}
                  onChange={(e) => setBusinessName(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', gap: '1rem' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label" htmlFor="listing-category">Category</label>
                  <select
                    id="listing-category"
                    className="form-control"
                    value={serviceCategory}
                    onChange={(e) => setServiceCategory(e.target.value)}
                  >
                    <option value="Cleaning">Cleaning 🧹</option>
                    <option value="Gardening">Lawn & Garden 🌱</option>
                    <option value="Handyman">Handyman 🔧</option>
                    <option value="Removals">Removals 📦</option>
                    <option value="Painting">House Painting 🎨</option>
                    <option value="IT">Computers & IT 💻</option>
                  </select>
                </div>

                <div className="form-group" style={{ flex: 1.2 }}>
                  <label className="form-label" htmlFor="listing-service">Service Title</label>
                  <input
                    id="listing-service"
                    type="text"
                    className="form-control"
                    required
                    placeholder="e.g. Lawn Mowing & Weeding"
                    value={serviceName}
                    onChange={(e) => setServiceName(e.target.value)}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="listing-desc">Description</label>
                <textarea
                  id="listing-desc"
                  className="form-control"
                  required
                  rows={3}
                  placeholder="Describe your service, what's included, and tools used..."
                  value={serviceDescription}
                  onChange={(e) => setServiceDescription(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', gap: '1rem' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label" htmlFor="listing-price">Hourly Rate (AUD)</label>
                  <input
                    id="listing-price"
                    type="number"
                    className="form-control"
                    required
                    min={5}
                    placeholder="80"
                    value={pricing}
                    onChange={(e) => setPricing(e.target.value)}
                  />
                </div>

                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label" htmlFor="listing-file">Image upload (Required)</label>
                  <input
                    id="listing-file"
                    type="file"
                    accept="image/*"
                    className="form-control"
                    required
                    onChange={(e) => {
                      if (e.target.files && e.target.files[0]) {
                        setListingImages(e.target.files[0]);
                      }
                    }}
                    style={{ padding: '0.5rem' }}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', gap: '1rem' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label" htmlFor="listing-from">Available From</label>
                  <input
                    id="listing-from"
                    type="time"
                    className="form-control"
                    required
                    value={availableFrom}
                    onChange={(e) => setAvailableFrom(e.target.value)}
                  />
                </div>
                
                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label" htmlFor="listing-to">Available To</label>
                  <input
                    id="listing-to"
                    type="time"
                    className="form-control"
                    required
                    value={availableTo}
                    onChange={(e) => setAvailableTo(e.target.value)}
                  />
                </div>
              </div>

              <button
                type="submit"
                className="submit-btn"
                disabled={isSubmittingListing}
                style={{ marginTop: '1rem' }}
              >
                {isSubmittingListing ? 'Creating Listing...' : 'Create Service Listing'}
              </button>
            </form>
          </div>
        </div>
      )}

    </div>
  );
};
