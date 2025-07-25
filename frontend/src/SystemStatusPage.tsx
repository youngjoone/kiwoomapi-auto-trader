import React, { useState, useEffect } from 'react';

interface SystemStatusData {
    accessTokenStatus: string;
    websocketConnectionStatus: string;
}

const SystemStatusPage: React.FC = () => {
    const [statusInfo, setStatusInfo] = useState<SystemStatusData | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchSystemStatus = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch('/api/v1/status');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data: SystemStatusData = await response.json();
            setStatusInfo(data);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchSystemStatus();
        const interval = setInterval(fetchSystemStatus, 5000); // 5초마다 새로고침
        return () => clearInterval(interval);
    }, []);

    if (loading) {
        return <div className="container mx-auto p-4">로딩 중...</div>;
    }

    if (error) {
        return <div className="container mx-auto p-4 text-red-500">에러: {error}</div>;
    }

    if (!statusInfo) {
        return <div className="container mx-auto p-4">시스템 상태 정보를 불러올 수 없습니다.</div>;
    }

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">시스템 상태</h1>
            <div className="bg-white p-6 rounded-lg shadow-md">
                <div className="mb-4">
                    <h2 className="text-lg font-semibold mb-2">액세스 토큰 상태</h2>
                    <p className={`text-xl font-bold ${statusInfo.accessTokenStatus === 'Valid' ? 'text-green-600' : 'text-red-600'}`}>
                        {statusInfo.accessTokenStatus}
                    </p>
                </div>
                <div className="mb-4">
                    <h2 className="text-lg font-semibold mb-2">웹소켓 연결 상태</h2>
                    <p className={`text-xl font-bold ${statusInfo.websocketConnectionStatus === 'Connected' ? 'text-green-600' : 'text-red-600'}`}>
                        {statusInfo.websocketConnectionStatus}
                    </p>
                </div>
                {/* TODO: 추가적인 시스템 상태 지표를 여기에 추가할 수 있습니다. */}
            </div>
        </div>
    );
};

export default SystemStatusPage;
