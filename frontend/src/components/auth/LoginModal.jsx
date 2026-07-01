import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../hooks/useAuth';

export const LoginModal = () => {
  const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1').replace('/api/v1', '');
  const REDIRECT_URI = `${window.location.origin}/`;

  const { isLoginModalOpen, login, closeLoginModal } = useAuth();
  const [isLoginTab, setIsLoginTab] = useState(true);
  
  // Login form state
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  
  // Registration form state (placeholder layout)
  const [regFirstName, setRegFirstName] = useState('');
  const [regLastName, setRegLastName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  
  const [errorMsg, setErrorMsg] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const dialogRef = useRef(null);

  // Sync native dialog state with AuthContext state
  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;

    if (isLoginModalOpen) {
      if (!dialog.open) {
        dialog.showModal();
        setErrorMsg('');
        setLoginEmail('');
        setLoginPassword('');
      }
    } else {
      if (dialog.open) {
        dialog.close();
      }
    }
  }, [isLoginModalOpen]);

  // Light dismiss: Close modal if user clicks outside dialog bounds (on backdrop)
  const handleBackdropClick = (e) => {
    if (e.target === dialogRef.current) {
      closeLoginModal();
    }
  };

  // Prevent Escape key from closing without updating context
  const handleCancel = (e) => {
    e.preventDefault();
    closeLoginModal();
  };

  // Sync aria-invalid with standard element validation states
  const handleFieldInteraction = (e) => {
    const el = e.target;
    // Let the browser evaluate :user-invalid state first, then sync
    setTimeout(() => {
      if (el.hasAttribute('aria-invalid') || el.matches(':user-invalid') || el.matches(':user-valid')) {
        el.setAttribute('aria-invalid', el.matches(':user-invalid') ? 'true' : 'false');
      }
    }, 50);
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setIsSubmitting(true);

    const result = await login(loginEmail, loginPassword);
    
    setIsSubmitting(false);
    if (!result.success) {
      setErrorMsg(result.error);
    }
  };

  const handleRegisterSubmit = (e) => {
    e.preventDefault();
    setErrorMsg('Registration is not implemented in this phase. Please log in with existing credentials.');
  };

  return (
    <dialog
      ref={dialogRef}
      onClick={handleBackdropClick}
      onCancel={handleCancel}
      aria-labelledby="modal-title"
    >
      <div className="modal-body">
        <button 
          className="modal-close-btn" 
          onClick={closeLoginModal}
          aria-label="Close modal"
        >
          &times;
        </button>

        {/* Auth Tab Switching */}
        <div className="auth-tabs">
          <button 
            className={`auth-tab ${isLoginTab ? 'active' : ''}`}
            onClick={() => { setIsLoginTab(true); setErrorMsg(''); }}
          >
            Sign In
          </button>
          <button 
            className={`auth-tab ${!isLoginTab ? 'active' : ''}`}
            onClick={() => { setIsLoginTab(false); setErrorMsg(''); }}
          >
            Register
          </button>
        </div>

        <h3 id="modal-title" className="text-center" style={{ marginBottom: '1.5rem' }}>
          {isLoginTab ? 'Welcome Back' : 'Create an Account'}
        </h3>

        {errorMsg && (
          <div style={{
            backgroundColor: 'var(--error-bg)',
            color: 'var(--error)',
            padding: '0.75rem 1rem',
            borderRadius: 'var(--radius-md)',
            fontSize: '0.85rem',
            fontWeight: '600',
            marginBottom: '1.25rem',
            border: '1px solid rgba(255, 69, 58, 0.2)'
          }}>
            {errorMsg}
          </div>
        )}

        {isLoginTab ? (
          /* Login Form */
          <form onSubmit={handleLoginSubmit}>
            <div className="form-group">
              <label className="form-label" htmlFor="login-email">Email Address</label>
              <input
                id="login-email"
                type="email"
                className="form-control"
                required
                placeholder="you@example.com"
                value={loginEmail}
                onChange={(e) => setLoginEmail(e.target.value)}
                onBlur={handleFieldInteraction}
                onInput={handleFieldInteraction}
              />
              <span className="error-message">Please enter a valid email address.</span>
            </div>

            <div className="form-group" style={{ marginBottom: '2rem' }}>
              <label className="form-label" htmlFor="login-password">Password</label>
              <input
                id="login-password"
                type="password"
                className="form-control"
                required
                minLength={4}
                placeholder="••••••••"
                value={loginPassword}
                onChange={(e) => setLoginPassword(e.target.value)}
                onBlur={handleFieldInteraction}
                onInput={handleFieldInteraction}
              />
              <span className="error-message">Password must be at least 4 characters.</span>
            </div>

            <button
              type="submit"
              className="submit-btn"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Signing In...' : 'Sign In'}
            </button>

            {/* OAuth2 Divider */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', margin: '1.5rem 0 1rem' }}>
              <hr style={{ flex: 1, border: 'none', borderTop: '1px solid var(--border)' }} />
              <span style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', whiteSpace: 'nowrap' }}>or continue with</span>
              <hr style={{ flex: 1, border: 'none', borderTop: '1px solid var(--border)' }} />
            </div>

            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <a
                href={`${BACKEND_URL}/oauth2/authorization/google?redirect_uri=${encodeURIComponent(REDIRECT_URI)}`}
                style={{
                  flex: 1,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.5rem',
                  padding: '0.6rem 0.75rem',
                  border: '1px solid var(--border)',
                  borderRadius: 'var(--radius-sm)',
                  background: 'var(--bg-secondary)',
                  color: 'var(--text-primary)',
                  textDecoration: 'none',
                  fontWeight: '600',
                  fontSize: '0.85rem',
                  transition: 'background 0.2s, border-color 0.2s',
                }}
                onMouseEnter={e => { e.currentTarget.style.background = 'var(--bg-tertiary)'; e.currentTarget.style.borderColor = 'var(--accent)'; }}
                onMouseLeave={e => { e.currentTarget.style.background = 'var(--bg-secondary)'; e.currentTarget.style.borderColor = 'var(--border)'; }}
              >
                <svg width="18" height="18" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                Google
              </a>
              <a
                href={`${BACKEND_URL}/oauth2/authorization/github?redirect_uri=${encodeURIComponent(REDIRECT_URI)}`}
                style={{
                  flex: 1,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.5rem',
                  padding: '0.6rem 0.75rem',
                  border: '1px solid var(--border)',
                  borderRadius: 'var(--radius-sm)',
                  background: 'var(--bg-secondary)',
                  color: 'var(--text-primary)',
                  textDecoration: 'none',
                  fontWeight: '600',
                  fontSize: '0.85rem',
                  transition: 'background 0.2s, border-color 0.2s',
                }}
                onMouseEnter={e => { e.currentTarget.style.background = 'var(--bg-tertiary)'; e.currentTarget.style.borderColor = 'var(--accent)'; }}
                onMouseLeave={e => { e.currentTarget.style.background = 'var(--bg-secondary)'; e.currentTarget.style.borderColor = 'var(--border)'; }}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/>
                </svg>
                GitHub
              </a>
            </div>
          </form>
        ) : (
          /* Registration Form */
          <form onSubmit={handleRegisterSubmit}>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <div className="form-group" style={{ flex: 1 }}>
                <label className="form-label" htmlFor="reg-first">First Name</label>
                <input
                  id="reg-first"
                  type="text"
                  className="form-control"
                  required
                  value={regFirstName}
                  onChange={(e) => setRegFirstName(e.target.value)}
                />
              </div>
              <div className="form-group" style={{ flex: 1 }}>
                <label className="form-label" htmlFor="reg-last">Last Name</label>
                <input
                  id="reg-last"
                  type="text"
                  className="form-control"
                  required
                  value={regLastName}
                  onChange={(e) => setRegLastName(e.target.value)}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="reg-email">Email Address</label>
              <input
                id="reg-email"
                type="email"
                className="form-control"
                required
                placeholder="you@example.com"
                value={regEmail}
                onChange={(e) => setRegEmail(e.target.value)}
                onBlur={handleFieldInteraction}
                onInput={handleFieldInteraction}
              />
              <span className="error-message">Please enter a valid email address.</span>
            </div>

            <div className="form-group" style={{ marginBottom: '2rem' }}>
              <label className="form-label" htmlFor="reg-password">Password</label>
              <input
                id="reg-password"
                type="password"
                className="form-control"
                required
                minLength={6}
                placeholder="••••••••"
                value={regPassword}
                onChange={(e) => setRegPassword(e.target.value)}
                onBlur={handleFieldInteraction}
                onInput={handleFieldInteraction}
              />
              <span className="error-message">Password must be at least 6 characters.</span>
            </div>

            <button
              type="submit"
              className="submit-btn"
              disabled={isSubmitting}
            >
              Create Account
            </button>

            {/* OAuth2 Divider */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', margin: '1.5rem 0 1rem' }}>
              <hr style={{ flex: 1, border: 'none', borderTop: '1px solid var(--border)' }} />
              <span style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', whiteSpace: 'nowrap' }}>or sign up with</span>
              <hr style={{ flex: 1, border: 'none', borderTop: '1px solid var(--border)' }} />
            </div>

            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <a href={`${BACKEND_URL}/oauth2/authorization/google?redirect_uri=${encodeURIComponent(REDIRECT_URI)}`} style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', padding: '0.6rem 0.75rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', background: 'var(--bg-secondary)', color: 'var(--text-primary)', textDecoration: 'none', fontWeight: '600', fontSize: '0.85rem' }}>
                <svg width="18" height="18" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92(3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
                Google
              </a>
              <a href={`${BACKEND_URL}/oauth2/authorization/github?redirect_uri=${encodeURIComponent(REDIRECT_URI)}`} style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', padding: '0.6rem 0.75rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', background: 'var(--bg-secondary)', color: 'var(--text-primary)', textDecoration: 'none', fontWeight: '600', fontSize: '0.85rem' }}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/></svg>
                GitHub
              </a>
            </div>
          </form>
        )}
      </div>
    </dialog>
  );
};
