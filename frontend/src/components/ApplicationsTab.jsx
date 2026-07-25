import { useEffect, useMemo, useState } from 'react'
import { api } from '../api.js'
import ApplicationCard from './ApplicationCard.jsx'
import NewApplicationForm from './NewApplicationForm.jsx'

const STATUSES = ['APPLIED', 'INTERVIEW', 'OFFER', 'ACCEPTED', 'REJECTED', 'CONVERTED_TO_FULLTIME']
const TYPES = ['INTERNSHIP', 'FULLTIME']

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

export default function ApplicationsTab() {
  const [applications, setApplications] = useState([])
  const [companies, setCompanies] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [jobRoleFilter, setJobRoleFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [typeFilter, setTypeFilter] = useState('')

  async function loadAll() {
    setLoading(true)
    setError(null)
    try {
      const [apps, comps] = await Promise.all([api.getApplications(), api.getCompanies()])
      setApplications(apps)
      setCompanies(comps)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadAll()
  }, [])

  const jobRoles = useMemo(
    () => dedupeCaseInsensitive(applications.map((a) => a.jobRole)),
    [applications]
  )

  const filteredApplications = applications.filter((a) => {
    const term = search.trim().toLowerCase()
    const matchesSearch =
      !term ||
      (a.companyName || '').toLowerCase().includes(term) ||
      (a.jobRole || '').toLowerCase().includes(term)
    const matchesJobRole = !jobRoleFilter || sameIgnoreCase(a.jobRole, jobRoleFilter)
    const matchesStatus = !statusFilter || a.status === statusFilter
    const matchesType = !typeFilter || a.applicationType === typeFilter
    return matchesSearch && matchesJobRole && matchesStatus && matchesType
  })

  const hasActiveFilters = search || jobRoleFilter || statusFilter || typeFilter

  function clearFilters() {
    setSearch('')
    setJobRoleFilter('')
    setStatusFilter('')
    setTypeFilter('')
  }

  return (
    <div className="tab-layout">
      <div>
        <h2>Applications</h2>

        <div className="filters">
          <input
            type="search"
            placeholder="Search by company or job role..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <select value={jobRoleFilter} onChange={(e) => setJobRoleFilter(e.target.value)}>
            <option value="">All job roles</option>
            {jobRoles.map((r) => (
              <option key={r} value={r}>
                {r}
              </option>
            ))}
          </select>
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="">All statuses</option>
            {STATUSES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
          <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
            <option value="">All types</option>
            {TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
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
        {!loading && applications.length === 0 && <p>No applications yet.</p>}
        {!loading && applications.length > 0 && filteredApplications.length === 0 && (
          <p>No applications match your filters.</p>
        )}
        <ul className="list">
          {filteredApplications.map((app) => (
            <ApplicationCard
              key={app.id}
              application={app}
              companies={companies}
              onChanged={loadAll}
            />
          ))}
        </ul>
      </div>
      <NewApplicationForm companies={companies} onCreated={loadAll} />
    </div>
  )
}
