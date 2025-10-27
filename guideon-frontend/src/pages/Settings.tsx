import { Card, Form, Input, Select, InputNumber, Switch, Button, message, Typography } from 'antd';
import { SaveOutlined } from '@ant-design/icons';
import { useSettingsStore } from '@/stores/settingsStore';

const { Title, Paragraph } = Typography;

export const Settings = () => {
  const settings = useSettingsStore();
  const [form] = Form.useForm();

  const handleSave = (values: typeof settings) => {
    settings.updateSettings(values);
    message.success('설정이 저장되었습니다.');
  };

  const handleReset = () => {
    settings.resetSettings();
    form.setFieldsValue(settings);
    message.info('설정이 초기화되었습니다.');
  };

  return (
    <div>
      <Title level={2}>설정</Title>
      <Paragraph type="secondary">
        애플리케이션 동작을 설정합니다.
      </Paragraph>

      <Card style={{ marginTop: 24, maxWidth: 800 }}>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSave}
          initialValues={{
            apiKey: settings.apiKey,
            model: settings.model,
            maxResults: settings.maxResults,
            minConfidence: settings.minConfidence,
            enableNotifications: settings.enableNotifications,
          }}
        >
          <Form.Item
            label="Google API Key"
            name="apiKey"
            rules={[
              {
                required: false,
                message: 'API 키를 입력해주세요.',
              },
            ]}
            extra="Google Gemini API 키를 입력하세요. 비워두면 서버 설정을 사용합니다."
          >
            <Input.Password placeholder="AIza..." />
          </Form.Item>

          <Form.Item
            label="AI 모델"
            name="model"
            rules={[{ required: true, message: '모델을 선택해주세요.' }]}
            extra="Flash는 빠른 응답, Pro는 높은 정확도를 제공합니다."
          >
            <Select
              options={[
                { label: 'Gemini 2.5 Flash (빠른 응답)', value: 'gemini-2.5-flash' },
                { label: 'Gemini 2.5 Pro (높은 정확도)', value: 'gemini-2.5-pro' },
              ]}
            />
          </Form.Item>

          <Form.Item
            label="최대 검색 결과 수"
            name="maxResults"
            rules={[
              { required: true, message: '검색 결과 수를 입력해주세요.' },
              {
                type: 'number',
                min: 1,
                max: 20,
                message: '1~20 사이의 값을 입력해주세요.',
              },
            ]}
            extra="답변 생성에 사용할 관련 규정 조항의 최대 개수입니다."
          >
            <InputNumber min={1} max={20} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            label="최소 신뢰도"
            name="minConfidence"
            rules={[
              { required: true, message: '최소 신뢰도를 입력해주세요.' },
              {
                type: 'number',
                min: 0,
                max: 1,
                message: '0~1 사이의 값을 입력해주세요.',
              },
            ]}
            extra="이 값보다 낮은 신뢰도의 답변은 경고 메시지를 표시합니다."
          >
            <InputNumber
              min={0}
              max={1}
              step={0.1}
              style={{ width: '100%' }}
            />
          </Form.Item>

          <Form.Item
            label="알림 활성화"
            name="enableNotifications"
            valuePropName="checked"
            extra="새로운 규정 업데이트나 중요한 알림을 받습니다."
          >
            <Switch />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0 }}>
            <Button
              type="primary"
              htmlType="submit"
              icon={<SaveOutlined />}
              size="large"
              style={{ marginRight: 8 }}
            >
              저장
            </Button>
            <Button size="large" onClick={handleReset}>
              초기화
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};
