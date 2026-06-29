import { Outlet } from 'react-router-dom';
import { Header } from '../components/common/Header';
import { LoginModal } from '../components/auth/LoginModal';

export const PublicLayout = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Header />
      <main style={{ flex: 1 }}>
        <Outlet />
      </main>
      {/* Global quick-auth modal */}
      <LoginModal />
    </div>
  );
};
