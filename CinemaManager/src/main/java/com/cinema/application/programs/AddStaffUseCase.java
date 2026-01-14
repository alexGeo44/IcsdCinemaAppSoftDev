package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.DuplicateException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class AddStaffUseCase {

    private final ProgramRepository programRepository;
    private final UserRepository userRepository;

    public AddStaffUseCase(ProgramRepository programRepository, UserRepository userRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
        this.userRepository = Objects.requireNonNull(userRepository);
    }


    @Transactional
    public void addStaff(UserId actorId, ProgramId programId, UserId staffId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (programId == null) throw new ValidationException("programId", "programId is required");
        if (staffId == null) throw new ValidationException("staffId", "staffId is required");

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));


        if (!programRepository.isProgrammer(programId, actorId)) {
            throw new AuthorizationException("Only PROGRAMMER of this program can add staff");
        }


        if (program.state() == ProgramState.ASSIGNMENT
                || program.state() == ProgramState.REVIEW
                || program.state() == ProgramState.SCHEDULING
                || program.state() == ProgramState.FINAL_PUBLICATION
                || program.state() == ProgramState.DECISION
                || program.state() == ProgramState.ANNOUNCED) {
            throw new ValidationException("programState", "Staff set is frozen after SUBMISSION phase");
        }


        User staffUser = userRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("User", "Staff user not found"));

        if (!staffUser.isActive()) {
            throw new ValidationException("staffId", "Staff user account is inactive");
        }


        if (program.isProgrammer(staffId)) {
            throw new ValidationException("staffId", "User is already a PROGRAMMER of this program");
        }


        if (program.isStaff(staffId)) {
            throw new DuplicateException("staff", "User is already STAFF of this program");
        }


        program.addStaff(staffId);

        programRepository.save(program);
    }
}
