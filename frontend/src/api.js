export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

async function parseErrorResponse(res) {
  let message = `Request failed with status ${res.status}`
  try {
    const body = await res.json()
    message = body.message || message
  } catch {
    // response had no JSON body
  }
  return message
}

async function request(path, options = {}) {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  })

  if (!res.ok) {
    throw new Error(await parseErrorResponse(res))
  }

  if (res.status === 204) return null
  return res.json()
}

export const api = {
  // Companies
  getCompanies: () => request('/api/companies'),
  getCompany: (id) => request(`/api/companies/${id}`),
  createCompany: (company) =>
    request('/api/companies', { method: 'POST', body: JSON.stringify(company) }),
  updateCompany: (id, company) =>
    request(`/api/companies/${id}`, { method: 'PUT', body: JSON.stringify(company) }),
  deleteCompany: (id) => request(`/api/companies/${id}`, { method: 'DELETE' }),

  // Applications
  getApplications: () => request('/api/applications'),
  getApplication: (id) => request(`/api/applications/${id}`),
  createApplication: (application) =>
    request('/api/applications', { method: 'POST', body: JSON.stringify(application) }),
  updateApplication: (id, application) =>
    request(`/api/applications/${id}`, { method: 'PUT', body: JSON.stringify(application) }),
  getNextStates: (id) => request(`/api/applications/${id}/next-states`),
  transitionApplication: (id, status) =>
    request(`/api/applications/${id}/transition`, {
      method: 'PATCH',
      body: JSON.stringify({ status })
    }),
  uploadDocument: async (id, documentType, file) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('documentType', documentType)

    const res = await fetch(`${API_BASE_URL}/api/applications/${id}/documents/upload`, {
      method: 'POST',
      body: formData
    })

    if (!res.ok) {
      throw new Error(await parseErrorResponse(res))
    }
    return res.json()
  },
  deleteApplication: (id) => request(`/api/applications/${id}`, { method: 'DELETE' })
}
