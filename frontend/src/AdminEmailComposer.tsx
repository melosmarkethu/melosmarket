import { useState } from 'react'

type Recipient = { id: number; email: string; role: string }

export default function AdminEmailComposer({ apiBaseUrl, token }: { apiBaseUrl: string; token: string }) {
  const [draft, setDraft] = useState({ subject: '', heading: '', message: '', audience: 'ALL' })
  const [preview, setPreview] = useState('')
  const [recipients, setRecipients] = useState<Recipient[]>([])
  const [selectedIds, setSelectedIds] = useState<number[] | null>(null)
  const [busy, setBusy] = useState(false)
  const [feedback, setFeedback] = useState('')
  const [error, setError] = useState(false)

  const request = async (action: string) => {
    const response = await fetch(`${apiBaseUrl}/admin/emails/${action}`, {
      method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify({ ...draft, recipientIds: selectedIds }),
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
      const eligible: Recipient[] = result.recipients
      setRecipients(eligible)
      const selection = selectedIds === null ? eligible.map((member) => member.id) : selectedIds.filter((id) => eligible.some((member) => member.id === id))
      setSelectedIds(selection)
      if (action === 'preview') {
        setFeedback(`Címzettek: ${eligible.length} megerősített fiók.`)
      } else if (action === 'send' && (!preview || selectedIds === null)) {
        setFeedback('Ellenőrizd az előnézetet és válaszd ki a címzetteket, majd kattints újra a küldésre.')
      } else if (action === 'send' && selection.length === 0) {
        setError(true)
        setFeedback('Válassz legalább egy címzettet.')
      } else if (action !== 'send' || window.confirm(`Elküldöd ezt a szolgáltatási tájékoztatót ${selection.length} tagnak?`)) {
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
        <label>Címzettek<select value={draft.audience} onChange={(event) => { setPreview(''); setRecipients([]); setSelectedIds(null); setDraft({ ...draft, audience: event.target.value }) }}>
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
        {preview && <fieldset style={{ minWidth: 0, border: '1px solid #e5e7eb', borderRadius: 8, padding: 14 }}>
          <legend>Címzettek kiválasztása ({selectedIds?.length ?? 0}/{recipients.length})</legend>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, marginBottom: 12 }}>
            <button className="button ghost" type="button" onClick={() => setSelectedIds(recipients.map((member) => member.id))}>Összes kijelölése</button>
            <button className="button ghost" type="button" onClick={() => setSelectedIds([])}>Kijelölések törlése</button>
          </div>
          <div style={{ maxHeight: 320, overflowY: 'auto', display: 'grid', gap: 12 }}>
            {recipients.map((member) => <label className="checkbox-field" key={member.id}>
              <input type="checkbox" checked={selectedIds?.includes(member.id) ?? false} onChange={(event) => setSelectedIds((current) => event.target.checked ? [...(current ?? []), member.id] : (current ?? []).filter((id) => id !== member.id))} />
              <span style={{ overflowWrap: 'anywhere', minWidth: 0 }}>{member.email} · {({ WORKER: 'Szakember', CUSTOMER: 'Ügyfél', SUPPLIER: 'Tüzép' } as Record<string, string>)[member.role] ?? member.role}</span>
            </label>)}
            {recipients.length === 0 && <p>Nincs megfelelő címzett.</p>}
          </div>
        </fieldset>}
      </fieldset>
      {busy && <p role="status">Feldolgozás folyamatban, kérjük várj…</p>}
      {feedback && <p role="status" className={`form-message ${error ? 'error' : 'success'}`}>{feedback}</p>}
      {preview && <iframe title="Email előnézet" sandbox="" srcDoc={preview} style={{ width: '100%', height: 480, border: '1px solid #e5e7eb', borderRadius: 8 }} />}
    </form>
  </section>
}
