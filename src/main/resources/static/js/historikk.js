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
            window.location.href = "sporsmal.html?subject=" + encodeURIComponent(item.emne) + "&topic=" + encodeURIComponent(item.tema);
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

// ── Sidebar ──
function renderSidebar() {
    var list = document.getElementById("sidebarEmneList");
    list.innerHTML = "";

    subjects.forEach(function(subject, index) {
        var item = document.createElement("div");
        item.className = "sp-emne-item";

        var header = document.createElement("div");
        header.className = "sp-emne-header";
        header.onclick = function() { toggleEmne(index); };

        header.innerHTML =
            '<span class="sp-emne-dot"></span>' +
            '<span class="sp-emne-name">' + subject.name + '</span>' +
            '<span class="sp-emne-arrow" id="arrow-' + index + '">∨</span>';
        item.appendChild(header);

        var topics = document.createElement("div");
        topics.className = "sp-emne-topics";
        topics.id = "topics-" + index;

        if (subject.topics && subject.topics.length > 0) {
            subject.topics.forEach(function(topic) {
                var link = document.createElement("a");
                link.href = "sporsmal.html?subject=" + encodeURIComponent(subject.name) + "&topic=" + encodeURIComponent(topic);
                link.className = "sp-topic-link";
                link.textContent = topic;
                topics.appendChild(link);
            });
        }

        item.appendChild(topics);
        list.appendChild(item);
    });
}

function toggleEmne(index) {
    var topics = document.getElementById("topics-" + index);
    var arrow = document.getElementById("arrow-" + index);
    if (topics.classList.contains("open")) {
        topics.classList.remove("open");
        arrow.textContent = "∨";
    } else {
        topics.classList.add("open");
        arrow.textContent = "∧";
    }
}

// ── Modals ──
function openNyttEmneModal() {
    document.getElementById("nyttEmneModal").classList.add("active");
}
function closeNyttEmneModal() {
    document.getElementById("nyttEmneModal").classList.remove("active");
}
function addEmne() {
    var name = document.getElementById("modalEmneName2").value.trim();
    var desc = document.getElementById("modalEmneDesc2").value.trim();
    if (!name) return;
    subjects.push({ name: name, description: desc, topics: [] });
    saveSubjects();
    renderSidebar();
    closeNyttEmneModal();
    document.getElementById("modalEmneName2").value = "";
    document.getElementById("modalEmneDesc2").value = "";
}

function toggleSidebar() {
    document.querySelector(".sp-sidebar").classList.toggle("open");
    document.querySelector(".sidebar-overlay").classList.toggle("open");
}


document.querySelectorAll(".modal-overlay").forEach(function(overlay) {
    overlay.addEventListener("click", function(e) {
        if (e.target === overlay) overlay.classList.remove("active");
    });
});

// ── Init ──
renderSidebar();
renderFilterChips();
filterHistory();