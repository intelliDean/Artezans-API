import { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { CategoryGrid } from '../components/marketplace/CategoryGrid';
import { TaskBoardFeed } from '../components/marketplace/TaskBoardFeed';

export const Home = () => {
  const { isAuthenticated, openLoginModal, openPostTaskModal } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');

  const handlePostTaskClick = () => {
    if (!isAuthenticated) {
      openLoginModal();
    } else {
      openPostTaskModal();
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      console.log(`Searching listings for: ${searchQuery}`);
      // Future integration: redirect to /listings?q=searchQuery
    }
  };

  return (
    <div className="container" style={{ padding: '2rem 1.5rem' }}>
      
      {/* 1. Hero Section */}
      <div style={{
        background: 'linear-gradient(135deg, #1d1d1f, #2d2d2f)',
        color: '#ffffff',
        borderRadius: 'var(--radius-lg)',
        padding: '3.5rem 2rem',
        textAlign: 'center',
        marginBottom: '2.5rem',
        boxShadow: 'var(--shadow-md)',
        position: 'relative',
        overflow: 'hidden'
      }}>
        {/* Subtle grid patterns */}
        <div style={{
          position: 'absolute',
          top: 0, left: 0, right: 0, bottom: 0,
          background: 'radial-gradient(circle at 80% 20%, rgba(10, 132, 255, 0.15), transparent 45%)',
          pointerEvents: 'none'
        }}></div>

        <h2 style={{ fontSize: '2.2rem', color: '#ffffff', marginBottom: '0.75rem', letterSpacing: '-0.02em' }}>
          Get Anything Done, Your Way
        </h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', maxWidth: '600px', margin: '0 auto 2rem auto', lineHeight: '1.6' }}>
          Book fixed-price services instantly from local pros, or post a task describing what you need and receive competitive bids.
        </p>

        {/* Unified Search and Action Row */}
        <form onSubmit={handleSearchSubmit} style={{
          backgroundColor: '#ffffff',
          borderRadius: '50px',
          padding: '0.4rem',
          maxWidth: '680px',
          margin: '0 auto',
          display: 'flex',
          alignItems: 'center',
          boxShadow: 'var(--shadow-lg)',
          border: '1px solid var(--border)'
        }}>
          <span style={{ fontSize: '1.2rem', padding: '0 0.5rem 0 1rem', color: 'var(--text-secondary)' }}>🔍</span>
          <input
            type="text"
            placeholder="What service do you need? (e.g. Lawn mowing, plumbing)"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{
              border: 'none',
              outline: 'none',
              padding: '0.6rem 0.5rem',
              flex: 1,
              fontSize: '0.95rem',
              color: '#1d1d1f',
              backgroundColor: 'transparent'
            }}
          />
          <button type="submit" className="btn-primary" style={{ borderRadius: '25px', padding: '0.6rem 1.5rem', fontSize: '0.85rem' }}>
            Search
          </button>
          <span style={{ width: '1px', height: '24px', backgroundColor: 'var(--border)', margin: '0 0.5rem' }}></span>
          <button
            type="button"
            className="btn-primary"
            onClick={handlePostTaskClick}
            style={{
              backgroundColor: '#34c759',
              borderRadius: '25px',
              padding: '0.6rem 1.5rem',
              fontSize: '0.85rem',
              whiteSpace: 'nowrap',
              border: 'none'
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#30b0c7'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#34c759'}
          >
            Post a Task
          </button>
        </form>
      </div>

      {/* 2. Dual Pathway Cards */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
        gap: '1.5rem',
        marginBottom: '3rem'
      }}>
        {/* Booking Card */}
        <div style={{
          backgroundColor: 'var(--bg-secondary)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius-lg)',
          padding: '1.75rem',
          display: 'flex',
          flexDirection: 'column',
          gap: '1rem',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <span style={{ fontSize: '2rem' }}>🛍️</span>
          <h3 style={{ fontSize: '1.1rem', fontWeight: '700' }}>Book a Service</h3>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: 0, flexGrow: 1, lineHeight: '1.5' }}>
            Browse predefined, fixed-price services posted by verified service providers. Choose standard packages, pick a date, and pay securely.
          </p>
          <a href="#" style={{
            fontSize: '0.9rem',
            fontWeight: '700',
            color: 'var(--accent)',
            display: 'flex',
            alignItems: 'center',
            gap: '0.2rem',
            marginTop: '0.5rem'
          }}>
            Find Services &rarr;
          </a>
        </div>

        {/* Task Card */}
        <div style={{
          backgroundColor: 'var(--bg-secondary)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius-lg)',
          padding: '1.75rem',
          display: 'flex',
          flexDirection: 'column',
          gap: '1rem',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <span style={{ fontSize: '2rem' }}>📋</span>
          <h3 style={{ fontSize: '1.1rem', fontWeight: '700' }}>Post a Custom Task</h3>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: 0, flexGrow: 1, lineHeight: '1.5' }}>
            Can't find a matching standard package? Explain exactly what you need, set a comfortable budget, and select from competitive offers submitted by local pros.
          </p>
          <button
            onClick={handlePostTaskClick}
            style={{
              background: 'none',
              border: 'none',
              padding: 0,
              fontSize: '0.9rem',
              fontWeight: '700',
              color: '#34c759',
              display: 'flex',
              alignItems: 'center',
              gap: '0.2rem',
              textAlign: 'left',
              marginTop: '0.5rem'
            }}
          >
            Post Your Task &rarr;
          </button>
        </div>
      </div>

      {/* 3. Category Grid */}
      <CategoryGrid />

      <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '2.5rem 0' }} />

      {/* 4. Live Task Board Feed */}
      <TaskBoardFeed />

    </div>
  );
};
