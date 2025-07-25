import React, { useState, useEffect, useMemo } from 'react';

interface LogEntryData {
    id: number;
    timestamp: string;
    apiId: string;
    request: string;
    response: string;
    status: string;
    errorMessage: string;
}

type SortKey = keyof LogEntryData;
type SortOrder = 'asc' | 'desc';

const LogPage: React.FC = () => {
    const [allLogs, setAllLogs] = useState<LogEntryData[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState<string>('');
    const [filterStatus, setFilterStatus] = useState<string>('ALL');
    const [sortKey, setSortKey] = useState<SortKey>('timestamp');
    const [sortOrder, setSortOrder] = useState<SortOrder>('desc');
    const [currentPage, setCurrentPage] = useState<number>(1);
    const [itemsPerPage] = useState<number>(10); // 페이지당 10개 항목

    useEffect(() => {
        const fetchLogs = async () => {
            try {
                const response = await fetch('/api/v1/logs');
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const data: LogEntryData[] = await response.json();
                setAllLogs(data);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchLogs();
    }, []);

    // 검색, 필터링 및 정렬된 데이터 계산
    const filteredAndSortedLogs = useMemo(() => {
        let filtered = allLogs;

        // 검색 필터링
        if (searchTerm) {
            filtered = filtered.filter(log =>
                log.apiId.toLowerCase().includes(searchTerm.toLowerCase()) ||
                log.request.toLowerCase().includes(searchTerm.toLowerCase()) ||
                log.response.toLowerCase().includes(searchTerm.toLowerCase()) ||
                log.errorMessage?.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        // 상태 필터링
        if (filterStatus !== 'ALL') {
            filtered = filtered.filter(log => log.status === filterStatus);
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
    }, [allLogs, searchTerm, filterStatus, sortKey, sortOrder]);

    // 페이지네이션 계산
    const totalPages = Math.ceil(filteredAndSortedLogs.length / itemsPerPage);
    const currentItems = useMemo(() => {
        const startIndex = (currentPage - 1) * itemsPerPage;
        const endIndex = startIndex + itemsPerPage;
        return filteredAndSortedLogs.slice(startIndex, endIndex);
    }, [filteredAndSortedLogs, currentPage, itemsPerPage]);

    const handleSort = (key: SortKey) => {
        if (sortKey === key) {
            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
        } else {
            setSortKey(key);
            setSortOrder('desc'); // 로그는 최신순이 기본이므로 desc로 시작
        }
    };

    const handlePageChange = (pageNumber: number) => {
        setCurrentPage(pageNumber);
    };

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'SUCCESS':
                return 'text-green-600';
            case 'ERROR':
                return 'text-red-600';
            case 'WARN':
                return 'text-yellow-600';
            default:
                return 'text-gray-800';
        }
    };

    if (loading) {
        return <div className="container mx-auto p-4">로딩 중...</div>;
    }

    if (error) {
        return <div className="container mx-auto p-4 text-red-500">에러: {error}</div>;
    }

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">로그 기록</h1>

            {/* 검색 및 필터링 컨트롤 */}
            <div className="mb-4 flex space-x-4">
                <input
                    type="text"
                    placeholder="로그 검색... (API ID, 요청, 응답, 에러 메시지)"
                    className="p-2 border border-gray-300 rounded w-full"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
                <select
                    className="p-2 border border-gray-300 rounded"
                    value={filterStatus}
                    onChange={(e) => setFilterStatus(e.target.value)}
                >
                    <option value="ALL">모든 상태</option>
                    <option value="SUCCESS">SUCCESS</option>
                    <option value="ERROR">ERROR</option>
                    <option value="WARN">WARN</option>
                </select>
            </div>

            {currentItems.length === 0 ? (
                <p>로그 기록이 없습니다.</p>
            ) : (
                <div className="overflow-x-auto">
                    <table className="min-w-full bg-white border border-gray-300">
                        <thead>
                            <tr>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('id')}>
                                    ID {sortKey === 'id' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('timestamp')}>
                                    시간 {sortKey === 'timestamp' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('apiId')}>
                                    API ID {sortKey === 'apiId' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b">요청</th>
                                <th className="py-2 px-4 border-b">응답</th>
                                <th className="py-2 px-4 border-b cursor-pointer" onClick={() => handleSort('status')}>
                                    상태 {sortKey === 'status' && (sortOrder === 'asc' ? '▲' : '▼')}
                                </th>
                                <th className="py-2 px-4 border-b">에러 메시지</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentItems.map((logEntry) => (
                                <tr key={logEntry.id} className="hover:bg-gray-100">
                                    <td className="py-2 px-4 border-b">{logEntry.id}</td>
                                    <td className="py-2 px-4 border-b">{new Date(logEntry.timestamp).toLocaleString()}</td>
                                    <td className="py-2 px-4 border-b">{logEntry.apiId}</td>
                                    <td className="py-2 px-4 border-b text-sm break-all">{logEntry.request}</td>
                                    <td className="py-2 px-4 border-b text-sm break-all">{logEntry.response}</td>
                                    <td className={`py-2 px-4 border-b ${getStatusColor(logEntry.status)}`}>{logEntry.status}</td>
                                    <td className="py-2 px-4 border-b text-sm break-all">{logEntry.errorMessage}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

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
        </div>
    );
};

export default LogPage;