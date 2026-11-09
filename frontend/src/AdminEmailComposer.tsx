import { useState } from 'react'

export default function AdminEmailComposer({ apiBaseUrl, token }: { apiBaseUrl: string; token: string }) {
  const [draft, setDraft] = useState({ subject: '', heading: '', message: '', audience: 'ALL' })
  const [preview, setPreview] = useState('')
  const [busy, setBusy] = useState(false)
  const [feedback, setFeedback] = useState('')
  const [error, setError] = useState(false)

  const request = async (action: string) => {
    const response = await fetch(`${apiBaseUrl}/admin/emails/${action}`, {
      method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify(draft),
    })
    if (!response.ok) throw new Error('Nem sikerült végrehajtani a műveletet. Ellenőrizd az adatokat és a jogosultságodat.')
    return response.json()
  }

  const run = async (action: string) => {
    if (busy) return
    setBusy(true)
    setFeedback('')
    setError(false)
    try {
      const result = await request('preview')
      setPreview(result.html)
      if (action === 'preview') {
        setFeedback(`Címzettek: ${result.recipients} megerősített fiók.`)
      } else if (action !== 'send' || window.confirm(`Elküldöd ezt a szolgáltatási tájékoztatót ${result.recipients} tagnak?`)) {
        const sent = await request(action)
        setError(sent.failed > 0)
        setFeedback(`Elküldve: ${sent.sent}. Sikertelen: ${sent.failed}.`)
      }
    } catch (failure) {
      setError(true)
      setFeedback(failure instanceof Error ? failure.message : 'Hiba történt.')
    } finally {
      setBusy(false)
    }
  }

  return <section className="section-block">
    <h2>Email küldése tagoknak</h2>
    <p>Szolgáltatási tájékoztatók megerősített fiókoknak. A tesztlevél a saját admin email címedre érkezik.</p>
    <form className="problem-form" onSubmit={(event) => { event.preventDefault(); void run('preview') }}>
      <fieldset disabled={busy} style={{ border: 0, padding: 0, margin: 0, display: 'grid', gap: 14, minWidth: 0 }}>
        <label>Címzettek<select value={draft.audience} onChange={(event) => { setPreview(''); setDraft({ ...draft, audience: event.target.value }) }}>
          <option value="ALL">Minden tag</option><option value="WORKER">Szakemberek</option><option value="CUSTOMER">Ügyfelek</option><option value="SUPPLIER">Tüzépek</option>
        </select></label>
        {(['subject', 'heading', 'message'] as const).map((field) => <label key={field}>
          {{ subject: 'Tárgy', heading: 'Címsor', message: 'Üzenet' }[field]}
          {field === 'message' ? <textarea required maxLength={10000} rows={8} value={draft[field]} onChange={(event) => { setPreview(''); setDraft({ ...draft, [field]: event.target.value }) }} />
            : <input required maxLength={180} value={draft[field]} onChange={(event) => { setPreview(''); setDraft({ ...draft, [field]: event.target.value }) }} />}
        </label>)}
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
          <button className="button ghost" type="submit">Előnézet</button>
          <button className="button ghost" type="button" onClick={() => void run('test')}>Tesztlevél küldése</button>
          <button className="button primary" type="button" onClick={() => void run('send')}>Küldés a tagoknak</button>
        </div>
      </fieldset>
      {busy && <p role="status">Feldolgozás folyamatban, kérjük várj…</p>}
      {feedback && <p role="status" className={`form-message ${error ? 'error' : 'success'}`}>{feedback}</p>}
      {preview && <iframe title="Email előnézet" sandbox="" srcDoc={preview} style={{ width: '100%', height: 480, border: '1px solid #e5e7eb', borderRadius: 8 }} />}
    </form>
  </section>
}
