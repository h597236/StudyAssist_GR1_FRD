let valtBrukarId = null;
let debounceTimer = null;

// ── Init ──
document.getElementById('brukarnamn').textContent = localStorage.getItem('brukarnavn') || '';

window.addEventListener('DOMContentLoaded', async () => {
    await sjekkAdmin();
    lastBrukarar();
});

async function sjekkAdmin() {
    try {
        const res = await api('api/brukar/me');
        if (!res.ok) {
            window.location.href = getBase() + '/login';
            return;
        }
        const brukar = await res.json();
        if (brukar.rolle?.toUpperCase() !== 'ADMIN') {
            window.location.href = getBase() + '/home';
        }
    } catch {
        window.location.href = getBase() + '/login';
    }
}

// ── Hent og vis brukarar ──
async function lastBrukarar() {
    const email = document.getElementById('sokEmail').value.trim();
    const rolle = document.getElementById('filterRolle').value;

    const params = new URLSearchParams();
    if (email) params.append('email', email);
    if (rolle) params.append('rolle', rolle);

    try {
        const res = await api(`api/admin/brukarar?${params}`);
        if (!res.ok) throw new Error();
        const brukarar = await res.json();
        visBrukarar(brukarar);
    } catch {
        document.getElementById('brukarTbody').innerHTML =
            '<tr><td colspan="3" class="admin-table-empty">Feil ved henting av brukarar.</td></tr>';
    }
}

function visBrukarar(brukarar) {
    const tbody = document.getElementById('brukarTbody');
    document.getElementById('totalLabel').textContent = `Totalt: ${brukarar.length} brukarar`;

    if (!brukarar.length) {
        tbody.innerHTML = '<tr><td colspan="3" class="admin-table-empty">Ingen brukarar funne.</td></tr>';
        return;
    }

    tbody.innerHTML = brukarar.map(b => `
        <tr>
            <td>${escHtml(b.email)}</td>
            <td>
                <span class="rolle-badge ${b.rolle?.toUpperCase() === 'ADMIN' ? 'rolle-admin' : 'rolle-student'}">
                    ${escHtml(b.rolle || 'STUDENT')}
                </span>
            </td>
            <td>
                <button class="btn btn-secondary btn-xs" onclick="opneModal(${b.id}, '${escHtml(b.rolle || 'STUDENT')}')">Endre rolle</button>
                <button class="btn btn-danger btn-xs" onclick="slettBrukar(${b.id}, '${escHtml(b.email)}')">Slett</button>
            </td>
        </tr>
    `).join('');
}

// ── Slett ──
async function slettBrukar(id, email) {
    if (!confirm(`Er du sikker på at du vil slette ${email}?`)) return;
    try {
        const res = await api(`api/admin/brukarar/${id}`, { method: 'DELETE' });
        if (!res.ok) {
            const tekst = await res.text();
            alert(tekst || 'Sletting feilet.');
            return;
        }
        lastBrukarar();
    } catch {
        alert('Sletting feilet.');
    }
}

// ── Modal: endre rolle ──
function opneModal(id, rolle) {
    valtBrukarId = id;
    document.getElementById('nyRolleSelect').value = rolle.toUpperCase();
    document.getElementById('endreRolleModal').classList.add('active');
}

function lukkModal() {
    document.getElementById('endreRolleModal').classList.remove('active');
    valtBrukarId = null;
}

async function lagreRolle() {
    if (!valtBrukarId) return;
    const nyRolle = document.getElementById('nyRolleSelect').value;

    try {
        const res = await api(`api/admin/brukarar/${valtBrukarId}/rolle`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ rolle: nyRolle })
        });
        if (!res.ok) throw new Error();
        lukkModal();
        lastBrukarar();
    } catch {
        alert('Oppdatering feilet.');
    }
}

// ── Utils ──
function debouncedSok() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => lastBrukarar(), 300);
}

function escHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// Lukk modal ved klikk utanfor
document.getElementById('endreRolleModal').addEventListener('click', function (e) {
    if (e.target === this) lukkModal();
});