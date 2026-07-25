import { useState } from 'react'
import CompaniesTab from './components/CompaniesTab.jsx'
import ApplicationsTab from './components/ApplicationsTab.jsx'

export default function App() {
  const [tab, setTab] = useState('applications')

  return (
    <div className="app">
      <header className="app-header">
        <h1>Internship & Job Application Tracker</h1>
        <nav className="tabs">
          <button
            className={tab === 'applications' ? 'active' : ''}
            onClick={() => setTab('applications')}
          >
            Applications
          </button>
          <button
            className={tab === 'companies' ? 'active' : ''}
            onClick={() => setTab('companies')}
          >
            Companies
          </button>
        </nav>
      </header>

      <main>{tab === 'applications' ? <ApplicationsTab /> : <CompaniesTab />}</main>
    </div>
  )
}
