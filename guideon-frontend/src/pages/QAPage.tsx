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
  Space,
  message,
} from 'antd';
import {
  SendOutlined,
  CopyOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import { useMutation } from '@tanstack/react-query';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { regulationService } from '@/services/regulationService';
import type { RegulationSearchResult } from '@/types';
import { DocumentDetailModal } from '@/components/common/DocumentDetailModal';
import '@/styles/markdown.css';

const { Title, Paragraph, Text } = Typography;
const { TextArea } = Input;

export const QAPage = () => {
  const [question, setQuestion] = useState('');
  const [result, setResult] = useState<RegulationSearchResult | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedDocumentId, setSelectedDocumentId] = useState<string | null>(null);

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


  const handleDocumentClick = (documentId: string | undefined) => {
    if (documentId) {
      setSelectedDocumentId(documentId);
      setModalVisible(true);
    } else {
      message.warning('문서 ID가 없습니다.');
    }
  };

  const handleModalClose = () => {
    setModalVisible(false);
    setSelectedDocumentId(null);
  };

  // 참조 문서 목록 (중복 제거)
  const uniqueReferences = result
    ? Array.from(
        new Map(
          result.references
            .filter((ref) => ref.documentName)
            .map((ref) => [ref.documentName, ref])
        ).values()
      )
    : [];

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

            {uniqueReferences.length > 0 && (
              <>
                <Divider style={{ margin: '24px 0' }} />
                <div>
                  <Text strong style={{ fontSize: 14, display: 'block', marginBottom: 12 }}>
                    참조한 문서:
                  </Text>
                  <div style={{ marginTop: 12 }}>
                    {uniqueReferences.map((ref, index) => (
                      <Tag
                        key={index}
                        icon={<FileTextOutlined />}
                        color="cyan"
                        style={{
                          fontSize: 13,
                          padding: '6px 12px',
                          marginBottom: 8,
                          cursor: ref.documentId ? 'pointer' : 'default',
                        }}
                        onClick={() => ref.documentId && handleDocumentClick(ref.documentId)}
                      >
                        {ref.documentName}
                      </Tag>
                    ))}
                  </div>
                </div>
              </>
            )}
          </Card>

          {/* 문서 상세 모달 */}
          <DocumentDetailModal
            open={modalVisible}
            onClose={handleModalClose}
            documentId={selectedDocumentId}
          />
        </>
      )}
    </div>
  );
};
