/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.service.mapper.consent;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountAccessMapper;
import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessType;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Xs2aAisConsentMapper {
    // TODO remove this dependency. Should not be dependencies between spi-api and consent-api https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/437
    private final SpiToXs2aAccountAccessMapper spiToXs2aAccountAccessMapper;

    public CreateAisConsentRequest mapToCreateAisConsentRequest(CreateConsentReq req, String psuId, String tppId) {
        return Optional.ofNullable(req)
                   .map(r -> {
                       CreateAisConsentRequest aisRequest = new CreateAisConsentRequest();
                       aisRequest.setPsuId(psuId);
                       aisRequest.setTppId(tppId);
                       aisRequest.setFrequencyPerDay(r.getFrequencyPerDay());
                       aisRequest.setAccess(mapToAisAccountAccessInfo(req.getAccess()));
                       aisRequest.setValidUntil(r.getValidUntil());
                       aisRequest.setRecurringIndicator(r.isRecurringIndicator());
                       aisRequest.setCombinedServiceIndicator(r.isCombinedServiceIndicator());

                       return aisRequest;
                   })
                   .orElse(null);
    }

    public AccountConsent mapToAccountConsent(SpiAccountConsent spiAccountConsent) {
        return Optional.ofNullable(spiAccountConsent)
                   .map(ac -> new AccountConsent(
                       ac.getId(),
                       spiToXs2aAccountAccessMapper.mapToAccountAccess(ac.getAccess()),
                       ac.isRecurringIndicator(),
                       ac.getValidUntil(),
                       ac.getFrequencyPerDay(),
                       ac.getLastActionDate(),
                       ac.getConsentStatus(),
                       ac.isWithBalance(),
                       ac.isTppRedirectPreferred()))
                   .orElse(null);
    }


    public ActionStatus mapActionStatusError(MessageErrorCode error, boolean withBalance, TypeAccess access) {
        ActionStatus actionStatus = ActionStatus.FAILURE_ACCOUNT;
        if (error == MessageErrorCode.ACCESS_EXCEEDED) {
            actionStatus = ActionStatus.CONSENT_LIMIT_EXCEEDED;
        } else if (error == MessageErrorCode.CONSENT_EXPIRED) {
            actionStatus = ActionStatus.CONSENT_INVALID_STATUS;
        } else if (error == MessageErrorCode.CONSENT_UNKNOWN_400) {
            actionStatus = ActionStatus.CONSENT_NOT_FOUND;
        } else if (error == MessageErrorCode.CONSENT_INVALID) {
            if (access == TypeAccess.TRANSACTION) {
                actionStatus = ActionStatus.FAILURE_TRANSACTION;
            } else if (access == TypeAccess.BALANCE || withBalance) {
                actionStatus = ActionStatus.FAILURE_BALANCE;
            }
        }
        return actionStatus;
    }

    public UpdateConsentPsuDataReq mapToSpiUpdateConsentPsuDataReq(UpdateConsentPsuDataResponse updatePsuDataResponse,
                                                                   UpdateConsentPsuDataReq updatePsuDataRequest) {
        return Optional.ofNullable(updatePsuDataResponse)
                   .map(data -> {
                       UpdateConsentPsuDataReq request = new UpdateConsentPsuDataReq();
                       request.setPsuId(updatePsuDataResponse.getPsuId());
                       request.setConsentId(updatePsuDataRequest.getConsentId());
                       request.setAuthorizationId(updatePsuDataRequest.getAuthorizationId());
                       request.setAuthenticationMethodId(updatePsuDataResponse.getAuthenticationMethodId());
                       request.setAuthenticationMethodId(updatePsuDataResponse.getChosenScaMethod());
                       request.setScaAuthenticationData(updatePsuDataResponse.getScaAuthenticationData());
                       request.setScaStatus(data.getScaStatus());
                       return request;
                   })
                   .orElse(null);
    }

    public List<CmsScaMethod> mapToCmsScaMethods(List<SpiScaMethod> spiScaMethods) {
        return spiScaMethods.stream()
                   .map(this::mapToCmsScaMethod)
                   .collect(Collectors.toList());
    }

    public SpiScaConfirmation mapToSpiScaConfirmation(UpdateConsentPsuDataReq request) {
        SpiScaConfirmation accountConfirmation = new SpiScaConfirmation();
        accountConfirmation.setConsentId(request.getConsentId());
        accountConfirmation.setPsuId(request.getPsuId());
        accountConfirmation.setTanNumber(request.getScaAuthenticationData());
        return accountConfirmation;
    }

    private CmsScaMethod mapToCmsScaMethod(SpiScaMethod spiScaMethod) {
        return CmsScaMethod.valueOf(spiScaMethod.name());
    }

    private AisAccountAccessInfo mapToAisAccountAccessInfo(Xs2aAccountAccess access) {
        AisAccountAccessInfo accessInfo = new AisAccountAccessInfo();
        accessInfo.setAccounts(Optional.ofNullable(access.getAccounts())
                                   .map(this::mapToListAccountInfo)
                                   .orElseGet(Collections::emptyList));

        accessInfo.setBalances(Optional.ofNullable(access.getBalances())
                                   .map(this::mapToListAccountInfo)
                                   .orElseGet(Collections::emptyList));

        accessInfo.setTransactions(Optional.ofNullable(access.getTransactions())
                                       .map(this::mapToListAccountInfo)
                                       .orElseGet(Collections::emptyList));

        accessInfo.setAvailableAccounts(Optional.ofNullable(access.getAvailableAccounts())
                                            .map(accessType -> AisAccountAccessType.valueOf(accessType.name()))
                                            .orElse(null));
        accessInfo.setAllPsd2(Optional.ofNullable(access.getAllPsd2())
                                  .map(accessType -> AisAccountAccessType.valueOf(accessType.name()))
                                  .orElse(null));

        return accessInfo;
    }

    private List<AccountInfo> mapToListAccountInfo(List<Xs2aAccountReference> refs) {
        return refs.stream()
                   .map(this::mapToAccountInfo)
                   .collect(Collectors.toList());
    }

    private AccountInfo mapToAccountInfo(Xs2aAccountReference ref) {
        AccountInfo info = new AccountInfo();
        info.setIban(ref.getIban());
        info.setCurrency(Optional.ofNullable(ref.getCurrency())
                             .map(Currency::getCurrencyCode)
                             .orElse(null));
        return info;
    }
}
