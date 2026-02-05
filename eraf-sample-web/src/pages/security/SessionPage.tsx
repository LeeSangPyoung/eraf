import React, { useState } from 'react';
import { Card, Button, Input, Table, message, Typography, Space, Tag, Alert, Spin, Popconfirm } from 'antd';
import { UserOutlined, DeleteOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { securityApi } from '../../services/api';

const { Title, Text } = Typography;

const SessionPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [userId, setUserId] = useState('user-001');
  const [sessions, setSessions] = useState<any[]>([]);
  const [result, setResult] = useState<any>(null);

  const handleCreateSession = async () => {
    setLoading(true);
    try {
      const response = await securityApi.createSession(userId);
      setResult(response.data);
      message.success('Session created');
      handleLoadSessions();
    } catch (error: any) {
      if (error.response?.data?.message) {
        message.error(error.response.data.message);
      } else {
        message.error('Failed to create session');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleLoadSessions = async () => {
    setLoading(true);
    try {
      const response = await securityApi.getSessions(userId);
      setSessions(response.data.sessions || []);
    } catch (error) {
      message.error('Failed to load sessions');
    } finally {
      setLoading(false);
    }
  };

  const handleTerminate = async (sessionId: string) => {
    setLoading(true);
    try {
      await securityApi.terminateSession(sessionId);
      message.success('Session terminated');
      handleLoadSessions();
    } catch (error) {
      message.error('Failed to terminate session');
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { title: 'Session ID', dataIndex: 'sessionId', key: 'sessionId', render: (text: string) => <Text code>{text}</Text> },
    { title: 'User ID', dataIndex: 'userId', key: 'userId' },
    { title: 'IP Address', dataIndex: 'ipAddress', key: 'ipAddress' },
    { title: 'Created At', dataIndex: 'createdAtFormatted', key: 'createdAt' },
    { title: 'Expires At', dataIndex: 'expiresAtFormatted', key: 'expiresAt' },
    {
      title: 'Status',
      dataIndex: 'active',
      key: 'active',
      render: (active: boolean) => (
        <Tag color={active ? 'green' : 'default'}>{active ? 'Active' : 'Expired'}</Tag>
      ),
    },
    {
      title: 'Action',
      key: 'action',
      render: (_: any, record: any) => (
        <Popconfirm title="Terminate this session?" onConfirm={() => handleTerminate(record.sessionId)}>
          <Button danger size="small" icon={<DeleteOutlined />}>
            Terminate
          </Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <UserOutlined /> Session Management (#17~#19)
      </Title>

      <Card>
        <Title level={5}>Session Management</Title>
        <Text type="secondary">Create, view, and manage user sessions with concurrent session limit.</Text>

        <Alert
          type="info"
          message="Concurrent Session Limit"
          description="Maximum 2 sessions per user. Creating a new session when limit is reached will fail."
          showIcon
          style={{ marginTop: 16, marginBottom: 16 }}
        />

        <Space style={{ marginBottom: 16 }}>
          <Text>User ID:</Text>
          <Input value={userId} onChange={(e) => setUserId(e.target.value)} style={{ width: 200 }} />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateSession}>
            Create Session
          </Button>
          <Button icon={<ReloadOutlined />} onClick={handleLoadSessions}>
            Load Sessions
          </Button>
        </Space>

        <Table
          columns={columns}
          dataSource={sessions}
          rowKey="sessionId"
          pagination={false}
          size="small"
        />

        {result && (
          <Card title="Last Result" style={{ marginTop: 16 }}>
            <pre style={{ background: '#f5f5f5', padding: 16, borderRadius: 8, overflow: 'auto' }}>
              {JSON.stringify(result, null, 2)}
            </pre>
          </Card>
        )}
      </Card>
    </Spin>
  );
};

export default SessionPage;
