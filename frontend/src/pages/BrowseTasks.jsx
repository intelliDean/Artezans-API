import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../services/api';
import { useAuth } from '../hooks/useAuth';

const CATEGORIES = [
  { label: 'All Tasks', value: '' },
  { label: '🧹 Cleaning', value: 'Cleaning', keywords: ['clean', 'wash', 'vacuum', 'dust', 'maid', 'rubbish'] },
  { label: '🌱 Gardening', value: 'Gardening', keywords: ['garden', 'lawn', 'mow', 'weed', 'plant', 'trim', 'yard', 'outdoor'] },
  { label: '🔧 Handyman', value: 'Handyman', keywords: ['fix', 'repair', 'handyman', 'drill', 'install', 'mount', 'wood', 'door', 'lock'] },
  { label: '📦 Removals', value: 'Removals', keywords: ['move', 'removal', 'deliver', 'truck', 'pack', 'lift', 'furniture'] },
  { label: '🎨 Painting', value: 'Painting', keywords: ['paint', 'brush', 'wall', 'stain', 'deck'] },
  { label: '💻 IT & Computers', value: 'IT', keywords: ['computer', 'it', 'wifi', 'network', 'software', 'tech', 'laptop', 'setup'] },
];

export const BrowseTasks = () => {
  const { user, isAuthenticated, openLoginModal } = useAuth();

  // Search/Filter state
  const [activeCategory, setActiveCategory] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchLocation, setSearchLocation] = useState('');

  // Selected task state
  const [selectedTask, setSelectedTask] = useState(null);

  // Bidding form state
  const [bidAmount, setBidAmount] = useState('');
  const [bidMessage, setBidMessage] = useState('');
  const [bidSuccess, setBidSuccess] = useState('');
  const [bidError, setBidError] = useState('');

  // Query: Fetch active tasks from backend
  const { data: activeTasks, isLoading } = useQuery({
    queryKey: ['active-tasks'],
    queryFn: async () => {
      const response = await api.get('/task/active-tasks');
      return response.data;
    },
  });

  const isServiceProvider = user?.roles?.includes('SERVICE_PROVIDER') || user?.roles?.includes('ROLE_SERVICE_PROVIDER');

  // Filter tasks based on category, keyword, and location
  let displayTasks = activeTasks || [];

  // 1. Keep only active tasks
  displayTasks = displayTasks.filter((t) => t.isActive);

  // 2. Filter by category keywords
  if (activeCategory) {
    const selectedCat = CATEGORIES.find((c) => c.value === activeCategory);
    if (selectedCat && selectedCat.keywords) {
      displayTasks = displayTasks.filter((t) => {
        const title = (t.taskServiceName || '').toLowerCase();
        const desc = (t.taskDescription || '').toLowerCase();
        return selectedCat.keywords.some(
          (kw) => title.includes(kw) || desc.includes(kw)
        );
      });
    }
  }

  // 3. Filter by search keyword
  if (searchKeyword) {
    const query = searchKeyword.toLowerCase();
    displayTasks = displayTasks.filter(
      (t) =>
        (t.taskServiceName || '').toLowerCase().includes(query) ||
        (t.taskDescription || '').toLowerCase().includes(query)
    );
  }

  // 4. Filter by location
  if (searchLocation) {
    const loc = searchLocation.toLowerCase();
    displayTasks = displayTasks.filter((t) =>
      (t.userAddress || '').toLowerCase().includes(loc)
    );
  }

  const handleSendBid = (e) => {
    e.preventDefault();
    setBidSuccess('');
    setBidError('');

    if (!isAuthenticated) {
      openLoginModal();
      return;
    }

    if (!isServiceProvider) {
      setBidError('Only registered Service Providers can submit bid offers.');
      return;
    }

    const amount = Number(bidAmount);
    if (isNaN(amount) || amount <= 0) {
      setBidError('Please enter a valid bid amount.');
      return;
    }

    // Load existing local bids
    const existingBids = JSON.parse(localStorage.getItem('provider_bids') || '[]');

    // Check if provider has already bid on this task
    const alreadyBid = existingBids.some(
      (b) => b.taskId === selectedTask.id && b.providerName === `${user.firstName} ${user.lastName}`
    );

    if (alreadyBid) {
      setBidError('You have already submitted a bid offer for this job.');
      return;
    }

    // Save bid details locally
    const newBid = {
      id: Date.now(),
      taskId: selectedTask.id,
      providerName: `${user.firstName} ${user.lastName}`,
      providerRating: 4.8,
      providerJobs: 37,
      avatar: '👩‍🎨',
      bidAmount: amount,
      message: bidMessage,
      duration: 'Flexible',
      taskDate: selectedTask.taskDates?.[0] || new Date().toISOString().split('T')[0]
    };

    localStorage.setItem('provider_bids', JSON.stringify([...existingBids, newBid]));
    setBidSuccess('Your offer proposal was sent successfully! The customer will review it.');
    
    // Reset form
    setTimeout(() => {
      setBidSuccess('');
      setBidAmount('');
      setBidMessage('');
      setSelectedTask(null);
    }, 2000);
  };

  const handleCloseModal = () => {
    setSelectedTask(null);
    setBidAmount('');
    setBidMessage('');
    setBidSuccess('');
    setBidError('');
  };

  return (
    <div className="container" style={{ padding: '2rem 1.5rem' }}>

      {/* Header section */}
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: '900', margin: '0 0 0.25rem 0' }}>
          Browse Custom Tasks
        </h1>
        <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', margin: 0 }}>
          Find local tasks posted by customers and submit your bidding offers.
        </p>
      </div>

      {/* Search inputs */}
      <div style={{
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
          placeholder="Search task keywords (e.g. mowing, cleaning)..."
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
          style={{ flex: 1, minWidth: '220px' }}
        />
        <input
          type="text"
          className="form-control"
          placeholder="Filter by suburb/state (e.g. Sydney)..."
          value={searchLocation}
          onChange={(e) => setSearchLocation(e.target.value)}
          style={{ flex: 1, minWidth: '160px' }}
        />
        {(searchKeyword || searchLocation) && (
          <button
            onClick={() => { setSearchKeyword(''); setSearchLocation(''); }}
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
            Clear Filters
          </button>
        )}
      </div>

      {/* Category Chips */}
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

      {/* Status Counters */}
      <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
        {isLoading ? 'Loading active tasks...' : `Found ${displayTasks.length} open task request${displayTasks.length !== 1 ? 's' : ''}`}
        {activeCategory && ` matching "${activeCategory}"`}
      </p>

      {/* Task Cards Grid */}
      {isLoading ? (
        <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text-secondary)' }}>
          <p>Connecting to task feed board...</p>
        </div>
      ) : displayTasks.length === 0 ? (
        <div style={{
          textAlign: 'center',
          padding: '4rem 2rem',
          border: '2px dashed var(--border)',
          borderRadius: 'var(--radius-md)',
          backgroundColor: 'var(--bg-secondary)',
          color: 'var(--text-secondary)'
        }}>
          <span style={{ fontSize: '2.5rem', display: 'block', marginBottom: '0.5rem' }}>📋</span>
          <strong>No matching tasks found</strong>
          <p style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}>
            Try clearing search inputs or exploring other service categories.
          </p>
        </div>
      ) : (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
          gap: '1.5rem'
        }}>
          {displayTasks.map((task) => (
            <div
              key={task.id}
              style={{
                backgroundColor: 'var(--bg-secondary)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-lg)',
                padding: '1.5rem',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.75rem',
                cursor: 'pointer',
                boxShadow: 'var(--shadow-sm)',
                transition: 'box-shadow 0.2s ease, transform 0.2s ease'
              }}
              onClick={() => setSelectedTask(task)}
              onMouseOver={(e) => {
                e.currentTarget.style.boxShadow = 'var(--shadow-md)';
                e.currentTarget.style.transform = 'translateY(-2px)';
              }}
              onMouseOut={(e) => {
                e.currentTarget.style.boxShadow = 'var(--shadow-sm)';
                e.currentTarget.style.transform = 'translateY(0)';
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <h3 style={{ fontSize: '1.05rem', fontWeight: '800', margin: 0 }}>
                  {task.taskServiceName}
                </h3>
                <span style={{
                  fontSize: '0.65rem',
                  fontWeight: '700',
                  backgroundColor: 'var(--success-bg)',
                  color: 'var(--success)',
                  padding: '0.2rem 0.5rem',
                  borderRadius: '4px'
                }}>
                  Open
                </span>
              </div>

              <p style={{
                fontSize: '0.8rem',
                color: 'var(--text-secondary)',
                margin: 0,
                flexGrow: 1,
                display: '-webkit-box',
                WebkitLineClamp: 2,
                WebkitBoxOrient: 'vertical',
                overflow: 'hidden',
                lineHeight: '1.4'
              }}>
                {task.taskDescription}
              </p>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.75rem', color: 'var(--text-secondary)', borderTop: '1px solid var(--border)', paddingTop: '0.75rem', marginTop: '0.25rem' }}>
                <span>📍 {task.userAddress}</span>
                <span>📅 Scheduled: {task.taskDates?.join(', ') || 'Flexible'}</span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '0.25rem' }}>
                <div>
                  <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)', display: 'block' }}>Budget limit</span>
                  <strong style={{ fontSize: '1.2rem', color: 'var(--accent)' }}>${task.customerBudget}</strong>
                </div>
                <button
                  className="btn-primary"
                  style={{ padding: '0.45rem 1.2rem', fontSize: '0.75rem', backgroundColor: 'var(--accent)', border: 'none' }}
                  onClick={(e) => { e.stopPropagation(); setSelectedTask(task); }}
                >
                  Apply & Bid
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Task Details & Bid Form Modal */}
      {selectedTask && (
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
            maxWidth: '650px',
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
              borderBottom: '1px solid var(--border)'
            }}>
              <div>
                <h2 style={{ fontSize: '1.2rem', fontWeight: '800', margin: '0 0 0.15rem 0' }}>
                  {selectedTask.taskServiceName}
                </h2>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                  Task proposal ID: #{selectedTask.id}
                </span>
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

            <div style={{ padding: '1.75rem', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              
              {/* Task Details Info */}
              <div style={{
                backgroundColor: 'var(--bg-primary)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-md)',
                padding: '1.25rem',
                fontSize: '0.85rem',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.75rem'
              }}>
                <p style={{ margin: 0, color: 'var(--text-primary)', lineHeight: '1.5', fontSize: '0.9rem' }}>
                  <strong>Task Description:</strong><br />
                  {selectedTask.taskDescription}
                </p>
                <hr style={{ border: 'none', borderTop: '1px solid var(--border)' }} />
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div>
                    <span style={{ color: 'var(--text-secondary)', display: 'block' }}>Location:</span>
                    <strong>📍 {selectedTask.userAddress}</strong>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-secondary)', display: 'block' }}>Job Budget:</span>
                    <strong style={{ color: 'var(--accent)', fontSize: '1.1rem' }}>${selectedTask.customerBudget}</strong>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-secondary)', display: 'block' }}>Proposed Dates:</span>
                    <strong>📅 {selectedTask.taskDates?.join(', ') || 'Flexible'}</strong>
                  </div>
                </div>
              </div>

              {/* Bidding Form Area */}
              <div>
                <h3 style={{ fontSize: '1rem', fontWeight: '800', marginBottom: '1rem' }}>
                  ⚡ Submit Your Offer Proposal
                </h3>

                {bidSuccess && (
                  <div style={{
                    backgroundColor: 'var(--success-bg)',
                    color: 'var(--success)',
                    padding: '1rem',
                    borderRadius: 'var(--radius-md)',
                    fontSize: '0.85rem',
                    textAlign: 'center',
                    marginBottom: '1rem'
                  }}>
                    {bidSuccess}
                  </div>
                )}

                {bidError && (
                  <div style={{
                    backgroundColor: 'var(--error-bg)',
                    color: 'var(--error)',
                    padding: '0.8rem 1rem',
                    borderRadius: 'var(--radius-md)',
                    fontSize: '0.82rem',
                    marginBottom: '1rem'
                  }}>
                    {bidError}
                  </div>
                )}

                {!isAuthenticated ? (
                  <div style={{ textAlign: 'center', padding: '1.5rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)' }}>
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>You must be signed in to submit bid offers.</p>
                    <button className="btn-primary" onClick={openLoginModal} style={{ padding: '0.5rem 1.5rem', marginTop: '0.5rem' }}>
                      Sign In Session
                    </button>
                  </div>
                ) : !isServiceProvider ? (
                  <div style={{
                    backgroundColor: 'var(--accent-bg)',
                    color: 'var(--accent)',
                    padding: '1rem',
                    borderRadius: 'var(--radius-md)',
                    fontSize: '0.82rem',
                    textAlign: 'center'
                  }}>
                    ℹ️ Only users with the **Service Provider** profile role can bid on marketplace tasks.
                  </div>
                ) : (
                  <form onSubmit={handleSendBid} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                    <div className="form-group">
                      <label className="form-label" htmlFor="offer-price">Your Offer Budget (AUD)</label>
                      <input
                        id="offer-price"
                        type="number"
                        className="form-control"
                        required
                        min={10}
                        placeholder={`e.g. ${Math.round(selectedTask.customerBudget * 0.95)}`}
                        value={bidAmount}
                        onChange={(e) => setBidAmount(e.target.value)}
                      />
                      <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>
                        Client's budget is ${selectedTask.customerBudget}
                      </span>
                    </div>

                    <div className="form-group">
                      <label className="form-label" htmlFor="offer-message">Cover Message / Introduction</label>
                      <textarea
                        id="offer-message"
                        className="form-control"
                        required
                        rows={4}
                        placeholder="Introduce yourself and explain why you're a great fit for this task..."
                        value={bidMessage}
                        onChange={(e) => setBidMessage(e.target.value)}
                      />
                    </div>

                    <button type="submit" className="submit-btn" style={{ marginTop: '0.5rem' }}>
                      ⚡ Submit Offer Proposal
                    </button>
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
