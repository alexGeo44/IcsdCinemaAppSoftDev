package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ViewProgramUseCase {

    private final ProgramRepository programRepository;
    private final ScreeningRepository screeningRepository;

    public ViewProgramUseCase(ProgramRepository programRepository, ScreeningRepository screeningRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
    }


    @Transactional(readOnly = true)
    public ViewResult view(UserId actorId, ProgramId programId) {
        if (programId == null) throw new ValidationException("programId", "programId is required");

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));


        if (actorId == null) {
            if (program.state() != ProgramState.ANNOUNCED) {
                throw new AuthorizationException("Program not available");
            }
            return new ViewResult(program, false);
        }

        boolean full = canViewFull(actorId, program);


        if (!full && program.state() == ProgramState.CREATED) {
            throw new AuthorizationException("Program not available");
        }

        return new ViewResult(program, full);
    }


    public boolean canViewFull(UserId actorId, Program program) {
        if (actorId == null || program == null || program.id() == null) return false;


        if (program.creatorUserId().equals(actorId)) return true;
        if (program.isProgrammer(actorId)) return true;
        if (program.isStaff(actorId)) return true;


        return screeningRepository.existsByProgramIdAndSubmitterId(program.id(), actorId);
    }

    public record ViewResult(Program program, boolean full) {}
}
