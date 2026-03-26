requireLogin();

// ── Data ──
var subjects = JSON.parse(localStorage.getItem("subjects")) || [];
var brukarnavn = localStorage.getItem("brukarnavn") || "bruker@email.com";
var activeFilter = "alle";

document.getElementById("sidebarBrukarnavn").textContent = brukarnavn;

function saveSubjects() {
    localStorage.setItem("subjects", JSON.stringify(subjects));
}

// ── Chat history (will come from backend API later) ──
var chatHistory = JSON.parse(localStorage.getItem("chatHistory")) || [];

// ── Render filter chips ──
function renderFilterChips() {
    var chips = document.getElementById("filterChips");
    chips.innerHTML = '<button class="hist-chip active" onclick="setFilter(\'alle\', this)">Alle</button>';

    var emneSet = [];
    chatHistory.forEach(function(item) {
        if (emneSet.indexOf(item.emne) === -1) {
            emneSet.push(item.emne);
        }
    });

    emneSet.forEach(function(emne) {
        var btn = document.createElement("button");
        btn.className = "hist-chip";
        btn.textContent = emne;
        btn.onclick = function() { setFilter(emne, btn); };
        chips.appendChild(btn);
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

// ── Filter & render history ──
function filterHistory() {
    var query = document.getElementById("searchInput").value.toLowerCase();
    var results = document.getElementById("histResults");
    var empty = document.getElementById("histEmpty");

    var filtered = chatHistory.filter(function(item) {
        var matchFilter = activeFilter === "alle" || item.emne === activeFilter;
        var matchSearch = !query ||
            item.question.toLowerCase().indexOf(query) !== -1 ||
            item.answer.toLowerCase().indexOf(query) !== -1 ||
            item.emne.toLowerCase().indexOf(query) !== -1 ||
            item.tema.toLowerCase().indexOf(query) !== -1;
        return matchFilter && matchSearch;
    });

    results.innerHTML = "";

    if (filtered.length === 0) {
        empty.style.display = "block";
        return;
    }

    empty.style.display = "none";

    filtered.forEach(function(item) {
        var card = document.createElement("div");
        card.className = "hist-card";
        card.onclick = function() {
            window.location.href = "/sporsmal?subject=" + encodeURIComponent(item.emne) + "&topic=" + encodeURIComponent(item.tema);
        };
        card.innerHTML =
            '<div class="hist-card-top">' +
            '<div class="hist-card-tags">' +
            '<span class="hist-tag">' + item.emne + '</span>' +
            '<span class="hist-tag hist-tag-tema">' + item.tema + '</span>' +
            '</div>' +
            '<span class="hist-card-date">' + item.date + '</span>' +
            '</div>' +
            '<div class="hist-card-question">' + item.question + '</div>' +
            '<div class="hist-card-answer">' + item.answer + '</div>';
        results.appendChild(card);
    });
}

window.onload = function() {
    loadEmner();
};

document.querySelectorAll(".modal-overlay").forEach(function(overlay) {
    overlay.addEventListener("click", function(e) {
        if (e.target === overlay) overlay.classList.remove("active");
    });
});

// ── Init ──
renderSidebar();
renderFilterChips();
filterHistory();