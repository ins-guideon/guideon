import { useState } from 'react';
import {
    Card,
    Table,
    Typography,
    Space,
    Tag,
    message,
    Button,
    Popconfirm,
} from 'antd';
import {
    FileTextOutlined,
    FolderOutlined,
    UploadOutlined,
    DeleteOutlined,
    EditOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate, useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { documentService } from '@/services/documentService';
import type { DocumentInfo } from '@/types';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { DocumentDetailModal } from '@/components/common/DocumentDetailModal';
import { NotificationModal } from '@/components/common/NotificationModal';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

export const DocumentManagement = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const queryClient = useQueryClient();
    const [selectedDocumentId, setSelectedDocumentId] = useState<string | null>(null);
    const [modalVisible, setModalVisible] = useState(false);

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

    // 업로드 성공 시 모달 표시
    useEffect(() => {
        const state = location.state as { uploadSuccess?: boolean } | null;
        if (state?.uploadSuccess) {
            setNotificationModal({
                open: true,
                type: 'success',
                message: '문서가 성공적으로 업로드되고 인덱싱되었습니다.',
            });
            // state를 초기화하여 뒤로가기 시 다시 모달이 표시되지 않도록 함
            navigate(location.pathname, { replace: true, state: {} });
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [location.state]);

    // 문서 목록 조회 - 페이지가 열릴 때마다 항상 최신 데이터를 가져옴
    const { data: documentList, isLoading } = useQuery({
        queryKey: ['documents-view'],
        queryFn: () => documentService.getDocumentsForView(),
        refetchOnMount: true, // 컴포넌트 마운트 시 항상 재요청
        staleTime: 0, // 데이터를 즉시 stale로 표시하여 항상 재요청
    });

    // 문서 삭제
    const { mutate: deleteDocument } = useMutation({
        mutationFn: (id: string) => documentService.deleteDocument(id),
        onSuccess: () => {
            setNotificationModal({
                open: true,
                type: 'success',
                message: '문서가 삭제되었습니다.',
            });
            queryClient.invalidateQueries({ queryKey: ['documents-view'] });
        },
        onError: (error) => {
            setNotificationModal({
                open: true,
                type: 'error',
                message: error instanceof Error ? error.message : '삭제 중 오류가 발생했습니다.',
            });
        },
    });

    const handleFileNameClick = (record: DocumentInfo) => {
        setSelectedDocumentId(record.id);
        setModalVisible(true);
    };

    const handleCloseModal = () => {
        setModalVisible(false);
        setSelectedDocumentId(null);
    };

    const handleNewDocumentUpload = () => {
        navigate('/documents');
    };

    const handleEditDocument = async (record: DocumentInfo) => {
        try {
            const detail = await documentService.getDocumentDetail(record.id);
            // 수정 모드로 문서 업로드 페이지로 이동
            navigate('/documents', {
                state: {
                    editMode: true,
                    documentId: record.id,
                    documentDetail: detail,
                },
            });
        } catch (error) {
            message.error('문서 정보를 불러오는 중 오류가 발생했습니다.');
        }
    };

    const columns = [
        {
            title: '파일명',
            dataIndex: 'fileName',
            key: 'fileName',
            render: (text: string, record: DocumentInfo) => (
                <a
                    onClick={() => handleFileNameClick(record)}
                    style={{ cursor: 'pointer', color: '#1890ff' }}
                >
                    <FileTextOutlined style={{ marginRight: 8 }} />
                    {text}
                </a>
            ),
        },
        {
            title: '규정 유형',
            dataIndex: 'regulationType',
            key: 'regulationType',
            render: (type: string) => (
                <Tag color="blue">{type}</Tag>
            ),
        },
        {
            title: '업로드 시간',
            dataIndex: 'uploadTimestamp',
            key: 'uploadTimestamp',
            render: (timestamp: number) => dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss'),
            sorter: (a: DocumentInfo, b: DocumentInfo) => a.uploadTimestamp - b.uploadTimestamp,
        },
        {
            title: '파일 크기',
            dataIndex: 'fileSize',
            key: 'fileSize',
            render: (size: number) => {
                if (size < 1024) return `${size} B`;
                if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`;
                return `${(size / (1024 * 1024)).toFixed(2)} MB`;
            },
        },
        {
            title: '상태',
            dataIndex: 'status',
            key: 'status',
            render: (status: string) => {
                const statusConfig: Record<string, { color: string; text: string }> = {
                    indexed: { color: 'success', text: '인덱싱 완료' },
                    pending: { color: 'processing', text: '대기 중' },
                    error: { color: 'error', text: '오류' },
                };
                const config = statusConfig[status] || { color: 'default', text: status };
                return <Tag color={config.color}>{config.text}</Tag>;
            },
        },
        {
            title: '작업',
            key: 'action',
            render: (_: unknown, record: DocumentInfo) => (
                <Space>
                    <Button
                        type="link"
                        icon={<EditOutlined />}
                        onClick={() => handleEditDocument(record)}
                    >
                        수정
                    </Button>
                    <Popconfirm
                        title="문서를 삭제하시겠습니까?"
                        description="삭제된 문서는 복구할 수 없습니다."
                        onConfirm={() => deleteDocument(record.id)}
                        okText="삭제"
                        cancelText="취소"
                    >
                        <Button type="link" danger icon={<DeleteOutlined />}>
                            삭제
                        </Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    if (isLoading) {
        return <LoadingSpinner />;
    }

    return (
        <div style={{ padding: 24 }}>
            <Card
                style={{
                    border: '1px solid #e8e8e8',
                    borderRadius: 8,
                }}
            >
                <Space direction="vertical" size="large" style={{ width: '100%' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div>
                            <Title level={2} style={{ marginBottom: 8 }}>
                                <FolderOutlined style={{ marginRight: 12 }} />
                                문서 관리
                            </Title>
                            <Text type="secondary">
                                업로드된 문서 목록을 조회하고 관리할 수 있습니다.
                            </Text>
                        </div>
                        <Button
                            type="default"
                            icon={<UploadOutlined />}
                            size="middle"
                            onClick={handleNewDocumentUpload}
                        >
                            새 문서 업로드
                        </Button>
                    </div>

                    <Table
                        columns={columns}
                        dataSource={documentList?.documents || []}
                        rowKey="id"
                        pagination={{
                            pageSize: 10,
                            showSizeChanger: true,
                            showTotal: (total) => `총 ${total}건`,
                        }}
                        style={{ marginTop: 24 }}
                    />
                </Space>
            </Card>

            <DocumentDetailModal
                open={modalVisible}
                onClose={handleCloseModal}
                documentId={selectedDocumentId}
            />

            <NotificationModal
                open={notificationModal.open}
                type={notificationModal.type}
                message={notificationModal.message}
                onClose={() => setNotificationModal({ ...notificationModal, open: false })}
            />
        </div>
    );
};

