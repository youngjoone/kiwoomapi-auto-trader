import { useState, useEffect } from 'react';

interface StockData {
    stk_cd: string;
    stk_nm: string;
    cur_prc: string;
    pred_pre_sig: string;
    pred_pre: string;
    flu_rt: string;
    trde_qty: string;
    pred_trde_qty_pre_rt: string;
    sel_bid: string;
    buy_bid: string;
    high_pric: string;
    low_pric: string;
}

function UpperLimitStocksPage() {
    const [stocks, setStocks] = useState<StockData[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchUpperLimitStocks = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch('/api/v1/stock/upper-limit');
            if (!response.ok) {
                throw new Error(`Error: ${response.statusText}`);
            }
            const data = await response.json();
            setStocks(data);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUpperLimitStocks();
    }, []);

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">전일 상한가 종목</h1>
            <button onClick={fetchUpperLimitStocks} disabled={loading} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mb-4">
                {loading ? '로딩중...' : '새로고침'}
            </button>
            {error && <p className="text-red-500">{error}</p>}
            <table className="min-w-full bg-white">
                <thead>
                    <tr>
                        <th className="py-2 px-4 border-b">종목코드 (stk_cd)</th>
                        <th className="py-2 px-4 border-b">종목명 (stk_nm)</th>
                        <th className="py-2 px-4 border-b">현재가 (cur_prc)</th>
                        <th className="py-2 px-4 border-b">전일 대비 기호 (pred_pre_sig)</th>
                        <th className="py-2 px-4 border-b">전일 대비 (pred_pre)</th>
                        <th className="py-2 px-4 border-b">등락률 (flu_rt)</th>
                        <th className="py-2 px-4 border-b">거래량 (trde_qty)</th>
                        <th className="py-2 px-4 border-b">전일 거래량 대비율 (pred_trde_qty_pre_rt)</th>
                        <th className="py-2 px-4 border-b">매도호가 (sel_bid)</th>
                        <th className="py-2 px-4 border-b">매수호가 (buy_bid)</th>
                        <th className="py-2 px-4 border-b">고가 (high_pric)</th>
                        <th className="py-2 px-4 border-b">저가 (low_pric)</th>
                    </tr>
                </thead>
                <tbody>
                    {stocks.map((stock, index) => (
                        <tr key={index}>
                            <td className="py-2 px-4 border-b">{stock.stk_cd}</td>
                            <td className="py-2 px-4 border-b">{stock.stk_nm}</td>
                            <td className="py-2 px-4 border-b text-right">{Number(stock.cur_prc).toLocaleString()}</td>
                            <td className="py-2 px-4 border-b">{stock.pred_pre_sig}</td>
                            <td className="py-2 px-4 border-b text-right">{Number(stock.pred_pre).toLocaleString()}</td>
                            <td className={`py-2 px-4 border-b text-right ${Number(stock.flu_rt) > 0 ? 'text-red-500' : Number(stock.flu_rt) < 0 ? 'text-blue-500' : ''}`}>{Number(stock.flu_rt).toFixed(2)}%</td>
                            <td className="py-2 px-4 border-b text-right">{Number(stock.trde_qty).toLocaleString()}</td>
                            <td className="py-2 px-4 border-b text-right">{Number(stock.pred_trde_qty_pre_rt).toFixed(2)}%</td>
                            <td className="py-2 px-4 border-b">{stock.sel_bid}</td>
                            <td className="py-2 px-4 border-b">{stock.buy_bid}</td>
                            <td className="py-2 px-4 border-b">{stock.high_pric}</td>
                            <td className="py-2 px-4 border-b">{stock.low_pric}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default UpperLimitStocksPage;