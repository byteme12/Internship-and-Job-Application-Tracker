import { api } from '../api.js'
import CompanyForm, { emptyCompanyForm } from './CompanyForm.jsx'

export default function NewCompanyForm({ onCreated }) {
  return (
    <CompanyForm
      initialValues={emptyCompanyForm}
      title="Add Company"
      submitLabel="Add Company"
      onSubmit={async (payload) => {
        await api.createCompany(payload)
        onCreated()
      }}
    />
  )
}
