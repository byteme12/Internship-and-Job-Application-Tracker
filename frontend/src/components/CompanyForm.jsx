import { useState } from 'react'

export const emptyCompanyForm = { name: '', industry: '', location: '' }

export function companyToFormValues(company) {
  return {
    name: company.name || '',
    industry: company.industry || '',
    location: company.location || ''
  }
}

export default function CompanyForm({
  initialValues = emptyCompanyForm,
  title,
  submitLabel,
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
    try {
      await onSubmit({
        name: form.name.trim(),
        industry: form.industry.trim(),
        location: form.location.trim()
      })
      setForm(emptyCompanyForm)
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
        Name
        <input value={form.name} onChange={(e) => update('name', e.target.value)} required />
      </label>
      <label>
        Industry
        <input value={form.industry} onChange={(e) => update('industry', e.target.value)} />
      </label>
      <label>
        Location
        <input value={form.location} onChange={(e) => update('location', e.target.value)} />
      </label>
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
