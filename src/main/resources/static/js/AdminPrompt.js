let aktivNokkel = 'oppfolgingssporsmal';
let originalInnhald = '';

// Kjente plasshalderar per prompt-type
const KJENTE_PLASSHALDERAR = {
    'oppfolgingssporsmal': {
        pakravd: ['{{sporsmal}}'],
        system: ['{{emne}}', '{{tema}}']
    },
    'vurdering': {
        pakravd: ['{{sporsmal}}', '{{oppfolgingssporsmal}}', '{{svar}}'],
        system: ['{{emne}}', '{{tema}}']
    },
    'fasit': {
        pakravd: ['{{sporsmal}}'],
        system: ['{{emne}}', '{{tema}}']
    }
};

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
                ${!erAktiv ? `<button class="btn btn-primary btn-xs" onclick="settAktiv(${v.id})">Sett aktiv</button>` : ''}
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

        document.getElementById('promptInnhald').value = v.innhald;
        oppdaterTeikntellar();

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

async function settAktiv(id) {
    try {
        const res = await api(`api/admin/prompt/${id}/settaktiv`, {
            method: 'POST'
        });
        if (!res.ok) throw new Error();
        await lastPrompt(aktivNokkel);
    } catch (e) {
        alert('Feil ved aktivering.');
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

// ── Test-funksjonalitet ──

function finnPlasshalderar(tekst) {
    const matches = tekst.match(/\{\{[^}]+\}\}/g);
    return matches ? [...new Set(matches)] : [];
}

function opnTestModal() {
    const innhald = document.getElementById('promptInnhald').value.trim();
    if (!innhald) {
        alert('Skriv ein prompt først.');
        return;
    }

    const funne = finnPlasshalderar(innhald);
    const konfig = KJENTE_PLASSHALDERAR[aktivNokkel] || { pakravd: [], system: [] };
    const manglande = konfig.pakravd.filter(k => !funne.includes(k));

    const feltContainer = document.getElementById('testInputFelt');
    feltContainer.innerHTML = '';

    // Åtvar berre om påkravde brukarinput manglar
    if (manglande.length > 0) {
        const advarsel = document.createElement('p');
        advarsel.style.cssText = 'font-size:0.82rem; color:#c83232; background:#fde8e0; padding:10px 14px; border-radius:8px; margin-bottom:12px;';
        advarsel.textContent = `⚠ Desse påkravde plasshalderar manglar: ${manglande.join(', ')}`;
        feltContainer.appendChild(advarsel);
    }

    // Info om systemvariablar
    const systemFunne = funne.filter(p => konfig.system.includes(p));
    if (systemFunne.length > 0) {
        const info = document.createElement('p');
        info.style.cssText = 'font-size:0.82rem; color: var(--text-muted); background: var(--bg); padding:10px 14px; border-radius:8px; margin-bottom:16px;';
        info.textContent = `ℹ ${systemFunne.join(', ')} blir fylt inn automatisk av systemet.`;
        feltContainer.appendChild(info);
    }

    // Inputfelt berre for brukarinput-plasshalderar
    const brukarFunne = funne.filter(p => !konfig.system.includes(p));
    if (brukarFunne.length === 0 && manglande.length === 0) {
        const tom = document.createElement('p');
        tom.style.cssText = 'font-size:0.85rem; color: var(--text-muted); margin-bottom: 12px;';
        tom.textContent = 'Ingen brukarinput-plasshalderar funne.';
        feltContainer.appendChild(tom);
    }

    brukarFunne.forEach(p => {
        const nøkkel = p.replace(/[{}]/g, '');
        const div = document.createElement('div');
        div.className = 'test-input-group';
        div.innerHTML = `
            <label>${p}</label>
            <input type="text" id="testInput_${nøkkel}" placeholder="Testverdi for ${p}" oninput="oppdaterTestResultat()">
        `;
        feltContainer.appendChild(div);
    });

    document.getElementById('testAiSvar').style.display = 'none';
    document.getElementById('testAiResultat').textContent = '';

    oppdaterTestResultat();
    document.getElementById('testModal').classList.add('active');
}

function oppdaterTestResultat() {
    let tekst = document.getElementById('promptInnhald').value;
    const funne = finnPlasshalderar(tekst);
    const konfig = KJENTE_PLASSHALDERAR[aktivNokkel] || { pakravd: [], system: [] };

    funne.forEach(p => {
        const nøkkel = p.replace(/[{}]/g, '');
        const input = document.getElementById(`testInput_${nøkkel}`);

        let verdi;
        if (konfig.system.includes(p)) {
            // Systemvariablar får ein tydeleg standardverdi i forhåndsvisinga
            verdi = `[${nøkkel.toUpperCase()}]`;
        } else {
            verdi = input ? (input.value || p) : p;
        }
        tekst = tekst.replaceAll(p, verdi);
    });

    document.getElementById('testResultatTekst').textContent = tekst;
}

async function sendTestTilAI() {
    const ferdigTekst = document.getElementById('testResultatTekst').textContent;
    if (!ferdigTekst || ferdigTekst === '—') return;

    const knapp = document.getElementById('testAiKnapp');
    knapp.textContent = 'Sender...';
    knapp.disabled = true;

    document.getElementById('testAiSvar').style.display = 'block';
    document.getElementById('testAiResultat').textContent = 'Ventar på AI-svar...';

    try {
        const res = await api('api/ai/ask', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ question: ferdigTekst })
        });

        if (!res.ok) throw new Error();
        const data = await res.json();
        document.getElementById('testAiResultat').textContent =
            data.explanation || data.follow_up_question || JSON.stringify(data);

    } catch (e) {
        document.getElementById('testAiResultat').textContent = 'Feil ved AI-kall.';
    } finally {
        knapp.textContent = 'Send til AI';
        knapp.disabled = false;
    }
}

function lukkTestModal() {
    document.getElementById('testModal').classList.remove('active');
}

// Init
document.getElementById('brukarnamn').textContent = localStorage.getItem('brukarnavn') || '';
lastPrompt(aktivNokkel);