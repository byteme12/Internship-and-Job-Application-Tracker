import { useEffect, useMemo, useState } from 'react'
import { api } from '../api.js'
import NewCompanyForm from './NewCompanyForm.jsx'
import CompanyForm, { companyToFormValues } from './CompanyForm.jsx'

function dedupeCaseInsensitive(values) {
  const seen = new Map()
  values.filter(Boolean).forEach((v) => {
    const key = v.trim().toLowerCase()
    if (!seen.has(key)) seen.set(key, v.trim())
  })
  return [...seen.values()].sort((a, b) => a.localeCompare(b))
}

function sameIgnoreCase(a, b) {
  return (a || '').trim().toLowerCase() === (b || '').trim().toLowerCase()
}

export default function CompaniesTab() {
  const [companies, setCompanies] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [industryFilter, setIndustryFilter] = useState('')
  const [locationFilter, setLocationFilter] = useState('')
  const [editingId, setEditingId] = useState(null)
  const [rowError, setRowError] = useState(null)

  async function loadCompanies() {
    setLoading(true)
    setError(null)
    try {
      setCompanies(await api.getCompanies())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadCompanies()
  }, [])

  const industries = useMemo(() => dedupeCaseInsensitive(companies.map((c) => c.industry)), [companies])
  const locations = useMemo(() => dedupeCaseInsensitive(companies.map((c) => c.location)), [companies])

  const filteredCompanies = companies.filter((c) => {
    const matchesSearch = (c.name || '').toLowerCase().includes(search.trim().toLowerCase())
    const matchesIndustry = !industryFilter || sameIgnoreCase(c.industry, industryFilter)
    const matchesLocation = !locationFilter || sameIgnoreCase(c.location, locationFilter)
    return matchesSearch && matchesIndustry && matchesLocation
  })

  const hasActiveFilters = search || industryFilter || locationFilter

  function clearFilters() {
    setSearch('')
    setIndustryFilter('')
    setLocationFilter('')
  }

  async function handleDelete(company) {
    if (!confirm(`Delete company "${company.name}"?`)) return
    setRowError(null)
    try {
      await api.deleteCompany(company.id)
      loadCompanies()
    } catch (err) {
      setRowError(err.message)
    }
  }

  return (
    <div className="tab-layout">
      <div>
        <h2>Companies</h2>

        <div className="filters">
          <input
            type="search"
            placeholder="Search companies by name..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <select value={industryFilter} onChange={(e) => setIndustryFilter(e.target.value)}>
            <option value="">All industries</option>
            {industries.map((i) => (
              <option key={i} value={i}>
                {i}
              </option>
            ))}
          </select>
          <select value={locationFilter} onChange={(e) => setLocationFilter(e.target.value)}>
            <option value="">All locations</option>
            {locations.map((l) => (
              <option key={l} value={l}>
                {l}
              </option>
            ))}
          </select>
          {hasActiveFilters && (
            <button type="button" onClick={clearFilters}>
              Clear filters
            </button>
          )}
        </div>

        {loading && <p>Loading...</p>}
        {error && <p className="error">{error}</p>}
        {rowError && <p className="error">{rowError}</p>}
        {!loading && companies.length === 0 && <p>No companies yet.</p>}
        {!loading && companies.length > 0 && filteredCompanies.length === 0 && (
          <p>No companies match your filters.</p>
        )}
        <ul className="list">
          {filteredCompanies.map((c) =>
            editingId === c.id ? (
              <li key={c.id}>
                <CompanyForm
                  initialValues={companyToFormValues(c)}
                  submitLabel="Save Changes"
                  onCancel={() => setEditingId(null)}
                  onSubmit={async (payload) => {
                    await api.updateCompany(c.id, payload)
                    setEditingId(null)
                    loadCompanies()
                  }}
                />
              </li>
            ) : (
              <li key={c.id} className="card">
                <strong>{c.name}</strong>
                <div className="meta">
                  {c.industry && <span>{c.industry}</span>}
                  {c.location && <span>{c.location}</span>}
                </div>
                <div className="actions">
                  <button type="button" onClick={() => setEditingId(c.id)}>
                    Edit
                  </button>
                  <button type="button" className="danger" onClick={() => handleDelete(c)}>
                    Delete
                  </button>
                </div>
              </li>
            )
          )}
        </ul>
      </div>
      <NewCompanyForm onCreated={loadCompanies} />
    </div>
  )
}
