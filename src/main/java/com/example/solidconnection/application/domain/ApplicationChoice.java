package com.example.solidconnection.application.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class ApplicationChoice {

    @Column(name = "choice_order", nullable = false)
    private int choiceOrder;

    @Column(name = "univ_apply_info_id", nullable = false)
    private long univApplyInfoId;

    protected ApplicationChoice() {
    }

    public ApplicationChoice(int choiceOrder, long univApplyInfoId) {
        this.choiceOrder = choiceOrder;
        this.univApplyInfoId = univApplyInfoId;
    }
}
