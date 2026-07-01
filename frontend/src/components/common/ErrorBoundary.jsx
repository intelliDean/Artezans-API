import { Component } from 'react';

/**
 * Global Error Boundary — catches uncaught JS errors anywhere in the component tree
 * and renders a friendly fallback UI instead of a blank white screen.
 */
export class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, info) {
    console.error('ErrorBoundary caught:', error, info.componentStack);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          minHeight: '80vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          textAlign: 'center',
          padding: '4rem 2rem',
          gap: '1rem',
        }}>
          <div style={{ fontSize: '4rem', lineHeight: 1 }}>⚠️</div>
          <h1 style={{ fontSize: '1.8rem', fontWeight: '800', margin: 0 }}>
            Something went wrong
          </h1>
          <p style={{
            color: 'var(--text-secondary)',
            maxWidth: '440px',
            lineHeight: '1.6',
            margin: 0,
            fontSize: '0.95rem',
          }}>
            An unexpected error occurred. Try refreshing the page — if the problem persists, contact support.
          </p>
          {this.state.error && (
            <details style={{
              marginTop: '0.5rem',
              padding: '0.75rem 1rem',
              background: 'var(--error-bg)',
              border: '1px solid rgba(255,69,58,0.2)',
              borderRadius: 'var(--radius-sm)',
              maxWidth: '600px',
              width: '100%',
              textAlign: 'left',
              cursor: 'pointer',
              fontSize: '0.78rem',
              color: 'var(--text-secondary)',
            }}>
              <summary style={{ fontWeight: '600', marginBottom: '0.25rem', color: 'var(--error)' }}>
                Error details (for developers)
              </summary>
              <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word', margin: 0 }}>
                {this.state.error.toString()}
              </pre>
            </details>
          )}
          <button
            onClick={() => window.location.reload()}
            style={{
              marginTop: '0.5rem',
              padding: '0.75rem 2rem',
              backgroundColor: 'var(--accent)',
              color: 'white',
              border: 'none',
              borderRadius: 'var(--radius-sm)',
              fontWeight: '700',
              fontSize: '0.95rem',
              cursor: 'pointer',
              transition: 'opacity 0.2s',
            }}
            onMouseEnter={e => e.target.style.opacity = '0.85'}
            onMouseLeave={e => e.target.style.opacity = '1'}
          >
            Refresh Page
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
