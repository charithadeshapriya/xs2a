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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReportByPeriodRequest;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.CardAccountHandler;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAccountService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiCardTransactionListToXs2aAccountReportMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountReferenceMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aBalanceMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetTransactionsReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.TransactionsReportByPeriodObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CardAccountSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled
class CardTransactionServiceTest {

    private static final JsonReader jsonReader = new JsonReader();
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final boolean WITH_BALANCE = false;
    private static final String CONSENT_ID = "Test consentId";
    private static final String ACCOUNT_ID = "Test accountId";
    private static final String IBAN = "Test IBAN";
    private static final String BBAN = "Test BBAN";
    private static final String PAN = "Test PAN";
    private static final String MASKED_PAN = "Test MASKED_PAN";
    private static final String MSISDN = "Test MSISDN";
    private static final String REQUEST_URI = "request/uri";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final LocalDate DATE_FROM = LocalDate.of(2018, 1, 1);
    private static final LocalDate DATE_TO = LocalDate.now();
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = new SpiAccountConsent();
    private static final SpiAccountReference SPI_ACCOUNT_REFERENCE_GLOBAL = buildSpiAccountReferenceGlobal();
    private static final AccountReference XS2A_ACCOUNT_REFERENCE = buildXs2aAccountReference();
    private static final SpiCardTransactionReport SPI_CARD_TRANSACTION_REPORT = buildSpiTransactionReport();
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final BookingStatus BOOKING_STATUS = BookingStatus.BOTH;
    private static final MessageError VALIDATION_ERROR = new MessageError(ErrorType.AIS_401, of(CONSENT_INVALID));
    private static final String ENTRY_REFERENCE_FROM = "777";
    private static final Boolean DELTA_LIST = Boolean.TRUE;
    private static final Xs2aTransactionsReportByPeriodRequest XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST = buildXs2aTransactionsReportByPeriodRequest();
    private static final String BASE64_STRING_EXAMPLE = "dGVzdA==";

    private SpiAccountReference spiAccountReference;
    private AccountConsent accountConsent;
    private TransactionsReportByPeriodObject transactionsReportByPeriodObject;
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;

    @InjectMocks
    private CardTransactionService cardTransactionService;

    @Mock
    private CardAccountSpi cardAccountSpi;
    @Mock
    private SpiToXs2aBalanceMapper balanceMapper;
    @Mock
    private SpiToXs2aAccountReferenceMapper referenceMapper;
    @Mock
    private SpiCardTransactionListToXs2aAccountReportMapper spiCardTransactionListToXs2aAccountReportMapper;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper consentMapper;
    @Mock
    private TppService tppService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private GetTransactionsReportValidator getTransactionsReportValidator;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private AccountHelperService accountHelperService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private Xs2aAccountService xs2aAccountService;
    @Mock
    private CardAccountHandler cardAccountHandler;

    @BeforeEach
    void setUp() {
        accountConsent = createConsent(createAccountAccess());
        spiAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        transactionsReportByPeriodObject = buildTransactionsReportByPeriodObject();
        spiAspspConsentDataProvider = spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID);
    }

    @Test
    void getCardTransactionsReportByPeriod_Failure_NoAccountConsent() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.empty());
        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);
        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    void getCardTransactionsReportByPeriod_Failure_AllowedAccountDataHasError() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getTransactionsReportValidator.validate(transactionsReportByPeriodObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getCardTransactionsReportByPeriod_Failure_SpiResponseHasError() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(accountHelperService.findAccountReference(any(), any())).thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponse(SPI_CARD_TRANSACTION_REPORT));

        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(SPI_CARD_TRANSACTION_REPORT), ServiceType.AIS))
            .thenReturn(ErrorHolder
                            .builder(AIS_400)
                            .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                            .build());

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, FORMAT_ERROR);
    }

    @Test
    void getCardTransactionsReportByPeriod_failure_accountReferenceNotFoundInAccountAccess() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getTransactionsReportValidator.validate(transactionsReportByPeriodObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getCardTransactionsReportByPeriod_With406ErrorInSpiTransactionReport() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(accountHelperService.findAccountReference(any(), any())).thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorServiceNotSupportedSpiResponse());

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, REQUESTED_FORMATS_INVALID);
    }

    @Test
    void getCardTransactionsReportByPeriod_Success() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(accountHelperService.findAccountReference(any(), any())).thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_CARD_TRANSACTION_REPORT));

        Xs2aCardAccountReport xs2aAccountReport = new Xs2aCardAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

        when(spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));

        when(referenceMapper.mapToXs2aAccountReference(spiAccountReference))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ArgumentCaptor<SpiTransactionReportParameters> argumentCaptor = ArgumentCaptor.forClass(SpiTransactionReportParameters.class);

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aCardTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getCardAccountReport()).isEqualTo(xs2aAccountReport);
        assertThat(body.getAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertThat(CollectionUtils.isEqualCollection(body.getBalances(), Collections.emptyList())).isTrue();

        verify(cardAccountSpi).requestCardTransactionsForAccount(any(SpiContextData.class), argumentCaptor.capture(), any(SpiAccountReference.class), any(SpiAccountConsent.class), eq(null));
        checkPassingParametersWithoutAnyChanges(argumentCaptor.getValue());
    }

    private void checkPassingParametersWithoutAnyChanges(SpiTransactionReportParameters parameters) {
        assertEquals(DATE_FROM, parameters.getDateFrom());
        assertEquals(DATE_TO, parameters.getDateTo());
        assertEquals(ENTRY_REFERENCE_FROM, parameters.getEntryReferenceFrom());
        assertEquals(DELTA_LIST, parameters.getDeltaList());
    }

    @Test
    void getCardTransactionsReportByPeriod_WhenConsentIsGlobal_Success() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(accountHelperService.findAccountReference(any(), any())).thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        Xs2aAccountAccess xs2aAccountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access-global.json", Xs2aAccountAccess.class);
        when(accountHelperService.findAccountReference(any(), any())).thenReturn(SPI_ACCOUNT_REFERENCE_GLOBAL);

        AccountConsent accountConsent = createConsent(xs2aAccountAccess);

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), SPI_ACCOUNT_REFERENCE_GLOBAL, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_CARD_TRANSACTION_REPORT));

        Xs2aCardAccountReport xs2aAccountReport = new Xs2aCardAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

        when(spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));

        when(referenceMapper.mapToXs2aAccountReference(SPI_ACCOUNT_REFERENCE_GLOBAL))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);

        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aCardTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getCardAccountReport()).isEqualTo(xs2aAccountReport);
        assertThat(body.getAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertTrue(CollectionUtils.isEqualCollection(body.getBalances(), Collections.emptyList()));
    }

    @Test
    void getCardTransactionsReportByPeriod_Success_ShouldRecordEvent() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(accountHelperService.findAccountReference(any(), any())).thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);
        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_CARD_TRANSACTION_REPORT));
        Xs2aCardAccountReport xs2aAccountReport = new Xs2aCardAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);
        when(spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(referenceMapper.mapToXs2aAccountReference(spiAccountReference))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);
        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_CARD_TRANSACTION_LIST_REQUEST_RECEIVED);
    }

    @Test
    void getCardTransactionsReportByPeriod_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        verify(getTransactionsReportValidator).validate(transactionsReportByPeriodObject);
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getCardTransactionsReportByPeriod_shouldRecordStatusInLoggingContext() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(accountHelperService.findAccountReference(any(), any())).thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);
        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_CARD_TRANSACTION_REPORT));
        Xs2aCardAccountReport xs2aAccountReport = new Xs2aCardAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);
        when(spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(referenceMapper.mapToXs2aAccountReference(spiAccountReference))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);
        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // When
        cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        verify(loggingContextService).storeConsentStatus(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }


    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildSuccessSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .build();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .error(new TppMessage(FORMAT_ERROR))
                   .build();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorServiceNotSupportedSpiResponse() {
        return SpiResponse.<T>builder()
                   .payload((T) SPI_CARD_TRANSACTION_REPORT)
                   .error(new TppMessage(SERVICE_NOT_SUPPORTED))
                   .build();
    }

    private void assertThatErrorIs(ResponseObject actualResponse, MessageErrorCode messageErrorCode) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(messageErrorCode);
    }

    private void assertResponseHasNoErrors(ResponseObject actualResponse) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();
    }

    private static AccountConsent createConsent(Xs2aAccountAccess access) {
        return new AccountConsent(CONSENT_ID, access, access, false, LocalDate.now(), null, 4, null, ConsentStatus.VALID, false, false, null, createTppInfo(), AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.now(), Collections.emptyMap(), OffsetDateTime.now());
    }

    private static TppInfo createTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(UUID.randomUUID().toString());
        return tppInfo;
    }

    private static Xs2aAccountAccess createAccountAccess() {
        return new Xs2aAccountAccess(Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), null, null, null, null);
    }

    private static SpiAccountReference buildSpiAccountReferenceGlobal() {
        return new SpiAccountReference(ACCOUNT_ID, null, null, null, null, null, null);
    }

    private static AccountReference buildXs2aAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY);
    }

    // Needed because SpiCardTransactionReport is final, so it's impossible to mock it
    private static SpiCardTransactionReport buildSpiTransactionReport() {
        return new SpiCardTransactionReport(BASE64_STRING_EXAMPLE, Collections.emptyList(), Collections.emptyList(), SpiTransactionReport.RESPONSE_TYPE_JSON, null);
    }

    private TransactionsReportByPeriodObject buildTransactionsReportByPeriodObject() {
        return new TransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, DATE_FROM);
    }

    @NotNull
    private static Xs2aTransactionsReportByPeriodRequest buildXs2aTransactionsReportByPeriodRequest() {
        return new Xs2aTransactionsReportByPeriodRequest(CONSENT_ID, ACCOUNT_ID, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST);
    }

    private SpiTransactionReportParameters buildSpiTransactionReportParameters() {
        return new SpiTransactionReportParameters(MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, ENTRY_REFERENCE_FROM, DELTA_LIST);
    }


}