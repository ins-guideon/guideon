import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { useState } from 'react';
import { Card, Typography, Input, Space, Button, message } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { documentService } from '@/services/documentService';

const { Title, Paragraph, Text } = Typography;
const { TextArea } = Input;

interface RouteState {
    text?: string;
}

export const DocumentUploadDetail = () => {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { id } = useParams<{ id: string }>();
    const location = useLocation();
    const state = (location.state || {}) as RouteState;

    const [text, setText] = useState<string>(state.text || '');

    const { mutate: confirm, isPending } = useMutation({
        mutationFn: () => {
            if (!id) throw new Error('id가 없습니다.');
            if (!text.trim()) throw new Error('확정할 텍스트가 비어 있습니다.');
            return documentService.confirmEmbedding(id, text);
        },
        onSuccess: () => {
            message.success('문서가 성공적으로 확정되고 인덱싱되었습니다.');
            queryClient.invalidateQueries({ queryKey: ['documents'] });
            navigate('/documents');
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : '확정 처리 중 오류가 발생했습니다.');
        },
    });

    return (
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>
            <div style={{ marginBottom: 24 }}>
                <Title level={2} style={{ marginBottom: 8 }}>
                    텍스트 확인 및 확정
                </Title>
                <Paragraph type="secondary" style={{ fontSize: 15 }}>
                    아래 텍스트를 확인·수정한 뒤 확정 버튼을 눌러 임베딩 및 인덱싱을 완료하세요.
                </Paragraph>
            </div>

            <Card bodyStyle={{ padding: 24 }}>
                <Space direction="vertical" size="large" style={{ width: '100%' }}>
                    <div>
                        <Text strong style={{ display: 'block', marginBottom: 12 }}>
                            추출된 텍스트
                        </Text>
                        <TextArea
                            value={text}
                            onChange={(e) => setText(e.target.value)}
                            rows={20}
                            placeholder="추출된 텍스트가 표시됩니다."
                        />
                        <Text type="secondary" style={{ marginTop: 8, display: 'block' }}>
                            길이가 긴 경우 검수 후 개인정보 등 민감 정보를 제거해 주세요.
                        </Text>
                    </div>

                    <Space>
                        <Button onClick={() => navigate('/documents')}>취소</Button>
                        <Button type="primary" loading={isPending} onClick={() => confirm()}>
                            {isPending ? '확정 중...' : '확정 및 인덱싱'}
                        </Button>
                    </Space>
                </Space>
            </Card>
        </div>
    );
};


