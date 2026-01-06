import { useState } from 'react';
import { Form, Input, Button, Card, message, Row, Col } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, IdcardOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { authService } from '@/services/authService';

export const Register = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [isLoading, setIsLoading] = useState(false);
  const [isEmailSending, setIsEmailSending] = useState(false);
  const [isVerifying, setIsVerifying] = useState(false);
  const [isEmailVerified, setIsEmailVerified] = useState(false);
  const [verificationSent, setVerificationSent] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const onFinish = async (values: {
    username: string;
    password: string;
    name: string;
    email: string;
  }) => {
    if (!isEmailVerified) {
      messageApi.error('이메일 인증이 필요합니다.');
      return;
    }

    setIsLoading(true);
    try {
      await authService.register(values);
      messageApi.success('회원가입이 완료되었습니다. 로그인해주세요.');
      navigate('/login');
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '회원가입에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleRequestVerification = async () => {
    try {
      const email = form.getFieldValue('email');
      if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        messageApi.error('유효한 이메일을 입력해주세요.');
        return;
      }

      setIsEmailSending(true);
      await authService.requestEmailVerification(email);
      messageApi.success('인증번호가 발송되었습니다.');
      setVerificationSent(true);
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '인증번호 발송에 실패했습니다.');
    } finally {
      setIsEmailSending(false);
    }
  };

  const handleVerifyCode = async () => {
    try {
      const email = form.getFieldValue('email');
      const code = form.getFieldValue('verificationCode');

      if (!code) {
        messageApi.error('인증번호를 입력해주세요.');
        return;
      }

      setIsVerifying(true);
      await authService.verifyEmailCode(email, code);
      messageApi.success('이메일 인증에 성공했습니다.');
      setIsEmailVerified(true);
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '인증번호가 올바르지 않습니다.');
    } finally {
      setIsVerifying(false);
    }
  };

  return (
    <>
      {contextHolder}
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
          width: 450,
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

          <Form.Item
            name="password"
            rules={[
              { required: true, message: '비밀번호를 입력해주세요.' },
              { min: 6, message: '비밀번호는 최소 6자 이상이어야 합니다.' },
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="비밀번호" />
          </Form.Item>

          <Form.Item name="name" rules={[{ required: true, message: '이름을 입력해주세요.' }]}>
            <Input prefix={<IdcardOutlined />} placeholder="이름" />
          </Form.Item>

          <div style={{ marginBottom: 24 }}>
            <Row gutter={8}>
              <Col span={17}>
                <Form.Item
                  name="email"
                  rules={[
                    { required: true, message: '이메일을 입력해주세요.' },
                    { type: 'email', message: '유효한 이메일을 입력해주세요.' }
                  ]}
                  style={{ marginBottom: 0 }}
                >
                  <Input prefix={<MailOutlined />} placeholder="이메일" disabled={isEmailVerified} />
                </Form.Item>
              </Col>
              <Col span={7}>
                <Button 
                  onClick={handleRequestVerification} 
                  loading={isEmailSending} 
                  disabled={isEmailVerified}
                  style={{ width: '100%' }}
                >
                  {verificationSent ? '재발송' : '인증요청'}
                </Button>
              </Col>
            </Row>
          </div>

          {verificationSent && !isEmailVerified && (
            <div style={{ marginBottom: 24 }}>
              <Row gutter={8}>
                <Col span={17}>
                  <Form.Item
                    name="verificationCode"
                    rules={[{ required: true, message: '인증번호를 입력해주세요.' }]}
                    style={{ marginBottom: 0 }}
                  >
                    <Input prefix={<SafetyCertificateOutlined />} placeholder="인증번호 6자리" />
                  </Form.Item>
                </Col>
                <Col span={7}>
                  <Button 
                    type="primary"
                    onClick={handleVerifyCode} 
                    loading={isVerifying}
                    style={{ width: '100%' }}
                  >
                    확인
                  </Button>
                </Col>
              </Row>
            </div>
          )}

          {isEmailVerified && (
            <p style={{ color: '#52c41a', marginBottom: 24, textAlign: 'center' }}>
              ✓ 이메일 인증이 완료되었습니다.
            </p>
          )}

          <Form.Item style={{ marginTop: 24, marginBottom: 0 }}>
            <Button 
              type="primary" 
              htmlType="submit" 
              loading={isLoading} 
              block 
              style={{ height: 48 }}
              disabled={!isEmailVerified}
            >
              가입하기
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
    </>
  );
};


