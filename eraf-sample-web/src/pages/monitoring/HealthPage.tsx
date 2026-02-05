import React, { useState, useEffect } from 'react';
import { Card, Button, Typography, Space, Tag, Spin, Row, Col, Statistic, List, Switch } from 'antd';
import { HeartOutlined, ReloadOutlined, CheckCircleOutlined, CloseCircleOutlined, WarningOutlined } from '@ant-design/icons';
import { monitoringApi } from '../../services/api';

const { Title, Text } = Typography;

const HealthPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [health, setHealth] = useState<any>(null);

  const loadHealth = async () => {
    setLoading(true);
    try {
      const response = await monitoringApi.getHealth();
      setHealth(response.data);
    } catch (error) {
      console.error('Failed to load health');
    } finally {
      setLoading(false);
    }
  };

  const toggleComponent = async (component: string) => {
    setLoading(true);
    try {
      await monitoringApi.toggleHealth(component);
      loadHealth();
    } catch (error) {
      console.error('Failed to toggle component');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHealth();
    const interval = setInterval(loadHealth, 30000); // Auto refresh every 30 seconds
    return () => clearInterval(interval);
  }, []);

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'UP': return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
      case 'DOWN': return <CloseCircleOutlined style={{ color: '#ff4d4f' }} />;
      case 'UNKNOWN': return <WarningOutlined style={{ color: '#faad14' }} />;
      default: return null;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'UP': return 'green';
      case 'DOWN': return 'red';
      case 'UNKNOWN': return 'orange';
      default: return 'default';
    }
  };

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <HeartOutlined /> Health Check (#64)
      </Title>

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Button icon={<ReloadOutlined />} onClick={loadHealth}>Refresh</Button>
        </Space>

        {health && (
          <>
            <Row gutter={16} style={{ marginBottom: 24 }}>
              <Col span={8}>
                <Card>
                  <Statistic
                    title="Overall Status"
                    value={health.status}
                    valueStyle={{ color: health.status === 'UP' ? '#52c41a' : '#ff4d4f' }}
                    prefix={getStatusIcon(health.status)}
                  />
                </Card>
              </Col>
              <Col span={8}>
                <Card>
                  <Statistic
                    title="Healthy Components"
                    value={health.components ? Object.values(health.components).filter((c: any) => c.status === 'UP').length : 0}
                    suffix={`/ ${health.components ? Object.keys(health.components).length : 0}`}
                    valueStyle={{ color: '#52c41a' }}
                  />
                </Card>
              </Col>
              <Col span={8}>
                <Card>
                  <Statistic
                    title="Last Check"
                    value={health.timestampFormatted || new Date().toLocaleTimeString()}
                  />
                </Card>
              </Col>
            </Row>

            <Title level={5}>Component Health</Title>
            <List
              dataSource={health.components ? Object.entries(health.components) : []}
              renderItem={([name, component]: [string, any]) => (
                <List.Item
                  actions={[
                    <Switch
                      checked={component.status === 'UP'}
                      onChange={() => toggleComponent(name)}
                      checkedChildren="UP"
                      unCheckedChildren="DOWN"
                    />,
                  ]}
                >
                  <List.Item.Meta
                    avatar={getStatusIcon(component.status)}
                    title={
                      <Space>
                        <Text strong>{name}</Text>
                        <Tag color={getStatusColor(component.status)}>{component.status}</Tag>
                      </Space>
                    }
                    description={
                      component.details && (
                        <div>
                          {Object.entries(component.details).map(([key, value]: [string, any]) => (
                            <Tag key={key}>{key}: {String(value)}</Tag>
                          ))}
                        </div>
                      )
                    }
                  />
                </List.Item>
              )}
            />

            {health.diskSpace && (
              <Card size="small" title="Disk Space" style={{ marginTop: 16 }}>
                <Row gutter={16}>
                  <Col span={8}>
                    <Statistic title="Total" value={health.diskSpace.totalFormatted} />
                  </Col>
                  <Col span={8}>
                    <Statistic title="Free" value={health.diskSpace.freeFormatted} />
                  </Col>
                  <Col span={8}>
                    <Statistic title="Used" value={`${health.diskSpace.usedPercent}%`} />
                  </Col>
                </Row>
              </Card>
            )}

            {health.memory && (
              <Card size="small" title="Memory" style={{ marginTop: 16 }}>
                <Row gutter={16}>
                  <Col span={8}>
                    <Statistic title="Heap Used" value={health.memory.heapUsedFormatted} />
                  </Col>
                  <Col span={8}>
                    <Statistic title="Heap Max" value={health.memory.heapMaxFormatted} />
                  </Col>
                  <Col span={8}>
                    <Statistic title="Usage" value={`${health.memory.heapUsagePercent}%`} />
                  </Col>
                </Row>
              </Card>
            )}
          </>
        )}
      </Card>
    </Spin>
  );
};

export default HealthPage;
