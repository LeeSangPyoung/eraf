import React, { useState, useEffect } from 'react';
import { Card, Button, Input, message, Typography, Tabs, Space, Tag, Spin, Form, Select, Row, Col, Descriptions } from 'antd';
import { ToolOutlined, LockOutlined, KeyOutlined, EyeInvisibleOutlined, CodeOutlined, CalendarOutlined, NumberOutlined } from '@ant-design/icons';
import { utilityApi } from '../../services/api';

const { Title, Text } = Typography;

const UtilityPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [cryptoResult, setCryptoResult] = useState<any>(null);
  const [hashResult, setHashResult] = useState<any>(null);
  const [passwordResult, setPasswordResult] = useState<any>(null);
  const [maskResult, setMaskResult] = useState<any>(null);
  const [maskExamples, setMaskExamples] = useState<any[]>([]);
  const [jsonResult, setJsonResult] = useState<any>(null);
  const [dateResult, setDateResult] = useState<any>(null);
  const [idResult, setIdResult] = useState<any>(null);

  const [plainText, setPlainText] = useState('Hello, ERAF!');
  const [encryptedText, setEncryptedText] = useState('');
  const [hashText, setHashText] = useState('password123');
  const [rawPassword, setRawPassword] = useState('password123');
  const [encodedPassword, setEncodedPassword] = useState('');
  const [verifyRaw, setVerifyRaw] = useState('');
  const [maskValue, setMaskValue] = useState('01012345678');
  const [maskPattern, setMaskPattern] = useState('PHONE');
  const [jsonInput, setJsonInput] = useState('{"name": "John", "age": 30}');
  const [dateInput, setDateInput] = useState('');
  const [dateFormat, setDateFormat] = useState('yyyy-MM-dd HH:mm:ss');
  const [idCount, setIdCount] = useState(5);

  useEffect(() => {
    loadMaskExamples();
    loadCurrentDate();
  }, []);

  const loadMaskExamples = async () => {
    try {
      const response = await utilityApi.getMaskExamples();
      setMaskExamples(response.data.examples || []);
    } catch (error) {
      console.error('Failed to load mask examples');
    }
  };

  const loadCurrentDate = async () => {
    try {
      const response = await utilityApi.getCurrentDate();
      setDateInput(response.data.iso);
    } catch (error) {
      console.error('Failed to load current date');
    }
  };

  const handleEncrypt = async () => {
    setLoading(true);
    try {
      const response = await utilityApi.encrypt(plainText);
      setCryptoResult({ type: 'encrypt', ...response.data });
      setEncryptedText(response.data.encryptedText);
      message.success('Encrypted successfully');
    } catch (error) {
      message.error('Encryption failed');
    } finally {
      setLoading(false);
    }
  };

  const handleDecrypt = async () => {
    if (!encryptedText) {
      message.warning('No encrypted text to decrypt');
      return;
    }
    setLoading(true);
    try {
      const response = await utilityApi.decrypt(encryptedText);
      setCryptoResult({ type: 'decrypt', ...response.data });
      message.success('Decrypted successfully');
    } catch (error) {
      message.error('Decryption failed');
    } finally {
      setLoading(false);
    }
  };

  const handleHash = async () => {
    setLoading(true);
    try {
      const response = await utilityApi.hash(hashText);
      setHashResult(response.data);
    } catch (error) {
      message.error('Hash failed');
    } finally {
      setLoading(false);
    }
  };

  const handleEncodePassword = async () => {
    setLoading(true);
    try {
      const response = await utilityApi.encodePassword(rawPassword);
      setPasswordResult({ type: 'encode', ...response.data });
      setEncodedPassword(response.data.encodedPassword);
    } catch (error) {
      message.error('Password encoding failed');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyPassword = async () => {
    if (!encodedPassword) {
      message.warning('No encoded password to verify against');
      return;
    }
    setLoading(true);
    try {
      const response = await utilityApi.verifyPassword(verifyRaw || rawPassword, encodedPassword);
      setPasswordResult({ type: 'verify', ...response.data });
    } catch (error) {
      message.error('Password verification failed');
    } finally {
      setLoading(false);
    }
  };

  const handleMask = async () => {
    setLoading(true);
    try {
      const response = await utilityApi.mask(maskValue, maskPattern);
      setMaskResult(response.data);
    } catch (error) {
      message.error('Masking failed');
    } finally {
      setLoading(false);
    }
  };

  const handleJsonParse = async () => {
    setLoading(true);
    try {
      const response = await utilityApi.jsonToObject(jsonInput);
      setJsonResult({ type: 'parse', ...response.data });
    } catch (error) {
      message.error('JSON parsing failed');
    } finally {
      setLoading(false);
    }
  };

  const handleFormatDate = async () => {
    setLoading(true);
    try {
      const response = await utilityApi.formatDate(dateInput, dateFormat);
      setDateResult(response.data);
    } catch (error) {
      message.error('Date formatting failed');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateIds = async () => {
    setLoading(true);
    try {
      const response = await utilityApi.generateIds(idCount);
      setIdResult({ type: 'uuid', ...response.data });
    } catch (error) {
      message.error('ID generation failed');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateSnowflake = async () => {
    setLoading(true);
    try {
      const response = await utilityApi.generateSnowflake();
      setIdResult({ type: 'snowflake', ...response.data });
    } catch (error) {
      message.error('Snowflake ID generation failed');
    } finally {
      setLoading(false);
    }
  };

  const items = [
    {
      key: '1',
      label: '#43 Encryption',
      children: (
        <Card>
          <Title level={5}><LockOutlined /> Encryption/Decryption</Title>
          <Text type="secondary">AES-256 symmetric encryption.</Text>
          <Form layout="vertical" style={{ marginTop: 16, maxWidth: 500 }}>
            <Form.Item label="Plain Text">
              <Input value={plainText} onChange={(e) => setPlainText(e.target.value)} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" onClick={handleEncrypt} loading={loading}>Encrypt</Button>
            </Form.Item>
            <Form.Item label="Encrypted Text">
              <Input.TextArea value={encryptedText} onChange={(e) => setEncryptedText(e.target.value)} rows={2} />
            </Form.Item>
            <Form.Item>
              <Button onClick={handleDecrypt} loading={loading}>Decrypt</Button>
            </Form.Item>
          </Form>
          {cryptoResult && (
            <Descriptions bordered size="small" style={{ marginTop: 16 }}>
              <Descriptions.Item label="Operation">{cryptoResult.type}</Descriptions.Item>
              {cryptoResult.type === 'encrypt' && (
                <Descriptions.Item label="Encrypted">{cryptoResult.encryptedText}</Descriptions.Item>
              )}
              {cryptoResult.type === 'decrypt' && (
                <Descriptions.Item label="Decrypted">{cryptoResult.plainText}</Descriptions.Item>
              )}
            </Descriptions>
          )}
        </Card>
      ),
    },
    {
      key: '2',
      label: '#44 Hash',
      children: (
        <Card>
          <Title level={5}><KeyOutlined /> Hashing</Title>
          <Text type="secondary">SHA-256 one-way hashing.</Text>
          <Form layout="vertical" style={{ marginTop: 16, maxWidth: 500 }}>
            <Form.Item label="Text to Hash">
              <Input value={hashText} onChange={(e) => setHashText(e.target.value)} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" onClick={handleHash} loading={loading}>Generate Hash</Button>
            </Form.Item>
          </Form>
          {hashResult && (
            <Descriptions bordered size="small" column={1} style={{ marginTop: 16 }}>
              <Descriptions.Item label="Original">{hashResult.original}</Descriptions.Item>
              <Descriptions.Item label="SHA-256 Hash">
                <Text code style={{ wordBreak: 'break-all' }}>{hashResult.hash}</Text>
              </Descriptions.Item>
            </Descriptions>
          )}
        </Card>
      ),
    },
    {
      key: '3',
      label: '#45 Password',
      children: (
        <Card>
          <Title level={5}><KeyOutlined /> Password Encoding</Title>
          <Text type="secondary">BCrypt password encoding and verification.</Text>
          <Row gutter={24} style={{ marginTop: 16 }}>
            <Col span={12}>
              <Form layout="vertical">
                <Form.Item label="Raw Password">
                  <Input value={rawPassword} onChange={(e) => setRawPassword(e.target.value)} />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" onClick={handleEncodePassword} loading={loading}>Encode Password</Button>
                </Form.Item>
              </Form>
            </Col>
            <Col span={12}>
              <Form layout="vertical">
                <Form.Item label="Password to Verify">
                  <Input value={verifyRaw} onChange={(e) => setVerifyRaw(e.target.value)} placeholder={rawPassword} />
                </Form.Item>
                <Form.Item label="Encoded Password">
                  <Input.TextArea value={encodedPassword} rows={2} readOnly />
                </Form.Item>
                <Form.Item>
                  <Button onClick={handleVerifyPassword} loading={loading} disabled={!encodedPassword}>
                    Verify Password
                  </Button>
                </Form.Item>
              </Form>
            </Col>
          </Row>
          {passwordResult && (
            <Descriptions bordered size="small" style={{ marginTop: 16 }}>
              <Descriptions.Item label="Operation">{passwordResult.type}</Descriptions.Item>
              {passwordResult.type === 'encode' && (
                <Descriptions.Item label="Encoded">{passwordResult.encodedPassword}</Descriptions.Item>
              )}
              {passwordResult.type === 'verify' && (
                <Descriptions.Item label="Match">
                  <Tag color={passwordResult.match ? 'green' : 'red'}>
                    {passwordResult.match ? 'MATCH' : 'NO MATCH'}
                  </Tag>
                </Descriptions.Item>
              )}
            </Descriptions>
          )}
        </Card>
      ),
    },
    {
      key: '4',
      label: '#46 Masking',
      children: (
        <Card>
          <Title level={5}><EyeInvisibleOutlined /> Data Masking</Title>
          <Text type="secondary">Mask sensitive data for display.</Text>
          <Form layout="vertical" style={{ marginTop: 16, maxWidth: 500 }}>
            <Form.Item label="Value">
              <Input value={maskValue} onChange={(e) => setMaskValue(e.target.value)} />
            </Form.Item>
            <Form.Item label="Pattern">
              <Select value={maskPattern} onChange={setMaskPattern}>
                <Select.Option value="PHONE">Phone</Select.Option>
                <Select.Option value="EMAIL">Email</Select.Option>
                <Select.Option value="SSN">SSN (주민번호)</Select.Option>
                <Select.Option value="CARD">Card Number</Select.Option>
                <Select.Option value="NAME">Name</Select.Option>
                <Select.Option value="ADDRESS">Address</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item>
              <Button type="primary" onClick={handleMask} loading={loading}>Apply Mask</Button>
            </Form.Item>
          </Form>
          {maskResult && (
            <Descriptions bordered size="small" style={{ marginTop: 16 }}>
              <Descriptions.Item label="Original">{maskResult.original}</Descriptions.Item>
              <Descriptions.Item label="Masked">{maskResult.masked}</Descriptions.Item>
              <Descriptions.Item label="Pattern">{maskResult.pattern}</Descriptions.Item>
            </Descriptions>
          )}
          {maskExamples.length > 0 && (
            <div style={{ marginTop: 16 }}>
              <Text strong>Examples:</Text>
              <div style={{ marginTop: 8 }}>
                {maskExamples.map((ex, idx) => (
                  <Tag key={idx} style={{ margin: 4 }}>
                    {ex.pattern}: {ex.original} → {ex.masked}
                  </Tag>
                ))}
              </div>
            </div>
          )}
        </Card>
      ),
    },
    {
      key: '5',
      label: '#47 JSON',
      children: (
        <Card>
          <Title level={5}><CodeOutlined /> JSON Utilities</Title>
          <Text type="secondary">JSON parsing and formatting.</Text>
          <Form layout="vertical" style={{ marginTop: 16, maxWidth: 600 }}>
            <Form.Item label="JSON Input">
              <Input.TextArea
                value={jsonInput}
                onChange={(e) => setJsonInput(e.target.value)}
                rows={4}
              />
            </Form.Item>
            <Form.Item>
              <Space>
                <Button type="primary" onClick={handleJsonParse} loading={loading}>Parse JSON</Button>
              </Space>
            </Form.Item>
          </Form>
          {jsonResult && (
            <div style={{ marginTop: 16 }}>
              <Text strong>Result:</Text>
              <pre style={{ background: '#f5f5f5', padding: 16, borderRadius: 8, marginTop: 8 }}>
                {JSON.stringify(jsonResult.parsed || jsonResult, null, 2)}
              </pre>
            </div>
          )}
        </Card>
      ),
    },
    {
      key: '6',
      label: '#48 Date',
      children: (
        <Card>
          <Title level={5}><CalendarOutlined /> Date Utilities</Title>
          <Text type="secondary">Date formatting and manipulation.</Text>
          <Form layout="vertical" style={{ marginTop: 16, maxWidth: 500 }}>
            <Form.Item label="Date (ISO format)">
              <Input value={dateInput} onChange={(e) => setDateInput(e.target.value)} />
            </Form.Item>
            <Form.Item label="Output Format">
              <Select value={dateFormat} onChange={setDateFormat}>
                <Select.Option value="yyyy-MM-dd HH:mm:ss">yyyy-MM-dd HH:mm:ss</Select.Option>
                <Select.Option value="yyyy-MM-dd">yyyy-MM-dd</Select.Option>
                <Select.Option value="MM/dd/yyyy">MM/dd/yyyy</Select.Option>
                <Select.Option value="dd-MMM-yyyy">dd-MMM-yyyy</Select.Option>
                <Select.Option value="yyyy년 MM월 dd일">yyyy년 MM월 dd일</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item>
              <Space>
                <Button type="primary" onClick={handleFormatDate} loading={loading}>Format Date</Button>
                <Button onClick={loadCurrentDate}>Get Current</Button>
              </Space>
            </Form.Item>
          </Form>
          {dateResult && (
            <Descriptions bordered size="small" style={{ marginTop: 16 }}>
              <Descriptions.Item label="Input">{dateResult.input}</Descriptions.Item>
              <Descriptions.Item label="Format">{dateResult.format}</Descriptions.Item>
              <Descriptions.Item label="Formatted">{dateResult.formatted}</Descriptions.Item>
            </Descriptions>
          )}
        </Card>
      ),
    },
    {
      key: '7',
      label: '#49 ID Generation',
      children: (
        <Card>
          <Title level={5}><NumberOutlined /> ID Generation</Title>
          <Text type="secondary">Generate unique identifiers.</Text>
          <Space style={{ marginTop: 16 }}>
            <Text>Count:</Text>
            <Select value={idCount} onChange={setIdCount} style={{ width: 80 }}>
              {[1, 5, 10, 20].map((n) => (
                <Select.Option key={n} value={n}>{n}</Select.Option>
              ))}
            </Select>
            <Button type="primary" onClick={handleGenerateIds} loading={loading}>Generate UUIDs</Button>
            <Button onClick={handleGenerateSnowflake} loading={loading}>Generate Snowflake ID</Button>
          </Space>
          {idResult && (
            <div style={{ marginTop: 16 }}>
              <Text strong>Generated IDs ({idResult.type}):</Text>
              <div style={{ marginTop: 8, background: '#f5f5f5', padding: 16, borderRadius: 8 }}>
                {idResult.ids?.map((id: string, idx: number) => (
                  <div key={idx}><Text code>{id}</Text></div>
                ))}
                {idResult.id && <Text code>{idResult.id}</Text>}
              </div>
            </div>
          )}
        </Card>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <Title level={3}>
        <ToolOutlined /> Utilities (#43~#49)
      </Title>
      <Tabs items={items} />
    </Spin>
  );
};

export default UtilityPage;
