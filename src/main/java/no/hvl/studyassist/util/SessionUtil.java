package no.hvl.studyassist.util;

import jakarta.servlet.http.HttpSession;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.BrukarService;

public final class SessionUtil {

    private SessionUtil() {
    }

    public static Brukar getLoggedInBrukar(HttpSession session, BrukarService brukarService) {
        if (session == null) {
            return null;
        }

        Object brukarIdObj = session.getAttribute("brukarId");
        if (brukarIdObj == null) {
            return null;
        }

        Integer brukarId;
        if (brukarIdObj instanceof Integer id) {
            brukarId = id;
        } else {
            try {
                brukarId = Integer.parseInt(brukarIdObj.toString());
            } catch (NumberFormatException e) {
                session.invalidate();
                return null;
            }
        }

        Brukar brukar = brukarService.findById(brukarId);
        if (brukar == null) {
            session.invalidate();
            return null;
        }

        return brukar;
    }
}
