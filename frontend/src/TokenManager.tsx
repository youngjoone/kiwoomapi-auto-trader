import { useState } from 'react';

function TokenManager() {
    const [tokenStatus, setTokenStatus] = useState('Not fetched yet.');
    const [testResponse, setTestResponse] = useState<string | null>(null);

    const handleGetToken = async () => {
        try {
            const response = await fetch('/api/v1/auth/get-token');
            const data = await response.text();
            setTokenStatus(data);
        } catch (error) {
            console.error('Error getting token:', error);
            setTokenStatus('Error getting token.');
        }
    };

    const handleRevokeToken = async () => {
        try {
            const response = await fetch('/api/v1/auth/revoke-token');
            const data = await response.text();
            setTokenStatus(data);
        } catch (error) {
            console.error('Error revoking token:', error);
            setTokenStatus('Error revoking token.');
        }
    };

    const handleTestBuy = async () => {
        try {
            const response = await fetch('/api/v1/test/buy');
            const data = await response.json();
            setTestResponse(JSON.stringify(data, null, 2));
        } catch (error: any) {
            console.error('Error during buy test:', error);
            setTestResponse(`Error: ${error.message}`);
        }
    };

    const handleTestSell = async () => {
        try {
            const response = await fetch('/api/v1/test/sell');
            const data = await response.json();
            setTestResponse(JSON.stringify(data, null, 2));
        } catch (error: any) {
            console.error('Error during sell test:', error);
            setTestResponse(`Error: ${error.message}`);
        }
    };

    return (
        <div style={{ textAlign: 'center' }}>
            <h1>Kiwoom API Token Management</h1>
            <div className="card">
                <button onClick={handleGetToken}>
                    Get/Refresh Token
                </button>
                <button onClick={handleRevokeToken}>
                    Revoke Token
                </button>
                <p>Token Status: {tokenStatus}</p>
            </div>

            <hr style={{ margin: '20px 0' }} />

            <h2>매매 테스트 (코아시아 045970 1주)</h2>
            <div className="card">
                <button onClick={handleTestBuy}>
                    매수 테스트
                </button>
                <button onClick={handleTestSell}>
                    매도 테스트
                </button>
                {testResponse && (
                    <pre style={{ textAlign: 'left', backgroundColor: '#f0f0f0', padding: '10px', borderRadius: '5px', marginTop: '10px' }}>
                        {testResponse}
                    </pre>
                )}
            </div>
        </div>
    );
}

export default TokenManager;
