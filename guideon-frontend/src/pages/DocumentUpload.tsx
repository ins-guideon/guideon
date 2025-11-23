import { useState, useEffect } from 'react';
import {
  Card,
  Upload,
  Button,
  Select,
  Typography,
  Space,
  message,
  Progress,
  Input,
} from 'antd';
import {
  UploadOutlined,
} from '@ant-design/icons';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useLocation } from 'react-router-dom';
import { documentService } from '@/services/documentService';
import type { DocumentDetailResponse } from '@/types';
import { REGULATION_TYPES } from '@/types';
import type { UploadFile } from 'antd';
import { NotificationModal } from '@/components/common/NotificationModal';

const { Title, Paragraph, Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;

export const DocumentUpload = () => {
  const queryClient = useQueryClient();
  const location = useLocation();
  const [selectedType, setSelectedType] = useState<string>('');
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [extractedText, setExtractedText] = useState<string | null>(null);

  // 수정 모드 관련 state
  const [editMode, setEditMode] = useState<boolean>(false);
  const [editingDocumentId, setEditingDocumentId] = useState<string | null>(null);
  const [documentDetail, setDocumentDetail] = useState<DocumentDetailResponse | null>(null);

  // 알림 모달 관련 state
  const [notificationModal, setNotificationModal] = useState<{
    open: boolean;
    type: 'success' | 'error' | 'warning';
    message: string;
  }>({
    open: false,
    type: 'success',
    message: '',
  });

  // location.state에서 수정 모드 정보 확인 및 초기화
  useEffect(() => {
    const state = location.state as { editMode?: boolean; documentId?: string; documentDetail?: DocumentDetailResponse } | null;
    if (state?.editMode && state.documentId && state.documentDetail) {
      setEditMode(true);
      setEditingDocumentId(state.documentId);
      setDocumentDetail(state.documentDetail);
      // 기존 문서 정보로 폼 초기화
      setSelectedType(state.documentDetail.regulationType);
      setExtractedText(state.documentDetail.content);
    }
  }, [location.state]);

  // 텍스트 추출(프리뷰 단계)
  const { mutate: extractText, isPending: isExtracting } = useMutation({
    mutationFn: ({ file, type }: { file: File; type: string }) =>
      documentService.extractText(file, type),
    onSuccess: ({ text }) => {
      setNotificationModal({
        open: true,
        type: 'success',
        message: '텍스트를 추출했습니다. 아래에서 확인하세요.',
      });
      setExtractedText(text);
      // 파일은 업로드 시 필요하므로 유지
    },
    onError: (error) => {
      setNotificationModal({
        open: true,
        type: 'error',
        message: error instanceof Error ? error.message : '텍스트 추출 중 오류가 발생했습니다.',
      });
    },
  });

  // 문서 ID 기반 텍스트 추출(수정 모드용)
  const { mutate: extractTextFromDocumentId, isPending: isExtractingFromId } = useMutation({
    mutationFn: (documentId: string) => documentService.extractTextFromDocumentId(documentId),
    onSuccess: ({ text }) => {
      setNotificationModal({
        open: true,
        type: 'success',
        message: '텍스트를 재추출했습니다. 아래에서 확인하세요.',
      });
      setExtractedText(text);
    },
    onError: (error) => {
      setNotificationModal({
        open: true,
        type: 'error',
        message: error instanceof Error ? error.message : '텍스트 추출 중 오류가 발생했습니다.',
      });
    },
  });

  // 문서 업로드 및 저장
  const { mutate: uploadDocument, isPending: isUploading } = useMutation({
    mutationFn: () => {
      if (fileList.length === 0) throw new Error('파일을 선택해주세요.');
      if (!extractedText || !extractedText.trim()) throw new Error('텍스트가 비어 있습니다.');
      if (!selectedType) throw new Error('규정 유형을 선택해주세요.');

      const file = fileList[0].originFileObj as File;
      return documentService.uploadDocument(file, selectedType, extractedText);
    },
    onSuccess: () => {
      setNotificationModal({
        open: true,
        type: 'success',
        message: '문서가 성공적으로 업로드되고 인덱싱되었습니다.',
      });
      queryClient.invalidateQueries({ queryKey: ['documents'] });
      queryClient.invalidateQueries({ queryKey: ['documents-view'] });
      setExtractedText(null);
      setSelectedType('');
      setFileList([]);
      setEditMode(false);
      setEditingDocumentId(null);
    },
    onError: (error) => {
      setNotificationModal({
        open: true,
        type: 'error',
        message: error instanceof Error ? error.message : '문서 업로드 중 오류가 발생했습니다.',
      });
    },
  });

  // 문서 업데이트
  const { mutate: updateDocument, isPending: isUpdating } = useMutation({
    mutationFn: () => {
      if (!editingDocumentId) throw new Error('문서 ID가 없습니다.');
      if (!extractedText || !extractedText.trim()) throw new Error('텍스트가 비어 있습니다.');
      if (!selectedType) throw new Error('규정 유형을 선택해주세요.');

      const file = fileList.length > 0 ? (fileList[0].originFileObj as File) : null;
      return documentService.updateDocument(editingDocumentId, file, extractedText, selectedType);
    },
    onSuccess: () => {
      setNotificationModal({
        open: true,
        type: 'success',
        message: '문서가 성공적으로 업데이트되었습니다.',
      });
      queryClient.invalidateQueries({ queryKey: ['documents'] });
      queryClient.invalidateQueries({ queryKey: ['documents-view'] });
      setExtractedText(null);
      setSelectedType('');
      setFileList([]);
      setEditMode(false);
      setEditingDocumentId(null);
      setDocumentDetail(null);
    },
    onError: (error) => {
      setNotificationModal({
        open: true,
        type: 'error',
        message: error instanceof Error ? error.message : '문서 업데이트 중 오류가 발생했습니다.',
      });
    },
  });

  // 텍스트 다듬기
  const { mutate: cleanText, isPending: isCleaning } = useMutation({
    mutationFn: ({ text, fileExtension }: { text: string; fileExtension?: string }) =>
      documentService.cleanText(text, fileExtension),
    onSuccess: ({ text }) => {
      setNotificationModal({
        open: true,
        type: 'success',
        message: '텍스트를 다듬었습니다.',
      });
      setExtractedText(text);
    },
    onError: () => {
      setNotificationModal({
        open: true,
        type: 'error',
        message: '다듬기에 실패했습니다.',
      });
    },
  });

  const handleCancel = () => {
    setExtractedText(null);
    setFileList([]);
    setSelectedType('');
    setEditMode(false);
    setEditingDocumentId(null);
    setDocumentDetail(null);
  };

  // 파일 확장자 추출 헬퍼 함수
  const getFileExtension = (): string | undefined => {
    // 파일이 있는 경우
    if (fileList.length > 0 && fileList[0].originFileObj) {
      const fileName = fileList[0].originFileObj.name;
      const lastDotIndex = fileName.lastIndexOf('.');
      if (lastDotIndex > 0 && lastDotIndex < fileName.length - 1) {
        return fileName.substring(lastDotIndex + 1).toLowerCase();
      }
    }

    // 수정 모드이고 파일이 없는 경우 - 기존 문서의 파일명에서 추출
    if (editMode && !fileList.length && documentDetail?.fileName) {
      const fileName = documentDetail.fileName;
      const lastDotIndex = fileName.lastIndexOf('.');
      if (lastDotIndex > 0 && lastDotIndex < fileName.length - 1) {
        return fileName.substring(lastDotIndex + 1).toLowerCase();
      }
    }

    return undefined;
  };

  const handleCleanText = () => {
    if (!extractedText || !extractedText.trim()) {
      message.warning('다듬을 텍스트가 없습니다.');
      return;
    }

    const fileExtension = getFileExtension();
    cleanText({ text: extractedText, fileExtension });
  };


  const handleExtractText = () => {
    if (!selectedType) {
      message.warning('규정 유형을 선택해주세요.');
      return;
    }

    // 수정 모드: 문서 ID로 텍스트 재추출
    if (editMode && editingDocumentId) {
      extractTextFromDocumentId(editingDocumentId);
      return;
    }

    // 일반 모드: 파일이 있을 때만 텍스트 추출
    if (fileList.length === 0) {
      message.warning('업로드할 파일을 선택해주세요.');
      return;
    }

    if (fileList.length > 0) {
      const file = fileList[0].originFileObj as File;
      extractText({ file, type: selectedType });
    }
  };

  const beforeUpload = (file: File) => {
    const isValidType =
      file.type === 'application/pdf' ||
      file.type === 'application/msword' ||
      file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' ||
      file.type === 'text/plain';

    if (!isValidType) {
      message.error('PDF, DOC, DOCX, TXT 파일만 업로드 가능합니다.');
      return false;
    }

    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isLt10M) {
      message.error('파일 크기는 10MB를 초과할 수 없습니다.');
      return false;
    }

    return false; // 자동 업로드 방지
  };


  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ marginBottom: 32 }}>
        <Title level={2} style={{ marginBottom: 8 }}>
          {editMode ? '문서 업로드 (수정)' : '문서 업로드'}
        </Title>
        <Paragraph type="secondary" style={{ fontSize: 15 }}>
          {editMode
            ? '문서 내용을 수정하고 벡터 데이터베이스를 업데이트합니다.'
            : 'PDF, DOC, DOCX, TXT 파일을 업로드하여 벡터 데이터베이스에 인덱싱합니다.'}
        </Paragraph>
      </div>

      {/* 업로드 섹션 */}
      <Card
        title={<Text strong style={{ fontSize: 16 }}>파일 업로드</Text>}
        style={{
          marginBottom: 32,
          border: '1px solid #e8e8e8',
          borderRadius: 8,
        }}
        bodyStyle={{ padding: 24 }}
      >
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div>
            <Text strong style={{ display: 'block', marginBottom: 12 }}>
              규정 유형 선택 <span style={{ color: '#ff4d4f' }}>*</span>
            </Text>
            <Select
              style={{ width: '100%' }}
              placeholder="규정 유형을 선택하세요"
              value={selectedType || undefined}
              onChange={setSelectedType}
              size="large"
              showSearch
              filterOption={(input, option) =>
                (() => {
                  const child = option?.children;
                  const text = typeof child === 'string' ? child : '';
                  return text.toLowerCase().includes(input.toLowerCase());
                })()
              }
            >
              {REGULATION_TYPES.map((type) => (
                <Option key={type} value={type}>
                  {type}
                </Option>
              ))}
            </Select>
          </div>

          <div>
            <Text strong style={{ display: 'block', marginBottom: 12 }}>
              파일 선택 {!editMode && <span style={{ color: '#ff4d4f' }}>*</span>}
              {editMode && <span style={{ color: '#999', fontSize: 12, marginLeft: 8 }}>(선택사항 - 파일을 선택하지 않으면 기존 파일이 유지됩니다)</span>}
            </Text>
            <Upload
              fileList={fileList}
              onChange={({ fileList: newFileList }) => setFileList(newFileList)}
              beforeUpload={beforeUpload}
              maxCount={1}
              accept=".pdf,.doc,.docx,.txt"
            >
              <Button icon={<UploadOutlined />} size="large">
                파일 선택
              </Button>
            </Upload>
            <Text type="secondary" style={{ display: 'block', marginTop: 8 }}>
              지원 형식: PDF, DOC, DOCX, TXT (최대 10MB)
            </Text>
          </div>

          <Button
            type="primary"
            size="large"
            onClick={handleExtractText}
            loading={isExtracting || isExtractingFromId}
            disabled={
              (!editMode && fileList.length === 0) ||
              !selectedType ||
              (editMode && !editingDocumentId)
            }
            style={{
              height: 48,
              fontSize: 16,
              fontWeight: 500,
            }}
            block
          >
            {isExtracting || isExtractingFromId
              ? '텍스트 추출 중...'
              : editMode
                ? '텍스트 재추출'
                : '업로드 후 텍스트 추출'}
          </Button>

          {(isExtracting || isExtractingFromId) && (
            <div>
              <Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>
                문서를 파싱하여 본문 텍스트를 추출하는 중입니다...
              </Text>
              <Progress percent={100} status="active" showInfo={false} />
            </div>
          )}
        </Space>
      </Card>

      {/* 추출된 텍스트 확인 섹션 (수정 모드이거나 텍스트가 있을 때) */}
      {(extractedText || editMode) && (
        <Card
          title={<Text strong style={{ fontSize: 16 }}>추출된 텍스트 확인</Text>}
          style={{
            marginBottom: 32,
            border: '1px solid #e8e8e8',
            borderRadius: 8,
          }}
          bodyStyle={{ padding: 24 }}
        >
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <div>
              <Text strong style={{ display: 'block', marginBottom: 12 }}>
                추출된 텍스트
              </Text>
              <TextArea
                value={extractedText || ''}
                onChange={(e) => setExtractedText(e.target.value)}
                rows={20}
                placeholder="추출된 텍스트가 표시됩니다."
              />
              <Text type="secondary" style={{ marginTop: 8, display: 'block', whiteSpace: 'pre-wrap' }}>
                길이가 긴 경우 검수 후 개인정보 등 민감 정보를 제거해 주세요.
              </Text>
            </div>

            <Space>
              <Button onClick={handleCancel}>취소</Button>
              <Button
                onClick={handleCleanText}
                loading={isCleaning}
                disabled={!extractedText || !extractedText.trim()}
              >
                {isCleaning ? '다듬는 중...' : '다듬기'}
              </Button>
              {editMode ? (
                <Button
                  type="primary"
                  loading={isUpdating}
                  onClick={() => updateDocument()}
                >
                  {isUpdating ? '업데이트 중...' : '업데이트 및 저장'}
                </Button>
              ) : (
                <Button
                  type="primary"
                  loading={isUploading}
                  onClick={() => uploadDocument()}
                >
                  {isUploading ? '업로드 중...' : '업로드 및 저장'}
                </Button>
              )}
            </Space>
          </Space>
        </Card>
      )}

      <NotificationModal
        open={notificationModal.open}
        type={notificationModal.type}
        message={notificationModal.message}
        onClose={() => setNotificationModal({ ...notificationModal, open: false })}
      />
    </div>
  );
};
