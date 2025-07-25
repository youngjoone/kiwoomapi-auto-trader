import React, { useState, useEffect } from 'react';
import { Bar } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';

ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend
);

interface DashboardInfoData {
    totalOwnedStocks: number;
    totalTrades: number;
    totalProfitLoss: number;
    lastStrategyRunTime: string;
}

const DashboardPage: React.FC = () => {
    const [dashboardInfo, setDashboardInfo] = useState<DashboardInfoData | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchDashboardInfo = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch('/api/v1/dashboard');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data: DashboardInfoData = await response.json();
            setDashboardInfo(data);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchDashboardInfo();
    }, []);

    // 총 손익 차트 데이터
    const profitLossChartData = {
        labels: ['총 손익'],
        datasets: [
            {
                label: '손익',
                data: [dashboardInfo?.totalProfitLoss || 0],
                backgroundColor: dashboardInfo && dashboardInfo.totalProfitLoss > 0 ? 'rgba(255, 99, 132, 0.5)' : 'rgba(53, 162, 235, 0.5)',
            },
        ],
    };

    const profitLossChartOptions = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top' as const,
            },
            title: {
                display: true,
                text: '총 손익 현황',
            },
        },
    };

    if (loading) {
        return <div className="container mx-auto p-4">로딩 중...</div>;
    }

    if (error) {
        return <div className="container mx-auto p-4 text-red-500">에러: {error}</div>;
    }

    if (!dashboardInfo) {
        return <div className="container mx-auto p-4">대시보드 정보를 불러올 수 없습니다.</div>;
    }

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">대시보드</h1>
            <button
                onClick={fetchDashboardInfo}
                disabled={loading}
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mb-4"
            >
                {loading ? '로딩 중...' : '새로고침'}
            </button>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div className="bg-white p-6 rounded-lg shadow-md">
                    <h2 className="text-lg font-semibold mb-2">총 보유 종목 수</h2>
                    <p className="text-3xl font-bold">{dashboardInfo.totalOwnedStocks.toLocaleString()}</p>
                </div>
                <div className="bg-white p-6 rounded-lg shadow-md">
                    <h2 className="text-lg font-semibold mb-2">총 거래 횟수</h2>
                    <p className="text-3xl font-bold">{dashboardInfo.totalTrades.toLocaleString()}</p>
                </div>
                <div className="bg-white p-6 rounded-lg shadow-md">
                    <h2 className="text-lg font-semibold mb-2">총 손익</h2>
                    <p className={`text-3xl font-bold ${dashboardInfo.totalProfitLoss > 0 ? 'text-red-500' : dashboardInfo.totalProfitLoss < 0 ? 'text-blue-500' : ''}`}>
                        {dashboardInfo.totalProfitLoss.toLocaleString()} 원
                    </p>
                </div>
                <div className="bg-white p-6 rounded-lg shadow-md lg:col-span-3">
                    <h2 className="text-lg font-semibold mb-2">마지막 전략 실행 시간</h2>
                    <p className="text-xl">{dashboardInfo.lastStrategyRunTime}</p>
                </div>

                {/* 총 손익 차트 추가 */}
                <div className="bg-white p-6 rounded-lg shadow-md lg:col-span-3">
                    <Bar options={profitLossChartOptions} data={profitLossChartData} />
                </div>
            </div>
        </div>
    );
};

export default DashboardPage;
