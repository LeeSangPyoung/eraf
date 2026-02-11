import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Switch,
  Select,
  DatePicker,
  message,
  Tooltip,
  Typography,
  Row,
  Col,
  Statistic,
  Alert,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  WarningOutlined,
  SafetyCertificateOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import { gateway } from '../../services/api';
import dayjs from 'dayjs';

const { Text } = Typography;
const { TextArea } = Input;

interface Certificate {
  id: number;
  name: string;
  description?: string;
  cert: string;
  certChain?: string;
  snis: string[];
  issuer?: string;
  subject?: string;
  validFrom?: string;
  expiresAt?: string;
  certType: string;
  enabled: boolean;
  expired: boolean;
  expiringSoon: boolean;
  tags?: Record<string, string>;
  createdAt: string;
  updatedAt: string;
}

interface CertificateStats {
  totalCertificates: number;
  enabledCertificates: number;
  expiredCertificates: number;
}

const CERT_TYPES = [
  { value: 'STANDARD', label: 'Standard' },
  { value: 'WILDCARD', label: 'Wildcard' },
  { value: 'SAN', label: 'SAN (Subject Alternative Name)' },
  { value: 'SELF_SIGNED', label: 'Self-Signed' },
];

const CertificatesPage: React.FC = () => {
  const [certificates, setCertificates] = useState<Certificate[]>([]);
  const [stats, setStats] = useState<CertificateStats | null>(null);
  const [expiringSoon, setExpiringSoon] = useState<Certificate[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingCert, setEditingCert] = useState<Certificate | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadCertificates();
    loadStats();
    loadExpiringSoon();
  }, []);

  const loadCertificates = async () => {
    setLoading(true);
    try {
      const response = await gateway.getCertificates();
      setCertificates(response.data);
    } catch (error) {
      message.error('Failed to load certificates');
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const response = await gateway.getCertificateStats();
      setStats(response.data);
    } catch (error) {
      console.error('Failed to load stats:', error);
    }
  };

  const loadExpiringSoon = async () => {
    try {
      const response = await gateway.getExpiringSoonCertificates();
      setExpiringSoon(response.data);
    } catch (error) {
      console.error('Failed to load expiring soon:', error);
    }
  };

  const handleCreate = () => {
    setEditingCert(null);
    form.resetFields();
    form.setFieldsValue({
      certType: 'STANDARD',
      enabled: true,
      snis: '',
    });
    setModalVisible(true);
  };

  const handleEdit = (record: Certificate) => {
    setEditingCert(record);
    form.setFieldsValue({
      ...record,
      snis: record.snis ? record.snis.join('\n') : '',
      validFrom: record.validFrom ? dayjs(record.validFrom) : undefined,
      expiresAt: record.expiresAt ? dayjs(record.expiresAt) : undefined,
    });
    setModalVisible(true);
  };

  const handleDelete = (record: Certificate) => {
    Modal.confirm({
      title: 'Delete Certificate',
      content: `Are you sure you want to delete certificate "${record.name}"?`,
      okText: 'Delete',
      okType: 'danger',
      onOk: async () => {
        try {
          await gateway.deleteCertificate(record.id);
          message.success('Certificate deleted successfully');
          loadCertificates();
          loadStats();
        } catch (error) {
          message.error('Failed to delete certificate');
        }
      },
    });
  };

  const handleToggle = async (record: Certificate) => {
    try {
      await gateway.toggleCertificate(record.id);
      message.success(`Certificate ${record.enabled ? 'disabled' : 'enabled'} successfully`);
      loadCertificates();
      loadStats();
    } catch (error) {
      message.error('Failed to toggle certificate');
    }
  };

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields();
      const data = {
        ...values,
        snis: values.snis
          ? values.snis.split('\n').map((s: string) => s.trim()).filter(Boolean)
          : [],
        validFrom: values.validFrom ? values.validFrom.format('YYYY-MM-DDTHH:mm:ss') : null,
        expiresAt: values.expiresAt ? values.expiresAt.format('YYYY-MM-DDTHH:mm:ss') : null,
      };

      if (editingCert) {
        await gateway.updateCertificate(editingCert.id, data);
        message.success('Certificate updated successfully');
      } else {
        await gateway.createCertificate(data);
        message.success('Certificate created successfully');
      }
      setModalVisible(false);
      loadCertificates();
      loadStats();
      loadExpiringSoon();
    } catch (error: any) {
      if (error.response?.data?.message) {
        message.error(error.response.data.message);
      }
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
      width: 160,
      render: (name: string, record: Certificate) => (
        <Space direction="vertical" size={0}>
          <Text strong>{name}</Text>
          {record.subject && <Text type="secondary" style={{ fontSize: 11 }}>{record.subject}</Text>}
        </Space>
      ),
    },
    {
      title: 'Type',
      dataIndex: 'certType',
      key: 'certType',
      width: 100,
      render: (type: string) => {
        const colorMap: Record<string, string> = {
          STANDARD: 'blue',
          WILDCARD: 'purple',
          SAN: 'cyan',
          SELF_SIGNED: 'orange',
        };
        return <Tag color={colorMap[type] || 'default'}>{type}</Tag>;
      },
    },
    {
      title: 'SNIs',
      dataIndex: 'snis',
      key: 'snis',
      render: (snis: string[]) =>
        snis && snis.length > 0
          ? snis.map((sni) => <Tag key={sni}>{sni}</Tag>)
          : <Text type="secondary">None</Text>,
    },
    {
      title: 'Issuer',
      dataIndex: 'issuer',
      key: 'issuer',
      width: 140,
      ellipsis: true,
    },
    {
      title: 'Expires',
      dataIndex: 'expiresAt',
      key: 'expiresAt',
      width: 160,
      render: (expiresAt: string, record: Certificate) => {
        if (!expiresAt) return <Text type="secondary">N/A</Text>;
        if (record.expired) {
          return <Tag icon={<CloseCircleOutlined />} color="error">Expired: {expiresAt.substring(0, 10)}</Tag>;
        }
        if (record.expiringSoon) {
          return <Tag icon={<WarningOutlined />} color="warning">Expiring: {expiresAt.substring(0, 10)}</Tag>;
        }
        return <Tag icon={<CheckCircleOutlined />} color="success">{expiresAt.substring(0, 10)}</Tag>;
      },
    },
    {
      title: 'Status',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      align: 'center' as const,
      render: (enabled: boolean) =>
        enabled ? (
          <Tag icon={<CheckCircleOutlined />} color="success">Enabled</Tag>
        ) : (
          <Tag icon={<CloseCircleOutlined />} color="default">Disabled</Tag>
        ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 180,
      render: (_: any, record: Certificate) => (
        <Space size="small">
          <Tooltip title="Edit">
            <Button type="text" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          </Tooltip>
          <Tooltip title={record.enabled ? 'Disable' : 'Enable'}>
            <Switch size="small" checked={record.enabled} onChange={() => handleToggle(record)} />
          </Tooltip>
          <Tooltip title="Delete">
            <Button type="text" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)} />
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <div>
      {expiringSoon.length > 0 && (
        <Alert
          message={`${expiringSoon.length} certificate(s) expiring within 30 days`}
          description={expiringSoon.map((c) => c.name).join(', ')}
          type="warning"
          showIcon
          icon={<ExclamationCircleOutlined />}
          style={{ marginBottom: 16 }}
          closable
        />
      )}

      {stats && (
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col span={8}>
            <Card><Statistic title="Total Certificates" value={stats.totalCertificates} prefix={<SafetyCertificateOutlined />} /></Card>
          </Col>
          <Col span={8}>
            <Card><Statistic title="Enabled" value={stats.enabledCertificates} prefix={<CheckCircleOutlined />} valueStyle={{ color: '#3f8600' }} /></Card>
          </Col>
          <Col span={8}>
            <Card><Statistic title="Expired" value={stats.expiredCertificates} prefix={<CloseCircleOutlined />} valueStyle={{ color: '#cf1322' }} /></Card>
          </Col>
        </Row>
      )}

      <Card
        title={<Space><SafetyCertificateOutlined /> Certificates</Space>}
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadCertificates}>Refresh</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>Create Certificate</Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={certificates}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1200 }}
        />
      </Card>

      <Modal
        title={editingCert ? 'Edit Certificate' : 'Create Certificate'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
        width={700}
      >
        <Form form={form} layout="vertical">
          <Row gutter={16}>
            <Col span={16}>
              <Form.Item
                name="name"
                label="Name"
                rules={[
                  { required: true, message: 'Please input name' },
                  { pattern: /^[a-zA-Z0-9_.-]{2,100}$/, message: '2-100 chars, alphanumeric with _/-/.' },
                ]}
              >
                <Input placeholder="e.g., api.example.com" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="certType" label="Type" rules={[{ required: true }]}>
                <Select options={CERT_TYPES} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="description" label="Description">
            <Input placeholder="Description" />
          </Form.Item>

          <Form.Item
            name="cert"
            label="Certificate (PEM)"
            rules={[{ required: true, message: 'Certificate is required' }]}
          >
            <TextArea rows={4} placeholder="-----BEGIN CERTIFICATE-----&#10;...&#10;-----END CERTIFICATE-----" />
          </Form.Item>

          <Form.Item
            name="privateKey"
            label="Private Key (PEM)"
            rules={[{ required: !editingCert, message: 'Private key is required' }]}
          >
            <TextArea rows={4} placeholder="-----BEGIN PRIVATE KEY-----&#10;...&#10;-----END PRIVATE KEY-----" />
          </Form.Item>

          <Form.Item name="certChain" label="Certificate Chain (optional)">
            <TextArea rows={3} placeholder="Intermediate CA certificates (PEM)" />
          </Form.Item>

          <Form.Item name="snis" label="SNIs (one per line)" tooltip="Server Name Indication domains">
            <TextArea rows={2} placeholder="api.example.com&#10;*.example.com" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="issuer" label="Issuer">
                <Input placeholder="e.g., Let's Encrypt" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="subject" label="Subject">
                <Input placeholder="e.g., CN=api.example.com" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="validFrom" label="Valid From">
                <DatePicker showTime style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="expiresAt" label="Expires At">
                <DatePicker showTime style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="enabled" label="Enabled" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CertificatesPage;
