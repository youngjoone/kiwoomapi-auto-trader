import { useState, useEffect } from 'react';

function UpperLimitStocksPage() {
    const [stocks, setStocks] = useState<string[]>([]);
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
        <div>
            <h1>Previous Day Upper Limit Stocks</h1>
            <button onClick={fetchUpperLimitStocks} disabled={loading}>
                {loading ? 'Loading...' : 'Refresh'}
            </button>
            {error && <p style={{ color: 'red' }}>{error}</p>}
            <ul>
                {stocks.map((stock, index) => (
                    <li key={index}>{stock}</li>
                ))}
            </ul>
        </div>
    );
}

export default UpperLimitStocksPage;