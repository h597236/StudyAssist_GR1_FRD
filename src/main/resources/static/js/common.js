function getBase() {
    const path = window.location.pathname;

    // if running under /something/
    const parts = path.split('/').filter(p => p.length > 0);

    // if first part is a file (like login.html), return empty
    if (parts.length === 1 && parts[0].includes('.html')) {
        return '';
    }

    return parts.length > 0 ? '/' + parts[0] : '';
}

function api(path, options = {}) {
    const base = getBase();
    return fetch(`${base}/${path}`, options);
}

function requireLogin() {
    if (!localStorage.getItem("brukarId")) {
        window.location.href = "login.html";
    }
}

function loggUt() {
    localStorage.removeItem("brukarnavn");
    localStorage.removeItem("brukarId");
    window.location.href = "login.html";
}