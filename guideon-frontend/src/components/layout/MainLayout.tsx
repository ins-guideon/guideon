import { Layout, Menu, Avatar, Dropdown, type MenuProps } from 'antd';
import {
  HomeOutlined,
  QuestionCircleOutlined,
  FileTextOutlined,
  UploadOutlined,
  HistoryOutlined,
  BarChartOutlined,
  SettingOutlined,
  LogoutOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';

const { Header, Sider, Content } = Layout;

export const MainLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuthStore();

  const menuItems: MenuProps['items'] = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: '대시보드',
      onClick: () => navigate('/'),
    },
    {
      key: '/qa',
      icon: <QuestionCircleOutlined />,
      label: '질문하기',
      onClick: () => navigate('/qa'),
    },
    {
      key: '/documents',
      icon: <UploadOutlined />,
      label: '문서 업로드',
      onClick: () => navigate('/documents'),
    },
    {
      key: '/regulations',
      icon: <FileTextOutlined />,
      label: '규정 관리',
      onClick: () => navigate('/regulations'),
    },
    {
      key: '/history',
      icon: <HistoryOutlined />,
      label: '검색 이력',
      onClick: () => navigate('/history'),
    },
    {
      key: '/analytics',
      icon: <BarChartOutlined />,
      label: '통계',
      onClick: () => navigate('/analytics'),
    },
    {
      key: '/settings',
      icon: <SettingOutlined />,
      label: '설정',
      onClick: () => navigate('/settings'),
    },
  ];

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '프로필',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '로그아웃',
      onClick: async () => {
        await logout();
        navigate('/login');
      },
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        width={240}
        breakpoint="lg"
        collapsedWidth="80"
        style={{
          background: '#001529',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
          overflow: 'auto',
        }}
      >
        <div
          style={{
            height: '64px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white',
            fontSize: '22px',
            fontWeight: 'bold',
            borderBottom: '1px solid rgba(255,255,255,0.1)',
          }}
        >
          Guideon
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          style={{ borderRight: 0 }}
        />
      </Sider>
      <Layout style={{ marginLeft: 240 }}>
        <Header
          style={{
            padding: '0 48px',
            background: '#fff',
            display: 'flex',
            justifyContent: 'flex-end',
            alignItems: 'center',
            boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
            position: 'sticky',
            top: 0,
            zIndex: 10,
          }}
        >
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <div style={{ display: 'flex', alignItems: 'center', cursor: 'pointer', gap: 12 }}>
              <Avatar icon={<UserOutlined />} size="default" />
              <span style={{ fontSize: '14px', fontWeight: 500 }}>{user?.username || 'Guest'}</span>
            </div>
          </Dropdown>
        </Header>
        <Content
          style={{
            margin: '32px 48px',
            padding: 0,
            minHeight: 'calc(100vh - 128px)',
          }}
        >
          <div style={{
            maxWidth: 1400,
            margin: '0 auto',
            background: '#fff',
            padding: '32px',
            borderRadius: '8px',
            boxShadow: '0 1px 2px rgba(0,0,0,0.03)',
          }}>
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};
