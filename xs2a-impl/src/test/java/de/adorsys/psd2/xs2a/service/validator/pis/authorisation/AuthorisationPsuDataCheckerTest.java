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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationPsuDataChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AuthorisationPsuDataCheckerTest {

    private static final PsuIdData PSU_ID_DATA = new PsuIdData("PSU ID", null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_2 = new PsuIdData("PSU ID 2", null, null, null, null);

    @InjectMocks
    private AuthorisationPsuDataChecker authorisationPsuDataChecker;


    @Test
    void isPsuDataWrong_withoutMultilevelSca_samePsuId_shouldReturnFalse() {
        // When
        boolean result = authorisationPsuDataChecker.isPsuDataWrong(false, Collections.singletonList(PSU_ID_DATA), PSU_ID_DATA);

        // Then
        assertFalse(result);
    }

    @Test
    void isPsuDataWrong_withoutMultilevelSca_differentPsuId_shouldReturnTrue() {
        // When
        boolean result = authorisationPsuDataChecker.isPsuDataWrong(false, Collections.singletonList(PSU_ID_DATA), PSU_ID_DATA_2);

        // Then
        assertTrue(result);
    }

    @Test
    void isPsuDataWrong_withMultilevelSca_samePsuId_shouldReturnFalse() {
        // When
        boolean result = authorisationPsuDataChecker.isPsuDataWrong(true, Collections.singletonList(PSU_ID_DATA), PSU_ID_DATA);

        // Then
        assertFalse(result);
    }

    @Test
    void isPsuDataWrong_withMultilevelSca_differentPsuId_shouldReturnFalse() {
        // When
        boolean result = authorisationPsuDataChecker.isPsuDataWrong(true, Collections.singletonList(PSU_ID_DATA), PSU_ID_DATA_2);

        // Then
        assertFalse(result);
    }

}
