package org.example.expert.domain.manager.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.manager.entity.Log;
import org.example.expert.domain.manager.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class LogService {

    private final LogRepository logRepository;

    public void success() {
        logRepository.save(Log.success());
    }

    public void fail(String errorMessage) {
        logRepository.save((Log.fail(errorMessage)));
    }
}
