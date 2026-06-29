import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../services/api';

export const AdminDashboard = () => {
  const { user, isAuthenticated, isLoading, openLoginModal, logout } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [activeTab, setActiveTab] = useState('overview');

  // Category name input state
  const [newCategoryName, setNewCategoryName] = useState('');
  const [categoryNameSuccess, setCategoryNameSuccess] = useState('');
  const [categoryNameError, setCategoryNameError] = useState('');

  // Service Category input state
  const [selectedParentCategory, setSelectedParentCategory] = useState('');
  const [newServiceName, setNewServiceName] = useState('');
  const [serviceSuccess, setServiceSuccess] = useState('');
  const [serviceError, setServiceError] = useState('');

  // Guard: Admin role only
  useEffect(() => {
    if (!isLoading) {
      if (!isAuthenticated) {
        navigate('/');
        openLoginModal();
      } else {
        const isAdmin = user.roles?.includes('ADMIN') || user.roles?.includes('ROLE_ADMIN');
        if (!isAdmin) {
          alert('Access denied: Admin role required.');
          navigate('/dashboard/customer');
        }
      }
    }
  }, [isLoading, isAuthenticated, user, navigate, openLoginModal]);

  // Query: Fetch all users in the system
  const { data: usersList, isLoading: isUsersLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: async () => {
      const res = await api.get('/admin/users');
      return res.data;
    },
    enabled: isAuthenticated,
  });

  // Query: Fetch all listings in the system (page 1)
  const { data: listingsPage, isLoading: isListingsLoading } = useQuery({
    queryKey: ['admin-listings'],
    queryFn: async () => {
      const res = await api.get('/admin/listings/1');
      return res.data;
    },
    enabled: isAuthenticated,
  });

  const listingsList = listingsPage?.content || [];

  // Query: Fetch all tasks in the system
  const { data: tasksList, isLoading: isTasksLoading } = useQuery({
    queryKey: ['admin-tasks'],
    queryFn: async () => {
      const res = await api.get('/admin/admin-view-tasks');
      return res.data;
    },
    enabled: isAuthenticated,
  });

  // Query: Fetch all category names
  const { data: categoryNames, isLoading: isCategoriesLoading } = useQuery({
    queryKey: ['admin-category-names'],
    queryFn: async () => {
      const res = await api.get('/admin/all-category-names');
      return res.data;
    },
    enabled: isAuthenticated,
  });

  // Mutation: Enable user
  const enableUserMutation = useMutation({
    mutationFn: async (userId) => {
      const res = await api.post(`/admin/enable-user/${userId}`);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      alert('User account enabled successfully.');
    },
    onError: (err) => {
      alert(`Action failed: ${err.response?.data?.message || err.message}`);
    }
  });

  // Mutation: Disable user
  const disableUserMutation = useMutation({
    mutationFn: async (userId) => {
      const res = await api.post(`/admin/disable-user/${userId}`);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      alert('User account suspended successfully.');
    },
    onError: (err) => {
      alert(`Action failed: ${err.response?.data?.message || err.message}`);
    }
  });

  // Mutation: Delete listing
  const deleteListingMutation = useMutation({
    mutationFn: async (listingId) => {
      const res = await api.delete(`/listing/delete-listing/${listingId}`);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-listings'] });
      alert('Listing removed successfully.');
    },
    onError: (err) => {
      alert(`Delete failed: ${err.response?.data?.message || err.message}`);
    }
  });

  // Mutation: Add Category Name
  const addCategoryNameMutation = useMutation({
    mutationFn: async (name) => {
      const res = await api.post('/admin/add-category-name', [{ categoryName: name }]);
      return res.data;
    },
    onSuccess: () => {
      setCategoryNameSuccess('Category name created successfully!');
      setNewCategoryName('');
      queryClient.invalidateQueries({ queryKey: ['admin-category-names'] });
      setTimeout(() => setCategoryNameSuccess(''), 2000);
    },
    onError: (err) => {
      setCategoryNameError(err.response?.data?.message || 'Failed to add category name.');
    }
  });

  // Mutation: Add Service Category under Parent Name
  const addServiceCategoryMutation = useMutation({
    mutationFn: async ({ parentName, serviceName }) => {
      const res = await api.post('/admin/add-service-category', {
        categoryName: parentName,
        serviceName: serviceName
      });
      return res.data;
    },
    onSuccess: () => {
      setServiceSuccess('Service mapping added successfully!');
      setNewServiceName('');
      setTimeout(() => setServiceSuccess(''), 2000);
    },
    onError: (err) => {
      setServiceError(err.response?.data?.message || 'Failed to map service.');
    }
  });

  // Mutation: Delete Category Name
  const deleteCategoryMutation = useMutation({
    mutationFn: async (name) => {
      const res = await api.delete('/admin/delete', {
        data: { categoryName: name }
      });
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-category-names'] });
      alert('Category deleted successfully.');
    },
    onError: (err) => {
      alert(`Delete failed: ${err.response?.data?.message || err.message}`);
    }
  });

  const handleAddCategoryName = (e) => {
    e.preventDefault();
    setCategoryNameError('');
    addCategoryNameMutation.mutate(newCategoryName);
  };

  const handleAddServiceCategory = (e) => {
    e.preventDefault();
    setServiceError('');
    if (!selectedParentCategory) {
      setServiceError('Please select a parent category.');
      return;
    }
    addServiceCategoryMutation.mutate({
      parentName: selectedParentCategory,
      serviceName: newServiceName
    });
  };

  const handleDeleteListing = (listingId) => {
    if (window.confirm('Are you sure you want to remove this service listing from the marketplace?')) {
      deleteListingMutation.mutate(listingId);
    }
  };

  const handleDeleteCategory = (catName) => {
    if (window.confirm(`Are you sure you want to delete category "${catName}"? This will affect listings mapping to it.`)) {
      deleteCategoryMutation.mutate(catName);
    }
  };

  if (isLoading || !user) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <p>Verifying admin authorization...</p>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '2rem 1.5rem' }}>
      
      {/* Admin Title Banner */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '2rem',
        paddingBottom: '1rem',
        borderBottom: '1px solid var(--border)'
      }}>
        <div>
          <h2 style={{ fontSize: '1.75rem', fontWeight: '800' }}>Admin Workspace</h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
            Logged in as <strong>System Admin ({user.emailAddress})</strong>
          </p>
        </div>
        <span style={{
          fontSize: '0.75rem',
          fontWeight: '700',
          backgroundColor: '#ff3b30',
          color: '#ffffff',
          padding: '0.3rem 0.75rem',
          borderRadius: '12px',
          textTransform: 'uppercase'
        }}>
          Control Panel
        </span>
      </div>

      {/* Tabs Header */}
      <div style={{
        display: 'flex',
        gap: '0.5rem',
        borderBottom: '2px solid var(--border)',
        marginBottom: '2rem',
        flexWrap: 'wrap'
      }}>
        <button
          onClick={() => setActiveTab('overview')}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'overview' ? '3px solid var(--accent)' : '3px solid transparent',
            color: activeTab === 'overview' ? 'var(--text-primary)' : 'var(--text-secondary)',
            fontWeight: '700',
            cursor: 'pointer',
            fontSize: '0.95rem'
          }}
        >
          📊 Stats Overview
        </button>
        <button
          onClick={() => setActiveTab('users')}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'users' ? '3px solid var(--accent)' : '3px solid transparent',
            color: activeTab === 'users' ? 'var(--text-primary)' : 'var(--text-secondary)',
            fontWeight: '700',
            cursor: 'pointer',
            fontSize: '0.95rem'
          }}
        >
          👥 User Accounts
        </button>
        <button
          onClick={() => setActiveTab('listings')}
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
          🛍️ Listings Manager
        </button>
        <button
          onClick={() => setActiveTab('categories')}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'categories' ? '3px solid var(--accent)' : '3px solid transparent',
            color: activeTab === 'categories' ? 'var(--text-primary)' : 'var(--text-secondary)',
            fontWeight: '700',
            cursor: 'pointer',
            fontSize: '0.95rem'
          }}
        >
          ⚙️ Service Categories
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
          Sign Out Control
        </button>
      </div>

      {/* Tab Panels */}
      <div style={{ minHeight: '400px' }}>
        
        {/* OVERVIEW STATS TAB */}
        {activeTab === 'overview' && (
          <div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
              <div style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.5rem', textAlign: 'center' }}>
                <span style={{ fontSize: '2rem' }}>👥</span>
                <h4 style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: '0.5rem 0' }}>Total Accounts</h4>
                <strong style={{ fontSize: '2rem', color: 'var(--text-primary)' }}>
                  {isUsersLoading ? '...' : usersList?.length || 0}
                </strong>
              </div>
              <div style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.5rem', textAlign: 'center' }}>
                <span style={{ fontSize: '2rem' }}>🛍️</span>
                <h4 style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: '0.5rem 0' }}>Active Listings</h4>
                <strong style={{ fontSize: '2rem', color: 'var(--accent)' }}>
                  {isListingsLoading ? '...' : listingsList?.length || 0}
                </strong>
              </div>
              <div style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.5rem', textAlign: 'center' }}>
                <span style={{ fontSize: '2rem' }}>📋</span>
                <h4 style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: '0.5rem 0' }}>Posted Tasks</h4>
                <strong style={{ fontSize: '2rem', color: 'var(--success)' }}>
                  {isTasksLoading ? '...' : tasksList?.length || 0}
                </strong>
              </div>
            </div>

            <div style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '0.75rem' }}>Platform Administration Notice</h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: 0, lineHeight: '1.5' }}>
                Welcome to the Artezans administrative workspace. Here you can configure marketplace settings, moderate user accounts (suspensions or activations), verify listing compliance, and handle system-wide category mapping. Please manage all accounts and content in compliance with the local privacy policy rules.
              </p>
            </div>
          </div>
        )}

        {/* USERS MANAGEMENT TAB */}
        {activeTab === 'users' && (
          <div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '1rem' }}>User Directory Control</h3>
            {isUsersLoading ? (
              <p>Loading platform accounts...</p>
            ) : (
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--border)', textAlign: 'left', color: 'var(--text-secondary)' }}>
                      <th style={{ padding: '0.75rem' }}>ID</th>
                      <th style={{ padding: '0.75rem' }}>Name</th>
                      <th style={{ padding: '0.75rem' }}>Email Address</th>
                      <th style={{ padding: '0.75rem' }}>Account Role</th>
                      <th style={{ padding: '0.75rem' }}>Status</th>
                      <th style={{ padding: '0.75rem', textAlign: 'right' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {usersList?.map((u) => (
                      <tr key={u.id} style={{ borderBottom: '1px solid var(--border)' }}>
                        <td style={{ padding: '0.75rem' }}>{u.id}</td>
                        <td style={{ padding: '0.75rem', fontWeight: '700' }}>{u.firstName} {u.lastName}</td>
                        <td style={{ padding: '0.75rem' }}>{u.emailAddress}</td>
                        <td style={{ padding: '0.75rem' }}>
                          <span style={{
                            backgroundColor: u.roles?.includes('ADMIN') ? '#ff3b30' :
                                            u.roles?.includes('SERVICE_PROVIDER') ? '#30b0c7' : 'var(--accent)',
                            color: '#ffffff',
                            fontSize: '0.65rem',
                            fontWeight: 'bold',
                            padding: '0.2rem 0.5rem',
                            borderRadius: '4px'
                          }}>
                            {u.roles?.join(', ')}
                          </span>
                        </td>
                        <td style={{ padding: '0.75rem' }}>
                          <span style={{
                            color: u.enabled ? 'var(--success)' : 'var(--error)',
                            fontWeight: '700'
                          }}>
                            {u.enabled ? 'Active' : 'Suspended'}
                          </span>
                        </td>
                        <td style={{ padding: '0.75rem', textAlign: 'right' }}>
                          {u.roles?.includes('ADMIN') ? (
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Admin Protected</span>
                          ) : u.enabled ? (
                            <button
                              onClick={() => {
                                if (window.confirm(`Suspend account ${u.emailAddress}?`)) {
                                  disableUserMutation.mutate(u.id);
                                }
                              }}
                              style={{
                                backgroundColor: 'transparent',
                                border: '1px solid var(--error)',
                                color: 'var(--error)',
                                padding: '0.3rem 0.75rem',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontSize: '0.75rem'
                              }}
                            >
                              Suspend Account
                            </button>
                          ) : (
                            <button
                              onClick={() => enableUserMutation.mutate(u.id)}
                              style={{
                                backgroundColor: 'var(--success)',
                                border: 'none',
                                color: '#ffffff',
                                padding: '0.3rem 0.75rem',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontSize: '0.75rem'
                              }}
                            >
                              Activate Account
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* LISTINGS MANAGEMENT TAB */}
        {activeTab === 'listings' && (
          <div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '1rem' }}>Marketplace listings</h3>
            {isListingsLoading ? (
              <p>Loading marketplace listings...</p>
            ) : listingsList.length === 0 ? (
              <p style={{ color: 'var(--text-secondary)' }}>No active listings found in the system.</p>
            ) : (
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--border)', textAlign: 'left', color: 'var(--text-secondary)' }}>
                      <th style={{ padding: '0.75rem' }}>ID</th>
                      <th style={{ padding: '0.75rem' }}>Business Name</th>
                      <th style={{ padding: '0.75rem' }}>Service Title</th>
                      <th style={{ padding: '0.75rem' }}>Category</th>
                      <th style={{ padding: '0.75rem' }}>Hourly Price</th>
                      <th style={{ padding: '0.75rem', textAlign: 'right' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {listingsList.map((l) => (
                      <tr key={l.id} style={{ borderBottom: '1px solid var(--border)' }}>
                        <td style={{ padding: '0.75rem' }}>{l.id}</td>
                        <td style={{ padding: '0.75rem', fontWeight: '700' }}>{l.businessName}</td>
                        <td style={{ padding: '0.75rem' }}>{l.serviceName}</td>
                        <td style={{ padding: '0.75rem' }}>{l.serviceCategory}</td>
                        <td style={{ padding: '0.75rem', color: 'var(--accent)', fontWeight: 'bold' }}>
                          ${l.pricing?.toFixed(2)}/hr
                        </td>
                        <td style={{ padding: '0.75rem', textAlign: 'right' }}>
                          <button
                            onClick={() => handleDeleteListing(l.id)}
                            style={{
                              backgroundColor: 'transparent',
                              border: '1px solid var(--error)',
                              color: 'var(--error)',
                              padding: '0.3rem 0.75rem',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '0.75rem'
                            }}
                          >
                            Remove Listing
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* SERVICE CATEGORIES TAB */}
        {activeTab === 'categories' && (
          <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
            
            {/* Form Column */}
            <div style={{ flex: 1, minWidth: '300px', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              
              {/* Category Name Form */}
              <div style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.5rem' }}>
                <h4 style={{ fontSize: '0.95rem', fontWeight: '700', marginBottom: '1rem' }}>Create Parent Category Name</h4>
                
                {categoryNameSuccess && <div style={{ backgroundColor: 'var(--success-bg)', color: 'var(--success)', padding: '0.5rem', borderRadius: '4px', fontSize: '0.75rem', marginBottom: '0.75rem' }}>{categoryNameSuccess}</div>}
                {categoryNameError && <div style={{ backgroundColor: 'var(--error-bg)', color: 'var(--error)', padding: '0.5rem', borderRadius: '4px', fontSize: '0.75rem', marginBottom: '0.75rem' }}>{categoryNameError}</div>}

                <form onSubmit={handleAddCategoryName} style={{ display: 'flex', gap: '0.5rem' }}>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="e.g. Gardening"
                    required
                    value={newCategoryName}
                    onChange={(e) => setNewCategoryName(e.target.value)}
                  />
                  <button type="submit" className="btn-primary" style={{ padding: '0.5rem 1rem', fontSize: '0.8rem' }}>
                    Create
                  </button>
                </form>
              </div>

              {/* Service Mapping Form */}
              <div style={{ backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.5rem' }}>
                <h4 style={{ fontSize: '0.95rem', fontWeight: '700', marginBottom: '1rem' }}>Map Service Title to Category</h4>
                
                {serviceSuccess && <div style={{ backgroundColor: 'var(--success-bg)', color: 'var(--success)', padding: '0.5rem', borderRadius: '4px', fontSize: '0.75rem', marginBottom: '0.75rem' }}>{serviceSuccess}</div>}
                {serviceError && <div style={{ backgroundColor: 'var(--error-bg)', color: 'var(--error)', padding: '0.5rem', borderRadius: '4px', fontSize: '0.75rem', marginBottom: '0.75rem' }}>{serviceError}</div>}

                <form onSubmit={handleAddServiceCategory} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  <div className="form-group" style={{ margin: 0 }}>
                    <label className="form-label" style={{ fontSize: '0.75rem' }} htmlFor="parent-cat">Select Parent Category</label>
                    <select
                      id="parent-cat"
                      className="form-control"
                      value={selectedParentCategory}
                      onChange={(e) => setSelectedParentCategory(e.target.value)}
                      required
                    >
                      <option value="">-- Choose Category --</option>
                      {categoryNames?.map((cat) => (
                        <option key={cat} value={cat}>{cat}</option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group" style={{ margin: 0 }}>
                    <label className="form-label" style={{ fontSize: '0.75rem' }} htmlFor="serv-title">Service Mapping Name</label>
                    <input
                      id="serv-title"
                      type="text"
                      className="form-control"
                      placeholder="e.g. Lawn Mowing"
                      required
                      value={newServiceName}
                      onChange={(e) => setNewServiceName(e.target.value)}
                    />
                  </div>

                  <button type="submit" className="submit-btn" style={{ padding: '0.5rem', fontSize: '0.8rem' }}>
                    Map Service Title
                  </button>
                </form>
              </div>

            </div>

            {/* List Column */}
            <div style={{ flex: 1.2, minWidth: '300px', backgroundColor: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.5rem' }}>
              <h4 style={{ fontSize: '0.95rem', fontWeight: '700', marginBottom: '1rem' }}>Active Parent Categories</h4>
              {isCategoriesLoading ? (
                <p>Loading categories...</p>
              ) : categoryNames?.length === 0 ? (
                <p style={{ color: 'var(--text-secondary)' }}>No parent categories defined yet.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {categoryNames?.map((cat) => (
                    <div
                      key={cat}
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        backgroundColor: 'var(--bg-primary)',
                        padding: '0.5rem 1rem',
                        border: '1px solid var(--border)',
                        borderRadius: '4px'
                      }}
                    >
                      <span style={{ fontWeight: '700', fontSize: '0.85rem' }}>{cat}</span>
                      <button
                        onClick={() => handleDeleteCategory(cat)}
                        style={{
                          backgroundColor: 'transparent',
                          border: 'none',
                          color: 'var(--error)',
                          cursor: 'pointer',
                          fontWeight: 'bold',
                          fontSize: '0.8rem'
                        }}
                      >
                        Delete
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>

          </div>
        )}

      </div>

    </div>
  );
};
