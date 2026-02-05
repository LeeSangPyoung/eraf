import React, { useState, useEffect } from 'react';
import { Card, Button, Input, Table, message, Typography, Tabs, Space, Tag, Spin, Form, InputNumber, List, Select } from 'antd';
import { DatabaseOutlined, ReloadOutlined, DeleteOutlined, PlusOutlined, CloudServerOutlined } from '@ant-design/icons';
import { cacheApi } from '../../services/api';

const { Title, Text } = Typography;

const CachePage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [caches, setCaches] = useState<any[]>([]);
  const [selectedCache, setSelectedCache] = useState<string>('');
  const [cacheEntries, setCacheEntries] = useState<any[]>([]);
  const [redisKeys, setRedisKeys] = useState<any[]>([]);
  const [pubsubChannels, setPubsubChannels] = useState<Record<string, number>>({});
  const [pubsubMessages, setPubsubMessages] = useState<any[]>([]);

  // Cache form
  const [cacheKey, setCacheKey] = useState('');
  const [cacheValue, setCacheValue] = useState('');
  const [cacheTtl, setCacheTtl] = useState<number | null>(null);

  // Redis form
  const [redisPattern, setRedisPattern] = useState('*');
  const [redisKey, setRedisKey] = useState('');
  const [redisValue, setRedisValue] = useState('');
  const [redisTtl, setRedisTtl] = useState<number | null>(null);
  const [selectedRedisKey, setSelectedRedisKey] = useState('');
  const [redisResult, setRedisResult] = useState<any>(null);

  // PubSub form
  const [subscribeChannel, setSubscribeChannel] = useState('notifications');
  const [publishChannel, setPublishChannel] = useState('notifications');
  const [publishMessage, setPublishMessage] = useState('Hello from ERAF!');

  const loadCaches = async () => {
    try {
      const response = await cacheApi.getCaches();
      setCaches(response.data.caches || []);
      if (response.data.caches?.length > 0 && !selectedCache) {
        setSelectedCache(response.data.caches[0].name);
      }
    } catch (error) {
      console.error('Failed to load caches');
    }
  };

  const loadCacheEntries = async (cacheName: string) => {
    if (!cacheName) return;
    setLoading(true);
    try {
      const response = await cacheApi.getCacheEntries(cacheName);
      setCacheEntries(response.data.entries || []);
    } catch (error) {
      console.error('Failed to load cache entries');
    } finally {
      setLoading(false);
    }
  };

  const loadRedisKeys = async () => {
    setLoading(true);
    try {
      const response = await cacheApi.getRedisKeys(redisPattern);
      setRedisKeys(response.data.keys || []);
    } catch (error) {
      console.error('Failed to load Redis keys');
    } finally {
      setLoading(false);
    }
  };

  const loadPubSubChannels = async () => {
    try {
      const response = await cacheApi.getPubSubChannels();
      setPubsubChannels(response.data.channels || {});
    } catch (error) {
      console.error('Failed to load channels');
    }
  };

  const loadPubSubMessages = async () => {
    try {
      const response = await cacheApi.getPubSubMessages();
      setPubsubMessages(response.data.messages || []);
    } catch (error) {
      console.error('Failed to load messages');
    }
  };

  useEffect(() => {
    loadCaches();
    loadRedisKeys();
    loadPubSubChannels();
    loadPubSubMessages();
  }, []);

  useEffect(() => {
    if (selectedCache) {
      loadCacheEntries(selectedCache);
    }
  }, [selectedCache]);

  const putCache = async () => {
    if (!selectedCache || !cacheKey || !cacheValue) {
      message.warning('Please fill in all fields');
      return;
    }
    setLoading(true);
    try {
      await cacheApi.putCache(selectedCache, cacheKey, cacheValue, cacheTtl || undefined);
      message.success('Cache entry added');
      loadCacheEntries(selectedCache);
      setCacheKey('');
      setCacheValue('');
    } catch (error) {
      message.error('Failed to add cache entry');
    } finally {
      setLoading(false);
    }
  };

  const evictCache = async (key: string) => {
    setLoading(true);
    try {
      await cacheApi.evictCache(selectedCache, key);
      message.success('Cache entry evicted');
      loadCacheEntries(selectedCache);
    } catch (error) {
      message.error('Failed to evict cache');
    } finally {
      setLoading(false);
    }
  };

  const clearCache = async () => {
    if (!selectedCache) return;
    setLoading(true);
    try {
      await cacheApi.clearCache(selectedCache);
      message.success('Cache cleared');
      loadCacheEntries(selectedCache);
    } catch (error) {
      message.error('Failed to clear cache');
    } finally {
      setLoading(false);
    }
  };

  const getRedisValue = async (key: string) => {
    setLoading(true);
    try {
      const response = await cacheApi.getRedisValue(key);
      setRedisResult(response.data);
      setSelectedRedisKey(key);
    } catch (error) {
      message.error('Failed to get Redis value');
    } finally {
      setLoading(false);
    }
  };

  const setRedis = async () => {
    if (!redisKey || !redisValue) {
      message.warning('Please fill in key and value');
      return;
    }
    setLoading(true);
    try {
      await cacheApi.setRedisValue(redisKey, redisValue, redisTtl || undefined);
      message.success('Redis value set');
      loadRedisKeys();
      setRedisKey('');
      setRedisValue('');
    } catch (error) {
      message.error('Failed to set Redis value');
    } finally {
      setLoading(false);
    }
  };

  const deleteRedis = async (key: string) => {
    setLoading(true);
    try {
      await cacheApi.deleteRedisKey(key);
      message.success('Redis key deleted');
      loadRedisKeys();
    } catch (error) {
      message.error('Failed to delete Redis key');
    } finally {
      setLoading(false);
    }
  };

  const subscribePubSub = async () => {
    if (!subscribeChannel) return;
    setLoading(true);
    try {
      await cacheApi.subscribePubSub(subscribeChannel);
      message.success(`Subscribed to ${subscribeChannel}`);
      loadPubSubChannels();
    } catch (error) {
      message.error('Failed to subscribe');
    } finally {
      setLoading(false);
    }
  };

  const publishPubSub = async () => {
    if (!publishChannel || !publishMessage) return;
    setLoading(true);
    try {
      await cacheApi.publishPubSub(publishChannel, publishMessage);
      message.success('Message published');
      loadPubSubMessages();
    } catch (error) {
      message.error('Failed to publish');
    } finally {
      setLoading(false);
    }
  };

  const clearPubSub = async () => {
    try {
      await cacheApi.clearPubSub();
      message.success('PubSub cleared');
      loadPubSubChannels();
      loadPubSubMessages();
    } catch (error) {
      message.error('Failed to clear');
    }
  };

  const items = [
    {
      key: '1',
      label: '#60 Cache Management',
      children: (
        <Card>
          <Title level={5}><DatabaseOutlined /> Local Cache</Title>
          <Space style={{ marginBottom: 16 }}>
            <Select value={selectedCache} onChange={setSelectedCache} style={{ width: 200 }}>
              {caches.map((c) => (
                <Select.Option key={c.name} value={c.name}>{c.name} ({c.entryCount} entries)</Select.Option>
              ))}
            </Select>
            <Button icon={<ReloadOutlined />} onClick={() => loadCacheEntries(selectedCache)}>Refresh</Button>
            <Button danger icon={<DeleteOutlined />} onClick={clearCache}>Clear Cache</Button>
          </Space>

          <Card size="small" title="Add Entry" style={{ marginBottom: 16 }}>
            <Space>
              <Input placeholder="Key" value={cacheKey} onChange={(e) => setCacheKey(e.target.value)} style={{ width: 150 }} />
              <Input placeholder="Value" value={cacheValue} onChange={(e) => setCacheValue(e.target.value)} style={{ width: 200 }} />
              <InputNumber placeholder="TTL (sec)" value={cacheTtl} onChange={(v) => setCacheTtl(v)} style={{ width: 100 }} />
              <Button type="primary" icon={<PlusOutlined />} onClick={putCache}>Add</Button>
            </Space>
          </Card>

          <Table
            dataSource={cacheEntries}
            rowKey="key"
            size="small"
            columns={[
              { title: 'Key', dataIndex: 'key', key: 'key' },
              { title: 'Value', dataIndex: 'value', key: 'value', ellipsis: true },
              { title: 'TTL', dataIndex: 'ttlSeconds', key: 'ttlSeconds', render: (t: number) => t > 0 ? `${t}s` : 'No expiry' },
              {
                title: 'Action',
                key: 'action',
                render: (_: any, record: any) => (
                  <Button size="small" danger onClick={() => evictCache(record.key)}>Evict</Button>
                ),
              },
            ]}
          />
        </Card>
      ),
    },
    {
      key: '2',
      label: '#61 Redis Operations',
      children: (
        <Card>
          <Title level={5}><CloudServerOutlined /> Redis Operations</Title>

          <Card size="small" title="Search Keys" style={{ marginBottom: 16 }}>
            <Space>
              <Input placeholder="Pattern" value={redisPattern} onChange={(e) => setRedisPattern(e.target.value)} style={{ width: 150 }} />
              <Button onClick={loadRedisKeys}>Search</Button>
            </Space>
          </Card>

          <Card size="small" title="Set Value" style={{ marginBottom: 16 }}>
            <Space wrap>
              <Input placeholder="Key" value={redisKey} onChange={(e) => setRedisKey(e.target.value)} style={{ width: 150 }} />
              <Input placeholder="Value" value={redisValue} onChange={(e) => setRedisValue(e.target.value)} style={{ width: 200 }} />
              <InputNumber placeholder="TTL (sec)" value={redisTtl} onChange={(v) => setRedisTtl(v)} style={{ width: 100 }} />
              <Button type="primary" onClick={setRedis}>Set</Button>
            </Space>
          </Card>

          <Card size="small" title="Keys">
            <List
              size="small"
              dataSource={redisKeys}
              renderItem={(item: any) => (
                <List.Item
                  actions={[
                    <Button size="small" onClick={() => getRedisValue(item.key)}>Get</Button>,
                    <Button size="small" danger onClick={() => deleteRedis(item.key)}>Delete</Button>,
                  ]}
                >
                  <Space>
                    <Text code>{item.key}</Text>
                    <Tag color={item.mock ? 'orange' : 'green'}>{item.mock ? 'Mock' : 'Redis'}</Tag>
                    <Text type="secondary">TTL: {item.ttl === -1 ? 'No expiry' : `${item.ttl}s`}</Text>
                  </Space>
                </List.Item>
              )}
            />
          </Card>

          {redisResult && (
            <Card size="small" title={`Value: ${selectedRedisKey}`} style={{ marginTop: 16 }}>
              <pre style={{ background: '#f5f5f5', padding: 8, borderRadius: 4 }}>
                {JSON.stringify(redisResult, null, 2)}
              </pre>
            </Card>
          )}
        </Card>
      ),
    },
    {
      key: '3',
      label: '#62~#63 Pub/Sub',
      children: (
        <Card>
          <Title level={5}><CloudServerOutlined /> Redis Pub/Sub</Title>

          <Card size="small" title="Subscribe" style={{ marginBottom: 16 }}>
            <Space>
              <Input placeholder="Channel" value={subscribeChannel} onChange={(e) => setSubscribeChannel(e.target.value)} style={{ width: 200 }} />
              <Button onClick={subscribePubSub}>Subscribe</Button>
            </Space>
            <div style={{ marginTop: 8 }}>
              <Text strong>Active Channels: </Text>
              {Object.entries(pubsubChannels).map(([channel, count]) => (
                <Tag key={channel} color="blue">{channel} ({count})</Tag>
              ))}
            </div>
          </Card>

          <Card size="small" title="Publish" style={{ marginBottom: 16 }}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Input placeholder="Channel" value={publishChannel} onChange={(e) => setPublishChannel(e.target.value)} />
              <Input.TextArea placeholder="Message" value={publishMessage} onChange={(e) => setPublishMessage(e.target.value)} rows={2} />
              <Button type="primary" onClick={publishPubSub}>Publish</Button>
            </Space>
          </Card>

          <Card size="small" title="Messages">
            <Space style={{ marginBottom: 8 }}>
              <Button icon={<ReloadOutlined />} onClick={loadPubSubMessages}>Refresh</Button>
              <Button danger onClick={clearPubSub}>Clear</Button>
            </Space>
            <List
              size="small"
              dataSource={pubsubMessages}
              renderItem={(msg: any) => (
                <List.Item>
                  <Tag color="blue">{msg.channel}</Tag>
                  <Text>{msg.message}</Text>
                  <Text type="secondary" style={{ marginLeft: 8 }}>{msg.timestampFormatted}</Text>
                </List.Item>
              )}
            />
          </Card>
        </Card>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <DatabaseOutlined /> Cache & Redis (#60~#63)
      </Title>
      <Tabs items={items} />
    </Spin>
  );
};

export default CachePage;
