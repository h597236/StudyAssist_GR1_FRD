let emner = [];
let temaMap = {};
let activeEmneId = null;

function getBase() {
    const path = window.location.pathname;

    // f.eks:
    // /studyassist-gr1-frd/login  -> ["", "studyassist-gr1-frd", "login"]
    // /login                     -> ["", "login"]

    const parts = path.split("/");

    // Hvis vi er lokalt (ingen context path)
    if (parts.length <= 2) {
        return "";
    }

    // Hvis vi er på TomEE (har context path)
    return "/" + parts[1];
}

async function api(path, options = {}) {
    const base = getBase();

    // FIX: sørg for at path ikkje blir relative
    if (!path.startsWith("/")) {
        path = "/" + path;
    }

    const response = await fetch(`${base}${path}`, options);

    const isAuthEndpoint = path === "/api/brukar/logginn" ||
        path === "/api/brukar/registrer";

    if (response.status === 401 && !isAuthEndpoint) {
        localStorage.removeItem("brukarnavn");
        localStorage.removeItem("brukarId");
        window.location.href = "/login";
        throw new Error("Unauthorized");
    }

    return response;
}

function requireLogin() {
    if (!localStorage.getItem("brukarId")) {
        window.location.href = "/login";
    }
}

async function loggUt() {
    try {
        await api("api/brukar/loggut", {
            method: "POST"
        });
    } catch (error) {
    }

    localStorage.removeItem("brukarnavn");
    localStorage.removeItem("brukarId");
    window.location.href = "/login";
}

async function loadEmner() {
    try {
        const brukarID = localStorage.getItem("brukarId");
        const res = await api(`emne/brukar/${brukarID}`);
        if (!res.ok) throw new Error("Kunne ikkje hente emner");
        emner = await res.json();

        await loadAllTema();
        renderSidebar();
        renderDropdowns();

        if (emneIdFromUrl) {
            document.getElementById("emneSelect").value = emneIdFromUrl;
            await updateTemaSelect();

            if (temaIdFromUrl) {
                const temaSelect = document.getElementById("temaSelect");
                const temaListe = temaMap[emneIdFromUrl] || [];
                const match = temaListe.find(t => String(t.temaId) === String(temaIdFromUrl));

                if (match) {
                    temaSelect.value = String(match.temaId);
                }
            }

            updateSelectedContext();
        }

        isInitializing = false;
        updateUrlFromDropdowns();

    } catch (error) {
        console.error(error);
        isInitializing = false;
    }
}

function renderSidebar() {
    const list = document.getElementById("sidebarEmneList");
    list.innerHTML = "";

    emner.forEach(function(emne) {
        const item = document.createElement("div");
        item.className = "sp-emne-item";

        const header = document.createElement("div");
        header.className = "sp-emne-header";
        header.onclick = function() {
            toggleEmne(emne.emneId);
        };

        const dot = document.createElement("span");
        dot.className = "sp-emne-dot";

        const name = document.createElement("span");
        name.className = "sp-emne-name";
        name.textContent = emne.namn;

        const arrow = document.createElement("span");
        arrow.className = "sp-emne-arrow";
        arrow.id = "arrow-" + emne.emneId;
        arrow.textContent = "∨";

        header.appendChild(dot);
        header.appendChild(name);
        header.appendChild(arrow);
        item.appendChild(header);

        const topics = document.createElement("div");
        topics.className = "sp-emne-topics";
        topics.id = "topics-" + emne.emneId;

        const temaListe = temaMap[emne.emneId] || [];
        temaListe.forEach(function(temaObj) {
            const link = document.createElement("a");
            link.href = "#";
            link.className = "sp-topic-link";
            link.textContent = temaObj.namn;
            link.onclick = function(e) {
                e.preventDefault();
                window.location.href = `/sporsmal?emneId=${emne.emneId}&temaId=${temaObj.temaId}`;
            };
            topics.appendChild(link);
        });

        const addBtn = document.createElement("a");
        addBtn.href = "#";
        addBtn.className = "sp-topic-link sp-topic-add";
        addBtn.innerHTML = "<span class='sp-sidebar-icon'>＋</span> Nytt tema";
        addBtn.onclick = function(e) {
            e.preventDefault();
            openNyttTemaModal(emne.emneId, emne.namn);
        };
        topics.appendChild(addBtn);

        item.appendChild(topics);
        list.appendChild(item);
    });
}

async function toggleEmne(emneId) {
    const topics = document.getElementById("topics-" + emneId);
    const arrow = document.getElementById("arrow-" + emneId);

    if (!temaMap[emneId]) {
        try {
            const res = await api(`tema/emne/${emneId}`);
            if (!res.ok) throw new Error("Kunne ikkje hente tema");
            temaMap[emneId] = await res.json();
            renderSidebar();
        } catch (error) {
            console.error(error);
            alert("Feil ved henting av tema");
            return;
        }
    }

    const updatedTopics = document.getElementById("topics-" + emneId);
    const updatedArrow = document.getElementById("arrow-" + emneId);

    if (updatedTopics.classList.contains("open")) {
        updatedTopics.classList.remove("open");
        updatedArrow.textContent = "∨";
    } else {
        updatedTopics.classList.add("open");
        updatedArrow.textContent = "∧";
    }
}

async function loadAllTema() {
    for (const emne of emner) {
        try {
            const res = await api(`tema/emne/${emne.emneId}`);
            if (res.ok) {
                temaMap[emne.emneId] = await res.json();
            } else {
                temaMap[emne.emneId] = [];
            }
        } catch (e) {
            console.error(e);
            temaMap[emne.emneId] = [];
        }
    }
}

function openNyttEmneModal() {
    document.getElementById("nyttEmneModal").classList.add("active");
}

function closeNyttEmneModal() {
    document.getElementById("nyttEmneModal").classList.remove("active");
}

async function addEmne() {
    const brukarId = localStorage.getItem("brukarId");
    const name = document.getElementById("modalEmneName2").value.trim();
    const desc = document.getElementById("modalEmneDesc2").value.trim();
    if (!name) return;

    try {
        const response = await api("emne", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                namn: name,
                beskrivelse: desc,
                brukar: {
                    id: brukarId
                }
            })
        });

        if (!response.ok) {
            const msg = await response.text();
            throw new Error(msg || "Feil ved lagring av emne");
        }

        const nyttEmne = await response.json();
        document.getElementById("modalEmneName2").value = "";
        document.getElementById("modalEmneDesc2").value = "";
        closeNyttEmneModal();

        await loadEmner();
        document.getElementById("emneSelect").value = nyttEmne.emneId;
        await updateTemaSelect();
        updateSelectedContext();
        updateUrlFromDropdowns();
    } catch (error) {
        console.error(error);
        alert("Feil ved lagring av emne");
    }
}

function openNyttTemaModal(emneId, emneNamn) {
    activeEmneId = emneId;
    document.getElementById("nyttTemaTitle").textContent = emneNamn + " – Nytt tema";
    document.getElementById("nyttTemaModal").classList.add("active");
}

function closeNyttTemaModal() {
    document.getElementById("nyttTemaModal").classList.remove("active");
    document.getElementById("modalTemaName").value = "";
}

async function addTema() {
    const name = document.getElementById("modalTemaName").value.trim();
    if (!name || activeEmneId === null) return;

    try {
        const response = await api("tema", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                namn: name,
                emne: {
                    emneId: activeEmneId
                }
            })
        });

        if (!response.ok) {
            const msg = await response.text();
            throw new Error(msg || "Feil ved lagring av tema");
        }

        const nyttTema = await response.json();

        document.getElementById("modalTemaName").value = "";
        closeNyttTemaModal();

        temaMap[activeEmneId] = null;

        const res = await api(`tema/emne/${activeEmneId}`);
        temaMap[activeEmneId] = await res.json();

        renderSidebar();

        const topics = document.getElementById("topics-" + activeEmneId);
        const arrow = document.getElementById("arrow-" + activeEmneId);

        if (topics) {
            topics.classList.add("open");
            arrow.textContent = "∧";
        }

        document.getElementById("emneSelect").value = String(activeEmneId);
        await updateTemaSelect();
        document.getElementById("temaSelect").value = String(nyttTema.temaId);

        updateSelectedContext();
        updateUrlFromDropdowns();

    } catch (error) {
        console.error(error);
        alert("Feil ved lagring av tema");
    }
}

function toggleSidebar() {
    document.querySelector(".sp-sidebar").classList.toggle("open");
    document.querySelector(".sidebar-overlay").classList.toggle("open");
}


