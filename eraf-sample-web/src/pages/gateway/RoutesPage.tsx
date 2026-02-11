import React, { useState, useEffect } from 'react';
import { Card, Button, Table, Form, Input, InputNumber, Modal, message, Space, Tag, Switch, Select, Typography } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, ApiOutlined } from '@ant-design/icons';
import { gateway } from '../../services/api';

const { Title, Text } = Typography;
const { TextArea } = Input;

interface Route {
  id: number;
  name: string;
  paths: string[];
  methods: string[];
  hosts: string[] | null;
  serviceId: number;
  serviceName?: string;
  priority: number;
  stripPath: boolean;
  pathPrefix: string | null;
  description: string | null;
  enabled: boolean;
}

const RoutesPage: React.FC = () => {
  const [routes, setRoutes] = useState<Route[]>([]);
  const [services, setServices] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRoute, setEditingRoute] = useState<Route | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadRoutes();
    loadServices();
  }, []);

  const loadRoutes = async () => {
    setLoading(true);
    try {
      const response = await gateway.getRoutes();
      setRoutes(response.data);
    } catch (error) {
      message.error('Failed to load routes');
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
    setEditingRoute(null);
    form.resetFields();
    form.setFieldsValue({
      priority: 0,
      stripPath: true,
      enabled: true,
      methods: ['GET', 'POST', 'PUT', 'DELETE'],
    });
    setModalVisible(true);
  };

  const handleEdit = (record: Route) => {
    setEditingRoute(record);
    form.setFieldsValue({
      ...record,
      paths: record.paths.join('\n'),
      methods: record.methods,
      hosts: record.hosts ? record.hosts.join('\n') : '',
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    Modal.confirm({
      title: 'Delete Route',
      content: 'Are you sure you want to delete this route?',
      okText: 'Delete',
      okType: 'danger',
      onOk: async () => {
        try {
          await gateway.deleteRoute(id);
          message.success('Route deleted successfully');
          loadRoutes();
        } catch (error) {
          message.error('Failed to delete route');
        }
      },
    });
  };

  const handleToggle = async (id: number) => {
    try {
      await gateway.toggleRoute(id);
      message.success('Route toggled successfully');
      loadRoutes();
    } catch (error) {
      message.error('Failed to toggle route');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      const data = {
        ...values,
        paths: values.paths.split('\n').map((p: string) => p.trim()).filter(Boolean),
        hosts: values.hosts ? values.hosts.split('\n').map((h: string) => h.trim()).filter(Boolean) : null,
      };

      if (editingRoute) {
        await gateway.updateRoute(editingRoute.id, data);
        message.success('Route updated successfully');
      } else {
        await gateway.createRoute(data);
        message.success('Route created successfully');
      }

      setModalVisible(false);
      loadRoutes();
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
      title: 'Paths',
      dataIndex: 'paths',
      key: 'paths',
      render: (paths: string[]) => paths.map((p) => <Tag key={p}>{p}</Tag>),
    },
    {
      title: 'Methods',
      dataIndex: 'methods',
      key: 'methods',
      width: 200,
      render: (methods: string[] | null) =>
        methods ? methods.map((m) => <Tag key={m} color="blue">{m}</Tag>) : <Tag>ALL</Tag>,
    },
    {
      title: 'Service',
      dataIndex: 'serviceName',
      key: 'serviceName',
      width: 150,
    },
    {
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
      width: 80,
      align: 'center' as const,
    },
    {
      title: 'Strip Path',
      dataIndex: 'stripPath',
      key: 'stripPath',
      width: 100,
      align: 'center' as const,
      render: (stripPath: boolean) => <Tag color={stripPath ? 'green' : 'default'}>{stripPath ? 'Yes' : 'No'}</Tag>,
    },
    {
      title: 'Status',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      align: 'center' as const,
      render: (enabled: boolean, record: Route) => (
        <Switch checked={enabled} onChange={() => handleToggle(record.id)} />
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      align: 'center' as const,
      render: (_: any, record: Route) => (
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
              <ApiOutlined /> Routes Management
            </Title>
            <Text type="secondary">Manage API Gateway routes and routing rules</Text>
          </div>
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadRoutes}>
              Refresh
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              Create Route
            </Button>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={routes}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1200 }}
        />
      </Card>

      <Modal
        title={editingRoute ? 'Edit Route' : 'Create Route'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="name" label="Name" rules={[{ required: true, message: 'Please enter route name' }]}>
            <Input placeholder="api-routes" />
          </Form.Item>

          <Form.Item name="paths" label="Paths (one per line)" rules={[{ required: true }]}>
            <TextArea rows={3} placeholder="/api/**&#10;/v1/api/**" />
          </Form.Item>

          <Form.Item name="methods" label="Methods" rules={[{ required: true }]}>
            <Select mode="multiple" placeholder="Select methods">
              <Select.Option value="GET">GET</Select.Option>
              <Select.Option value="POST">POST</Select.Option>
              <Select.Option value="PUT">PUT</Select.Option>
              <Select.Option value="DELETE">DELETE</Select.Option>
              <Select.Option value="PATCH">PATCH</Select.Option>
              <Select.Option value="OPTIONS">OPTIONS</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item name="hosts" label="Hosts (optional, one per line)">
            <TextArea rows={2} placeholder="api.example.com&#10;www.example.com" />
          </Form.Item>

          <Form.Item name="serviceId" label="Service" rules={[{ required: true, message: 'Please select a service' }]}>
            <Select placeholder="Select service">
              {services.map((s) => (
                <Select.Option key={s.id} value={s.id}>
                  {s.name} ({s.host}:{s.port})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item name="priority" label="Priority">
            <InputNumber min={0} placeholder="0" style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="stripPath" label="Strip Path" valuePropName="checked">
            <Switch />
          </Form.Item>

          <Form.Item name="pathPrefix" label="Path Prefix (optional)">
            <Input placeholder="/v1" />
          </Form.Item>

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

export default RoutesPage;
