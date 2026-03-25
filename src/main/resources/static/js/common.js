function getBase() {
    const path = window.location.pathname;
    const parts = path.split('/').filter(p => p.length > 0);

    if (parts.length === 1 && parts[0].includes('.html')) {
        return '';
    }

    return parts.length > 0 ? '/' + parts[0] : '';
}

async function api(path, options = {}) {
    const base = getBase();
    const response = await fetch(`${base}/${path}`, options);

    const isAuthEndpoint = path === "api/brukar/logginn" ||
        path === "api/brukar/registrer";

    if (response.status === 401 && !isAuthEndpoint) {
        localStorage.removeItem("brukarnavn");
        localStorage.removeItem("brukarId");
        window.location.href = "login.html";
        throw new Error("Unauthorized");
    }

    return response;
}

function requireLogin() {
    if (!localStorage.getItem("brukarId")) {
        window.location.href = "login.html";
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
    window.location.href = "login.html";
}
