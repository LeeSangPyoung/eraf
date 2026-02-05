import React, { useState, useEffect } from 'react';
import { Card, Button, Input, Table, message, Typography, Tabs, Space, Tag, Spin, Form, Select, List } from 'antd';
import { BellOutlined, MailOutlined, MessageOutlined, MobileOutlined, ReloadOutlined, SendOutlined, CloudServerOutlined } from '@ant-design/icons';
import { notificationApi } from '../../services/api';

const { Title, Text } = Typography;

const NotificationPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [history, setHistory] = useState<any[]>([]);
  const [kafkaMessages, setKafkaMessages] = useState<any[]>([]);
  const [kafkaSubscriptions, setKafkaSubscriptions] = useState<string[]>([]);
  const [historyType, setHistoryType] = useState('ALL');

  // Email form
  const [emailTo, setEmailTo] = useState('test@example.com');
  const [emailSubject, setEmailSubject] = useState('Test Email');
  const [emailBody, setEmailBody] = useState('This is a test email from ERAF.');
  const [emailHtml, setEmailHtml] = useState(false);

  // SMS form
  const [smsTo, setSmsTo] = useState('01012345678');
  const [smsMessage, setSmsMessage] = useState('Test SMS from ERAF');

  // Push form
  const [pushToken, setPushToken] = useState('device-token-123');
  const [pushTitle, setPushTitle] = useState('Test Push');
  const [pushBody, setPushBody] = useState('This is a test push notification');
  const [pushPlatform, setPushPlatform] = useState('FCM');

  // Kafka form
  const [kafkaTopic, setKafkaTopic] = useState('eraf-events');
  const [kafkaEventType, setKafkaEventType] = useState('ORDER_CREATED');
  const [kafkaKey, setKafkaKey] = useState('order-001');
  const [kafkaPayload, setKafkaPayload] = useState('{"orderId": "ORD-001", "amount": 10000}');
  const [subscribeTopic, setSubscribeTopic] = useState('eraf-events');

  const loadHistory = async () => {
    setLoading(true);
    try {
      const response = await notificationApi.getHistory(historyType, 50);
      // Backend returns 'records' field
      setHistory(response.data.records || []);
    } catch (error) {
      console.error('Failed to load history');
    } finally {
      setLoading(false);
    }
  };

  const loadKafkaMessages = async () => {
    try {
      const response = await notificationApi.getKafkaMessages();
      setKafkaMessages(response.data.messages || []);
    } catch (error) {
      console.error('Failed to load Kafka messages');
    }
  };

  const loadKafkaSubscriptions = async () => {
    try {
      const response = await notificationApi.getKafkaSubscriptions();
      // Backend returns subscriptions as a Map (topic -> messageCount)
      const subs = response.data.subscriptions || {};
      setKafkaSubscriptions(Object.keys(subs));
    } catch (error) {
      console.error('Failed to load subscriptions');
    }
  };

  useEffect(() => {
    loadHistory();
    loadKafkaMessages();
    loadKafkaSubscriptions();
  }, []);

  useEffect(() => {
    loadHistory();
  }, [historyType]);

  const sendEmail = async () => {
    setLoading(true);
    try {
      await notificationApi.sendEmail([emailTo], emailSubject, emailBody, emailHtml);
      message.success('Email sent');
      loadHistory();
    } catch (error) {
      message.error('Failed to send email');
    } finally {
      setLoading(false);
    }
  };

  const sendSms = async () => {
    setLoading(true);
    try {
      await notificationApi.sendSms(smsTo, smsMessage);
      message.success('SMS sent');
      loadHistory();
    } catch (error) {
      message.error('Failed to send SMS');
    } finally {
      setLoading(false);
    }
  };

  const sendPush = async () => {
    setLoading(true);
    try {
      await notificationApi.sendPush([pushToken], pushTitle, pushBody, pushPlatform);
      message.success('Push notification sent');
      loadHistory();
    } catch (error) {
      message.error('Failed to send push');
    } finally {
      setLoading(false);
    }
  };

  const publishKafka = async () => {
    setLoading(true);
    try {
      let payload = {};
      try {
        payload = JSON.parse(kafkaPayload);
      } catch {
        message.error('Invalid JSON payload');
        setLoading(false);
        return;
      }
      await notificationApi.publishKafka(kafkaTopic, kafkaEventType, kafkaKey, payload);
      message.success('Message published to Kafka');
      loadKafkaMessages();
    } catch (error) {
      message.error('Failed to publish');
    } finally {
      setLoading(false);
    }
  };

  const subscribeToTopic = async () => {
    setLoading(true);
    try {
      await notificationApi.subscribeKafka(subscribeTopic);
      message.success(`Subscribed to ${subscribeTopic}`);
      loadKafkaSubscriptions();
    } catch (error) {
      message.error('Failed to subscribe');
    } finally {
      setLoading(false);
    }
  };

  const clearHistory = async () => {
    try {
      await notificationApi.clearHistory();
      message.success('History cleared');
      loadHistory();
    } catch (error) {
      message.error('Failed to clear history');
    }
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'EMAIL': return 'blue';
      case 'SMS': return 'green';
      case 'PUSH': return 'orange';
      case 'KAFKA': return 'purple';
      default: return 'default';
    }
  };

  const items = [
    {
      key: '1',
      label: '#50 Email',
      children: (
        <Card>
          <Title level={5}><MailOutlined /> Email Notification</Title>
          <Form layout="vertical" style={{ maxWidth: 500 }}>
            <Form.Item label="To">
              <Input value={emailTo} onChange={(e) => setEmailTo(e.target.value)} />
            </Form.Item>
            <Form.Item label="Subject">
              <Input value={emailSubject} onChange={(e) => setEmailSubject(e.target.value)} />
            </Form.Item>
            <Form.Item label="Body">
              <Input.TextArea value={emailBody} onChange={(e) => setEmailBody(e.target.value)} rows={4} />
            </Form.Item>
            <Form.Item label="Format">
              <Select value={emailHtml ? 'html' : 'text'} onChange={(v) => setEmailHtml(v === 'html')}>
                <Select.Option value="text">Plain Text</Select.Option>
                <Select.Option value="html">HTML</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item>
              <Button type="primary" icon={<SendOutlined />} onClick={sendEmail} loading={loading}>
                Send Email
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: '2',
      label: '#51 SMS',
      children: (
        <Card>
          <Title level={5}><MessageOutlined /> SMS Notification</Title>
          <Form layout="vertical" style={{ maxWidth: 500 }}>
            <Form.Item label="Phone Number">
              <Input value={smsTo} onChange={(e) => setSmsTo(e.target.value)} />
            </Form.Item>
            <Form.Item label="Message">
              <Input.TextArea value={smsMessage} onChange={(e) => setSmsMessage(e.target.value)} rows={3} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" icon={<SendOutlined />} onClick={sendSms} loading={loading}>
                Send SMS
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: '3',
      label: '#52 Push',
      children: (
        <Card>
          <Title level={5}><MobileOutlined /> Push Notification</Title>
          <Form layout="vertical" style={{ maxWidth: 500 }}>
            <Form.Item label="Device Token">
              <Input value={pushToken} onChange={(e) => setPushToken(e.target.value)} />
            </Form.Item>
            <Form.Item label="Platform">
              <Select value={pushPlatform} onChange={setPushPlatform}>
                <Select.Option value="FCM">FCM (Android)</Select.Option>
                <Select.Option value="APNS">APNS (iOS)</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item label="Title">
              <Input value={pushTitle} onChange={(e) => setPushTitle(e.target.value)} />
            </Form.Item>
            <Form.Item label="Body">
              <Input.TextArea value={pushBody} onChange={(e) => setPushBody(e.target.value)} rows={3} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" icon={<SendOutlined />} onClick={sendPush} loading={loading}>
                Send Push
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: '4',
      label: '#53~#54 Kafka',
      children: (
        <Card>
          <Title level={5}><CloudServerOutlined /> Kafka Messaging</Title>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Card size="small" title="Publish Message">
              <Form layout="vertical" style={{ maxWidth: 500 }}>
                <Form.Item label="Topic">
                  <Input value={kafkaTopic} onChange={(e) => setKafkaTopic(e.target.value)} />
                </Form.Item>
                <Form.Item label="Event Type">
                  <Select value={kafkaEventType} onChange={setKafkaEventType}>
                    <Select.Option value="ORDER_CREATED">ORDER_CREATED</Select.Option>
                    <Select.Option value="ORDER_UPDATED">ORDER_UPDATED</Select.Option>
                    <Select.Option value="USER_REGISTERED">USER_REGISTERED</Select.Option>
                    <Select.Option value="PAYMENT_COMPLETED">PAYMENT_COMPLETED</Select.Option>
                  </Select>
                </Form.Item>
                <Form.Item label="Key">
                  <Input value={kafkaKey} onChange={(e) => setKafkaKey(e.target.value)} />
                </Form.Item>
                <Form.Item label="Payload (JSON)">
                  <Input.TextArea value={kafkaPayload} onChange={(e) => setKafkaPayload(e.target.value)} rows={3} />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" icon={<SendOutlined />} onClick={publishKafka} loading={loading}>
                    Publish
                  </Button>
                </Form.Item>
              </Form>
            </Card>
            <Card size="small" title="Subscribe">
              <Space>
                <Input value={subscribeTopic} onChange={(e) => setSubscribeTopic(e.target.value)} placeholder="Topic" />
                <Button onClick={subscribeToTopic} loading={loading}>Subscribe</Button>
              </Space>
              <div style={{ marginTop: 8 }}>
                <Text strong>Active Subscriptions: </Text>
                {kafkaSubscriptions.map((s) => <Tag key={s} color="blue">{s}</Tag>)}
              </div>
            </Card>
            <Card size="small" title="Messages">
              <Button icon={<ReloadOutlined />} onClick={loadKafkaMessages} style={{ marginBottom: 8 }}>Refresh</Button>
              <List
                size="small"
                dataSource={kafkaMessages.slice(0, 10)}
                renderItem={(item: any) => (
                  <List.Item>
                    <Tag color="purple">{item.topic}</Tag>
                    <Tag>{item.eventType}</Tag>
                    <Text type="secondary">{item.key}: </Text>
                    <Text code>{JSON.stringify(item.payload)}</Text>
                  </List.Item>
                )}
              />
            </Card>
          </Space>
        </Card>
      ),
    },
    {
      key: '5',
      label: '#55 History',
      children: (
        <Card>
          <Title level={5}><BellOutlined /> Notification History</Title>
          <Space style={{ marginBottom: 16 }}>
            <Select value={historyType} onChange={setHistoryType} style={{ width: 120 }}>
              <Select.Option value="ALL">All</Select.Option>
              <Select.Option value="EMAIL">Email</Select.Option>
              <Select.Option value="SMS">SMS</Select.Option>
              <Select.Option value="PUSH">Push</Select.Option>
            </Select>
            <Button icon={<ReloadOutlined />} onClick={loadHistory}>Refresh</Button>
            <Button danger onClick={clearHistory}>Clear</Button>
          </Space>
          <Table
            dataSource={history}
            rowKey="id"
            size="small"
            columns={[
              { title: 'Type', dataIndex: 'type', key: 'type', render: (t: string) => <Tag color={getTypeColor(t)}>{t}</Tag> },
              { title: 'Recipient', dataIndex: 'recipient', key: 'recipient' },
              { title: 'Subject/Title', dataIndex: 'title', key: 'title', ellipsis: true },
              { title: 'Status', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={s === 'SUCCESS' ? 'green' : 'red'}>{s}</Tag> },
              { title: 'Time', dataIndex: 'sentAtFormatted', key: 'sentAt' },
            ]}
          />
        </Card>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <BellOutlined /> Notifications (#50~#55)
      </Title>
      <Tabs items={items} />
    </Spin>
  );
};

export default NotificationPage;
