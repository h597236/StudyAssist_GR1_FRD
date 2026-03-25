async function loggInn() {
    var brukarnavn = document.getElementById("brukarnavn").value.trim();
    var passord = document.getElementById("passord").value;

    if (!brukarnavn || !passord) {
        showMelding("Fyll inn brukarnamn og passord.", "red");
        return;
    }

    try {
        var response = await api("api/brukar/logginn", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: brukarnavn, passord: passord })
        });

        if (response.ok) {
            var user = await response.json();
            localStorage.setItem("brukarId", user.id);
            localStorage.setItem("brukarnavn", user.email);
            window.location.href = "/home";
        } else {
            showMelding("Feil brukarnamn eller passord.", "red");
        }
    } catch (error) {
        showMelding("Klarte ikkje kontakte serveren.", "red");
    }
}

async function registrer() {
    var brukarnavn = document.getElementById("brukarnavn").value.trim();
    var passord = document.getElementById("passord").value;

    if (!brukarnavn || !passord) {
        showMelding("Fyll inn brukarnamn og passord.", "red");
        return;
    }

    try {
        var response = await api("api/brukar/registrer", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: brukarnavn, passord: passord })
        });

        if (response.ok) {
            // ✅ YOUR CUSTOM SUCCESS MESSAGE
            showMelding("Brukar registrert! Trykk logg inn!", "green");
        } else {
            showMelding("Registrering feilet.", "red");
        }
    } catch (error) {
        showMelding("Klarte ikkje kontakte serveren.", "red");
    }
}

function showMelding(text, color) {
    var el = document.getElementById("melding");
    el.textContent = text;
    el.style.color = color;
}

document.getElementById("loginForm").addEventListener("submit", function (e) {
    e.preventDefault();
    loggInn();
});