import React, { useState } from 'react';
import { Form, Input, Button, Typography, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { securityApi } from '../services/api';

const { Title } = Typography;

const LoginPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      const response = await securityApi.login(values.username, values.password);

      if (response.data.accessToken) {
        localStorage.setItem('accessToken', response.data.accessToken);
        if (response.data.refreshToken) {
          localStorage.setItem('refreshToken', response.data.refreshToken);
        }

        const userInfo = {
          username: response.data.username,
          userId: response.data.userId,
          roles: response.data.roles
        };
        localStorage.setItem('user', JSON.stringify(userInfo));

        navigate('/');
      } else {
        message.error('로그인 실패');
      }
    } catch (error: any) {
      console.error('Login error:', error);
      message.error('아이디 또는 비밀번호를 확인하세요');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex' }}>
      <div
        style={{
          flex: 1,
          background: '#001529',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          padding: '60px',
        }}
      >
        <div style={{ textAlign: 'center', color: '#fff' }}>
          <div
            style={{
              fontSize: 72,
              fontWeight: 800,
              letterSpacing: 8,
              marginBottom: 20,
              color: '#fff',
            }}
          >
            ERAF
          </div>
          <div
            style={{
              fontSize: 16,
              color: 'rgba(255,255,255,0.75)',
              letterSpacing: 2,
              marginBottom: 8,
            }}
          >
            Enterprise Reusable Asset Factory
          </div>
          <div
            style={{
              fontSize: 14,
              color: 'rgba(255,255,255,0.5)',
            }}
          >
            Framework Sample Application
          </div>
        </div>
      </div>
      <div
        style={{
          width: 480,
          background: '#fff',
          padding: '80px 60px',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
        }}
      >
        <div style={{ marginBottom: 48 }}>
          <Title level={3} style={{ marginBottom: 4 }}>
            로그인
          </Title>
          <div style={{ color: '#666', fontSize: 13 }}>
            ERAF Sample Application
          </div>
        </div>

        <Form name="login" onFinish={handleLogin} layout="vertical">
          <Form.Item
            label="아이디"
            name="username"
            rules={[{ required: true, message: '아이디를 입력하세요' }]}
          >
            <Input placeholder="아이디" />
          </Form.Item>

          <Form.Item
            label="비밀번호"
            name="password"
            rules={[{ required: true, message: '비밀번호를 입력하세요' }]}
          >
            <Input.Password placeholder="비밀번호" />
          </Form.Item>

          <Form.Item style={{ marginTop: 24 }}>
            <Button type="primary" htmlType="submit" loading={loading} block>
              로그인
            </Button>
          </Form.Item>
        </Form>

        <div style={{ marginTop: 16, fontSize: 12, color: '#999' }}>
          * 테스트 계정: admin / admin123
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
