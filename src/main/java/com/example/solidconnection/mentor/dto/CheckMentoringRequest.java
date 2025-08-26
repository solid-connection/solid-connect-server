package com.example.solidconnection.mentor.dto;

import java.util.List;

public record CheckMentoringRequest(
        List<Long> checkedMentoringIds
) {

}
