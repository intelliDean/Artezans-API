import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { useQueryClient } from '@tanstack/react-query';
import api from '../../services/api';

export const PostTaskModal = () => {
  const { isPostTaskModalOpen, closePostTaskModal } = useAuth();
  const queryClient = useQueryClient();

  // Form states
  const [taskServiceName, setTaskServiceName] = useState('');
  const [taskDescription, setTaskDescription] = useState('');
  const [userAddress, setUserAddress] = useState('');
  const [customerBudget, setCustomerBudget] = useState('');
  const [taskDate, setTaskDate] = useState('');
  const [taskImageFile, setTaskImageFile] = useState(null);

  const [errorMsg, setErrorMsg] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  const dialogRef = useRef(null);

  // Sync dialog visibility
  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;

    if (isPostTaskModalOpen) {
      if (!dialog.open) {
        dialog.showModal();
        setErrorMsg('');
        setSuccessMsg('');
        // Reset form
        setTaskServiceName('');
        setTaskDescription('');
        setUserAddress('');
        setCustomerBudget('');
        setTaskDate('');
        setTaskImageFile(null);
      }
    } else {
      if (dialog.open) {
        dialog.close();
      }
    }
  }, [isPostTaskModalOpen]);

  const handleBackdropClick = (e) => {
    if (e.target === dialogRef.current) {
      closePostTaskModal();
    }
  };

  const handleCancel = (e) => {
    e.preventDefault();
    closePostTaskModal();
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setTaskImageFile(e.target.files[0]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');

    // Backend requires an image file. Validate upfront.
    if (!taskImageFile) {
      setErrorMsg('Please select a photo describing the task (required).');
      return;
    }

    setIsSubmitting(true);

    // Construct multipart form-data payload
    const formData = new FormData();
    formData.append('taskServiceName', taskServiceName);
    formData.append('taskDescription', taskDescription);
    formData.append('userAddress', userAddress);
    formData.append('customerBudget', customerBudget);
    formData.append('taskDate', taskDate); // Spring binds this automatically to Set<LocalDate>
    formData.append('taskImage', taskImageFile);

    try {
      const response = await api.post('/task/post', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      setSuccessMsg('Task posted successfully!');
      
      // Invalidate active task queries to reload homepage feeds
      queryClient.invalidateQueries({ queryKey: ['active-tasks'] });
      
      // Auto close after 1.5 seconds
      setTimeout(() => {
        closePostTaskModal();
      }, 1500);
    } catch (error) {
      console.error('Failed to post task:', error);
      const msg = error.response?.data?.message || 'Error occurred while posting task. Please try again.';
      setErrorMsg(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <dialog
      ref={dialogRef}
      onClick={handleBackdropClick}
      onCancel={handleCancel}
      aria-labelledby="post-task-title"
    >
      <div className="modal-body">
        <button 
          className="modal-close-btn" 
          onClick={closePostTaskModal}
          aria-label="Close modal"
        >
          &times;
        </button>

        <h3 id="post-task-title" className="text-center" style={{ marginBottom: '1.5rem', fontWeight: '700' }}>
          Post a Custom Task
        </h3>

        {successMsg && (
          <div style={{
            backgroundColor: 'var(--success-bg)',
            color: 'var(--success)',
            padding: '0.75rem 1rem',
            borderRadius: 'var(--radius-md)',
            fontSize: '0.85rem',
            fontWeight: '600',
            marginBottom: '1.25rem',
            border: '1px solid rgba(48, 209, 88, 0.2)'
          }}>
            {successMsg}
          </div>
        )}

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

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="task-service-name">Service Title</label>
            <input
              id="task-service-name"
              type="text"
              className="form-control"
              required
              minLength={3}
              placeholder="e.g. Clean 3 Bedroom Apartment"
              value={taskServiceName}
              onChange={(e) => setTaskServiceName(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="task-desc">Describe What You Need Done</label>
            <textarea
              id="task-desc"
              className="form-control"
              required
              rows={3}
              placeholder="Provide a brief description of the job..."
              value={taskDescription}
              onChange={(e) => setTaskDescription(e.target.value)}
              style={{ resize: 'vertical', width: '100%', minHeight: '80px' }}
            />
          </div>

          <div style={{ display: 'flex', gap: '1rem' }}>
            <div className="form-group" style={{ flex: 1 }}>
              <label className="form-label" htmlFor="task-budget">Estimated Budget (AUD)</label>
              <input
                id="task-budget"
                type="number"
                className="form-control"
                required
                min={10}
                placeholder="150"
                value={customerBudget}
                onChange={(e) => setCustomerBudget(e.target.value)}
              />
            </div>
            
            <div className="form-group" style={{ flex: 1 }}>
              <label className="form-label" htmlFor="task-date">Preferred Date</label>
              <input
                id="task-date"
                type="date"
                className="form-control"
                required
                value={taskDate}
                onChange={(e) => setTaskDate(e.target.value)}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="task-address">Location Address</label>
            <input
              id="task-address"
              type="text"
              className="form-control"
              placeholder="e.g. 100 George St, Sydney NSW (Leave empty to use home address)"
              value={userAddress}
              onChange={(e) => setUserAddress(e.target.value)}
            />
          </div>

          <div className="form-group" style={{ marginBottom: '2rem' }}>
            <label className="form-label" htmlFor="task-file">Upload Task Photo (Required)</label>
            <input
              id="task-file"
              type="file"
              accept="image/*"
              className="form-control"
              required
              onChange={handleFileChange}
              style={{ padding: '0.5rem' }}
            />
            <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
              Upload an image showing the work area (required by backend multimedia service).
            </span>
          </div>

          <button 
            type="submit" 
            className="submit-btn"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Posting Task...' : 'Submit Task'}
          </button>
        </form>
      </div>
    </dialog>
  );
};
