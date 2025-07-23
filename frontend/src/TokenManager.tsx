'''import { useState } from 'react';

function TokenManager() {
    const [tokenStatus, setTokenStatus] = useState('Not fetched yet.');

    const handleGetToken = async () => {
        try {
            const response = await fetch('/api/get-token');
            const data = await response.text();
            setTokenStatus(data);
        } catch (error) {
            console.error('Error getting token:', error);
            setTokenStatus('Error getting token.');
        }
    };

    const handleRevokeToken = async () => {
        try {
            const response = await fetch('/api/revoke-token');
            const data = await response.text();
            setTokenStatus(data);
        } catch (error) {
            console.error('Error revoking token:', error);
            setTokenStatus('Error revoking token.');
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
        </div>
    );
}

export default TokenManager;
'''