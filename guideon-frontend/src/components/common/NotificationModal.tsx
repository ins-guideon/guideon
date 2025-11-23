import { Modal, Space, Typography } from 'antd';
import { CheckCircleOutlined, CloseCircleOutlined, ExclamationCircleOutlined } from '@ant-design/icons';

const { Text } = Typography;

interface NotificationModalProps {
    open: boolean;
    type: 'success' | 'error' | 'warning';
    title?: string;
    message: string;
    onClose: () => void;
}

export const NotificationModal = ({ open, type, title, message, onClose }: NotificationModalProps) => {
    const getIcon = () => {
        switch (type) {
            case 'success':
                return <CheckCircleOutlined style={{ color: '#52c41a', fontSize: 24 }} />;
            case 'error':
                return <CloseCircleOutlined style={{ color: '#ff4d4f', fontSize: 24 }} />;
            case 'warning':
                return <ExclamationCircleOutlined style={{ color: '#faad14', fontSize: 24 }} />;
            default:
                return null;
        }
    };

    const getDefaultTitle = () => {
        switch (type) {
            case 'success':
                return '성공했습니다';
            case 'error':
                return '실패했습니다';
            case 'warning':
                return '경고';
            default:
                return '';
        }
    };

    return (
        <Modal
            open={open}
            onCancel={onClose}
            onOk={onClose}
            okText="확인"
            cancelButtonProps={{ style: { display: 'none' } }}
            width={400}
            centered
        >
            <Space direction="vertical" size="middle" style={{ width: '100%', textAlign: 'center', padding: '16px 0' }}>
                {getIcon()}
                <div>
                    <Text strong style={{ fontSize: 16, display: 'block', marginBottom: 8 }}>
                        {title || getDefaultTitle()}
                    </Text>
                    <Text style={{ fontSize: 14, color: '#666' }}>{message}</Text>
                </div>
            </Space>
        </Modal>
    );
};

