let currentSessionId = null;
let isInitializing = true;

const MIN_SVAR_LENGDE = 10;
let isWaitingForReflection = false;
let selectedRating = 0;

requireVanlig();

const params = new URLSearchParams(window.location.search);
const isTempChat = params.get("temp") === "true";

const emneIdFromUrl = params.get("emneId");
const temaIdFromUrl = params.get("temaId");
const sessionIdFromUrl = params.get("sessionId");

const brukarnavn = localStorage.getItem("brukarnavn") || "bruker@email.com";

document.getElementById("sidebarBrukarnavn").textContent = brukarnavn;

// ── Gjenopprett pågåande session frå historikk ──
async function gjenopprettSession(sessionId) {
    try {
        const res = await api("/api/historikk/" + sessionId);
        if (!res.ok) throw new Error("Kunne ikkje hente session");
        const data = await res.json();

        currentSessionId = data.sessionId;

        // Vis det originale spørsmålet
        addUserMessage(data.startSporsmal);

        // Vis oppfølgingsspørsmålet frå KI
        if (data.oppfolgingsSporsmal) {
            addAIMessage("🤔 Oppfølgingsspørsmål:\n" + data.oppfolgingsSporsmal);
        }

        // Viss state er FOLLOW_UP ventar vi på refleksjon frå brukar
        if (data.state === "FOLLOW_UP" || data.state === "INITIAL") {
            isWaitingForReflection = true;
            addAIMessage("💬 Du har ein uferdig chat her. Skriv svaret ditt under for å halde fram.");
        }

    } catch (err) {
        console.error(err);
        addAIMessage("❌ Kunne ikkje laste tidlegare chat.");
    }
}

function selectEmneAndTema(emneId, temaId) {
    const emneSelect = document.getElementById("emneSelect");
    const temaSelect = document.getElementById("temaSelect");

    emneSelect.value = String(emneId);

    updateTemaSelect().then(function() {
        temaSelect.value = String(temaId);
        updateSelectedContext();

        if (isTempChat) {
            window.location.href = getBase() + `/sporsmal?emneId=${emneId}&temaId=${temaId}`;
            return;
        }

        updateUrlFromDropdowns();
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
        updateUrlFromDropdowns();
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
            opt.value = temaObj.temaId;
            opt.textContent = temaObj.namn;
            temaSelect.appendChild(opt);
        });
    } catch (error) {
        console.error(error);
        alert("Feil ved henting av tema");
    }

    updateSelectedContext();
    updateUrlFromDropdowns();
}

function updateSelectedContext() {
    const context = document.getElementById("selectedContext");
    if (!context) return;

    const emneSelect = document.getElementById("emneSelect");
    const temaSelect = document.getElementById("temaSelect");

    const emneTekst = emneSelect.options[emneSelect.selectedIndex]?.text || "";
    const temaTekst = temaSelect.options[temaSelect.selectedIndex]?.text || "";

    if (!emneSelect.value && !temaSelect.value) {
        context.textContent = "Velg emne og tema og still spørsmålet ditt for å komme i gang";
        return;
    }

    if (emneSelect.value && !temaSelect.value) {
        context.textContent = `Valt emne: ${emneTekst}`;
        return;
    }

    context.textContent = `Valt emne: ${emneTekst} | Tema: ${temaTekst}`;
}

function updateUrlFromDropdowns() {
    if (isInitializing) return;

    const emneId = document.getElementById("emneSelect").value;
    const temaId = document.getElementById("temaSelect").value;
    const newParams = new URLSearchParams();

    if (emneId) newParams.set("emneId", emneId);
    if (temaId) newParams.set("temaId", temaId);

    const queryString = newParams.toString();
    const newUrl = queryString
        ? `${window.location.pathname}?${queryString}`
        : window.location.pathname;

    history.replaceState({}, "", newUrl);
}

async function sendQuestion() {
    if (!isWaitingForReflection && currentSessionId === null && document.getElementById("question").disabled) {
        return;
    }

    const input = document.getElementById("question");
    const btn = document.getElementById("sendBtn");

    const question = input.value.trim();
    if (!question) return;

    if (isWaitingForReflection && question.length < MIN_SVAR_LENGDE) {
        addAIMessage(`❌ Du må skrive minst ${MIN_SVAR_LENGDE} teikn.`);
        return;
    }

    addUserMessage(question);
    input.value = "";
    input.style.height = "auto";
    btn.disabled = true;

    addThinkingMessage();

    if (isWaitingForReflection) {
        try {
            const res = await api("api/sporsmal/refleksjon", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    sessionId: currentSessionId,
                    svar: question
                })
            });

            const data = await res.json();
            removeThinkingMessage();

            if (data.vurdering) {
                addAIMessage("✅ " + data.vurdering);
            }

            if (data.rating !== null && data.rating !== undefined) {
                addAIMessageHTML("Din score: " + renderStars(data.rating));
            }

            if (data.fasit) {
                addAIMessage("📖 " + data.fasit);
            }

            showUserRatingUI();

            currentSessionId = null;
            isWaitingForReflection = false;

        } catch (err) {
            console.error("ERROR:", err);
            removeThinkingMessage();
            addAIMessage("❌ Feil oppstod.");
        }

        btn.disabled = false;
        scrollToBottom();
        return;
    }

    if (isTempChat) {
        try {
            const res = await api("api/ai/ask", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    question: question
                })
            });

            const data = await res.json();
            removeThinkingMessage();

            if (data.explanation) {
                addAIMessage(data.explanation);
            } else {
                addAIMessage("❌ Ingen svar frå AI.");
            }

        } catch (err) {
            console.error(err);
            removeThinkingMessage();
            addAIMessage("❌ Feil oppstod.");
        }

        btn.disabled = false;
        scrollToBottom();
        return;
    }

    try {
        const temaIdRaw = document.getElementById("temaSelect").value;

        if (!temaIdRaw && !isTempChat) {
            removeThinkingMessage();
            addAIMessage("⚠️ Velg eit tema først.");
            btn.disabled = false;
            return;
        }

        const temaId = parseInt(temaIdRaw);

        const res = await api("api/sporsmal/start", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                temaId: temaId,
                sporsmal: question
            })
        });

        const data = await res.json();

        currentSessionId = data.sessionId;
        isWaitingForReflection = true;

        removeThinkingMessage();

        if (data.oppfolgingsSporsmal) {
            addAIMessage("🤔 Oppfølgingsspørsmål:\n" + data.oppfolgingsSporsmal);
        } else {
            currentSessionId = null;
            isWaitingForReflection = false;
        }

    } catch (err) {
        console.error(err);
        removeThinkingMessage();
        addAIMessage("❌ Feil oppstod.");
    }

    btn.disabled = isWaitingForReflection && question.length < MIN_SVAR_LENGDE;
    scrollToBottom();
}

let messageCounter = 0;

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

function openNyChatModal() {
    window.location.href = getBase() + "/sporsmal";
}

function initTempChat() {
    const emneSelect = document.getElementById("emneSelect");
    const temaSelect = document.getElementById("temaSelect");

    if (emneSelect) emneSelect.classList.add("hidden");
    if (temaSelect) temaSelect.classList.add("hidden");

    const messages = document.getElementById("chatMessages");
    messages.innerHTML = `
        <div class="chat-welcome">
            <p class="sp-hint" style="text-align:center; font-size:18px;">
                Denne chatten er midlertidig og blir ikkje lagra
            </p>
        </div>
    `;
}

document.querySelectorAll(".modal-overlay").forEach(function(overlay) {
    overlay.addEventListener("click", function(e) {
        if (e.target === overlay) overlay.classList.remove("active");
    });
});

window.onload = async function () {
    const emneSelect = document.getElementById("emneSelect");
    const temaSelect = document.getElementById("temaSelect");

    if (emneSelect) emneSelect.classList.remove("hidden");
    if (temaSelect) temaSelect.classList.remove("hidden");

    if (isTempChat) {
        initTempChat();
    }

    await loadEmner();

    // Viss vi kjem frå historikk med ein pågåande session
    if (sessionIdFromUrl) {
        await gjenopprettSession(sessionIdFromUrl);
    }
};

window.onTemaSelected = function(emneId, temaId) {
    selectedEmneAndTema(emneId, temaId);
}

function addUserMessage(text) {
    const chat = document.getElementById("chatMessages");

    const welcome = document.querySelector(".chat-welcome");
    if (welcome) welcome.remove();

    const div = document.createElement("div");
    div.className = "chat-bubble chat-user";
    div.innerHTML = `<div class="chat-bubble-content">${escapeHtml(text)}</div>`;

    chat.appendChild(div);
    scrollToBottom();
}

function addAIMessage(text) {
    const chat = document.getElementById("chatMessages");

    const welcome = document.querySelector(".chat-welcome");
    if (welcome) welcome.remove();

    const div = document.createElement("div");
    div.className = "chat-bubble chat-ai";
    div.innerHTML = `<div class="chat-bubble-content">${escapeHtml(text)}</div>`;

    chat.appendChild(div);
    scrollToBottom();
}

function addAIMessageHTML(html) {
    const chat = document.getElementById("chatMessages");

    const welcome = document.querySelector(".chat-welcome");
    if (welcome) welcome.remove();

    const div = document.createElement("div");
    div.className = "chat-bubble chat-ai";
    div.innerHTML = `<div class="chat-bubble-content">${html}</div>`;

    chat.appendChild(div);
    scrollToBottom();
}

function scrollToBottom() {
    const chat = document.getElementById("chatMessages");
    chat.scrollTop = chat.scrollHeight;
}

function addThinkingMessage() {
    const chat = document.getElementById("chatMessages");

    const div = document.createElement("div");
    div.className = "chat-bubble chat-ai";
    div.id = "thinkingBubble";

    div.innerHTML = `
        <div class="chat-bubble-content">
            <em>⏳ AI jobbar med svaret...</em>
        </div>
    `;

    chat.appendChild(div);
    scrollToBottom();
}

function removeThinkingMessage() {
    const el = document.getElementById("thinkingBubble");
    if (el) el.remove();
}

function renderClickableStars() {
    let stars = "";
    for (let i = 1; i <= 5; i++) {
        stars += `<span class="star" data-value="${i}">☆</span>`;
    }
    return stars;
}

function renderStars(rating) {
    let stars = '<span style="display:inline-flex; gap:2px;">';
    for (let i = 1; i <= 5; i++) {
        stars += `<span style="color: ${i <= rating ? "#e8a838" : "#ccc"}; font-size:1rem;">★</span>`;
    }
    stars += '</span>';
    return stars;
}

function showUserRatingUI() {
    const chat = document.getElementById("chatMessages");

    const div = document.createElement("div");
    div.className = "chat-bubble chat-ai";

    div.innerHTML = `
        <div class="chat-bubble-content">
            <p>Kor bra var svaret frå KI?</p>
            <div class="ratingStars">
                ${renderClickableStars()}
            </div>
        </div>
    `;

    chat.appendChild(div);

    const stars = div.querySelectorAll(".star");

    stars.forEach(star => {
        star.addEventListener("mouseover", function () {
            const rating = parseInt(star.dataset.value);
            highlightStars(div, rating);
        });

        star.addEventListener("mouseout", function () {
            highlightStars(div, selectedRating);
        });

        star.addEventListener("click", function () {
            selectedRating = parseInt(star.dataset.value);
            highlightStars(div, selectedRating);
            showEndChatUI();
        });
    });
}

function highlightStars(container, rating) {
    const stars = container.querySelectorAll(".star");

    stars.forEach(star => {
        const value = parseInt(star.dataset.value);

        if (value <= rating) {
            star.textContent = "★";
            star.classList.add("filled");
        } else {
            star.textContent = "☆";
            star.classList.remove("filled");
        }
    });
}

function showEndChatUI() {
    const chat = document.getElementById("chatMessages");

    const div = document.createElement("div");
    div.className = "chat-bubble chat-ai";

    div.innerHTML = `
        <div class="chat-bubble-content">
            <p>✨ Takk for vurderinga!</p>
            <button class="sp-btn">Start ny chat</button>
        </div>
    `;

    chat.appendChild(div);

    div.querySelector("button").onclick = openNyChatModal;

    document.getElementById("question").disabled = true;
    document.getElementById("sendBtn").disabled = true;

    currentSessionId = null;
    isWaitingForReflection = false;

    scrollToBottom();
}