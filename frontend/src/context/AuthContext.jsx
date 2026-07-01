import { createContext, useState, useEffect, useCallback } from 'react';
import api, { setAccessToken, registerLogoutCallback } from '../services/api';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const [isPostTaskModalOpen, setIsPostTaskModalOpen] = useState(false);

  // Define logout first so it can be referenced in callbacks
  const logout = useCallback(async () => {
    try {
      // Notify backend to revoke tokens
      await api.post('/auth/logout', {});
    } catch (e) {
      console.warn('Backend logout failed or token already revoked', e);
    } finally {
      // Clear client session regardless of server status
      setAccessToken(null);
      localStorage.removeItem('artezans_refresh');
      setUser(null);
      setIsAuthenticated(false);
    }
  }, []);

  // Perform initial session restore on mount
  useEffect(() => {
    // Register the API logout callback so Axios interceptor triggers it on invalid tokens
    registerLogoutCallback(logout);

    const restoreSession = async () => {
      // Check for OAuth2 tokens in redirect URL parameters
      const params = new URLSearchParams(window.location.search);
      const urlToken = params.get('token');
      const urlRefreshToken = params.get('refreshToken');

      if (urlToken && urlRefreshToken) {
        try {
          setAccessToken(urlToken);
          localStorage.setItem('artezans_refresh', urlRefreshToken);

          // Retrieve user profile
          const profileResponse = await api.get('/user/me');
          setUser(profileResponse.data.data);
          setIsAuthenticated(true);

          // Clean parameters from address bar
          const cleanUrl = window.location.pathname;
          window.history.replaceState({}, document.title, cleanUrl);
          setIsLoading(false);
          return;
        } catch (error) {
          console.error('OAuth2 token restore failed:', error);
          localStorage.removeItem('artezans_refresh');
          setAccessToken(null);
        }
      }

      const refreshToken = localStorage.getItem('artezans_refresh');
      if (refreshToken) {
        try {
          // Backend expects the refresh token in Authorization header
          const response = await api.post('/auth/refresh', {}, {
            headers: {
              'Authorization': `Bearer ${refreshToken}`
            }
          });
          const { accessToken, refreshToken: newRefreshToken, user: userProfile } = response.data;
          
          setAccessToken(accessToken);
          localStorage.setItem('artezans_refresh', newRefreshToken);
          
          // If the refresh endpoint does not return user details directly,
          // retrieve user details using a profile endpoint
          if (userProfile) {
            setUser(userProfile);
          } else {
            // Fallback: Fetch profile details (e.g. from /user/me or similar if available)
            const profileResponse = await api.get('/user/me');
            setUser(profileResponse.data.data);
          }
          setIsAuthenticated(true);
        } catch (error) {
          console.error('Session restore failed:', error);
          localStorage.removeItem('artezans_refresh');
        }
      }
      setIsLoading(false);
    };

    restoreSession();
  }, [logout]);

  // Login handler
  const login = async (emailAddress, password) => {
    try {
      const response = await api.post('/auth/login', {
        emailAddress,
        password
      });

      const { accessToken, refreshToken, user: userProfile } = response.data;

      if (!accessToken || !refreshToken) {
        throw new Error(response.data.message || 'Authentication failed');
      }

      setAccessToken(accessToken);
      localStorage.setItem('artezans_refresh', refreshToken);
      setUser(userProfile);
      setIsAuthenticated(true);
      setIsLoginModalOpen(false); // Close login modal on success

      return { success: true };
    } catch (error) {
      const message = error.response?.data?.message || error.message || 'Invalid email or password';
      return { success: false, error: message };
    }
  };

  const openLoginModal = () => setIsLoginModalOpen(true);
  const closeLoginModal = () => setIsLoginModalOpen(false);

  const openPostTaskModal = () => setIsPostTaskModalOpen(true);
  const closePostTaskModal = () => setIsPostTaskModalOpen(false);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated,
        isLoading,
        isLoginModalOpen,
        isPostTaskModalOpen,
        login,
        logout,
        openLoginModal,
        closeLoginModal,
        openPostTaskModal,
        closePostTaskModal
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
