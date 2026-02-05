import React, { useState } from 'react';
import { Card, Button, Input, Form, message, Typography, Tabs, Space, Tag, Alert, Spin } from 'antd';
import { SafetyOutlined, LoginOutlined, ReloadOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { securityApi } from '../../services/api';

const { Title, Text } = Typography;

const JwtAuthPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<any>(null);
  const [tokens, setTokens] = useState<{ accessToken: string; refreshToken: string } | null>(null);

  const handleLogin = async (values: any) => {
    setLoading(true);
    try {
      const response = await securityApi.login(values.username, values.password);
      setResult(response.data);
      if (response.data.accessToken) {
        setTokens(response.data);
        localStorage.setItem('accessToken', response.data.accessToken);
        message.success('Login successful');
      }
    } catch (error) {
      message.error('Login failed');
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = async () => {
    if (!tokens?.refreshToken) {
      message.warning('No refresh token available');
      return;
    }
    setLoading(true);
    try {
      const response = await securityApi.refresh(tokens.refreshToken);
      setResult(response.data);
      if (response.data.accessToken) {
        setTokens({ ...tokens, accessToken: response.data.accessToken });
        localStorage.setItem('accessToken', response.data.accessToken);
        message.success('Token refreshed');
      }
    } catch (error) {
      message.error('Token refresh failed');
    } finally {
      setLoading(false);
    }
  };

  const handleValidate = async () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      message.warning('No access token available');
      return;
    }
    setLoading(true);
    try {
      const response = await securityApi.validateToken(token);
      setResult(response.data);
      message.success('Token is valid');
    } catch (error) {
      message.error('Token validation failed');
    } finally {
      setLoading(false);
    }
  };

  const handleGetMenu = async (role: string) => {
    setLoading(true);
    try {
      const response = await securityApi.getRoleMenu(role);
      setResult(response.data);
    } catch (error) {
      message.error('Failed to get menu');
    } finally {
      setLoading(false);
    }
  };

  const items = [
    {
      key: '1',
      label: '#13 JWT Login',
      children: (
        <Card>
          <Title level={5}>JWT Login</Title>
          <Text type="secondary">Authenticate with username and password to receive JWT tokens.</Text>
          <Form
            layout="vertical"
            onFinish={handleLogin}
            style={{ marginTop: 16, maxWidth: 400 }}
            initialValues={{ username: 'admin', password: 'admin123' }}
          >
            <Form.Item name="username" label="Username" rules={[{ required: true }]}>
              <Input placeholder="Username" />
            </Form.Item>
            <Form.Item name="password" label="Password" rules={[{ required: true }]}>
              <Input.Password placeholder="Password" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" icon={<LoginOutlined />} loading={loading}>
                Login
              </Button>
            </Form.Item>
          </Form>
          {tokens && (
            <Alert
              type="success"
              message="Logged In"
              description={
                <div style={{ wordBreak: 'break-all' }}>
                  <Text strong>Access Token:</Text>
                  <br />
                  <Text code style={{ fontSize: 10 }}>{tokens.accessToken?.substring(0, 50)}...</Text>
                </div>
              }
            />
          )}
        </Card>
      ),
    },
    {
      key: '2',
      label: '#14 Token Refresh',
      children: (
        <Card>
          <Title level={5}>Token Refresh</Title>
          <Text type="secondary">Refresh the access token using the refresh token.</Text>
          <div style={{ marginTop: 16 }}>
            <Space>
              <Button
                type="primary"
                icon={<ReloadOutlined />}
                onClick={handleRefresh}
                loading={loading}
                disabled={!tokens?.refreshToken}
              >
                Refresh Token
              </Button>
              <Button icon={<CheckCircleOutlined />} onClick={handleValidate} loading={loading}>
                Validate Token
              </Button>
            </Space>
          </div>
        </Card>
      ),
    },
    {
      key: '3',
      label: '#15 Role-based Menu',
      children: (
        <Card>
          <Title level={5}>Role-based Menu Control</Title>
          <Text type="secondary">Get menu items based on user role.</Text>
          <div style={{ marginTop: 16 }}>
            <Space>
              <Button onClick={() => handleGetMenu('ADMIN')} loading={loading}>
                <Tag color="red">ADMIN</Tag> Menu
              </Button>
              <Button onClick={() => handleGetMenu('USER')} loading={loading}>
                <Tag color="blue">USER</Tag> Menu
              </Button>
              <Button onClick={() => handleGetMenu('GUEST')} loading={loading}>
                <Tag color="default">GUEST</Tag> Menu
              </Button>
            </Space>
          </div>
        </Card>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <SafetyOutlined /> JWT Authentication (#13~#15)
      </Title>
      <Tabs items={items} />
      {result && (
        <Card title="Result" style={{ marginTop: 16 }}>
          <pre style={{ background: '#f5f5f5', padding: 16, borderRadius: 8, overflow: 'auto' }}>
            {JSON.stringify(result, null, 2)}
          </pre>
        </Card>
      )}
    </Spin>
  );
};

export default JwtAuthPage;
