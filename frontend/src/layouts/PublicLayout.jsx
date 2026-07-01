import { useState, useEffect } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Header } from '../components/common/Header';
import { LoginModal } from '../components/auth/LoginModal';
import { PostTaskModal } from '../components/marketplace/PostTaskModal';
import { ChangePasswordModal } from '../components/auth/ChangePasswordModal';
import { useAuth } from '../hooks/useAuth';

export const PublicLayout = () => {
  const [isChangePwOpen, setIsChangePwOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  // Open Change Password modal when ?changePassword=1 is in the URL
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('changePassword') === '1' && isAuthenticated) {
      setIsChangePwOpen(true);
      // Clean the query param from the URL without reloading
      navigate(location.pathname, { replace: true });
    }
  }, [location.search, isAuthenticated, navigate, location.pathname]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Header onOpenChangePw={() => setIsChangePwOpen(true)} />
      <main style={{ flex: 1 }}>
        <Outlet />
      </main>
      {/* Global modals */}
      <LoginModal />
      <PostTaskModal />
      <ChangePasswordModal
        isOpen={isChangePwOpen}
        onClose={() => setIsChangePwOpen(false)}
      />
    </div>
  );
};
