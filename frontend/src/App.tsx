import { useState } from 'react';
import reactLogo from './assets/react.svg';
import viteLogo from '/vite.svg';
import './App.css';
import AccountPage from './AccountPage'; // AccountPage 컴포넌트 임포트

function App() {
  const [tokenStatus, setTokenStatus] = useState('Not fetched yet.');
  const [showAccountPage, setShowAccountPage] = useState(false); // 계좌 페이지 표시 여부 상태

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
    <>
      <div>
        <a href="https://vitejs.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>

      {!showAccountPage ? (
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
          <button onClick={() => setShowAccountPage(true)} style={{ marginTop: '20px', padding: '10px 20px', backgroundColor: '#6c757d', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>
            Go to Account Page
          </button>
          <p className="read-the-docs">
            Click on the Vite and React logos to learn more
          </p>
        </div>
      ) : (
        <AccountPage />
      )}
    </>
  );
}

export default App;
