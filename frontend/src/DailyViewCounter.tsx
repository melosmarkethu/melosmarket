import { useEffect, useState } from 'react'

let initialView: Promise<number> | undefined

export default function DailyViewCounter({ apiBaseUrl }: { apiBaseUrl: string }) {
  const [count, setCount] = useState<number | null>(null)

  useEffect(() => {
    let active = true
    const load = async (method: 'GET' | 'POST') => {
      const response = await fetch(`${apiBaseUrl}/page-views`, { method, cache: 'no-store' })
      if (!response.ok) throw new Error('Page view request failed')
      const data = await response.json()
      return Number(data.count)
    }
    initialView ??= load('POST')
    initialView.then((value) => { if (active) setCount(value) }).catch(() => undefined)
    const timer = window.setInterval(() => {
      load('GET').then((value) => { if (active) setCount(value) }).catch(() => undefined)
    }, 60000)
    return () => { active = false; window.clearInterval(timer) }
  }, [apiBaseUrl])

  return <div className="daily-view-counter" title="Mai oldalmegtekintések (magyarországi idő szerint)">
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" /><circle cx="12" cy="12" r="3" />
    </svg>
    <span><strong>{count === null ? '–' : count.toLocaleString('hu-HU')}</strong><small>mai megtekintés</small></span>
  </div>
}
