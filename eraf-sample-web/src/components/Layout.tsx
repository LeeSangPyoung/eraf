import React, { useState } from 'react';
import { Layout as AntLayout, Menu, theme, Typography } from 'antd';
import type { MenuProps } from 'antd';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import {
  HomeOutlined,
  FileExcelOutlined,
  FilePdfOutlined,
  BarcodeOutlined,
  PictureOutlined,
  SafetyOutlined,
  ThunderboltOutlined,
  ApartmentOutlined,
  SwapOutlined,
  ToolOutlined,
  BellOutlined,
  ClockCircleOutlined,
  DatabaseOutlined,
  DashboardOutlined,
  ApiOutlined,
} from '@ant-design/icons';

const { Sider, Content, Header } = AntLayout;
const { Title } = Typography;

type MenuItem = Required<MenuProps>['items'][number];

const menuItems: MenuItem[] = [
  {
    key: '/',
    icon: <HomeOutlined />,
    label: 'Dashboard',
  },
  {
    key: 'document',
    icon: <FileExcelOutlined />,
    label: 'Document & Media (#1~#12)',
    children: [
      { key: '/document/excel', icon: <FileExcelOutlined />, label: 'Excel (#1~#4)' },
      { key: '/document/pdf', icon: <FilePdfOutlined />, label: 'PDF (#5~#7)' },
      { key: '/document/barcode', icon: <BarcodeOutlined />, label: 'Barcode/QR (#8~#9)' },
      { key: '/document/image', icon: <PictureOutlined />, label: 'Image (#10~#12)' },
    ],
  },
  {
    key: 'security',
    icon: <SafetyOutlined />,
    label: 'Auth & Security (#13~#20)',
    children: [
      { key: '/security/jwt', icon: <SafetyOutlined />, label: 'JWT Auth (#13~#15)' },
      { key: '/security/bot', icon: <SafetyOutlined />, label: 'Bot Detection (#16)' },
      { key: '/security/session', icon: <SafetyOutlined />, label: 'Session (#17~#19)' },
      { key: '/security/audit', icon: <SafetyOutlined />, label: 'Audit Log (#20)' },
    ],
  },
  {
    key: '/resilience',
    icon: <ThunderboltOutlined />,
    label: 'Resilience (#21~#28)',
  },
  {
    key: 'state',
    icon: <ApartmentOutlined />,
    label: 'State & Saga (#29~#42)',
    children: [
      { key: '/statemachine', icon: <ApartmentOutlined />, label: 'State Machine (#29~#34)' },
      { key: '/saga', icon: <SwapOutlined />, label: 'Saga (#35~#42)' },
    ],
  },
  {
    key: '/utility',
    icon: <ToolOutlined />,
    label: 'Utilities (#43~#49)',
  },
  {
    key: '/notification',
    icon: <BellOutlined />,
    label: 'Notification & Kafka (#50~#55)',
  },
  {
    key: '/scheduler',
    icon: <ClockCircleOutlined />,
    label: 'Scheduler & Batch (#56~#59)',
  },
  {
    key: '/cache',
    icon: <DatabaseOutlined />,
    label: 'Cache & Redis (#60~#63)',
  },
  {
    key: 'monitoring',
    icon: <DashboardOutlined />,
    label: 'Monitoring & API (#64~#67)',
    children: [
      { key: '/monitoring/health', icon: <DashboardOutlined />, label: 'Health Check (#64)' },
      { key: '/monitoring/metrics', icon: <DashboardOutlined />, label: 'Metrics (#65)' },
      { key: '/monitoring/tracing', icon: <DashboardOutlined />, label: 'Tracing (#66)' },
      { key: '/monitoring/swagger', icon: <ApiOutlined />, label: 'Swagger (#67)' },
    ],
  },
];

const Layout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  const handleMenuClick: MenuProps['onClick'] = (e) => {
    navigate(e.key);
  };

  // Find selected keys and open keys based on current path
  const getSelectedKeys = () => {
    const path = location.pathname;
    return [path];
  };

  const getOpenKeys = () => {
    const path = location.pathname;
    if (path.startsWith('/document')) return ['document'];
    if (path.startsWith('/security')) return ['security'];
    if (path.startsWith('/statemachine') || path.startsWith('/saga')) return ['state'];
    if (path.startsWith('/monitoring')) return ['monitoring'];
    return [];
  };

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={(value) => setCollapsed(value)}
        width={280}
        theme="dark"
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
        }}
      >
        <div style={{ padding: '16px', textAlign: 'center' }}>
          <Title level={4} style={{ color: '#fff', margin: 0 }}>
            {collapsed ? 'ERAF' : 'ERAF Sample'}
          </Title>
          {!collapsed && (
            <div style={{ color: '#888', fontSize: '12px' }}>
              v1.0.0 | 67 Features
            </div>
          )}
        </div>
        <Menu
          theme="dark"
          selectedKeys={getSelectedKeys()}
          defaultOpenKeys={getOpenKeys()}
          mode="inline"
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <AntLayout style={{ marginLeft: collapsed ? 80 : 280, transition: 'all 0.2s' }}>
        <Header
          style={{
            padding: '0 24px',
            background: colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 1px 4px rgba(0,0,0,0.1)',
          }}
        >
          <Title level={4} style={{ margin: 0 }}>
            ERAF Framework Demo
          </Title>
          <div style={{ color: '#888' }}>
            Backend: http://localhost:8080
          </div>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
          }}
        >
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  );
};

export default Layout;
