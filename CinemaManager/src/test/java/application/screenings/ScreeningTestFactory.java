package application.screenings;

import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ScreeningState;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

final class ScreeningTestFactory {

    private ScreeningTestFactory() {}

    static Screening rehydrate(
            ScreeningId sid,
            ProgramId pid,
            UserId submitter,
            ScreeningState state,
            UserId staffMemberOrNull
    ) {
        try {
            Method m = Arrays.stream(Screening.class.getDeclaredMethods())
                    .filter(mm -> Modifier.isStatic(mm.getModifiers()))
                    .filter(mm -> mm.getName().equals("rehydrate"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Screening.rehydrate(...) not found"));

            m.setAccessible(true);

            Class<?>[] types = m.getParameterTypes();
            Object[] args = new Object[types.length];

            int userIdCount = 0;
            int stringCount = 0;

            for (int i = 0; i < types.length; i++) {
                Class<?> t = types[i];

                if (t.equals(ScreeningId.class)) {
                    args[i] = sid;
                } else if (t.equals(ProgramId.class)) {
                    args[i] = pid;
                } else if (t.equals(UserId.class)) {
                    userIdCount++;

                    if (userIdCount == 1) args[i] = submitter;
                    else if (userIdCount == 2) args[i] = staffMemberOrNull;
                    else args[i] = null;
                } else if (t.equals(ScreeningState.class)) {
                    args[i] = state;
                } else if (t.equals(String.class)) {

                    stringCount++;
                    if (stringCount == 1) args[i] = "Film";
                    else if (stringCount == 2) args[i] = "Genre";
                    else if (stringCount == 3) args[i] = "Desc";
                    else if (stringCount == 4) args[i] = null;
                    else args[i] = null;
                } else if (t.equals(LocalDate.class)) {
                    args[i] = null;
                } else if (t.equals(LocalDateTime.class)) {
                    args[i] = null;
                } else if (t.equals(int.class) || t.equals(Integer.class)) {
                    args[i] = 0;
                } else if (t.equals(boolean.class) || t.equals(Boolean.class)) {
                    args[i] = false;
                } else if (t.isEnum()) {

                    args[i] = null;
                } else {

                    args[i] = null;
                }
            }

            return (Screening) m.invoke(null, args);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to create Screening via reflection rehydrate", ex);
        }
    }
}
