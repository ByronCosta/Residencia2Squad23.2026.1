// ══════════════════════════════════════════════════════
// CONFIG
// ══════════════════════════════════════════════════════
const API_BASE = 'http://localhost:8080/api'; // ← altere aqui para sua API Java

// ══════════════════════════════════════════════════════
// STATE
// ══════════════════════════════════════════════════════
let authToken   = sessionStorage.getItem('roomflow_token') || null;
let authUser    = sessionStorage.getItem('roomflow_user')  || null;
let rooms       = [];
let currentEditId = null;
let gridView    = 'grid';
let uploadedPlantas = [];
let uploadedFotos   = [];
let currentDetailId = null;
let pendingDeleteId = null;

// ══════════════════════════════════════════════════════
// INIT
// ══════════════════════════════════════════════════════
window.addEventListener('DOMContentLoaded', () => {
  if (authToken) {
    showApp();
  } else {
    document.getElementById('login-page').style.display = 'flex';
    document.getElementById('app').style.display = 'none';
  }
});

// ESC para fechar qualquer modal aberto
document.addEventListener('keydown', e => {
  if (e.key !== 'Escape') return;
  if (document.getElementById('detail-overlay').classList.contains('open'))  { closeDetail();  return; }
  if (document.getElementById('modal-overlay').classList.contains('open'))   { closeModal();   return; }
  if (document.getElementById('confirm-overlay').classList.contains('open')) { closeConfirm(); return; }
});

// Enter no login
document.addEventListener('keydown', e => {
  if (e.key === 'Enter' && document.getElementById('login-page').style.display !== 'none') {
    doLogin();
  }
});

// ══════════════════════════════════════════════════════
// AUTH
// ══════════════════════════════════════════════════════
async function doLogin() {
  const userEl = document.getElementById('l-user');
  const passEl = document.getElementById('l-pass');
  const btn    = document.getElementById('login-btn');

  if (!userEl || !passEl) return;

  const username = userEl.value.trim();
  const password = passEl.value;

  if (!username || !password) {
    showLoginError('Preencha usuário e senha.');
    return;
  }

  btn.disabled = true;
  const originalHtml = btn.innerHTML;
  btn.innerHTML = '<i class="ti ti-loader" style="animation:spin 1s linear infinite"></i> Entrando…';

  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    if (!res.ok) {
      const msg = res.status === 401 ? 'Usuário ou senha inválidos.' : 'Erro ao conectar com o servidor.';
      showLoginError(msg);
      btn.disabled = false;
      btn.innerHTML = originalHtml;
      return;
    }

    const data = await res.json();
    authToken = data.token;
    authUser  = data.nomeUsuario || username;

    sessionStorage.setItem('roomflow_token', authToken);
    sessionStorage.setItem('roomflow_user',  authUser);

    showApp();

  } catch (err) {
    console.warn("API Offline, entrando em modo demo");
    authToken = 'demo-token';
    authUser  = username || 'Demo';
    sessionStorage.setItem('roomflow_token', authToken);
    sessionStorage.setItem('roomflow_user',  authUser);
    showApp();
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<i class="ti ti-login"></i> Entrar';
  }
}

function showLoginError(msg) {
  const el = document.getElementById('login-error');
  const msgEl = document.getElementById('login-error-msg');
  if (msgEl) msgEl.textContent = msg;
  if (el) el.classList.add('show');
}

function showApp() {
  const loginPg = document.getElementById('login-page');
  const appPg   = document.getElementById('app');
  
  if (loginPg) loginPg.style.display = 'none';
  if (appPg) appPg.style.display = 'flex';

  const initials = (authUser || 'U').split(' ').map(w=>w[0]).join('').substring(0,2).toUpperCase();
  const avatarEl = document.getElementById('sidebar-avatar');
  const userEl   = document.getElementById('sidebar-username');
  
  if (avatarEl) avatarEl.textContent = initials;
  if (userEl) userEl.textContent = authUser || 'Usuário';

  checkApiStatus();
  loadRooms();
}

function doLogout() {
  authToken = null; authUser = null;
  sessionStorage.removeItem('roomflow_token');
  sessionStorage.removeItem('roomflow_user');
  rooms = [];
  document.getElementById('app').style.display = 'none';
  document.getElementById('login-page').style.display = 'flex';
  document.getElementById('login-error').classList.remove('show');
}

function togglePassword() {
  const input = document.getElementById('l-pass');
  const icon  = document.getElementById('pass-eye');
  if (!input || !icon) return;
  if (input.type === 'password') {
    input.type = 'text';
    icon.className = 'ti ti-eye-off';
  } else {
    input.type = 'password';
    icon.className = 'ti ti-eye';
  }
}

// ══════════════════════════════════════════════════════
// API UTILITIES
// ══════════════════════════════════════════════════════

async function apiFetch(path, options = {}) {
  const headers = { ...options.headers };
  if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  if (res.status === 401) {
    showToast('Sessão expirada. Faça login novamente.', 'error');
    doLogout();
    throw new Error('Unauthorized');
  }

  return res;
}

async function checkApiStatus() {
  const badge    = document.getElementById('api-status-badge');
  const textEl   = document.getElementById('api-status-text');
  if (!badge || !textEl) return;

  badge.className = 'api-status checking';
  textEl.textContent = 'Verificando…';

  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000);
    
    const res = await fetch(`${API_BASE}/salas`, {
      signal: controller.signal,
      headers: authToken ? { 'Authorization': `Bearer ${authToken}` } : {}
    });
    clearTimeout(timeoutId);
    
    if (res.ok || res.status === 401) {
      badge.className   = 'api-status online';
      textEl.textContent = 'API Online';
    } else { throw new Error(); }
  } catch {
    badge.className   = 'api-status offline';
    textEl.textContent = 'API Offline (modo demo)';
  }
}

// ══════════════════════════════════════════════════════
// LOAD & DEMO DATA
// ══════════════════════════════════════════════════════
async function loadRooms() {
  showSkeleton();
  try {
    const res = await apiFetch('/salas');
    if (!res.ok) throw new Error();
    rooms = await res.json();
  } catch {
    if (rooms.length === 0) rooms = getDemoData();
  }
  updateStats();
  updateFilters();
  updateBadge();
  renderRooms();
}

function getDemoData() {
  return [
    { id:1, nome:'Sala Executive',      codigo:'SAL-101', andar:'1º Andar', capacidade:10, area:35,  status:'disponivel', tipo:'reuniao',     responsavel:'João Silva',  descricao:'Sala de reuniões executivas.',           comodidades:['Projetor','Videoconferência','Ar condicionado','Wi-Fi'], temPlanta:true,  fotos:[] },
    { id:2, nome:'Auditório Central',   codigo:'AUD-001', andar:'2º Andar', capacidade:120,area:280, status:'ocupada',    tipo:'auditorio',   responsavel:'Maria Santos', descricao:'Auditório com palco e sistema de som.', comodidades:['Projetor','TV/Monitor','Ar condicionado','Wi-Fi'],        temPlanta:true,  fotos:[] },
    { id:3, nome:'Lab de Inovação',     codigo:'LAB-201', andar:'2º Andar', capacidade:25, area:80,  status:'disponivel', tipo:'laboratorio',  responsavel:'Carlos Tech',  descricao:'Espaço colaborativo.',                   comodidades:['Wi-Fi','Quadro branco','Ar condicionado'],               temPlanta:false, fotos:[] },
    { id:4, nome:'Sala de Treinamento', codigo:'TRE-301', andar:'3º Andar', capacidade:40, area:90,  status:'manutencao', tipo:'treinamento',  responsavel:'Ana Costa',    descricao:'Sala para treinamentos e workshops.',    comodidades:['Projetor','Quadro branco','Ar condicionado'],            temPlanta:true,  fotos:[] },
    { id:5, nome:'Escritório Flex',     codigo:'ESC-401', andar:'4º Andar', capacidade:15, area:50,  status:'disponivel', tipo:'escritorio',   responsavel:'Pedro Lima',   descricao:'Espaço de trabalho flexível.',           comodidades:['Wi-Fi','Ar condicionado','Cafeteira'],                  temPlanta:false, fotos:[] },
  ];
}

// ══════════════════════════════════════════════════════
// STATS & FILTERS
// ══════════════════════════════════════════════════════
function updateStats() {
  const total = rooms.length;
  const disp  = rooms.filter(r => r.status === 'disponivel').length;
  const ocup  = rooms.filter(r => r.status === 'ocupada').length;
  const manut = rooms.filter(r => r.status === 'manutencao').length;

  ['stat-total','d-total'].forEach(id => setEl(id, total));
  ['stat-disp', 'd-disp' ].forEach(id => setEl(id, disp));
  ['stat-ocup'            ].forEach(id => setEl(id, ocup));
  ['stat-manut','d-manut' ].forEach(id => setEl(id, manut));

  const cap = rooms.reduce((s, r) => s + (r.capacidade||0), 0);
  setEl('d-cap', cap);
}

function setEl(id, val) { const el = document.getElementById(id); if (el) el.textContent = val; }

function updateBadge() { setEl('count-badge', rooms.length); }

function updateFilters() {
  const andares = [...new Set(rooms.map(r => r.andar))].sort();
  const sel = document.getElementById('filter-andar');
  if (!sel) return;
  const cur = sel.value;
  sel.innerHTML = '<option value="">Todos os andares</option>';
  andares.forEach(a => {
    const o = document.createElement('option');
    o.value = a; o.textContent = a;
    if (a === cur) o.selected = true;
    sel.appendChild(o);
  });
}

// ══════════════════════════════════════════════════════
// RENDER — SKELETON
// ══════════════════════════════════════════════════════
function showSkeleton() {
  const c = document.getElementById('rooms-container');
  if (!c) return;
  c.className = 'rooms-grid';
  c.innerHTML = Array(6).fill(`
    <div class="skeleton-card">
      <div class="skeleton skeleton-thumb"></div>
      <div class="skeleton-body">
        <div class="skeleton skeleton-line w80"></div>
        <div class="skeleton skeleton-line w50"></div>
        <div class="skeleton skeleton-line w60"></div>
      </div>
    </div>`).join('');
}

// ══════════════════════════════════════════════════════
// RENDER — ROOMS
// ══════════════════════════════════════════════════════
function renderRooms() {
  const searchInput = document.getElementById('search-input');
  const statusFilter = document.getElementById('filter-status');
  const andarFilter = document.getElementById('filter-andar');
  const tipoFilter = document.getElementById('filter-tipo');
  const c = document.getElementById('rooms-container');
  
  if (!c) return;

  const search  = searchInput ? searchInput.value.toLowerCase() : '';
  const fStatus = statusFilter ? statusFilter.value : '';
  const fAndar  = andarFilter ? andarFilter.value : '';
  const fTipo   = tipoFilter ? tipoFilter.value : '';

  const filtered = rooms.filter(r => {
    const matchSearch = !search || r.nome.toLowerCase().includes(search) || r.codigo.toLowerCase().includes(search) || (r.andar||'').toLowerCase().includes(search);
    return matchSearch
      && (!fStatus || r.status === fStatus)
      && (!fAndar  || r.andar  === fAndar)
      && (!fTipo   || r.tipo   === fTipo);
  });

  if (filtered.length === 0) {
    c.innerHTML = `
      <div class="empty-state">
        <i class="ti ti-building-off"></i>
        <h3>Nenhuma sala encontrada</h3>
        <p>Tente ajustar os filtros ou cadastre uma nova sala.</p>
        <button class="btn btn-primary" onclick="openModal()"><i class="ti ti-plus"></i> Nova Sala</button>
      </div>`;
    return;
  }

  if (gridView === 'grid') {
    c.className = 'rooms-grid';
    c.innerHTML = filtered.map((r, i) => renderCard(r, i)).join('');
  } else {
    c.className = 'rooms-list';
    c.innerHTML = filtered.map((r, i) => renderRow(r, i)).join('');
  }
}

function statusLabel(s) {
  if (s === 'disponivel') return 'Disponível';
  if (s === 'ocupada')    return 'Ocupada';
  return 'Manutenção';
}

function tipoLabel(t) {
  return { reuniao:'Reunião', auditorio:'Auditório', treinamento:'Treinamento', escritorio:'Escritório', laboratorio:'Laboratório', outro:'Outro' }[t] || t;
}

function renderCard(r, i) {
  const thumb = r.fotos && r.fotos.length
    ? `<img src="${r.fotos[0]}" alt="${r.nome}">`
    : `<div class="no-img"><i class="ti ti-building"></i><span>Sem foto</span></div>`;
  return `
    <div class="room-card" style="animation-delay:${i*40}ms" onclick="openDetail(${r.id})">
      <div class="room-thumb">
        ${thumb}
        ${r.temPlanta ? '<div class="has-pdf-badge"><i class="ti ti-file-type-pdf"></i> Planta</div>' : ''}
        <div class="room-status-badge status-${r.status}">${statusLabel(r.status)}</div>
      </div>
      <div class="room-body">
        <div class="room-name">${r.nome}</div>
        <div class="room-location"><i class="ti ti-map-pin" style="font-size:12px"></i>${r.andar} • ${r.codigo}</div>
        <div class="room-meta">
          <div class="meta-chip"><i class="ti ti-users"></i>${r.capacidade} pessoas</div>
          ${r.area ? `<div class="meta-chip"><i class="ti ti-ruler"></i>${r.area}m²</div>` : ''}
          <div class="meta-chip"><i class="ti ti-door"></i>${tipoLabel(r.tipo)}</div>
        </div>
        <div class="room-actions" onclick="event.stopPropagation()">
          <button class="btn btn-ghost" style="flex:1;justify-content:center;font-size:12px;padding:6px" onclick="openEdit(${r.id})">
            <i class="ti ti-edit"></i> Editar
          </button>
          <button class="btn btn-danger" style="padding:6px 10px" onclick="confirmDelete(${r.id})">
            <i class="ti ti-trash"></i>
          </button>
        </div>
      </div>
    </div>`;
}

function renderRow(r, i) {
  const thumb = r.fotos && r.fotos.length
    ? `<img src="${r.fotos[0]}" alt="">`
    : `<i class="ti ti-building"></i>`;
  return `
    <div class="room-row" style="animation-delay:${i*30}ms" onclick="openDetail(${r.id})">
      <div class="room-row-thumb">${thumb}</div>
      <div class="room-row-info">
        <div class="room-row-name">${r.nome}</div>
        <div class="room-row-sub">${r.codigo} • ${r.andar} • ${tipoLabel(r.tipo)}</div>
      </div>
      <div class="room-row-caps">
        <div class="room-row-caps-num">${r.capacidade}</div>
        <div class="room-row-caps-label">pessoas</div>
      </div>
      <div class="room-row-status">
        <span class="room-status-badge status-${r.status}">${statusLabel(r.status)}</span>
      </div>
      <div class="room-row-actions" onclick="event.stopPropagation()">
        <button class="btn btn-ghost" style="font-size:12px;padding:6px 10px" onclick="openEdit(${r.id})"><i class="ti ti-edit"></i></button>
        <button class="btn btn-danger" style="padding:6px 10px" onclick="confirmDelete(${r.id})"><i class="ti ti-trash"></i></button>
      </div>
    </div>`;
}

// ══════════════════════════════════════════════════════
// DASHBOARD
// ══════════════════════════════════════════════════════
function renderDashboard() {
  updateStats();
  const grid = document.getElementById('dashboard-grid');
  if (!grid) return;

  const total = rooms.length || 1;
  const byStatus = {
    disponivel: rooms.filter(r=>r.status==='disponivel').length,
    ocupada:    rooms.filter(r=>r.status==='ocupada').length,
    manutencao: rooms.filter(r=>r.status==='manutencao').length,
  };

  const tiposCount = {};
  rooms.forEach(r => { tiposCount[r.tipo] = (tiposCount[r.tipo]||0)+1; });

  const cx=70, cy=70, r2=50, circ=2*Math.PI*r2;
  const pcts = [byStatus.disponivel/total, byStatus.ocupada/total, byStatus.manutencao/total];
  const colors = ['#34d399','#f87171','#fbbf24'];
  let offset = 0;
  const arcs = pcts.map((p,i) => {
    const dash = p*circ;
    const gap  = (1-p)*circ;
    const seg  = `<circle cx="${cx}" cy="${cy}" r="${r2}" fill="none" stroke="${colors[i]}" stroke-width="18" stroke-dasharray="${dash} ${gap}" stroke-dashoffset="${-offset*circ}" transform="rotate(-90 ${cx} ${cy})"/>`;
    offset += p;
    return seg;
  }).join('');

  const topByCap = [...rooms].sort((a,b)=>b.capacidade-a.capacidade).slice(0,4);

  const barsHtml = Object.entries(tiposCount)
    .sort((a,b)=>b[1]-a[1])
    .map(([tipo, cnt]) => `
      <div class="bar-row">
        <div class="bar-label">${tipoLabel(tipo)}</div>
        <div class="bar-track"><div class="bar-fill" style="width:${(cnt/total*100).toFixed(0)}%"></div></div>
        <div class="bar-count">${cnt}</div>
      </div>`).join('');

  grid.innerHTML = `
    <div class="dash-card">
      <div class="dash-card-title"><i class="ti ti-chart-donut"></i> Status das Salas</div>
      <div class="donut-wrap">
        <svg width="140" height="140" viewBox="0 0 140 140">
          <circle cx="70" cy="70" r="50" fill="none" stroke="var(--bg3)" stroke-width="18"/>
          ${arcs}
          <text x="70" y="66" text-anchor="middle" font-family="Space Grotesk" font-size="22" font-weight="700" fill="var(--text)">${total}</text>
          <text x="70" y="82" text-anchor="middle" font-size="10" fill="var(--text3)">salas</text>
        </svg>
        <div class="donut-legend">
          <div class="legend-item"><div class="legend-dot" style="background:#34d399"></div><div class="legend-label">Disponíveis</div><div class="legend-val">${byStatus.disponivel}</div></div>
          <div class="legend-item"><div class="legend-dot" style="background:#f87171"></div><div class="legend-label">Ocupadas</div><div class="legend-val">${byStatus.ocupada}</div></div>
          <div class="legend-item"><div class="legend-dot" style="background:#fbbf24"></div><div class="legend-label">Manutenção</div><div class="legend-val">${byStatus.manutencao}</div></div>
        </div>
      </div>
    </div>
    <div class="dash-card">
      <div class="dash-card-title"><i class="ti ti-chart-bar"></i> Salas por Tipo</div>
      <div class="bar-chart">${barsHtml || '<p style="color:var(--text3);font-size:13px">Nenhuma sala cadastrada</p>'}</div>
    </div>
    <div class="dash-card">
      <div class="dash-card-title"><i class="ti ti-users"></i> Maior Capacidade</div>
      <div class="cap-list">
        ${topByCap.map((r,i) => `
          <div class="cap-item">
            <div class="cap-rank">#${i+1}</div>
            <div class="cap-info">
              <div class="cap-name">${r.nome}</div>
              <div class="cap-loc">${r.andar} · ${tipoLabel(r.tipo)}</div>
            </div>
            <div class="cap-val">${r.capacidade}</div>
          </div>`).join('') || '<p style="color:var(--text3);font-size:13px">Sem dados</p>'}
      </div>
    </div>
    <div class="dash-card">
      <div class="dash-card-title"><i class="ti ti-activity"></i> Taxa de Disponibilidade</div>
      <div style="text-align:center;padding:10px 0">
        <div style="font-family:var(--font-display);font-size:52px;font-weight:700;color:var(--accent);line-height:1">
          ${total > 0 ? Math.round(byStatus.disponivel/total*100) : 0}<span style="font-size:28px">%</span>
        </div>
        <div style="font-size:13px;color:var(--text3);margin-top:6px">${byStatus.disponivel} de ${total} salas disponíveis</div>
        <div style="margin-top:16px;background:var(--bg3);border-radius:99px;height:10px;overflow:hidden">
          <div style="height:100%;border-radius:99px;background:var(--accent);width:${total>0?Math.round(byStatus.disponivel/total*100):0}%;transition:width 1s ease"></div>
        </div>
      </div>
    </div>`;
}

// ══════════════════════════════════════════════════════
// VIEW SWITCHING
// ══════════════════════════════════════════════════════
function setView(v, btn) {
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  if (btn) btn.classList.add('active');

  const vSalas = document.getElementById('view-salas');
  const vDash  = document.getElementById('view-dashboard');
  const title  = document.getElementById('topbar-title');

  if (vSalas) vSalas.classList.toggle('active', v === 'salas');
  if (vDash) vDash.classList.toggle('active', v === 'dashboard');
  if (title) title.textContent = v === 'salas' ? 'Salas' : 'Dashboard';

  if (v === 'dashboard') renderDashboard();
  closeSidebar();
}

function setGridView(v) {
  gridView = v;
  const bGrid = document.getElementById('btn-grid');
  const bList = document.getElementById('btn-list');
  if (bGrid) bGrid.classList.toggle('active', v === 'grid');
  if (bList) bList.classList.toggle('active', v === 'list');
  renderRooms();
}

// ══════════════════════════════════════════════════════
// MOBILE SIDEBAR
// ══════════════════════════════════════════════════════
function toggleSidebar() {
  const sb = document.getElementById('sidebar');
  const ov = document.getElementById('sidebar-overlay');
  if (sb) sb.classList.toggle('open');
  if (ov) ov.classList.toggle('open');
}

function closeSidebar() {
  const sb = document.getElementById('sidebar');
  const ov = document.getElementById('sidebar-overlay');
  if (sb) sb.classList.remove('open');
  if (ov) ov.classList.remove('open');
}

// ══════════════════════════════════════════════════════
// MODAL — CRUD
// ══════════════════════════════════════════════════════
function openModal(editData = null) {
  currentEditId = editData ? editData.id : null;
  uploadedPlantas = []; uploadedFotos = [];
  
  const pList = document.getElementById('planta-list');
  const fPrev = document.getElementById('fotos-preview');
  if (pList) pList.innerHTML = '';
  if (fPrev) fPrev.innerHTML = '';
  
  document.querySelectorAll('.amenity-chip').forEach(c => c.classList.remove('selected'));

  const mTitle = document.getElementById('modal-title');
  const sBtnText = document.getElementById('save-btn-text');
  if (mTitle) mTitle.textContent = editData ? 'Editar Sala' : 'Nova Sala';
  if (sBtnText) sBtnText.textContent = editData ? 'Salvar Alterações' : 'Salvar Sala';

  ['nome','codigo','andar','capacidade','area','status','tipo','responsavel','descricao'].forEach(f => {
    const el = document.getElementById('f-' + f);
    if (el) el.value = editData ? (editData[f] ?? '') : '';
  });

  if (editData?.comodidades) {
    document.querySelectorAll('.amenity-chip').forEach(c => {
      if (editData.comodidades.includes(c.textContent.trim())) c.classList.add('selected');
    });
  }

  const mOv = document.getElementById('modal-overlay');
  if (mOv) mOv.classList.add('open');
}

function closeModal() { 
  const mOv = document.getElementById('modal-overlay');
  if (mOv) mOv.classList.remove('open'); 
}

function closeModalOnBg(e) { if (e.target === document.getElementById('modal-overlay')) closeModal(); }

function openEdit(id) {
  const r = rooms.find(room => room.id == id);
  if (r) openModal(r);
}

async function saveRoom() {
  const btn = document.getElementById('save-btn');
  const btnText = document.getElementById('save-btn-text');
  
  const sala = {
    nome:        document.getElementById('f-nome').value,
    codigo:      document.getElementById('f-codigo').value,
    andar:       document.getElementById('f-andar').value,
    capacidade:  parseInt(document.getElementById('f-capacidade').value) || 0,
    area:        parseFloat(document.getElementById('f-area').value) || 0,
    status:      document.getElementById('f-status').value,
    tipo:        document.getElementById('f-tipo').value,
    responsavel: document.getElementById('f-responsavel').value,
    descricao:   document.getElementById('f-descricao').value,
    comodidades: Array.from(document.querySelectorAll('.amenity-chip.selected')).map(c => c.textContent.trim()),
    temPlanta:   uploadedPlantas.length > 0 || (currentEditId && rooms.find(r=>r.id==currentEditId).temPlanta)
  };

  if (!sala.nome || !sala.codigo) {
    showToast('Nome e Código são obrigatórios.', 'error');
    return;
  }

  if (btn) btn.disabled = true;
  if (btnText) btnText.textContent = 'Salvando...';

  const formData = new FormData();
  formData.append('sala', new Blob([JSON.stringify(sala)], { type: 'application/json' }));
  uploadedPlantas.forEach(f => formData.append('plantas', f));
  uploadedFotos.forEach(f => formData.append('fotos', f));

  try {
    const method = currentEditId ? 'PUT' : 'POST';
    const path   = currentEditId ? `/salas/${currentEditId}` : '/salas';
    
    const res = await apiFetch(path, { method, body: formData });
    if (!res.ok) throw new Error();
    
    const saved = await res.json();
    if (currentEditId) {
      rooms = rooms.map(r => r.id == currentEditId ? saved : r);
    } else {
      rooms.push(saved);
    }
  } catch {
    if (currentEditId) {
      rooms = rooms.map(r => r.id == currentEditId ? { ...r, ...sala } : r);
    } else {
      sala.id = Date.now();
      sala.fotos = [];
      rooms.push(sala);
    }
  }

  updateStats(); updateBadge(); updateFilters(); renderRooms();
  const vDash = document.getElementById('view-dashboard');
  if (vDash && vDash.classList.contains('active')) renderDashboard();
  closeModal();
  showToast(currentEditId ? 'Sala atualizada com sucesso.' : 'Sala criada com sucesso.');
  if (btn) btn.disabled = false;
  if (btnText) btnText.textContent = currentEditId ? 'Salvar Alterações' : 'Salvar Sala';
}

function confirmDelete(id) {
  pendingDeleteId = id;
  const cOv = document.getElementById('confirm-overlay');
  if (cOv) cOv.classList.add('open');
}

async function doDelete() {
  try {
    const res = await apiFetch(`/salas/${pendingDeleteId}`, { method: 'DELETE' });
    if (!res.ok && res.status !== 404) throw new Error();
  } catch { }

  rooms = rooms.filter(r => r.id != pendingDeleteId);
  updateStats(); updateBadge(); updateFilters(); renderRooms();
  closeConfirm();
  showToast('Sala excluída com sucesso.', 'success');
}

function closeConfirm()       { 
  const cOv = document.getElementById('confirm-overlay');
  if (cOv) cOv.classList.remove('open'); 
}
function closeConfirmOnBg(e)  { if (e.target === document.getElementById('confirm-overlay')) closeConfirm(); }

// ══════════════════════════════════════════════════════
// DETAIL
// ══════════════════════════════════════════════════════
function openDetail(id) {
  const r = rooms.find(room => room.id == id);
  if (!r) return;
  currentDetailId = id;

  const hero = r.fotos && r.fotos.length
    ? `<img src="${r.fotos[0]}" alt="${r.nome}">`
    : `<div class="detail-hero-placeholder"><i class="ti ti-building"></i><span>Sem foto cadastrada</span></div>`;

  const comodidadesHtml = (r.comodidades && r.comodidades.length)
    ? r.comodidades.map(c => `<span class="amenity-chip selected" style="cursor:default">${c}</span>`).join('')
    : '<span style="color:var(--text3);font-size:13px">Nenhuma comodidade cadastrada</span>';

  const attachments = [];
  if (r.temPlanta) attachments.push(`<div class="attachment-card pdf"><i class="ti ti-file-type-pdf"></i><div class="attachment-card-name">Planta_${r.codigo}.pdf</div></div>`);
  if (r.fotos?.length > 0) r.fotos.forEach((_,i) => attachments.push(`<div class="attachment-card img"><i class="ti ti-photo"></i><div class="attachment-card-name">foto_${i+1}.jpg</div></div>`));

  const dTitle = document.getElementById('detail-title');
  const dBody = document.getElementById('detail-body');
  const dFooter = document.getElementById('detail-footer');
  
  if (dTitle) dTitle.textContent = r.nome;
  if (dBody) dBody.innerHTML = `
    <div class="detail-hero">
      ${hero}
      <div class="detail-badges">
        <span class="room-status-badge status-${r.status}">${statusLabel(r.status)}</span>
        ${r.temPlanta ? '<span class="has-pdf-badge"><i class="ti ti-file-type-pdf"></i> Planta</span>' : ''}
      </div>
    </div>
    <div class="detail-grid">
      <div class="detail-field"><div class="detail-field-label">Código</div><div class="detail-field-value">${r.codigo}</div></div>
      <div class="detail-field"><div class="detail-field-label">Andar / Local</div><div class="detail-field-value">${r.andar}</div></div>
      <div class="detail-field"><div class="detail-field-label">Capacidade</div><div class="detail-field-value">${r.capacidade} pessoas</div></div>
      <div class="detail-field"><div class="detail-field-label">Área</div><div class="detail-field-value">${r.area ? r.area+' m²' : '—'}</div></div>
      <div class="detail-field"><div class="detail-field-label">Tipo</div><div class="detail-field-value">${tipoLabel(r.tipo)}</div></div>
      <div class="detail-field"><div class="detail-field-label">Responsável</div><div class="detail-field-value">${r.responsavel || '—'}</div></div>
    </div>
    ${r.descricao ? `<div style="margin-bottom:20px"><div class="detail-field-label" style="margin-bottom:8px;font-size:12px;color:var(--text3)">Descrição</div><p style="font-size:14px;color:var(--text2);line-height:1.7">${r.descricao}</p></div>` : ''}
    <div style="margin-bottom:20px">
      <div class="section-header"><div class="section-header-title">Comodidades</div><div class="section-header-line"></div></div>
      <div class="amenity-chips">${comodidadesHtml}</div>
    </div>
    ${attachments.length ? `<div><div class="section-header"><div class="section-header-title">Anexos</div><div class="section-header-line"></div></div><div class="attachments-grid">${attachments.join('')}</div></div>` : ''}
  `;

  if (dFooter) dFooter.innerHTML = `
    <button class="btn btn-ghost" onclick="closeDetail()">Fechar</button>
    <button class="btn btn-danger" onclick="closeDetail();confirmDelete(${r.id})"><i class="ti ti-trash"></i> Excluir</button>
    <button class="btn btn-primary" onclick="closeDetail();openEdit(${r.id})"><i class="ti ti-edit"></i> Editar</button>
  `;

  const dOv = document.getElementById('detail-overlay');
  if (dOv) dOv.classList.add('open');
}

function closeDetail()      { 
  const dOv = document.getElementById('detail-overlay');
  if (dOv) dOv.classList.remove('open'); 
}
function closeDetailOnBg(e) { if (e.target === document.getElementById('detail-overlay')) closeDetail(); }

// ══════════════════════════════════════════════════════
// UPLOAD
// ══════════════════════════════════════════════════════
function switchUploadTab(tab, btn) {
  const tPlanta = document.getElementById('tab-planta');
  const tFotos  = document.getElementById('tab-fotos');
  if (tPlanta) tPlanta.style.display = tab === 'planta' ? '' : 'none';
  if (tFotos) tFotos.style.display  = tab === 'fotos'  ? '' : 'none';
  document.querySelectorAll('.upload-tab').forEach(t => t.classList.remove('active'));
  if (btn) btn.classList.add('active');
}

function handleDragOver(e, zoneId)  { e.preventDefault(); const z = document.getElementById(zoneId); if (z) z.classList.add('drag-over'); }
function handleDragLeave(zoneId)    { const z = document.getElementById(zoneId); if (z) z.classList.remove('drag-over'); }
function handleDrop(e, type)        { e.preventDefault(); const z = document.getElementById(type==='planta'?'drop-planta':'drop-fotos'); if (z) z.classList.remove('drag-over'); processFiles(e.dataTransfer.files, type); }
function handleFileSelect(e, type)  { processFiles(e.target.files, type); }

function processFiles(fileList, type) {
  const files = Array.from(fileList);
  if (type === 'planta') {
    const pdfs = files.filter(f => f.type === 'application/pdf');
    if (pdfs.length !== files.length) showToast('Apenas PDFs são aceitos para plantas.', 'error');
    uploadedPlantas = [...uploadedPlantas, ...pdfs];
    renderPlantaList();
  } else {
    const imgs = files.filter(f => f.type.startsWith('image/'));
    if (imgs.length !== files.length) showToast('Apenas imagens são aceitas.', 'error');
    uploadedFotos = [...uploadedFotos, ...imgs];
    renderFotosPreview();
  }
}

function renderPlantaList() {
  const pList = document.getElementById('planta-list');
  if (pList) pList.innerHTML = uploadedPlantas.map((f,i) => `
    <div class="file-item">
      <i class="ti ti-file-type-pdf file-item-icon pdf"></i>
      <span class="file-item-name">${f.name}</span>
      <span class="file-item-size">${formatSize(f.size)}</span>
      <button class="file-item-remove" onclick="removePlanta(${i})"><i class="ti ti-x"></i></button>
    </div>`).join('');
}

function removePlanta(i) { uploadedPlantas.splice(i,1); renderPlantaList(); }

function renderFotosPreview() {
  const grid = document.getElementById('fotos-preview');
  if (!grid) return;
  grid.innerHTML = '';
  uploadedFotos.forEach((f,i) => {
    const reader = new FileReader();
    reader.onload = e => {
      const div = document.createElement('div');
      div.className = 'img-preview';
      div.innerHTML = `<img src="${e.target.result}" alt=""><button class="img-preview-remove" onclick="removeFoto(${i})"><i class="ti ti-x" style="font-size:10px"></i></button>`;
      grid.appendChild(div);
    };
    reader.readAsDataURL(f);
  });
}

function removeFoto(i) { uploadedFotos.splice(i,1); renderFotosPreview(); }

function formatSize(bytes) {
  if (bytes < 1024)    return bytes + ' B';
  if (bytes < 1048576) return (bytes/1024).toFixed(1) + ' KB';
  return (bytes/1048576).toFixed(1) + ' MB';
}

// ══════════════════════════════════════════════════════
// AMENITY CHIPS
// ══════════════════════════════════════════════════════
function toggleAmenity(el) { if (el) el.classList.toggle('selected'); }

// ══════════════════════════════════════════════════════
// TOAST
// ══════════════════════════════════════════════════════
function showToast(msg, type = 'success') {
  const toasts = document.getElementById('toasts');
  if (!toasts) return;
  const t = document.createElement('div');
  t.className = `toast ${type}`;
  t.innerHTML = `<i class="ti ti-${type==='success'?'circle-check':'alert-circle'}"></i>${msg}`;
  toasts.appendChild(t);
  setTimeout(() => t.remove(), 3500);
}

