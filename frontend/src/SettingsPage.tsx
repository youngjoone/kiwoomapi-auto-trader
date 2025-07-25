import React, { useState, useEffect } from 'react';

interface Settings {
    totalAmount: number;
    profitMargin: number;
    lossMargin: number;
}

const SettingsPage: React.FC = () => {
    const [settings, setSettings] = useState<Settings | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [isEditing, setIsEditing] = useState<boolean>(false);

    useEffect(() => {
        fetchSettings();
    }, []);

    const fetchSettings = async () => {
        setLoading(true); // 데이터 로드 시작 시 로딩 상태 설정
        setError(null); // 에러 초기화
        try {
            const response = await fetch('/api/v1/settings');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data: Settings = await response.json();
            setSettings(data);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false); // 데이터 로드 완료 시 로딩 상태 해제
        }
    };

    const handleSave = async () => {
        if (!settings) return;
        setLoading(true); // 저장 시작 시 로딩 상태 설정
        setError(null); // 에러 초기화
        try {
            const response = await fetch('/api/v1/settings', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(settings),
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data: Settings = await response.json();
            setSettings(data);
            setIsEditing(false);
            alert('설정이 저장되었습니다.');
        } catch (err: any) {
            setError(err.message);
            alert(`설정 저장 실패: ${err.message}`);
        } finally {
            setLoading(false); // 저장 완료 시 로딩 상태 해제
        }
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setSettings(prevSettings => ({
            ...prevSettings!,
            [name]: name === 'totalAmount' ? parseInt(value) : parseFloat(value),
        }));
    };

    if (loading) {
        return <div className="container mx-auto p-4">로딩 중...</div>;
    }

    if (error) {
        return <div className="container mx-auto p-4 text-red-500">에러: {error}</div>;
    }

    if (!settings) {
        return <div className="container mx-auto p-4">설정 데이터를 불러올 수 없습니다.</div>;
    }

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">전략 설정</h1>
            <div className="bg-white p-6 rounded-lg shadow-md">
                <div className="mb-4">
                    <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="totalAmount">
                        총 매수 금액 (원)
                    </label>
                    <input
                        type="number"
                        id="totalAmount"
                        name="totalAmount"
                        value={settings.totalAmount}
                        onChange={handleChange}
                        disabled={!isEditing}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                    />
                </div>
                <div className="mb-4">
                    <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="profitMargin">
                        익절 목표 (%) (예: 5.0)
                    </label>
                    <input
                        type="number"
                        id="profitMargin"
                        name="profitMargin"
                        step="0.1"
                        value={settings.profitMargin}
                        onChange={handleChange}
                        disabled={!isEditing}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                    />
                </div>
                <div className="mb-6">
                    <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="lossMargin">
                        손절 제한 (%) (예: -5.0)
                    </label>
                    <input
                        type="number"
                        id="lossMargin"
                        name="lossMargin"
                        step="0.1"
                        value={settings.lossMargin}
                        onChange={handleChange}
                        disabled={!isEditing}
                        className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                    />
                </div>
                <div className="flex items-center justify-between">
                    {!isEditing ? (
                        <button
                            onClick={() => setIsEditing(true)}
                            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                        >
                            수정
                        </button>
                    ) : (
                        <> 
                            <button
                                onClick={handleSave}
                                disabled={loading}
                                className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline mr-2"
                            >
                                {loading ? '저장 중...' : '저장'}
                            </button>
                            <button
                                onClick={() => {
                                    setIsEditing(false);
                                    fetchSettings(); // 변경사항 취소하고 원래 설정 불러오기
                                }}
                                disabled={loading}
                                className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                            >
                                취소
                            </button>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SettingsPage;