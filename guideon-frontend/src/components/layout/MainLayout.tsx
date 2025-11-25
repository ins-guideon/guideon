import { Layout, Menu, Avatar, Dropdown, type MenuProps } from 'antd';
import {
  QuestionCircleOutlined,
  UploadOutlined,
  SettingOutlined,
  LogoutOutlined,
  UserOutlined,
  EyeOutlined,
  FolderOutlined,
  BulbOutlined,
} from '@ant-design/icons';
import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';

const { Header, Sider, Content } = Layout;

export const MainLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuthStore();
  const [collapsed, setCollapsed] = useState(false);

  const menuItems: MenuProps['items'] = [
    {
      key: '/qa',
      icon: <QuestionCircleOutlined />,
      label: '질문하기',
      onClick: () => navigate('/qa'),
    },
    ...(user?.role === 'ADMIN'
      ? [
          {
            key: '/documents',
            icon: <UploadOutlined />,
            label: '문서 업로드',
            onClick: () => navigate('/documents'),
          },
        ]
      : []),
    {
      key: '/documents/view',
      icon: <EyeOutlined />,
      label: '문서 조회',
      onClick: () => navigate('/documents/view'),
    },
    ...(user?.role === 'ADMIN'
      ? [
          {
            key: '/documents/manage',
            icon: <FolderOutlined />,
            label: '문서 관리',
            onClick: () => navigate('/documents/manage'),
          },
        ]
      : []),
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
        collapsed={collapsed}
        onCollapse={setCollapsed}
        style={{
          background: '#001529',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }}
      >
        <div
          style={{
            height: '64px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderBottom: '1px solid rgba(255,255,255,0.1)',
            padding: '8px',
          }}
        >
          <img
            src="/logo.svg"
            alt="완나생"
            style={{
              height: '160px',
              width: 'auto',
              maxWidth: '100%',
              objectFit: 'contain',
            }}
          />
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          style={{ borderRight: 0, flex: 1, overflowY: 'auto', paddingBottom: '80px' }}
        />
        <div
          style={{
            position: 'absolute',
            bottom: 0,
            left: 0,
            right: 0,
            padding: collapsed ? '16px' : '16px',
            borderTop: '1px solid rgba(255,255,255,0.1)',
            display: 'flex',
            alignItems: 'flex-start',
            justifyContent: collapsed ? 'center' : 'flex-start',
            gap: '8px',
            color: 'rgba(255,255,255,0.65)',
            background: '#001529',
          }}
        >
          <BulbOutlined style={{ color: '#faad14', fontSize: 16, marginTop: 2, flexShrink: 0 }} />
          {!collapsed && (
            <span style={{ fontSize: 12, lineHeight: 1.6 }}>
              팁: 이 챗봇은 사내 규정, 복지, 업무 프로세스에 대해 답변합니다.
            </span>
          )}
        </div>
      </Sider>
      <Layout style={{ marginLeft: collapsed ? 80 : 240, transition: 'margin-left 0.2s' }}>
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
