requireLogin();

// ── Data ──
let sporsmalCount = parseInt(localStorage.getItem("sporsmalCount")) || 0;
let selectedSubjectIndex = null;

// ── Display username ──
const brukarnavn = localStorage.getItem("brukarnavn") || "bruker";
document.getElementById("navBrukarnavn").textContent = brukarnavn;

// ── Stats ──
async function loadStats() {
    try {
        const brukarId = localStorage.getItem("brukarId");

        const res = await api(`emne/brukar/${brukarId}`);
        const emner = await res.json();

        document.getElementById("statEmne").textContent = emner.length;

        let totalTema = 0;

        for (const emne of emner) {
            const temaRes = await api(`tema/emne/${emne.emneId}`);
            const temaListe = await temaRes.json();
            totalTema += temaListe.length;
        }

        document.getElementById("statTema").textContent = totalTema;

        // keep this as is
        document.getElementById("statSporsmal").textContent = sporsmalCount;

    } catch (error) {
        console.error("Stats error:", error);
    }
}

// ── Add Subject ──
async function addSubject() {
    const emneNavn = document.getElementById("modalEmneName").value;
    const emneDesc = document.getElementById("modalEmneDesc").value;

    if (!emneNavn) {
        alert("Skriv inn emnenamn");
        return;
    }

    try {
        const response = await api("emne", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                namn: emneNavn,
                beskrivelse: emneDesc,
                brukar: {
                    id: parseInt(localStorage.getItem("brukarId"))
                }
            })
        });

        const emne = await response.json();

        // 👇 store selected emne globally for later use
        window.selectedEmneId = emne.emneId;

        closeNewEmneModal();
        await loadEmner();
        await loadStats();
        // open tema modal after creating emne
        openTemaModal(emne.emneID);

    } catch (error) {
        console.error(error);
        alert("Feil ved oppretting av emne");
    }
}

// ── Delete Subject ──
function deleteSubject(index) {
    var name = subjects[index].name;
    if (confirm('Er du sikker på at du vil slette "' + name + '"?')) {
        subjects.splice(index, 1);
        saveSubjects();
        renderEmneCards();
    }
}

// ── Modals ──
function openNewEmneModal() {
    document.getElementById("newEmneModal").classList.add("active");
}

function closeNewEmneModal() {
    document.getElementById("newEmneModal").classList.remove("active");
}

async function openTemaModal(emneId) {
    window.selectedEmneId = emneId;

    document.getElementById("temaModalTitle").textContent = "Vel tema";

    const select = document.getElementById("temaSelect");
    select.innerHTML = "";

    try {
        const res = await api(`tema/emne/${emneId}`);
        const temaListe = await res.json();

        if (temaListe.length > 0) {
            temaListe.forEach(tema => {
                const opt = document.createElement("option");
                opt.value = tema.namn;
                opt.textContent = tema.namn;
                select.appendChild(opt);
            });
        } else {
            const opt = document.createElement("option");
            opt.value = "";
            opt.textContent = "Ingen tema enno – legg til eit nedanfor";
            select.appendChild(opt);
        }

    } catch (error) {
        console.error(error);
        alert("Feil ved henting av tema");
    }

    document.getElementById("temaModal").classList.add("active");
}

function closeTemaModal() {
    document.getElementById("temaModal").classList.remove("active");
    document.getElementById("newTemaInput").value = "";
}

async function addTemaFromModal() {
    const temaNavn = document.getElementById("newTemaInput").value;

    if (!temaNavn) {
        alert("Skriv inn tema");
        return;
    }

    if (!window.selectedEmneId) {
        alert("Ingen emne valgt");
        return;
    }

    try {
        const response = await api("tema", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                namn: temaNavn,
                emne: {
                    emneId: window.selectedEmneId
                }
            })
        });

        const tema = await response.json();

        console.log("Tema lagret:", tema);

        await openTemaModal(window.selectedEmneId);

        // optional: clear input
        document.getElementById("newTemaInput").value = "";

    } catch (error) {
        console.error(error);
        alert("Feil ved lagring av tema");
    }
}

async function loadEmner() {
    const brukarID = localStorage.getItem("brukarId");

    const res = await api(`emne/brukar/${brukarID}`);

    if (!res.ok) {
        const text = await res.text();
        console.error("Backend error:", text);
        throw new Error("Failed to load emner");
    }

    const data = await res.json();
    const emner = Array.isArray(data) ? data : [data];

    const grid = document.getElementById("emneGrid");
    const empty = document.getElementById("emptyState");

    grid.innerHTML = "";

    if (emner.length === 0) {
        empty.style.display = "block";
        return;
    }

    empty.style.display = "none";

    emner.forEach(emne => {
        const card = document.createElement("div");
        card.className = "emne-card";

        card.innerHTML =
            '<div class="emne-card-top">' +
            '<div class="emne-icon"></div>' +
            '</div>' +
            '<div class="emne-card-body">' +
            '<h3>' + emne.namn + '</h3>' +
            '<p>' + (emne.beskrivelse || 'Ingen skildring.') + '</p>' +
            '</div>';

        // 👇 IMPORTANT: store selected emneId
        card.onclick = function() {
            window.selectedEmneId = emne.emneId;
            openTemaModal(emne.emneId);
        };

        grid.appendChild(card);
    });
}

function goToQuestionPage() {
    const tema = document.getElementById("temaSelect").value;

    if (!window.selectedEmneId) {
        alert("Ingen emne valgt");
        return;
    }

    if (!tema) {
        alert("Vel eit tema");
        return;
    }

    window.location.href =
        "sporsmal.html?emneId=" + encodeURIComponent(window.selectedEmneId) +
        "&tema=" + encodeURIComponent(tema);
}

function goToTempChat() {
    window.location.href = "sporsmal.html?temp=true";
}

function loggUt() {
    localStorage.removeItem("brukarnavn");
    localStorage.removeItem("brukarId");
    window.location.href = "login.html";
}

// Close modals on overlay click
document.querySelectorAll(".modal-overlay").forEach(function(overlay) {
    overlay.addEventListener("click", function(e) {
        if (e.target === overlay) {
            overlay.classList.remove("active");
        }
    });
});

// ── Init ──
window.onload = async function() {
    await loadEmner();
    await loadStats();
};

window.addEventListener("pageshow", async function(event) {
    await loadEmner();
    await loadStats();
});