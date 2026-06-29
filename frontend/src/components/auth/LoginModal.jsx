import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../hooks/useAuth';

export const LoginModal = () => {
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
          </form>
        )}
      </div>
    </dialog>
  );
};
