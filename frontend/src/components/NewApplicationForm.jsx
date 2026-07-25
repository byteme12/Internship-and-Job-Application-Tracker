import { api } from '../api.js'
import ApplicationForm, { emptyApplicationForm } from './ApplicationForm.jsx'

export default function NewApplicationForm({ companies, onCreated }) {
  return (
    <ApplicationForm
      companies={companies}
      initialValues={emptyApplicationForm}
      title="New Application"
      submitLabel="Create Application"
      onSubmit={async (payload) => {
        await api.createApplication(payload)
        onCreated()
      }}
    />
  )
}
