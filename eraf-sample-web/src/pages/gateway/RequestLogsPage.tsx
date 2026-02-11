import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Space, Tag, Statistic, Row, Col, message, Modal } from 'antd';
import { ReloadOutlined, DeleteOutlined, HistoryOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { gateway } from '../../services/api';
import dayjs from 'dayjs';

interface RequestLog {
  timestamp: string;
  method: string;
  path: string;
  queryParams: string;
  clientIp: string;
  statusCode: number;
  duration: number;
  userAgent: string;
}

interface Stats {
  totalRequests: number;
  successRate: number;
  avgDuration: number;
  requestsByMethod: Record<string, number>;
  requestsByStatus: Record<string, number>;
}

const RequestLogsPage: React.FC = () => {
  const [logs, setLogs] = useState<RequestLog[]>([]);
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadLogs();
    loadStats();
  }, []);

  const loadLogs = async () => {
    setLoading(true);
    try {
      const response = await gateway.getRequestLogs(100);
      setLogs(response.data);
    } catch (error) {
      message.error('Failed to load request logs');
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const response = await gateway.getRequestLogsStats();
      setStats(response.data);
    } catch (error) {
      console.error('Failed to load stats');
    }
  };

  const handleClear = () => {
    Modal.confirm({
      title: 'Clear Request Logs',
      content: 'Are you sure you want to clear all request logs?',
      okText: 'Clear',
      okType: 'danger',
      onOk: async () => {
        try {
          await gateway.clearRequestLogs();
          message.success('Request logs cleared');
          loadLogs();
          loadStats();
        } catch (error) {
          message.error('Failed to clear logs');
        }
      },
    });
  };

  const getStatusColor = (status: number) => {
    if (status >= 200 && status < 300) return 'success';
    if (status >= 300 && status < 400) return 'processing';
    if (status >= 400 && status < 500) return 'warning';
    return 'error';
  };

  const getMethodColor = (method: string) => {
    const colors: Record<string, string> = {
      GET: 'blue',
      POST: 'green',
      PUT: 'orange',
      DELETE: 'red',
      PATCH: 'purple',
    };
    return colors[method] || 'default';
  };

  const columns = [
    {
      title: 'Timestamp',
      dataIndex: 'timestamp',
      key: 'timestamp',
      width: 180,
      render: (timestamp: string) => dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: 'Method',
      dataIndex: 'method',
      key: 'method',
      width: 100,
      render: (method: string) => <Tag color={getMethodColor(method)}>{method}</Tag>,
    },
    {
      title: 'Path',
      dataIndex: 'path',
      key: 'path',
      ellipsis: true,
    },
    {
      title: 'Client IP',
      dataIndex: 'clientIp',
      key: 'clientIp',
      width: 150,
    },
    {
      title: 'Status',
      dataIndex: 'statusCode',
      key: 'statusCode',
      width: 100,
      align: 'center' as const,
      render: (status: number) => (
        <Tag color={getStatusColor(status)}>{status}</Tag>
      ),
    },
    {
      title: 'Duration',
      dataIndex: 'duration',
      key: 'duration',
      width: 120,
      align: 'right' as const,
      render: (duration: number) => `${duration}ms`,
      sorter: (a: RequestLog, b: RequestLog) => a.duration - b.duration,
    },
  ];

  return (
    <div>
      {stats && (
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col span={6}>
            <Card>
              <Statistic
                title="Total Requests"
                value={stats.totalRequests}
                prefix={<HistoryOutlined />}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="Success Rate"
                value={stats.successRate}
                precision={1}
                suffix="%"
                prefix={<CheckCircleOutlined />}
                valueStyle={{ color: stats.successRate >= 95 ? '#3f8600' : '#cf1322' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="Avg Duration"
                value={stats.avgDuration}
                precision={0}
                suffix="ms"
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <div style={{ fontSize: 14, color: '#8c8c8c', marginBottom: 8 }}>By Method</div>
              <Space size="small" wrap>
                {Object.entries(stats.requestsByMethod).map(([method, count]) => (
                  <Tag key={method} color={getMethodColor(method)}>
                    {method}: {count}
                  </Tag>
                ))}
              </Space>
            </Card>
          </Col>
        </Row>
      )}

      <Card
        title={
          <Space>
            <HistoryOutlined />
            Request Logs
            <span style={{ fontSize: 14, color: '#8c8c8c' }}>
              (Last {logs.length} requests)
            </span>
          </Space>
        }
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => { loadLogs(); loadStats(); }}>
              Refresh
            </Button>
            <Button danger icon={<DeleteOutlined />} onClick={handleClear}>
              Clear Logs
            </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={logs}
          rowKey={(record, index) => `${record.timestamp}-${record.method}-${record.path}-${index}`}
          loading={loading}
          pagination={{ pageSize: 20 }}
          scroll={{ x: 1000 }}
        />
      </Card>
    </div>
  );
};

export default RequestLogsPage;
