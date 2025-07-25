import React, { useState, useEffect } from 'react';

interface TradeHistoryData {
    id: number;
    stockCode: string;
    stockName: string;
    buyPrice: number;
    buyQuantity: number;
    buyTimestamp: number;
    sellPrice: number;
    sellQuantity: number;
    sellTimestamp: number;
    profitLoss: number;
    profitLossPercentage: number;
}

const TradeHistoryPage: React.FC = () => {
    const [tradeHistory, setTradeHistory] = useState<TradeHistoryData[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchTradeHistory = async () => {
            try {
                const response = await fetch('/api/v1/trade-history');
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const data: TradeHistoryData[] = await response.json();
                setTradeHistory(data);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchTradeHistory();
    }, []);

    if (loading) {
        return <div className="container mx-auto p-4">로딩 중...</div>;
    }

    if (error) {
        return <div className="container mx-auto p-4 text-red-500">에러: {error}</div>;
    }

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">매매 이력</h1>
            {tradeHistory.length === 0 ? (
                <p>매매 이력이 없습니다.</p>
            ) : (
                <table className="min-w-full bg-white border border-gray-300">
                    <thead>
                        <tr>
                            <th className="py-2 px-4 border-b">종목코드</th>
                            <th className="py-2 px-4 border-b">종목명</th>
                            <th className="py-2 px-4 border-b">매수 가격</th>
                            <th className="py-2 px-4 border-b">매수 수량</th>
                            <th className="py-2 px-4 border-b">매수 시간</th>
                            <th className="py-2 px-4 border-b">매도 가격</th>
                            <th className="py-2 px-4 border-b">매도 수량</th>
                            <th className="py-2 px-4 border-b">매도 시간</th>
                            <th className="py-2 px-4 border-b">손익</th>
                            <th className="py-2 px-4 border-b">손익률 (%)</th>
                        </tr>
                    </thead>
                    <tbody>
                        {tradeHistory.map((trade) => (
                            <tr key={trade.id} className="hover:bg-gray-100">
                                <td className="py-2 px-4 border-b">{trade.stockCode}</td>
                                <td className="py-2 px-4 border-b">{trade.stockName || '알 수 없음'}</td>
                                <td className="py-2 px-4 border-b text-right">{trade.buyPrice.toLocaleString()}</td>
                                <td className="py-2 px-4 border-b text-right">{trade.buyQuantity.toLocaleString()}</td>
                                <td className="py-2 px-4 border-b">{new Date(trade.buyTimestamp).toLocaleString()}</td>
                                <td className="py-2 px-4 border-b text-right">{trade.sellPrice.toLocaleString()}</td>
                                <td className="py-2 px-4 border-b text-right">{trade.sellQuantity.toLocaleString()}</td>
                                <td className="py-2 px-4 border-b">{new Date(trade.sellTimestamp).toLocaleString()}</td>
                                <td className={`py-2 px-4 border-b text-right ${trade.profitLoss > 0 ? 'text-red-500' : trade.profitLoss < 0 ? 'text-blue-500' : ''}`}>{trade.profitLoss.toLocaleString()}</td>
                                <td className={`py-2 px-4 border-b text-right ${trade.profitLossPercentage > 0 ? 'text-red-500' : trade.profitLossPercentage < 0 ? 'text-blue-500' : ''}`}>{trade.profitLossPercentage.toFixed(2)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default TradeHistoryPage;
