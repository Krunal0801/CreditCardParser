package com.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatementData {
    private String cardProvider;
    private String cardLastFourDigits;
    private String cardVariant;
    private String billingCycle;
    private String paymentDueDate;
    private String totalBalance;
    private String totalTransactions;
    private String statementPeriod;
}

