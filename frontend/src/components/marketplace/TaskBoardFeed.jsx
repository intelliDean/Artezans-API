import { useQuery } from '@tanstack/react-query';
import api from '../../services/api';

export const TaskBoardFeed = () => {
  // Fetch active tasks from backend, polling every 30 seconds
  const { data: tasks, isLoading, isError, error } = useQuery({
    queryKey: ['active-tasks'],
    queryFn: async () => {
      const response = await api.get('/task/active-tasks');
      return response.data;
    },
    refetchInterval: 30000, // 30s auto-refresh
  });

  const formatBudget = (budget) => {
    if (budget === undefined || budget === null) return '$0';
    return new Intl.NumberFormat('en-AU', { style: 'currency', currency: 'AUD', maximumFractionDigits: 0 }).format(budget);
  };

  const formatDate = (dateArray) => {
    if (!dateArray || dateArray.length === 0) return 'Flexible';
    try {
      const date = new Date(dateArray[0]);
      return date.toLocaleDateString('en-AU', { day: 'numeric', month: 'short' });
    } catch {
      return 'Flexible';
    }
  };

  if (isLoading) {
    return (
      <div style={{ padding: '2rem 0', textAlign: 'center' }}>
        <div style={{
          width: '30px',
          height: '30px',
          border: '3px solid var(--border)',
          borderTopColor: 'var(--accent)',
          borderRadius: '50%',
          animation: 'spin 1s linear infinite',
          margin: '0 auto 1rem auto'
        }}></div>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Loading active tasks...</p>
        <style>{`
          @keyframes spin {
            to { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    );
  }

  if (isError) {
    return (
      <div style={{
        padding: '1.5rem',
        backgroundColor: 'var(--error-bg)',
        border: '1px solid rgba(255, 69, 58, 0.2)',
        borderRadius: 'var(--radius-md)',
        color: 'var(--error)',
        fontSize: '0.9rem',
        margin: '1.5rem 0'
      }}>
        Failed to load active tasks: {error.message || 'Server connection error'}
      </div>
    );
  }

  return (
    <div style={{ margin: '2.5rem 0' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
        <div>
          <h3 style={{ fontSize: '1.25rem', fontWeight: '700' }}>Active Tasks Board</h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Live bids and opportunities matching local requests</p>
        </div>
        <span style={{
          backgroundColor: 'var(--success-bg)',
          color: 'var(--success)',
          padding: '0.25rem 0.6rem',
          borderRadius: '20px',
          fontSize: '0.75rem',
          fontWeight: '700',
          display: 'flex',
          alignItems: 'center',
          gap: '0.3rem'
        }}>
          <span style={{
            width: '6px',
            height: '6px',
            backgroundColor: 'var(--success)',
            borderRadius: '50%',
            animation: 'pulse 1.5s infinite'
          }}></span>
          Live Feed
        </span>
        <style>{`
          @keyframes pulse {
            0% { transform: scale(0.95); opacity: 0.5; }
            50% { transform: scale(1.15); opacity: 1; }
            100% { transform: scale(0.95); opacity: 0.5; }
          }
        `}</style>
      </div>

      {(!tasks || tasks.length === 0) ? (
        <div style={{
          border: '2px dashed var(--border)',
          borderRadius: 'var(--radius-md)',
          padding: '3rem 1.5rem',
          textAlign: 'center',
          color: 'var(--text-secondary)',
          backgroundColor: 'var(--bg-secondary)'
        }}>
          <span style={{ fontSize: '2rem', display: 'block', marginBottom: '0.5rem' }}>📋</span>
          <strong>No active tasks available right now</strong>
          <p style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}>Be the first to post a new job and find providers!</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {tasks.map((task) => (
            <div
              key={task.id}
              style={{
                backgroundColor: 'var(--bg-secondary)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-md)',
                padding: '1.25rem 1.5rem',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                boxShadow: 'var(--shadow-sm)',
                transition: 'all 0.15s ease'
              }}
              onMouseOver={(e) => {
                e.currentTarget.style.borderColor = 'var(--accent)';
                e.currentTarget.style.boxShadow = 'var(--shadow-md)';
              }}
              onMouseOut={(e) => {
                e.currentTarget.style.borderColor = 'var(--border)';
                e.currentTarget.style.boxShadow = 'var(--shadow-sm)';
              }}
            >
              <div style={{ display: 'flex', gap: '1.25rem', alignItems: 'center', flex: 1, minWidth: 0 }}>
                <div style={{
                  width: '44px',
                  height: '44px',
                  borderRadius: '50%',
                  backgroundColor: 'var(--bg-primary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '1.4rem',
                  flexShrink: 0
                }}>
                  🛠️
                </div>
                <div style={{ minWidth: 0, flex: 1 }}>
                  <h4 style={{
                    fontSize: '0.95rem',
                    fontWeight: '700',
                    color: 'var(--text-primary)',
                    marginBottom: '0.2rem',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis'
                  }}>
                    {task.taskServiceName}
                  </h4>
                  <p style={{
                    fontSize: '0.8rem',
                    color: 'var(--text-secondary)',
                    marginBottom: '0.4rem',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis'
                  }}>
                    {task.taskDescription || 'No description provided.'}
                  </p>
                  <div style={{ display: 'flex', gap: '1rem', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                    <span>📍 {task.userAddress || 'Australia'}</span>
                    <span>📅 {formatDate(task.taskDates)}</span>
                  </div>
                </div>
              </div>

              <div style={{ textAlign: 'right', flexShrink: 0, marginLeft: '1.5rem' }}>
                <div style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--accent)', marginBottom: '0.15rem' }}>
                  {formatBudget(task.customerBudget)}
                </div>
                <span style={{
                  fontSize: '0.65rem',
                  fontWeight: '700',
                  color: 'var(--success)',
                  backgroundColor: 'var(--success-bg)',
                  padding: '0.15rem 0.4rem',
                  borderRadius: '4px',
                  textTransform: 'uppercase',
                  letterSpacing: '0.05em'
                }}>
                  Open
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
