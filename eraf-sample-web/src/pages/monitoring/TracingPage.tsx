import React, { useState, useEffect } from 'react';
import { Card, Button, Table, Typography, Space, Tag, Spin, Select, Form, InputNumber, Modal, Timeline, Descriptions } from 'antd';
import { NodeIndexOutlined, ReloadOutlined, EyeOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { monitoringApi } from '../../services/api';

const { Title, Text } = Typography;

const TracingPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [traces, setTraces] = useState<any[]>([]);
  const [selectedTrace, setSelectedTrace] = useState<any>(null);
  const [detailModal, setDetailModal] = useState(false);
  const [statusFilter, setStatusFilter] = useState('');

  // Generate trace form
  const [operation, setOperation] = useState('api-call');
  const [duration, setDuration] = useState(100);
  const [status, setStatus] = useState('OK');

  const loadTraces = async () => {
    setLoading(true);
    try {
      const response = await monitoringApi.getTraces(statusFilter || undefined, 50);
      setTraces(response.data.traces || []);
    } catch (error) {
      console.error('Failed to load traces');
    } finally {
      setLoading(false);
    }
  };

  const loadTraceDetail = async (traceId: string) => {
    setLoading(true);
    try {
      const response = await monitoringApi.getTrace(traceId);
      setSelectedTrace(response.data);
      setDetailModal(true);
    } catch (error) {
      console.error('Failed to load trace detail');
    } finally {
      setLoading(false);
    }
  };

  const generateTrace = async () => {
    setLoading(true);
    try {
      await monitoringApi.generateTrace(operation, duration, status);
      loadTraces();
    } catch (error) {
      console.error('Failed to generate trace');
    } finally {
      setLoading(false);
    }
  };

  const clearTraces = async () => {
    setLoading(true);
    try {
      await monitoringApi.clearTraces();
      setTraces([]);
    } catch (error) {
      console.error('Failed to clear traces');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTraces();
  }, []);

  useEffect(() => {
    loadTraces();
  }, [statusFilter]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OK': return 'green';
      case 'ERROR': return 'red';
      case 'TIMEOUT': return 'orange';
      default: return 'default';
    }
  };

  const traceColumns = [
    { title: 'Trace ID', dataIndex: 'traceId', key: 'traceId', width: 200, ellipsis: true },
    { title: 'Operation', dataIndex: 'operation', key: 'operation' },
    { title: 'Status', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={getStatusColor(s)}>{s}</Tag> },
    { title: 'Duration', dataIndex: 'durationMs', key: 'duration', render: (d: number) => `${d}ms` },
    { title: 'Spans', dataIndex: 'spanCount', key: 'spanCount' },
    { title: 'Started', dataIndex: 'startedAtFormatted', key: 'startedAt' },
    {
      title: 'Action',
      key: 'action',
      render: (_: any, record: any) => (
        <Button size="small" icon={<EyeOutlined />} onClick={() => loadTraceDetail(record.traceId)}>
          Detail
        </Button>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <NodeIndexOutlined /> Distributed Tracing (#66)
      </Title>

      <Card>
        <Space direction="vertical" style={{ width: '100%' }}>
          <Card size="small" title="Generate Test Trace">
            <Space wrap>
              <Select value={operation} onChange={setOperation} style={{ width: 150 }}>
                <Select.Option value="api-call">API Call</Select.Option>
                <Select.Option value="database-query">Database Query</Select.Option>
                <Select.Option value="http-request">HTTP Request</Select.Option>
                <Select.Option value="message-publish">Message Publish</Select.Option>
                <Select.Option value="cache-operation">Cache Operation</Select.Option>
              </Select>
              <InputNumber
                value={duration}
                onChange={(v) => setDuration(v || 100)}
                addonAfter="ms"
                min={1}
                max={10000}
                style={{ width: 120 }}
              />
              <Select value={status} onChange={setStatus} style={{ width: 100 }}>
                <Select.Option value="OK">OK</Select.Option>
                <Select.Option value="ERROR">ERROR</Select.Option>
                <Select.Option value="TIMEOUT">TIMEOUT</Select.Option>
              </Select>
              <Button type="primary" icon={<PlusOutlined />} onClick={generateTrace}>
                Generate
              </Button>
            </Space>
          </Card>

          <Space style={{ marginBottom: 16 }}>
            <Select
              value={statusFilter}
              onChange={setStatusFilter}
              style={{ width: 150 }}
              placeholder="Filter by status"
              allowClear
            >
              <Select.Option value="">All</Select.Option>
              <Select.Option value="OK">OK</Select.Option>
              <Select.Option value="ERROR">ERROR</Select.Option>
              <Select.Option value="TIMEOUT">TIMEOUT</Select.Option>
            </Select>
            <Button icon={<ReloadOutlined />} onClick={loadTraces}>Refresh</Button>
            <Button danger icon={<DeleteOutlined />} onClick={clearTraces}>Clear All</Button>
          </Space>

          <Table
            dataSource={traces}
            columns={traceColumns}
            rowKey="traceId"
            size="small"
            loading={loading}
          />
        </Space>
      </Card>

      <Modal
        title="Trace Detail"
        open={detailModal}
        onCancel={() => setDetailModal(false)}
        footer={null}
        width={800}
      >
        {selectedTrace && (
          <>
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="Trace ID">{selectedTrace.traceId}</Descriptions.Item>
              <Descriptions.Item label="Operation">{selectedTrace.operation}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={getStatusColor(selectedTrace.status)}>{selectedTrace.status}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Total Duration">{selectedTrace.durationMs}ms</Descriptions.Item>
              <Descriptions.Item label="Started">{selectedTrace.startedAtFormatted}</Descriptions.Item>
              <Descriptions.Item label="Spans">{selectedTrace.spans?.length || 0}</Descriptions.Item>
            </Descriptions>

            <Title level={5} style={{ marginTop: 16 }}>Spans</Title>
            <Timeline>
              {selectedTrace.spans?.map((span: any, idx: number) => (
                <Timeline.Item
                  key={idx}
                  color={span.status === 'OK' ? 'green' : span.status === 'ERROR' ? 'red' : 'orange'}
                >
                  <Space direction="vertical" size={0}>
                    <Text strong>{span.name}</Text>
                    <Space>
                      <Tag color={getStatusColor(span.status)}>{span.status}</Tag>
                      <Text type="secondary">{span.durationMs}ms</Text>
                    </Space>
                    {span.attributes && Object.keys(span.attributes).length > 0 && (
                      <div style={{ marginTop: 4 }}>
                        {Object.entries(span.attributes).map(([key, value]: [string, any]) => (
                          <Tag key={key} style={{ marginRight: 4 }}>{key}: {String(value)}</Tag>
                        ))}
                      </div>
                    )}
                    {span.error && <Text type="danger">{span.error}</Text>}
                  </Space>
                </Timeline.Item>
              ))}
            </Timeline>

            {selectedTrace.tags && Object.keys(selectedTrace.tags).length > 0 && (
              <>
                <Title level={5}>Tags</Title>
                <div>
                  {Object.entries(selectedTrace.tags).map(([key, value]: [string, any]) => (
                    <Tag key={key}>{key}: {String(value)}</Tag>
                  ))}
                </div>
              </>
            )}
          </>
        )}
      </Modal>
    </Spin>
  );
};

export default TracingPage;
