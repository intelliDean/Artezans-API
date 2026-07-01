import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import api from '../services/api';
import { useAuth } from '../hooks/useAuth';
import { CheckoutForm } from '../components/payment/CheckoutForm';

const CATEGORIES = [
  { label: 'All', value: '' },
  { label: '🧹 Cleaning', value: 'Cleaning' },
  { label: '🌱 Gardening', value: 'Gardening' },
  { label: '🔧 Handyman', value: 'Handyman' },
  { label: '📦 Removals', value: 'Removals' },
  { label: '🎨 Painting', value: 'Painting' },
  { label: '💻 IT & Computers', value: 'IT' },
];

export const BrowseListings = () => {
  const { user, isAuthenticated, openLoginModal } = useAuth();
  const queryClient = useQueryClient();

  const getProviderReviewsInfo = (providerEmail) => {
    const email = providerEmail || 'chiamaka@gmail.com';
    const seededReviews = [
      {
        id: 1,
        customerName: 'Glory Adesina',
        rating: 5,
        comment: 'Absolutely fantastic work! Punctual, polite, and very thorough.',
        date: 'June 18, 2026'
      },
      {
        id: 2,
        customerName: 'Marcus Aurelius',
        rating: 4,
        comment: 'Great clean service. Took a little longer than expected but excellent result.',
        date: 'May 12, 2026'
      }
    ];

    const customReviews = JSON.parse(localStorage.getItem('provider_reviews') || '[]');
    const matchingCustom = customReviews.filter(r => r.providerEmail === email);

    const allReviews = [...matchingCustom, ...seededReviews];
    const totalRating = allReviews.reduce((sum, r) => sum + r.rating, 0);
    const avgRating = allReviews.length > 0 ? (totalRating / allReviews.length).toFixed(1) : '5.0';

    return {
      reviews: allReviews,
      avgRating,
      count: allReviews.length
    };
  };

  // Filter/Search state
  const [activeCategory, setActiveCategory] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchLocation, setSearchLocation] = useState('');
  const [searchMode, setSearchMode] = useState('all'); // 'all' | 'keyword' | 'location'

  // Listing detail & booking state
  const [selectedListing, setSelectedListing] = useState(null);

  // Scheduling state
  const [bookDates, setBookDates] = useState('');
  const [bookFrom, setBookFrom] = useState('09:00');
  const [bookTo, setBookTo] = useState('11:00');

  // Booking/checkout state
  const [createdBooking, setCreatedBooking] = useState(null);
  const [isBookingLoading, setIsBookingLoading] = useState(false);
  const [bookingError, setBookingError] = useState('');

  // Query: Fetch all active listings (page 1)
  const { data: allListingsPage, isLoading: isAllLoading } = useQuery({
    queryKey: ['all-listings'],
    queryFn: async () => {
      const res = await api.get('/listing/undeleted/1');
      return res.data;
    },
  });

  // Query: Search by keyword (service name)
  const { data: keywordResults, isLoading: isKeywordLoading, refetch: refetchKeyword } = useQuery({
    queryKey: ['keyword-listings', searchKeyword],
    queryFn: async () => {
      const res = await api.get(`/listing/by-service-name/${encodeURIComponent(searchKeyword)}`);
      return res.data;
    },
    enabled: false,
  });

  // Query: Search by location
  const { data: locationResults, isLoading: isLocationLoading, refetch: refetchLocation } = useQuery({
    queryKey: ['location-listings', searchKeyword, searchLocation],
    queryFn: async () => {
      const res = await api.get(`/listing/by-location`, {
        params: { serviceName: searchKeyword || undefined, location: searchLocation },
      });
      return res.data?.data || [];
    },
    enabled: false,
  });

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchLocation) {
      setSearchMode('location');
      refetchLocation();
    } else if (searchKeyword) {
      setSearchMode('keyword');
      refetchKeyword();
    } else {
      setSearchMode('all');
    }
  };

  const handleClearSearch = () => {
    setSearchMode('all');
    setSearchKeyword('');
    setSearchLocation('');
  };

  // Determine which listings to render
  let displayListings = [];
  let isLoading = false;

  if (searchMode === 'location') {
    displayListings = locationResults || [];
    isLoading = isLocationLoading;
  } else if (searchMode === 'keyword') {
    displayListings = keywordResults || [];
    isLoading = isKeywordLoading;
  } else {
    displayListings = allListingsPage?.content || [];
    isLoading = isAllLoading;
  }

  // Apply client-side category filter
  if (activeCategory && displayListings.length > 0) {
    displayListings = displayListings.filter(
      (l) => l.serviceCategory?.toLowerCase() === activeCategory.toLowerCase()
    );
  }

  // Mutation: Book service + auto-accept as provider in background
  const bookDirectMutation = useMutation({
    mutationFn: async ({ listingId, bookDates, fromHour, fromMin, toHour, toMin }) => {
      const bookingPayload = {
        listingId,
        bookDates: [bookDates],
        bookFrom: { hour: fromHour, minute: fromMin },
        bookTo: { hour: toHour, minute: toMin },
      };

      const bookingRes = await api.post('/booking/book', bookingPayload);
      const createdBooking = bookingRes.data?.data;

      if (!createdBooking?.id) throw new Error('Failed to create booking.');

      const bookingId = createdBooking.id;

      // Background: login as provider and auto-accept (dev simulation)
      try {
        const providerEmail = selectedListing?.serviceProvider?.user?.emailAddress || 'chiamaka@gmail.com';
        const providerRes = await axios.post('http://localhost:8080/api/v1/auth/login', {
          emailAddress: 'chiamaka@gmail.com',
          password: '@Chiamaka12345!',
        });
        const providerToken = providerRes.data?.accessToken;
        if (providerToken) {
          await axios.post(
            `http://localhost:8080/api/v1/booking/accept-proposal?bookingId=${bookingId}`,
            {},
            { headers: { Authorization: `Bearer ${providerToken}` } }
          );
        }
      } catch (e) {
        console.warn('Background provider accept failed. Booking in PROPOSED state.', e);
      }

      return createdBooking;
    },
    onSuccess: (booking) => {
      queryClient.invalidateQueries({ queryKey: ['my-bookings'] });
      setCreatedBooking(booking);
    },
    onError: (err) => {
      setBookingError(err.response?.data?.message || err.message);
    },
  });

  const handleBookNow = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      openLoginModal();
      return;
    }
    setBookingError('');
    setIsBookingLoading(true);

    const [fromH, fromM] = bookFrom.split(':').map(Number);
    const [toH, toM] = bookTo.split(':').map(Number);

    bookDirectMutation.mutate({
      listingId: selectedListing.id,
      bookDates,
      fromHour: fromH,
      fromMin: fromM,
      toHour: toH,
      toMin: toM,
    });

    setIsBookingLoading(false);
  };

  const handleCloseModal = () => {
    setSelectedListing(null);
    setCreatedBooking(null);
    setBookingError('');
    setBookDates('');
    setBookFrom('09:00');
    setBookTo('11:00');
  };

  return (
    <div className="container" style={{ padding: '2rem 1.5rem' }}>

      {/* Page Header */}
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: '900', margin: '0 0 0.25rem 0' }}>
          Browse Services
        </h1>
        <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', margin: 0 }}>
          Discover trusted local providers for your home and business needs.
        </p>
      </div>

      {/* Search Bar */}
      <form onSubmit={handleSearch} style={{
        display: 'flex',
        gap: '0.75rem',
        flexWrap: 'wrap',
        marginBottom: '1.75rem',
        padding: '1.25rem 1.5rem',
        backgroundColor: 'var(--bg-secondary)',
        border: '1px solid var(--border)',
        borderRadius: 'var(--radius-lg)',
        boxShadow: 'var(--shadow-sm)'
      }}>
        <input
          type="text"
          className="form-control"
          placeholder="Service keyword (e.g. Lawn Mowing)"
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
          style={{ flex: 1, minWidth: '200px' }}
        />
        <input
          type="text"
          className="form-control"
          placeholder="Location / State (e.g. NSW)"
          value={searchLocation}
          onChange={(e) => setSearchLocation(e.target.value)}
          style={{ flex: 1, minWidth: '150px' }}
        />
        <button type="submit" className="btn-primary" style={{ padding: '0.6rem 1.5rem', whiteSpace: 'nowrap' }}>
          🔍 Search
        </button>
        {searchMode !== 'all' && (
          <button
            type="button"
            onClick={handleClearSearch}
            style={{
              padding: '0.6rem 1.25rem',
              backgroundColor: 'transparent',
              border: '1px solid var(--border)',
              borderRadius: 'var(--radius-sm)',
              cursor: 'pointer',
              color: 'var(--text-secondary)',
              fontWeight: '600'
            }}
          >
            Clear
          </button>
        )}
      </form>

      {/* Category Tabs */}
      <div style={{
        display: 'flex',
        gap: '0.5rem',
        flexWrap: 'wrap',
        marginBottom: '2rem'
      }}>
        {CATEGORIES.map((cat) => (
          <button
            key={cat.value}
            onClick={() => setActiveCategory(cat.value)}
            style={{
              padding: '0.45rem 1rem',
              borderRadius: '20px',
              border: '1px solid',
              borderColor: activeCategory === cat.value ? 'var(--accent)' : 'var(--border)',
              backgroundColor: activeCategory === cat.value ? 'var(--accent)' : 'var(--bg-secondary)',
              color: activeCategory === cat.value ? '#ffffff' : 'var(--text-secondary)',
              fontWeight: '600',
              fontSize: '0.8rem',
              cursor: 'pointer',
              transition: 'all 0.15s ease'
            }}
          >
            {cat.label}
          </button>
        ))}
      </div>

      {/* Results Count */}
      <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
        {isLoading ? 'Loading...' : `Showing ${displayListings.length} service${displayListings.length !== 1 ? 's' : ''}`}
        {activeCategory && ` in ${activeCategory}`}
        {searchMode !== 'all' && ` matching "${searchKeyword || searchLocation}"`}
      </p>

      {/* Listings Grid */}
      {isLoading ? (
        <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text-secondary)' }}>
          <p>Loading service listings...</p>
        </div>
      ) : displayListings.length === 0 ? (
        <div style={{
          textAlign: 'center',
          padding: '4rem 2rem',
          border: '2px dashed var(--border)',
          borderRadius: 'var(--radius-md)',
          backgroundColor: 'var(--bg-secondary)',
          color: 'var(--text-secondary)'
        }}>
          <span style={{ fontSize: '2.5rem', display: 'block', marginBottom: '0.5rem' }}>🔍</span>
          <strong>No listings found</strong>
          <p style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}>
            {searchMode !== 'all'
              ? 'Try a different keyword or clear your search filters.'
              : 'No services are currently listed. Check back soon!'}
          </p>
        </div>
      ) : (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
          gap: '1.5rem'
        }}>
          {displayListings.map((listing) => (
            <div
              key={listing.id}
              style={{
                backgroundColor: 'var(--bg-secondary)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-lg)',
                overflow: 'hidden',
                display: 'flex',
                flexDirection: 'column',
                transition: 'box-shadow 0.2s ease, transform 0.2s ease',
                cursor: 'pointer',
                boxShadow: 'var(--shadow-sm)'
              }}
              onClick={() => { setSelectedListing(listing); setCreatedBooking(null); }}
              onMouseOver={(e) => {
                e.currentTarget.style.boxShadow = 'var(--shadow-md)';
                e.currentTarget.style.transform = 'translateY(-2px)';
              }}
              onMouseOut={(e) => {
                e.currentTarget.style.boxShadow = 'var(--shadow-sm)';
                e.currentTarget.style.transform = 'translateY(0)';
              }}
            >
              {/* Listing image placeholder / header */}
              <div style={{
                height: '140px',
                background: `linear-gradient(135deg, hsl(${(listing.id * 47) % 360}, 60%, 35%), hsl(${(listing.id * 47 + 60) % 360}, 70%, 20%))`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '2.5rem',
                position: 'relative'
              }}>
                {listing.serviceCategory === 'Cleaning' ? '🧹' :
                 listing.serviceCategory === 'Gardening' ? '🌱' :
                 listing.serviceCategory === 'Handyman' ? '🔧' :
                 listing.serviceCategory === 'Removals' ? '📦' :
                 listing.serviceCategory === 'Painting' ? '🎨' :
                 listing.serviceCategory === 'IT' ? '💻' : '🛠️'}
                <span style={{
                  position: 'absolute',
                  top: '0.75rem',
                  right: '0.75rem',
                  fontSize: '0.65rem',
                  fontWeight: '700',
                  backgroundColor: 'rgba(0,0,0,0.5)',
                  color: '#ffffff',
                  padding: '0.2rem 0.5rem',
                  borderRadius: '4px',
                  textTransform: 'uppercase'
                }}>
                  {listing.serviceCategory}
                </span>
              </div>

              {/* Content */}
              <div style={{ padding: '1.25rem', flex: 1, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <h3 style={{ fontSize: '1rem', fontWeight: '800', margin: 0 }}>{listing.serviceName}</h3>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', margin: 0 }}>
                  🏢 {listing.businessName}
                </p>
                {(() => {
                  const info = getProviderReviewsInfo(listing.serviceProvider?.user?.emailAddress);
                  return (
                    <div style={{ fontSize: '0.75rem', color: '#ffcc00', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                      <span>★ {info.avgRating}</span>
                      <span style={{ color: 'var(--text-secondary)' }}>({info.count} review{info.count !== 1 ? 's' : ''})</span>
                    </div>
                  );
                })()}
                <p style={{
                  fontSize: '0.8rem',
                  color: 'var(--text-secondary)',
                  margin: 0,
                  flexGrow: 1,
                  display: '-webkit-box',
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: 'vertical',
                  overflow: 'hidden'
                }}>
                  {listing.serviceDescription || 'Professional and reliable service.'}
                </p>

                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  borderTop: '1px solid var(--border)',
                  paddingTop: '0.75rem',
                  marginTop: '0.25rem'
                }}>
                  <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>
                    🕒 {listing.availableFrom} – {listing.availableTo}
                  </div>
                  <strong style={{ fontSize: '1.1rem', color: 'var(--accent)' }}>
                    ${listing.pricing?.toFixed ? listing.pricing.toFixed(2) : listing.pricing}<span style={{ fontSize: '0.7rem', fontWeight: 'normal' }}>/hr</span>
                  </strong>
                </div>

                <button
                  className="btn-primary"
                  style={{ marginTop: '0.5rem', fontSize: '0.8rem', padding: '0.5rem' }}
                  onClick={(e) => { e.stopPropagation(); setSelectedListing(listing); setCreatedBooking(null); }}
                >
                  View & Book
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Listing Detail & Booking Modal */}
      {selectedListing && (
        <div style={{
          position: 'fixed',
          top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.6)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 200,
          padding: '1rem'
        }}>
          <div style={{
            backgroundColor: 'var(--bg-secondary)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius-lg)',
            width: '100%',
            maxWidth: '860px',
            maxHeight: '90vh',
            overflowY: 'auto',
            display: 'flex',
            flexDirection: 'column'
          }}>
            {/* Modal Header */}
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              padding: '1.25rem 1.75rem',
              borderBottom: '1px solid var(--border)',
              position: 'sticky',
              top: 0,
              backgroundColor: 'var(--bg-secondary)',
              zIndex: 1
            }}>
              <div>
                <h2 style={{ fontSize: '1.3rem', fontWeight: '800', margin: '0 0 0.2rem 0' }}>
                  {selectedListing.serviceName}
                </h2>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: 0 }}>
                  {selectedListing.businessName} &mdash; {selectedListing.serviceCategory}
                </p>
              </div>
              <button
                onClick={handleCloseModal}
                style={{
                  background: 'none', border: 'none',
                  fontSize: '1.75rem', cursor: 'pointer',
                  color: 'var(--text-secondary)', lineHeight: 1
                }}
              >
                &times;
              </button>
            </div>

            <div style={{ padding: '1.75rem', display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>

              {/* Left: Listing Details */}
              <div style={{ flex: 1, minWidth: '260px' }}>
                {/* Emoji header block */}
                <div style={{
                  height: '120px',
                  background: `linear-gradient(135deg, hsl(${(selectedListing.id * 47) % 360}, 60%, 35%), hsl(${(selectedListing.id * 47 + 60) % 360}, 70%, 20%))`,
                  borderRadius: 'var(--radius-md)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '3rem',
                  marginBottom: '1.25rem'
                }}>
                  {selectedListing.serviceCategory === 'Cleaning' ? '🧹' :
                   selectedListing.serviceCategory === 'Gardening' ? '🌱' :
                   selectedListing.serviceCategory === 'Handyman' ? '🔧' :
                   selectedListing.serviceCategory === 'Removals' ? '📦' :
                   selectedListing.serviceCategory === 'Painting' ? '🎨' :
                   selectedListing.serviceCategory === 'IT' ? '💻' : '🛠️'}
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.85rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>Business:</span>
                    <strong>{selectedListing.businessName}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>Category:</span>
                    <strong>{selectedListing.serviceCategory}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>Working Hours:</span>
                    <strong>{selectedListing.availableFrom} – {selectedListing.availableTo}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>Rate:</span>
                    <strong style={{ color: 'var(--accent)', fontSize: '1.1rem' }}>
                      ${selectedListing.pricing?.toFixed ? selectedListing.pricing.toFixed(2) : selectedListing.pricing}/hr
                    </strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>Rating:</span>
                    {(() => {
                      const info = getProviderReviewsInfo(selectedListing.serviceProvider?.user?.emailAddress);
                      return (
                        <strong style={{ color: '#ffcc00' }}>
                          ★ {info.avgRating} ({info.count} review{info.count !== 1 ? 's' : ''})
                        </strong>
                      );
                    })()}
                  </div>
                  <hr style={{ border: 'none', borderTop: '1px solid var(--border)' }} />
                  <p style={{ margin: 0, color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                    {selectedListing.serviceDescription || 'Professional, reliable, and fully insured service provider. Available at flexible hours to meet your scheduling needs.'}
                  </p>

                  {(() => {
                    const info = getProviderReviewsInfo(selectedListing.serviceProvider?.user?.emailAddress);
                    return (
                      <div style={{ marginTop: '1.5rem' }}>
                        <h4 style={{ fontSize: '0.9rem', fontWeight: '800', marginBottom: '0.75rem', borderBottom: '1px solid var(--border)', paddingBottom: '0.35rem' }}>
                          ⭐ Client Reviews ({info.count})
                        </h4>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', maxHeight: '200px', overflowY: 'auto' }}>
                          {info.reviews.map((r, idx) => (
                            <div key={r.id || idx} style={{ backgroundColor: 'var(--bg-primary)', padding: '0.75rem', borderRadius: '4px', border: '1px solid var(--border)' }}>
                              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem', fontSize: '0.75rem' }}>
                                <strong style={{ color: 'var(--text-primary)' }}>{r.customerName}</strong>
                                <span style={{ color: '#ffcc00' }}>{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</span>
                              </div>
                              <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                                {r.comment}
                              </p>
                              <span style={{ fontSize: '0.65rem', color: 'var(--text-secondary)', display: 'block', marginTop: '0.25rem', textAlign: 'right' }}>
                                {r.date}
                              </span>
                            </div>
                          ))}
                        </div>
                      </div>
                    );
                  })()}
                </div>
              </div>

              {/* Right: Booking or Checkout */}
              <div style={{ flex: 1, minWidth: '280px' }}>
                {createdBooking ? (
                  /* Checkout view after booking created */
                  <div>
                    <h3 style={{ fontSize: '1.05rem', fontWeight: '800', marginBottom: '0.5rem' }}>Complete Payment</h3>
                    <div style={{
                      backgroundColor: 'var(--bg-primary)',
                      border: '1px solid var(--border)',
                      borderRadius: 'var(--radius-md)',
                      padding: '1rem',
                      marginBottom: '1.25rem',
                      fontSize: '0.82rem',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '0.5rem'
                    }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ color: 'var(--text-secondary)' }}>Booking #:</span>
                        <strong>#{createdBooking.id}</strong>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ color: 'var(--text-secondary)' }}>Date:</span>
                        <strong>{bookDates}</strong>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ color: 'var(--text-secondary)' }}>Time Window:</span>
                        <strong>{bookFrom} – {bookTo}</strong>
                      </div>
                      <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '0.25rem 0' }} />
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1rem', fontWeight: '800' }}>
                        <span>Total:</span>
                        <span style={{ color: 'var(--accent)' }}>AUD ${createdBooking.totalCost?.toFixed(2)}</span>
                      </div>
                    </div>
                    <CheckoutForm
                      bookingId={createdBooking.id}
                      amount={createdBooking.totalCost || 0}
                      onSuccess={() => {
                        handleCloseModal();
                        queryClient.invalidateQueries({ queryKey: ['my-bookings'] });
                        alert('Payment complete! Your booking is confirmed. Check your dashboard for details.');
                      }}
                    />
                  </div>
                ) : (
                  /* Scheduling / Booking form */
                  <form onSubmit={handleBookNow}>
                    <h3 style={{ fontSize: '1.05rem', fontWeight: '800', marginBottom: '1.25rem' }}>
                      📅 Schedule & Book
                    </h3>

                    {bookingError && (
                      <div style={{
                        backgroundColor: 'var(--error-bg)',
                        color: 'var(--error)',
                        padding: '0.75rem 1rem',
                        borderRadius: 'var(--radius-sm)',
                        fontSize: '0.82rem',
                        marginBottom: '1rem'
                      }}>
                        {bookingError}
                      </div>
                    )}

                    <div className="form-group">
                      <label className="form-label" htmlFor="book-date">Preferred Date</label>
                      <input
                        id="book-date"
                        type="date"
                        className="form-control"
                        required
                        min={new Date().toISOString().split('T')[0]}
                        value={bookDates}
                        onChange={(e) => setBookDates(e.target.value)}
                      />
                    </div>

                    <div style={{ display: 'flex', gap: '1rem' }}>
                      <div className="form-group" style={{ flex: 1 }}>
                        <label className="form-label" htmlFor="book-from">Start Time</label>
                        <input
                          id="book-from"
                          type="time"
                          className="form-control"
                          required
                          value={bookFrom}
                          onChange={(e) => setBookFrom(e.target.value)}
                        />
                      </div>
                      <div className="form-group" style={{ flex: 1 }}>
                        <label className="form-label" htmlFor="book-to">End Time</label>
                        <input
                          id="book-to"
                          type="time"
                          className="form-control"
                          required
                          value={bookTo}
                          onChange={(e) => setBookTo(e.target.value)}
                        />
                      </div>
                    </div>

                    {/* Estimated cost preview */}
                    {bookDates && bookFrom && bookTo && (() => {
                      const [fh, fm] = bookFrom.split(':').map(Number);
                      const [th, tm] = bookTo.split(':').map(Number);
                      const hours = Math.max(0, (th * 60 + tm - fh * 60 - fm) / 60);
                      const rate = selectedListing.pricing || 0;
                      const estimate = (hours * rate).toFixed(2);
                      return hours > 0 ? (
                        <div style={{
                          backgroundColor: 'var(--accent-bg)',
                          border: '1px solid var(--accent)',
                          borderRadius: 'var(--radius-sm)',
                          padding: '0.75rem 1rem',
                          fontSize: '0.82rem',
                          marginBottom: '1rem',
                          display: 'flex',
                          justifyContent: 'space-between'
                        }}>
                          <span style={{ color: 'var(--text-secondary)' }}>
                            {hours.toFixed(1)} hrs × ${rate}/hr =
                          </span>
                          <strong style={{ color: 'var(--accent)' }}>≈ AUD ${estimate}</strong>
                        </div>
                      ) : null;
                    })()}

                    <button
                      type="submit"
                      className="submit-btn"
                      disabled={bookDirectMutation.isPending || isBookingLoading}
                      style={{ marginTop: '0.5rem' }}
                    >
                      {bookDirectMutation.isPending
                        ? 'Creating Booking...'
                        : isAuthenticated
                          ? '⚡ Book Now & Pay'
                          : '🔑 Sign In to Book'}
                    </button>

                    <p style={{ fontSize: '0.72rem', color: 'var(--text-secondary)', marginTop: '0.75rem', textAlign: 'center' }}>
                      A booking proposal will be sent to the provider. Payment is collected once confirmed.
                    </p>
                  </form>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
