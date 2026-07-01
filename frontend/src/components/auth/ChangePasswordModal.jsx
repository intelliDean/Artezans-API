import { useState, useEffect, useRef } from 'react';
import api from '../../services/api';

/**
 * ChangePasswordModal
 *
 * Wires to the backend:
 *   POST /api/v1/change-password/init  { oldPassword, newPassword }
 * The backend sends an email verification link; the user clicks it to confirm.
 */
export const ChangePasswordModal = ({ isOpen, onClose }) => {
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const dialogRef = useRef(null);

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;
    if (isOpen) {
      if (!dialog.open) dialog.showModal();
      setOldPassword(''); setNewPassword(''); setConfirmPassword('');
      setError(''); setSuccess('');
    } else {
      if (dialog.open) dialog.close();
    }
  }, [isOpen]);

  const handleBackdropClick = (e) => {
    if (e.target === dialogRef.current) onClose();
  };

  const handleCancel = (e) => {
    e.preventDefault();
    onClose();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');

    if (newPassword !== confirmPassword) {
      setError('New passwords do not match.');
      return;
    }
    if (newPassword.length < 6) {
      setError('New password must be at least 6 characters.');
      return;
    }

    setIsSubmitting(true);
    try {
      await api.post('/change-password/init', { oldPassword, newPassword });
      setSuccess('✅ A verification link has been sent to your email. Click it to confirm your new password.');
      setOldPassword(''); setNewPassword(''); setConfirmPassword('');
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'Password change failed. Please check your current password.';
      setError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <dialog ref={dialogRef} onClick={handleBackdropClick} onCancel={handleCancel} aria-labelledby="change-pw-title">
      <div className="modal-body">
        <button className="modal-close-btn" onClick={onClose} aria-label="Close">
          &times;
        </button>

        <h3 id="change-pw-title" style={{ marginBottom: '1.5rem', fontWeight: '800' }}>
          🔐 Change Password
        </h3>

        {error && (
          <div style={{
            background: 'var(--error-bg)', color: 'var(--error)',
            padding: '0.75rem 1rem', borderRadius: 'var(--radius-md)',
            fontSize: '0.85rem', fontWeight: '600', marginBottom: '1rem',
            border: '1px solid rgba(255,69,58,0.2)'
          }}>
            {error}
          </div>
        )}
        {success && (
          <div style={{
            background: 'var(--success-bg)', color: 'var(--success)',
            padding: '0.75rem 1rem', borderRadius: 'var(--radius-md)',
            fontSize: '0.85rem', fontWeight: '600', marginBottom: '1rem',
            border: '1px solid rgba(52,199,89,0.25)'
          }}>
            {success}
          </div>
        )}

        {!success && (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label" htmlFor="old-password">Current Password</label>
              <input
                id="old-password"
                type="password"
                className="form-control"
                required
                placeholder="Your current password"
                value={oldPassword}
                onChange={e => setOldPassword(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="new-password">New Password</label>
              <input
                id="new-password"
                type="password"
                className="form-control"
                required
                minLength={6}
                placeholder="At least 6 characters"
                value={newPassword}
                onChange={e => setNewPassword(e.target.value)}
              />
            </div>

            <div className="form-group" style={{ marginBottom: '2rem' }}>
              <label className="form-label" htmlFor="confirm-password">Confirm New Password</label>
              <input
                id="confirm-password"
                type="password"
                className="form-control"
                required
                minLength={6}
                placeholder="Repeat new password"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
              />
            </div>

            <button
              type="submit"
              className="submit-btn"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Sending verification...' : 'Change Password'}
            </button>

            <p style={{ marginTop: '1rem', fontSize: '0.78rem', color: 'var(--text-secondary)', textAlign: 'center', lineHeight: 1.5 }}>
              You'll receive a confirmation email. Click the link inside to complete the change.
            </p>
          </form>
        )}

        {success && (
          <button className="submit-btn" onClick={onClose} style={{ marginTop: '0.5rem' }}>
            Close
          </button>
        )}
      </div>
    </dialog>
  );
};
