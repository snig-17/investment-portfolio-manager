import React, { useState } from 'react';
import PortfolioDashboard from './components/CashManager';
import CashManager from './components/CashManager';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

function App() {

const [currentPortfolioId] = useState(1);
const [refreshKey, setRefreshKey] = useState(0);

const handleCashUpdate = () => {
  setRefreshKey(prev => prev + 1);
};

  return (
    <div className="App">

{/* Navigation Header */}

<nav className='navbar navbar-expand-lg navbar-dark bg-dark'>
  <div className='container'>
    <span className='navbar-brand'>
      Investment Portfolio Manager
    </span>
  </div>
</nav>
{/* Main Content */}
<div className='container-fluid'>
  <div className='row'>
    {/* Left Column - Dashboard */}
    <div className='col-md-8'>
      <PortfolioDashboard
      portfolioId={currentPortfolioId}
      key={refreshKey}
      />
    </div>
    {/* Right Column - Cash Management */}
    <div className='col-md-4'>
      <CashManager
      portfolioId={currentPortfolioId}
      onCashUpdated={handleCashUpdate}
      />
    </div>


  </div>
</div>
</div>


  );
}

export default App;
