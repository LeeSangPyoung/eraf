import React, { useState, useEffect } from 'react';
import { Card, Button, Input, Table, message, Typography, Space, Tag, Spin, Select } from 'antd';
import { AuditOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { securityApi } from '../../services/api';

const { Title, Text } = Typography;

const AuditLogPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [userId, setUserId] = useState('');
  const [logs, setLogs] = useState<any[]>([]);

  const handleLoadLogs = async () => {
    setLoading(true);
    try {
      const response = await securityApi.getAuditLogs(userId || undefined);
      setLogs(response.data.logs || []);
    } catch (error) {
      message.error('Failed to load audit logs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    handleLoadLogs();
  }, []);

  const columns = [
    {
      title: 'Timestamp',
      dataIndex: 'timestampFormatted',
      key: 'timestamp',
      width: 180,
    },
    {
      title: 'User ID',
      dataIndex: 'userId',
      key: 'userId',
      width: 120,
    },
    {
      title: 'Action',
      dataIndex: 'action',
      key: 'action',
      width: 150,
      render: (action: string) => {
        const colors: Record<string, string> = {
          LOGIN: 'green',
          LOGOUT: 'blue',
          LOGIN_FAILED: 'red',
          TOKEN_REFRESH: 'cyan',
          SESSION_CREATE: 'purple',
          SESSION_TERMINATE: 'orange',
        };
        return <Tag color={colors[action] || 'default'}>{action}</Tag>;
      },
    },
    {
      title: 'IP Address',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 130,
    },
    {
      title: 'User Agent',
      dataIndex: 'userAgent',
      key: 'userAgent',
      ellipsis: true,
    },
    {
      title: 'Details',
      dataIndex: 'details',
      key: 'details',
      ellipsis: true,
      render: (details: any) => details ? JSON.stringify(details) : '-',
    },
    {
      title: 'Success',
      dataIndex: 'success',
      key: 'success',
      width: 80,
      render: (success: boolean) => (
        <Tag color={success ? 'green' : 'red'}>{success ? 'Yes' : 'No'}</Tag>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <AuditOutlined /> Audit Log (#20)
      </Title>

      <Card>
        <Title level={5}>Security Audit Logs</Title>
        <Text type="secondary">View security-related events and user activities.</Text>

        <Space style={{ margin: '16px 0' }}>
          <Input
            placeholder="Filter by User ID"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            style={{ width: 200 }}
            prefix={<SearchOutlined />}
          />
          <Button type="primary" icon={<ReloadOutlined />} onClick={handleLoadLogs}>
            Refresh
          </Button>
        </Space>

        <Table
          columns={columns}
          dataSource={logs}
          rowKey={(record) => `${record.timestampFormatted}-${record.userId}-${record.action}`}
          pagination={{ pageSize: 10 }}
          size="small"
          scroll={{ x: 1000 }}
        />
      </Card>
    </Spin>
  );
};

export default AuditLogPage;
