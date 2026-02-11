import React, { useState, useEffect } from 'react';
import { Card, Button, Table, Form, Input, InputNumber, Modal, message, Space, Tag, Switch, Select, Typography } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, CloudServerOutlined } from '@ant-design/icons';
import { gateway } from '../../services/api';

const { Title, Text } = Typography;
const { TextArea } = Input;

interface Service {
  id: number;
  name: string;
  protocol: string;
  host: string;
  port: number;
  basePath: string | null;
  connectTimeout: number;
  readTimeout: number;
  writeTimeout: number;
  retries: number;
  loadBalancingAlgorithm: string;
  healthCheckEnabled: boolean;
  healthCheckPath: string | null;
  healthCheckInterval: number;
  healthCheckTimeout: number;
  healthyThreshold: number;
  unhealthyThreshold: number;
  description: string | null;
  enabled: boolean;
}

const ServicesPage: React.FC = () => {
  const [services, setServices] = useState<Service[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingService, setEditingService] = useState<Service | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadServices();
  }, []);

  const loadServices = async () => {
    setLoading(true);
    try {
      const response = await gateway.getServices();
      setServices(response.data);
    } catch (error) {
      message.error('Failed to load services');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingService(null);
    form.resetFields();
    form.setFieldsValue({
      protocol: 'http',
      port: 8080,
      connectTimeout: 5000,
      readTimeout: 30000,
      writeTimeout: 30000,
      retries: 3,
      loadBalancingAlgorithm: 'ROUND_ROBIN',
      healthCheckEnabled: true,
      healthCheckInterval: 30000,
      healthCheckTimeout: 5000,
      healthyThreshold: 2,
      unhealthyThreshold: 3,
      enabled: true,
    });
    setModalVisible(true);
  };

  const handleEdit = (record: Service) => {
    setEditingService(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    Modal.confirm({
      title: 'Delete Service',
      content: 'Are you sure you want to delete this service? All related routes and targets will be affected.',
      okText: 'Delete',
      okType: 'danger',
      onOk: async () => {
        try {
          await gateway.deleteService(id);
          message.success('Service deleted successfully');
          loadServices();
        } catch (error) {
          message.error('Failed to delete service');
        }
      },
    });
  };

  const handleToggle = async (id: number) => {
    try {
      await gateway.toggleService(id);
      message.success('Service toggled successfully');
      loadServices();
    } catch (error) {
      message.error('Failed to toggle service');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      if (editingService) {
        await gateway.updateService(editingService.id, values);
        message.success('Service updated successfully');
      } else {
        await gateway.createService(values);
        message.success('Service created successfully');
      }

      setModalVisible(false);
      loadServices();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      width: 150,
    },
    {
      title: 'URL',
      key: 'url',
      width: 250,
      render: (_: any, record: Service) => (
        <Text code>{`${record.protocol}://${record.host}:${record.port}${record.basePath || ''}`}</Text>
      ),
    },
    {
      title: 'LB Algorithm',
      dataIndex: 'loadBalancingAlgorithm',
      key: 'loadBalancingAlgorithm',
      width: 120,
      render: (algo: string) => <Tag color="cyan">{algo}</Tag>,
    },
    {
      title: 'Health Check',
      dataIndex: 'healthCheckEnabled',
      key: 'healthCheckEnabled',
      width: 120,
      align: 'center' as const,
      render: (enabled: boolean) => (
        <Tag color={enabled ? 'green' : 'default'}>{enabled ? 'Enabled' : 'Disabled'}</Tag>
      ),
    },
    {
      title: 'Timeouts (ms)',
      key: 'timeouts',
      width: 150,
      render: (_: any, record: Service) => (
        <div>
          <div><Text type="secondary">Connect: </Text>{record.connectTimeout}</div>
          <div><Text type="secondary">Read: </Text>{record.readTimeout}</div>
          <div><Text type="secondary">Write: </Text>{record.writeTimeout}</div>
        </div>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      align: 'center' as const,
      render: (enabled: boolean, record: Service) => (
        <Switch checked={enabled} onChange={() => handleToggle(record.id)} />
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      align: 'center' as const,
      render: (_: any, record: Service) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)} />
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card>
        <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <Title level={4} style={{ margin: 0 }}>
              <CloudServerOutlined /> Services Management
            </Title>
            <Text type="secondary">Manage upstream services and their configurations</Text>
          </div>
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadServices}>
              Refresh
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              Create Service
            </Button>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={services}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1400 }}
        />
      </Card>

      <Modal
        title={editingService ? 'Edit Service' : 'Create Service'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={700}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="name" label="Name" rules={[{ required: true, message: 'Please enter service name' }]}>
            <Input placeholder="backend-service" />
          </Form.Item>

          <Space style={{ width: '100%' }} size="large">
            <Form.Item name="protocol" label="Protocol" rules={[{ required: true }]}>
              <Select style={{ width: 100 }}>
                <Select.Option value="http">HTTP</Select.Option>
                <Select.Option value="https">HTTPS</Select.Option>
              </Select>
            </Form.Item>

            <Form.Item name="host" label="Host" rules={[{ required: true }]} style={{ flex: 1 }}>
              <Input placeholder="localhost" />
            </Form.Item>

            <Form.Item name="port" label="Port" rules={[{ required: true }]}>
              <InputNumber min={1} max={65535} placeholder="8080" style={{ width: 100 }} />
            </Form.Item>
          </Space>

          <Form.Item name="basePath" label="Base Path">
            <Input placeholder="/api" />
          </Form.Item>

          <Title level={5}>Timeouts & Retry</Title>
          <Space style={{ width: '100%' }}>
            <Form.Item name="connectTimeout" label="Connect Timeout (ms)">
              <InputNumber min={100} placeholder="5000" style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item name="readTimeout" label="Read Timeout (ms)">
              <InputNumber min={100} placeholder="30000" style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item name="writeTimeout" label="Write Timeout (ms)">
              <InputNumber min={100} placeholder="30000" style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item name="retries" label="Retries">
              <InputNumber min={0} placeholder="3" style={{ width: 80 }} />
            </Form.Item>
          </Space>

          <Form.Item name="loadBalancingAlgorithm" label="Load Balancing Algorithm">
            <Select>
              <Select.Option value="ROUND_ROBIN">Round Robin</Select.Option>
              <Select.Option value="LEAST_CONNECTIONS">Least Connections</Select.Option>
              <Select.Option value="IP_HASH">IP Hash</Select.Option>
              <Select.Option value="WEIGHTED_ROUND_ROBIN">Weighted Round Robin</Select.Option>
            </Select>
          </Form.Item>

          <Title level={5}>Health Check</Title>
          <Form.Item name="healthCheckEnabled" label="Health Check Enabled" valuePropName="checked">
            <Switch />
          </Form.Item>

          <Form.Item name="healthCheckPath" label="Health Check Path">
            <Input placeholder="/actuator/health" />
          </Form.Item>

          <Space style={{ width: '100%' }}>
            <Form.Item name="healthCheckInterval" label="Interval (ms)">
              <InputNumber min={1000} placeholder="30000" style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item name="healthCheckTimeout" label="Timeout (ms)">
              <InputNumber min={100} placeholder="5000" style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item name="healthyThreshold" label="Healthy Threshold">
              <InputNumber min={1} placeholder="2" style={{ width: 100 }} />
            </Form.Item>

            <Form.Item name="unhealthyThreshold" label="Unhealthy Threshold">
              <InputNumber min={1} placeholder="3" style={{ width: 100 }} />
            </Form.Item>
          </Space>

          <Form.Item name="description" label="Description">
            <TextArea rows={2} placeholder="Description" />
          </Form.Item>

          <Form.Item name="enabled" label="Enabled" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ServicesPage;
