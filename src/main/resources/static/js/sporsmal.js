requireLogin();

// ── State ──
let currentSessionId = null;
let isTemp = false;

const brukarnavn = localStorage.getItem("brukarnavn") || "bruker@email.com";
document.getElementById("sidebarBrukarnavn").textContent = brukarnavn;

// ── Init ──
window.onload = async function () {
    const params = new URLSearchParams(window.location.search);
    isTemp = params.get("temp") === "true";

    await loadEmnerIntoSidebar();
    await loadEmnerIntoDropdown();

    const emneId = params.get("emneId");
    const tema = params.get("tema");

    if (emneId) {
        document.getElementById("emneSelect").value = emneId;
        await updateTemaSelect();

        if (tema) {
            const temaSelect = document.getElementById("temaSelect");
            for (let opt of temaSelect.options) {
                if (opt.textContent === tema) {
                    temaSelect.value = opt.value;
                    break;
                }
            }
        }

        updateSelectedContext();
    }
};

// ── Load emner into dropdown ──
async function loadEmnerIntoDropdown() {
    const brukarId = localStorage.getItem("brukarId");
    const select = document.getElementById("emneSelect");
    select.innerHTML = '<option value="">Emne</option>';

    try {
        const res = await api(`emne/brukar/${brukarId}`);
        const emner = await res.json();

        emner.forEach(emne => {
            const opt = document.createElement("option");
            opt.value = emne.emneId;
            opt.textContent = emne.namn;
            select.appendChild(opt);
        });
    } catch (e) {
        console.error("Feil ved henting av emner:", e);
    }
}

// ── Update tema dropdown when emne changes ──
async function updateTemaSelect() {
    const emneId = document.getElementById("emneSelect").value;
    const select = document.getElementById("temaSelect");
    select.innerHTML = '<option value="">Tema</option>';

    if (!emneId) return;

    try {
        const res = await api(`tema/emne/${emneId}`);
        const temaListe = await res.json();

        temaListe.forEach(tema => {
            const opt = document.createElement("option");
            opt.value = tema.temaId;
            opt.textContent = tema.namn;
            select.appendChild(opt);
        });
    } catch (e) {
        console.error("Feil ved henting av tema:", e);
    }

    updateSelectedContext();
    updateUrlFromDropdowns();
}

// ── Update context hint ──
function updateSelectedContext() {
    const emneSelect = document.getElementById("emneSelect");
    const temaSelect = document.getElementById("temaSelect");
    const emneText = emneSelect.options[emneSelect.selectedIndex]?.text || "";
    const temaText = temaSelect.options[temaSelect.selectedIndex]?.text || "";

    const hint = document.getElementById("selectedContext");
    if (emneText && emneText !== "Emne" && temaText && temaText !== "Tema") {
        hint.textContent = `${emneText} → ${temaText}`;
    } else {
        hint.textContent = "Velg emne og tema og still spørsmålet ditt for å komme i gang";
    }
}

// ── Update URL ──
function updateUrlFromDropdowns() {
    const emneId = document.getElementById("emneSelect").value;
    const tema = document.getElementById("temaSelect").options[document.getElementById("temaSelect").selectedIndex]?.text;
    if (emneId) {
        const url = `/sporsmal?emneId=${emneId}${tema ? "&tema=" + encodeURIComponent(tema) : ""}`;
        history.replaceState(null, "", url);
    }
}

// ── Step 1: Send question ──
async function sendQuestion() {
    const temaId = document.getElementById("temaSelect").value;
    const sporsmal = document.getElementById("question").value.trim();

    if (!sporsmal) {
        alert("Skriv inn eit spørsmål.");
        return;
    }

    // Temp mode — no emne/tema, use old AI endpoint
    if (isTemp || !temaId) {
        await sendTempQuestion(sporsmal);
        return;
    }

    appendMessage("Du", sporsmal);
    document.getElementById("question").value = "";
    document.getElementById("sendBtn").disabled = true;

    try {
        const res = await api("api/sporsmal/start", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ temaId: parseInt(temaId), sporsmal })
        });

        const data = await res.json();
        currentSessionId = data.sessionId;

        if (data.explanation) appendMessage("StudyAssist", data.explanation);
        if (data.followUpSporsmal) {
            appendMessage("StudyAssist 🤔", "Oppfølgingsspørsmål: " + data.followUpSporsmal);
            document.getElementById("followUpContainer").style.display = "block";
        }

        // Update sporsmal count
        let count = parseInt(localStorage.getItem("sporsmalCount")) || 0;
        localStorage.setItem("sporsmalCount", count + 1);

    } catch (e) {
        appendMessage("Feil", "Noko gjekk gale. Prøv igjen.");
        console.error(e);
    }

    document.getElementById("sendBtn").disabled = false;
}

// ── Temp chat (no session) ──
async function sendTempQuestion(sporsmal) {
    appendMessage("Du", sporsmal);
    document.getElementById("question").value = "";

    try {
        const res = await api("api/ai/ask", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ question: sporsmal })
        });

        const data = await res.json();
        if (data.explanation) appendMessage("StudyAssist", data.explanation);
        if (data.follow_up_question) appendMessage("StudyAssist 🤔", data.follow_up_question);
    } catch (e) {
        appendMessage("Feil", "Noko gjekk gale.");
    }
}

// ── Step 2: Send follow-up answer ──
async function sendFollowUp() {
    const svar = document.getElementById("followUpAnswer").value.trim();
    if (!svar) {
        alert("Skriv inn svaret ditt.");
        return;
    }

    appendMessage("Du", svar);
    document.getElementById("followUpContainer").style.display = "none";

    try {
        const res = await api("api/sporsmal/refleksjon", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ sessionId: currentSessionId, svar })
        });

        const data = await res.json();
        if (data.vurdering) appendMessage("Vurdering ✅", data.vurdering);
        if (data.fasit) appendMessage("Fasit 📖", data.fasit);

        document.getElementById("feedbackContainer").style.display = "block";
    } catch (e) {
        appendMessage("Feil", "Noko gjekk gale.");
        console.error(e);
    }
}

// ── Step 3: Send feedback ──
async function sendFeedback() {
    const tekst = document.getElementById("feedbackInput").value.trim();

    try {
        await api("api/sporsmal/tilbakemelding", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ sessionId: currentSessionId, tekst })
        });

        document.getElementById("feedbackContainer").style.display = "none";
        appendMessage("StudyAssist 🎉", "Takk for tilbakemeldinga! Økta er fullført.");
        currentSessionId = null;
        document.getElementById("followUpAnswer").value = "";
        document.getElementById("feedbackInput").value = "";
    } catch (e) {
        appendMessage("Feil", "Noko gjekk gale.");
        console.error(e);
    }
}

// ── Append message to chat ──
function appendMessage(sender, text) {
    const container = document.getElementById("chatMessages");
    const msg = document.createElement("div");
    msg.className = "chat-message";
    msg.innerHTML = `<strong>${sender}:</strong> <span>${text}</span>`;
    container.appendChild(msg);
    container.scrollTop = container.scrollHeight;
}

// ── Sidebar ──
async function loadEmnerIntoSidebar() {
    const brukarId = localStorage.getItem("brukarId");
    const list = document.getElementById("sidebarEmneList");
    list.innerHTML = "";

    try {
        const res = await api(`emne/brukar/${brukarId}`);
        const emner = await res.json();

        for (const emne of emner) {
            const item = document.createElement("div");
            item.className = "sp-emne-item";

            const header = document.createElement("div");
            header.className = "sp-emne-header";
            header.innerHTML = `<span class="sp-emne-dot"></span><span class="sp-emne-name">${emne.namn}</span><span class="sp-emne-arrow">∨</span>`;
            header.onclick = async () => await toggleSidebarEmne(emne.emneId, item);

            item.appendChild(header);
            list.appendChild(item);
        }
    } catch (e) {
        console.error("Sidebar feil:", e);
    }
}

async function toggleSidebarEmne(emneId, item) {
    let topics = item.querySelector(".sp-emne-topics");
    if (topics) {
        topics.classList.toggle("open");
        return;
    }

    topics = document.createElement("div");
    topics.className = "sp-emne-topics open";

    try {
        const res = await api(`tema/emne/${emneId}`);
        const temaListe = await res.json();

        temaListe.forEach(tema => {
            const link = document.createElement("a");
            link.href = `/sporsmal?emneId=${emneId}&tema=${encodeURIComponent(tema.namn)}`;
            link.className = "sp-topic-link";
            link.textContent = tema.namn;
            topics.appendChild(link);
        });
    } catch (e) {
        console.error(e);
    }

    item.appendChild(topics);
}

// ── Modals ──
function openNyttEmneModal() {
    document.getElementById("nyttEmneModal").classList.add("active");
}
function closeNyttEmneModal() {
    document.getElementById("nyttEmneModal").classList.remove("active");
}
async function addEmne() {
    const name = document.getElementById("modalEmneName2").value.trim();
    const desc = document.getElementById("modalEmneDesc2").value.trim();
    if (!name) return;

    try {
        await api("emne", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                namn: name,
                beskrivelse: desc,
                brukar: { id: parseInt(localStorage.getItem("brukarId")) }
            })
        });
        closeNyttEmneModal();
        await loadEmnerIntoSidebar();
        await loadEmnerIntoDropdown();
        document.getElementById("modalEmneName2").value = "";
        document.getElementById("modalEmneDesc2").value = "";
    } catch (e) {
        alert("Feil ved oppretting av emne");
    }
}

function openNyChatModal() {
    window.location.href = "/sporsmal";
}

function toggleSidebar() {
    document.querySelector(".sp-sidebar").classList.toggle("open");
    document.querySelector(".sidebar-overlay").classList.toggle("open");
}

document.querySelectorAll(".modal-overlay").forEach(overlay => {
    overlay.addEventListener("click", e => {
        if (e.target === overlay) overlay.classList.remove("active");
    });
});