requireVanlig();

// ── Display username ──
const brukarnavn = localStorage.getItem("brukarnavn") || "bruker";
document.getElementById("navBrukarnavn").textContent = brukarnavn;

// ── Stats ──
async function loadStats() {
    try {
        const brukarId = localStorage.getItem("brukarId");

        const emneRes = await api(`emne/brukar/${brukarId}`);
        const emner = await emneRes.json();
        const antallEmne = emner.length;

        let totalTema = 0;
        for (const emne of emner) {
            const temaRes = await api(`tema/emne/${emne.emneId}`);
            const temaListe = await temaRes.json();
            totalTema += temaListe.length;
        }

        const historikkRes = await api("api/historikk");
        const historikk = await historikkRes.json();

        const totaltSporsmal = historikk.length;
        const fullforte = historikk.filter(h =>
            h.state === "COMPLETED" || h.state === "FINAL_ANSWER"
        ).length;

        const enUkeSidan = new Date();
        enUkeSidan.setDate(enUkeSidan.getDate() - 7);
        const denneVeka = historikk.filter(h =>
            new Date(h.opprettaTid) > enUkeSidan
        ).length;

        // Rekn ut streak — antal dagar på rad med minst ein session
        const streak = regnUtStreak(historikk);

        // Oppdater stat-kort
        document.getElementById("statEmne").textContent = antallEmne;
        document.getElementById("statEmneLabel").textContent = "aktive fag";

        document.getElementById("statTema").textContent = totalTema;
        document.getElementById("statTemaLabel").innerHTML =
            `påbegynt <span class="stat-sublabel-secondary">${fullforte} av ${totaltSporsmal} fullført</span>`;

        document.getElementById("statSporsmal").textContent = totaltSporsmal;
        document.getElementById("statSporsmalLabel").innerHTML =
            `denne veka <span class="stat-sublabel-secondary">snitt ${totaltSporsmal > 0 ? (totaltSporsmal / 7).toFixed(1) : "0"}/dag</span>`;

        // Oppdater velkomst-ingress
        const ingress = document.getElementById("welcomeIngress");
        if (ingress) {
            if (antallEmne === 0) {
                ingress.textContent = "Kom i gang ved å opprette ditt første emne!";
            } else {
                ingress.innerHTML = `${antallEmne} ${antallEmne === 1 ? "emne er aktivt" : "emne er aktive"}. Fortsett der du slapp, eller still eit nytt<br>spørsmål - StudyAssist hugsar kor vi var.`;
            }
        }

        // Vis streak viss > 0
        if (streak > 0) {
            document.getElementById("welcomeStreak").style.display = "flex";
            document.getElementById("streakCircle").textContent = streak;
            document.getElementById("streakDays").textContent = `${streak} ${streak === 1 ? "dag" : "dagar"} på rad`;
        }

    } catch (error) {
        console.error("Stats error:", error);
    }
}

function regnUtStreak(historikk) {
    if (!historikk.length) return 0;

    const datoar = [...new Set(historikk.map(h => {
        const d = new Date(h.opprettaTid);
        return `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`;
    }))].sort().reverse();

    let streak = 0;
    const idag = new Date();

    for (let i = 0; i < datoar.length; i++) {
        const forventar = new Date(idag);
        forventar.setDate(idag.getDate() - i);
        const forventarStr = `${forventar.getFullYear()}-${forventar.getMonth()}-${forventar.getDate()}`;
        if (datoar[i] === forventarStr) {
            streak++;
        } else {
            break;
        }
    }
    return streak;
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
        await loadEmnerIndex();
        await loadStats();
        document.getElementById("modalEmneName").value = "";
        document.getElementById("modalEmneDesc").value = "";
        // open tema modal after creating emne
        openTemaModal(emne.emneId);

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
                opt.value = tema.temaId;
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

async function loadEmnerIndex() {
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
            //'<div class="emne-icon"></div>' +
            '<button class="emne-delete-btn" title="Slett emne">&times;</button>' +
            '</div>' +
            '<div class="emne-card-body">' +
            '<h3>' + emne.namn + '</h3>' +
            '<p>' + (emne.beskrivelse || 'Ingen skildring.') + '</p>' +
            '</div>';

        // Delete button – use mousedown to fire before card click
        var deleteBtn = card.querySelector(".emne-delete-btn");
        deleteBtn.addEventListener("mousedown", function(e) {
            e.stopPropagation();
            e.stopImmediatePropagation();
            e.preventDefault();
            openDeleteModal(emne.emneId, emne.namn);
        });

        // Card click – open tema modal
        card.addEventListener("click", function(e) {
            if (e.target.closest(".emne-delete-btn")) return;
            window.selectedEmneId = emne.emneId;
            openTemaModal(emne.emneId);
        });

        grid.appendChild(card);
    });
}

function goToQuestionPage() {
    const temaSelect = document.getElementById("temaSelect");

    const tema = temaSelect.value;
    const emneId = window.selectedEmneId; // denne har du allerede satt når du åpner modal

    if (!emneId) {
        alert("Vel eit emne først");
        return;
    }

    let url = getBase() + `/sporsmal?emneId=${emneId}`;

    if (tema) {
        url += `&temaId=${tema}`;
    }

    window.location.href = url;
}

function goToTempChat() {
    window.location.href = getBase() + "/sporsmal?temp=true";
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
    window.location.href = getBase() + "/login";
}

// Close modals on overlay click
document.querySelectorAll(".modal-overlay").forEach(function(overlay) {
    overlay.addEventListener("click", function(e) {
        if (e.target === overlay) {
            overlay.classList.remove("active");
        }
    });
});

// ── Delete Emne (with confirmation modal) ──
let pendingDeleteEmneId = null;

function openDeleteModal(emneId, emneNavn) {
    pendingDeleteEmneId = emneId;
    document.getElementById("deleteEmneName").textContent = emneNavn;
    document.getElementById("deleteEmneModal").classList.add("active");
}

function closeDeleteModal() {
    pendingDeleteEmneId = null;
    document.getElementById("deleteEmneModal").classList.remove("active");
}

async function confirmDeleteEmne() {
    if (!pendingDeleteEmneId) return;

    try {
        const res = await api("emne/" + pendingDeleteEmneId, {
            method: "DELETE"
        });

        if (!res.ok) {
            throw new Error("Sletting feila");
        }

        closeDeleteModal();
        await loadEmnerIndex();
        await loadStats();

    } catch (error) {
        console.error("Delete error:", error);
        alert("Kunne ikkje slette emnet. Prøv igjen.");
    }
}

// ── Init ──
window.onload = async function() {
    await loadEmnerIndex();
    await loadStats();
};

window.addEventListener("pageshow", async function(event) {
    await loadEmnerIndex();
    await loadStats();
});


// ── Delete Tema (with confirmation modal) ──
let pendingDeleteTemaId = null;

function openDeleteTemaModal() {
    const select = document.getElementById("temaSelect");
    const selectedOption = select.options[select.selectedIndex];

    if (!selectedOption || !selectedOption.value) {
        alert("Vel eit tema å slette først.");
        return;
    }

    pendingDeleteTemaId = selectedOption.value;
    document.getElementById("deleteTemaName").textContent = selectedOption.textContent;
    document.getElementById("deleteTemaModal").classList.add("active");
}

function closeDeleteTemaModal() {
    pendingDeleteTemaId = null;
    document.getElementById("deleteTemaModal").classList.remove("active");
}

async function confirmDeleteTema() {
    if (!pendingDeleteTemaId) return;

    try {
        const res = await api("tema/" + pendingDeleteTemaId, {
            method: "DELETE"
        });

        if (!res.ok) {
            throw new Error("Sletting feila");
        }

        closeDeleteTemaModal();
        await openTemaModal(window.selectedEmneId);
        await loadStats();

    } catch (error) {
        console.error("Delete tema error:", error);
        alert("Kunne ikkje slette temaet. Prøv igjen.");
    }
}