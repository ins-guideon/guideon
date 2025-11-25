import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ConfigProvider } from 'antd';
import koKR from 'antd/locale/ko_KR';
import { Toaster } from 'react-hot-toast';
import { useAuthStore } from './stores/authStore';
import { PrivateRoute } from './components/common/PrivateRoute';
import { MainLayout } from './components/layout/MainLayout';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { QAPage } from './pages/QAPage';
import { DocumentUpload } from './pages/DocumentUpload';
import { DocumentView } from './pages/DocumentView';
import { DocumentManagement } from './pages/DocumentManagement';
import { Settings } from './pages/Settings';

// React Query 클라이언트 생성
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 5 * 60 * 1000, // 5분
    },
  },
});

function App() {
  const { loadUser } = useAuthStore();

  // 앱 시작시 사용자 정보 로드
  useEffect(() => {
    loadUser();
  }, [loadUser]);

  return (
    <QueryClientProvider client={queryClient}>
      <ConfigProvider locale={koKR}>
        <BrowserRouter>
          <Routes>
            {/* 로그인 페이지 */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            {/* 보호된 라우트 */}
            <Route
              element={
                <PrivateRoute>
                  <MainLayout />
                </PrivateRoute>
              }
            >
              <Route path="/qa" element={<QAPage />} />
              <Route
                path="/documents"
                element={
                  <PrivateRoute roles={['ADMIN']}>
                    <DocumentUpload />
                  </PrivateRoute>
                }
              />
              <Route path="/documents/view" element={<DocumentView />} />
              <Route
                path="/documents/manage"
                element={
                  <PrivateRoute roles={['ADMIN']}>
                    <DocumentManagement />
                  </PrivateRoute>
                }
              />
              <Route path="/settings" element={<Settings />} />
            </Route>

            {/* 404 처리 및 기본 라우트 */}
            <Route path="/" element={<Navigate to="/qa" replace />} />
            <Route path="*" element={<Navigate to="/qa" replace />} />
          </Routes>
        </BrowserRouter>
        <Toaster
          position="bottom-right"
          toastOptions={{
            duration: 3000,
            style: {
              background: '#fff',
              color: '#333',
            },
            success: {
              duration: 3000,
              iconTheme: {
                primary: '#52c41a',
                secondary: '#fff',
              },
            },
            error: {
              duration: 4000,
              iconTheme: {
                primary: '#ff4d4f',
                secondary: '#fff',
              },
            },
          }}
        />
      </ConfigProvider>
    </QueryClientProvider>
  );
}

export default App;
