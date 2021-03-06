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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AisEndpointAccessCheckerServiceTest {

    private static final String AUTHORISATION_ID = "11111111";

    @InjectMocks
    private AisEndpointAccessCheckerService aisEndpointAccessCheckerService;

    @Mock
    private Xs2aAisConsentService aisConsentService;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @Test
    void isEndpointAccessible_Received_false() {

        when(aspspProfileService.isAuthorisationConfirmationRequestMandated())
            .thenReturn(true);

        when(aisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAccountConsentAuthorization(ScaStatus.RECEIVED, ScaApproach.REDIRECT)));

        boolean actual = aisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, true);

        assertFalse(actual);
    }

    @Test
    void isEndpointAccessible_Unconfirmed_true() {

        when(aspspProfileService.isAuthorisationConfirmationRequestMandated())
            .thenReturn(true);

        when(aisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAccountConsentAuthorization(ScaStatus.UNCONFIRMED, ScaApproach.REDIRECT)));

        boolean actual = aisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, true);

        assertTrue(actual);
    }

    @Test
    void isEndpointAccessible_Unconfirmed_Decoupled_true() {

        when(aspspProfileService.isAuthorisationConfirmationRequestMandated())
            .thenReturn(true);

        when(aisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAccountConsentAuthorization(ScaStatus.UNCONFIRMED, ScaApproach.DECOUPLED)));

        boolean actual = aisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, true);

        assertTrue(actual);
    }

    @Test
    void isEndpointAccessible_Unconfirmed_Embedded_true() {

        when(aspspProfileService.isAuthorisationConfirmationRequestMandated())
            .thenReturn(true);

        when(aisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAccountConsentAuthorization(ScaStatus.UNCONFIRMED, ScaApproach.EMBEDDED)));

        boolean actual = aisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, true);

        assertTrue(actual);
    }

    private Authorisation buildAccountConsentAuthorization(ScaStatus scaStatus, ScaApproach scaApproach) {
        Authorisation authorisation = new Authorisation();
        authorisation.setChosenScaApproach(scaApproach);
        authorisation.setScaStatus(scaStatus);
        return authorisation;
    }

}
