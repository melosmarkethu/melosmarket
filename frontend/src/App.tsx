import { useEffect, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import './App.css'

type ProblemFormState = {
  title: string
  description: string
  trade: string
  location: string
  phone: string
}

type ProblemPost = {
  id?: number
  customerId?: number
  title: string
  description: string
  trade: string
  location: string
  phone?: string
  posted: string
  images: ProblemImage[]
}

type ProblemImage = {
  id: string
  title: string
  imageUrl: string
}

type ProblemApiResponse = {
  id: number
  customerId?: number
  title: string
  description: string
  status?: string
  trade?: string
  location?: string
  phone?: string
  createdAt?: string
  problemImages?: ProblemImageApiResponse[]
}

type ProblemImageApiResponse = {
  id: number
  title: string
  imageUrl: string
}

type WorkerCard = {
  id?: number
  name: string
  contactName?: string
  email?: string
  phone?: string
  taxNumber?: string
  trade: string
  area: string
  rating: string
  jobs: string
  verified: boolean
  topWorker: boolean
  manyReferences: boolean
  hundredJobs: boolean
  fastResponder: boolean
  availabilityStatus: WorkerAvailabilityStatus
  urgentWork: boolean
  trialStartedAt?: string
  trialEndsAt?: string
  accessStatus: WorkerAccessStatus
  trialDaysRemaining: number
  description: string
  referenceWorks: ReferenceWork[]
  reviews: Review[]
}

type WorkerAvailabilityStatus = 'AVAILABLE' | 'LIMITED' | 'UNAVAILABLE_THIS_MONTH'
type WorkerAccessStatus = 'TRIALING' | 'TRIAL_EXPIRED' | 'ACTIVE' | 'PAST_DUE' | 'CANCELED'

type ReferenceWork = {
  id: string
  title: string
  imageUrl: string
}

type Review = {
  id: string
  reviewerWorkerId: number
  reviewerName: string
  rating: number
  text: string
  createdAt: string
  updatedAt: string
}

type ReviewFormState = {
  rating: string
  text: string
}

type WorkerFormState = {
  businessName: string
  contactName: string
  email: string
  password: string
  phone: string
  taxNumber: string
  trade: string
  serviceArea: string
  description: string
}

type WorkerApiResponse = {
  id: number
  businessName: string
  contactName: string
  email: string
  phone?: string
  taxNumber?: string
  trade: string
  verified?: boolean
  topWorker?: boolean
  manyReferences?: boolean
  hundredJobs?: boolean
  fastResponder?: boolean
  availabilityStatus?: WorkerAvailabilityStatus
  urgentWork?: boolean
  trialStartedAt?: string
  trialEndsAt?: string
  accessStatus?: WorkerAccessStatus
  trialDaysRemaining?: number
  serviceArea?: string
  description?: string
  referenceImages?: WorkerReferenceApiResponse[]
  reviews?: WorkerReviewApiResponse[]
}

type WorkerReviewApiResponse = {
  id: number
  reviewerWorkerId: number
  reviewerName: string
  rating: number
  text: string
  createdAt: string
  updatedAt: string
}

type WorkerReferenceApiResponse = {
  id: number
  title: string
  imageUrl: string
}

type CurrentUser = {
  id: number
  email: string
  role: string
  emailVerified: boolean
  workerId?: number
}

type AuthResponse = {
  token: string
  user: CurrentUser
  worker?: WorkerApiResponse
}

type LoginFormState = {
  email: string
  password: string
}

type WorkerProfileEditState = {
  businessName: string
  contactName: string
  phone: string
  taxNumber: string
  trade: string
  serviceArea: string
  description: string
  availabilityStatus: WorkerAvailabilityStatus
  urgentWork: boolean
}

type CustomerRegistrationFormState = {
  email: string
  password: string
}

type WorkerBadgeKey =
  | 'topWorker'
  | 'verified'
  | 'manyReferences'
  | 'hundredJobs'
  | 'fastResponder'

const workerBadgeDefinitions: Array<{ key: WorkerBadgeKey; label: string }> = [
  { key: 'topWorker', label: '⭐ Top szakember' },
  { key: 'verified', label: '✔️ Ellenőrzött profil' },
  { key: 'manyReferences', label: '📸 Sok referencia' },
  { key: 'hundredJobs', label: '🏆 100+ sikeres munka' },
  { key: 'fastResponder', label: '⚡ Gyors válaszoló' },
]

const workerAvailabilityDefinitions: Array<{ value: WorkerAvailabilityStatus; label: string }> = [
  { value: 'AVAILABLE', label: '🟢 Vállal új munkát' },
  { value: 'LIMITED', label: '🟡 Korlátozott kapacitás' },
  { value: 'UNAVAILABLE_THIS_MONTH', label: '🔴 A hónapban nem vállal új munkát' },
]

const workerAvailabilityLabels: Record<WorkerAvailabilityStatus, string> = Object.fromEntries(
  workerAvailabilityDefinitions.map((availability) => [availability.value, availability.label]),
) as Record<WorkerAvailabilityStatus, string>

const urgentWorkBadge = { key: 'urgentWork', label: '⚡ Sürgős munkát is vállal' }

const accessStatusLabels: Record<WorkerAccessStatus, string> = {
  TRIALING: 'Ingyenes próbaidő',
  TRIAL_EXPIRED: 'Próbaidő lejárt',
  ACTIVE: 'Előfizetés aktív',
  PAST_DUE: 'Fizetés rendezésre vár',
  CANCELED: 'Előfizetés lemondva',
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'
const apiOrigin = apiBaseUrl.replace(/\/api\/?$/, '')

const slugify = (value: string) =>
  value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')

const workerProfileSlug = (worker: WorkerCard) => {
  const nameSlug = slugify(worker.name) || 'szakember'
  const citySlug = worker.area === 'Nincs megadva' ? '' : slugify(worker.area)
  return citySlug ? `${nameSlug}-${citySlug}` : nameSlug
}

const workerProfilePath = (worker: WorkerCard) => `/${workerProfileSlug(worker)}`

const problemProfileSlug = (problem: ProblemPost) => {
  const titleSlug = slugify(problem.title) || 'problema'
  const citySlug = problem.location === 'Nincs megadva' ? '' : slugify(problem.location)
  return citySlug ? `${titleSlug}-${citySlug}` : titleSlug
}

const problemProfilePath = (problem: ProblemPost) => `/${problemProfileSlug(problem)}`

const ignoredRouteSlugs = new Set([
  'adatkezeles.html',
  'aszf.html',
  'ertekelesi-szabalyzat.html',
  'felhasznaloi-hozzajarulas.html',
  'hirdetesi-szabalyzat.html',
  'moderacios-szabalyzat.html',
  'panaszkezelesi-szabalyzat.html',
])

const trades = [
  'Generálkivitelezés',
  'Kőműves',
  'Burkoló',
  'Festő–mázoló–tapétázó',
  'Ács–tetőfedő',
  'Bádogos',
  'Víz-, gáz- és fűtésszerelő',
  'Klímaszerelő',
  'Hegesztő',
  'Földmunka',
  'Épületbontás',
  'Duguláselhárítás',
  'Gázkészülék-javítás',
  'Klímatisztítás',
  'Kamerarendszer-szerelő',
  'Árnyékolástechnika',
  'Veszélyesfa-kivágás',
  'Autószerelő',
  'Autófényező',
  'Karosszérialakatos',
  'Gumijavító',
  'Szélvédő-javítás',
  'Autókozmetikus',
  'Autómentő',
  'Költöztetés',
  'Fuvarozás',
]

const tradeApiValues: Record<string, string> = {
  Generálkivitelezés: 'GENERAL_CONTRACTING',
  Kőműves: 'MASON',
  Burkoló: 'TILER',
  'Festő–mázoló–tapétázó': 'PAINTER_DECORATOR_WALLPAPERER',
  'Ács–tetőfedő': 'CARPENTER_ROOFER',
  Bádogos: 'TINSMITH',
  'Víz-, gáz- és fűtésszerelő': 'PLUMBING_GAS_HEATING',
  Klímaszerelő: 'AIR_CONDITIONING_INSTALLER',
  Hegesztő: 'WELDER',
  Földmunka: 'EARTHWORKS',
  Épületbontás: 'BUILDING_DEMOLITION',
  Duguláselhárítás: 'DRAIN_CLEANING',
  'Gázkészülék-javítás': 'GAS_APPLIANCE_REPAIR',
  Klímatisztítás: 'AIR_CONDITIONING_CLEANING',
  'Kamerarendszer-szerelő': 'CCTV_INSTALLER',
  Árnyékolástechnika: 'SHADING_TECHNOLOGY',
  'Veszélyesfa-kivágás': 'DANGEROUS_TREE_REMOVAL',
  Autószerelő: 'CAR_MECHANIC',
  Autófényező: 'CAR_PAINTER',
  Karosszérialakatos: 'AUTO_BODY_REPAIRER',
  Gumijavító: 'TIRE_REPAIR',
  'Szélvédő-javítás': 'WINDSHIELD_REPAIR',
  Autókozmetikus: 'CAR_DETAILER',
  Autómentő: 'CAR_TOWING',
  Költöztetés: 'MOVING',
  Fuvarozás: 'FREIGHT_TRANSPORT',
}

const tradeLabelsByApiValue: Record<string, string> = Object.fromEntries(
  Object.entries(tradeApiValues).map(([label, value]) => [value, label]),
)

const initialWorkers: WorkerCard[] = [
  {
    name: 'Gyors Csőszerviz',
    trade: 'Víz-, gáz- és fűtésszerelő',
    area: 'Budapest és környéke',
    rating: '4.9',
    jobs: '128 munka',
    verified: true,
    topWorker: true,
    manyReferences: true,
    hundredJobs: true,
    fastResponder: true,
    availabilityStatus: 'AVAILABLE',
    urgentWork: true,
    trialStartedAt: new Date().toISOString(),
    trialEndsAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
    accessStatus: 'TRIALING',
    trialDaysRemaining: 30,
    description: 'Szivárgásjavítás, fürdőszoba-szerelés, duguláselhárítás és sürgős kiszállás.',
    referenceWorks: [],
    reviews: [],
  },
  {
    name: 'Stabil Otthon Építők',
    trade: 'Generálkivitelezés',
    area: 'Pest megye',
    rating: '4.8',
    jobs: '84 munka',
    verified: false,
    topWorker: false,
    manyReferences: false,
    hundredJobs: false,
    fastResponder: false,
    availabilityStatus: 'LIMITED',
    urgentWork: false,
    trialStartedAt: new Date().toISOString(),
    trialEndsAt: new Date(Date.now() + 18 * 24 * 60 * 60 * 1000).toISOString(),
    accessStatus: 'TRIALING',
    trialDaysRemaining: 18,
    description: 'Felújítás, falbontás, bővítés, teraszépítés és szerkezeti munkák.',
    referenceWorks: [],
    reviews: [],
  },
  {
    name: 'Biztos Kamera',
    trade: 'Kamerarendszer-szerelő',
    area: 'Budapest nyugati része',
    rating: '4.7',
    jobs: '96 munka',
    verified: false,
    topWorker: false,
    manyReferences: false,
    hundredJobs: false,
    fastResponder: true,
    availabilityStatus: 'AVAILABLE',
    urgentWork: true,
    trialStartedAt: new Date().toISOString(),
    trialEndsAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
    accessStatus: 'TRIALING',
    trialDaysRemaining: 7,
    description: 'Kamerarendszerek telepítése, beállítása, bővítése és karbantartása.',
    referenceWorks: [],
    reviews: [],
  },
]

const defaultReferenceWorks: ReferenceWork[] = [
  {
    id: 'bathroom-renovation',
    title: 'Fürdőszoba felújítás',
    imageUrl: 'https://images.unsplash.com/photo-1620626011761-996317b8d101?auto=format&fit=crop&w=900&q=80',
  },
  {
    id: 'kitchen-repair',
    title: 'Konyhai javítás',
    imageUrl: 'https://images.unsplash.com/photo-1556912173-3bb406ef7e77?auto=format&fit=crop&w=900&q=80',
  },
]

const initialProblems: ProblemPost[] = [
  {
    title: 'Szivárog a konyhai mosogató a szekrény alatt',
    description: 'A mosogató alatti csőnél víz jelenik meg használat közben, valószínűleg tömítés vagy csőcsatlakozás hibája.',
    trade: 'Víz-, gáz- és fűtésszerelő',
    location: 'Újlipótváros',
    posted: '12 perce',
    images: [],
  },
  {
    title: 'Két beltéri fal festésére van szükség',
    description: 'Két közepes méretű beltéri fal tisztasági festéséhez keresünk szakembert.',
    trade: 'Festő–mázoló–tapétázó',
    location: 'Terézváros',
    posted: '34 perce',
    images: [],
  },
  {
    title: 'Törött kerítésszakasz cseréje a kertben',
    description: 'A kert egyik kerítésszakasza megsérült, javításra vagy részleges cserére lenne szükség.',
    trade: 'Ács–tetőfedő',
    location: 'Budaörs',
    posted: '1 órája',
    images: [],
  },
]

function App() {
  const [selectedWorker, setSelectedWorker] = useState<WorkerCard | null>(null)
  const [selectedProblem, setSelectedProblem] = useState<ProblemPost | null>(null)
  const [authToken, setAuthToken] = useState(() => localStorage.getItem('melosmarket_token') ?? '')
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null)
  const [loginForm, setLoginForm] = useState<LoginFormState>({
    email: '',
    password: '',
  })
  const [isLoginOpen, setIsLoginOpen] = useState(false)
  const [loginState, setLoginState] = useState<'idle' | 'submitting' | 'success' | 'error'>('idle')
  const [loginMessage, setLoginMessage] = useState('')
  const [verificationModalOpen, setVerificationModalOpen] = useState(false)
  const [verificationState, setVerificationState] = useState<'idle' | 'submitting' | 'success' | 'error'>('idle')
  const [verificationMessage, setVerificationMessage] = useState('')
  const [customerForm, setCustomerForm] = useState<CustomerRegistrationFormState>({
    email: '',
    password: '',
  })
  const [customerSubmitState, setCustomerSubmitState] = useState<'idle' | 'submitting' | 'success' | 'error'>('idle')
  const [customerSubmitMessage, setCustomerSubmitMessage] = useState('')
  const [profileEditForm, setProfileEditForm] = useState<WorkerProfileEditState>({
    businessName: '',
    contactName: '',
    phone: '',
    taxNumber: '',
    trade: '',
    serviceArea: '',
    description: '',
    availabilityStatus: 'AVAILABLE',
    urgentWork: false,
  })
  const [profileEditState, setProfileEditState] = useState<'idle' | 'submitting' | 'success' | 'error'>('idle')
  const [profileEditMessage, setProfileEditMessage] = useState('')
  const [workerSearch, setWorkerSearch] = useState({
    trade: '',
    serviceArea: '',
  })
  const [workerCards, setWorkerCards] = useState<WorkerCard[]>(initialWorkers)
  const [workerSearchStatus, setWorkerSearchStatus] = useState<'idle' | 'loading' | 'error'>('idle')
  const [workerSearchMessage, setWorkerSearchMessage] = useState('')
  const [workerForm, setWorkerForm] = useState<WorkerFormState>({
    businessName: '',
    contactName: '',
    email: '',
    password: '',
    phone: '',
    taxNumber: '',
    trade: '',
    serviceArea: '',
    description: '',
  })
  const [workerSubmitState, setWorkerSubmitState] = useState<'idle' | 'submitting' | 'success' | 'error'>('idle')
  const [workerSubmitMessage, setWorkerSubmitMessage] = useState('')
  const [reviewForm, setReviewForm] = useState<ReviewFormState>({
    rating: '5',
    text: '',
  })
  const [reviewSubmitState, setReviewSubmitState] = useState<'idle' | 'submitting' | 'success' | 'error'>('idle')
  const [reviewSubmitMessage, setReviewSubmitMessage] = useState('')
  const [problemForm, setProblemForm] = useState<ProblemFormState>({
    title: '',
    description: '',
    trade: '',
    location: '',
    phone: '',
  })
  const [problemEditForm, setProblemEditForm] = useState<ProblemFormState>({
    title: '',
    description: '',
    trade: '',
    location: '',
    phone: '',
  })
  const [problemPhotoFiles, setProblemPhotoFiles] = useState<File[]>([])
  const [problemEditPhotoFiles, setProblemEditPhotoFiles] = useState<File[]>([])
  const [problemEditState, setProblemEditState] = useState<'idle' | 'submitting' | 'success' | 'error'>('idle')
  const [problemEditMessage, setProblemEditMessage] = useState('')
  const [problemPosts, setProblemPosts] = useState<ProblemPost[]>(initialProblems)
  const [customerProblems, setCustomerProblems] = useState<ProblemPost[]>([])
  const [customerProblemsState, setCustomerProblemsState] = useState<'idle' | 'loading' | 'error'>('idle')
  const [submitState, setSubmitState] = useState<'idle' | 'submitting' | 'success' | 'error'>('idle')
  const [submitMessage, setSubmitMessage] = useState('')
  const [adminState, setAdminState] = useState<'idle' | 'loading' | 'success' | 'error'>('idle')
  const [adminMessage, setAdminMessage] = useState('')

  const workerFromApi = (worker: WorkerApiResponse): WorkerCard => ({
    id: worker.id,
    name: worker.businessName,
    contactName: worker.contactName,
    email: worker.email,
    phone: worker.phone,
    taxNumber: worker.taxNumber,
    trade: tradeLabelsByApiValue[worker.trade] ?? worker.trade,
    area: worker.serviceArea ?? 'Nincs megadva',
    rating: 'Új',
    jobs: '0 munka',
    verified: Boolean(worker.verified),
    topWorker: Boolean(worker.topWorker),
    manyReferences: Boolean(worker.manyReferences),
    hundredJobs: Boolean(worker.hundredJobs),
    fastResponder: Boolean(worker.fastResponder),
    availabilityStatus: worker.availabilityStatus ?? 'AVAILABLE',
    urgentWork: Boolean(worker.urgentWork),
    trialStartedAt: worker.trialStartedAt,
    trialEndsAt: worker.trialEndsAt,
    accessStatus: worker.accessStatus ?? 'TRIALING',
    trialDaysRemaining: worker.trialDaysRemaining ?? 30,
    description: worker.description ?? 'Ez a szakember még nem adott meg bemutatkozást.',
    referenceWorks: (worker.referenceImages ?? []).map((reference) => ({
      id: String(reference.id),
      title: reference.title,
      imageUrl: reference.imageUrl.startsWith('/api') ? `${apiOrigin}${reference.imageUrl}` : reference.imageUrl,
    })),
    reviews: (worker.reviews ?? []).map((review) => ({
      id: String(review.id),
      reviewerWorkerId: review.reviewerWorkerId,
      reviewerName: review.reviewerName,
      rating: review.rating,
      text: review.text,
      createdAt: review.createdAt,
      updatedAt: review.updatedAt,
    })),
  })

  const problemImageFromApi = (image: ProblemImageApiResponse): ProblemImage => ({
    id: String(image.id),
    title: image.title,
    imageUrl: image.imageUrl.startsWith('/api') ? `${apiOrigin}${image.imageUrl}` : image.imageUrl,
  })

  const problemFromApi = (problem: ProblemApiResponse): ProblemPost => ({
    id: problem.id,
    customerId: problem.customerId,
    title: problem.title,
    description: problem.description,
    trade: problem.trade ? tradeLabelsByApiValue[problem.trade] ?? problem.trade : 'Nincs megadva',
    location: problem.location ?? 'Nincs megadva',
    phone: problem.phone,
    posted: problem.createdAt ? new Date(problem.createdAt).toLocaleDateString('hu-HU') : 'adatbázisból',
    images: (problem.problemImages ?? []).map(problemImageFromApi),
  })

  const updateProblemEverywhere = (updatedProblem: ProblemPost) => {
    setSelectedProblem(updatedProblem)
    setProblemPosts((current) =>
      current.map((problem) => (problem.id === updatedProblem.id ? updatedProblem : problem)),
    )
    setCustomerProblems((current) =>
      current.map((problem) => (problem.id === updatedProblem.id ? updatedProblem : problem)),
    )
  }

  const formatHungarianDate = (dateValue?: string) =>
    dateValue
      ? new Date(dateValue).toLocaleDateString('hu-HU', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
        })
      : 'Nincs megadva'

  const authHeaders = () => ({
    Authorization: `Bearer ${authToken}`,
  })

  const isCustomer = currentUser?.role === 'CUSTOMER'
  const isWorker = currentUser?.role === 'WORKER'
  const isAdmin = currentUser?.role === 'ADMIN'
  const currentCustomerProblems = customerProblems.slice(0, 5)
  const recentCustomerProblems = customerProblems.slice(0, 3)
  const activeWorkerBadges = (worker: WorkerCard) => [
    ...workerBadgeDefinitions.filter((badge) => worker[badge.key]),
    { key: 'availability', label: workerAvailabilityLabels[worker.availabilityStatus] },
    ...(worker.urgentWork ? [urgentWorkBadge] : []),
  ]

  const rememberAuth = (auth: AuthResponse) => {
    localStorage.setItem('melosmarket_token', auth.token)
    setAuthToken(auth.token)
    setCurrentUser(auth.user)
    if (!auth.user.emailVerified) {
      setVerificationModalOpen(true)
      setVerificationState('idle')
      setVerificationMessage('Küldtünk egy megerősítő emailt. Kérjük, nyisd meg a levelet és erősítsd meg az email címed.')
    }
    if (auth.worker) {
      const worker = workerFromApi(auth.worker)
      setWorkerCards((current) => [worker, ...current.filter((item) => item.id !== worker.id)])
    }
    if (auth.user.role === 'CUSTOMER') {
      loadMyProblems(auth.token).catch(() => undefined)
    }
    if (auth.user.role === 'ADMIN') {
      loadAdminData(auth.token).catch(() => undefined)
    }
  }

  const logout = () => {
    localStorage.removeItem('melosmarket_token')
    setAuthToken('')
    setCurrentUser(null)
    setIsLoginOpen(false)
    setLoginMessage('')
    setVerificationModalOpen(false)
    setVerificationMessage('')
    setVerificationState('idle')
    setSelectedWorker(null)
    setCustomerProblems([])
    setCustomerProblemsState('idle')
    setAdminMessage('')
    setAdminState('idle')
  }

  const loadWorkers = async (search = workerSearch, useFallbackWhenEmpty = false) => {
    const params = new URLSearchParams()
    if (search.trade) {
      params.set('trade', tradeApiValues[search.trade])
    }
    if (search.serviceArea.trim()) {
      params.set('serviceArea', search.serviceArea.trim())
    }

    const query = params.toString()
    const response = await fetch(`${apiBaseUrl}/workers${query ? `?${query}` : ''}`)
    if (!response.ok) {
      throw new Error(`Worker search failed with status ${response.status}`)
    }
    const workersFromApi = await response.json()
    const mappedWorkers = workersFromApi.map(workerFromApi)
    setWorkerCards(mappedWorkers.length > 0 || !useFallbackWhenEmpty ? mappedWorkers : initialWorkers)
    return mappedWorkers
  }

  const loadProblems = async (useFallbackWhenEmpty = true) => {
    const response = await fetch(`${apiBaseUrl}/problems`)
    if (!response.ok) {
      throw new Error(`Problem search failed with status ${response.status}`)
    }
    const problemsFromApi = await response.json()
    const mappedProblems = problemsFromApi.map(problemFromApi)
    setProblemPosts(mappedProblems.length > 0 || !useFallbackWhenEmpty ? mappedProblems : initialProblems)
    return mappedProblems
  }

  const loadAdminData = async (token = authToken) => {
    setAdminState('loading')
    setAdminMessage('')

    try {
      const [workersResponse, problemsResponse] = await Promise.all([
        fetch(`${apiBaseUrl}/admin/workers`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }),
        fetch(`${apiBaseUrl}/admin/problems`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }),
      ])

      if (!workersResponse.ok || !problemsResponse.ok) {
        throw new Error('Admin list load failed')
      }

      const [workersFromApi, problemsFromApi] = await Promise.all([
        workersResponse.json(),
        problemsResponse.json(),
      ])
      setWorkerCards(workersFromApi.map(workerFromApi))
      setProblemPosts(problemsFromApi.map(problemFromApi))
      setAdminState('success')
      setAdminMessage('Admin lista frissítve.')
    } catch {
      setAdminState('error')
      setAdminMessage('Nem sikerült betölteni az admin listákat.')
    }
  }

  const loadMyProblems = async (token = authToken) => {
    setCustomerProblemsState('loading')
    const response = await fetch(`${apiBaseUrl}/problems/me`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    if (!response.ok) {
      setCustomerProblemsState('error')
      throw new Error(`Customer problem load failed with status ${response.status}`)
    }
    const problemsFromApi = await response.json()
    const mappedProblems = problemsFromApi.map(problemFromApi)
    setCustomerProblems(mappedProblems)
    setCustomerProblemsState('idle')
    return mappedProblems
  }

  const openWorkerProfile = (worker: WorkerCard, updatePath = true) => {
    setSelectedWorker(worker)
    setProfileEditForm({
      businessName: worker.name,
      contactName: worker.contactName ?? '',
      phone: worker.phone ?? '',
      taxNumber: worker.taxNumber ?? '',
      trade: worker.trade,
      serviceArea: worker.area === 'Nincs megadva' ? '' : worker.area,
      description: worker.description,
      availabilityStatus: worker.availabilityStatus,
      urgentWork: worker.urgentWork,
    })
    setProfileEditMessage('')
    setProfileEditState('idle')
    setReviewForm({ rating: '5', text: '' })
    setReviewSubmitMessage('')
    setReviewSubmitState('idle')
    if (updatePath) {
      window.history.pushState(null, '', workerProfilePath(worker))
    }
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const closeWorkerProfile = () => {
    setSelectedWorker(null)
    window.history.pushState(null, '', '/')
  }

  const showProblemProfile = (problem: ProblemPost) => {
    setSelectedProblem(problem)
    setProblemEditForm({
      title: problem.title,
      description: problem.description,
      trade: problem.trade === 'Nincs megadva' ? '' : problem.trade,
      location: problem.location === 'Nincs megadva' ? '' : problem.location,
      phone: problem.phone ?? '',
    })
    setProblemEditPhotoFiles([])
    setProblemEditMessage('')
    setProblemEditState('idle')
  }

  const openProblemProfile = async (problem: ProblemPost, updatePath = true) => {
    showProblemProfile(problem)
    if (updatePath) {
      window.history.pushState(null, '', problemProfilePath(problem))
    }
    window.scrollTo({ top: 0, behavior: 'smooth' })

    if (!problem.id) {
      return
    }

    try {
      const response = await fetch(`${apiBaseUrl}/problems/${problem.id}`)
      if (!response.ok) {
        throw new Error(`Problem load failed with status ${response.status}`)
      }
      showProblemProfile(problemFromApi(await response.json()))
    } catch {
      showProblemProfile(problem)
    }
  }

  const closeProblemProfile = () => {
    setSelectedProblem(null)
    window.history.pushState(null, '', '/')
  }

  const uploadReferenceWorks = async (event: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? [])
    if (files.length === 0 || !selectedWorker || !authToken) {
      return
    }

    try {
      const uploadedWorks = await Promise.all(
        files.map(async (file) => {
          const formData = new FormData()
          formData.append('image', file)
          formData.append('title', file.name.replace(/\.[^.]+$/, ''))

          const response = await fetch(`${apiBaseUrl}/workers/me/references`, {
            method: 'POST',
            headers: authHeaders(),
            body: formData,
          })

          if (!response.ok) {
            throw new Error(`Reference upload failed with status ${response.status}`)
          }

          const uploadedReference = await response.json()
          return {
            id: String(uploadedReference.id),
            title: uploadedReference.title,
            imageUrl: uploadedReference.imageUrl.startsWith('/api')
              ? `${apiOrigin}${uploadedReference.imageUrl}`
              : uploadedReference.imageUrl,
          }
        }),
      )

      setSelectedWorker((current) =>
        current ? { ...current, referenceWorks: [...uploadedWorks, ...current.referenceWorks] } : current,
      )
      setWorkerCards((current) =>
        current.map((worker) =>
          worker.id === selectedWorker.id
            ? { ...worker, referenceWorks: [...uploadedWorks, ...worker.referenceWorks] }
            : worker,
        ),
      )
      event.target.value = ''
    } catch {
      alert('Nem sikerült feltölteni a referencia képet. Jelentkezz be újra, vagy próbáld később.')
    }
  }

  const deleteReferenceWork = async (work: ReferenceWork) => {
    if (!selectedWorker || !authToken) {
      return
    }

    const confirmed = window.confirm(`Biztosan törlöd ezt a referencia képet: ${work.title}?`)
    if (!confirmed) {
      return
    }

    try {
      const response = await fetch(`${apiBaseUrl}/workers/me/references/${work.id}`, {
        method: 'DELETE',
        headers: authHeaders(),
      })

      if (!response.ok) {
        throw new Error(`Reference delete failed with status ${response.status}`)
      }

      setSelectedWorker((current) =>
        current
          ? { ...current, referenceWorks: current.referenceWorks.filter((reference) => reference.id !== work.id) }
          : current,
      )
      setWorkerCards((current) =>
        current.map((worker) =>
          worker.id === selectedWorker.id
            ? { ...worker, referenceWorks: worker.referenceWorks.filter((reference) => reference.id !== work.id) }
            : worker,
        ),
      )
    } catch {
      alert('Nem sikerült törölni a referencia képet. Csak a saját képeidet törölheted.')
    }
  }

  const submitWorkerReview = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!selectedWorker?.id) {
      return
    }

    setReviewSubmitState('submitting')
    setReviewSubmitMessage('')

    try {
      const response = await fetch(`${apiBaseUrl}/workers/${selectedWorker.id}/reviews`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...authHeaders(),
        },
        body: JSON.stringify({
          rating: Number(reviewForm.rating),
          text: reviewForm.text,
        }),
      })

      if (!response.ok) {
        throw new Error(`Worker review failed with status ${response.status}`)
      }

      const savedReview = await response.json()
      const mappedReview: Review = {
        id: String(savedReview.id),
        reviewerWorkerId: savedReview.reviewerWorkerId,
        reviewerName: savedReview.reviewerName,
        rating: savedReview.rating,
        text: savedReview.text,
        createdAt: savedReview.createdAt,
        updatedAt: savedReview.updatedAt,
      }
      const replaceReview = (reviews: Review[]) => [
        mappedReview,
        ...reviews.filter((review) => review.reviewerWorkerId !== mappedReview.reviewerWorkerId),
      ]

      setSelectedWorker((current) =>
        current ? { ...current, reviews: replaceReview(current.reviews) } : current,
      )
      setWorkerCards((current) =>
        current.map((worker) =>
          worker.id === selectedWorker.id ? { ...worker, reviews: replaceReview(worker.reviews) } : worker,
        ),
      )
      setReviewForm({ rating: '5', text: '' })
      setReviewSubmitState('success')
      setReviewSubmitMessage('Köszönjük, az értékelésed mentve lett.')
    } catch {
      setReviewSubmitState('error')
      setReviewSubmitMessage('Nem sikerült menteni az értékelést. Csak másik szakember profilját értékelheted.')
    }
  }

  useEffect(() => {
    loadWorkers(workerSearch, true).catch(() => {
      setWorkerCards(initialWorkers)
    })
    loadProblems(true).catch(() => {
      setProblemPosts(initialProblems)
    })
  }, [])

  useEffect(() => {
    const openProfileFromPath = () => {
      const pathSlug = window.location.pathname.replace(/^\/+|\/+$/g, '')
      if (!pathSlug) {
        setSelectedWorker(null)
        setSelectedProblem(null)
        return
      }
      if (ignoredRouteSlugs.has(pathSlug)) {
        return
      }

      const worker = workerCards.find((item) => workerProfileSlug(item) === pathSlug)
      if (worker) {
        openWorkerProfile(worker, false)
        setSelectedProblem(null)
        return
      }

      const problem = problemPosts.find((item) => problemProfileSlug(item) === pathSlug)
      if (problem) {
        setSelectedWorker(null)
        openProblemProfile(problem, false).catch(() => undefined)
      }
    }

    openProfileFromPath()
    window.addEventListener('popstate', openProfileFromPath)
    return () => window.removeEventListener('popstate', openProfileFromPath)
  }, [workerCards, problemPosts])

  useEffect(() => {
    if (!authToken) {
      return
    }

    fetch(`${apiBaseUrl}/auth/me`, {
      headers: authHeaders(),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`Current user failed with status ${response.status}`)
        }
        return response.json()
      })
      .then((user) => {
        setCurrentUser(user)
        if (!user.emailVerified) {
          setVerificationModalOpen(true)
          setVerificationMessage('Kérjük, erősítsd meg az email címedet a fiók teljes használatához.')
        }
        if (user.role === 'CUSTOMER') {
          loadMyProblems().catch(() => undefined)
        }
        if (user.role === 'ADMIN') {
          loadAdminData().catch(() => undefined)
        }
      })
      .catch(logout)
  }, [authToken])

  useEffect(() => {
    const token = new URLSearchParams(window.location.search).get('emailVerificationToken')
    if (!token) {
      return
    }

    setVerificationModalOpen(true)
    setVerificationState('submitting')
    setVerificationMessage('Email cím megerősítése folyamatban...')

    fetch(`${apiBaseUrl}/auth/verify-email`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ token }),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`Email verification failed with status ${response.status}`)
        }
        return response.json()
      })
      .then((user: CurrentUser) => {
        setCurrentUser((current) => (current ? { ...current, emailVerified: true } : user))
        setVerificationState('success')
        setVerificationMessage('Sikeresen megerősítetted az email címedet. Köszönjük!')
        window.history.replaceState(null, '', window.location.pathname)
      })
      .catch(() => {
        setVerificationState('error')
        setVerificationMessage('Nem sikerült megerősíteni az email címet. Lehet, hogy a link lejárt vagy már fel lett használva.')
      })
  }, [])

  const resendVerificationEmail = async () => {
    setVerificationState('submitting')
    setVerificationMessage('')

    try {
      const response = await fetch(`${apiBaseUrl}/auth/resend-verification-email`, {
        method: 'POST',
        headers: authHeaders(),
      })
      if (!response.ok) {
        throw new Error(`Resend verification failed with status ${response.status}`)
      }
      setVerificationState('success')
      setVerificationMessage('Új megerősítő emailt küldtünk. Nézd meg a bejövő leveleket és a spam mappát is.')
    } catch {
      setVerificationState('error')
      setVerificationMessage('Nem sikerült újraküldeni a megerősítő emailt.')
    }
  }

  const submitLogin = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setLoginState('submitting')
    setLoginMessage('')

    try {
      const response = await fetch(`${apiBaseUrl}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(loginForm),
      })

      if (!response.ok) {
        throw new Error(`Login failed with status ${response.status}`)
      }

      const auth = await response.json()
      rememberAuth(auth)
      setLoginForm({ email: '', password: '' })
      setLoginState('success')
      setLoginMessage(auth.user.role === 'CUSTOMER' ? 'Sikeres belépés az ügyfél fiókba.' : 'Sikeres bejelentkezés.')
      setIsLoginOpen(false)
    } catch {
      setLoginState('error')
      setLoginMessage('Nem sikerült bejelentkezni. Ellenőrizd az email címet és a jelszót.')
    }
  }

  const updateWorkerForm = (field: keyof WorkerFormState, value: string) => {
    setWorkerForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  const updateCustomerForm = (field: keyof CustomerRegistrationFormState, value: string) => {
    setCustomerForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  const updateProblemForm = (field: keyof ProblemFormState, value: string) => {
    setProblemForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  const updateProblemPhotos = (event: ChangeEvent<HTMLInputElement>) => {
    setProblemPhotoFiles(Array.from(event.target.files ?? []))
  }

  const updateProblemEditForm = (field: keyof ProblemFormState, value: string) => {
    setProblemEditForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  const updateProblemEditPhotos = (event: ChangeEvent<HTMLInputElement>) => {
    setProblemEditPhotoFiles(Array.from(event.target.files ?? []))
  }

  const submitWorkerSearch = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setWorkerSearchStatus('loading')
    setWorkerSearchMessage('')

    try {
      const results = await loadWorkers(workerSearch)
      const hasFilters = Boolean(workerSearch.trade || workerSearch.serviceArea.trim())
      if (results.length === 0) {
        setWorkerSearchMessage(
          hasFilters
            ? 'Nincs találat erre a városra és szakmára. Próbálj meg másik szűrést.'
            : 'Még nincs regisztrált szakember az adatbázisban.',
        )
      } else {
        setWorkerSearchMessage(`${results.length} szakember található a megadott szűrésre.`)
      }
      setWorkerSearchStatus('idle')
    } catch {
      setWorkerSearchStatus('error')
      setWorkerSearchMessage('Nem sikerült betölteni a szakembereket. Ellenőrizd, hogy fut-e a backend.')
    }
  }

  const clearWorkerSearch = async () => {
    const emptySearch = {
      trade: '',
      serviceArea: '',
    }
    setWorkerSearch(emptySearch)
    setWorkerSearchMessage('')
    setWorkerSearchStatus('loading')

    try {
      await loadWorkers(emptySearch, true)
      setWorkerSearchStatus('idle')
    } catch {
      setWorkerCards(initialWorkers)
      setWorkerSearchStatus('error')
      setWorkerSearchMessage('Nem sikerült újratölteni a szakembereket.')
    }
  }

  const deleteAdminWorker = async (worker: WorkerCard) => {
    if (!worker.id) {
      return
    }
    const confirmed = window.confirm(`Biztosan törlöd ezt a szakember profilt: ${worker.name}?`)
    if (!confirmed) {
      return
    }

    setAdminState('loading')
    setAdminMessage('')
    try {
      const response = await fetch(`${apiBaseUrl}/admin/workers/${worker.id}`, {
        method: 'DELETE',
        headers: authHeaders(),
      })
      if (!response.ok) {
        throw new Error(`Worker delete failed with status ${response.status}`)
      }
      setWorkerCards((current) => current.filter((item) => item.id !== worker.id))
      if (selectedWorker?.id === worker.id) {
        setSelectedWorker(null)
      }
      setAdminState('success')
      setAdminMessage('Szakember profil törölve.')
    } catch {
      setAdminState('error')
      setAdminMessage('Nem sikerült törölni a szakember profilt.')
    }
  }

  const updateAdminWorkerBadge = async (worker: WorkerCard, badgeKey: WorkerBadgeKey, enabled: boolean) => {
    if (!worker.id) {
      return
    }

    setAdminState('loading')
    setAdminMessage('')
    try {
      const response = await fetch(`${apiBaseUrl}/admin/workers/${worker.id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          ...authHeaders(),
        },
        body: JSON.stringify({
          verified: badgeKey === 'verified' ? enabled : worker.verified,
          topWorker: badgeKey === 'topWorker' ? enabled : worker.topWorker,
          manyReferences: badgeKey === 'manyReferences' ? enabled : worker.manyReferences,
          hundredJobs: badgeKey === 'hundredJobs' ? enabled : worker.hundredJobs,
          fastResponder: badgeKey === 'fastResponder' ? enabled : worker.fastResponder,
        }),
      })
      if (!response.ok) {
        throw new Error(`Worker badge update failed with status ${response.status}`)
      }
      const updatedWorker = workerFromApi(await response.json())
      setWorkerCards((current) =>
        current.map((item) => (item.id === updatedWorker.id ? updatedWorker : item)),
      )
      setSelectedWorker((current) => (current?.id === updatedWorker.id ? updatedWorker : current))
      setAdminState('success')
      setAdminMessage(enabled ? 'Jelvény megadva.' : 'Jelvény levéve.')
    } catch {
      setAdminState('error')
      setAdminMessage('Nem sikerült frissíteni a jelvényt.')
    }
  }

  const deleteAdminProblem = async (problem: ProblemPost) => {
    if (!problem.id) {
      return
    }
    const confirmed = window.confirm(`Biztosan törlöd ezt a problémát: ${problem.title}?`)
    if (!confirmed) {
      return
    }

    setAdminState('loading')
    setAdminMessage('')
    try {
      const response = await fetch(`${apiBaseUrl}/admin/problems/${problem.id}`, {
        method: 'DELETE',
        headers: authHeaders(),
      })
      if (!response.ok) {
        throw new Error(`Problem delete failed with status ${response.status}`)
      }
      setProblemPosts((current) => current.filter((item) => item.id !== problem.id))
      setCustomerProblems((current) => current.filter((item) => item.id !== problem.id))
      if (selectedProblem?.id === problem.id) {
        setSelectedProblem(null)
      }
      setAdminState('success')
      setAdminMessage('Probléma törölve.')
    } catch {
      setAdminState('error')
      setAdminMessage('Nem sikerült törölni a problémát.')
    }
  }

  const updateProfileEditForm = <Field extends keyof WorkerProfileEditState>(
    field: Field,
    value: WorkerProfileEditState[Field],
  ) => {
    setProfileEditForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  const submitProfileEdit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setProfileEditState('submitting')
    setProfileEditMessage('')

    try {
      const response = await fetch(`${apiBaseUrl}/workers/me`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...authHeaders(),
        },
        body: JSON.stringify({
          businessName: profileEditForm.businessName,
          contactName: profileEditForm.contactName,
          phone: profileEditForm.phone || undefined,
          taxNumber: profileEditForm.taxNumber || undefined,
          trade: tradeApiValues[profileEditForm.trade],
          serviceArea: profileEditForm.serviceArea || undefined,
          description: profileEditForm.description || undefined,
          availabilityStatus: profileEditForm.availabilityStatus,
          urgentWork: profileEditForm.urgentWork,
        }),
      })

      if (!response.ok) {
        throw new Error(`Profile update failed with status ${response.status}`)
      }

      const updatedWorker = workerFromApi(await response.json())
      setSelectedWorker(updatedWorker)
      setWorkerCards((current) =>
        current.map((worker) => (worker.id === updatedWorker.id ? updatedWorker : worker)),
      )
      setProfileEditState('success')
      setProfileEditMessage('A profil frissítve.')
    } catch {
      setProfileEditState('error')
      setProfileEditMessage('Nem sikerült frissíteni a profilt.')
    }
  }

  const submitWorker = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setWorkerSubmitState('submitting')
    setWorkerSubmitMessage('')
    const registerWorkerUrl = `${apiBaseUrl}/auth/register/worker`

    try {
      const response = await fetch(registerWorkerUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          businessName: workerForm.businessName,
          contactName: workerForm.contactName,
          email: workerForm.email,
          password: workerForm.password,
          phone: workerForm.phone || undefined,
          taxNumber: workerForm.taxNumber || undefined,
          trade: tradeApiValues[workerForm.trade],
          serviceArea: workerForm.serviceArea || undefined,
          description: workerForm.description || undefined,
        }),
      })

      if (!response.ok) {
        if (response.status === 409) {
          setWorkerSubmitMessage(
            'Ez az email cím már regisztrálva van. Próbálj belépni, vagy használj másik email címet.',
          )
        } else if (response.status === 400) {
          setWorkerSubmitMessage('Ellenőrizd a megadott adatokat, mert valamelyik mező hibás vagy hiányzik.')
        } else {
          setWorkerSubmitMessage('Nem sikerült regisztrálni. Ellenőrizd, hogy fut-e a backend, majd próbáld újra.')
        }
        setWorkerSubmitState('error')
        return
      }

      const auth = await response.json()
      rememberAuth(auth)
      setWorkerForm({
        businessName: '',
        contactName: '',
        email: '',
        password: '',
        phone: '',
        taxNumber: '',
        trade: '',
        serviceArea: '',
        description: '',
      })
      setWorkerSubmitState('success')
      setWorkerSubmitMessage('A szakember regisztrációja sikeres. Be is jelentkeztél.')
    } catch (error) {
      console.error('Worker registration failed', error)
      setWorkerSubmitState('error')
      setWorkerSubmitMessage('Nem sikerült kapcsolódni a backendhez. Ellenőrizd, hogy fut-e, majd próbáld újra.')
    }
  }

  const submitCustomerRegistration = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setCustomerSubmitState('submitting')
    setCustomerSubmitMessage('')

    try {
      const response = await fetch(`${apiBaseUrl}/auth/register/customer`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(customerForm),
      })

      if (!response.ok) {
        throw new Error(`Customer registration failed with status ${response.status}`)
      }

      const auth = await response.json()
      rememberAuth(auth)
      setCustomerForm({ email: '', password: '' })
      setCustomerSubmitState('success')
      setCustomerSubmitMessage('Sikeres regisztráció. Most már feltöltheted a problémádat.')
    } catch {
      setCustomerSubmitState('error')
      setCustomerSubmitMessage('Nem sikerült regisztrálni. Lehet, hogy ez az email cím már használatban van.')
    }
  }

  const submitProblem = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitState('submitting')
    setSubmitMessage('')

    if (!isCustomer) {
      setSubmitState('error')
      setSubmitMessage('Probléma feltöltéséhez először ügyfélként kell regisztrálnod vagy belépned.')
      return
    }

    try {
      const response = await fetch(`${apiBaseUrl}/problems`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...authHeaders(),
        },
        body: JSON.stringify({
          title: problemForm.title,
          description: problemForm.description,
          trade: tradeApiValues[problemForm.trade],
          location: problemForm.location,
          phone: problemForm.phone || undefined,
        }),
      })

      if (!response.ok) {
        throw new Error(`Problem creation failed with status ${response.status}`)
      }

      const createdProblem = await response.json()
      const uploadedImages = await Promise.all(
        problemPhotoFiles.map(async (file) => {
          const formData = new FormData()
          formData.append('image', file)
          formData.append('title', file.name.replace(/\.[^.]+$/, ''))

          const photoResponse = await fetch(`${apiBaseUrl}/problems/${createdProblem.id}/photos`, {
            method: 'POST',
            headers: authHeaders(),
            body: formData,
          })

          if (!photoResponse.ok) {
            throw new Error(`Problem photo upload failed with status ${photoResponse.status}`)
          }

          return problemImageFromApi(await photoResponse.json())
        }),
      )

      const createdProblemCard = {
        ...problemFromApi(createdProblem),
        trade: problemForm.trade,
        location: createdProblem.location ?? problemForm.location,
        posted: 'épp most',
        images: uploadedImages,
      }
      setProblemPosts((current) => [createdProblemCard, ...current])
      setCustomerProblems((current) => [createdProblemCard, ...current])
      setProblemForm({
        title: '',
        description: '',
        trade: '',
        location: '',
        phone: '',
      })
      setProblemPhotoFiles([])
      setSubmitState('success')
      setSubmitMessage('A problémát sikeresen feltöltöttük.')
    } catch {
      setSubmitState('error')
      setSubmitMessage('Nem sikerült feltölteni a problémát. Ellenőrizd, hogy fut-e a backend.')
    }
  }

  const submitProblemEdit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!selectedProblem?.id) {
      return
    }

    setProblemEditState('submitting')
    setProblemEditMessage('')

    try {
      const response = await fetch(`${apiBaseUrl}/problems/${selectedProblem.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...authHeaders(),
        },
        body: JSON.stringify({
          title: problemEditForm.title,
          description: problemEditForm.description,
          trade: problemEditForm.trade ? tradeApiValues[problemEditForm.trade] : undefined,
          location: problemEditForm.location || undefined,
          phone: problemEditForm.phone || undefined,
        }),
      })

      if (!response.ok) {
        throw new Error(`Problem update failed with status ${response.status}`)
      }

      const updatedProblem = problemFromApi(await response.json())
      updateProblemEverywhere(updatedProblem)
      window.history.replaceState(null, '', problemProfilePath(updatedProblem))
      setProblemEditState('success')
      setProblemEditMessage('A probléma frissítve.')
    } catch {
      setProblemEditState('error')
      setProblemEditMessage('Nem sikerült frissíteni a problémát.')
    }
  }

  const uploadProblemEditPhotos = async () => {
    if (!selectedProblem?.id || problemEditPhotoFiles.length === 0) {
      return
    }

    setProblemEditState('submitting')
    setProblemEditMessage('')

    try {
      const uploadedImages = await Promise.all(
        problemEditPhotoFiles.map(async (file) => {
          const formData = new FormData()
          formData.append('image', file)
          formData.append('title', file.name.replace(/\.[^.]+$/, ''))

          const response = await fetch(`${apiBaseUrl}/problems/${selectedProblem.id}/photos`, {
            method: 'POST',
            headers: authHeaders(),
            body: formData,
          })

          if (!response.ok) {
            throw new Error(`Problem photo upload failed with status ${response.status}`)
          }

          return problemImageFromApi(await response.json())
        }),
      )

      const updatedProblem = {
        ...selectedProblem,
        images: [...uploadedImages, ...selectedProblem.images],
      }
      updateProblemEverywhere(updatedProblem)
      setProblemEditPhotoFiles([])
      setProblemEditState('success')
      setProblemEditMessage('A képek feltöltve.')
    } catch {
      setProblemEditState('error')
      setProblemEditMessage('Nem sikerült feltölteni a képeket.')
    }
  }

  const deleteProblemPhoto = async (image: ProblemImage) => {
    if (!selectedProblem?.id) {
      return
    }
    const confirmed = window.confirm(`Biztosan törlöd ezt a képet: ${image.title}?`)
    if (!confirmed) {
      return
    }

    setProblemEditState('submitting')
    setProblemEditMessage('')

    try {
      const response = await fetch(`${apiBaseUrl}/problems/${selectedProblem.id}/photos/${image.id}`, {
        method: 'DELETE',
        headers: authHeaders(),
      })

      if (!response.ok) {
        throw new Error(`Problem photo delete failed with status ${response.status}`)
      }

      const updatedProblem = {
        ...selectedProblem,
        images: selectedProblem.images.filter((item) => item.id !== image.id),
      }
      updateProblemEverywhere(updatedProblem)
      setProblemEditState('success')
      setProblemEditMessage('A kép törölve.')
    } catch {
      setProblemEditState('error')
      setProblemEditMessage('Nem sikerült törölni a képet.')
    }
  }

  if (selectedProblem) {
    const isOwnProblem = Boolean(
      isCustomer && selectedProblem.id && customerProblems.some((problem) => problem.id === selectedProblem.id),
    )

    return (
      <main className="page-shell">
        <header className="site-header">
          <button className="brand brand-button" type="button" onClick={closeProblemProfile}>
            <span className="brand-mark">M</span>
            <span>Melos Market</span>
          </button>

          <button className="header-action" type="button" onClick={closeProblemProfile}>
            Vissza a munkákhoz
          </button>
        </header>

        <section className="profile-hero problem-hero">
          <div>
            <p className="eyebrow">Probléma adatlap</p>
            <h1>{selectedProblem.title}</h1>
            <p>{selectedProblem.description}</p>
            <div className="profile-tags">
              <span className="trade-pill">{selectedProblem.trade}</span>
              <span>{selectedProblem.location}</span>
              <span>{selectedProblem.posted}</span>
            </div>
          </div>

          <div className="profile-score">
            <strong>{selectedProblem.images.length}</strong>
            <span>{selectedProblem.images.length === 1 ? 'feltöltött fotó' : 'feltöltött fotó'}</span>
          </div>
        </section>

        <section className="profile-grid">
          <div className="profile-panel">
            <p className="section-kicker">Ügyfél probléma</p>
            <h2>Részletes leírás</h2>
            <p>{selectedProblem.description}</p>
            <dl className="profile-details">
              <div>
                <dt>Szakma</dt>
                <dd>{selectedProblem.trade}</dd>
              </div>
              <div>
                <dt>Helyszín</dt>
                <dd>{selectedProblem.location}</dd>
              </div>
              {selectedProblem.phone && (
                <div>
                  <dt>Telefonszám</dt>
                  <dd>{selectedProblem.phone}</dd>
                </div>
              )}
            </dl>
          </div>

          <div className="profile-panel">
            <p className="section-kicker">Szakembereknek</p>
            <h2>Kapcsolatfelvétel</h2>
            <p>
              Ha a munka illik hozzád, a megadott telefonszámon tudsz egyeztetni az ügyféllel.
            </p>
          </div>

          {isOwnProblem && (
            <div className="profile-panel">
              <p className="section-kicker">Saját probléma</p>
              <h2>Probléma szerkesztése</h2>
              <form className="profile-edit-form" onSubmit={submitProblemEdit}>
                <div className="field">
                  <label htmlFor="problem-edit-title">Probléma címe</label>
                  <input
                    id="problem-edit-title"
                    minLength={3}
                    maxLength={180}
                    required
                    value={problemEditForm.title}
                    onChange={(event) => updateProblemEditForm('title', event.target.value)}
                  />
                </div>
                <div className="field">
                  <label htmlFor="problem-edit-description">Leírás</label>
                  <textarea
                    id="problem-edit-description"
                    minLength={10}
                    maxLength={4000}
                    rows={4}
                    required
                    value={problemEditForm.description}
                    onChange={(event) => updateProblemEditForm('description', event.target.value)}
                  />
                </div>
                <div className="field">
                  <label htmlFor="problem-edit-trade">Szakma</label>
                  <select
                    id="problem-edit-trade"
                    value={problemEditForm.trade}
                    onChange={(event) => updateProblemEditForm('trade', event.target.value)}
                  >
                    <option value="">Nincs megadva</option>
                    {trades.map((trade) => (
                      <option key={trade}>{trade}</option>
                    ))}
                  </select>
                </div>
                <div className="field">
                  <label htmlFor="problem-edit-location">Helyszín</label>
                  <input
                    id="problem-edit-location"
                    maxLength={180}
                    value={problemEditForm.location}
                    onChange={(event) => updateProblemEditForm('location', event.target.value)}
                  />
                </div>
                <div className="field">
                  <label htmlFor="problem-edit-phone">Telefonszám</label>
                  <input
                    id="problem-edit-phone"
                    maxLength={50}
                    value={problemEditForm.phone}
                    onChange={(event) => updateProblemEditForm('phone', event.target.value)}
                  />
                </div>
                <button type="submit" className="button primary" disabled={problemEditState === 'submitting'}>
                  {problemEditState === 'submitting' ? 'Mentés...' : 'Probléma mentése'}
                </button>
              </form>

              <h2>Képek kezelése</h2>
              <label className="upload-box" htmlFor="problem-edit-photos">
                <span>Új képek feltöltése</span>
                <small>JPG vagy PNG képeket adhatsz hozzá ehhez a problémához</small>
                <input
                  id="problem-edit-photos"
                  type="file"
                  accept="image/png,image/jpeg"
                  multiple
                  onChange={updateProblemEditPhotos}
                />
              </label>
              <button
                type="button"
                className="button secondary"
                disabled={problemEditState === 'submitting' || problemEditPhotoFiles.length === 0}
                onClick={uploadProblemEditPhotos}
              >
                {problemEditPhotoFiles.length > 0
                  ? `${problemEditPhotoFiles.length} kép feltöltése`
                  : 'Válassz képeket'}
              </button>
              {problemEditMessage && (
                <p className={`form-message ${problemEditState === 'success' ? 'success' : 'error'}`} role="status">
                  {problemEditMessage}
                </p>
              )}
            </div>
          )}
        </section>

        {selectedProblem.images.length > 0 && (
          <section className="section-block">
            <div className="section-heading">
              <div>
                <p className="section-kicker">Fotók</p>
                <h2>Feltöltött képek a problémáról</h2>
              </div>
            </div>

            <div className="reference-grid">
              {selectedProblem.images.map((image) => (
                <article className="reference-card" key={image.id}>
                  {isOwnProblem && (
                    <button
                      className="reference-delete-button"
                      type="button"
                      aria-label={`${image.title} törlése`}
                      onClick={() => deleteProblemPhoto(image)}
                    >
                      ×
                    </button>
                  )}
                  <img src={image.imageUrl} alt={image.title} />
                  <h3>{image.title}</h3>
                </article>
              ))}
            </div>
          </section>
        )}
      </main>
    )
  }

  if (selectedWorker) {
    const isOwnProfile = Boolean(currentUser?.workerId && selectedWorker.id === currentUser.workerId)
    const displayedReferenceWorks =
      selectedWorker.referenceWorks.length > 0 ? selectedWorker.referenceWorks : defaultReferenceWorks
    const missingReferenceCount = Math.max(0, 5 - selectedWorker.referenceWorks.length)
    const hasIntroduction = selectedWorker.description.trim().length >= 80
    const completedProfileParts = [
      Boolean(selectedWorker.name.trim()),
      Boolean(selectedWorker.contactName?.trim()),
      Boolean(selectedWorker.phone?.trim()),
      Boolean(selectedWorker.email?.trim()),
      selectedWorker.trade !== 'Nincs megadva',
      selectedWorker.area !== 'Nincs megadva',
      hasIntroduction,
      selectedWorker.referenceWorks.length >= 1,
      selectedWorker.referenceWorks.length >= 3,
      selectedWorker.referenceWorks.length >= 5,
    ].filter(Boolean).length
    const profileCompletion = Math.round((completedProfileParts / 10) * 100)
    const profileCompletionTips = [
      missingReferenceCount > 0
        ? `Még tölts fel ${missingReferenceCount} képet`
        : '',
      !hasIntroduction ? 'írj részletesebb bemutatkozást' : '',
      !selectedWorker.phone ? 'adj meg telefonszámot' : '',
      selectedWorker.area === 'Nincs megadva' ? 'add meg a szolgáltatási területedet' : '',
    ].filter(Boolean)
    const profileCompletionMessage =
      profileCompletionTips.length > 0
        ? `${profileCompletionTips.join(' és ')}, hogy előrébb kerülj a találatok között.`
        : 'Szuper, a profilod erős állapotban van. Tartsd frissen a referenciákat és az elérhetőségeket.'
    const trialPanelCopy =
      selectedWorker.accessStatus === 'TRIALING'
        ? `Még ${selectedWorker.trialDaysRemaining} nap van hátra az ingyenes próbaidőből. Addig a profilod megjelenik a találatok között.`
        : selectedWorker.accessStatus === 'TRIAL_EXPIRED'
          ? 'A 30 napos próbaidőd lejárt. Előfizetés szükséges, hogy a profilod újra megjelenjen a találatok között.'
          : selectedWorker.accessStatus === 'ACTIVE'
            ? 'Az előfizetésed aktív, a profilod megjelenhet a találatok között.'
            : 'A fizetésed rendezésre vár. A profilod csak akkor jelenik meg újra, ha az előfizetés rendben van.'

    return (
      <main className="page-shell">
        <header className="site-header">
          <button className="brand brand-button" type="button" onClick={closeWorkerProfile}>
            <span className="brand-mark">M</span>
            <span>Melos Market</span>
          </button>

          <button className="header-action" type="button" onClick={closeWorkerProfile}>
            Vissza a piactérre
          </button>
        </header>

        <section className="profile-hero">
          <div>
            <p className="eyebrow">Szakember profil</p>
            <div className="profile-title-row">
              <h1>{selectedWorker.name}</h1>
              {selectedWorker.verified && (
                <span className="verified-checkmark" aria-label="Ellenőrzött szakember" title="Ellenőrzött szakember">
                  ✓
                </span>
              )}
            </div>
            <p>{selectedWorker.description}</p>
            <div className="profile-tags">
              {activeWorkerBadges(selectedWorker).map((badge) => (
                <span className="worker-badge" key={badge.key}>{badge.label}</span>
              ))}
              <span className="trade-pill">{selectedWorker.trade}</span>
              <span>{selectedWorker.area}</span>
              <span>{selectedWorker.jobs}</span>
            </div>
          </div>

          <div className="profile-score">
            <strong>{selectedWorker.rating}</strong>
            <span>{selectedWorker.rating === 'Új' ? 'Új szakember' : 'átlagos értékelés'}</span>
          </div>
        </section>

        <section className="profile-grid">
          <div className="profile-panel">
            <p className="section-kicker">Bemutatkozás</p>
            <h2>Részletes szakmai profil</h2>
            <p>
              Itt jelenik meg a szakember részletes bemutatkozása, vállalt munkái,
              szolgáltatási területei, elérhetőségei és később az igazolt referencia anyagai.
            </p>
            <dl className="profile-details">
              <div>
                <dt>Szakma</dt>
                <dd>{selectedWorker.trade}</dd>
              </div>
              <div>
                <dt>Terület</dt>
                <dd>{selectedWorker.area}</dd>
              </div>
              <div>
                <dt>Elvégzett munkák</dt>
                <dd>{selectedWorker.jobs}</dd>
              </div>
              {selectedWorker.email && (
                <div>
                  <dt>Email</dt>
                  <dd>{selectedWorker.email}</dd>
                </div>
              )}
              {selectedWorker.phone && (
                <div>
                  <dt>Telefon</dt>
                  <dd>{selectedWorker.phone}</dd>
                </div>
              )}
              {selectedWorker.taxNumber && (
                <div>
                  <dt>Adószám</dt>
                  <dd>{selectedWorker.taxNumber}</dd>
                </div>
              )}
            </dl>
          </div>

          {isOwnProfile && (
            <div className="profile-panel">
              <p className="section-kicker">Saját profil</p>
              <div className={`trial-card ${selectedWorker.accessStatus === 'TRIAL_EXPIRED' ? 'expired' : ''}`}>
                <div className="trial-card-header">
                  <div>
                    <h2>{accessStatusLabels[selectedWorker.accessStatus]}</h2>
                    <p>{trialPanelCopy}</p>
                  </div>
                  <strong>
                    {selectedWorker.accessStatus === 'TRIALING'
                      ? `${selectedWorker.trialDaysRemaining} nap`
                      : accessStatusLabels[selectedWorker.accessStatus]}
                  </strong>
                </div>
                <div className="trial-meta">
                  <span>Próba kezdete: {formatHungarianDate(selectedWorker.trialStartedAt)}</span>
                  <span>Próba vége: {formatHungarianDate(selectedWorker.trialEndsAt)}</span>
                </div>
              </div>
              <div className="completion-card" aria-label="Profil kitöltöttsége">
                <div className="completion-header">
                  <div>
                    <h2>Profil kitöltöttsége</h2>
                    <p>{profileCompletionMessage}</p>
                  </div>
                  <strong>{profileCompletion}%</strong>
                </div>
                <div className="completion-bar" aria-hidden="true">
                  <span style={{ width: `${profileCompletion}%` }} />
                </div>
              </div>
              <h2>Profil szerkesztése</h2>
              <form className="profile-edit-form" onSubmit={submitProfileEdit}>
                <div className="field">
                  <label htmlFor="profile-business-name">Vállalkozás neve</label>
                  <input
                    id="profile-business-name"
                    minLength={2}
                    maxLength={180}
                    required
                    value={profileEditForm.businessName}
                    onChange={(event) => updateProfileEditForm('businessName', event.target.value)}
                  />
                </div>
                <div className="field">
                  <label htmlFor="profile-contact-name">Kapcsolattartó</label>
                  <input
                    id="profile-contact-name"
                    minLength={2}
                    maxLength={160}
                    required
                    value={profileEditForm.contactName}
                    onChange={(event) => updateProfileEditForm('contactName', event.target.value)}
                  />
                </div>
                <div className="field">
                  <label htmlFor="profile-phone">Telefon</label>
                  <input
                    id="profile-phone"
                    maxLength={50}
                    value={profileEditForm.phone}
                    onChange={(event) => updateProfileEditForm('phone', event.target.value)}
                  />
                </div>
                <div className="field">
                  <label htmlFor="profile-tax-number">Adószám</label>
                  <input
                    id="profile-tax-number"
                    maxLength={50}
                    placeholder="12345678-1-42"
                    value={profileEditForm.taxNumber}
                    onChange={(event) => updateProfileEditForm('taxNumber', event.target.value)}
                  />
                </div>
                <div className="field">
                  <label htmlFor="profile-trade">Szakma</label>
                  <select
                    id="profile-trade"
                    required
                    value={profileEditForm.trade}
                    onChange={(event) => updateProfileEditForm('trade', event.target.value)}
                  >
                    {trades.map((trade) => (
                      <option key={trade}>{trade}</option>
                    ))}
                  </select>
                </div>
                <div className="field">
                  <label htmlFor="profile-service-area">Szolgáltatási terület</label>
                  <input
                    id="profile-service-area"
                    maxLength={160}
                    value={profileEditForm.serviceArea}
                    onChange={(event) => updateProfileEditForm('serviceArea', event.target.value)}
                  />
                </div>
                <div className="field">
                  <label htmlFor="profile-description">Bemutatkozás</label>
                  <textarea
                    id="profile-description"
                    maxLength={2000}
                    rows={4}
                    value={profileEditForm.description}
                    onChange={(event) => updateProfileEditForm('description', event.target.value)}
                  />
                </div>
                <div className="field">
                  <label htmlFor="profile-availability">Kapacitás</label>
                  <select
                    id="profile-availability"
                    value={profileEditForm.availabilityStatus}
                    onChange={(event) =>
                      updateProfileEditForm('availabilityStatus', event.target.value as WorkerAvailabilityStatus)
                    }
                  >
                    {workerAvailabilityDefinitions.map((availability) => (
                      <option key={availability.value} value={availability.value}>
                        {availability.label}
                      </option>
                    ))}
                  </select>
                </div>
                <label className="checkbox-field" htmlFor="profile-urgent-work">
                  <input
                    id="profile-urgent-work"
                    type="checkbox"
                    checked={profileEditForm.urgentWork}
                    onChange={(event) => updateProfileEditForm('urgentWork', event.target.checked)}
                  />
                  <span>Sürgős munkát is vállalok</span>
                </label>
                <button type="submit" className="button primary" disabled={profileEditState === 'submitting'}>
                  {profileEditState === 'submitting' ? 'Mentés...' : 'Profil mentése'}
                </button>
                {profileEditMessage && (
                  <p className={`form-message ${profileEditState === 'success' ? 'success' : 'error'}`} role="status">
                    {profileEditMessage}
                  </p>
                )}
              </form>

              <h2>Korábbi munkák feltöltése</h2>
              <label className="upload-box" htmlFor="reference-upload">
                <span>Képek feltöltése</span>
                <small>JPG vagy PNG referencia képek a nyilvános profilodra</small>
                <input
                  id="reference-upload"
                  type="file"
                  accept="image/png,image/jpeg"
                  multiple
                  onChange={uploadReferenceWorks}
                />
              </label>
            </div>
          )}
        </section>

        <section className="section-block">
          <div className="section-heading">
            <div>
              <p className="section-kicker">Portfólió</p>
              <h2>Referencia munkák</h2>
            </div>
          </div>

          <div className="reference-grid">
            {displayedReferenceWorks.map((work) => (
              <article className="reference-card" key={work.id}>
                {isOwnProfile && selectedWorker.referenceWorks.some((reference) => reference.id === work.id) && (
                  <button
                    className="reference-delete-button"
                    type="button"
                    aria-label={`${work.title} törlése`}
                    onClick={() => deleteReferenceWork(work)}
                  >
                    X
                  </button>
                )}
                <img src={work.imageUrl} alt={work.title} />
                <h3>{work.title}</h3>
              </article>
            ))}
          </div>
        </section>

        <section className="section-block">
          <div className="section-heading">
            <div>
              <p className="section-kicker">Értékelések</p>
              <h2>Vélemények és tapasztalatok</h2>
            </div>
          </div>

          {isWorker && !isOwnProfile && (
            <form className="review-form" onSubmit={submitWorkerReview}>
              <div className="field">
                <label htmlFor="worker-review-rating">Értékelés</label>
                <select
                  id="worker-review-rating"
                  value={reviewForm.rating}
                  onChange={(event) => setReviewForm((current) => ({ ...current, rating: event.target.value }))}
                >
                  <option value="5">5 - Kiváló</option>
                  <option value="4">4 - Jó</option>
                  <option value="3">3 - Rendben volt</option>
                  <option value="2">2 - Gyenge</option>
                  <option value="1">1 - Problémás</option>
                </select>
              </div>
              <div className="field">
                <label htmlFor="worker-review-text">Vélemény</label>
                <textarea
                  id="worker-review-text"
                  minLength={5}
                  maxLength={1000}
                  rows={4}
                  required
                  value={reviewForm.text}
                  onChange={(event) => setReviewForm((current) => ({ ...current, text: event.target.value }))}
                  placeholder="Írd le röviden, milyen volt együtt dolgozni ezzel a szakemberrel."
                />
              </div>
              <button className="button primary" type="submit" disabled={reviewSubmitState === 'submitting'}>
                {reviewSubmitState === 'submitting' ? 'Mentés...' : 'Értékelés mentése'}
              </button>
              {reviewSubmitMessage && (
                <p className={`form-message ${reviewSubmitState === 'success' ? 'success' : 'error'}`} role="status">
                  {reviewSubmitMessage}
                </p>
              )}
            </form>
          )}

          <div className="review-list">
            {selectedWorker.reviews.length > 0 ? selectedWorker.reviews.map((review) => (
              <article className="review-card" key={review.id}>
                <div>
                  <strong>{review.reviewerName}</strong>
                  <span>★ {review.rating}</span>
                </div>
                <p>{review.text}</p>
              </article>
            )) : (
              <p className="empty-note">Még nincs értékelés ezen a profilon.</p>
            )}
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="page-shell">
      <header className="site-header">
        <a className="brand" href="#top" aria-label="Melos Market főoldal">
          <span className="brand-mark">M</span>
          <span>Melos Market</span>
        </a>

        <nav className="nav-links" aria-label="Fő navigáció">
          <a href="#workers">Szakemberek keresése</a>
          <a href="#problem">Probléma feltöltése</a>
          <a href="#jobs">Nyitott munkák</a>
          {isAdmin && <a href="#admin">Admin</a>}
        </nav>

        <div className="header-actions">
          {currentUser ? (
            <>
              <span className="account-pill">{currentUser.email}</span>
              {isAdmin && (
                <a className="header-action" href="#admin">
                  Admin
                </a>
              )}
              <button className="header-action" type="button" onClick={logout}>
                Kilépés
              </button>
            </>
          ) : (
            <>
              <a className="header-action" href="#problem">
                Regisztráció ügyfélként
              </a>
              <a className="header-action" href="#worker-signup">
                Regisztráció szakemberként
              </a>
              <div className="login-popover-wrap">
                <button
                  className="header-action"
                  type="button"
                  aria-expanded={isLoginOpen}
                  onClick={() => {
                    setIsLoginOpen((open) => !open)
                    setLoginMessage('')
                    setLoginState('idle')
                  }}
                >
                  Belépés
                </button>

                {isLoginOpen && (
                  <form className="login-popover" onSubmit={submitLogin}>
                    <div className="admin-row-copy">
                      <h3>Belépés</h3>
                      <p>Ügyfélként a problémáidat, szakemberként a profilodat kezeled.</p>
                    </div>
                    <div className="field">
                      <label htmlFor="login-email">Email</label>
                      <input
                        id="login-email"
                        type="email"
                        maxLength={255}
                        placeholder="peter@example.hu"
                        required
                        value={loginForm.email}
                        onChange={(event) => setLoginForm((current) => ({ ...current, email: event.target.value }))}
                      />
                    </div>
                    <div className="field">
                      <label htmlFor="login-password">Jelszó</label>
                      <input
                        id="login-password"
                        type="password"
                        minLength={8}
                        maxLength={200}
                        required
                        value={loginForm.password}
                        onChange={(event) => setLoginForm((current) => ({ ...current, password: event.target.value }))}
                      />
                    </div>
                    <button type="submit" className="button primary full-width" disabled={loginState === 'submitting'}>
                      {loginState === 'submitting' ? 'Belépés...' : 'Belépés'}
                    </button>
                    {loginMessage && (
                      <p className={`form-message ${loginState === 'success' ? 'success' : 'error'}`} role="status">
                        {loginMessage}
                      </p>
                    )}
                  </form>
                )}
              </div>
            </>
          )}
        </div>
      </header>

      {verificationModalOpen && (
        <div className="modal-backdrop" role="presentation">
          <section className="verification-modal" role="dialog" aria-modal="true" aria-labelledby="email-verification-title">
            <p className="section-kicker">Email megerősítés</p>
            <h2 id="email-verification-title">Erősítsd meg az email címedet</h2>
            <p>
              A regisztráció befejezéséhez kattints a Melos Markettől kapott emailben található
              megerősítő gombra.
            </p>
            {currentUser && (
              <p className="verification-email">
                Címzett: <strong>{currentUser.email}</strong>
              </p>
            )}
            {verificationMessage && (
              <p className={`form-message ${verificationState === 'error' ? 'error' : 'success'}`} role="status">
                {verificationMessage}
              </p>
            )}
            <div className="verification-actions">
              {currentUser && !currentUser.emailVerified && (
                <button
                  className="button primary"
                  type="button"
                  disabled={verificationState === 'submitting'}
                  onClick={resendVerificationEmail}
                >
                  {verificationState === 'submitting' ? 'Küldés...' : 'Email újraküldése'}
                </button>
              )}
              <button className="button secondary" type="button" onClick={() => setVerificationModalOpen(false)}>
                Bezárás
              </button>
            </div>
          </section>
        </div>
      )}

      <section className="hero-section" id="top">
        <div className="hero-copy">
          <p className="eyebrow">Helyi szakemberek, felesleges találgatás nélkül</p>
          <h1>Találj megbízható szakembert javításhoz, felújításhoz és sürgős munkákhoz.</h1>
          <p className="hero-text">
            <strong>
              Az ügyfelek kereshetnek szakembereket vagy feltölthetik a problémájukat.
              A szakemberek pedig megtalálhatják a közelben lévő, hozzájuk illő munkákat.
            </strong>
          </p>

          <div className="hero-actions" aria-label="Elsődleges műveletek">
            <a className="button primary" href="#workers">
              Szakember keresése
            </a>
            <a className="button secondary" href="#problem">
              Probléma feltöltése
            </a>
          </div>
        </div>

        <form className="search-panel" aria-label="Szakemberek keresése" onSubmit={submitWorkerSearch}>
          <div className="field">
            <label htmlFor="location">Melyik városban keresel?</label>
            <input
              id="location"
              name="location"
              placeholder="Budapest, Debrecen, Szeged"
              value={workerSearch.serviceArea}
              onChange={(event) =>
                setWorkerSearch((current) => ({
                  ...current,
                  serviceArea: event.target.value,
                }))
              }
            />
          </div>
          <div className="field">
            <label htmlFor="trade">Milyen szakemberre van szükség?</label>
            <select
              id="trade"
              name="trade"
              value={workerSearch.trade}
              onChange={(event) =>
                setWorkerSearch((current) => ({
                  ...current,
                  trade: event.target.value,
                }))
              }
            >
              <option value="">Minden szakma</option>
              {trades.map((trade) => (
                <option key={trade}>{trade}</option>
              ))}
            </select>
          </div>
          <button type="submit" className="button primary full-width" disabled={workerSearchStatus === 'loading'}>
            {workerSearchStatus === 'loading' ? 'Keresés...' : 'Elérhető szakemberek keresése'}
          </button>
          <button type="button" className="button ghost full-width" onClick={clearWorkerSearch}>
            Szűrők törlése
          </button>
          {workerSearchMessage && (
            <p
              className={`form-message ${workerSearchStatus === 'error' ? 'error' : 'success'}`}
              role="status"
            >
              {workerSearchMessage}
            </p>
          )}
        </form>
      </section>

      <section className="trust-strip" aria-label="Piactér kiemelések">
        <div>
          <strong>240+</strong>
          <span>ellenőrzött szakember</span>
        </div>
        <div>
          <strong>18 perc</strong>
          <span>átlagos első válasz</span>
        </div>
        <div>
          <strong>4.8/5</strong>
          <span>átlagos ügyfélértékelés</span>
        </div>
      </section>

      <section className="content-grid">
        <div className="problem-panel" id="problem">
          <p className="section-kicker">Ügyfeleknek</p>
          {isCustomer ? (
            <>
              <h2>Ügyfél fiókod</h2>
              <p className="panel-text">Itt látod a saját problémáidat, és innen tudsz új problémát feltölteni.</p>

              <div className="customer-dashboard">
                <div className="dashboard-panel">
                  <div className="dashboard-heading">
                    <h3>Aktuális problémáid</h3>
                    <button type="button" className="text-button" onClick={() => loadMyProblems().catch(() => undefined)}>
                      Frissítés
                    </button>
                  </div>
                  {customerProblemsState === 'loading' ? (
                    <p className="empty-note">Betöltés...</p>
                  ) : currentCustomerProblems.length > 0 ? (
                    <div className="mini-list">
                      {currentCustomerProblems.map((problem) => (
                        <button
                          className="mini-row"
                          key={problem.id ?? problem.title}
                          type="button"
                          onClick={() => openProblemProfile(problem)}
                        >
                          <span>{problem.title}</span>
                          <small>{problem.location}</small>
                        </button>
                      ))}
                    </div>
                  ) : (
                    <p className="empty-note">Még nincs feltöltött problémád.</p>
                  )}
                </div>

                <div className="dashboard-panel">
                  <h3>Legutóbbi problémák</h3>
                  {recentCustomerProblems.length > 0 ? (
                    <div className="mini-list">
                      {recentCustomerProblems.map((problem) => (
                        <button
                          className="mini-row"
                          key={problem.id ?? problem.title}
                          type="button"
                          onClick={() => openProblemProfile(problem)}
                        >
                          <span>{problem.trade}</span>
                          <small>{problem.posted}</small>
                        </button>
                      ))}
                    </div>
                  ) : (
                    <p className="empty-note">A friss problémáid itt jelennek meg.</p>
                  )}
                </div>
              </div>

              <h2 className="form-section-title">Új probléma feltöltése</h2>
              <form className="problem-form" onSubmit={submitProblem}>
                <div className="field">
                  <label htmlFor="problem-title">Probléma címe</label>
                  <input
                    id="problem-title"
                    minLength={3}
                    maxLength={180}
                    placeholder="Szivárog a cső a konyhai mosogató alatt"
                    required
                    value={problemForm.title}
                    onChange={(event) => updateProblemForm('title', event.target.value)}
                  />
                </div>
                <div className="field">
                  <label htmlFor="problem-description">Leírás</label>
                  <textarea
                    id="problem-description"
                    minLength={10}
                    maxLength={4000}
                    rows={5}
                    placeholder="Írd le, mi történt, mikor kezdődött, és milyen segítségre van szükséged."
                    required
                    value={problemForm.description}
                    onChange={(event) => updateProblemForm('description', event.target.value)}
                  />
                </div>
                <div className="form-row">
                  <div className="field">
                    <label htmlFor="problem-trade">Szakma</label>
                    <select
                      id="problem-trade"
                      required
                      value={problemForm.trade}
                      onChange={(event) => updateProblemForm('trade', event.target.value)}
                    >
                      <option value="" disabled>
                        Válassz szakmát
                      </option>
                      {trades.map((trade) => (
                        <option key={trade}>{trade}</option>
                      ))}
                    </select>
                  </div>
                  <div className="field">
                    <label htmlFor="problem-location">Helyszín</label>
                    <input
                      id="problem-location"
                      maxLength={180}
                      placeholder="Budapest"
                      value={problemForm.location}
                      onChange={(event) => updateProblemForm('location', event.target.value)}
                    />
                  </div>
                </div>
                <div className="form-row">
                  <div className="field">
                    <label htmlFor="problem-phone">Telefonszám opcionális</label>
                    <input
                      id="problem-phone"
                      maxLength={50}
                      placeholder="+36 30 123 4567"
                      value={problemForm.phone}
                      onChange={(event) => updateProblemForm('phone', event.target.value)}
                    />
                  </div>
                  <div className="field">
                    <label htmlFor="problem-photos">Fotó opcionális</label>
                    <input
                      id="problem-photos"
                      type="file"
                      accept="image/png,image/jpeg"
                      multiple
                      onChange={updateProblemPhotos}
                    />
                  </div>
                </div>
                <button type="submit" className="button primary" disabled={submitState === 'submitting'}>
                  {submitState === 'submitting' ? 'Feltöltés...' : 'Probléma feltöltése'}
                </button>
                {submitMessage && (
                  <p className={`form-message ${submitState === 'success' ? 'success' : 'error'}`} role="status">
                    {submitMessage}
                  </p>
                )}
              </form>
            </>
          ) : isWorker ? (
            <>
              <h2>Problémát ügyfél fiókkal lehet feltölteni.</h2>
              <p className="panel-text">
                Szakemberként a nyitott munkákat böngészheted, és a saját profilodat szerkesztheted.
              </p>
              <a className="button primary" href="#jobs">
                Nyitott munkák megtekintése
              </a>
            </>
          ) : isAdmin ? (
            <>
              <h2>Admin fiókkal moderálni tudod a tartalmakat.</h2>
              <p className="panel-text">
                Probléma feltöltéséhez ügyfél fiók kell. Adminisztrációhoz menj az admin felületre.
              </p>
              <a className="button primary" href="#admin">
                Admin felület megnyitása
              </a>
            </>
          ) : (
            <>
              <h2>Regisztrálj ügyfélként, és töltsd fel a problémádat.</h2>
              <p className="panel-text">
                A probléma feltöltése csak bejelentkezett ügyfeleknek elérhető, így később vissza tudod nézni a saját hirdetéseidet.
              </p>
              <form className="problem-form" onSubmit={submitCustomerRegistration}>
                <div className="form-row">
                  <div className="field">
                    <label htmlFor="customer-email">Email</label>
                    <input
                      id="customer-email"
                      type="email"
                      maxLength={255}
                      placeholder="ugyfel@example.hu"
                      required
                      value={customerForm.email}
                      onChange={(event) => updateCustomerForm('email', event.target.value)}
                    />
                  </div>
                  <div className="field">
                    <label htmlFor="customer-password">Jelszó</label>
                    <input
                      id="customer-password"
                      type="password"
                      minLength={8}
                      maxLength={200}
                      required
                      value={customerForm.password}
                      onChange={(event) => updateCustomerForm('password', event.target.value)}
                    />
                  </div>
                </div>
                <button type="submit" className="button primary" disabled={customerSubmitState === 'submitting'}>
                  {customerSubmitState === 'submitting' ? 'Regisztráció...' : 'Regisztráció ügyfélként'}
                </button>
                {customerSubmitMessage && (
                  <p
                    className={`form-message ${customerSubmitState === 'success' ? 'success' : 'error'}`}
                    role="status"
                  >
                    {customerSubmitMessage}
                  </p>
                )}
              </form>
            </>
          )}
        </div>

        <div className="steps-panel">
          <p className="section-kicker">Hogyan működik?</p>
          <h2>Egyszerű folyamat az ügyfeleknek és a szakembereknek is.</h2>
          <ol className="steps-list">
            <li>
              <span>1</span>
              <div>
                <strong>Az ügyfél feltölti a problémát</strong>
                <p>Megad egy rövid leírást, szakmát és helyszínt.</p>
              </div>
            </li>
            <li>
              <span>2</span>
              <div>
                <strong>A szakemberek megtalálják a megfelelő munkákat</strong>
                <p>A szakemberek böngészhetik a közelükben lévő nyitott feladatokat.</p>
              </div>
            </li>
            <li>
              <span>3</span>
              <div>
                <strong>A két oldal kapcsolatba lép</strong>
                <p>Az ügyfél kiválasztja, kivel szeretne egyeztetni, és indulhat a munka.</p>
              </div>
            </li>
          </ol>
        </div>
      </section>

      <section className="section-block" id="workers">
        <div className="section-heading">
          <div>
            <p className="section-kicker">Kiemelt szakemberek</p>
            <h2>Böngéssz helyi, megbízható szakemberek között.</h2>
          </div>
          <a href="#worker-signup">Vállalkozás regisztrálása</a>
        </div>

        <div className="worker-grid">
          {workerCards.length > 0 ? workerCards.map((worker) => (
            <button className="worker-card worker-card-button" key={worker.id ?? worker.name} onClick={() => openWorkerProfile(worker)}>
              <div className="worker-card-top">
                <span className="trade-pill">{worker.trade}</span>
                <span className={worker.verified ? 'verified-badge compact' : 'rating'}>
                  {worker.verified ? 'Ellenőrzött' : worker.rating === 'Új' ? 'Új' : `★ ${worker.rating}`}
                </span>
              </div>
              <h3>{worker.name}</h3>
              {activeWorkerBadges(worker).length > 0 && (
                <div className="worker-card-badges">
                  {activeWorkerBadges(worker).slice(0, 3).map((badge) => (
                    <span className="worker-badge compact" key={badge.key}>{badge.label}</span>
                  ))}
                </div>
              )}
              <p>{worker.description}</p>
              <div className="worker-meta">
                <span>{worker.area}</span>
                <span>{worker.jobs}</span>
              </div>
            </button>
          )) : (
            <div className="empty-results">
              <h3>Nincs találat</h3>
              <p>Próbálj másik várost vagy szakmát választani, vagy töröld a szűrőket.</p>
            </div>
          )}
        </div>
      </section>

      <section className="section-block jobs-section" id="jobs">
        <div className="section-heading">
          <div>
            <p className="section-kicker">Szakembereknek</p>
            <h2>Nyitott problémák, amelyek a megfelelő szakemberre várnak.</h2>
          </div>
          <a href="#top">Keresés szakma szerint</a>
        </div>

        <div className="job-list">
          {problemPosts.map((problem) => (
            <button className="job-row job-row-button" key={problem.id ?? problem.title} type="button" onClick={() => openProblemProfile(problem)}>
              <div>
                <span className="trade-pill">{problem.trade}</span>
                <h3>{problem.title}</h3>
              </div>
              <div className="job-meta">
                <span>{problem.location}</span>
                <span>{problem.posted}</span>
              </div>
            </button>
          ))}
        </div>
      </section>

      {isAdmin && (
        <section className="section-block admin-section" id="admin">
          <div className="section-heading">
            <div>
              <p className="section-kicker">Admin</p>
              <h2>Tartalmak kezelése probléma esetén.</h2>
            </div>
            <button className="button ghost" type="button" onClick={() => loadAdminData()}>
              {adminState === 'loading' ? 'Frissítés...' : 'Admin lista frissítése'}
            </button>
          </div>

          {adminMessage && (
            <p className={`form-message ${adminState === 'error' ? 'error' : 'success'}`} role="status">
              {adminMessage}
            </p>
          )}

          <div className="admin-grid">
            <div className="admin-panel">
              <h3>Szakember profilok</h3>
              <div className="admin-list">
                {workerCards.length > 0 ? workerCards.map((worker) => (
                  <div className="admin-row" key={worker.id ?? worker.name}>
                    <div className="admin-row-copy">
                      <strong>{worker.name}</strong>
                      <span>{worker.trade} · {worker.area}</span>
                    </div>
                    <div className="admin-worker-controls">
                      <div className="admin-badge-actions">
                        {workerBadgeDefinitions.map((badge) => (
                          <button
                            className={worker[badge.key] ? 'badge-toggle active' : 'badge-toggle'}
                            type="button"
                            key={badge.key}
                            disabled={!worker.id || adminState === 'loading'}
                            onClick={() => updateAdminWorkerBadge(worker, badge.key, !worker[badge.key])}
                          >
                            {badge.label}
                          </button>
                        ))}
                      </div>
                      <button
                        className="button danger"
                        type="button"
                        disabled={!worker.id || adminState === 'loading'}
                        onClick={() => deleteAdminWorker(worker)}
                      >
                        Törlés
                      </button>
                    </div>
                  </div>
                )) : (
                  <p className="empty-note">Nincs törölhető szakember profil.</p>
                )}
              </div>
            </div>

            <div className="admin-panel">
              <h3>Problémák</h3>
              <div className="admin-list">
                {problemPosts.length > 0 ? problemPosts.map((problem) => (
                  <div className="admin-row" key={problem.id ?? problem.title}>
                    <div>
                      <strong>{problem.title}</strong>
                      <span>{problem.trade} · {problem.location}</span>
                    </div>
                    <button
                      className="button danger"
                      type="button"
                      disabled={!problem.id || adminState === 'loading'}
                      onClick={() => deleteAdminProblem(problem)}
                    >
                      Törlés
                    </button>
                  </div>
                )) : (
                  <p className="empty-note">Nincs törölhető probléma.</p>
                )}
              </div>
            </div>
          </div>
        </section>
      )}

      <section className="worker-signup" id="worker-signup">
        <div>
          <p className="section-kicker">Szerezz több munkát</p>
          <h2>A szakemberek már most regisztrálhatnak, hogy készen álljanak az induláskor.</h2>
          <p className="muted-text">
            A regisztráció mostantól fiókot is létrehoz, így később csak te tudod szerkeszteni a saját profilodat.
          </p>
        </div>

        <form className="worker-form" onSubmit={submitWorker}>
          <div className="form-row">
            <div className="field">
              <label htmlFor="business-name">Vállalkozás neve</label>
              <input
                id="business-name"
                minLength={2}
                maxLength={180}
                placeholder="Példa Generálkivitelezés Kft."
                required
                value={workerForm.businessName}
                onChange={(event) => updateWorkerForm('businessName', event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="contact-name">Kapcsolattartó</label>
              <input
                id="contact-name"
                minLength={2}
                maxLength={160}
                placeholder="Nagy Péter"
                required
                value={workerForm.contactName}
                onChange={(event) => updateWorkerForm('contactName', event.target.value)}
              />
            </div>
          </div>

          <div className="form-row">
            <div className="field">
              <label htmlFor="worker-email">Email</label>
              <input
                id="worker-email"
                type="email"
                maxLength={255}
                placeholder="peter@example.hu"
                required
                value={workerForm.email}
                onChange={(event) => updateWorkerForm('email', event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="worker-password">Jelszó</label>
              <input
                id="worker-password"
                type="password"
                minLength={8}
                maxLength={200}
                required
                value={workerForm.password}
                onChange={(event) => updateWorkerForm('password', event.target.value)}
              />
            </div>
          </div>

          <div className="form-row">
            <div className="field">
              <label htmlFor="worker-phone">Telefon</label>
              <input
                id="worker-phone"
                maxLength={50}
                placeholder="+36 30 123 4567"
                value={workerForm.phone}
                onChange={(event) => updateWorkerForm('phone', event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="worker-tax-number">Adószám</label>
              <input
                id="worker-tax-number"
                maxLength={50}
                placeholder="12345678-1-42"
                value={workerForm.taxNumber}
                onChange={(event) => updateWorkerForm('taxNumber', event.target.value)}
              />
            </div>
          </div>

          <div className="form-row">
            <div className="field">
              <label htmlFor="worker-trade">Szakma</label>
              <select
                id="worker-trade"
                required
                value={workerForm.trade}
                onChange={(event) => updateWorkerForm('trade', event.target.value)}
              >
                <option value="" disabled>
                  Válassz szakmát
                </option>
                {trades.map((trade) => (
                  <option key={trade}>{trade}</option>
                ))}
              </select>
            </div>
            <div className="field">
              <label htmlFor="service-area">Szolgáltatási terület</label>
              <input
                id="service-area"
                maxLength={160}
                placeholder="Budapest és környéke"
                value={workerForm.serviceArea}
                onChange={(event) => updateWorkerForm('serviceArea', event.target.value)}
              />
            </div>
          </div>

          <div className="field">
            <label htmlFor="worker-description">Rövid bemutatkozás</label>
            <textarea
              id="worker-description"
              maxLength={2000}
              rows={4}
              placeholder="Írd le, milyen munkákat vállalsz és miben vagy erős."
              value={workerForm.description}
              onChange={(event) => updateWorkerForm('description', event.target.value)}
            />
          </div>

          <button type="submit" className="button primary" disabled={workerSubmitState === 'submitting'}>
            {workerSubmitState === 'submitting' ? 'Regisztráció...' : 'Csatlakozás szakemberként'}
          </button>
          {workerSubmitMessage && (
            <p className={`form-message ${workerSubmitState === 'success' ? 'success' : 'error'}`} role="status">
              {workerSubmitMessage}
            </p>
          )}
        </form>
      </section>

      <footer className="site-footer">
        <a href="/aszf.html">Általános Szerződési Feltételek (ÁSZF)</a>
        <a href="/adatkezeles.html">Adatkezelési tájékoztató</a>
        <a href="/ertekelesi-szabalyzat.html">Értékelési és véleményezési szabályzat</a>
        <a href="/felhasznaloi-hozzajarulas.html">Felhasználói hozzájáruló nyilatkozat</a>
        <a href="/hirdetesi-szabalyzat.html">Hirdetési szabályzat</a>
        <a href="/moderacios-szabalyzat.html">Moderációs szabályzat</a>
        <a href="/panaszkezelesi-szabalyzat.html">Panaszkezelési szabályzat</a>
      </footer>
    </main>
  )
}

export default App
