import React, { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import * as Stomp from 'stompjs';

interface HoldingInfoData {
    stockCode: string;
    stockName: string;
    currentPrice: number;
    buyPrice: number;
    quantity: number;
    profitLoss: number;
    profitLossPercentage: number;
}

const HoldingsPage: React.FC = () => {
    const [holdings, setHoldings] = useState<HoldingInfoData[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [stompClient, setStompClient] = useState<Stomp.Client | null>(null);

    useEffect(() => {
        // 초기 데이터 로드 (REST API)
        const fetchHoldings = async () => {
            try {
                const response = await fetch('/api/v1/holdings');
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const data: HoldingInfoData[] = await response.json();
                setHoldings(data);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchHoldings();

        // WebSocket 연결 및 구독
        const socket = new SockJS('http://localhost:8080/ws'); // 백엔드 WebSocket 엔드포인트의 절대 경로
        const client = Stomp.over(socket);

        client.connect({}, () => {
            console.log('Connected to WebSocket');
            setStompClient(client);

            client.subscribe('/topic/holdings', (message) => {
                const updatedHolding: HoldingInfoData = JSON.parse(message.body);
                setHoldings(prevHoldings => {
                    const existingIndex = prevHoldings.findIndex(h => h.stockCode === updatedHolding.stockCode);
                    if (existingIndex > -1) {
                        // 기존 종목 업데이트
                        const newHoldings = [...prevHoldings];
                        newHoldings[existingIndex] = updatedHolding;
                        return newHoldings;
                    } else {
                        // 새로운 종목 추가
                        return [...prevHoldings, updatedHolding];
                    }
                });
            });
        }, (err: any) => {
            console.error('WebSocket connection error:', err);
            setError('WebSocket 연결 오류 발생.');
        });

        // 컴포넌트 언마운트 시 WebSocket 연결 해제
        return () => {
            if (stompClient && stompClient.connected) {
                stompClient.disconnect(() => {
                    console.log('Disconnected from WebSocket');
                });
            }
        };
    }, []); // stompClient를 의존성 배열에 추가하지 않음 (연결은 한 번만)

    if (loading) {
        return <div className="container mx-auto p-4">로딩 중...</div>;
    }

    if (error) {
        return <div className="container mx-auto p-4 text-red-500">에러: {error}</div>;
    }

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">보유 종목 현황</h1>
            {holdings.length === 0 ? (
                <p>보유 종목이 없습니다.</p>
            ) : (
                <table className="min-w-full bg-white border border-gray-300">
                    <thead>
                        <tr>
                            <th className="py-2 px-4 border-b">종목코드</th>
                            <th className="py-2 px-4 border-b">종목명</th>
                            <th className="py-2 px-4 border-b">현재가</th>
                            <th className="py-2 px-4 border-b">매수 가격</th>
                            <th className="py-2 px-4 border-b">수량</th>
                            <th className="py-2 px-4 border-b">손익</th>
                            <th className="py-2 px-4 border-b">손익률 (%)</th>
                        </tr>
                    </thead>
                    <tbody>
                        {holdings.map((holding, index) => (
                            <tr key={holding.stockCode} className="hover:bg-gray-100">
                                <td className="py-2 px-4 border-b">{holding.stockCode}</td>
                                <td className="py-2 px-4 border-b">{holding.stockName || '알 수 없음'}</td>
                                <td className="py-2 px-4 border-b text-right">{holding.currentPrice.toLocaleString()}</td>
                                <td className="py-2 px-4 border-b text-right">{holding.buyPrice.toLocaleString()}</td>
                                <td className="py-2 px-4 border-b text-right">{holding.quantity.toLocaleString()}</td>
                                <td className={`py-2 px-4 border-b text-right ${holding.profitLoss > 0 ? 'text-red-500' : holding.profitLoss < 0 ? 'text-blue-500' : ''}`}>{holding.profitLoss.toLocaleString()}</td>
                                <td className={`py-2 px-4 border-b text-right ${holding.profitLossPercentage > 0 ? 'text-red-500' : holding.profitLossPercentage < 0 ? 'text-blue-500' : ''}`}>{holding.profitLossPercentage.toFixed(2)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default HoldingsPage;