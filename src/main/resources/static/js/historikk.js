requireLogin();

// ── Data ──
var brukarnavn = localStorage.getItem("brukarnavn") || "bruker@email.com";
var activeFilter = "alle";
var allHistory = [];
var activeSession = null;

document.getElementById("sidebarBrukarnavn").textContent = brukarnavn;

// ── Load history from backend ──
async function loadHistorikk() {
    try {
        const res = await api('/api/historikk');
        if (!res.ok) throw new Error('Kunne ikkje hente historikk');
        allHistory = await res.json();
        renderFilterChips();
        filterHistory();
    } catch (err) {
        console.error(err);
        const empty = document.getElementById('histEmpty');
        empty.style.display = 'block';
        empty.querySelector('p').textContent = 'Feil ved henting av historikk.';
    }
}

// ── Filter chips (by emne) ──
function renderFilterChips() {
    var chips = document.getElementById("filterChips");
    chips.innerHTML = '<button class="hist-chip active" onclick="setFilter(\'alle\', this)">Alle</button>';

    var seen = [];
    allHistory.forEach(function(item) {
        if (item.emneNamn && seen.indexOf(item.emneNamn) === -1) {
            seen.push(item.emneNamn);
            var btn = document.createElement("button");
            btn.className = "hist-chip";
            btn.textContent = item.emneNamn;
            btn.onclick = function() { setFilter(item.emneNamn, btn); };
            chips.appendChild(btn);
        }
    });
}

function setFilter(filter, btn) {
    activeFilter = filter;
    document.querySelectorAll(".hist-chip").forEach(function(c) {
        c.classList.remove("active");
    });
    btn.classList.add("active");
    filterHistory();
}

// ── Filter & render ──
function filterHistory() {
    var query = document.getElementById("searchInput").value.toLowerCase().trim();
    var results = document.getElementById("histResults");
    var empty = document.getElementById("histEmpty");

    var filtered = allHistory.filter(function(item) {
        var matchFilter = activeFilter === "alle" || item.emneNamn === activeFilter;
        var matchSearch = !query ||
            contains(item.startSporsmal, query) ||
            contains(item.brukarRefleksjon, query) ||
            contains(item.fasitSvar, query) ||
            contains(item.temaNamn, query) ||
            contains(item.emneNamn, query);
        return matchFilter && matchSearch;
    });

    results.innerHTML = "";

    if (filtered.length === 0) {
        empty.style.display = "block";
        return;
    }
    empty.style.display = "none";

    filtered.sort(function(a, b) {
        return new Date(b.opprettaTid) - new Date(a.opprettaTid);
    });

    filtered.forEach(function(item) {
        results.appendChild(buildCard(item));
    });
}

function contains(text, query) {
    return text != null && text.toLowerCase().indexOf(query) !== -1;
}

// ── Build a single history card ──
function buildCard(item) {
    var card = document.createElement("div");
    card.className = "hist-card";
    card.style.cursor = "pointer";
    card.onclick = function() { openHistorikkModal(item); };

    var date = item.opprettaTid
        ? new Date(item.opprettaTid).toLocaleDateString("no-NO", {
            day: "2-digit", month: "short", year: "numeric"
        })
        : "";

    var ratingHtml = "";
    if (item.rating != null && item.rating > 0) {
        var stars = "";
        for (var i = 1; i <= 5; i++) {
            stars += '<span class="hist-star' + (i <= item.rating ? " filled" : "") + '">★</span>';
        }
        ratingHtml = '<div class="hist-rating">' + stars + "</div>";
    }

    var stateLabel = "";
    if (item.state === "COMPLETED") {
        stateLabel = '<span class="hist-state completed">Fullført</span>';
    } else if (item.state === "FINAL_ANSWER") {
        stateLabel = '<span class="hist-state final">Avslutta</span>';
    } else {
        stateLabel = '<span class="hist-state ongoing">Pågåande</span>';
    }

    card.innerHTML =
        '<div class="hist-card-top">' +
        '<div class="hist-card-tags">' +
        (item.emneNamn ? '<span class="hist-tag">' + item.emneNamn + "</span>" : "") +
        (item.temaNamn ? '<span class="hist-tag hist-tag-tema">' + item.temaNamn + "</span>" : "") +
        stateLabel +
        "</div>" +
        '<div class="hist-card-meta">' +
        ratingHtml +
        '<span class="hist-card-date">' + date + "</span>" +
        "</div>" +
        "</div>" +
        '<div class="hist-card-question">' + (item.startSporsmal || "") + "</div>" +
        (item.brukarRefleksjon
            ? '<div class="hist-card-answer">' + truncate(item.brukarRefleksjon, 160) + "</div>"
            : "");

    return card;
}

function truncate(text, maxLen) {
    if (!text) return "";
    return text.length > maxLen ? text.substring(0, maxLen) + "…" : text;
}

// ── Modal ──
function openHistorikkModal(item) {
    var isPagaande = item.state === "INITIAL" || item.state === "FOLLOW_UP";

    // Viss pågåande — send direkte til spørsmålssida
    if (isPagaande) {
        window.location.href = getBase() +
            "/sporsmal?emneId=" + encodeURIComponent(item.emneId) +
            "&temaId=" + encodeURIComponent(item.temaId) +
            "&sessionId=" + encodeURIComponent(item.sessionId);
        return;
    }

    activeSession = item;

    document.getElementById("modalEmneTag").textContent = item.emneNamn || "";
    document.getElementById("modalTemaTag").textContent = item.temaNamn || "";

    var stateEl = document.getElementById("modalStateTag");
    if (item.state === "COMPLETED") {
        stateEl.innerHTML = '<span class="hist-state completed">Fullført</span>';
    } else {
        stateEl.innerHTML = '<span class="hist-state final">Avslutta</span>';
    }

    var date = item.opprettaTid
        ? new Date(item.opprettaTid).toLocaleDateString("no-NO", {
            day: "2-digit", month: "long", year: "numeric"
        })
        : "";
    document.getElementById("modalDate").textContent = date;

    document.getElementById("modalSporsmal").textContent = item.startSporsmal || "";
    document.getElementById("modalOppfolging").textContent = item.oppfolgingsSporsmal || "";

    // Berre vis refleksjon/vurdering/fasit om dei finst
    setModalSection("modalRefleksjonSection", "modalRefleksjon", item.brukarRefleksjon);
    setModalSection("modalVurderingSection", "modalVurdering", item.vurdering);
    setModalSection("modalFasitSection", "modalFasit", item.fasitSvar);

    // Rating
    var ratingEl = document.getElementById("modalRating");
    if (item.rating != null && item.rating > 0) {
        var stars = "";
        for (var i = 1; i <= 5; i++) {
            stars += '<span class="hist-star' + (i <= item.rating ? " filled" : "") + '">★</span>';
        }
        ratingEl.innerHTML = '<div class="hist-rating">' + stars + "</div>";
        ratingEl.style.display = "block";
    } else {
        ratingEl.style.display = "none";
    }

    document.getElementById("modalContinueBtn").style.display = "none";
    document.getElementById("historikkModal").classList.add("active");
}

function setModalSection(sectionId, textId, value) {
    var section = document.getElementById(sectionId);
    if (value) {
        document.getElementById(textId).textContent = value;
        section.style.display = "block";
    } else {
        section.style.display = "none";
    }
}

function closeHistorikkModal() {
    document.getElementById("historikkModal").classList.remove("active");
    activeSession = null;
}

function fortsettChat() {
    if (!activeSession) return;
    window.location.href = getBase() +
        "/sporsmal?emneId=" + encodeURIComponent(activeSession.emneId) +
        "&temaId=" + encodeURIComponent(activeSession.temaId) +
        "&sessionId=" + encodeURIComponent(activeSession.sessionId);
}

// ── Init ──
window.onload = function() {
    loadEmner();
    loadHistorikk();
};

document.querySelectorAll(".modal-overlay").forEach(function(overlay) {
    overlay.addEventListener("click", function(e) {
        if (e.target === overlay) overlay.classList.remove("active");
    });
});

renderSidebar();
renderFilterChips();
filterHistory();