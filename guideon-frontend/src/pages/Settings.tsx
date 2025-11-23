import { useEffect, useState } from 'react';
import { Card, Form, Input, InputNumber, Button, message, Typography, Spin } from 'antd';
import { SaveOutlined } from '@ant-design/icons';
import { useSettingsStore } from '@/stores/settingsStore';
import { settingsService } from '@/services/settingsService';
import { NotificationModal } from '@/components/common/NotificationModal';
import type { AppSettings } from '@/types';

const { Title, Paragraph } = Typography;

export const Settings = () => {
  const settings = useSettingsStore();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalType, setModalType] = useState<'success' | 'error' | 'warning'>('success');
  const [modalMessage, setModalMessage] = useState('');

  // 마운트 시 설정 조회
  useEffect(() => {
    const loadSettings = async () => {
      try {
        setInitialLoading(true);
        const serverSettings = await settingsService.getSettings();

        // 서버에서 받은 설정으로 form 초기화
        form.setFieldsValue({
          apiKey: serverSettings.apiKey || '',
          searchModel: serverSettings.searchModel,
          embeddingModel: serverSettings.embeddingModel,
          chunkSize: serverSettings.chunkSize,
          chunkOverlap: serverSettings.chunkOverlap,
        });

        // 로컬 스토어도 업데이트
        settings.updateSettings(serverSettings);
      } catch (error) {
        message.error(error instanceof Error ? error.message : '설정을 불러오는 중 오류가 발생했습니다.');
        // 오류 발생 시 로컬 스토어의 기본값 사용
        form.setFieldsValue({
          apiKey: settings.apiKey || '',
          searchModel: settings.searchModel,
          embeddingModel: settings.embeddingModel,
          chunkSize: settings.chunkSize,
          chunkOverlap: settings.chunkOverlap,
        });
      } finally {
        setInitialLoading(false);
      }
    };

    loadSettings();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSave = async (values: AppSettings) => {
    try {
      setLoading(true);

      // 백엔드에 설정 저장
      const updatedSettings = await settingsService.updateSettings(values);

      // 로컬 스토어도 업데이트
      settings.updateSettings(updatedSettings);

      // 성공 모달 표시
      setModalType('success');
      setModalMessage('설정이 성공적으로 저장되었습니다.');
      setModalOpen(true);
    } catch (error) {
      // 실패 모달 표시
      setModalType('error');
      setModalMessage(error instanceof Error ? error.message : '설정 저장 중 오류가 발생했습니다.');
      setModalOpen(true);
    } finally {
      setLoading(false);
    }
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
        {initialLoading ? (
          <div style={{ textAlign: 'center', padding: '40px 0' }}>
            <Spin size="large" />
            <div style={{ marginTop: 16 }}>설정을 불러오는 중...</div>
          </div>
        ) : (
          <Form
            form={form}
            layout="vertical"
            onFinish={handleSave}
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
              label="검색 모델 (Chat Model)"
              name="searchModel"
              rules={[{ required: true, message: '검색 모델을 입력해주세요.' }]}
              extra="질문 답변 생성에 사용되는 모델입니다. 예: gemini-2.5-flash, gemini-2.5-pro"
            >
              <Input placeholder="gemini-2.5-flash" />
            </Form.Item>

            <Form.Item
              label="임베딩 모델 (Embedding Model)"
              name="embeddingModel"
              rules={[{ required: true, message: '임베딩 모델을 입력해주세요.' }]}
              extra="문서 벡터화에 사용되는 임베딩 모델입니다. 기본값: text-embedding-004"
            >
              <Input placeholder="text-embedding-004" />
            </Form.Item>

            <Form.Item
              label="임베딩 청크 글자 수"
              name="chunkSize"
              rules={[
                { required: true, message: '청크 글자 수를 입력해주세요.' },
                {
                  type: 'number',
                  min: 100,
                  max: 2000,
                  message: '100~2000 사이의 값을 입력해주세요.',
                },
              ]}
              extra="문서를 분할할 때 각 청크의 최대 글자 수입니다."
            >
              <InputNumber min={100} max={2000} style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item
              label="임베딩 오버랩 글자 수"
              name="chunkOverlap"
              rules={[
                { required: true, message: '오버랩 글자 수를 입력해주세요.' },
                {
                  type: 'number',
                  min: 0,
                  max: 500,
                  message: '0~500 사이의 값을 입력해주세요.',
                },
              ]}
              extra="인접한 청크 간 겹치는 글자 수입니다. 문맥 유지를 위해 사용됩니다."
            >
              <InputNumber min={0} max={500} style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item style={{ marginBottom: 0 }}>
              <Button
                type="primary"
                htmlType="submit"
                icon={<SaveOutlined />}
                size="large"
                loading={loading}
                style={{ marginRight: 8 }}
              >
                저장
              </Button>
              <Button size="large" onClick={handleReset} disabled={loading}>
                초기화
              </Button>
            </Form.Item>
          </Form>
        )}
      </Card>

      <NotificationModal
        open={modalOpen}
        type={modalType}
        title={modalType === 'success' ? '저장 완료' : '저장 실패'}
        message={modalMessage}
        onClose={() => setModalOpen(false)}
      />
    </div>
  );
};
