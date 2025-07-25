import React, { useState, useEffect, useMemo } from 'react';

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

type SortKey = keyof TradeHistoryData;
type SortOrder = 'asc' | 'desc';

const TradeHistoryPage: React.FC = () => {
    const [allTradeHistory, setAllTradeHistory] = useState<TradeHistoryData[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState<string>('');
    const [sortKey, setSortKey] = useState<SortKey>('buyTimestamp');
    const [sortOrder, setSortOrder] = useState<SortOrder>('desc');
    const [currentPage, setCurrentPage] = useState<number>(1);
    const [itemsPerPage] = useState<number>(10); // 페이지당 10개 항목

    useEffect(() => {
        const fetchTradeHistory = async () => {
            try {
                const response = await fetch('/api/v1/trade-history');
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const data: TradeHistoryData[] = await response.json();
                setAllTradeHistory(data);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchTradeHistory();
    }, []);

    // 검색 및 정렬된 데이터 계산
    const filteredAndSortedHistory = useMemo(() => {
        let filtered = allTradeHistory;

        // 검색 필터링
        if (searchTerm) {
            filtered = allTradeHistory.filter(trade =>
                trade.stockCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
                trade.stockName.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        // 정렬
        const sorted = [...filtered].sort((a, b) => {
            const aValue = a[sortKey];
            const bValue = b[sortKey];

            if (typeof aValue === 'number' && typeof bValue === 'number') {
                return sortOrder === 'asc' ? aValue - bValue : bValue - aValue;
            }
            if (typeof aValue === 'string' && typeof bValue === 'string') {
                return sortOrder === 'asc' ? aValue.localeCompare(bValue) : bValue.localeCompare(aValue);
            }
            return 0;
        });
        return sorted;
    }, [allTradeHistory, searchTerm, sortKey, sortOrder]);

    // 페이지네이션 계산
    const totalPages = Math.ceil(filteredAndSortedHistory.length / itemsPerPage);
    const currentItems = useMemo(() => {
        const startIndex = (currentPage - 1) * itemsPerPage;
        const endIndex = startIndex + itemsPerPage;
        return filteredAndSortedHistory.slice(startIndex, endIndex);
    }, [filteredAndSortedHistory, currentPage, itemsPerPage]);

    const handleSort = (key: SortKey) => {
        if (sortKey === key) {
            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
        } else {
            setSortKey(key);
            setSortOrder('asc');
        }
    };

    const handlePageChange = (pageNumber: number) => {
        setCurrentPage(pageNumber);
    };

    if (loading) {
        return <div className="container mx-auto p-4">로딩 중...</div>;
    }

    if (error) {
        return <div className="container mx-auto p-4 text-red-500">에러: {error}</div>;
    }

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">매매 이력</h1>

            {/* 검색 입력 필드 */}
            <div className="mb-4">
                <input
                    type="text"
                    placeholder="종목코드 또는 종목명 검색..."
                    className="p-2 border border-gray-300 rounded w-full"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
            </div>

            {currentItems.length === 0 ? (
                <p>매매 이력이 없습니다.</p>
            ) : (
                <>
                    <table className="min-w-full bg-white border border-gray-300">
                        <thead>
                            <tr>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('stockCode')}>
                                    종목코드 {sortKey === 'stockCode' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('stockName')}>
                                    종목명 {sortKey === 'stockName' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('buyPrice')}>
                                    매수 가격 {sortKey === 'buyPrice' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('buyQuantity')}>
                                    매수 수량 {sortKey === 'buyQuantity' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('buyTimestamp')}>
                                    매수 시간 {sortKey === 'buyTimestamp' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('sellPrice')}>
                                    매도 가격 {sortKey === 'sellPrice' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('sellQuantity')}>
                                    매도 수량 {sortKey === 'sellQuantity' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('sellTimestamp')}>
                                    매도 시간 {sortKey === 'sellTimestamp' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('profitLoss')}>
                                    손익 {sortKey === 'profitLoss' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('profitLossPercentage')}>
                                    손익률 (%) {sortKey === 'profitLossPercentage' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentItems.map((trade) => (
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

                    {/* 페이지네이션 컨트롤 */}
                    <div className="flex justify-center mt-4">
                        {Array.from({ length: totalPages }, (_, i) => (
                            <button
                                key={i + 1}
                                onClick={() => handlePageChange(i + 1)}
                                className={`mx-1 px-3 py-1 rounded ${currentPage === i + 1 ? 'bg-blue-500 text-white' : 'bg-gray-200'}`}
                            >
                                {i + 1}
                            </button>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
};

export default TradeHistoryPage;