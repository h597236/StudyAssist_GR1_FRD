let aktivNokkel = 'oppfolgingssporsmal';
let originalInnhald = '';

async function lastPrompt(nokkel) {
    try {
        const res = await api(`api/admin/prompt/${nokkel}`);
        if (!res.ok) throw new Error();
        const data = await res.json();

        const aktiv = data.aktiv;
        const versjonar = data.versjonar;

        if (aktiv && aktiv.innhald) {
            document.getElementById('promptInnhald').value = aktiv.innhald;
            originalInnhald = aktiv.innhald;
            const dato = new Date(aktiv.endraTid).toLocaleDateString('no-NO');
            document.getElementById('promptMeta').textContent =
                `Sist endra: ${dato} av ${aktiv.endraAv?.email ?? 'ukjend'}`;
        } else {
            document.getElementById('promptInnhald').value = '';
            originalInnhald = '';
            document.getElementById('promptMeta').textContent = 'Ingen aktiv prompt enno.';
        }

        oppdaterTeikntellar();
        renderVersjonar(versjonar, aktiv?.id);

    } catch (e) {
        console.error('Feil ved henting av prompt:', e);
    }
}

function renderVersjonar(versjonar, aktivId) {
    const liste = document.getElementById('versjonListe');
    liste.innerHTML = '';

    if (!versjonar || versjonar.length === 0) {
        liste.innerHTML = '<p style="font-size:0.85rem; color: var(--text-muted);">Ingen versjonar enno.</p>';
        return;
    }

    [...versjonar].reverse().forEach(v => {
        const erAktiv = v.id === aktivId;
        const dato = new Date(v.endraTid).toLocaleDateString('no-NO');

        const item = document.createElement('div');
        item.className = 'version-item' + (erAktiv ? ' aktiv' : '');
        item.innerHTML = `
            <div class="version-top">
                <span class="version-label">Versjon ${v.versjon}</span>
                ${erAktiv ? '<span class="version-aktiv-badge">aktiv</span>' : ''}
            </div>
            <div class="version-date">${dato}</div>
            <div class="version-btns">
                <button class="btn btn-secondary btn-xs" onclick="sjåVersjon(${v.id})">Sjå</button>
                ${!erAktiv ? `<button class="btn btn-primary btn-xs" onclick="gjenopprett(${v.id})">Gjenopprett</button>` : ''}
            </div>
        `;
        liste.appendChild(item);
    });
}

async function sjåVersjon(id) {
    try {
        const res = await api(`api/admin/prompt/versjon/${id}`);
        if (!res.ok) throw new Error();
        const v = await res.json();
        alert(v.innhald);
    } catch (e) {
        console.error(e);
    }
}

async function lagrePrompt() {
    const innhald = document.getElementById('promptInnhald').value.trim();
    if (!innhald) {
        alert('Prompten kan ikkje vere tom.');
        return;
    }

    try {
        const res = await api(`api/admin/prompt/${aktivNokkel}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ innhald })
        });

        if (!res.ok) throw new Error();
        await lastPrompt(aktivNokkel);

    } catch (e) {
        alert('Feil ved lagring.');
    }
}

async function gjenopprett(id) {
    try {
        const res = await api(`api/admin/prompt/${id}/gjenopprett`, {
            method: 'POST'
        });
        if (!res.ok) throw new Error();
        await lastPrompt(aktivNokkel);
    } catch (e) {
        alert('Feil ved gjenoppretting.');
    }
}

function tilbakestill() {
    document.getElementById('promptInnhald').value = originalInnhald;
    oppdaterTeikntellar();
}

function oppdaterTeikntellar() {
    const len = document.getElementById('promptInnhald').value.length;
    document.getElementById('teiknteller').textContent = `${len} tekn`;
}

function byttPrompt(nokkel, knapp) {
    aktivNokkel = nokkel;
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    knapp.classList.add('active');
    lastPrompt(nokkel);
}

// Init
document.getElementById('brukarnamn').textContent = localStorage.getItem('brukarnavn') || '';
lastPrompt(aktivNokkel);