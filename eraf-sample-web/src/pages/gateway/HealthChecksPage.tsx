import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  message,
  Row,
  Col,
  Statistic,
  Select,
  Tooltip,
  Badge,
} from 'antd';
import {
  HeartOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  QuestionCircleOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import { gateway } from '../../services/api';
import dayjs from 'dayjs';

interface HealthCheckResult {
  targetId: number;
  serviceName: string;
  host: string;
  port: number;
  healthStatus: string;
  consecutiveFailures: number;
  lastHealthCheckAt: string | null;
  responseTimeMs: number | null;
  errorMessage: string | null;
  enabled: boolean;
}

interface HealthCheckStats {
  totalTargets: number;
  healthyTargets: number;
  unhealthyTargets: number;
  unknownTargets: number;
  healthyPercentage: number;
}

const HealthChecksPage: React.FC = () => {
  const [healthChecks, setHealthChecks] = useState<HealthCheckResult[]>([]);
  const [stats, setStats] = useState<HealthCheckStats | null>(null);
  const [loading, setLoading] = useState(false);
  const [selectedService, setSelectedService] = useState<number | null>(null);
  const [servicesList, setServicesList] = useState<{ id: number; name: string }[]>([]);

  useEffect(() => {
    const loadServices = async () => {
      try {
        const response = await gateway.getServices();
        setServicesList(response.data);
      } catch (error) {
        console.error('Failed to load services:', error);
      }
    };
    loadServices();
  }, []);

  useEffect(() => {
    loadHealthChecks();
    loadStats();

    // Auto-refresh every 30 seconds
    const interval = setInterval(() => {
      loadHealthChecks();
      loadStats();
    }, 30000);

    return () => clearInterval(interval);
  }, [selectedService]);

  const loadHealthChecks = async () => {
    setLoading(true);
    try {
      const response = selectedService
        ? await gateway.getHealthChecksByService(selectedService)
        : await gateway.getHealthChecks();
      setHealthChecks(response.data);
    } catch (error) {
      message.error('Failed to load health checks');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const response = await gateway.getHealthCheckStats();
      setStats(response.data);
    } catch (error) {
      console.error('Failed to load stats:', error);
    }
  };

  const handlePerformHealthCheck = async (targetId: number) => {
    try {
      message.loading({ content: 'Performing health check...', key: 'healthcheck' });
      await gateway.performHealthCheck(targetId);
      message.success({ content: 'Health check completed', key: 'healthcheck' });
      loadHealthChecks();
      loadStats();
    } catch (error) {
      message.error({ content: 'Failed to perform health check', key: 'healthcheck' });
      console.error(error);
    }
  };

  const getHealthStatusColor = (status: string): string => {
    switch (status?.toUpperCase()) {
      case 'HEALTHY':
        return 'success';
      case 'UNHEALTHY':
        return 'error';
      default:
        return 'default';
    }
  };

  const getHealthStatusIcon = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'HEALTHY':
        return <CheckCircleOutlined />;
      case 'UNHEALTHY':
        return <CloseCircleOutlined />;
      default:
        return <QuestionCircleOutlined />;
    }
  };

  const columns = [
    {
      title: 'Target ID',
      dataIndex: 'targetId',
      key: 'targetId',
      width: 100,
    },
    {
      title: 'Service',
      dataIndex: 'serviceName',
      key: 'serviceName',
      width: 180,
    },
    {
      title: 'Target',
      key: 'target',
      width: 200,
      render: (_: any, record: HealthCheckResult) => (
        <span>
          {record.host}:{record.port}
        </span>
      ),
    },
    {
      title: 'Health Status',
      dataIndex: 'healthStatus',
      key: 'healthStatus',
      width: 150,
      align: 'center' as const,
      render: (status: string) => (
        <Tag icon={getHealthStatusIcon(status)} color={getHealthStatusColor(status)}>
          {status?.toUpperCase() || 'UNKNOWN'}
        </Tag>
      ),
    },
    {
      title: 'Consecutive Failures',
      dataIndex: 'consecutiveFailures',
      key: 'consecutiveFailures',
      width: 150,
      align: 'center' as const,
      render: (failures: number) =>
        failures > 0 ? (
          <Badge count={failures} style={{ backgroundColor: '#ff4d4f' }} />
        ) : (
          <span style={{ color: '#52c41a' }}>0</span>
        ),
    },
    {
      title: 'Response Time',
      dataIndex: 'responseTimeMs',
      key: 'responseTimeMs',
      width: 130,
      align: 'right' as const,
      render: (responseTime: number | null) =>
        responseTime !== null ? `${responseTime}ms` : '-',
    },
    {
      title: 'Last Check',
      dataIndex: 'lastHealthCheckAt',
      key: 'lastHealthCheckAt',
      width: 180,
      render: (lastCheck: string | null) =>
        lastCheck ? dayjs(lastCheck).format('YYYY-MM-DD HH:mm:ss') : 'Never',
    },
    {
      title: 'Enabled',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      align: 'center' as const,
      render: (enabled: boolean) =>
        enabled ? (
          <Tag color="success">Yes</Tag>
        ) : (
          <Tag color="default">No</Tag>
        ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      render: (_: any, record: HealthCheckResult) => (
        <Space>
          <Tooltip title="Perform Health Check Now">
            <Button
              type="text"
              icon={<ThunderboltOutlined />}
              onClick={() => handlePerformHealthCheck(record.targetId)}
              disabled={!record.enabled}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <div>
      {stats && (
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col span={6}>
            <Card>
              <Statistic
                title="Total Targets"
                value={stats.totalTargets}
                prefix={<HeartOutlined />}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="Healthy Targets"
                value={stats.healthyTargets}
                prefix={<CheckCircleOutlined />}
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="Unhealthy Targets"
                value={stats.unhealthyTargets}
                prefix={<CloseCircleOutlined />}
                valueStyle={{ color: '#cf1322' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="Health Rate"
                value={stats.healthyPercentage}
                precision={1}
                suffix="%"
                prefix={<HeartOutlined />}
                valueStyle={{ color: stats.healthyPercentage >= 80 ? '#3f8600' : '#cf1322' }}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Card
        title={
          <Space>
            <HeartOutlined />
            Health Checks
            <Tag color="blue">Auto-refresh: 30s</Tag>
          </Space>
        }
        extra={
          <Space>
            <Select
              style={{ width: 200 }}
              placeholder="Filter by Service"
              allowClear
              value={selectedService}
              onChange={(value) => setSelectedService(value ?? null)}
            >
              {servicesList.map((service) => (
                <Select.Option key={service.id} value={service.id}>
                  {service.name}
                </Select.Option>
              ))}
            </Select>
            <Button icon={<ReloadOutlined />} onClick={() => { loadHealthChecks(); loadStats(); }}>
              Refresh
            </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={healthChecks}
          rowKey="targetId"
          loading={loading}
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1400 }}
          expandable={{
            expandedRowRender: (record) =>
              record.errorMessage ? (
                <div style={{ padding: '8px 16px', backgroundColor: '#fff1f0', borderLeft: '3px solid #ff4d4f' }}>
                  <strong>Error:</strong> {record.errorMessage}
                </div>
              ) : null,
            rowExpandable: (record) => !!record.errorMessage,
          }}
        />
      </Card>
    </div>
  );
};

export default HealthChecksPage;
