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
