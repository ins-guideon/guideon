import { useState } from 'react';
import {
  Card,
  Input,
  Button,
  Typography,
  Alert,
  Tag,
  Divider,
  Progress,
  List,
  Space,
  message,
} from 'antd';
import {
  SendOutlined,
  LikeOutlined,
  DislikeOutlined,
  CopyOutlined,
} from '@ant-design/icons';
import { useMutation } from '@tanstack/react-query';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { regulationService } from '@/services/regulationService';
import type { RegulationSearchResult } from '@/types';
import '@/styles/markdown.css';

const { Title, Paragraph, Text } = Typography;
const { TextArea } = Input;

export const QAPage = () => {
  const [question, setQuestion] = useState('');
  const [result, setResult] = useState<RegulationSearchResult | null>(null);

  const { mutate: askQuestion, isPending } = useMutation({
    mutationFn: (q: string) => regulationService.askQuestion(q),
    onSuccess: (data) => {
      setResult(data);
      message.success('답변을 생성했습니다.');
    },
    onError: (error) => {
      message.error(error instanceof Error ? error.message : '오류가 발생했습니다.');
    },
  });

  const handleSubmit = () => {
    if (!question.trim()) {
      message.warning('질문을 입력해주세요.');
      return;
    }
    askQuestion(question);
  };

  const handleCopyAnswer = () => {
    if (result) {
      navigator.clipboard.writeText(result.answer);
      message.success('답변이 클립보드에 복사되었습니다.');
    }
  };

  const handleRating = async (_rating: 'helpful' | 'not_helpful') => {
    try {
      // 답변 평가 API 호출 (historyId 필요)
      message.success('피드백이 저장되었습니다.');
    } catch (error) {
      message.error('피드백 저장에 실패했습니다.');
    }
  };

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ marginBottom: 32 }}>
        <Title level={2} style={{ marginBottom: 8 }}>질문하기</Title>
        <Paragraph type="secondary" style={{ fontSize: 15 }}>
          규정에 대해 궁금한 점을 자연어로 질문해보세요.
        </Paragraph>
      </div>

      {/* 질문 입력 */}
      <Card
        style={{
          marginBottom: 32,
          border: '1px solid #e8e8e8',
          borderRadius: 8,
        }}
        bodyStyle={{ padding: 24 }}
      >
        <TextArea
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          placeholder="예: 해외 출장시 숙박비는 얼마까지 지원되나요?"
          autoSize={{ minRows: 4, maxRows: 8 }}
          size="large"
          disabled={isPending}
          style={{ fontSize: 15 }}
        />
        <Button
          type="primary"
          size="large"
          icon={<SendOutlined />}
          onClick={handleSubmit}
          loading={isPending}
          style={{
            marginTop: 16,
            height: 48,
            fontSize: 16,
            fontWeight: 500,
          }}
          block
        >
          질문하기
        </Button>
      </Card>

      {/* 분석 결과 */}
      {result && (
        <>
          <Card
            title={<Text strong style={{ fontSize: 16 }}>질문 분석</Text>}
            style={{
              marginBottom: 24,
              border: '1px solid #e8e8e8',
              borderRadius: 8,
            }}
            bodyStyle={{ padding: 24 }}
          >
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <div>
                <Text strong style={{ fontSize: 14 }}>추출된 키워드:</Text>
                <div style={{ marginTop: 12 }}>
                  {result.analysis.keywords.map((keyword) => (
                    <Tag key={keyword} color="blue" style={{ fontSize: 13, padding: '4px 12px', marginBottom: 8 }}>
                      {keyword}
                    </Tag>
                  ))}
                </div>
              </div>

              <div>
                <Text strong style={{ fontSize: 14 }}>관련 규정 유형:</Text>
                <div style={{ marginTop: 12 }}>
                  {result.analysis.regulationTypes.map((type) => (
                    <Tag key={type} color="green" style={{ fontSize: 13, padding: '4px 12px', marginBottom: 8 }}>
                      {type}
                    </Tag>
                  ))}
                </div>
              </div>

              <div>
                <Text strong style={{ fontSize: 14 }}>질문 의도:</Text>
                <Tag color="purple" style={{ fontSize: 13, padding: '4px 12px', marginLeft: 8 }}>
                  {result.analysis.questionIntent}
                </Tag>
              </div>

              <div>
                <Text strong style={{ fontSize: 14, display: 'block', marginBottom: 12 }}>신뢰도:</Text>
                <Progress
                  percent={Math.round(result.confidenceScore * 100)}
                  status={
                    result.confidenceScore >= 0.8
                      ? 'success'
                      : result.confidenceScore >= 0.6
                      ? 'normal'
                      : 'exception'
                  }
                  strokeWidth={12}
                />
              </div>
            </Space>
          </Card>

          {/* 답변 */}
          <Card
            title={<Text strong style={{ fontSize: 16 }}>답변</Text>}
            style={{
              marginBottom: 24,
              border: '1px solid #e8e8e8',
              borderRadius: 8,
            }}
            bodyStyle={{ padding: 24 }}
            extra={
              <Button
                icon={<CopyOutlined />}
                onClick={handleCopyAnswer}
              >
                복사
              </Button>
            }
          >
            {result.confidenceScore < 0.6 && (
              <Alert
                message="신뢰도가 낮습니다"
                description="이 답변은 신뢰도가 낮을 수 있습니다. 반드시 원본 규정을 확인해주세요."
                type="warning"
                showIcon
                style={{ marginBottom: 20 }}
              />
            )}

            <div className="markdown-answer" style={{ fontSize: 15, lineHeight: 1.8, marginBottom: 24 }}>
              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {result.answer}
              </ReactMarkdown>
            </div>

            <Divider style={{ margin: '24px 0' }} />

            <div>
              <Text strong style={{ fontSize: 14, display: 'block', marginBottom: 12 }}>
                답변이 도움이 되었나요?
              </Text>
              <Space>
                <Button
                  icon={<LikeOutlined />}
                  onClick={() => handleRating('helpful')}
                  size="middle"
                >
                  도움됨
                </Button>
                <Button
                  icon={<DislikeOutlined />}
                  onClick={() => handleRating('not_helpful')}
                  size="middle"
                >
                  도움안됨
                </Button>
              </Space>
            </div>
          </Card>

          {/* 근거 규정 */}
          <Card
            title={<Text strong style={{ fontSize: 16 }}>근거 규정</Text>}
            style={{
              border: '1px solid #e8e8e8',
              borderRadius: 8,
            }}
            bodyStyle={{ padding: 24 }}
          >
            <List
              dataSource={result.references}
              renderItem={(ref, index) => (
                <List.Item style={{ padding: '20px 0' }}>
                  <List.Item.Meta
                    avatar={
                      <div
                        style={{
                          width: 48,
                          height: 48,
                          borderRadius: '50%',
                          background: '#1890ff',
                          color: 'white',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontWeight: 'bold',
                          fontSize: 18,
                        }}
                      >
                        {index + 1}
                      </div>
                    }
                    title={
                      <Space style={{ marginBottom: 8 }}>
                        <Text strong style={{ fontSize: 15 }}>{ref.documentName}</Text>
                        <Tag color="orange" style={{ fontSize: 13 }}>
                          관련도: {(ref.relevanceScore * 100).toFixed(0)}%
                        </Tag>
                      </Space>
                    }
                    description={
                      <Text style={{ fontSize: 14, lineHeight: 1.6 }}>{ref.content}</Text>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </>
      )}
    </div>
  );
};
