import { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, IdcardOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { authService } from '@/services/authService';

export const Register = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [isLoading, setIsLoading] = useState(false);

  const onFinish = async (values: {
    username: string;
    password: string;
    name: string;
    email: string;
  }) => {
    setIsLoading(true);
    try {
      await authService.register(values);
      message.success('회원가입이 완료되었습니다. 로그인해주세요.');
      navigate('/login');
    } catch (error) {
      message.error(error instanceof Error ? error.message : '회원가입에 실패했습니다.');
    } finally {
      setIsLoading(false);
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
          <h1 style={{ fontSize: 32, fontWeight: 'bold', margin: 0 }}>회원가입</h1>
          <p style={{ color: '#666', marginTop: 8 }}>새 계정을 생성합니다</p>
        </div>

        <Form form={form} name="register" onFinish={onFinish} autoComplete="off" size="large">
          <Form.Item name="username" rules={[{ required: true, message: '아이디를 입력해주세요.' }]}>
            <Input prefix={<UserOutlined />} placeholder="아이디" />
          </Form.Item>

          <Form.Item name="password" rules={[{ required: true, message: '비밀번호를 입력해주세요.' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="비밀번호" />
          </Form.Item>

          <Form.Item name="name" rules={[{ required: true, message: '이름을 입력해주세요.' }]}>
            <Input prefix={<IdcardOutlined />} placeholder="이름" />
          </Form.Item>

          <Form.Item name="email" rules={[{ required: true, message: '이메일을 입력해주세요.' }, { type: 'email', message: '유효한 이메일을 입력해주세요.' }]}>
            <Input prefix={<MailOutlined />} placeholder="이메일" />
          </Form.Item>

          <Form.Item style={{ marginTop: 24, marginBottom: 0 }}>
            <Button type="primary" htmlType="submit" loading={isLoading} block style={{ height: 48 }}>
              가입하기
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};


