import { useState } from 'react'

export const emptyApplicationForm = {
  applicationType: 'INTERNSHIP',
  companyName: '',
  companyId: '',
  jobRole: '',
  dateApplied: '',
  durationMonths: '',
  stipend: '',
  university: '',
  salary: '',
  noticePeriodDays: ''
}

export function applicationToFormValues(application) {
  return {
    applicationType: application.applicationType,
    companyName: application.companyName || '',
    companyId: application.companyId || '',
    jobRole: application.jobRole || '',
    dateApplied: application.dateApplied || '',
    durationMonths: application.durationMonths ?? '',
    stipend: application.stipend ?? '',
    university: application.university || '',
    salary: application.salary ?? '',
    noticePeriodDays: application.noticePeriodDays ?? ''
  }
}

function findCompanyByName(companies, name) {
  const normalized = name.trim().toLowerCase()
  return companies.find((c) => c.name.trim().toLowerCase() === normalized)
}

export default function ApplicationForm({
  companies,
  initialValues = emptyApplicationForm,
  title,
  submitLabel,
  lockType = false,
  onSubmit,
  onCancel
}) {
  const [form, setForm] = useState(initialValues)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)

    const base = {
      applicationType: form.applicationType,
      companyName: form.companyName.trim(),
      companyId: form.companyId || undefined,
      jobRole: form.jobRole.trim(),
      dateApplied: form.dateApplied
    }

    const payload =
      form.applicationType === 'INTERNSHIP'
        ? {
            ...base,
            durationMonths: Number(form.durationMonths),
            stipend: Number(form.stipend || 0),
            university: form.university
          }
        : {
            ...base,
            salary: Number(form.salary || 0),
            noticePeriodDays: Number(form.noticePeriodDays || 0)
          }

    try {
      await onSubmit(payload)
      setForm(emptyApplicationForm)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="card form" onSubmit={handleSubmit}>
      {title && <h3>{title}</h3>}
      {error && <p className="error">{error}</p>}

      <label>
        Type
        <select
          value={form.applicationType}
          onChange={(e) => update('applicationType', e.target.value)}
          disabled={lockType}
        >
          <option value="INTERNSHIP">Internship</option>
          <option value="FULLTIME">Full-Time</option>
        </select>
      </label>

      <label>
        Company Name
        <input
          list="known-companies"
          value={form.companyName}
          onChange={(e) => {
            const name = e.target.value
            const matched = findCompanyByName(companies, name)
            setForm((f) => ({ ...f, companyName: name, companyId: matched ? matched.id : '' }))
          }}
          required
        />
        <datalist id="known-companies">
          {companies.map((c) => (
            <option key={c.id} value={c.name} />
          ))}
        </datalist>
        <small className="hint">
          Start typing to see existing companies — picking one links this application to it.
          Otherwise just type a new name.
        </small>
      </label>

      <label>
        Job Role
        <input
          value={form.jobRole}
          onChange={(e) => update('jobRole', e.target.value)}
          placeholder="e.g. Software Engineer Intern"
          required
        />
      </label>

      <label>
        Date Applied
        <input
          type="date"
          value={form.dateApplied}
          onChange={(e) => update('dateApplied', e.target.value)}
          required
        />
      </label>

      {form.applicationType === 'INTERNSHIP' ? (
        <>
          <label>
            Duration (months)
            <input
              type="number"
              min="1"
              value={form.durationMonths}
              onChange={(e) => update('durationMonths', e.target.value)}
              required
            />
          </label>
          <label>
            Stipend
            <input
              type="number"
              min="0"
              value={form.stipend}
              onChange={(e) => update('stipend', e.target.value)}
            />
          </label>
          <label>
            University
            <input value={form.university} onChange={(e) => update('university', e.target.value)} />
          </label>
        </>
      ) : (
        <>
          <label>
            Salary
            <input
              type="number"
              min="0"
              value={form.salary}
              onChange={(e) => update('salary', e.target.value)}
            />
          </label>
          <label>
            Notice Period (days)
            <input
              type="number"
              min="0"
              value={form.noticePeriodDays}
              onChange={(e) => update('noticePeriodDays', e.target.value)}
            />
          </label>
        </>
      )}

      <div className="form-actions">
        <button type="submit" disabled={submitting}>
          {submitting ? 'Saving...' : submitLabel}
        </button>
        {onCancel && (
          <button type="button" className="secondary" onClick={onCancel} disabled={submitting}>
            Cancel
          </button>
        )}
      </div>
    </form>
  )
}
