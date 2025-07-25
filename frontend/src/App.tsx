import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import AccountPage from './AccountPage';
import UpperLimitStocksPage from './UpperLimitStocksPage';
import TokenManager from './TokenManager';
import TradeHistoryPage from './TradeHistoryPage';
import HoldingsPage from './HoldingsPage';

function App() {
    return (
        <Router>
            <div>
                <nav>
                    <ul>
                        <li>
                            <Link to="/">Home</Link>
                        </li>
                        <li>
                            <Link to="/account">Account</Link>
                        </li>
                        <li>
                            <Link to="/upper-limit-stocks">Upper Limit Stocks</Link>
                        </li>
                        <li>
                            <Link to="/trade-history">Trade History</Link>
                        </li>
                        <li>
                            <Link to="/holdings">Holdings</Link>
                        </li>
                    </ul>
                </nav>

                <hr />

                <Routes>
                    <Route path="/" element={<TokenManager />} />
                    <Route path="/account" element={<AccountPage />} />
                    <Route path="/upper-limit-stocks" element={<UpperLimitStocksPage />} />
                    <Route path="/trade-history" element={<TradeHistoryPage />} />
                    <Route path="/holdings" element={<HoldingsPage />} />
                </Routes>
            </div>
        </Router>
    );
}

export default App;
