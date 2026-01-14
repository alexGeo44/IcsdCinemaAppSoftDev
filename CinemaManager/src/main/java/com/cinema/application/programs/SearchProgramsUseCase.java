package com.cinema.application.programs;

import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class SearchProgramsUseCase {

    private final ProgramRepository programRepository;
    private final ScreeningRepository screeningRepository;

    public SearchProgramsUseCase(ProgramRepository programRepository, ScreeningRepository screeningRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
    }


    public List<Program> search(
            UserId actorId,
            String name,
            ProgramState state,
            LocalDate from,
            LocalDate to,
            int offset,
            int limit
    ) {
        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.max(1, Math.min(limit, 200));


        int fetchOffset = 0;
        int fetchLimit = safeOffset + safeLimit + 200;

        List<Program> raw = programRepository.search(name, state, from, to, fetchOffset, fetchLimit);

        return raw.stream()
                .filter(p -> canSee(actorId, p))
                .sorted(byDateThenName())
                .skip(safeOffset)
                .limit(safeLimit)
                .toList();
    }

    private boolean canSee(UserId actorId, Program p) {

        if (actorId == null) {
            return p.state() == ProgramState.ANNOUNCED;
        }


        if (p.creatorUserId().equals(actorId)
                || programRepository.isProgrammer(p.id(), actorId)
                || programRepository.isStaff(p.id(), actorId)) {
            return true;
        }


        if (screeningRepository.existsByProgramIdAndSubmitterId(p.id(), actorId)) {
            return true;
        }


        return p.state() == ProgramState.ANNOUNCED;
    }

    private Comparator<Program> byDateThenName() {
        return Comparator
                .comparing((Program p) -> p.startDate() != null ? p.startDate() : LocalDate.MIN)
                .thenComparing(p -> p.name() != null ? p.name().toLowerCase() : "");
    }
}
