import { Outlet } from 'react-router-dom';
import { Header } from '../components/common/Header';
import { LoginModal } from '../components/auth/LoginModal';
import { PostTaskModal } from '../components/marketplace/PostTaskModal';

export const PublicLayout = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Header />
      <main style={{ flex: 1 }}>
        <Outlet />
      </main>
      {/* Global modals */}
      <LoginModal />
      <PostTaskModal />
    </div>
  );
};
