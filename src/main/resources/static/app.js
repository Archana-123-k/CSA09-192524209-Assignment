const byId = (id) => document.getElementById(id);
const money = (value) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(Number(value || 0));
const safe = (value) => String(value ?? '—').replace(/[&<>"']/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char]);
const prettyType = (type) => ({ TWO_WHEELER: 'Two-wheeler', CAR: 'Car', ELECTRIC_CAR: 'Electric car', E_BIKE: 'E-bike', DELIVERY_VAN: 'Delivery van' }[type] || type || 'Vehicle');

async function api(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }
  });
  if (response.status === 204) return null;
  const result = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(result.message || `Request failed (${response.status})`);
  return result;
}

function showNotice(message, type = 'success') {
  const notice = byId('notice');
  notice.textContent = message;
  notice.className = `notice show ${type}`;
  window.clearTimeout(showNotice.timer);
  showNotice.timer = window.setTimeout(() => { notice.className = 'notice'; }, 5200);
}

function dateLabel(value, includeDate = true) {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '—';
  return new Intl.DateTimeFormat('en-IN', includeDate
    ? { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' }
    : { hour: '2-digit', minute: '2-digit' }).format(date);
}

function durationLabel(minutes) {
  if (minutes == null) return 'In progress';
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return hours ? `${hours}h ${mins}m` : `${mins}m`;
}

function displayView(name) {
  document.querySelectorAll('.view').forEach((view) => view.classList.toggle('active-view', view.id === `${name}-view`));
  document.querySelectorAll('.nav-link').forEach((link) => link.classList.toggle('active', link.dataset.view === name));
  const titles = { dashboard: 'Parking overview', tickets: 'Parking history', passes: 'Passes & users', reports: 'Facility reports' };
  byId('page-title').textContent = titles[name] || titles.dashboard;
  if (name === 'tickets') loadTickets();
  if (name === 'passes') loadPasses();
  if (name === 'reports') loadReport();
}

function renderSlots(slots) {
  const floors = new Map();
  slots.forEach((slot) => {
    if (!floors.has(slot.floor)) floors.set(slot.floor, []);
    floors.get(slot.floor).push(slot);
  });
  byId('floor-list').innerHTML = floors.size ? [...floors.entries()].map(([floor, spaces]) => {
    const free = spaces.filter((slot) => !slot.occupied).length;
    return `<div class="floor-row"><span class="floor-name">Floor ${safe(floor)}</span><div class="slot-track" aria-label="${free} of ${spaces.length} available">${spaces.map((slot) => `<i class="slot-cell ${slot.occupied ? 'occupied' : ''}" title="${safe(slot.type)} · ${slot.occupied ? safe(slot.registrationNumber) : 'Available'}" aria-label="${slot.occupied ? 'Occupied' : 'Available'}"></i>`).join('')}</div><span class="floor-count">${free} / ${spaces.length} free</span></div>`;
  }).join('') : '<div class="loading">No parking slots configured.</div>';
}

function renderRecent(tickets) {
  const recent = tickets.slice(0, 5);
  byId('recent-list').innerHTML = recent.length ? recent.map((ticket) => `<div class="recent-item"><span class="vehicle-avatar">${ticket.status === 'ACTIVE' ? '⌑' : '✓'}</span><div><div class="recent-primary">${safe(ticket.registrationNumber)} · ${safe(prettyType(ticket.vehicleType))}</div><div class="recent-secondary">${safe(ticket.ticketId.slice(0, 8))} · Floor ${safe(ticket.floor)}, slot ${safe(ticket.slotNumber)} · ${safe(dateLabel(ticket.entryTime))}</div></div><span class="recent-state">${ticket.status === 'ACTIVE' ? 'Parked' : safe(money(ticket.totalFee))}</span></div>`).join('') : '<div class="loading">No parking activity yet.</div>';
}

async function loadDashboard() {
  try {
    const [dashboard, slots, tickets] = await Promise.all([api('/dashboard'), api('/slots'), api('/tickets')]);
    byId('total-slots').textContent = dashboard.totalSlots;
    byId('available-slots').textContent = dashboard.availableSlots;
    byId('occupied-slots').textContent = dashboard.occupiedSlots;
    byId('occupancy-copy').textContent = `${dashboard.activeTickets} active parking ${dashboard.activeTickets === 1 ? 'session' : 'sessions'}`;
    byId('revenue-today').textContent = money(dashboard.revenueToday);
    renderSlots(slots);
    renderRecent(tickets);
  } catch (error) {
    showNotice(error.message, 'error');
    byId('floor-list').innerHTML = '<div class="loading">Could not load facility data. Check the database connection.</div>';
  }
}

function renderTicketRows(tickets) {
  byId('ticket-rows').innerHTML = tickets.length ? tickets.map((ticket) => `<tr><td class="ticket-id" title="${safe(ticket.ticketId)}">${safe(ticket.ticketId.slice(0, 8))}…</td><td><strong>${safe(ticket.registrationNumber)}</strong><br><span class="recent-secondary">${safe(prettyType(ticket.vehicleType))}</span></td><td>F${safe(ticket.floor)} · ${safe(ticket.slotNumber)}</td><td>${safe(dateLabel(ticket.entryTime))}</td><td>${safe(dateLabel(ticket.exitTime))}</td><td>${safe(durationLabel(ticket.durationMinutes))}</td><td>${ticket.totalFee == null ? '—' : safe(money(ticket.totalFee))}</td><td><span class="badge ${ticket.status === 'ACTIVE' ? '' : 'closed'}">${safe(ticket.status)}</span></td></tr>`).join('') : '<tr><td colspan="8" class="loading">No tickets have been recorded.</td></tr>';
}

async function loadTickets() {
  try { renderTicketRows(await api('/tickets')); }
  catch (error) { showNotice(error.message, 'error'); }
}

async function loadUsers() {
  const users = await api('/users');
  ['entry-user', 'pass-user'].forEach((id) => {
    const select = byId(id);
    const first = id === 'entry-user' ? '<option value="">Visitor / no account</option>' : '<option value="">Select a user</option>';
    select.innerHTML = first + users.map((user) => `<option value="${safe(user.id)}">${safe(user.fullName)} · ${safe(user.category)}</option>`).join('');
  });
  return users;
}

async function loadPasses() {
  try {
    const [users, passes] = await Promise.all([loadUsers(), api('/passes')]);
    const names = new Map(users.map((user) => [user.id, user]));
    const active = passes.filter((pass) => pass.active);
    byId('pass-rows').innerHTML = active.length ? active.map((pass) => {
      const user = names.get(pass.userId) || {};
      return `<tr><td><strong>${safe(user.fullName || 'Unknown user')}</strong></td><td>${safe(user.email)}</td><td>${safe(user.category)}</td><td>${pass.passType === 'ONE_DAY' ? 'One-day visitor' : 'Monthly'}</td><td>${safe(dateLabel(pass.validTo))}</td></tr>`;
    }).join('') : '<tr><td colspan="5" class="loading">No active passes. Add a user and issue a pass above.</td></tr>';
  } catch (error) { showNotice(error.message, 'error'); }
}

async function loadReport() {
  try {
    const report = await api('/reports');
    byId('report-entries').textContent = report.ticketsToday;
    byId('report-exits').textContent = report.exitsToday;
    byId('report-duration').textContent = durationLabel(report.averageDurationMinutes);
    byId('report-active').textContent = report.activeTickets;
    byId('report-revenue').textContent = money(report.revenueToday);
  } catch (error) { showNotice(error.message, 'error'); }
}

function showTicket(ticket, exiting = false) {
  const details = [
    ['Ticket ID', ticket.ticketId], ['Registration', ticket.registrationNumber],
    ['Parking space', `Floor ${ticket.floor} · Slot ${ticket.slotNumber}`],
    ['Entry time', dateLabel(ticket.entryTime)],
    ...(exiting ? [['Duration', durationLabel(ticket.durationMinutes)], ['Parking fee', money(ticket.parkingFee)],
      ['EV charging', money(ticket.chargingFee)], ['Pass applied', ticket.passApplied ? 'Yes — parking waived' : 'No'], ['Total due', money(ticket.totalFee)]] : [['Status', 'Active']])
  ];
  byId('modal-kicker').textContent = exiting ? 'PARKING PAYMENT' : 'PARKING TICKET';
  byId('modal-title').textContent = exiting ? `Total ${money(ticket.totalFee)}` : 'You’re all set';
  byId('modal-details').innerHTML = details.map(([label, value]) => `<div><span>${safe(label)}</span><strong>${safe(value)}</strong></div>`).join('');
  byId('ticket-modal').classList.add('open');
  byId('ticket-modal').setAttribute('aria-hidden', 'false');
}

function formObject(form) { return Object.fromEntries(new FormData(form).entries()); }

document.querySelectorAll('.nav-link').forEach((link) => link.addEventListener('click', () => displayView(link.dataset.view)));
document.querySelectorAll('[data-goto]').forEach((button) => button.addEventListener('click', () => displayView(button.dataset.goto)));
byId('refresh-button').addEventListener('click', loadDashboard);
byId('ticket-refresh').addEventListener('click', loadTickets);

byId('entry-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const data = formObject(event.currentTarget);
  data.userId = data.userId ? Number(data.userId) : null;
  try {
    const ticket = await api('/entry', { method: 'POST', body: JSON.stringify(data) });
    event.currentTarget.reset();
    showTicket(ticket);
    showNotice('Entry recorded and a parking space has been assigned.');
    await loadDashboard();
  } catch (error) { showNotice(error.message, 'error'); }
});

byId('exit-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const data = formObject(event.currentTarget);
  const lookup = data.lookup.trim();
  const isTicket = /^[0-9a-f]{8}-[0-9a-f-]{27}$/i.test(lookup);
  const payload = { ticketId: isTicket ? lookup : null, registrationNumber: isTicket ? null : lookup,
    kwh: Number(data.kwh || 0), paymentMethod: data.paymentMethod };
  try {
    const ticket = await api('/exit', { method: 'POST', body: JSON.stringify(payload) });
    event.currentTarget.reset();
    showTicket(ticket, true);
    showNotice('Vehicle checked out and the parking space is available again.');
    await loadDashboard();
  } catch (error) { showNotice(error.message, 'error'); }
});

byId('user-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    await api('/users', { method: 'POST', body: JSON.stringify(formObject(event.currentTarget)) });
    event.currentTarget.reset();
    await loadPasses();
    showNotice('User account created.');
  } catch (error) { showNotice(error.message, 'error'); }
});

byId('pass-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const data = formObject(event.currentTarget);
  data.userId = Number(data.userId);
  data.validFrom = new Date(data.validFrom).toISOString();
  data.validTo = new Date(data.validTo).toISOString();
  try {
    await api('/passes', { method: 'POST', body: JSON.stringify(data) });
    event.currentTarget.reset();
    await loadPasses();
    showNotice('Parking pass issued.');
  } catch (error) { showNotice(error.message, 'error'); }
});

function closeModal() {
  byId('ticket-modal').classList.remove('open');
  byId('ticket-modal').setAttribute('aria-hidden', 'true');
}
byId('modal-close').addEventListener('click', closeModal);
byId('modal-done').addEventListener('click', closeModal);
byId('ticket-modal').addEventListener('click', (event) => { if (event.target === event.currentTarget) closeModal(); });
document.addEventListener('keydown', (event) => { if (event.key === 'Escape') closeModal(); });

byId('today-label').textContent = new Intl.DateTimeFormat('en-IN', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' }).format(new Date());
loadDashboard();
loadUsers().catch(() => { /* User choices become available once the API is reachable. */ });