requireLogin();

const params = new URLSearchParams(window.location.search);
const emneIdFromUrl = params.get("emneId");
const temaFromUrl = params.get("tema");

const brukarnavn = localStorage.getItem("brukarnavn") || "bruker@email.com";
let activeEmneId = null;
let emner = [];
let temaMap = {};

document.getElementById("sidebarBrukarnavn").textContent = brukarnavn;

async function loadEmner() {
    try {
        const brukarID = localStorage.getItem("brukarId");
        const res = await api(`emne/brukar/${brukarID}`);
        if (!res.ok) throw new Error("Kunne ikkje hente emner");
        emner = await res.json();

        renderSidebar();
        renderDropdowns();

        if (emneIdFromUrl) {
            document.getElementById("emneSelect").value = emneIdFromUrl;
            await updateTemaSelect();

            if (temaFromUrl) {
                document.getElementById("temaSelect").value = temaFromUrl;
            }

            updateSelectedContext();
        }
    } catch (error) {
        console.error(error);
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
                selectEmneAndTema(emne.emneId, temaObj.namn);
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

function selectEmneAndTema(emneId, temaNamn) {
    const emneSelect = document.getElementById("emneSelect");
    const temaSelect = document.getElementById("temaSelect");

    emneSelect.value = emneId;
    updateTemaSelect().then(function() {
        temaSelect.value = temaNamn;
        updateSelectedContext();
    });
}

function renderDropdowns() {
    const emneSelect = document.getElementById("emneSelect");
    emneSelect.innerHTML = '<option value="">Emne</option>';

    emner.forEach(function(emne) {
        const opt = document.createElement("option");
        opt.value = emne.emneId;
        opt.textContent = emne.namn;
        emneSelect.appendChild(opt);
    });
}

async function updateTemaSelect() {
    const emneSelect = document.getElementById("emneSelect");
    const temaSelect = document.getElementById("temaSelect");
    const emneId = emneSelect.value;

    temaSelect.innerHTML = '<option value="">Tema</option>';

    if (!emneId) {
        updateSelectedContext();
        return;
    }

    try {
        if (!temaMap[emneId]) {
            const res = await api(`tema/emne/${emneId}`);
            if (!res.ok) throw new Error("Kunne ikkje hente tema");
            temaMap[emneId] = await res.json();
        }

        temaMap[emneId].forEach(function(temaObj) {
            const opt = document.createElement("option");
            opt.value = temaObj.namn;
            opt.textContent = temaObj.namn;
            temaSelect.appendChild(opt);
        });
    } catch (error) {
        console.error(error);
        alert("Feil ved henting av tema");
    }

    updateSelectedContext();
}

function updateSelectedContext() {
    const context = document.getElementById("selectedContext");
    context.textContent = "Velg emne og tema og still spørsmålet ditt for å komme i gang";
}

async function sendQuestion() {
    const emneId = document.getElementById("emneSelect").value;
    const tema = document.getElementById("temaSelect").value;
    const question = document.getElementById("question").value.trim();

    if (!question) return;

    const valgtEmne = emner.find(e => String(e.emneId) === String(emneId));
    const subject = valgtEmne ? valgtEmne.namn : "Generelt";
    const topic = tema || "Fritt spørsmål";

    addMessage("user", question);
    document.getElementById("question").value = "";
    autoResizeTextarea();

    const loadingId = addMessage("ai", "Tenkjer...");

    try {
        const response = await api("api/ai/ask", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                subject: subject,
                topic: topic,
                question: question
            })
        });

        const data = await response.json();

        let aiText = "";
        if (data.explanation) {
            aiText += data.explanation;
        }
        if (data.follow_up_question) {
            aiText += "\n\n💡 " + data.follow_up_question;
        }

        updateMessage(loadingId, aiText || "Ingen forklaring mottatt.");

        const count = parseInt(localStorage.getItem("sporsmalCount")) || 0;
        localStorage.setItem("sporsmalCount", count + 1);

    } catch (error) {
        updateMessage(loadingId, "Feil ved kall til backend. Sjekk at serveren køyrer.");
        console.error(error);
    }
}

let messageCounter = 0;

function addMessage(type, text) {
    const messages = document.getElementById("chatMessages");
    const welcome = messages.querySelector(".chat-welcome");
    if (welcome) welcome.style.display = "none";

    const id = "msg-" + (++messageCounter);
    const bubble = document.createElement("div");
    bubble.className = "chat-bubble chat-" + type;
    bubble.id = id;
    bubble.innerHTML = '<div class="chat-bubble-content">' + escapeHtml(text) + '</div>';
    messages.appendChild(bubble);
    messages.scrollTop = messages.scrollHeight;
    return id;
}

function updateMessage(id, text) {
    const el = document.getElementById(id);
    if (el) {
        el.querySelector(".chat-bubble-content").innerHTML = escapeHtml(text).replace(/\n/g, "<br>");
    }
    const messages = document.getElementById("chatMessages");
    messages.scrollTop = messages.scrollHeight;
}

function escapeHtml(str) {
    const div = document.createElement("div");
    div.textContent = str;
    return div.innerHTML;
}

const textarea = document.getElementById("question");
textarea.addEventListener("input", autoResizeTextarea);
textarea.addEventListener("keydown", function(e) {
    if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        sendQuestion();
    }
});

function autoResizeTextarea() {
    textarea.style.height = "auto";
    textarea.style.height = Math.min(textarea.scrollHeight, 150) + "px";
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

        document.getElementById("modalTemaName").value = "";
        closeNyttTemaModal();

        // 🔥 ONLY reload THIS emne
        temaMap[activeEmneId] = null;

        const res = await api(`tema/emne/${activeEmneId}`);
        temaMap[activeEmneId] = await res.json();

        renderSidebar(); // redraw WITHOUT closing everything

        // 🔥 reopen the emne manually
        const topics = document.getElementById("topics-" + activeEmneId);
        const arrow = document.getElementById("arrow-" + activeEmneId);

        if (topics) {
            topics.classList.add("open");
            arrow.textContent = "∧";
        }

        // update dropdown
        document.getElementById("emneSelect").value = activeEmneId;
        await updateTemaSelect();
        document.getElementById("temaSelect").value = name;
        updateSelectedContext();

    } catch (error) {
        console.error(error);
        alert("Feil ved lagring av tema");
    }
}

function openNyChatModal() {
    document.getElementById("emneSelect").value = "";
    document.getElementById("temaSelect").innerHTML = '<option value="">Tema</option>';
    document.getElementById("question").value = "";
    updateSelectedContext();

    const messages = document.getElementById("chatMessages");
    messages.innerHTML = '<div class="chat-welcome"><p class="sp-hint" id="selectedContext">Velg emne og tema og still spørsmålet ditt for å komme i gang</p></div>';
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

window.onload = loadEmner;