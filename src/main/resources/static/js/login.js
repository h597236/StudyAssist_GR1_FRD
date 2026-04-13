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
            localStorage.setItem("rolle", user.rolle || "VANLIG");

            if (user.rolle && user.rolle.toUpperCase() === "ADMIN") {
                window.location.href = getBase() + "/admin";
            } else {
                window.location.href = getBase() + "/home";
            }
        } else {
            showMelding("Feil brukarnamn eller passord.", "red");
        }
    } catch (error) {
        showMelding("Klarte ikkje kontakte serveren.", "red");
    }
}

function showRegister() {
    document.getElementById("loginView").style.display = "none";
    document.getElementById("registerView").style.display = "block";
    document.getElementById("melding").textContent = "";
}

function showLogin() {
    document.getElementById("registerView").style.display = "none";
    document.getElementById("loginView").style.display = "block";
    document.getElementById("melding").textContent = "";
}

async function registrer() {
    var email = document.getElementById("regEmail").value.trim();
    var passord = document.getElementById("regPassord").value;
    var passordRepeat = document.getElementById("regPassordRepeat").value;

    if (!email || !passord) {
        showMelding("Fyll inn alle felt.", "red");
        return;
    }

    if (passord !== passordRepeat) {
        showMelding("Passorda er ikkje like.", "red");
        return;
    }

    try {
        var response = await api("api/brukar/registrer", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: email, passord: passord })
        });

        if (response.ok) {
            showMelding("Brukar registrert!", "green");
            setTimeout(function() { showLogin(); }, 1500);
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

function togglePassword(inputId, btn) {
    var input = document.getElementById(inputId);
    if (input.type === "password") {
        input.type = "text";
        btn.textContent = "SKJUL";
    } else {
        input.type = "password";
        btn.textContent = "VIS";
    }
}