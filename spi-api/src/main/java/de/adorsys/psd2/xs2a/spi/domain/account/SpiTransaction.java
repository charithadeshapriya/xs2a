/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.spi.domain.account;

import de.adorsys.psd2.xs2a.core.pis.Remittance;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@RequiredArgsConstructor
public class SpiTransaction {
    private final String transactionId;
    private final String entryReference;
    private final String endToEndId;
    private final String mandateId;
    private final String checkId;
    private final String creditorId;
    private final LocalDate bookingDate;
    private final LocalDate valueDate;
    private final SpiAmount spiAmount;
    private final List<SpiExchangeRate> exchangeRate;
    private final String creditorName;
    private final SpiAccountReference creditorAccount;
    private final String creditorAgent;
    private final String ultimateCreditor;
    private final String debtorName;
    private final SpiAccountReference debtorAccount;
    private final String debtorAgent;
    private final String ultimateDebtor;
    private final String remittanceInformationUnstructured;
    private final List<String> remittanceInformationUnstructuredArray;
    private final Remittance remittanceInformationStructured;
    private final List<Remittance> remittanceInformationStructuredArray;
    private final String purposeCode;
    private final String bankTransactionCodeCode;
    private final String proprietaryBankTransactionCode;
    private final String additionalInformation;
    private final SpiAdditionalInformationStructured additionalInformationStructured;
    private final SpiAccountBalance balanceAfterTransaction;

    /**
     * @deprecated since 6.0/7.0, use all args constructor instead
     */
    @Deprecated // ToDo remove deprecated constructor https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1240
    public SpiTransaction(String transactionId, String entryReference, String endToEndId, String mandateId, String checkId,
                          String creditorId, LocalDate bookingDate, LocalDate valueDate, SpiAmount spiAmount,
                          List<SpiExchangeRate> exchangeRate, String creditorName, SpiAccountReference creditorAccount,
                          String creditorAgent, String ultimateCreditor, String debtorName, SpiAccountReference debtorAccount,
                          String debtorAgent, String ultimateDebtor, String remittanceInformationUnstructured,
                          Remittance remittanceInformationStructured, String purposeCode, String bankTransactionCodeCode,
                          String proprietaryBankTransactionCode, SpiAdditionalInformationStructured additionalInformationStructured,
                          SpiAccountBalance balanceAfterTransaction) {
        this(transactionId, entryReference, endToEndId, mandateId, checkId, creditorId, bookingDate, valueDate, spiAmount,
             exchangeRate, creditorName, creditorAccount, creditorAgent, ultimateCreditor, debtorName, debtorAccount, debtorAgent,
             ultimateDebtor, remittanceInformationUnstructured, null, remittanceInformationStructured,
             null, purposeCode, bankTransactionCodeCode, proprietaryBankTransactionCode,
             null, additionalInformationStructured, balanceAfterTransaction);
    }

    public boolean isBookedTransaction() {
        return bookingDate != null;
    }

    public boolean isPendingTransaction() {
        return !isBookedTransaction();
    }
}
