import { useState } from 'react';
import { Form, Input, Button, Card, Checkbox, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';

export const Login = () => {
  const navigate = useNavigate();
  const { login, isLoading } = useAuthStore();
  const [form] = Form.useForm();

  const onFinish = async (values: {
    username: string;
    password: string;
    rememberMe?: boolean;
  }) => {
    try {
      await login(values.username, values.password, values.rememberMe);
      message.success('로그인에 성공했습니다.');
      navigate('/');
    } catch (error) {
      message.error(error instanceof Error ? error.message : '로그인에 실패했습니다.');
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      }}
    >
      <Card
        style={{
          width: 400,
          boxShadow: '0 10px 40px rgba(0,0,0,0.2)',
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <h1 style={{ fontSize: 32, fontWeight: 'bold', margin: 0 }}>Guideon</h1>
          <p style={{ color: '#666', marginTop: 8 }}>규정 Q&A 시스템</p>
        </div>

        <Form
          form={form}
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '아이디를 입력해주세요.' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="아이디"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '비밀번호를 입력해주세요.' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="비밀번호"
            />
          </Form.Item>

          <Form.Item name="rememberMe" valuePropName="checked" noStyle>
            <Checkbox>자동 로그인</Checkbox>
          </Form.Item>

          <Form.Item style={{ marginTop: 24, marginBottom: 0 }}>
            <Button
              type="primary"
              htmlType="submit"
              loading={isLoading}
              block
              style={{ height: 48 }}
            >
              로그인
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};
