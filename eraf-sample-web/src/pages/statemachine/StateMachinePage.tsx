import React, { useState, useEffect } from 'react';
import { Card, Button, Select, Table, message, Typography, Tabs, Space, Tag, Input, Spin, Descriptions, Timeline } from 'antd';
import { ApartmentOutlined, ReloadOutlined, SendOutlined, HistoryOutlined } from '@ant-design/icons';
import { stateMachineApi } from '../../services/api';

const { Title, Text } = Typography;

const StateMachinePage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [definitions, setDefinitions] = useState<any[]>([]);
  const [selectedMachine, setSelectedMachine] = useState<string>('');
  const [machineDefinition, setMachineDefinition] = useState<any>(null);
  const [entityId, setEntityId] = useState('entity-001');
  const [currentState, setCurrentState] = useState<any>(null);
  const [history, setHistory] = useState<any[]>([]);
  const [selectedEvent, setSelectedEvent] = useState<string>('');

  const loadDefinitions = async () => {
    try {
      const response = await stateMachineApi.getDefinitions();
      setDefinitions(response.data.machines || []);
      if (response.data.machines?.length > 0) {
        setSelectedMachine(response.data.machines[0].name);
      }
    } catch (error) {
      console.error('Failed to load definitions');
    }
  };

  const loadMachineDefinition = async (name: string) => {
    try {
      const response = await stateMachineApi.getDefinition(name);
      setMachineDefinition(response.data);
    } catch (error) {
      console.error('Failed to load machine definition');
    }
  };

  const loadCurrentState = async () => {
    if (!selectedMachine || !entityId) return;
    setLoading(true);
    try {
      const response = await stateMachineApi.getState(selectedMachine, entityId);
      setCurrentState(response.data);
    } catch (error) {
      console.error('Failed to load state');
    } finally {
      setLoading(false);
    }
  };

  const loadHistory = async () => {
    if (!selectedMachine || !entityId) return;
    setLoading(true);
    try {
      const response = await stateMachineApi.getHistory(selectedMachine, entityId);
      setHistory(response.data.history || []);
    } catch (error) {
      console.error('Failed to load history');
    } finally {
      setLoading(false);
    }
  };

  const sendEvent = async () => {
    if (!selectedMachine || !entityId || !selectedEvent) return;
    setLoading(true);
    try {
      const response = await stateMachineApi.sendEvent(selectedMachine, entityId, selectedEvent);
      setCurrentState(response.data);
      message.success(`Event ${selectedEvent} sent`);
      loadHistory();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Failed to send event');
    } finally {
      setLoading(false);
    }
  };

  const forceState = async (state: string) => {
    if (!selectedMachine || !entityId) return;
    setLoading(true);
    try {
      const response = await stateMachineApi.forceState(selectedMachine, entityId, state);
      setCurrentState(response.data);
      message.success(`State forced to ${state}`);
      loadHistory();
    } catch (error) {
      message.error('Failed to force state');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDefinitions();
  }, []);

  useEffect(() => {
    if (selectedMachine) {
      loadMachineDefinition(selectedMachine);
      loadCurrentState();
      loadHistory();
    }
  }, [selectedMachine, entityId]);

  const items = [
    {
      key: '1',
      label: '#29 Definitions',
      children: (
        <Card>
          <Title level={5}>State Machine Definitions</Title>
          <Table
            dataSource={definitions}
            rowKey="name"
            size="small"
            columns={[
              { title: 'Name', dataIndex: 'name', key: 'name' },
              { title: 'States', dataIndex: 'stateCount', key: 'stateCount' },
              { title: 'Transitions', dataIndex: 'transitionCount', key: 'transitionCount' },
              { title: 'Initial State', dataIndex: 'initialState', key: 'initialState', render: (s) => <Tag color="blue">{s}</Tag> },
            ]}
          />
        </Card>
      ),
    },
    {
      key: '2',
      label: '#30 Visualization',
      children: (
        <Card>
          <Title level={5}>State Machine Visualization</Title>
          <Select value={selectedMachine} onChange={setSelectedMachine} style={{ width: 200, marginBottom: 16 }}>
            {definitions.map((d) => <Select.Option key={d.name} value={d.name}>{d.name}</Select.Option>)}
          </Select>
          {machineDefinition && (
            <div style={{ background: '#f5f5f5', padding: 16, borderRadius: 8 }}>
              <Text strong>States:</Text>
              <div style={{ margin: '8px 0' }}>
                {machineDefinition.states?.map((s: string) => (
                  <Tag key={s} color={s === currentState?.state ? 'blue' : 'default'}>{s}</Tag>
                ))}
              </div>
              <Text strong>Transitions:</Text>
              <div style={{ marginTop: 8 }}>
                {machineDefinition.transitions?.map((t: any, idx: number) => (
                  <div key={idx}>
                    <Tag>{t.from}</Tag> → <Tag color="green">{t.event}</Tag> → <Tag>{t.to}</Tag>
                  </div>
                ))}
              </div>
            </div>
          )}
        </Card>
      ),
    },
    {
      key: '3',
      label: '#31 Send Event',
      children: (
        <Card>
          <Title level={5}>State Transition</Title>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Space>
              <Text>Entity ID:</Text>
              <Input value={entityId} onChange={(e) => setEntityId(e.target.value)} style={{ width: 150 }} />
              <Button onClick={loadCurrentState}>Load State</Button>
            </Space>
            {currentState && (
              <Descriptions bordered size="small">
                <Descriptions.Item label="Current State"><Tag color="blue">{currentState.state}</Tag></Descriptions.Item>
                <Descriptions.Item label="Available Events">
                  {currentState.availableEvents?.map((e: string) => <Tag key={e} color="green">{e}</Tag>)}
                </Descriptions.Item>
              </Descriptions>
            )}
            <Space>
              <Select value={selectedEvent} onChange={setSelectedEvent} style={{ width: 200 }} placeholder="Select event">
                {currentState?.availableEvents?.map((e: string) => (
                  <Select.Option key={e} value={e}>{e}</Select.Option>
                ))}
              </Select>
              <Button type="primary" icon={<SendOutlined />} onClick={sendEvent} disabled={!selectedEvent}>
                Send Event
              </Button>
            </Space>
          </Space>
        </Card>
      ),
    },
    {
      key: '4',
      label: '#32 History',
      children: (
        <Card>
          <Title level={5}>State History</Title>
          <Button icon={<ReloadOutlined />} onClick={loadHistory} style={{ marginBottom: 16 }}>Refresh</Button>
          <Timeline mode="left">
            {history.map((h, idx) => (
              <Timeline.Item key={idx} color={h.success ? 'green' : 'red'}>
                <Text strong>{h.fromState}</Text> → <Text strong>{h.toState}</Text>
                <br />
                <Text type="secondary">Event: {h.event} | {h.timestampFormatted}</Text>
              </Timeline.Item>
            ))}
          </Timeline>
        </Card>
      ),
    },
    {
      key: '5',
      label: '#33~#34 Force State',
      children: (
        <Card>
          <Title level={5}>Force State Change</Title>
          <Text type="secondary">Directly set the state (bypass normal transitions).</Text>
          <div style={{ marginTop: 16 }}>
            {machineDefinition?.states?.map((s: string) => (
              <Button key={s} onClick={() => forceState(s)} style={{ marginRight: 8, marginBottom: 8 }}>
                Force to {s}
              </Button>
            ))}
          </div>
        </Card>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <ApartmentOutlined /> State Machine (#29~#34)
      </Title>
      <Tabs items={items} />
    </Spin>
  );
};

export default StateMachinePage;
