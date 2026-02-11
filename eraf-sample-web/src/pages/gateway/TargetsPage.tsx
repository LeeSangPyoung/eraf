import React, { useState, useEffect } from 'react';
import { Card, Button, Table, Form, Input, InputNumber, Modal, message, Space, Tag, Switch, Select, Typography } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, NodeIndexOutlined } from '@ant-design/icons';
import { gateway } from '../../services/api';

const { Title, Text } = Typography;
const { TextArea } = Input;

interface Target {
  id: number;
  serviceId: number;
  serviceName?: string;
  host: string;
  port: number;
  weight: number;
  healthStatus: string;
  consecutiveFailures: number;
  lastHealthCheckAt: string | null;
  description: string | null;
  enabled: boolean;
}

const TargetsPage: React.FC = () => {
  const [targets, setTargets] = useState<Target[]>([]);
  const [services, setServices] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingTarget, setEditingTarget] = useState<Target | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadTargets();
    loadServices();
  }, []);

  const loadTargets = async () => {
    setLoading(true);
    try {
      const response = await gateway.getTargets();
      setTargets(response.data);
    } catch (error) {
      message.error('Failed to load targets');
    } finally {
      setLoading(false);
    }
  };

  const loadServices = async () => {
    try {
      const response = await gateway.getServices();
      setServices(response.data);
    } catch (error) {
      console.error('Failed to load services');
    }
  };

  const handleCreate = () => {
    setEditingTarget(null);
    form.resetFields();
    form.setFieldsValue({
      weight: 100,
      enabled: true,
    });
    setModalVisible(true);
  };

  const handleEdit = (record: Target) => {
    setEditingTarget(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    Modal.confirm({
      title: 'Delete Target',
      content: 'Are you sure you want to delete this target?',
      okText: 'Delete',
      okType: 'danger',
      onOk: async () => {
        try {
          await gateway.deleteTarget(id);
          message.success('Target deleted successfully');
          loadTargets();
        } catch (error) {
          message.error('Failed to delete target');
        }
      },
    });
  };

  const handleToggle = async (id: number) => {
    try {
      await gateway.toggleTarget(id);
      message.success('Target toggled successfully');
      loadTargets();
    } catch (error) {
      message.error('Failed to toggle target');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      if (editingTarget) {
        await gateway.updateTarget(editingTarget.id, values);
        message.success('Target updated successfully');
      } else {
        await gateway.createTarget(values);
        message.success('Target created successfully');
      }

      setModalVisible(false);
      loadTargets();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const getHealthStatusColor = (status: string) => {
    switch (status) {
      case 'HEALTHY':
        return 'green';
      case 'UNHEALTHY':
        return 'red';
      case 'CHECKING':
        return 'orange';
      default:
        return 'default';
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
      title: 'Service',
      dataIndex: 'serviceName',
      key: 'serviceName',
      width: 150,
    },
    {
      title: 'Target',
      key: 'target',
      width: 200,
      render: (_: any, record: Target) => <Text code>{`${record.host}:${record.port}`}</Text>,
    },
    {
      title: 'Weight',
      dataIndex: 'weight',
      key: 'weight',
      width: 80,
      align: 'center' as const,
    },
    {
      title: 'Health Status',
      dataIndex: 'healthStatus',
      key: 'healthStatus',
      width: 120,
      align: 'center' as const,
      render: (status: string) => <Tag color={getHealthStatusColor(status)}>{status}</Tag>,
    },
    {
      title: 'Failures',
      dataIndex: 'consecutiveFailures',
      key: 'consecutiveFailures',
      width: 100,
      align: 'center' as const,
      render: (failures: number) => (
        <Tag color={failures > 0 ? 'red' : 'default'}>{failures}</Tag>
      ),
    },
    {
      title: 'Last Health Check',
      dataIndex: 'lastHealthCheckAt',
      key: 'lastHealthCheckAt',
      width: 180,
      render: (date: string | null) => date ? new Date(date).toLocaleString() : '-',
    },
    {
      title: 'Status',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      align: 'center' as const,
      render: (enabled: boolean, record: Target) => (
        <Switch checked={enabled} onChange={() => handleToggle(record.id)} />
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      align: 'center' as const,
      render: (_: any, record: Target) => (
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
              <NodeIndexOutlined /> Targets Management
            </Title>
            <Text type="secondary">Manage service targets for load balancing</Text>
          </div>
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadTargets}>
              Refresh
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              Create Target
            </Button>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={targets}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1200 }}
        />
      </Card>

      <Modal
        title={editingTarget ? 'Edit Target' : 'Create Target'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="serviceId" label="Service" rules={[{ required: true, message: 'Please select a service' }]}>
            <Select placeholder="Select service">
              {services.map((s) => (
                <Select.Option key={s.id} value={s.id}>
                  {s.name} ({s.host}:{s.port})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Space style={{ width: '100%' }}>
            <Form.Item
              name="host"
              label="Host"
              rules={[{ required: true, message: 'Please enter host' }]}
              style={{ flex: 1 }}
            >
              <Input placeholder="localhost or 192.168.1.100" />
            </Form.Item>

            <Form.Item name="port" label="Port" rules={[{ required: true, message: 'Please enter port' }]}>
              <InputNumber min={1} max={65535} placeholder="8080" style={{ width: 100 }} />
            </Form.Item>

            <Form.Item name="weight" label="Weight" rules={[{ required: true }]}>
              <InputNumber min={1} max={1000} placeholder="100" style={{ width: 100 }} />
            </Form.Item>
          </Space>

          <Form.Item name="description" label="Description">
            <TextArea rows={2} placeholder="Backend instance 1" />
          </Form.Item>

          <Form.Item name="enabled" label="Enabled" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default TargetsPage;
