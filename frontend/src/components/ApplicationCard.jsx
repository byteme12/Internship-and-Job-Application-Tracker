import { useState } from 'react'
import { api, API_BASE_URL } from '../api.js'
import ApplicationForm, { applicationToFormValues } from './ApplicationForm.jsx'

export default function ApplicationCard({ application, companies, onChanged }) {
  const [nextStates, setNextStates] = useState(null)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const [docType, setDocType] = useState('RESUME')
  const [docFile, setDocFile] = useState(null)
  const [showDocForm, setShowDocForm] = useState(false)
  const [showEditForm, setShowEditForm] = useState(false)

  async function loadNextStates() {
    setError(null)
    try {
      setNextStates(await api.getNextStates(application.id))
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleTransition(status) {
    setBusy(true)
    setError(null)
    try {
      await api.transitionApplication(application.id, status)
      setNextStates(null)
      onChanged()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function handleDelete() {
    if (!confirm(`Delete application at ${application.companyName}?`)) return
    setBusy(true)
    setError(null)
    try {
      await api.deleteApplication(application.id)
      onChanged()
    } catch (err) {
      setError(err.message)
      setBusy(false)
    }
  }

  async function handleAddDocument(e) {
    e.preventDefault()
    if (!docFile) {
      setError('Choose a file first')
      return
    }
    setBusy(true)
    setError(null)
    try {
      await api.uploadDocument(application.id, docType, docFile)
      setDocType('RESUME')
      setDocFile(null)
      setShowDocForm(false)
      onChanged()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <li className="card application-card">
      <div className="application-header">
        <div>
          <strong>{application.companyName}</strong>
          <span className={`badge status-${application.status}`}>{application.status}</span>
        </div>
        <span className="type-tag">{application.applicationType}</span>
      </div>

      {application.jobRole && <div className="job-role">{application.jobRole}</div>}

      <div className="meta">
        <span>Applied: {application.dateApplied}</span>
        {application.applicationType === 'INTERNSHIP' ? (
          <>
            <span>{application.durationMonths} months</span>
            <span>Stipend: {application.stipend}</span>
            {application.university && <span>{application.university}</span>}
          </>
        ) : (
          <>
            <span>Salary: {application.salary}</span>
            <span>Notice: {application.noticePeriodDays} days</span>
          </>
        )}
      </div>

      {application.documents?.length > 0 && (
        <ul className="documents">
          {application.documents.map((d) => (
            <li key={d.id}>
              {d.documentType}:{' '}
              <a
                href={d.fileUrl?.startsWith('/') ? `${API_BASE_URL}${d.fileUrl}` : d.fileUrl}
                target="_blank"
                rel="noreferrer"
              >
                {d.fileName}
              </a>
            </li>
          ))}
        </ul>
      )}

      {error && <p className="error">{error}</p>}

      <div className="actions">
        <button type="button" onClick={loadNextStates} disabled={busy}>
          Show Next States
        </button>
        <button type="button" onClick={() => setShowDocForm((s) => !s)} disabled={busy}>
          Add Document
        </button>
        <button type="button" onClick={() => setShowEditForm((s) => !s)} disabled={busy}>
          Edit
        </button>
        <button type="button" className="danger" onClick={handleDelete} disabled={busy}>
          Delete
        </button>
      </div>

      {showEditForm && (
        <ApplicationForm
          companies={companies}
          initialValues={applicationToFormValues(application)}
          submitLabel="Save Changes"
          lockType
          onCancel={() => setShowEditForm(false)}
          onSubmit={async (payload) => {
            await api.updateApplication(application.id, payload)
            setShowEditForm(false)
            onChanged()
          }}
        />
      )}

      {nextStates && (
        <div className="actions">
          {nextStates.length === 0 && <span>No further transitions (terminal state).</span>}
          {nextStates.map((s) => (
            <button key={s} type="button" onClick={() => handleTransition(s)} disabled={busy}>
              → {s}
            </button>
          ))}
        </div>
      )}

      {showDocForm && (
        <form className="doc-form" onSubmit={handleAddDocument}>
          <select value={docType} onChange={(e) => setDocType(e.target.value)}>
            <option value="RESUME">Resume</option>
            <option value="COVER_LETTER">Cover Letter</option>
            <option value="OFFER_LETTER">Offer Letter</option>
            <option value="TRANSCRIPT">Transcript</option>
            <option value="OTHER">Other</option>
          </select>
          <input
            type="file"
            onChange={(e) => setDocFile(e.target.files?.[0] ?? null)}
            required
          />
          <button type="submit" disabled={busy}>
            {busy ? 'Uploading...' : 'Upload'}
          </button>
        </form>
      )}
    </li>
  )
}
