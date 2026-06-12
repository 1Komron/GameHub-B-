package com.gamehubbot.match.application;

import com.gamehubbot.match.infrastructure.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class MatchJoinCodeGenerator {
    private final MatchRepository matchRepository;
    private final Random random = new Random();

    public String generateJoinCode() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        String joinCode;

        do {
            StringBuilder sb = new StringBuilder(7);
            int alphabetLength = alphabet.length();

            for (int i = 0; i < 7; i++) {
                sb.append(alphabet.charAt(random.nextInt(alphabetLength)));
            }

            joinCode = sb.toString();
        } while (matchRepository.existsJoinCode(joinCode));
        return joinCode;
    }
}
