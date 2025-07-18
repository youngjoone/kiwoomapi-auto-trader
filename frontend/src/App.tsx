import { useState } from 'react';
import reactLogo from './assets/react.svg';
import viteLogo from '/vite.svg';
import './App.css';

function App() {
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
    <>
      <div>
        <a href="https://vitejs.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
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
      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>
    </>
  );
}

export default App;
